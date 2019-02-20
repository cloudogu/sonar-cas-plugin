package org.sonar.plugins.cas.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.io.IOException;

@ServerSide
public class CasSessionStoreFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CasSessionStoreFactory.class);
    private final CasSessionStore impl;

    public CasSessionStoreFactory(Configuration configuration) throws IOException {
        String sessionStorePath = SonarCasProperties.SESSION_STORE_PATH.getStringProperty();

        LOG.debug("creating instance of CAS file session store implementation");
        impl = new FileSessionStore(sessionStorePath);
        impl.prepareForWork();
    }

    public CasSessionStore getInstance() {
        return impl;
    }
}
