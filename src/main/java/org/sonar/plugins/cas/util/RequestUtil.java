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

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Util class for request methods.
 *
 * @author Sebastian Sdorra, TRIOLOGY GmbH
 */
public final class RequestUtil {

    private RequestUtil() {
    }

    public static boolean isBrowser(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return Strings.nullToEmpty(ua).startsWith("Mozilla");
    }

    public static HttpServletRequest toHttp(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            throw new IllegalArgumentException("request is not a http servlet request");
        }
        return (HttpServletRequest) request;
    }

    public static Credentials getBasicAuthentication(HttpServletRequest request) throws UnsupportedEncodingException {
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
}
