package org.sonar.plugins.cas;

import org.jasig.cas.client.validation.*;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class CasTicketValidatorFactoryTest {

    @Test(expected = IllegalStateException.class)
    public void createShouldThrowExceptionOnUnknownProtocol() {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.protocol", "BreadAndButterProtocolV3.rot")
                .withAttribute("sonar.cas.casServerUrlPrefix", "http://url.com");
        CasTicketValidatorFactory sut = new CasTicketValidatorFactory(configuration);

        sut.create();
    }

    @Test
    public void createShouldReturnSaml11Validator() {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.protocol", "saml11")
                .withAttribute("sonar.cas.casServerUrlPrefix", "http://url.com");
        CasTicketValidatorFactory sut = new CasTicketValidatorFactory(configuration);

        TicketValidator actual = sut.create();
        assertThat(actual).isNotNull().isInstanceOf(Saml11TicketValidator.class);
    }

    @Test
    public void createShouldReturnCas1Validator() {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.protocol", "cas1")
                .withAttribute("sonar.cas.casServerUrlPrefix", "http://url.com");
        CasTicketValidatorFactory sut = new CasTicketValidatorFactory(configuration);

        TicketValidator actual = sut.create();
        assertThat(actual).isNotNull().isInstanceOf(Cas10TicketValidator.class);
    }

    @Test
    public void createShouldReturnCas2Validator() {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.protocol", "cas2")
                .withAttribute("sonar.cas.casServerUrlPrefix", "http://url.com");
        CasTicketValidatorFactory sut = new CasTicketValidatorFactory(configuration);

        TicketValidator actual = sut.create();
        assertThat(actual).isNotNull().isInstanceOf(Cas20ServiceTicketValidator.class);
    }

    @Test
    public void createShouldReturnCas3Validator() {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", "http://url.com")
                .withAttribute("sonar.cas.protocol", "cas3");
        CasTicketValidatorFactory sut = new CasTicketValidatorFactory(configuration);

        TicketValidator actual = sut.create();

        assertThat(actual).isNotNull().isInstanceOf(Cas30ServiceTicketValidator.class);
    }

    @Test
    public void createShouldReturnDefaultValidator() {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.protocol<----was not configured", "dummy")
                .withAttribute("sonar.cas.casServerUrlPrefix", "http://url.com");
        CasTicketValidatorFactory sut = new CasTicketValidatorFactory(configuration);

        TicketValidator actual = sut.create();
        assertThat(actual).isNotNull().isInstanceOf(Cas30ServiceTicketValidator.class);
    }

    @Test(expected = IllegalStateException.class)
    public void createForProxyShouldThrowExceptionForCas2() {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.protocol", "cas2")
                .withAttribute("sonar.cas.casServerUrlPrefix", "http://url.com");
        CasTicketValidatorFactory sut = new CasTicketValidatorFactory(configuration);

        sut.createForProxy();
    }

    @Test
    public void createForProxyShouldReturnCas3ProxyValidator() {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.casServerUrlPrefix", "http://url.com")
                .withAttribute("sonar.cas.protocol", "cas3")
                .withAttribute("sonar.cas.proxyTicketing.services", "^https://test.de/.*$");
        CasTicketValidatorFactory sut = new CasTicketValidatorFactory(configuration);

        TicketValidator actual = sut.createForProxy();

        assertThat(actual).isNotNull().isInstanceOf(Cas30ProxyTicketValidator.class);
    }

    @Test(expected = IllegalStateException.class)
    public void createForProxyShouldThrowExceptionOnSupportedProtocol() {
        SonarTestConfiguration configuration = new SonarTestConfiguration()
                .withAttribute("sonar.cas.protocol", "saml11")
                .withAttribute("sonar.cas.casServerUrlPrefix", "http://url.com");
        CasTicketValidatorFactory sut = new CasTicketValidatorFactory(configuration);

        sut.createForProxy();
    }
}