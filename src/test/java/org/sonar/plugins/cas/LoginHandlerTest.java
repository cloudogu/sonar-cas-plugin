package org.sonar.plugins.cas;

import org.junit.Test;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.sonar.plugins.cas.LoginHandler.getTicketParameter;

public class LoginHandlerTest {
    @Test
    public void getTicketParameterShouldReturnEmptyString() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getParameter("ticket")).thenReturn(null);

        CasSessionStore sessionStore = mock(CasSessionStore.class);
        CasSessionStoreFactory sessionStoreFactory = mock(CasSessionStoreFactory.class);
        when(sessionStoreFactory.getInstance()).thenReturn(sessionStore);

        // when
        String actual = getTicketParameter(request);

        // then
        assertThat(actual).isEqualTo("");
        verify(request, times(1)).getParameter("ticket");
    }

    @Test
    public void getTicketParameterShouldReturnTicketParameter() {
        HttpRequest request = mock(HttpRequest.class);
        // even when the ticket is called ST-XYZZY it could represent both a proxy ticket or a service ticket
        when(request.getParameter("ticket")).thenReturn("ST-012345678");

        CasSessionStore sessionStore = mock(CasSessionStore.class);
        CasSessionStoreFactory sessionStoreFactory = mock(CasSessionStoreFactory.class);
        when(sessionStoreFactory.getInstance()).thenReturn(sessionStore);

        // when
        String actual = getTicketParameter(request);

        // then
        assertThat(actual).isEqualTo("ST-012345678");
        verify(request, times(1)).getParameter("ticket");
    }
}
