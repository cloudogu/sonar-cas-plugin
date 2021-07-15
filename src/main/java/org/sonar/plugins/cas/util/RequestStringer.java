package org.sonar.plugins.cas.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

public final class RequestStringer {
    public static String string(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String method = request.getMethod();
        Enumeration<String> headers = request.getHeaderNames();
        Cookie[] cookies = request.getCookies();
        Set<Map.Entry<String, String[]>> parameterEntries = request.getParameterMap().entrySet();

        StringBuilder sb = new StringBuilder();
        sb.append("Request data for URL ").append(requestURL)
                .append("Method:\t").append(method).append("\n");

        sb.append("Headers:\n");
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            sb.append(header).append(":\t").append(request.getHeader(header)).append("\n");
        }

        sb.append("Cookies:\n");
        if (cookies == null || cookies.length == 0) {
            sb.append("no cookies found");
        } else {
            for (Cookie cookie : cookies) {
                sb.append(cookie.getName()).append(":\t")
                        .append("Max age:\t").append(cookie.getMaxAge()).append("\n")
                        .append("Path:\t").append(cookie.getPath()).append("\n")
                        .append("Secure:\t").append(cookie.getSecure()).append("\n")
                        .append("Value:\t").append(cookie.getValue()).append("\n");
            }
        }

        sb.append("Parameters:\n");
        for (Map.Entry<String, String[]> entry : parameterEntries) {
            sb.append(entry.getKey()).append(":\t").append(Arrays.asList(entry.getValue())).append("\n");
        }

        return sb.toString();
    }
}
