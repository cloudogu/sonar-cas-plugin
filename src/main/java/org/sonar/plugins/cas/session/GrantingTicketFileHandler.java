package org.sonar.plugins.cas.session;

import org.sonar.plugins.cas.util.SimpleJwt;

class GrantingTicketFileHandler {
    private String sessionStorePath;

    public GrantingTicketFileHandler(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
    }

    public SimpleJwt put(String key, SimpleJwt value) {
        return null;
    }

    public void replace(String grantingTicketId, SimpleJwt invalidated) {

    }

    public SimpleJwt get(String grantingTicketId) {
        return null;
    }
}
