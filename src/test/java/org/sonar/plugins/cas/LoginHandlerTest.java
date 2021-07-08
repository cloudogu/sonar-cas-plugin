package org.sonar.plugins.cas;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;

import javax.servlet.http.HttpServletRequest;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LoginHandlerTest {
    @Test
    public void getTicketParameterShouldReturnEmptyString() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("ticket")).thenReturn(null);

        CasSessionStore sessionStore = mock(CasSessionStore.class);
        CasSessionStoreFactory sessionStoreFactory = mock(CasSessionStoreFactory.class);
        when(sessionStoreFactory.getInstance()).thenReturn(sessionStore);
        LoginHandler sut = new LoginHandler(null, null, sessionStoreFactory, null);

        // when
        String actual = sut.getTicketParameter(request);

        // then
        assertThat(actual).isEqualTo("");
        verify(request, times(1)).getParameter("ticket");
    }

    @Test
    public void getTicketParameterShouldReturnTicketParameter() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        // even when the ticket is called ST-XYZZY it could represent both a proxy ticket or a service ticket
        when(request.getParameter("ticket")).thenReturn("ST-012345678");

        CasSessionStore sessionStore = mock(CasSessionStore.class);
        CasSessionStoreFactory sessionStoreFactory = mock(CasSessionStoreFactory.class);
        when(sessionStoreFactory.getInstance()).thenReturn(sessionStore);
        LoginHandler sut = new LoginHandler(null, null, sessionStoreFactory, null);

        // when
        String actual = sut.getTicketParameter(request);

        // then
        assertThat(actual).isEqualTo("ST-012345678");
        verify(request, times(1)).getParameter("ticket");
    }
}