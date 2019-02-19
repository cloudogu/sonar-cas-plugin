package org.sonar.plugins.cas.session;

import org.sonar.plugins.cas.util.SimpleJwt;

public class JwtTokenFileHandler {
    private String sessionStorePath;

    public JwtTokenFileHandler(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
    }

    public boolean containsKey(Object key) {
        return false;
    }

    public SimpleJwt get(Object key) {
        return null;
    }


    public SimpleJwt put(String key, SimpleJwt value) {
        return null;
    }

    public void replace(String jwtId, SimpleJwt invalidated) {

    }
}
