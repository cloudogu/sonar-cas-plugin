package org.sonar.plugins.cas.logout;

import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.api.server.http.HttpResponse;
import org.sonar.api.web.FilterChain;
import org.sonar.plugins.cas.SonarTestConfiguration;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import static org.mockito.Mockito.*;

public class CasSonarSignOutInjectorFilterTest {

    @Test
    public void doFilterShouldCallFilterChainOnce() throws Exception {
        HttpRequest mockRequest = mock(HttpRequest.class);
        String requestURL = "http://sonar.url.com/sonar/";
        when(mockRequest.getRequestURL())
                .thenReturn(requestURL)
                .thenReturn(requestURL);
        when(mockRequest.getHeader("accept")).thenReturn("text/html");

        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.getOutputStream()).thenReturn(createOutputStream());
        FilterChain mockFilterChain = mock(FilterChain.class);

        CasSonarSignOutInjectorFilter sut = new CasSonarSignOutInjectorFilter(CasSonarSignOutInjectorFilter.class.getClassLoader());

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