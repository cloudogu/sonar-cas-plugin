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
import org.sonar.api.web.ServletFilter;

import javax.servlet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This class injects the CAS logout URL into SonarQube's original logout button in order to call CAS backchannel
 * logout.
 */
public final class CasSonarSignOutInjectorFilter extends ServletFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CasSonarSignOutInjectorFilter.class);
    private static final String CASLOGOUTURL_PLACEHOLDER = "CASLOGOUTURL";

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create("/");
    }

    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(request, response);

        String javascriptToInject = "casLogoutUrl.js";
        InputStream stream = CasSonarSignOutInjectorFilter.class.getClassLoader().getResourceAsStream(javascriptToInject);

        if(stream == null) {
            LOG.error("Could not find file {} in classpath. Exiting filtering", javascriptToInject);
            filterChain.doFilter(request, response);
            return;
        }

        LOG.debug("Inject CAS logout javascript");
        StringBuilder builder = new StringBuilder();
        String line;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        } finally {
            stream.close();
        }

        response.getOutputStream().println("<script type='text/javascript'>");
        String casLogoutUrl = getCasLogoutUrl();
        String s = builder.toString().replace(CASLOGOUTURL_PLACEHOLDER, casLogoutUrl);
        response.getOutputStream().println(s);
        response.getOutputStream().println("window.onload = logoutHandler;");
        response.getOutputStream().println("</script>");
    }

    private String getCasLogoutUrl() {
        // TODO replace from properties
        return "https://cas.hitchhiker.com:8443/cas/logout";
    }

    public void destroy() {
        // nothing to do
    }
}
