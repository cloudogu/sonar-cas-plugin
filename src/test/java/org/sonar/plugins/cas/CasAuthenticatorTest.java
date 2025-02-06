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
import static org.mockito.Mockito.*;

public class CasAuthenticatorTest {

    private static final String CAS_SERVER_PREFIX = "https://cas.server.com";
    private static final String SONAR_SERVER_URL_PREFIX = "https://cas.server.com";

    //@Test
    public void userShouldAuthenticateRestCallByBasicAuth() throws TicketValidationException {
        // given
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", CAS_SERVER_PREFIX)
                .withAttribute("sonar.cas.sonarServerUrl", SONAR_SERVER_URL_PREFIX);
        CasAttributeSettings attributes = new CasAttributeSettings(configuration);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://sonar.server.com/api/endpoint"));
        UserDetails userDetails = new UserDetails();
        userDetails.setName("Mr. T");
        when(request.getAttribute(UserDetails.class.getName())).thenReturn(userDetails);

        TicketValidator validator = mock(TicketValidator.class);
        Assertion assertion = mock(Assertion.class);
        when(assertion.getPrincipal()).thenReturn(new AttributePrincipalImpl("mrt"));
        when(assertion.isValid()).thenReturn(true);
        when(validator.validate(any(), any())).thenReturn(assertion);
        when(request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)).thenReturn(assertion);
        TicketValidatorFactory ticketValidatorFactory = mock(TicketValidatorFactory.class);
        when(ticketValidatorFactory.create()).thenReturn(validator);

        CasRestClientFactory casRestClientFactory = new CasRestClientFactory(configuration, new EasyTicketTestCasRestClient());

        CasAuthenticator authenticator = new CasAuthenticator(configuration, attributes, ticketValidatorFactory,
                casRestClientFactory);
        // CasAuthenticator.Context context = new CasAuthenticator.Context("mrt", "I pitty the fools who don't use passphrases!", request);

        // when
        // boolean actual = authenticator.doAuthenticate(context);

        // then
        // assertThat(actual).isTrue();
        verify(assertion, times(1)).isValid();
        verify(ticketValidatorFactory, times(1)).create();
        verify(validator).validate(any(), any());
    }

    //@Test
    public void userShouldAuthenticateRestCallByProxyTicket() throws TicketValidationException {
        // given
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", CAS_SERVER_PREFIX)
                .withAttribute("sonar.cas.sonarServerUrl", SONAR_SERVER_URL_PREFIX);
        CasAttributeSettings attributes = new CasAttributeSettings(configuration);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("https://sonar.server.com/api/endpoint"));
        UserDetails userDetails = new UserDetails();
        userDetails.setName("Mr. T");
        when(request.getAttribute(UserDetails.class.getName())).thenReturn(userDetails);

        TicketValidator validator = mock(TicketValidator.class);
        Assertion assertion = mock(Assertion.class);
        when(assertion.getPrincipal()).thenReturn(new AttributePrincipalImpl("mrt"));
        when(assertion.isValid()).thenReturn(true);
        when(validator.validate(any(), any())).thenReturn(assertion);
        when(request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)).thenReturn(assertion);
        TicketValidatorFactory ticketValidatorFactory = mock(TicketValidatorFactory.class);
        when(ticketValidatorFactory.createForProxy()).thenReturn(validator);

        CasRestClientFactory casRestClientFactory = new CasRestClientFactory(configuration, new EasyTicketTestCasRestClient());
        //CasAuthenticator.Context context = new CasAuthenticator.Context("mrt", "ProxyTicket===:ST-1234", request);

        CasAuthenticator sut = new CasAuthenticator(configuration, attributes, ticketValidatorFactory,
                casRestClientFactory);

        // when
        //boolean actual = sut.doAuthenticate(context);

        // then
        //assertThat(actual).isTrue();
        verify(assertion, times(1)).isValid();
        verify(ticketValidatorFactory, times(1)).createForProxy();
        verify(validator).validate(any(), any());
    }

    //@Test
    public void userShouldNotBeAuthenticated() {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", CAS_SERVER_PREFIX)
                .withAttribute("sonar.cas.sonarServerUrl", SONAR_SERVER_URL_PREFIX);

        TicketValidator validator = mock(TicketValidator.class);
        TicketValidatorFactory ticketValidatorFactory = mock(TicketValidatorFactory.class);
        when(ticketValidatorFactory.create()).thenReturn(validator);

        CasRestClientFactory clientFactory = new CasRestClientFactory(configuration, new EasyTicketTestCasRestClient());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)).thenReturn(null);
        //CasAuthenticator.Context context = new CasAuthenticator.Context(null, null, request);

        CasAuthenticator sut = new CasAuthenticator(configuration, null, ticketValidatorFactory, clientFactory);

        // when
        //boolean actual = sut.doAuthenticate(context);

        // then
        //assertThat(actual).isFalse();
    }

    @Test
    public void validateTicketAssertionShouldReturnNullOnTicketValidationException() throws TicketValidationException {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", CAS_SERVER_PREFIX)
                .withAttribute("sonar.cas.sonarServerUrl", SONAR_SERVER_URL_PREFIX);

        TicketValidator validator = mock(TicketValidator.class);
        when(validator.validate(any(), any())).thenThrow(new TicketValidationException("no u"));
        CasRestClientFactory clientFactory = new CasRestClientFactory(configuration, new EasyTicketTestCasRestClient());

        CasAuthenticator sut = new CasAuthenticator(configuration, null, null, clientFactory);

        // when
        Assertion actual = sut.validateTicketAssertion(validator, "ST-1234");

        // then
        assertThat(actual).isNull();
        verify(validator, times(1)).validate("ST-1234", SONAR_SERVER_URL_PREFIX);
    }

    @Test
    public void validateTicketAssertionShouldReturnNullOnNullAssertion() throws TicketValidationException {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", CAS_SERVER_PREFIX)
                .withAttribute("sonar.cas.sonarServerUrl", SONAR_SERVER_URL_PREFIX);

        TicketValidator validator = mock(TicketValidator.class);
        when(validator.validate(any(), any())).thenReturn(null);
        CasRestClientFactory clientFactory = new CasRestClientFactory(configuration, new EasyTicketTestCasRestClient());

        CasAuthenticator sut = new CasAuthenticator(configuration, null, null, clientFactory);

        // when
        Assertion actual = sut.validateTicketAssertion(validator, "ST-1234");

        // then
        assertThat(actual).isNull();
        verify(validator, times(1)).validate("ST-1234", SONAR_SERVER_URL_PREFIX);
    }

    @Test
    public void validateTicketAssertionShouldReturnNullOnInvalidAssertion() throws TicketValidationException {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", CAS_SERVER_PREFIX)
                .withAttribute("sonar.cas.sonarServerUrl", SONAR_SERVER_URL_PREFIX);

        TicketValidator validator = mock(TicketValidator.class);
        Assertion assertion = mock(Assertion.class);
        when(assertion.isValid()).thenReturn(false);
        when(validator.validate(any(), any())).thenReturn(assertion);
        CasRestClientFactory clientFactory = new CasRestClientFactory(configuration, new EasyTicketTestCasRestClient());

        CasAuthenticator sut = new CasAuthenticator(configuration, null, null, clientFactory);

        // when
        Assertion actual = sut.validateTicketAssertion(validator, "ST-1234");

        // then
        assertThat(actual).isNull();
        verify(assertion, times(1)).isValid();
        verify(validator, times(1)).validate("ST-1234", SONAR_SERVER_URL_PREFIX);
    }

    @Test
    public void validateTicketAssertionShouldReturnValidAssertion() throws TicketValidationException {
        Configuration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", CAS_SERVER_PREFIX)
                .withAttribute("sonar.cas.sonarServerUrl", SONAR_SERVER_URL_PREFIX);

        TicketValidator validator = mock(TicketValidator.class);
        Assertion assertion = mock(Assertion.class);
        when(assertion.isValid()).thenReturn(true);
        when(validator.validate(any(), any())).thenReturn(assertion);
        CasRestClientFactory clientFactory = new CasRestClientFactory(configuration, new EasyTicketTestCasRestClient());

        CasAuthenticator sut = new CasAuthenticator(configuration, null, null, clientFactory);

        // when
        Assertion actual = sut.validateTicketAssertion(validator, "ST-1234");

        // then
        assertThat(actual).isNotNull();
        verify(validator, times(1)).validate("ST-1234", SONAR_SERVER_URL_PREFIX);
    }

    static class EasyTicketTestCasRestClient extends CasRestClient {
        EasyTicketTestCasRestClient() {
            super(SONAR_SERVER_URL_PREFIX, CAS_SERVER_PREFIX);
        }

        @Override
        public String createServiceTicket(String username, String password) {
            return "ST-1234";
        }
    }
}
