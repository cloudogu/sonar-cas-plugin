/*
 * Sonar CAS Plugin
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.cas;

import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.security.UserDetails;

import javax.servlet.http.HttpServletRequest;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CasAuthenticatorTest {
    @Test
    public void should_authenticate() throws TicketValidationException {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", "https://cas.server.com")
                .withAttribute("sonar.cas.sonarServerUrl", "https://sonar.server.com");
        CasAttributeSettings attributes = new CasAttributeSettings(configuration);

        HttpServletRequest request = mock(HttpServletRequest.class);
        UserDetails userDetails = new UserDetails();
        userDetails.setName("Mr. T");
        when(request.getAttribute(UserDetails.class.getName())).thenReturn(userDetails);

        TicketValidator validator = mock(TicketValidator.class);
        Assertion assertion = mock(Assertion.class);
        when(assertion.getPrincipal()).thenReturn(new AttributePrincipalImpl("goldorak"));
        when(validator.validate(any(), any())).thenReturn(assertion);
        when(request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)).thenReturn(assertion);
        TicketValidatorFactory ticketValidatorFactory = mock(TicketValidatorFactory.class);
        when(ticketValidatorFactory.create()).thenReturn(validator);

        CasRestClientFactory casRestClientFactory = new CasRestClientFactory(configuration, new TestCasRestClient());

        CasAuthenticator authenticator = new CasAuthenticator(configuration, attributes, ticketValidatorFactory,
                casRestClientFactory);
        CasAuthenticator.Context context = new CasAuthenticator.Context(null, null, request);

        boolean actual = authenticator.doAuthenticate(context);

        assertThat(actual).isTrue();
    }

    private class TestCasRestClient extends CasRestClient {
        TestCasRestClient() {
            super("https://sonar.server.com", "https://cas.server.com");
        }

        @Override
        public String createServiceTicket(String username, String password) {
            return "ST-1234";
        }
    }

    @Test
    public void user_should_not_be_authenticated() {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", "https://cas.server.com")
                .withAttribute("sonar.cas.sonarServerUrl", "https://sonar.server.com");

        TicketValidator validator = mock(TicketValidator.class);
        TicketValidatorFactory ticketValidatorFactory = mock(TicketValidatorFactory.class);
        when(ticketValidatorFactory.create()).thenReturn(validator);

        CasRestClientFactory clientFactory = new CasRestClientFactory(configuration, new TestCasRestClient());
        CasAuthenticator authenticator = new CasAuthenticator(configuration, null, ticketValidatorFactory, clientFactory);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)).thenReturn(null);

        CasAuthenticator.Context context = new CasAuthenticator.Context(null, null, request);

        // when
        boolean actual = authenticator.doAuthenticate(context);

        // then
        assertThat(actual).isFalse();
    }
}
