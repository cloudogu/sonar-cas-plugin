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

import org.junit.Test;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic d2lraTpwZWRpYQ==");

        Credentials creds = HttpStreams.getBasicAuthentication(request);
        assertNotNull(creds);
        assertEquals(creds.getUsername(), "wiki");
        assertEquals(creds.getPassword(), "pedia");
    }

    @Test
    public void testGetBasicAuthenticationWithoutAuthorization() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        Credentials creds = HttpStreams.getBasicAuthentication(request);
        assertNull(creds);
    }

    @Test
    public void testGetBasicAuthenticationWithoutBasic() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Other d2lraTpwZWRpYQ==");

        Credentials creds = HttpStreams.getBasicAuthentication(request);
        assertNull(creds);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ToHttpResponseShouldThrowException() {
        ServletResponse servletResponseMock = mock(ServletResponse.class);
        HttpStreams.toHttp(servletResponseMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ToHttpRequestThrowException() {
        ServletRequest servletRequestMock = mock(ServletRequest.class);
        HttpStreams.toHttp(servletRequestMock);
    }

    @Test
    public void ToHttpRequestShouldReturnTypeCast() {
        ServletRequest servletRequestMock = mock(HttpServletRequest.class);

        HttpServletRequest actual = HttpStreams.toHttp(servletRequestMock);

        assertThat(actual).isSameAs(servletRequestMock);
    }

    @Test
    public void ToHttpResponseShouldReturnTypeCast() {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        HttpServletResponse actual = HttpStreams.toHttp(servletResponse);

        assertThat(actual).isSameAs(servletResponse);
    }

    @Test
    public void saveRequestedURLInCookieShouldSaveWholeURLInCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String originalURL = "http://sonar.url.com/somePageWhichIsNotLogin";
        when(request.getRequestURL()).thenReturn(new StringBuffer(originalURL));
        when(request.getContextPath()).thenReturn("/sonar");
        // getRequestURL does NOT return query params. Kinda important for called sonar URLs
        when(request.getQueryString()).thenReturn("project=Das+&amp;Uuml;ber+Project&file=src/main/com/cloudogu/App.java");

        HttpServletResponse response = mock(HttpServletResponse.class);

        HttpStreams.saveRequestedURLInCookie(request, response, 300);

        verify(request).getRequestURL();
        // Cookie is a crappy class that does not allow equals or hashcode, so we test the number of invocations of
        // getParameterMap. If it was only once, it would not have jumped into urlEncodeQueryParameters()
        verify(request, times(2)).getQueryString();
        verify(response).addCookie(any());
    }
}