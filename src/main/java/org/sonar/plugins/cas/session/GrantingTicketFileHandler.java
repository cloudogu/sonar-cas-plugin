package org.sonar.plugins.cas.session;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class GrantingTicketFileHandler {
    private String sessionStorePath;

    GrantingTicketFileHandler(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
    }

    public void store(String grantingTicketId, SimpleJwt jwt) throws IOException {
        if (StringUtils.isEmpty(grantingTicketId)) {
            throw new IllegalArgumentException("Could not store ST->JWT: serviceTicket must not be null");
        }
        if (jwt == null) {
            throw new IllegalArgumentException("Could not store ST->JWT: jwt must not be null");
        }

        byte[] jwtIdAsBytes = jwt.getJwtId().getBytes();
        Path path = Paths.get(sessionStorePath, grantingTicketId);
        Files.write(path, jwtIdAsBytes);
    }

    public String get(String grantingTicketId) throws IOException {
        Path path = Paths.get(sessionStorePath, grantingTicketId);
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes);
    }
}
