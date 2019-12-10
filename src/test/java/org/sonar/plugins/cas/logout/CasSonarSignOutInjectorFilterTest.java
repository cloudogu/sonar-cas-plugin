package org.sonar.plugins.cas.logout;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.cas.SonarTestConfiguration;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.*;

public class CasSonarSignOutInjectorFilterTest {

    @Test
    public void doFilterShouldCacheJavascriptInjection() throws Exception {
        Configuration mockConfig = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerLogoutUrl", "http://sonar.server.com");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        StringBuffer requestURL = new StringBuffer("http://sonar.url.com/sonar/");
        when(mockRequest.getRequestURL())
                .thenReturn(requestURL)
                .thenReturn(requestURL);
        when(mockRequest.getHeader("accept")).thenReturn("text/html");

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getOutputStream()).thenReturn(createOutputStream());
        FilterChain mockFilterChain = mock(FilterChain.class);

        ClassLoader mockClassloader = mock(ClassLoader.class);
        InputStream injectionStream = createJavascriptAsStream();
        when(mockClassloader.getResourceAsStream("casLogoutUrl.js")).thenReturn(injectionStream);
        CasSonarSignOutInjectorFilter sut = new CasSonarSignOutInjectorFilter(mockConfig, mockClassloader);

        // when: two request are processed there must be only one caching call
        sut.doFilter(mockRequest, mockResponse, mockFilterChain);
        sut.doFilter(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(mockClassloader, times(1)).getResourceAsStream("casLogoutUrl.js");
    }

    @Test
    public void doFilterShouldCallFilterChainOnce() throws Exception {
        Configuration mockConfig = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerLogoutUrl", "http://sonar.server.com");
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        StringBuffer requestURL = new StringBuffer("http://sonar.url.com/sonar/");
        when(mockRequest.getRequestURL())
                .thenReturn(requestURL)
                .thenReturn(requestURL);
        when(mockRequest.getHeader("accept")).thenReturn("text/html");

        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getOutputStream()).thenReturn(createOutputStream());
        FilterChain mockFilterChain = mock(FilterChain.class);

        ClassLoader mockClassloader = mock(ClassLoader.class);
        InputStream injectionStream = createJavascriptAsStream();
        when(mockClassloader.getResourceAsStream("casLogoutUrl.js")).thenReturn(injectionStream);
        CasSonarSignOutInjectorFilter sut = new CasSonarSignOutInjectorFilter(mockConfig, mockClassloader);

        // when: two request are processed there must be only one caching call
        sut.doFilter(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(mockFilterChain, times(1)).doFilter(any(), any());
    }

    @Test
    public void readInputStreamShouldReturnTwoLines() throws IOException {
        CasSonarSignOutInjectorFilter sut = new CasSonarSignOutInjectorFilter(null);
        InputStream stream = createJavascriptAsStream();

        String actual = sut.readInputStream(stream);

        Assertions.assertThat(actual).isEqualTo("var line1;var line2;");
    }

    private ServletOutputStream createOutputStream() {
        return new ServletOutputStream() {
            @Override
            public void write(int b) {
            }
        };
    }

    private InputStream createJavascriptAsStream() {
        return new ByteArrayInputStream("var line1;\nvar line2;\n".getBytes());
    }
}