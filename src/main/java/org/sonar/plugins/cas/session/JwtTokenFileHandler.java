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

class JwtTokenFileHandler {
    private String sessionStorePath;

    JwtTokenFileHandler(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
    }

    boolean isJwtStored(String jwtId) {
        String jwtFile = sessionStorePath + File.separator + jwtId;
        return Files.exists(Paths.get(jwtFile));
    }

    public SimpleJwt get(String jwtId) throws IOException {
        Charset charset = Charset.forName("US-ASCII");
        Path filePath = Paths.get(sessionStorePath + File.separator + jwtId);
        try (BufferedReader reader = Files.newBufferedReader(filePath, charset)) {
            return JAXB.unmarshal(reader, SimpleJwt.class);
        }
    }

    void store(String jwtId, SimpleJwt jwt) throws IOException {
        if (StringUtils.isEmpty(jwtId)) {
            throw new IllegalArgumentException("Could not store JWT: jwtId must not be null");
        }
        if (jwt == null) {
            throw new IllegalArgumentException("Could not store JWT: jwt must not be null");
        }

        String jwtFile = sessionStorePath + File.separator + jwtId;
        Path path = Files.createFile(Paths.get(jwtFile));

        Charset charset = Charset.forName("US-ASCII");
        try (BufferedWriter writer = Files.newBufferedWriter(path, charset)) {
            JAXB.marshal(jwt, writer);
            writer.flush();
        }
    }

    void replace(String jwtId, SimpleJwt invalidated) throws IOException {
        if (StringUtils.isEmpty(jwtId)) {
            throw new IllegalArgumentException("Could not store JWT: jwtId must not be null");
        }
        if (invalidated == null) {
            throw new IllegalArgumentException("Could not store JWT: jwt must not be null");
        }

        String jwtFile = sessionStorePath + File.separator + jwtId;

        Files.delete(Paths.get(jwtFile));

        store(jwtId, invalidated);
    }
}
