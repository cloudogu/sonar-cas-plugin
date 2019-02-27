package org.sonar.plugins.cas;

import org.jasig.cas.client.validation.*;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.util.Locale;

/**
 *
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
            throw new IllegalStateException("unknown cas protocol ".concat(protocol));
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
        if(tolerance != -1) {
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
}
