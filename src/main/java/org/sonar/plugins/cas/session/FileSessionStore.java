package org.sonar.plugins.cas.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.cas.util.SimpleJwt;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.io.IOException;

public final class FileSessionStore implements CasSessionStore {
    private static final Logger LOG = LoggerFactory.getLogger(FileSessionStore.class);

    private static final FileSessionStore STORE = new FileSessionStore();

    /**
     * This map contains the CAS granting ticket and the issued JWT. This map is only hit during back-channel logout.
     */
    private GrantingTicketFileHandler ticketToJwt;
    /**
     * This map provides the CAS plugin with information about a JWT's validity. This collection is hit on every Sonar
     * request and must be super-fast.
     */
    private JwtTokenFileHandler jwtIdToJwt;

    private FileSessionStore() {
        this(SonarCasProperties.SESSION_STORE_PATH.getStringProperty());
    }

    /**
     * default visibility constructor for testing
     */
    FileSessionStore(String sessionStorePath) {
        LOG.info("Creating CAS session STORE with path {}", sessionStorePath);
        ticketToJwt = new GrantingTicketFileHandler(sessionStorePath);
        jwtIdToJwt = new JwtTokenFileHandler(sessionStorePath);
    }

    public static CasSessionStore getInstance() {
        return STORE;
    }

    public void store(String ticket, SimpleJwt jwt) {
        ticketToJwt.store(ticket, jwt);
        try {
            jwtIdToJwt.store(jwt.getJwtId(), jwt);
        } catch (IOException e) {
            LOG.error("Could not store JWT {} to storage path {}", jwt.getJwtId());
        }
    }

    public boolean isJwtStored(SimpleJwt jwt) {
        return jwtIdToJwt.isJwtStored(jwt.getJwtId());
    }

    public SimpleJwt getJwtById(SimpleJwt jwt) {
        SimpleJwt result = null;
        try {
            result = jwtIdToJwt.get(jwt.getJwtId());
        } catch (IOException e) {
            LOG.error("Could not find JWT file {}", jwt.getJwtId());
        }
        if (result == null) {
            result = SimpleJwt.getNullObject();
        }

        return result;
    }

    public String invalidateJwt(String grantingTicketId) {
        SimpleJwt jwt = ticketToJwt.get(grantingTicketId);
        if (jwt == null) {
            return "no ticket found";
        }

        SimpleJwt invalidated = jwt.cloneAsInvalidated();

        jwtIdToJwt.replace(jwt.getJwtId(), invalidated);
        ticketToJwt.replace(grantingTicketId, invalidated);

        return invalidated.getJwtId();
    }

    public void pruneExpiredEntries() {
        // TODO prune all the expired things
    }
}
