package org.sonar.plugins.cas.logout;

import org.junit.Test;
import org.mockito.verification.VerificationMode;
import org.sonar.plugins.cas.AuthTestData;
import org.sonar.plugins.cas.SonarTestConfiguration;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;
import org.sonar.plugins.cas.util.Cookies;
import org.sonar.plugins.cas.util.SimpleJwt;
import org.xml.sax.SAXException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.sonar.plugins.cas.AuthTestData.JWT_TOKEN;
import static org.sonar.plugins.cas.AuthTestData.getJwtToken;
import static org.sonar.plugins.cas.util.Cookies.JWT_SESSION_COOKIE;

public class LogoutHandlerTest {

    @Test
    public void logoutShouldInvalidateTicketInStore() throws IOException, ParserConfigurationException, SAXException {
//        SonarTestConfiguration configuration = new SonarTestConfiguration()
//                .withAttribute("sonar.cas.sonarServerUrl", "http://sonar.url.com");
//        CasSessionStore store = mock(CasSessionStore.class);
//        CasSessionStoreFactory factory = mock(CasSessionStoreFactory.class);
//        when(factory.getInstance()).thenReturn(store);
//        String ticketID = "ST-2-MCVscBHPvotTXcRW7kFF-45aa256f981c";
//
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        String logoutRequest = AuthTestData.getLogoutTicketForId(ticketID);
//        when(request.getParameter("logoutRequest")).thenReturn(logoutRequest);
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        LogoutHandler sut = new LogoutHandler(configuration, factory);
//
//        // when
//        sut.logout(request, response);
//
//        // then
//        verify(store).invalidateJwt(ticketID);
//        verify(response).sendRedirect("http://sonar.url.com/sessions/init/sonarqube");
    }

    @Test
    public void handleInvalidJwtCookie() {
        // given
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.urlAfterCasRedirectCookieMaxAgeSeconds", "300");
        CasSessionStore store = mock(CasSessionStore.class);
        CasSessionStoreFactory factory = mock(CasSessionStoreFactory.class);
        when(factory.getInstance()).thenReturn(store);
        when(store.isJwtStored(JWT_TOKEN)).thenReturn(true);
        SimpleJwt invalidJwtToken = JWT_TOKEN.cloneAsInvalidated();
        when(store.fetchStoredJwt(JWT_TOKEN)).thenReturn(invalidJwtToken);

        HttpServletRequest request = mock(HttpServletRequest.class);
        String jwtCookieDoughContent = getJwtToken();
        Cookie httpOnlyCookie = new Cookies.HttpOnlyCookieBuilder()
                .name(JWT_SESSION_COOKIE)
                .value(jwtCookieDoughContent)
                .maxAgeInSecs(100)
                .contextPath("/sonar")
                .build();
        Cookie[] cookies = {httpOnlyCookie};
        when(request.getCookies()).thenReturn(cookies);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://sonar.url.com/somePageWhichIsNotLogin"));
        when(request.getContextPath()).thenReturn("/sonar");

        HttpServletResponse response = mock(HttpServletResponse.class);
        LogoutHandler sut = new LogoutHandler(configuration, factory);

        // when
        sut.handleInvalidJwtCookie(request, response);

        // then
        verify(store).isJwtStored(JWT_TOKEN);
        verify(store).fetchStoredJwt(JWT_TOKEN);
        VerificationMode addedCookies = times(2); //add deletion cookie for JWT and XSRF
        verify(response, addedCookies).addCookie(any());
    }
}