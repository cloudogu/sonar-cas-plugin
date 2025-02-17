package org.sonar.plugins.cas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.api.server.http.HttpResponse;
import org.sonar.api.web.FilterChain;
import org.sonar.api.web.HttpFilter;
import org.sonar.api.web.UrlPattern;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.io.IOException;

/**
 * The {@link AuthenticationFilter} always redirects to the CAS Server.
 *
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
public class AuthenticationFilter extends HttpFilter {
    static final String SONAR_LOGIN_URL_PATH = "/sessions/new";
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final Configuration config;

    /** called with injection by SonarQube during server initialization */
    public AuthenticationFilter(Configuration configuration) {
        this.config = configuration;
    }

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create(SONAR_LOGIN_URL_PATH);
    }

    @Override
    public void init() {
        // nothing to init
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws IOException {
        String loginRedirectUrl = getCasLoginUrl() + "?service=" + getSonarServiceUrl();
        LOG.debug("redirecting for CAS authentication to {}", loginRedirectUrl);
        response.sendRedirect(loginRedirectUrl);
    }

    private String getCasLoginUrl() {
        return SonarCasProperties.CAS_SERVER_LOGIN_URL.mustGetString(config);
    }

    private String getSonarServiceUrl() {
        String sonarUrl = SonarCasProperties.SONAR_SERVER_URL.mustGetString(config);
        return sonarUrl + "/sessions/init/sonarqube";
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }
}
