package org.sonar.plugins.cas.session;

import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

class JwtTokenFileHandler {
    private String sessionStorePath;

    public JwtTokenFileHandler(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
    }

    public boolean isJwtStored(String jwtId) {
        return Files.exists(Paths.get(sessionStorePath + File.separator + jwtId));
    }

    public SimpleJwt get(String jwtId) {
        return null;
    }


    public SimpleJwt store(String jwtId, SimpleJwt jwt) {
        return null;
    }

    public void replace(String jwtId, SimpleJwt invalidated) {

    }
}
