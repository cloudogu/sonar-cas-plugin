package org.sonar.plugins.cas.util;

import org.sonar.api.server.http.Cookie;
import org.sonar.api.server.http.HttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of the HttpRequest class to enable actually setting attributes
 */
public class MockHttpRequest implements HttpRequest {
    public Map<String, Object> attributes = new HashMap<>();
    public String requestURL = "";

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
    public String getServerName() {
        return "";
    }

    @Override
    public String getRequestURL() {
        return requestURL;
    }

    @Override
    public String getRequestURI() {
        return "";
    }

    @Override
    public String getQueryString() {
        return "";
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getParameter(String s) {
        return "";
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[0];
    }

    @Override
    public String getHeader(String s) {
        return "";
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    @Override
    public String getMethod() {
        return "";
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public String getRemoteAddr() {
        return "";
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    // use itself as delegate
    public HttpRequest getDelegate() {
        return this;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
