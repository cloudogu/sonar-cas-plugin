package org.sonar.plugins.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.platform.Server;
import org.sonar.api.platform.ServerStartHandler;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.IgnoreCert;
import org.sonar.plugins.cas.util.SonarCasProperties;

/**
 * This class deactivates SSL support during SonarQube start-up and should ONLY be used for development purposes.
 */
@ServerSide
public class DevelopmentServerStartHandler implements ServerStartHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DevelopmentServerStartHandler.class);

    private Configuration configuration;

    public DevelopmentServerStartHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onServerStart(Server server) {
        if (isSslSupportDeactivated()) {
            LOG.error("SSL certificate check is disabled. Please ENABLE SSL disabling on a production machine for " +
                    "security reasons by configuring the property 'sonar.cas.disableCertValidation'.");
            IgnoreCert.disableSslVerification();
        }
    }

    private boolean isSslSupportDeactivated() {
        return SonarCasProperties.DISABLE_CERT_VALIDATION.getBoolean(configuration, false);
    }
}
