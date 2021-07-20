package org.sonar.plugins.cas;

import org.jasig.cas.client.validation.*;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class provides support for different CAS validation protocols of which these are supported:
 *
 * <ul>
 * <li>SAML 1.1</li>
 * <li>CAS 1</li>
 * <li>CAS 2</li>
 * <li>CAS 3, which is also the default protocol.</li>
 * </ul>
 *
 * <p>
 *     The protocol is configurable by the sonar property <code>sonar.cas.protocol</code>. Furthermore the CAS server
 *     URL (configurable by setting <code>sonar.cas.casServerUrlPrefix</code> is needed in order to redirect SonarQube's
 *     validation request .
 * </p>
 */
@ServerSide
public final class CasTicketValidatorFactory implements TicketValidatorFactory {
    private static final String DEFAULT_CAS_PROTOCOL = "cas3";
    private final Configuration configuration;

    public CasTicketValidatorFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public TicketValidator create() {
        String protocol = getCasProtocol();
        TicketValidator validator;
        if ("saml11".equals(protocol)) {
            validator = createSaml11TicketValidator();
        } else if ("cas1".equalsIgnoreCase(protocol)) {
            validator = createCas10TicketValidator();
        } else if ("cas2".equalsIgnoreCase(protocol)) {
            validator = createCas20ServiceTicketValidator();
        } else if ("cas3".equalsIgnoreCase(protocol)) {
            validator = createCas20ServiceTicketValidator();
        } else {
            throw new IllegalStateException("Could not create service ticket validator: unsupported CAS protocol ".concat(protocol));
        }
        return validator;
    }

    public Cas30ProxyTicketValidator createForProxy() {
        String protocol = getCasProtocol();
        Cas30ProxyTicketValidator validator;
        if ("cas3".equalsIgnoreCase(protocol)) {
            validator = createCas30ProxyTicketValidator();
            validator.setAcceptAnyProxy(false);

            List<String[]> proxyChains = new ArrayList<>();
            proxyChains.add(new String[]{"^https?://192\\.168\\.56\\.2/.*$"});
            proxyChains.add(new String[]{"^https?://192\\.168\\.56\\.1(:\\d{4,5})?/.*$"});
            ProxyList proxyList = new ProxyList(proxyChains);
            validator.setAllowedProxyChains(proxyList);
        } else {
            throw new IllegalStateException("Could not create proxy ticket validator: unsupported CAS protocol ".concat(protocol));
        }
        return validator;
    }

    private String getCasProtocol() {
        return SonarCasProperties.CAS_PROTOCOL.getString(configuration, DEFAULT_CAS_PROTOCOL).toLowerCase(Locale.ENGLISH);
    }

    private String getCasServerUrlPrefix() {
        return SonarCasProperties.CAS_SERVER_URL_PREFIX.mustGetString(configuration);
    }

    private Saml11TicketValidator createSaml11TicketValidator() {
        Saml11TicketValidator saml11TicketValidator = new Saml11TicketValidator(getCasServerUrlPrefix());

        // the validator's internal tolerance is already at 1000 millis so the drifting tolerance does not
        // need to be set at any circumstance.
        int tolerance = SonarCasProperties.SAML11_TIME_TOLERANCE.getInteger(configuration, -1);
        if (tolerance != -1) {
            saml11TicketValidator.setTolerance(tolerance);
        }

        return saml11TicketValidator;
    }

    private Cas10TicketValidator createCas10TicketValidator() {
        return new Cas10TicketValidator(getCasServerUrlPrefix());
    }

    private Cas20ServiceTicketValidator createCas20ServiceTicketValidator() {
        return new Cas30ServiceTicketValidator(getCasServerUrlPrefix());
    }

    private Cas20ProxyTicketValidator createCas20ProxyTicketValidator() {
        return new Cas20ProxyTicketValidator(getCasServerUrlPrefix());
    }

    private Cas30ProxyTicketValidator createCas30ProxyTicketValidator() {
        return new Cas30ProxyTicketValidator(getCasServerUrlPrefix());
    }
}
