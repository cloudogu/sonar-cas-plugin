package org.sonar.plugins.cas.logout;

import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.cas.SonarTestConfiguration;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import static org.sonar.plugins.cas.logout.CasSonarSignOutInjectorFilter.LOGOUT_SCRIPT;

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
        when(mockClassloader.getResource(LOGOUT_SCRIPT)).then(ic -> CasSonarSignOutInjectorFilter.class.getClassLoader().getResource(LOGOUT_SCRIPT));
        CasSonarSignOutInjectorFilter sut = new CasSonarSignOutInjectorFilter(mockConfig, mockClassloader);

        // when: two request are processed there must be only one caching call
        sut.doFilter(mockRequest, mockResponse, mockFilterChain);
        sut.doFilter(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(mockClassloader, times(1)).getResource(LOGOUT_SCRIPT);
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

        CasSonarSignOutInjectorFilter sut = new CasSonarSignOutInjectorFilter(mockConfig, CasSonarSignOutInjectorFilter.class.getClassLoader());

        // when: two request are processed there must be only one caching call
        sut.doFilter(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(mockFilterChain, times(1)).doFilter(any(), any());
    }

    private ServletOutputStream createOutputStream() {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) {
            }
        };
    }
}