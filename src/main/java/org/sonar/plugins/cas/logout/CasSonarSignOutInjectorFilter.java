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
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.sonar.plugins.cas.util.RequestUtil.toHttp;

/**
 * This class injects the CAS logout URL into SonarQube's original logout button in order to call CAS backchannel
 * logout.
 */
public final class CasSonarSignOutInjectorFilter extends ServletFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CasSonarSignOutInjectorFilter.class);
    private static final String CASLOGOUTURL_PLACEHOLDER = "CASLOGOUTURL";

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to init
    }

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create("/*");
    }

    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(request, response);

        if (!acceptsHtml(request)) {
            LOG.debug("Requested resource does not accept HTML-ish content. Javascript will not be injected");
            filterChain.doFilter(request, response);
            return;
        }

        String javascriptFile = "casLogoutUrl.js";
        InputStream stream = CasSonarSignOutInjectorFilter.class.getClassLoader().getResourceAsStream(javascriptFile);

        if (stream == null) {
            LOG.error("Could not find file {} in classpath. Exiting filtering", javascriptFile);
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
        String javaScriptToInject = builder.toString().replace(CASLOGOUTURL_PLACEHOLDER, casLogoutUrl);
        response.getOutputStream().println(javaScriptToInject);
        response.getOutputStream().println("window.onload = logoutHandler;");
        response.getOutputStream().println("</script>");
    }

    private boolean acceptsHtml(ServletRequest request) {
        HttpServletRequest httpServletRequest = toHttp(request);

        String acceptable = httpServletRequest.getHeader("accept");
        LOG.debug("Resource {} accepts {}", httpServletRequest.getRequestURL(), acceptable);
        return acceptable != null && acceptable.contains("html");
    }

    private String getCasLogoutUrl() {
        return SonarCasProperties.CAS_SERVER_LOGOUT_URL.getStringProperty();
    }

    public void destroy() {
        // nothing to do
    }
}
