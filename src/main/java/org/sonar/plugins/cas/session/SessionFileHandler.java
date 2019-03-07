package org.sonar.plugins.cas.session;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.cas.util.JwtFiles;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class handles all the necessary file actions towards JWT and Service Ticket files for {@link FileSessionStore}.
 */
class SessionFileHandler {

    private String sessionStorePath;

    SessionFileHandler(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
    }

    boolean isJwtStored(String jwtId) {
        Path path = Paths.get(sessionStorePath, jwtId);
        return Files.exists(path);
    }

    SimpleJwt readJwtFile(String jwtId) {
        Path filePath = Paths.get(sessionStorePath, jwtId);
        return JwtFiles.unmarshal(filePath);
    }

    void writeJwtFile(String jwtId, SimpleJwt jwt) {
        if (StringUtils.isEmpty(jwtId)) {
            throw new IllegalArgumentException("Could not writeJwtFile JWT: jwtId must not be null");
        }
        if (jwt == null) {
            throw new IllegalArgumentException("Could not writeJwtFile JWT: jwt must not be null");
        }

        Path path = Paths.get(sessionStorePath, jwtId);

        JwtFiles.marshalIntoNewFile(path, jwt);
    }

    void replaceJwtFile(String jwtId, SimpleJwt invalidated) throws IOException {
        if (StringUtils.isEmpty(jwtId)) {
            throw new IllegalArgumentException("Could not replaceJwtFile JWT: jwtId must not be null");
        }
        if (invalidated == null) {
            throw new IllegalArgumentException("Could not replaceJwtFile JWT: jwt must not be null");
        }

        Path path = Paths.get(sessionStorePath, jwtId);
        Files.delete(path);

        writeJwtFile(jwtId, invalidated);
    }

    void createServiceTicketFile(String serviceTicket, SimpleJwt jwt) throws IOException {
        if (StringUtils.isEmpty(serviceTicket)) {
            throw new IllegalArgumentException("Could not create ServiceTicketFile ST->JWT: serviceTicket must not be null");
        }
        if (jwt == null) {
            throw new IllegalArgumentException("Could not create ServiceTicketFile ST->JWT: jwt must not be null");
        }

        byte[] jwtIdAsBytes = jwt.getJwtId().getBytes();
        Path path = Paths.get(sessionStorePath, serviceTicket);
        Files.write(path, jwtIdAsBytes);
    }

    String readServiceTicketFile(String serviceTicket) throws IOException {
        Path path = Paths.get(sessionStorePath, serviceTicket);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes);
    }
}
