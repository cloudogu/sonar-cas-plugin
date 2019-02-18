package org.sonar.plugins.cas;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.BaseIdentityProvider;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.plugins.cas.logout.CasSonarSignOutInjectorFilter;
import org.sonar.plugins.cas.logout.LogoutHandler;
import org.sonar.plugins.cas.util.JwtProcessor;
import org.sonar.plugins.cas.util.SimpleJwt;
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * The {@link CasIdentifyProvider} is responsible for the browser based cas sso authentication. The authentication
 * workflow for an unauthenticated user is as follows:
 *
 * <ol>
 * <li>the {@link ForceCasLoginFilter} redirects the user to /sessions/new</li>
 * <li>the {@link AuthenticationFilter} redirects the user to the CAS Server</li>
 * <li>the user authenticates to the CAS Server</li>
 * <li>the CAS Server redirects back to /sessions/init/cas</li>
 *
 * <li>the {@link CasIdentifyProvider} is called by sonarqube (InitFilter) and creates the user from the assertions and
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
public class CasIdentifyProvider implements BaseIdentityProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CasIdentifyProvider.class);

    private final CasAttributeSettings attributeSettings;

    public CasIdentifyProvider(CasAttributeSettings attributeSettings) {
        this.attributeSettings = attributeSettings;
    }

    @Override
    public void init(Context context) {
        try {
            if (isLogin(context.getRequest())) {
                handleAuthentication(context);
            } else if (isLogout(context.getRequest())) {
                handleLogout(context);
            } else {
                LOG.debug("CasIdentifyProvider found an expected case. Ignoring this request to {}", context.getRequest().getRequestURL());
            }
        } catch (Exception e) {
            LOG.error("authentication failed", e);
        }
    }

    private void handleAuthentication(Context context) throws IOException, TicketValidationException {
        LOG.debug("Found internal login case");

        String grantingTicket = context.getRequest().getParameter("ticket");
        Cas30ProxyTicketValidator validator = new Cas30ProxyTicketValidator(getCasServerUrlPrefix());
        Assertion assertion = validator.validate(grantingTicket, getSonarServiceUrl());

        context.authenticate(createUserIdentity(assertion));

        Collection<String> headers = context.getResponse().getHeaders("Set-Cookie");
        SimpleJwt jwt = JwtProcessor.getJwtTokenFromRequestHeaders(headers);
        LOG.debug("Storing granting ticket {} with JWT {}", grantingTicket, jwt.getJwtId());
        CasSessionStore.store(grantingTicket, jwt);

        // redirect back to start page
        // TODO what about opened page? lost?
        context.getResponse().sendRedirect(StringUtils.defaultIfEmpty(context.getRequest().getContextPath(), "/"));
    }

    private UserIdentity createUserIdentity(Assertion assertion) {
        AttributePrincipal principal = assertion.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();

        UserIdentity.Builder builder = UserIdentity.builder()
                .setLogin(principal.getName())
                .setProviderLogin(principal.getName());

        String displayName = attributeSettings.getDisplayName(attributes);
        if (!Strings.isNullOrEmpty(displayName)) {
            builder = builder.setName(displayName);
        }

        String email = attributeSettings.getEmail(attributes);
        if (!Strings.isNullOrEmpty(email)) {
            builder = builder.setEmail(email);
        }

        Set<String> groups = attributeSettings.getGroups(attributes);
        if (groups != null) {
            builder = builder.setGroups(groups);
        }

        return builder.build();
    }

    private void handleLogout(Context context) {
        LOG.debug("Found external logout case");
        String logoutAttributes = getLogoutRequestParameter(context.getRequest());
        new LogoutHandler().logout(logoutAttributes);
    }

    private boolean isLogout(HttpServletRequest request) {
        String requestMethod = request.getMethod();
        String logoutAttributes = getLogoutRequestParameter(request);
        return "POST".equals(requestMethod) && !logoutAttributes.isEmpty();
    }

    private String getLogoutRequestParameter(HttpServletRequest request) {
        return StringUtils.defaultIfEmpty(request.getParameter("logoutRequest"), "");
    }

    private boolean isLogin(HttpServletRequest request) {
        String requestMethod = request.getMethod();
        String ticket = StringUtils.defaultIfEmpty(request.getParameter("ticket"), "");

        return "GET".equals(requestMethod) && !ticket.isEmpty();
    }

    @Override
    public String getKey() {
        return "cas";
    }

    @Override
    public String getName() {
        return "CAS";
    }

    @Override
    public Display getDisplay() {
        return Display.builder().build();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean allowsUsersToSignUp() {
        return SonarCasProperties.SONAR_CREATE_USERS.getBooleanProperty();
    }

    private String getSonarServiceUrl() {
        String sonarUrl = SonarCasProperties.SONAR_SERVER_URL.getStringProperty();
        return sonarUrl + "/sessions/init/cas"; // cas corresponds to the value from getKey()
    }

    private String getCasServerUrlPrefix() {
        return SonarCasProperties.CAS_SERVER_URL_PREFIX.getStringProperty();
    }
}
