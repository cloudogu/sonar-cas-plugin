package org.sonar.plugins.cas;

import org.jasig.cas.client.validation.Cas20ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * This interface is used in conjunction with SonarQube's dependency injection in order to enable testing.
 * <p>Implementations must produce two different TicketValidator type:</p>
 * <ul>
 *     <li>a service ticket validator</li>
 *     <li>a proxy ticket validator</li>
 * </ul>
 */
interface TicketValidatorFactory {
    /**
     * Creates a new {@link TicketValidator} that validates session tickets
     * @return a new {@link TicketValidator}
     */
    TicketValidator create();

    /**
     * Creates a new {@link TicketValidator} that validates proxy tickets
     * @return a new {@link TicketValidator}
     */
    Cas20ProxyTicketValidator createForProxy();
}
