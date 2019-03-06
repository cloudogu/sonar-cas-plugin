package org.sonar.plugins.cas.session;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.cas.util.JwtFileUtil;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class JwtTokenFileHandler {

    private String sessionStorePath;

    JwtTokenFileHandler(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
    }

    boolean isJwtStored(String jwtId) {
        Path path = Paths.get(sessionStorePath, jwtId);
        return Files.exists(path);
    }

    public SimpleJwt get(String jwtId) {
        Path filePath = Paths.get(sessionStorePath, jwtId);
        return new JwtFileUtil().unmarshal(filePath);
    }

    void store(String jwtId, SimpleJwt jwt) throws IOException {
        if (StringUtils.isEmpty(jwtId)) {
            throw new IllegalArgumentException("Could not store JWT: jwtId must not be null");
        }
        if (jwt == null) {
            throw new IllegalArgumentException("Could not store JWT: jwt must not be null");
        }

        Path path = Paths.get(sessionStorePath, jwtId);

        new JwtFileUtil().marshalIntoNewFile(path, jwt);
    }

    void replace(String jwtId, SimpleJwt invalidated) throws IOException {
        if (StringUtils.isEmpty(jwtId)) {
            throw new IllegalArgumentException("Could not replace JWT: jwtId must not be null");
        }
        if (invalidated == null) {
            throw new IllegalArgumentException("Could not replace JWT: jwt must not be null");
        }

        Path path = Paths.get(sessionStorePath, jwtId);
        Files.delete(path);

        store(jwtId, invalidated);
    }
}
