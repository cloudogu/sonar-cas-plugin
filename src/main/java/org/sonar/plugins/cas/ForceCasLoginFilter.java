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
import org.sonar.plugins.cas.util.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

    private final RestAuthenticator restAuthenticator;

    public ForceCasLoginFilter(Configuration configuration) {
        this.restAuthenticator = new RestAuthenticator(configuration);
    }

    public void init(final FilterConfig filterConfig) {
        // nothing to do
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest request = RequestUtil.toHttp(servletRequest);
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        // authenticate non browser clients
        if (!RequestUtil.isBrowser(request)) {
            Credentials credentials = RequestUtil.getBasicAuthentication(request);
            if (credentials != null) {
                LOG.debug("Found non-browser authentication request");
                restAuthenticator.authenticate(credentials, request);
            }
        }

        if (isInWhiteList(request.getServletPath()) || isAuthenticated(request)) {
            LOG.debug("Found permitted request to {}", request.getRequestURL());
            new LogoutHandler().invalidateLoginCookiesIfNecessary(request, response);

            chain.doFilter(request, servletResponse);
        } else {
            LOG.debug("Found unauthenticated request or request not in whitelist: {}. Redirecting to /sessions/new", request.getRequestURL());
            redirect(request, response, "/sessions/new");
        }
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response, String uri) throws IOException {
        response.sendRedirect(request.getContextPath() + uri);
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        // https://github.com/SonarSource/sonarqube/blob/9973bacbfa4a945e509bf1b574d7e5aae4ba155a/server/sonar-server/src/main/java/org/sonar/server/authentication/UserSessionInitializer.java#L138
        String login = StringUtils.defaultString((String) request.getAttribute("LOGIN"));
        return !"-".equals(login);
    }

    public void destroy() {
        // nothing to do
    }

    /**
     * Looks for the given value if it or parts of it are containing in the white list.
     *
     * @param entry Entry to look for in white list.
     * @return true if found, false otherwise.
     */
    private boolean isInWhiteList(final String entry) {
        if (null != entry) {
            for (final String item : WHITE_LIST) {
                if (entry.contains(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
