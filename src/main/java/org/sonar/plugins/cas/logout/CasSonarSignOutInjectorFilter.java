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
package org.sonar.plugins.cas.logout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.api.server.http.HttpResponse;
import org.sonar.api.web.FilterChain;
import org.sonar.api.web.HttpFilter;

import java.io.IOException;


/**
 * This class injects the CAS logout URL into SonarQube's original logout button in order to call CAS backchannel
 * logout.
 */
public final class CasSonarSignOutInjectorFilter extends HttpFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CasSonarSignOutInjectorFilter.class);
    ClassLoader resourceClassloader;

    /**
     * called with injection by SonarQube during server initialization
     */
    public CasSonarSignOutInjectorFilter(Configuration configuration) {
        this.resourceClassloader = CasSonarSignOutInjectorFilter.class.getClassLoader();
    }

    /**
     * for testing
     */
    CasSonarSignOutInjectorFilter(Configuration configuration, ClassLoader resourceClassloader) {
        this.resourceClassloader = resourceClassloader;
    }

    @Override
    public void init() {
        // nothing to init
    }

    public void doFilter(final HttpRequest request, final HttpResponse response,
                         final FilterChain filterChain) {

        try {
            // recursively call the filter chain exactly once per filter, otherwise it may lead to double content per request
            filterChain.doFilter(request, response);
            // redirect logout requests directly
            if (request.getRequestURL().contains("sessions/logout")) {
                response.sendRedirect("/cas/logout");
            }

            if (isResourceBlacklisted(request) || !acceptsHtml(request)) {
                LOG.debug("Requested resource does not accept HTML-ish content. Javascript will not be injected");
                return;
            }

            String requestedUrl = request.getRequestURL();
            appendJavascriptInjectionToHtmlStream(requestedUrl, response);
        } catch (Exception e) {
            LOG.error("doFilter failed", e);
        }
    }


    private void appendJavascriptInjectionToHtmlStream(String requestURL, HttpResponse response) throws IOException {
        LOG.debug("Inject CAS logout javascript into {}", requestURL);
        response.getOutputStream().write(("<script type='text/javascript' src='js/casLogoutUrl.js' >").getBytes());
        response.getOutputStream().write("</script>".getBytes());
    }

    private boolean isResourceBlacklisted(HttpRequest request) {
        String url = request.getRequestURL();
        return url.contains("favicon.ico");
    }

    private boolean acceptsHtml(HttpRequest request) {
        String acceptable = request.getHeader("accept");
        LOG.debug("Resource {} accepts {}", request.getRequestURL(), acceptable);
        return acceptable != null && acceptable.contains("html");
    }

    public void destroy() {
        // nothing to do
    }
}
