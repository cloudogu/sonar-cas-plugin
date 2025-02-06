package org.sonar.plugins.cas;

import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.api.server.http.HttpResponse;
import org.sonar.api.web.FilterChain;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AuthenticationFilterTest {

    @Test
    public void doFilterShouldCreateRedirect() throws IOException {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerLoginUrl", "https://cas.server.net/cas/login")
                .withAttribute("sonar.cas.sonarServerUrl", "https://sonar.server.net");
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);

        FilterChain filterChain = mock(FilterChain.class);

        AuthenticationFilter sut = new AuthenticationFilter(config);

        sut.doFilter(request, response, filterChain);

        verify(response).sendRedirect("https://cas.server.net/cas/login?service=https://sonar.server.net/sessions/init/sonarqube");
    }
}