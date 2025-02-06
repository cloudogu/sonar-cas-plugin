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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Resources;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.api.server.http.HttpResponse;
import org.sonar.api.web.FilterChain;
import org.sonar.api.web.HttpFilter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


/**
 * This class injects the CAS logout URL into SonarQube's original logout button in order to call CAS backchannel
 * logout.
 */
public final class CasSonarSignOutInjectorFilter extends HttpFilter {

    private static final Logger LOG = LoggerFactory.getLogger(CasSonarSignOutInjectorFilter.class);
    private static final String CASLOGOUTURL_PLACEHOLDER = "CASLOGOUTURL";
    private final Configuration config;
    ClassLoader resourceClassloader;
    // cachedJsInjection stores logout javascript being injected in HTML resources. This cache only invalidates by
    // Sonar restart. As this injection relies on values from the sonar-cas properties SonarQube must be restarted as
    // well. Usually this is done by restarting the whole container which would then invalidate this cache at the
    // same time.
    private String cachedJsInjection;

    @VisibleForTesting
    static final String LOGOUT_SCRIPT = "casLogoutUrl.js";

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
    public void init() {
        // nothing to init
    }

    public void doFilter(final HttpRequest request, final HttpResponse response,
                         final FilterChain filterChain) {

        try {
            // recursively call the filter chain exactly once per filter, otherwise it may lead to double content per request
            filterChain.doFilter(request, response);
            if (isResourceBlacklisted(request) || !acceptsHtml(request)) {
                LOG.debug("Requested resource does not accept HTML-ish content. Javascript will not be injected");
                return;
            }

            if (StringUtils.isEmpty(this.cachedJsInjection)) {
                readJsInjectionIntoCache();
            }

            String requestedUrl = request.getRequestURL();
            appendJavascriptInjectionToHtmlStream(requestedUrl, response);
        } catch (Exception e) {
            LOG.error("doFilter failed", e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    void readJsInjectionIntoCache() throws IOException {
        URL resource = this.resourceClassloader.getResource(LOGOUT_SCRIPT);
        if (resource == null) {
            throw new FileNotFoundException(String.format("Could not find file %s in classpath of %s. Exiting filtering",
                    LOGOUT_SCRIPT, this.resourceClassloader.getClass()));
        }
        this.cachedJsInjection = Resources.toString(resource, StandardCharsets.UTF_8);
    }


    private void appendJavascriptInjectionToHtmlStream(String requestURL, HttpResponse response) throws IOException {
        LOG.info("Inject CAS logout javascript into {}", requestURL);
        response.getOutputStream().write(("<script type='text/javascript' src='js/casLogoutUrl.js' >").getBytes());
        response.getOutputStream().write("</script>".getBytes());
    }

    private boolean isResourceBlacklisted(HttpRequest request) {
        String url = request.getRequestURL();
        return url.contains("favicon.ico");
    }

    private boolean acceptsHtml(HttpRequest request) {
        String acceptable = request.getHeader("accept");
        LOG.info("Resource {} accepts {}", request.getRequestURL(), acceptable);
        return acceptable != null && acceptable.contains("html");
    }

    public void destroy() {
        // nothing to do
    }
}
