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

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Sebastian Sdorra
 */
public class HttpUtilTest {

    @Test
    public void testGetBasicAuthentication() throws UnsupportedEncodingException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic d2lraTpwZWRpYQ==");

        Credentials creds = HttpUtil.getBasicAuthentication(request);
        assertNotNull(creds);
        assertEquals(creds.getUsername(), "wiki");
        assertEquals(creds.getPassword(), "pedia");
    }

    @Test
    public void testGetBasicAuthenticationWithoutAuthorization() throws UnsupportedEncodingException {
        HttpServletRequest request = mock(HttpServletRequest.class);

        Credentials creds = HttpUtil.getBasicAuthentication(request);
        assertNull(creds);
    }

    @Test
    public void testGetBasicAuthenticationWithoutBasic() throws UnsupportedEncodingException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Other d2lraTpwZWRpYQ==");

        Credentials creds = HttpUtil.getBasicAuthentication(request);
        assertNull(creds);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ToHttpResponseShouldThrowException() {
        ServletResponse servletResponseMock = mock(ServletResponse.class);
        HttpUtil.toHttp(servletResponseMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ToHttpRequestThrowException() {
        ServletRequest servletRequestMock = mock(ServletRequest.class);
        HttpUtil.toHttp(servletRequestMock);
    }

    @Test
    public void ToHttpRequestShouldReturnTypeCast() {
        ServletRequest servletRequestMock = mock(HttpServletRequest.class);

        HttpServletRequest actual = HttpUtil.toHttp(servletRequestMock);

        assertThat(actual).isSameAs(servletRequestMock);
    }

    @Test
    public void ToHttpResponseShouldReturnTypeCast() {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        HttpServletResponse actual = HttpUtil.toHttp(servletResponse);

        assertThat(actual).isSameAs(servletResponse);
    }
}