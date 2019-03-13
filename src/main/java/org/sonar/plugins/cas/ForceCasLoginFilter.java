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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.web.ServletFilter;
import org.sonar.plugins.cas.logout.LogoutHandler;
import org.sonar.plugins.cas.util.HttpStreams;
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
    /**
     * Array of request URLS that should not be redirected to the login page.
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/sessions/", "/api/", "/batch_bootstrap/", "/deploy/", "/batch");

    private final Configuration configuration;
    private LogoutHandler logoutHandler;

    public ForceCasLoginFilter(Configuration configuration, LogoutHandler logoutHandler) {
        this.configuration = configuration;
        this.logoutHandler = logoutHandler;
    }

    public void init(FilterConfig filterConfig) {
        // nothing to do
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = HttpStreams.toHttp(servletRequest);
        HttpServletResponse response = HttpStreams.toHttp(servletResponse);
        String requestedURL = request.getRequestURL().toString();
        int maxRedirectCookieAge = SonarCasProperties.URL_AFTER_CAS_REDIRECT_COOKIE_MAX_AGE_IN_SECS.mustGetInteger(configuration);

        if (isInWhiteList(request.getServletPath()) || isAuthenticated(request)) {
            LOG.debug("Found permitted request to {}", requestedURL);

            if (logoutHandler.isUserLoggedOutAndLogsInAgain(request)) {
                HttpStreams.saveRequestedURLInCookie(request, response, maxRedirectCookieAge);
                logoutHandler.handleInvalidJwtCookie(request, response);
                redirectToLogin(request, response);
            } else {
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

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.debug("Redirecting to login page {}", request.getRequestURL(), SONAR_LOGIN_URL_PATH);

        response.sendRedirect(request.getContextPath() + SONAR_LOGIN_URL_PATH);
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        // https://github.com/SonarSource/sonarqube/blob/9973bacbfa4a945e509bf1b574d7e5aae4ba155a/server/sonar-server/src/main/java/org/sonar/server/authentication/UserSessionInitializer.java#L138
        String login = StringUtils.defaultString((String) request.getAttribute("LOGIN"));
        return !"-".equals(login);
    }

    /**
     * Looks for the given value if it or parts of it are containing in the white list.
     *
     * @param entry Entry to look for in white list.
     * @return true if found, false otherwise.
     */
    private boolean isInWhiteList(final String entry) {
        if (null == entry) {
            return false;
        }

        for (final String item : WHITE_LIST) {
            if (entry.contains(item)) {
                return true;
            }
        }
        return false;
    }

    public void destroy() {
        // nothing to do
    }
}
