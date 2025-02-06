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
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.api.server.http.Cookie;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.api.server.http.HttpResponse;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;
import org.sonar.plugins.cas.util.Cookies;
import org.sonar.plugins.cas.util.JwtProcessor;
import org.sonar.plugins.cas.util.SimpleJwt;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.sonar.plugins.cas.util.Cookies.COOKIE_NAME_URL_AFTER_CAS_REDIRECT;

/**
 * This class handles the initial authentication use case.
 */
@ServerSide
public class LoginHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LoginHandler.class);
    private static final String GROUP_REPLICATION_CAS = "CAS";

    private final Configuration configuration;
    private final CasAttributeSettings attributeSettings;
    private final CasSessionStore sessionStore;
    private final TicketValidatorFactory validatorFactory;

    /**
     * called with injection by SonarQube during server initialization
     */
    public LoginHandler(Configuration configuration,
                        CasAttributeSettings attributeSettings,
                        CasSessionStoreFactory sessionStoreFactory,
                        TicketValidatorFactory ticketValidatorFactory) {
        this.configuration = configuration;
        this.attributeSettings = attributeSettings;
        this.sessionStore = sessionStoreFactory.getInstance();
        validatorFactory = ticketValidatorFactory;
    }

    void handleLogin(BaseIdentityProvider.Context context) throws IOException, TicketValidationException {
        LOG.debug("Starting to handle login. Trying to validate login with CAS");

        String serviceTicket = getTicketParameter(context.getHttpRequest());
        TicketValidator validator = validatorFactory.create();
        Assertion assertion = validator.validate(serviceTicket, getSonarServiceUrl());

        UserIdentity userIdentity = createUserIdentity(assertion);

        LOG.debug("Received assertion. Authenticating with user {}", userIdentity.getName());
        context.authenticate(userIdentity);

        Collection<String> headers = context.getHttpResponse().getHeaders("Set-Cookie");
        SimpleJwt jwt = JwtProcessor.mustGetJwtTokenFromResponseHeaders(headers);

        LOG.debug("Storing service ticket {} with JWT {}", serviceTicket, jwt.getJwtId());
        sessionStore.store(serviceTicket, jwt);

        String redirectTo = getOriginalUrlFromCookieOrDefault(context.getHttpRequest());
        removeRedirectCookie(context.getHttpResponse(), context.getHttpRequest().getContextPath());

        LOG.debug("redirecting to {}", redirectTo);
        context.getHttpResponse().sendRedirect(redirectTo);
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
        if (GROUP_REPLICATION_CAS.equals(getGroupReplicationMode())) {
            // currently SonarQube only sets groups which already exists in the local group database.
            // Thus, new CAS groups will never be added unless manually added in SonarQube.
            builder = builder.setGroups(groups);
        }

        return builder.build();
    }

    private String getOriginalUrlFromCookieOrDefault(HttpRequest request) {
        Cookie cookie = Cookies.findCookieByName(request.getCookies(), COOKIE_NAME_URL_AFTER_CAS_REDIRECT);

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

    private void removeRedirectCookie(HttpResponse response, String contextPath) {
        if (StringUtils.isBlank(contextPath)) {
            contextPath = "/";
        }
        boolean useSecureCookies = SonarCasProperties.USE_SECURE_REDIRECT_COOKIES.getBoolean(configuration, true);
        Cookie cookie = Cookies.createDeletionCookie(COOKIE_NAME_URL_AFTER_CAS_REDIRECT, contextPath, useSecureCookies);

        response.addCookie(cookie);
    }

    /**
     * getTicketParameter searches the given request for CAS service tickets or proxy tickets. The CAS specification
     * names the parameter "ticket" which is used in both scenarios, web authentication and proxy authentication.
     *
     * @param request the request to be authenticated
     * @return the value of the parameter "ticket" if set, otherwise the empty string
     */
    public static String getTicketParameter(HttpRequest request) {
        String ticket = request.getParameter("ticket");
        return StringUtils.defaultIfEmpty(ticket, "");
    }

    private String getSonarServiceUrl() {
        String sonarUrl = SonarCasProperties.SONAR_SERVER_URL.mustGetString(configuration);
        // SonarQube recognizes the Identity Provider by the identifier in the URL. `sonarqube` corresponds to the value from getKey()
        return sonarUrl + "/sessions/init/sonarqube";
    }

    private String getGroupReplicationMode() {
        return SonarCasProperties.GROUP_REPLICATE.getString(configuration, GROUP_REPLICATION_CAS);
    }
}
