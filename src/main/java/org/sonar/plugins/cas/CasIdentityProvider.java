package org.sonar.plugins.cas;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.BaseIdentityProvider;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.plugins.cas.logout.CasSonarSignOutInjectorFilter;
import org.sonar.plugins.cas.logout.LogoutHandler;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;
import org.sonar.plugins.cas.util.CookieUtil;
import org.sonar.plugins.cas.util.JwtProcessor;
import org.sonar.plugins.cas.util.SimpleJwt;
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.sonar.plugins.cas.ForceCasLoginFilter.COOKIE_NAME_URL_AFTER_CAS_REDIRECT;

/**
 * The {@link CasIdentityProvider} is responsible for the browser based cas sso authentication. The authentication
 * workflow for an unauthenticated user is as follows:
 *
 * <ol>
 * <li>the {@link ForceCasLoginFilter} redirects the user to /sessions/new</li>
 * <li>the {@link AuthenticationFilter} redirects the user to the CAS Server</li>
 * <li>the user authenticates to the CAS Server</li>
 * <li>the CAS Server redirects back to /sessions/init/cas</li>
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

    private final CasAttributeSettings attributeSettings;
    private final CasSessionStore casSessionStore;
    private final Configuration config;
    private final TicketValidatorFactory ticketValidatorFactory;

    /**
     * called with injection by SonarQube during server initialization
     */
    public CasIdentityProvider(Configuration configuration,
                               CasAttributeSettings attributeSettings,
                               CasSessionStoreFactory sessionStoreFactory,
                               TicketValidatorFactory ticketValidatorFactory) {
        this.config = configuration;
        this.attributeSettings = attributeSettings;
        this.casSessionStore = sessionStoreFactory.getInstance();
        this.ticketValidatorFactory = ticketValidatorFactory;
    }

    @Override
    public void init(Context context) {
        try {
            HttpServletRequest request = context.getRequest();

            if (isLogin(request)) {
                handleLogin(context);
            } else if (isLogout(request)) {
                handleLogout(context);
            } else {
                LOG.debug("CasIdentityProvider found an unexpected case. Ignoring this request to {}", request.getRequestURL());
            }
        } catch (Exception e) {
            LOG.error("authentication failed", e);
        }
    }

    private void handleLogin(Context context) throws IOException, TicketValidationException {
        LOG.debug("Found internal login case");

        String grantingTicket = getServiceTicketParameter(context.getRequest());
        TicketValidator validator = ticketValidatorFactory.create();
        Assertion assertion = validator.validate(grantingTicket, getSonarServiceUrl());

        UserIdentity userIdentity = createUserIdentity(assertion);
        context.authenticate(userIdentity);

        Collection<String> headers = context.getResponse().getHeaders("Set-Cookie");
        SimpleJwt jwt = JwtProcessor.mustGetJwtTokenFromResponseHeaders(headers);

        LOG.debug("Storing granting ticket {} with JWT {}", grantingTicket, jwt.getJwtId());
        casSessionStore.store(grantingTicket, jwt);

        String redirectTo = getOriginalUrlFromCookieOrDefault(context.getRequest());
        removeRedirectCookie(context.getResponse());

        LOG.debug("redirecting to {}", redirectTo);
        context.getResponse().sendRedirect(redirectTo);
    }

    private String getOriginalUrlFromCookieOrDefault(HttpServletRequest request) {
        Cookie cookie = CookieUtil.findCookieByName(request.getCookies(), COOKIE_NAME_URL_AFTER_CAS_REDIRECT);

        if (cookie != null) {
            String urlFromCookie = cookie.getValue();
            LOG.debug("found redirect cookie {}", urlFromCookie);

            return urlFromCookie;
        }

        String fallbackToRoot = "/";
        String fallback = StringUtils.defaultIfEmpty(request.getContextPath(), fallbackToRoot);
        LOG.debug("No redirect URL in cookie found. Falling back to {}", fallback);

        return fallback;
    }

    private void removeRedirectCookie(HttpServletResponse response) {
        Cookie cookie = CookieUtil.createDeletionCookie(COOKIE_NAME_URL_AFTER_CAS_REDIRECT);

        response.addCookie(cookie);
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
        builder = builder.setGroups(groups);

        return builder.build();
    }

    private void handleLogout(Context context) throws IOException {
        LOG.debug("Found external logout case");
        String logoutAttributes = getLogoutRequestParameter(context.getRequest());
        new LogoutHandler(casSessionStore).logout(logoutAttributes);

        context.getResponse().sendRedirect(getSonarServiceUrl());
    }

    private boolean isLogout(HttpServletRequest request) {
        String requestMethod = request.getMethod();
        String logoutAttributes = getLogoutRequestParameter(request);
        return "POST".equals(requestMethod) && !logoutAttributes.isEmpty();
    }

    private boolean isLogin(HttpServletRequest request) {
        String ticket = getServiceTicketParameter(request);

        String requestMethod = request.getMethod();

        return "GET".equals(requestMethod) && !ticket.isEmpty();
    }

    private String getServiceTicketParameter(HttpServletRequest request) {
        String ticket = request.getParameter("ticket");
        return StringUtils.defaultIfEmpty(ticket, "");
    }

    private String getLogoutRequestParameter(HttpServletRequest request) {
        String logoutRequest = request.getParameter("logoutRequest");
        return StringUtils.defaultIfEmpty(logoutRequest, "");
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
        return SonarCasProperties.SONAR_CREATE_USERS.mustGetBoolean(config);
    }

    private String getSonarServiceUrl() {
        String sonarUrl = SonarCasProperties.SONAR_SERVER_URL.mustGetString(config);
        return sonarUrl + "/sessions/init/cas"; // cas corresponds to the value from getKey()
    }
}
