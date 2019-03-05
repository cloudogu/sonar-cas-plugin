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
import org.sonar.plugins.cas.session.CasSessionStore;
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
 * This class handles the initial authentication use case.
 */
@ServerSide
class LoginHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LoginHandler.class);

    private final Configuration configuration;
    private final CasAttributeSettings attributeSettings;
    private final CasSessionStore sessionStore;
    private final TicketValidatorFactory validatorFactory;

    /**
     * called with injection by SonarQube during server initialization
     */
    public LoginHandler(Configuration configuration,
                 CasAttributeSettings attributeSettings,
                 CasSessionStore sessionStore,
                 TicketValidatorFactory ticketValidatorFactory) {
        this.configuration = configuration;
        this.attributeSettings = attributeSettings;
        this.sessionStore = sessionStore;
        validatorFactory = ticketValidatorFactory;
    }

    void handleLogin(BaseIdentityProvider.Context context) throws IOException, TicketValidationException {
        LOG.debug("Starting to handle login. Trying to validate login with CAS");

        String grantingTicket = getServiceTicketParameter(context.getRequest());
        TicketValidator validator = validatorFactory.create();
        Assertion assertion = validator.validate(grantingTicket, getSonarServiceUrl());

        UserIdentity userIdentity = createUserIdentity(assertion);

        LOG.debug("Received assertion. Authenticating with user {}", userIdentity.getName());
        context.authenticate(userIdentity);

        Collection<String> headers = context.getResponse().getHeaders("Set-Cookie");
        SimpleJwt jwt = JwtProcessor.mustGetJwtTokenFromResponseHeaders(headers);

        LOG.debug("Storing granting ticket {} with JWT {}", grantingTicket, jwt.getJwtId());
        sessionStore.store(grantingTicket, jwt);

        String redirectTo = getOriginalUrlFromCookieOrDefault(context.getRequest());
        removeRedirectCookie(context.getResponse());

        LOG.debug("redirecting to {}", redirectTo);
        context.getResponse().sendRedirect(redirectTo);
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

    private String getServiceTicketParameter(HttpServletRequest request) {
        String ticket = request.getParameter("ticket");
        return StringUtils.defaultIfEmpty(ticket, "");
    }

    private String getSonarServiceUrl() {
        String sonarUrl = SonarCasProperties.SONAR_SERVER_URL.mustGetString(configuration);
        return sonarUrl + "/sessions/init/cas"; // cas corresponds to the value from getKey()
    }
}
