package org.sonar.plugins.cas;

import org.jasig.cas.client.validation.TicketValidator;

/**
 * This interface is used in conjunction with SonarQube's dependency injection in order to enable testing.
 */
interface TicketValidatorFactory {
    /**
     * Creates a new {@link TicketValidator}
     * @return a new {@link TicketValidator}
     */
    TicketValidator create();
}
