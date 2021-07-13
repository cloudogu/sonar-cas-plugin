/*
 * Sonar CAS Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cas;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.ProxyList;
import org.jasig.cas.client.validation.TicketValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.api.web.ServletFilter;
import org.sonar.plugins.cas.logout.LogoutHandler;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;
import org.sonar.plugins.cas.util.HttpStreams;
import org.sonar.plugins.cas.util.JwtProcessor;
import org.sonar.plugins.cas.util.SimpleJwt;
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.sonar.plugins.cas.AuthenticationFilter.SONAR_LOGIN_URL_PATH;

/**
 * This filter checks if the current user is authenticated. If not, the client is redirected to the /session/new
 * address to create a new user session.
 * <p>
 * We cannot use the {@code sonar.forceAuthentication} feature of sonar, because it uses a client side
 * {@code history.rewrite} to change the url and this does not trigger the {@link AuthenticationFilter}.
 *
 * @author Jan Boerner, TRIOLOGY GmbH
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
public class ForceCasLoginFilter extends ServletFilter {
    private static final Logger LOG = LoggerFactory.getLogger(ForceCasLoginFilter.class);
    private static final int DEFAULT_CAS_REDIRECT_COOKIE_AGE = (int) TimeUnit.MINUTES.toSeconds(5);
    private static final String GROUP_REPLICATION_CAS = "CAS";
    /**
     * Array of request URLS that should not be redirected to the login page.
     */
    private static final List<String> ALLOW_LIST = Arrays.asList(
            "/js/", "/images/", "/favicon.ico", "/sessions/", "/api/", "/batch_bootstrap/", "/deploy/", "/batch");
    private static final String PROXY_TICKET_URL_SUFFIX = "/sessions/cas/proxy";

    private final Configuration configuration;
    private final LogoutHandler logoutHandler;
    private final TicketValidatorFactory validatorFactory;
    private final CasAttributeSettings attributeSettings;
    private final CasSessionStore sessionStore;

    /**
     * called with injection by SonarQube during server initialization
     */
    public ForceCasLoginFilter(Configuration configuration, LogoutHandler logoutHandler, CasAttributeSettings attributeSettings,
                               CasSessionStoreFactory sessionStoreFactory, TicketValidatorFactory ticketValidatorFactory) {
        this.configuration = configuration;
        this.logoutHandler = logoutHandler;
        this.attributeSettings = attributeSettings;
        this.sessionStore = sessionStoreFactory.getInstance();
        this.validatorFactory = ticketValidatorFactory;

    }

    public void init(FilterConfig filterConfig) {
        // nothing to do
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = HttpStreams.toHttp(servletRequest);
        HttpServletResponse response = HttpStreams.toHttp(servletResponse);
        String requestedURL = request.getRequestURL().toString();
        int maxRedirectCookieAge = getMaxCookieAge(configuration);
        LOG.debug("ForceCasLoginFilter.doFilter(): {} ", requestedURL);

        if (isProxyLogin(request.getServletPath(), request.getMethod())) {
            LOG.debug("Found proxy ticket login with method {}", request.getMethod());
            try {
                handleProxyLogin(request, response);
            } catch (TicketValidationException e) {
                throw new RuntimeException("Could not validate proxy ticket login for URL " + requestedURL, e);
            }
        } else if (isInAllowList(request.getServletPath()) || isAuthenticated(request)) {
            LOG.debug("Found permitted request to {}", requestedURL);

            if (logoutHandler.isUserLoggedOutAndLogsInAgain(request)) {
                LOG.debug("Redirecting logged-out user to log-in page");
                HttpStreams.saveRequestedURLInCookie(request, response, maxRedirectCookieAge);
                logoutHandler.handleInvalidJwtCookie(request, response);
                redirectToLogin(request, response);
            } else {
                LOG.debug("Continue request processing...");
                chain.doFilter(request, servletResponse);
            }
        } else {
            LOG.debug("Found unauthenticated request or request not in whitelist: {}. Redirecting to login page",
                    requestedURL);
            // keep the original URL during redirectToLogin to the CAS server in order to have the URL opened as intended by the user
            HttpStreams.saveRequestedURLInCookie(request, response, maxRedirectCookieAge);
            redirectToLogin(request, response);
        }
    }

    int getMaxCookieAge(Configuration configuration) {
        return SonarCasProperties.URL_AFTER_CAS_REDIRECT_COOKIE_MAX_AGE_IN_SECS.getInteger(configuration, DEFAULT_CAS_REDIRECT_COOKIE_AGE);
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.debug("Redirecting to login page {} -> {}", request.getRequestURL(), SONAR_LOGIN_URL_PATH);

        response.sendRedirect(request.getContextPath() + SONAR_LOGIN_URL_PATH);
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        // https://github.com/SonarSource/sonarqube/blob/9973bacbfa4a945e509bf1b574d7e5aae4ba155a/server/sonar-server/src/main/java/org/sonar/server/authentication/UserSessionInitializer.java#L138
        String login = StringUtils.defaultString((String) request.getAttribute("LOGIN"));
        LOG.debug("login value: {}", login);
        return !"-".equals(login) && !"".equals(login);
    }

    /**
     * Looks for the given value if it or parts of it are containing in the white list.
     *
     * @param servletPath Entry to look for in white list.
     * @return true if found, false otherwise.
     */
    private boolean isInAllowList(final String servletPath) {
        if (null == servletPath) {
            return false;
        }

        for (final String item : ALLOW_LIST) {
            if (servletPath.contains(item)) {
                return true;
            }
        }
        return false;
    }

    boolean isProxyLogin(final String servletPath, String httpMethod) {
        if (null == servletPath) {
            return false;
        }

        if (!servletPath.contains(PROXY_TICKET_URL_SUFFIX)) {
            return false;
        }

        if (!"GET".equals(httpMethod)) {
            String msg = String.format("Received unexpected method for URL %s: %s", servletPath, httpMethod);
            throw new IllegalStateException(msg);
        }

        return true;
    }

    void handleProxyLogin(HttpServletRequest request, HttpServletResponse response) throws TicketValidationException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> kv : request.getParameterMap().entrySet()) {
            sb.append(kv.getKey())
                    .append("->");
            for (String val : kv.getValue()) {
                sb.append(val);
            }
            sb.append("\\n");
        }
        LOG.debug("Starting to handle proxy ticket authentication. attrs: {}", sb.toString());

        String proxyTicket = LoginHandler.getTicketParameter(request);
        LOG.debug("Trying to validate proxy ticket {} with CAS", proxyTicket);
        Cas20ProxyTicketValidator validator = ((Cas20ProxyTicketValidator) validatorFactory.createForProxy());
        validator.setAcceptAnyProxy(false);

        List<String[]> proxyChains = Collections.singletonList(new String[]{"^https?://192\\.168\\.56\\.2/.*$"});
        ProxyList proxyList = new ProxyList(proxyChains);
        validator.setAllowedProxyChains(proxyList);

        Assertion assertion = validator.validate(proxyTicket, getSonarServiceUrl());
        UserIdentity userIdentity = createUserIdentity(assertion);
        LOG.debug("Received assertion. Authenticating with user {}, login {}", userIdentity.getName(), userIdentity.getProviderLogin());

        if (assertion.isValid()) {
            LOG.debug("Proxy ticket is not valid for user {}", userIdentity.getName());
            throw new RuntimeException("Forbidden: Proxy ticket was invalid");
        }


        Collection<String> headers = response.getHeaders("Set-Cookie");
        SimpleJwt jwt = JwtProcessor.mustGetJwtTokenFromResponseHeaders(headers);

        LOG.debug("Storing proxy ticket {} with JWT {}", proxyTicket, jwt.getJwtId());
        sessionStore.store(proxyTicket, jwt);

        String jsonResponse = String.format("{ 'username': '%s', 'token': '%s'}", userIdentity.getProviderLogin(), jwt);
        try {
            response.getWriter().println(jsonResponse);
        } catch (IOException e) {
            throw new RuntimeException("Error while writing proxy response for user " + userIdentity.getProviderLogin(), e);
        }
    }

    private String getSonarServiceUrl() {
        String sonarUrl = SonarCasProperties.SONAR_SERVER_URL.mustGetString(configuration);
        // SonarQube recognizes the Identity Provider by the identifier in the URL. `sonarqube` corresponds to the value from getKey()
        return sonarUrl + "/sessions/cas/proxy";
    }

    private UserIdentity createUserIdentity(Assertion assertion) {
        AttributePrincipal principal = assertion.getPrincipal();
        Map<String, Object> attributes = principal.getAttributes();

        LOG.debug("Building User identity: Found principal: {}", principal.getName());
        UserIdentity.Builder builder = UserIdentity.builder()
                .setLogin(principal.getName())
                .setProviderLogin(principal.getName());

        LOG.debug("CAS Attributes: {}", attributes);
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            LOG.debug("CAS Attributes: {} => {}", entry.getKey(), entry.getValue());
        }
        String displayName = attributeSettings.getDisplayName(attributes);
        LOG.debug("Building User identity: Display name: {}", displayName);
        if (!Strings.isNullOrEmpty(displayName)) {
            builder = builder.setName(displayName);
        }

        String email = attributeSettings.getEmail(attributes);
        LOG.debug("Building User identity: Email: {}", email);
        if (!Strings.isNullOrEmpty(email)) {
            builder = builder.setEmail(email);
        }

        Set<String> groups = attributeSettings.getGroups(attributes);
        LOG.debug("Building User identity: Groups: {}", groups);
        if (GROUP_REPLICATION_CAS.equals(getGroupReplicationMode())) {
            // currently SonarQube only sets groups which already exists in the local group database.
            // Thus, new CAS groups will never be added unless manually added in SonarQube.
            builder = builder.setGroups(groups);
        }

        return builder.build();
    }

    private String getGroupReplicationMode() {
        return SonarCasProperties.GROUP_REPLICATE.getString(configuration, GROUP_REPLICATION_CAS);
    }

    public void destroy() {
        // nothing to do
    }
}
