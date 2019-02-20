package org.sonar.plugins.cas.session;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.cas.util.SimpleJwt;

import javax.xml.bind.JAXB;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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

        String jwtFile = sessionStorePath + File.separator + grantingTicketId;
        Path path = Files.createFile(Paths.get(jwtFile));

        Charset charset = Charset.forName("US-ASCII");
        try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
            JAXB.marshal(jwt, writer);
            writer.flush();
        }
    }

    public SimpleJwt get(String grantingTicketId) throws IOException {
        Charset charset = Charset.forName("US-ASCII");
        Path filePath = Paths.get(sessionStorePath + File.separator + grantingTicketId);
        try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
            return JAXB.unmarshal(reader, SimpleJwt.class);
        }
    }

    void replace(String grantingTicketId, SimpleJwt invalidated) throws IOException {
        if (StringUtils.isEmpty(grantingTicketId)) {
            throw new IllegalArgumentException("Could not replace ST->JWT: jwtId must not be null");
        }
        if (invalidated == null) {
            throw new IllegalArgumentException("Could not replace ST->JWT: jwt must not be null");
        }

        String jwtFile = sessionStorePath + File.separator + grantingTicketId;

        Files.delete(Paths.get(jwtFile));

        store(grantingTicketId, invalidated);
    }
}
