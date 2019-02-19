package org.sonar.plugins.cas.session;

import org.sonar.plugins.cas.util.SimpleJwt;

class GrantingTicketFileHandler {
    private String sessionStorePath;

    public GrantingTicketFileHandler(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
    }

    public SimpleJwt store(String key, SimpleJwt value) {
        return null;
    }

    public SimpleJwt get(String grantingTicketId) {
        return null;
    }

    public void replace(String grantingTicketId, SimpleJwt invalidated) {

    }
}
