package org.sonar.plugins.cas.util;

import org.sonar.api.server.http.Cookie;
import org.sonar.api.server.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

/**
 * Mock implementation of the HttpRequest class to enable actually setting attributes
 */
public class MockHttpResponse implements HttpResponse {
    private Cookie aSingleCookie;

    Cookie getCookie() {
        return aSingleCookie;
    }

    public void addCookie(Cookie cookie) {
        aSingleCookie = cookie;
    }

    @Override
    public void addHeader(String s, String s1) {

    }

    @Override
    public String getHeader(String s) {
        return "";
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return List.of();
    }

    @Override
    public void setStatus(int i) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public void setContentType(String s) {

    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return null;
    }

    @Override
    public void setHeader(String s, String s1) {

    }

    @Override
    public void sendRedirect(String s) throws IOException {

    }


    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) {

    }
}
