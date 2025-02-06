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

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.api.server.http.HttpResponse;
import org.sonar.plugins.cas.SonarTestConfiguration;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Sebastian Sdorra
 */
public class HttpStreamsTest {

    @Test
    public void testGetBasicAuthentication() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic d2lraTpwZWRpYQ==");

        Credentials creds = HttpStreams.getBasicAuthentication(request);
        assertNotNull(creds);
        assertEquals(creds.getUsername(), "wiki");
        assertEquals(creds.getPassword(), "pedia");
    }

    @Test
    public void testGetBasicAuthenticationWithoutAuthorization() {
        HttpRequest request = mock(HttpRequest.class);

        Credentials creds = HttpStreams.getBasicAuthentication(request);
        assertNull(creds);
    }

    @Test
    public void testGetBasicAuthenticationWithoutBasic() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Other d2lraTpwZWRpYQ==");

        Credentials creds = HttpStreams.getBasicAuthentication(request);
        assertNull(creds);
    }

    @Test
    public void saveRequestedURLInCookieShouldSaveWholeURLInCookie() {
        HttpRequest request = mock(HttpRequest.class);
        String originalURL = "http://sonar.url.com/sonar/somePageWhichIsNotLogin";
        when(request.getRequestURL()).thenReturn(originalURL);
        when(request.getContextPath()).thenReturn("/sonar");
        // getRequestURL does NOT return query params. Kinda important for called sonar URLs
        when(request.getQueryString()).thenReturn("project=Das+&amp;Uuml;ber+Project&file=src/main/com/cloudogu/App.java");
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sonarServerUrl", "http://sonar.url.com/sonar");

        HttpResponse response = mock(HttpResponse.class);

        HttpStreams.saveRequestedURLInCookie(request, response, 300, config);

        verify(request).getRequestURL();
        // Cookie is a crappy class that does not allow equals or hashcode, so we test the number of invocations of
        // getParameterMap. If it was only once, it would not have jumped into urlEncodeQueryParameters()
        verify(request, times(2)).getQueryString();
        verify(response).addCookie(any());
    }

    // @Test()
    public void saveRequestedURLInCookieShouldReplaceSchemaFromSonarCasProperties() {
        // In reverse-proxied systems (f. i. Cloudogu EcoSystem) the SonarQube-local scheme may differ from the global
        // scheme. This is because SSL termination may be in place so that the URL looks like https://fqdn/sonar from a
        // user's perspective, while SonarQube runs as http://localIP:port/sonar locally.

        HttpRequest request = mock(HttpRequest.class);
        String originalURL = "http://sonar.url.com/sonar/somePageWhichIsNotLogin";
        when(request.getRequestURL()).thenReturn(originalURL);
        when(request.getContextPath()).thenReturn("/sonar");
        // getRequestURL does NOT return query params. Kinda important for called sonar URLs
        when(request.getQueryString()).thenReturn("project=Das+&amp;Uuml;ber+Project&file=src/main/com/cloudogu/App.java");
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sonarServerUrl", "https://sonar.url.com/sonar"); // different scheme than in the original URL
        HttpResponse response = mock(HttpResponse.class);

        HttpStreams.saveRequestedURLInCookie(request, response, 300, config);

        verify(request).getRequestURL();
        // Cookie is a crappy class that does not allow equals or hashcode, so we test the number of invocations of
        // getParameterMap. If it was only once, it would not have jumped into urlEncodeQueryParameters()
        verify(request, times(2)).getQueryString();
        //assertThat(response.getCookie().getName()).isEqualTo(Cookies.COOKIE_NAME_URL_AFTER_CAS_REDIRECT);
        //assertThat(response.getCookie().getValue()).isEqualTo("https://sonar.url.com/sonar/somePageWhichIsNotLogin?project=Das+&amp;Uuml;ber+Project&file=src/main/com/cloudogu/App.java");
        //assertThat(response.getCookie().getMaxAge()).isEqualTo(300);
        //assertThat(response.getCookie().getPath()).isEqualTo("/sonar");
        //assertThat(response.getCookie().getSecure()).isTrue();
    }

    @Test()
    public void saveRequestedURLInCookieShouldApplySecureCookieConfigFromSonarCasProperties() {
        // In reverse-proxied systems (f. i. Cloudogu EcoSystem) the SonarQube-local scheme may differ from the global
        // scheme. This is because SSL termination may be in place so that the URL looks like https://fqdn/sonar from a
        // user's perspective, while SonarQube runs as http://localIP:port/sonar locally.

        HttpRequest request = mock(HttpRequest.class);
        String originalURL = "http://sonar.url.com/sonar/somePageWhichIsNotLogin";
        when(request.getRequestURL()).thenReturn(originalURL);
        when(request.getContextPath()).thenReturn("/sonar");
        // getRequestURL does NOT return query params. Kinda important for called sonar URLs
        when(request.getQueryString()).thenReturn("project=Das+&amp;Uuml;ber+Project&file=src/main/com/cloudogu/App.java");
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sonarServerUrl", "http://sonar.url.com/sonar")
                .withAttribute("sonar.cas.userSecureRedirectCookies", "false");
        HttpResponse response = mock(HttpResponse.class);

        HttpStreams.saveRequestedURLInCookie(request, response, 300, config);

        verify(request).getRequestURL();
        verify(request, times(2)).getQueryString();
        // assertThat(response.getCookie().getSecure()).isFalse();
    }

    @Test
    public void saveRequestedURLInCookieEmptyContext() {
        HttpRequest request = mock(HttpRequest.class);
        String originalURL = "http://sonar.url.com/somePageWhichIsNotLogin";
        when(request.getRequestURL()).thenReturn(originalURL);
        when(request.getContextPath()).thenReturn("");
        // getRequestURL does NOT return query params. Kinda important for called sonar URLs
        when(request.getQueryString()).thenReturn("project=Das+&amp;Uuml;ber+Project&file=src/main/com/cloudogu/App.java");
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sonarServerUrl", "https://sonar.url.com/sonar");

        HttpResponse response = mock(HttpResponse.class);

        HttpStreams.saveRequestedURLInCookie(request, response, 300, config);

        verify(request).getRequestURL();
        // Cookie is a crappy class that does not allow equals or hashcode, so we test the number of invocations of
        // getParameterMap. If it was only once, it would not have jumped into urlEncodeQueryParameters()
        verify(request, times(2)).getQueryString();
        verify(response).addCookie(any());
    }

    @Test
    public void replaceSchemeShouldReplaceOnlySchemeFromURL() {
        String configSonarURL = "https://my.server.com:9000/sonar";
        String originalURL = "http://my.server.com:9000/sonar/project?key=value";

        String actualURL = HttpStreams.replaceSchema(configSonarURL, originalURL);

        assertThat(actualURL).isEqualTo("https://my.server.com:9000/sonar/project?key=value");
    }

    @Test(expected = SonarCasProperties.SonarCasPropertyMisconfigurationException.class)
    public void replaceSchemeShouldThrowExceptionOnMissingSchema() {
        String configSonarURL = "my.server.com:9000/sonar";
        String originalURL = "http://my.server.com:9000/sonar/project?key=value";

        HttpStreams.replaceSchema(configSonarURL, originalURL);
    }
}