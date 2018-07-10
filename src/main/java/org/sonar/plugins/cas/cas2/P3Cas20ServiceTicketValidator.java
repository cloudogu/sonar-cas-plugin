package org.sonar.plugins.cas.cas2;

import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;

/**
 * {@link Cas20ServiceTicketValidator} with configurable service validation endpoint.
 *
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
public class P3Cas20ServiceTicketValidator extends Cas20ServiceTicketValidator {

    public P3Cas20ServiceTicketValidator(String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    @Override
    protected String getUrlSuffix() {
        // TODO configurable
        return "p3/serviceValidate";
    }
}
