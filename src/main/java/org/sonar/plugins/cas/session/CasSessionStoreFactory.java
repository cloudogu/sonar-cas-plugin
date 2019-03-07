package org.sonar.plugins.cas.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.platform.Server;
import org.sonar.api.platform.ServerStartHandler;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.io.IOException;

@ServerSide
public class CasSessionStoreFactory implements ServerStartHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CasSessionStoreFactory.class);
    private final CasSessionStore impl;

    /** called with injection by SonarQube during server initialization */
    public CasSessionStoreFactory(Configuration configuration) {
        String sessionStorePath = SonarCasProperties.SESSION_STORE_PATH.mustGetString(configuration);

        LOG.debug("creating instance of CAS file session writeJwtFile implementation");
        impl = new FileSessionStore(sessionStorePath);
    }

    public CasSessionStore getInstance() {
        return impl;
    }

    @Override
    public void onServerStart(Server server) {
        try {
            impl.prepareForWork();
        } catch (IOException e) {
            throw new CasSessionStoreInitializationException(e);
        }
    }

    private class CasSessionStoreInitializationException extends RuntimeException {
        private CasSessionStoreInitializationException(IOException e) {
            super("Could not prepare CAS session writeJwtFile for work.", e);
        }
    }
}
