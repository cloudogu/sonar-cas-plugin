package org.sonar.plugins.cas.util;

import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestStringerTest {

    @Test
    public void stringShouldReturnAllRequestFields() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://server.com/endpoint?thing=asdf"));
        String[] asdf = {"header1"};
        when(request.getHeaderNames()).thenReturn((Collections.enumeration(Arrays.asList(asdf))));
        when(request.getHeader("header1")).thenReturn("Header value 1");
        Cookie cookie1 = new Cookie("cookie1", "cookie value 1");
        cookie1.setHttpOnly(true);
        cookie1.setSecure(true);
        cookie1.setMaxAge(42);
        Cookie[] cookies = new Cookie[]{cookie1};

        when(request.getCookies()).thenReturn(cookies);
        Map<String, String[]> params = new HashMap<>();
        String[] paramValues1 = {"Value 1", "Another rarely seen value 2"};
        params.put("parameter1", paramValues1);
        when(request.getParameterMap()).thenReturn(params);

        String actual = RequestStringer.string(request);

        assertThat(actual).isEqualTo("Request data for URL https://server.com/endpoint?thing=asdfMethod:\tnull\n" +
                "Headers:\n" +
                "header1:\tHeader value 1\n" +
                "Cookies:\n" +
                "cookie1:\tMax age:\t42\n" +
                "Path:\tnull\n" +
                "Secure:\ttrue\n" +
                "Value:\tcookie value 1\n" +
                "Parameters:\n" +
                "parameter1:\t[Value 1, Another rarely seen value 2]\n");
    }

    @Test
    public void stringShouldReturnOnlyFewRequestFields() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://server.com/endpoint"));

        String actual = RequestStringer.string(request);

        assertThat(actual).isEqualTo("Request data for URL https://server.com/endpointMethod:\tnull\n" +
                "Headers:\n" +
                "no headers found\n" +
                "Cookies:\n" +
                "no cookies found\n" +
                "Parameters:\n");
    }
}
