package org.sonar.plugins.cas.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileSessionStore implements CasSessionStore {
    private static final Logger LOG = LoggerFactory.getLogger(FileSessionStore.class);
    private final String sessionStorePath;

    /**
     * This map provides the CAS plugin with information about a JWT's validity. This collection is hit on every Sonar
     * request and must be super-fast.
     */
    private SessionFileHandler fileHandler;

    /**
     * default visibility constructor for testing
     */
    FileSessionStore(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
        this.fileHandler = new SessionFileHandler(sessionStorePath);
    }

    public void prepareForWork() {
        try {
            createSessionDirectory();
        } catch (IOException e) {
            throw new CasInitializationException(e);
        }
    }

    private void createSessionDirectory() throws IOException {
        Path sessionStoreDir = Paths.get(sessionStorePath);
        LOG.info("Creating CAS session writeJwtFile with path {}", sessionStoreDir.toString());

        Files.createDirectories(sessionStoreDir);
    }

    public void store(String ticket, SimpleJwt jwt) {
        LOG.debug("writeJwtFile ticket {} to token {}", ticket, jwt.getJwtId());
        try {
            fileHandler.createServiceTicketFile(ticket, jwt);
            fileHandler.writeJwtFile(jwt.getJwtId(), jwt);
        } catch (IOException e) {
            LOG.error("Could not writeJwtFile JWT " + jwt.getJwtId() + "to storage path.", e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }
    }

    public boolean isJwtStored(SimpleJwt jwt) {
        boolean stored = fileHandler.isJwtStored(jwt.getJwtId());
        LOG.debug("check if JWT {} is stored: {}", jwt.getJwtId(), stored);

        return stored;
    }

    public SimpleJwt fetchStoredJwt(SimpleJwt jwt) {
        LOG.debug("get token {}", jwt.getJwtId());

        SimpleJwt result;
        try {
            result = fileHandler.readJwtFile(jwt.getJwtId());
        } catch (Exception e) {
            LOG.error("Could not return JWT file " + jwt.getJwtId(), e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }
        if (result == null) {
            result = SimpleJwt.getNullObject();
        }

        return result;
    }

    public String invalidateJwt(String serviceTicketId) {
        LOG.debug("invalidate token by ticket {}", serviceTicketId);

        SimpleJwt jwt;
        try {
            String jwtId = fileHandler.readServiceTicketFile(serviceTicketId);
            jwt = fileHandler.readJwtFile(jwtId);
        } catch (IOException e) {
            LOG.error("Could not invalidate JWT with granting ticket " + serviceTicketId, e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }
        if (jwt == null) {
            return "no ticket found";
        }

        SimpleJwt invalidated = jwt.cloneAsInvalidated();

        try {
            fileHandler.replaceJwtFile(jwt.getJwtId(), invalidated);
        } catch (IOException e) {
            LOG.error("Could not invalidate JWT file " + jwt.getJwtId(), e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }

        LOG.debug("successfully invalidated token {} by ticket {}", jwt.getJwtId(), serviceTicketId);

        return invalidated.getJwtId();
    }

    @Override
    public void refreshJwt(SimpleJwt jwtWithLongerExpirationDate) {
        String jwtId = jwtWithLongerExpirationDate.getJwtId();
        LOG.debug("refresh token {}", jwtId);

        try {
            fileHandler.replaceJwtFile(jwtId, jwtWithLongerExpirationDate);
        } catch (IOException e) {
            LOG.error("Could not invalidate JWT file " + jwtId, e);
            throw new CasIOAuthenticationException("An authentication problem occurred. Please let your SonarQube administrator know.");
        }

        LOG.debug("successfully refreshed token {}", jwtId);
    }

    public int removeExpiredEntries() {
        return new SessionFileRemover(sessionStorePath).cleanUp();
    }

    private static class CasIOAuthenticationException extends RuntimeException {
        CasIOAuthenticationException(String message) {
            super(message);
        }
    }

    private class CasInitializationException extends RuntimeException {
        CasInitializationException(IOException e) {
            super(e);
        }
    }
}
