package org.sonar.plugins.cas;

import org.junit.Test;
import org.sonar.api.config.Configuration;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AuthenticationFilterTest {

    //@Test
    public void doFilterShouldCreateRedirect() throws IOException {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerLoginUrl", "https://cas.server.net/cas/login")
                .withAttribute("sonar.cas.sonarServerUrl", "https://sonar.server.net");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        FilterChain filterChain = mock(FilterChain.class);

        AuthenticationFilter sut = new AuthenticationFilter(config);

        // sut.doFilter(request, response, filterChain);

        verify(response).sendRedirect("https://cas.server.net/cas/login?service=https://sonar.server.net/sessions/init/sonarqube");
    }
}