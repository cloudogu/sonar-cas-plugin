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
package org.sonar.plugins.cas;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.cas.util.CasAuthenticationException;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Client for the CAS REST API.
 *
 * @author Sebastian Sdorra, TRIOLOGY GmbH
 * @see <a href="https://apereo.github.io/cas/6.0.x/protocol/REST-Protocol.html">CAS RESTful API</a>
 */
public class CasRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(CasRestClient.class);

    private final String casServerUrl;
    private final String serviceUrl;

    public CasRestClient(String casServerUrl, String serviceUrl) {
        this.casServerUrl = casServerUrl;
        this.serviceUrl = serviceUrl;
    }

    /**
     * Creates a service ticket for the given username and password.
     *
     * @param username username the username provided by the basic authentication
     * @param password password the password provided by the basic authentication
     * @return the service ticket identifier
     * @throws CasAuthenticationException if any service call or connection fails this exception is thrown
     */
    public String createServiceTicket(final String username, final String password) throws CasAuthenticationException {
        String tgt = createGrantingTicket(casServerUrl, username, password);

        LOG.debug("TGT is: {}", tgt);

        String st = createServiceTicket(tgt);

        LOG.debug("ST is: {}", st);

        return st;
    }

    private void appendServiceUrl(HttpURLConnection connection) throws IOException, CasAuthenticationException {
        String encodedServiceURL = "service=".concat(encode(serviceUrl));

        LOG.debug("Service url is: {}", encodedServiceURL);

        try (BufferedWriter writer = createWriter(connection)) {
            writer.write(encodedServiceURL);
            writer.flush();
        }
    }

    private void safelyClose(HttpURLConnection c) {
        if (c != null) {
            c.disconnect();
        }
    }

    String createGrantingTicket(String casServerUrl, String username, String password)
            throws CasAuthenticationException {
        HttpURLConnection connection = null;

        try {
            connection = open(casServerUrl + "/v1/tickets");

            String encodedUsername = encode(username);
            String encodedPassword = encode(password);
            appendCredentials(connection, encodedUsername, encodedPassword);

            int rc = connection.getResponseCode();

            if (rc != HttpServletResponse.SC_CREATED) {
                throw new CasAuthenticationException(
                        "could not create granting ticket, web service returned " + rc);
            }

            String location = connection.getHeaderField("Location");

            if (Strings.isNullOrEmpty(location)) {
                throw new CasAuthenticationException(
                        "could not create granting ticket, web service returned no location header");
            }

            return extractTgtFromLocation(location);
        } catch (Exception e) {
            throw new CasAuthenticationException("Could not create Granting Ticket.", e);
        } finally {
            safelyClose(connection);
        }
    }

    void appendCredentials(HttpURLConnection connection, String encodedUsername, String encodedPassword)
            throws IOException {
        String encodedCredentials = "username=".concat(encodedUsername).concat("&password=").concat(encodedPassword);

        try (BufferedWriter bwr = createWriter(connection)) {
            bwr.write(encodedCredentials);
            bwr.flush();
        }
    }

    private BufferedReader createReader(HttpURLConnection connection) throws IOException {
        InputStream stream = connection.getInputStream();
        InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
        return new BufferedReader(in);
    }

    String createServiceTicket(String tgt) throws CasAuthenticationException {
        String st;
        HttpURLConnection connection = null;

        try {
            connection = open(createServiceTicketUrl(tgt));
            appendServiceUrl(connection);

            int rc = connection.getResponseCode();

            if (rc != HttpServletResponse.SC_OK) {
                throw new CasAuthenticationException("could not create service ticket, web service returned " + rc);
            }

            String content;
            StringBuilder contentBuilder = new StringBuilder();
            char[] readBuffer = new char[256];

            try (BufferedReader reader = createReader(connection)) {
                while (reader.read(readBuffer) != -1) {
                    contentBuilder.append(readBuffer);
                }
                content = contentBuilder.toString().trim();
            }

            if (Strings.isNullOrEmpty(content)) {
                throw new CasAuthenticationException("could not create service ticket, body is empty");
            }

            st = content;
        } catch (Exception e) {
            throw new CasAuthenticationException("Could not create Service Ticket.", e);
        } finally {
            safelyClose(connection);
        }

        return st;
    }

    private String createServiceTicketUrl(String tgt) {
        return casServerUrl + "/v1/tickets/" + tgt;
    }

    private BufferedWriter createWriter(HttpURLConnection connection) throws IOException {
        OutputStream stream = connection.getOutputStream();
        OutputStreamWriter out = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        return new BufferedWriter(out);
    }

    private String encode(String value) throws CasAuthenticationException {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new CasAuthenticationException("failure during encoding", ex);
        }
    }

    private String extractTgtFromLocation(String location) throws CasAuthenticationException {
        int index = location.lastIndexOf('/');

        if (index < 0) {
            throw new CasAuthenticationException(
                    "could not create granting ticket, web service returned invalid location header");
        }

        return location.substring(index + 1);
    }

    HttpURLConnection open(final String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        return connection;
    }
}
