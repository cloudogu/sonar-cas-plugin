package org.sonar.plugins.cas;

import org.jasig.cas.client.validation.TicketValidator;

interface TicketValidatorFactory {
    /**
     * Creates a new {@link TicketValidator}
     * @return a new {@link TicketValidator}
     */
    TicketValidator create();
}
