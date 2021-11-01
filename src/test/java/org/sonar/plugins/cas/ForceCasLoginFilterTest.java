package org.sonar.plugins.cas;

import org.junit.Test;
import org.mockito.verification.VerificationMode;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.cas.logout.LogoutHandler;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;
import org.sonar.plugins.cas.util.Cookies;
import org.sonar.plugins.cas.util.SimpleJwt;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.sonar.plugins.cas.AuthTestData.getJwtToken;

public class ForceCasLoginFilterTest {
    private static final long EXPIRATION_AS_EPOCH_SECONDS = 1550331060L;
    private final static SimpleJwt JWT_TOKEN = SimpleJwt.fromIdAndExpiration("AWjne4xYY4T-z3CxdIRY", EXPIRATION_AS_EPOCH_SECONDS);

    @Test
    public void doFilterShouldHandleGetResource_alreadyLoggedIn() throws IOException, ServletException {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sessionStorePath", "/tmp")
                .withAttribute("sonar.cas.urlAfterCasRedirectCookieMaxAgeSeconds", "300")
                .withAttribute("sonar.cas.sonarServerUrl", "http://sonar.com/sonar");
        CasSessionStoreFactory sessionStoreFactory = new CasSessionStoreFactory(config);
        LogoutHandler logoutHandler = new LogoutHandler(config, sessionStoreFactory);
        ForceCasLoginFilter sut = new ForceCasLoginFilter(config, logoutHandler);

        CasSessionStore store = mock(CasSessionStore.class);
        when(store.isJwtStored(JWT_TOKEN)).thenReturn(true);
        SimpleJwt invalidJwtToken = JWT_TOKEN.cloneAsInvalidated();
        when(store.fetchStoredJwt(JWT_TOKEN)).thenReturn(invalidJwtToken);

        HttpServletRequest request = mock(HttpServletRequest.class);
        String jwtCookieDoughContent = getJwtToken();
        Cookie httpOnlyCookie = createJwtCookie(jwtCookieDoughContent);
        Cookie[] cookies = {httpOnlyCookie};
        when(request.getCookies()).thenReturn(cookies);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://sonar.url.com/sonar/somePageWhichIsNotLogin"));
        when(request.getContextPath()).thenReturn("/sonar");

        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        sut.doFilter(request, response, filterChain);

        VerificationMode singleInteraction = times(1);
        verifyZeroInteractions(filterChain);
        verify(response, singleInteraction).sendRedirect(any());
        verify(response, singleInteraction).addCookie(any());
        verify(store, never()).invalidateJwt(anyString());
    }

    private Cookie createJwtCookie(String jwtCookieDoughContent) {
        return new Cookies.HttpOnlyCookieBuilder()
                .name(Cookies.JWT_SESSION_COOKIE)
                .value(jwtCookieDoughContent)
                .contextPath("/")
                .maxAgeInSecs(100)
                .build();
    }

    @Test
    public void doFilterShouldHandle_notLoggedIn() throws IOException, ServletException {
        // given
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sessionStorePath", "/tmp")
                .withAttribute("sonar.cas.urlAfterCasRedirectCookieMaxAgeSeconds", "100")
                .withAttribute("sonar.cas.sonarServerUrl", "http://sonar.com/sonar");
        CasSessionStoreFactory sessionStoreFactory = new CasSessionStoreFactory(config);
        LogoutHandler logoutHandler = new LogoutHandler(config, sessionStoreFactory);
        ForceCasLoginFilter sut = new ForceCasLoginFilter(config, logoutHandler);

        CasSessionStore store = mock(CasSessionStore.class);
        when(store.isJwtStored(JWT_TOKEN)).thenReturn(true);
        SimpleJwt invalidJwtToken = JWT_TOKEN.cloneAsInvalidated();
        when(store.fetchStoredJwt(JWT_TOKEN)).thenReturn(invalidJwtToken);

        String jwtCookieDoughContent = getJwtToken();
        Cookie httpOnlyCookie = createJwtCookie(jwtCookieDoughContent);
        Cookie[] cookies = {httpOnlyCookie};
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(cookies);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://sonar.url.com/somePageWhichIsNotLogin"));
        when(request.getQueryString()).thenReturn("test=testValue&test2=schn&Uuml;deWelt");
        when(request.getContextPath()).thenReturn("/sonar");
        // this bit switches the condition into a different branch
        when(request.getAttribute("LOGIN")).thenReturn("-");

        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        // when
        sut.doFilter(request, response, filterChain);

        // then
        VerificationMode noInteraction = times(0);
        verify(filterChain, noInteraction).doFilter(request, response);
        verify(response, times(1)).sendRedirect("/sonar/sessions/new");
        verify(response, times(1)).addCookie(any()); // keep the original URL in a cookie
        verify(store, noInteraction).invalidateJwt(anyString());
        verify(response).sendRedirect(any());
    }

    @Test
    public void getMaxCookieAgeShouldReturnConfiguredValue() {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sessionStorePath", "/tmp")
                .withAttribute("sonar.cas.urlAfterCasRedirectCookieMaxAgeSeconds", "100");
        CasSessionStoreFactory sessionStoreFactory = new CasSessionStoreFactory(config);
        LogoutHandler logoutHandler = new LogoutHandler(config, sessionStoreFactory);
        ForceCasLoginFilter sut = new ForceCasLoginFilter(config, logoutHandler);

        int actual = sut.getMaxCookieAge(config);

        assertThat(actual).isEqualTo(100);
    }

    @Test
    public void getMaxCookieAgeShouldReturnDefaultValue() {
        Configuration config = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sessionStorePath", "/tmp"); // no max age was set
        CasSessionStoreFactory sessionStoreFactory = new CasSessionStoreFactory(config);
        LogoutHandler logoutHandler = new LogoutHandler(config, sessionStoreFactory);
        ForceCasLoginFilter sut = new ForceCasLoginFilter(config, logoutHandler);

        int actualSeconds = sut.getMaxCookieAge(config);

        assertThat(actualSeconds).isEqualTo(300);
    }

    @Test
    public void isInAllowListShouldReturnFalse() {
        ForceCasLoginFilter sut = new ForceCasLoginFilter(null, null);

        boolean actual = sut.isInAllowList("/ui/endpoint");

        assertThat(actual).isFalse();
    }

    @Test
    public void isInAllowListShouldReturnTrue() {
        ForceCasLoginFilter sut = new ForceCasLoginFilter(null, null);

        assertThat(sut.isInAllowList("/sessions/init/sonarqube")).isTrue();
        assertThat(sut.isInAllowList("/api/endpoint/test")).isTrue();
    }
}
