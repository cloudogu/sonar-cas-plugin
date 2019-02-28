package org.sonar.plugins.cas.logout;

import org.junit.Test;
import org.sonar.plugins.cas.AuthTestData;
import org.sonar.plugins.cas.SonarTestConfiguration;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.util.CookieUtil;
import org.sonar.plugins.cas.util.SimpleJwt;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.sonar.plugins.cas.AuthTestData.JWT_TOKEN;
import static org.sonar.plugins.cas.AuthTestData.getJwtToken;
import static org.sonar.plugins.cas.util.CookieUtil.JWT_SESSION_COOKIE;

public class LogoutHandlerTest {

    @Test
    public void logoutShouldInvalidateTicketInStore() throws IOException {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.sonarServerUrl", "sonar.url.com");
        CasSessionStore store = mock(CasSessionStore.class);
        String ticketID = "ST-2-MCVscBHPvotTXcRW7kFF-45aa256f981c";

        HttpServletRequest request = mock(HttpServletRequest.class);
        String logoutRequest = AuthTestData.getLogoutTicketForId(ticketID);
        when(request.getParameter("logoutRequest")).thenReturn(logoutRequest);
        HttpServletResponse response = mock(HttpServletResponse.class);
        LogoutHandler sut = new LogoutHandler(configuration, store);

        // when
        sut.logout(request, response);

        // then
        verify(store).invalidateJwt(ticketID);
    }

    @Test
    public void handleInvalidJwtCookie() throws IOException {
        // given
        SonarTestConfiguration configuration = new SonarTestConfiguration();
        CasSessionStore store = mock(CasSessionStore.class);
        when(store.isJwtStored(JWT_TOKEN)).thenReturn(true);
        SimpleJwt invalidJwtToken = JWT_TOKEN.cloneAsInvalidated();
        when(store.getJwtById(JWT_TOKEN)).thenReturn(invalidJwtToken);

        HttpServletRequest request = mock(HttpServletRequest.class);
        String jwtCookieDoughContent = getJwtToken();
        Cookie httpOnlyCookie = CookieUtil.createHttpOnlyCookie(JWT_SESSION_COOKIE, jwtCookieDoughContent, 100);
        Cookie[] cookies = {httpOnlyCookie};
        when(request.getCookies()).thenReturn(cookies);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://sonar.url.com/somePageWhichIsNotLogin"));
        when(request.getContextPath()).thenReturn("http://sonar.url.com/");

        HttpServletResponse response = mock(HttpServletResponse.class);
        LogoutHandler sut = new LogoutHandler(configuration, store);

        // when
        boolean removeCookiesAndRedirectToLogin = sut.handleInvalidJwtCookie(request, response);

        // then
        verify(store).isJwtStored(JWT_TOKEN);
        verify(store).getJwtById(JWT_TOKEN);
        verify(response, times(2)).addCookie(any());
        verify(response).sendRedirect("http://sonar.url.com//sessions/new");
        assertThat(removeCookiesAndRedirectToLogin).isTrue();
    }
}