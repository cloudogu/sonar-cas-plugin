package org.sonar.plugins.cas;

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

@ServerSide
public final class CasRestClientFactory {
    private CasRestClient impl;
    private Configuration configuration;

    /** This constructor is used with Dependency Injection during SonarQube start-up time*/
    @SuppressWarnings("unused")
    public CasRestClientFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    CasRestClientFactory(Configuration configuration, CasRestClient impl) {
        this.configuration = configuration;
        this.impl = impl;
    }

    CasRestClient create() {
        if(impl != null) {
            return impl;
        }

        String casServerUrlPrefix = getCasServerUrlPrefix();
        String serviceUrl = getServiceUrl();
        return new CasRestClient(casServerUrlPrefix, serviceUrl);
    }

    private String getCasServerUrlPrefix() {
        return SonarCasProperties.CAS_SERVER_URL_PREFIX.mustGetString(configuration);
    }

    private String getServiceUrl() {
        return SonarCasProperties.SONAR_SERVER_URL.mustGetString(configuration);
    }
}
