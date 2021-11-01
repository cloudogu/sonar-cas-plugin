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
package org.sonar.plugins.cas.util;

import com.google.common.base.Strings;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import static org.sonar.plugins.cas.util.Cookies.COOKIE_NAME_URL_AFTER_CAS_REDIRECT;

/**
 * Util class for request methods.
 *
 * @author Sebastian Sdorra, TRIOLOGY GmbH
 */
public final class HttpStreams {
    private static final Logger LOG = LoggerFactory.getLogger(HttpStreams.class);

    private HttpStreams() {
        // util classes should not be instantiable
    }

    public static HttpServletResponse toHttp(ServletResponse response) {
        if (!(response instanceof HttpServletResponse)) {
            throw new IllegalArgumentException("response is not a http servlet response");
        }
        return (HttpServletResponse) response;
    }

    public static HttpServletRequest toHttp(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            throw new IllegalArgumentException("request is not a http servlet request");
        }
        return (HttpServletRequest) request;
    }

    static Credentials getBasicAuthentication(HttpServletRequest request) {
        Credentials credentials = null;
        String header = request.getHeader("Authorization");
        if (Strings.nullToEmpty(header).startsWith("Basic ")) {
            String auth = header.substring("Basic ".length());
            String decodedAuth = new String(Base64.decodeBase64(auth), StandardCharsets.ISO_8859_1);
            if (!Strings.isNullOrEmpty(decodedAuth)) {
                String[] parts = decodedAuth.split(":");
                if (parts.length == 2) {
                    credentials = new Credentials(parts[0], parts[1]);
                }
            }
        }
        return credentials;
    }

    private static String getRequestUrlWithQueryParameters(HttpServletRequest request) {
        String url = request.getRequestURL().toString();

        if (StringUtils.isNotBlank(request.getQueryString())) {
            url += "?" + request.getQueryString();
        }

        return url;
    }

    /**
     * Keep the original URL during redirectToLogin to the CAS server in order to have the URL opened as intended by the
     * user.
     *
     * @param request  the request is used to get the original URL and query parameters
     * @param response the response is used to save a cookie containing the original URL
     * @param config   the SonarQube config object which contains instance specific configuration values from the <pre>sonar.properties</pre> file.
     */
    public static void saveRequestedURLInCookie(HttpServletRequest request, HttpServletResponse response, int maxCookieAge, Configuration config) {
        String configSonarURL = SonarCasProperties.SONAR_SERVER_URL.mustGetString(config);
        String originalURL = HttpStreams.getRequestUrlWithQueryParameters(request);
        String contextPath = request.getContextPath();

        String redirectAfterCasLoginURL = replaceSchema(configSonarURL, originalURL);

        if (StringUtils.isBlank(contextPath)) {
            contextPath = "/";
        }

        LOG.debug("Redirect to URL after CAS login {}", redirectAfterCasLoginURL);
        LOG.debug("Using context path {}", contextPath);

        Cookie cookie = new Cookies.HttpOnlyCookieBuilder()
                .name(COOKIE_NAME_URL_AFTER_CAS_REDIRECT)
                .value(redirectAfterCasLoginURL)
                .maxAgeInSecs(maxCookieAge)
                .contextPath(contextPath)
                .build();

        LOG.debug("set cookie with context path {}", contextPath);
        response.addCookie(cookie);
    }

    static String replaceSchema(String configSonarURL, String originalURL) {
        int schemaIndex = configSonarURL.indexOf("://");
        if (schemaIndex == -1) {
            throw new SonarCasProperties.SonarCasPropertyMisconfigurationException(
                    SonarCasProperties.SONAR_SERVER_URL.propertyKey,
                    "must contain a schema, like http or https.");
        }

        String schema = configSonarURL.substring(0, schemaIndex);
        return originalURL.replaceFirst("https?", schema);
    }
}
