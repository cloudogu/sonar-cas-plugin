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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.security.Authenticator;
import org.sonar.api.security.UserDetails;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.CasAuthenticationException;
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * The {@link CasAuthenticator} authenticates the user against the CAS Rest API with the provided username and password
 * from the context.
 * <p>
 * After a successful authentication the CAS assertion is stored in the {@link HttpServletRequest} (to process the
 * groups in the next step with {@link CasGroupsProvider}) and additionally the {@link UserDetails}, which have been
 * stored in the request by the {@link CasUserProvider}, are populated with the now available information from the
 * assertion.
 * </p>
 *
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
@ServerSide
public class CasAuthenticator extends Authenticator {

    private static final Logger LOG = LoggerFactory.getLogger(CasAuthenticator.class);

    private final Configuration configuration;
    private final CasAttributeSettings attributeSettings;

    private final TicketValidatorFactory ticketValidatorFactory;
    private final CasRestClient casRestClient;

    /**
     * called with injection by SonarQube during server initialization
     */
    CasAuthenticator(Configuration configuration, CasAttributeSettings attributeSettings,
                     TicketValidatorFactory ticketValidatorFactory, CasRestClientFactory casRestClientFactory) {
        this.configuration = configuration;
        this.attributeSettings = attributeSettings;

        this.ticketValidatorFactory = ticketValidatorFactory;
        this.casRestClient = casRestClientFactory.create();
    }

    @Override
    public boolean doAuthenticate(Context context) {
        LOG.debug("CasAuthenticator.doAuthenticate()");
        try {
            String ticket = casRestClient.createServiceTicket(context.getUsername(), context.getPassword());

            TicketValidator validator = ticketValidatorFactory.create();
            String serviceUrl = getServiceUrl();
            Assertion assertion = validator.validate(ticket, serviceUrl);

            if (assertion != null) {
                LOG.info("successful authentication via CAS REST API");
                // add assertions to request attribute, in order to process groups with the CasGroupsProvider
                context.getRequest().setAttribute(Assertion.class.getName(), assertion);

                populateUserDetails(context.getRequest(), assertion);
                return true;
            }

            LOG.warn("ticket validator returned no assertion");
        } catch (CasAuthenticationException ex) {
            LOG.warn("authentication failed", ex);
        } catch (TicketValidationException ex) {
            LOG.warn("ticket validation failed", ex);
        }
        return false;
    }

    private void populateUserDetails(HttpServletRequest request, Assertion assertion) {
        // get user attributes from request, which was previously added by the CasUserProvider
        UserDetails user = (UserDetails) request.getAttribute(UserDetails.class.getName());
        Preconditions.checkState(user != null, "could not find UserDetails in the request");

        // populate user details from assertion
        AttributePrincipal principal = assertion.getPrincipal();

        Map<String, Object> attributes = principal.getAttributes();
        user.setUserId(principal.getName());

        String displayName = attributeSettings.getDisplayName(attributes);
        if (!Strings.isNullOrEmpty(displayName)) {
            user.setName(displayName);
        }

        String email = attributeSettings.getEmail(attributes);
        if (!Strings.isNullOrEmpty(email)) {
            user.setEmail(email);
        }
    }

    private String getServiceUrl() {
        return SonarCasProperties.SONAR_SERVER_URL.mustGetString(configuration);
    }
}
