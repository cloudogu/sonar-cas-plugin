package org.sonar.plugins.cas;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.BaseIdentityProvider;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.plugins.cas.logout.CasSonarSignOutInjectorFilter;
import org.sonar.plugins.cas.logout.LogoutHandler;
import org.sonar.plugins.cas.util.SonarCasProperties;

/**
 * The {@link CasIdentityProvider} is responsible for the browser based cas sso authentication. The authentication
 * workflow for an unauthenticated user is as follows:
 *
 * <ol>
 * <li>the {@link ForceCasLoginFilter} redirects the user to /sessions/new</li>
 * <li>the {@link AuthenticationFilter} redirects the user to the CAS Server</li>
 * <li>the user authenticates to the CAS Server</li>
 * <li>the CAS Server redirects back to /sessions/init/sonarqube</li>
 *
 * <li>the {@link CasIdentityProvider} is called by sonarqube (InitFilter) and creates the user from the assertions and
 * redirects the user to the root of sonarqube.
 * During this phase:
 * <ol>
 * <li>the generated JWT token is fetched from the response</li>
 * <li>the CAS granting ticket is stored along the JWT for later backchannel logout</li>
 * <li>the JWT is stored for black/whitelisting of each incoming </li>
 * </ol>
 * </li>
 * <li>the user logs out at some point (by Javascript injection to the backchannel single logout URL {@link CasSonarSignOutInjectorFilter})</li>
 * <li>CAS requests a logout with a Service Ticket which contains the original Service Ticket
 * <ul>
 * <li>the stored JWT is invalidated and stored back.</li>
 * </ul>
 * </li>
 * <li>The user with an existing JWT cannot re-use existing JWT</li>
 * </ol>
 */
@ServerSide
public class CasIdentityProvider implements BaseIdentityProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CasIdentityProvider.class);

    private final Configuration configuration;
    private final LoginHandler loginHandler;
    private final LogoutHandler logoutHandler;

    /**
     * called with injection by SonarQube during server initialization
     */
    public CasIdentityProvider(Configuration configuration, LoginHandler loginHandler, LogoutHandler logoutHandler) {
        this.configuration = configuration;
        this.loginHandler = loginHandler;
        this.logoutHandler = logoutHandler;
    }

    @Override
    public void init(Context context) {
        try {
            HttpRequest request = context.getHttpRequest();
            LOG.debug("Initialize CAS identity handling for URL " + request.getRequestURL());
            if (isLogin(request)) {
                LOG.debug("Found internal login case");
                loginHandler.handleLogin(context);
            } else if (isLogout(request)) {
                LOG.debug("Found external logout case");
                logoutHandler.logout(context.getHttpRequest(), context.getHttpResponse());
            } else {
                LOG.debug("CasIdentityProvider found an unexpected case. Ignoring this request to {}", request.getRequestURL());
            }
        } catch (Exception e) {
            LOG.debug("authentication or logout failed", e);
        }
    }

    private boolean isLogin(HttpRequest request) {
        String ticket = request.getParameter("ticket");
        String requestMethod = request.getMethod();
        String path = request.getRequestURL();

        return "GET".equals(requestMethod) && StringUtils.isNotBlank(ticket) && !path.contains("sessions/logout");
    }

    /**
     * Local log-out and Single Log-out is done by a back-channel logout.
     */
    private boolean isLogout(HttpRequest request) {
        String logoutAttributes = request.getParameter("logoutRequest");
        String requestMethod = request.getMethod();
        String path = request.getRequestURL();

        return ("POST".equals(requestMethod) && StringUtils.isNotBlank(logoutAttributes)) || path.contains("sessions/logout");
    }

    @Override
    public String getKey() {
        return "sonarqube";
    }

    @Override
    public String getName() {
        return "CAS";
    }

    @Override
    public Display getDisplay() {
        return Display.builder()
                .setBackgroundColor("#143E51")
                .setIconPath("/static/casplugin/cas_logo.png")
                .build();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean allowsUsersToSignUp() {
        return SonarCasProperties.SONAR_CREATE_USERS.mustGetBoolean(configuration);
    }
}
