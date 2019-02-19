package org.sonar.plugins.cas.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.cas.util.SimpleJwt;
import org.sonar.plugins.cas.util.SonarCasProperties;

public final class CasSessionStore {
    private static final Logger LOG = LoggerFactory.getLogger(CasSessionStore.class);
    /**
     * This map contains the CAS granting ticket and the issued JWT. This map is only hit during back-channel logout.
     */
    private static GrantingTicketFileHandler ticketToJwt;
    /**
     * This map provides the CAS plugin with information about a JWT's validity. This collection is hit on every Sonar
     * request and must be super-fast.
     */
    private static JwtTokenFileHandler jwtIdToJwt;

    static {
        String sessionStorePath = SonarCasProperties.SESSION_STORE_PATH.getStringProperty();
        LOG.info("Creating CAS session store with path {}", sessionStorePath);
        ticketToJwt = new GrantingTicketFileHandler(sessionStorePath);
        jwtIdToJwt = new JwtTokenFileHandler(sessionStorePath);
    }

    public static void store(String ticket, SimpleJwt jwt) {
        ticketToJwt.put(ticket, jwt);
        jwtIdToJwt.put(jwt.getJwtId(), jwt);
    }

    public static boolean isJwtStored(SimpleJwt jwt) {
        return jwtIdToJwt.containsKey(jwt.getJwtId());
    }

    public static SimpleJwt getJwtById(SimpleJwt jwt) {
        SimpleJwt result = jwtIdToJwt.get(jwt.getJwtId());
        if (result == null) {
            result = SimpleJwt.getNullObject();
        }

        return result;
    }

    /**
     * Render existing JWT invalid.
     *
     * @param grantingTicketId the CAS granting ticket ID
     * @return the JWT id which is now invalid.
     */
    public static String invalidateJwt(String grantingTicketId) {
        SimpleJwt jwt = ticketToJwt.get(grantingTicketId);
        if (jwt == null) {
            return "no ticket found";
        }

        SimpleJwt invalidated = jwt.cloneAsInvalidated();

        jwtIdToJwt.replace(jwt.getJwtId(), invalidated);
        ticketToJwt.replace(grantingTicketId, invalidated);

        return invalidated.getJwtId();
    }

    public static void pruneExpiredEntries() {

    }
}
