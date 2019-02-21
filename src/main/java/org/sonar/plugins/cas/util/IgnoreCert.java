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

import javax.net.ssl.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Disable certification validation check. This is useful in development environemnt
 * with self signed certificates. This will prevent the SSLHandshakeExeption. Do not use
 * this in production environment because it will be a security risk, e.g. man-in-the-middle
 * attacks can not be identified.
 *
 * @author Philip Pohle, TRIOLOGY GmbH
 */
public class IgnoreCert extends HttpServlet {

    private static final long serialVersionUID = -9002436159316961665L;

    @Override
    public void init() throws ServletException {
        //		disableSslVerification();
    }

    public static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(final X509Certificate[] certs,
                                               final String authType) {
                }

                public void checkServerTrusted(final X509Certificate[] certs,
                                               final String authType) {
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            final HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(final String arg0, final SSLSession arg1) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (final KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
