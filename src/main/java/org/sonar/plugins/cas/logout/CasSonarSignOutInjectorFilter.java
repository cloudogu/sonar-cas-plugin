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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.web.ServletFilter;
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.sonar.plugins.cas.util.HttpStreams.toHttp;

/**
 * This class injects the CAS logout URL into SonarQube's original logout button in order to call CAS backchannel
 * logout.
 */
public final class CasSonarSignOutInjectorFilter extends ServletFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CasSonarSignOutInjectorFilter.class);
    private static final String CASLOGOUTURL_PLACEHOLDER = "CASLOGOUTURL";
    private final Configuration config;
    ClassLoader resourceClassloader;
    // cachedJsInjection stores logout javascript being injected in HTML resources. This cache only invalidates by
    // Sonar restart. As this injection relies on values from the sonar-cas properties SonarQube must be restarted as
    // well. Usually this is done by restarting the whole container which would then invalidate this cache at the
    // same time.
    private String cachedJsInjection;

    /**
     * called with injection by SonarQube during server initialization
     */
    public CasSonarSignOutInjectorFilter(Configuration configuration) {
        this.config = configuration;
        this.cachedJsInjection = "";
        this.resourceClassloader = CasSonarSignOutInjectorFilter.class.getClassLoader();
    }

    /**
     * for testing
     */
    CasSonarSignOutInjectorFilter(Configuration configuration, ClassLoader resourceClassloader) {
        this.config = configuration;
        this.cachedJsInjection = "";
        this.resourceClassloader = resourceClassloader;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to init
    }

    @Override
    public UrlPattern doGetPattern() {
        return UrlPattern.create("/*");
    }

    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) {

        try {
            // recursively call the filter chain exactly once per filter, otherwise it may lead to double content per request
            filterChain.doFilter(request, response);

            HttpServletRequest httpRequest = toHttp(request);
            if (isResourceBlacklisted(httpRequest) || !acceptsHtml(httpRequest)) {
                LOG.debug("Requested resource does not accept HTML-ish content. Javascript will not be injected");
                return;
            }

            if (StringUtils.isEmpty(this.cachedJsInjection)) {
                readJsInjectionIntoCache();
            }

            String requestedUrl = httpRequest.getRequestURL().toString();
            appendJavascriptInjectionToHtmlStream(requestedUrl, response);
        } catch (Exception e) {
            LOG.error("doFilter failed", e);
        }
    }

    void readJsInjectionIntoCache() throws IOException {
        String javascriptFile = "casLogoutUrl.js";
        InputStream jsInjectionStream = this.resourceClassloader.getResourceAsStream(javascriptFile);

        if (jsInjectionStream == null) {
            throw new FileNotFoundException(String.format("Could not find file %s in classpath of %s. Exiting filtering",
                    javascriptFile, this.resourceClassloader.getClass()));
        }

        this.cachedJsInjection = readInputStream(jsInjectionStream);
    }

    String readInputStream(InputStream jsInjectionStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        String line;
        try (jsInjectionStream; BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jsInjectionStream, StandardCharsets.UTF_8))) {
            while ((line = bufferedReader.readLine()) != null) {
                // the builder appends the line without newlines. Since this is our javascript this is not a problem.
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private void appendJavascriptInjectionToHtmlStream(String requestURL, ServletResponse response) throws IOException {
        LOG.debug("Inject CAS logout javascript into {}", requestURL);

        response.getOutputStream().println("<script type='text/javascript'>");
        String casLogoutUrl = getCasLogoutUrl();
        String javaScriptToInject = this.cachedJsInjection.replace(CASLOGOUTURL_PLACEHOLDER, casLogoutUrl);

        response.getOutputStream().println(javaScriptToInject);
        response.getOutputStream().println("window.onload = logoutMenuHandler;");
        response.getOutputStream().println("</script>");
    }

    private boolean isResourceBlacklisted(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        return url.contains("favicon.ico");
    }

    private boolean acceptsHtml(HttpServletRequest request) {
        String acceptable = request.getHeader("accept");
        LOG.debug("Resource {} accepts {}", request.getRequestURL(), acceptable);
        return acceptable != null && acceptable.contains("html");
    }

    private String getCasLogoutUrl() {
        return SonarCasProperties.CAS_SERVER_LOGOUT_URL.mustGetString(config);
    }

    public void destroy() {
        // nothing to do
    }
}
