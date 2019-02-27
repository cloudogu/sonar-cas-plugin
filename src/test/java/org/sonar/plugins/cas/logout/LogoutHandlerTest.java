package org.sonar.plugins.cas.logout;

import org.junit.Test;
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
import static org.sonar.plugins.cas.logout.LogoutHandler.JWT_SESSION_COOKIE;

public class LogoutHandlerTest {

    @Test
    public void logoutShouldInvalidateTicketInStore() {
        CasSessionStore store = mock(CasSessionStore.class);
        String ticketID = "ST-2-MCVscBHPvotTXcRW7kFF-45aa256f981c";
        String logoutRequest = getLogoutRequest(ticketID);
        LogoutHandler sut = new LogoutHandler(store);

        sut.logout(logoutRequest);

        verify(store).invalidateJwt(ticketID);
    }

    @Test
    public void handleInvalidJwtCookie() throws IOException {
        // given
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
        LogoutHandler sut = new LogoutHandler(store);

        // when
        boolean removeCookiesAndRedirectToLogin = sut.handleInvalidJwtCookie(request, response);

        // then
        verify(store).isJwtStored(JWT_TOKEN);
        verify(store).getJwtById(JWT_TOKEN);
        verify(response, times(2)).addCookie(any());
        verify(response).sendRedirect("http://sonar.url.com//sessions/new");
        assertThat(removeCookiesAndRedirectToLogin).isTrue();
    }

    private String getLogoutRequest(String ticketID) {
        return "<samlp:LogoutRequest " +
                "xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" " +
                "ID=\"LR-1-GcWNE4kZSwIynPKkRuDGnbXN1l9WoZXr9AX\" " +
                "Version=\"2.0\" " +
                "IssueInstant=\"2019-02-26T12:39:09Z\">" +
                "   <saml:NameID " +
                "       xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">" +
                "       @NOT_USED@" +
                "   </saml:NameID>" +
                "   <samlp:SessionIndex>" + ticketID + "</samlp:SessionIndex>" +
                "</samlp:LogoutRequest>";
    }
}