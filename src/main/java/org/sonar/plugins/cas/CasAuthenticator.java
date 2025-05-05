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
import org.apache.commons.lang.StringUtils;
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
import org.sonar.api.server.http.HttpRequest;
import org.sonar.plugins.cas.util.CasAuthenticationException;
import org.sonar.plugins.cas.util.JakartaHttpRequestAttributeWrapper;
import org.sonar.plugins.cas.util.SonarCasProperties;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private static final String PROXY_TICKET_STARTER = "ProxyTicket===:";

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
        LOG.debug("CasAuthenticator.doAuthenticate(): {}", context.getHttpRequest().getContextPath());
        boolean authenticated;

        if (isAuthenticateByProxyTicket(context.getPassword())) {
            LOG.debug("Start handling proxy ticket authentication");
            authenticated = handleProxyTicketing(context);
        } else {
            LOG.debug("Start handling service ticket authentication");
            authenticated = handleServiceTicketing(context);
        }

        if (authenticated) {
            LOG.info("Successful authentication via CAS REST API");
        }

        return authenticated;
    }

    private boolean handleProxyTicketing(Context context) {
        String proxyTicket = getProxyTicket(context.getPassword());
        TicketValidator validator = ticketValidatorFactory.createForProxy();

        Assertion assertion = validateTicketAssertion(validator, proxyTicket);
        if (assertion == null) {
            return false;
        }

        enrichSuccessfullyAuthenticatedRequest(context, assertion);

        return true;
    }

    private boolean handleServiceTicketing(Context context) {
        String username = context.getUsername();
        String password = context.getPassword();
        String serviceTicket;

        try {
            serviceTicket = casRestClient.createServiceTicket(username, password);
        } catch (CasAuthenticationException ex) {
            LOG.warn("CAS authentication failed", ex);
            return false;
        }
        TicketValidator validator = ticketValidatorFactory.create();

        Assertion assertion = validateTicketAssertion(validator, serviceTicket);
        if (assertion == null) {
            return false;
        }

        enrichSuccessfullyAuthenticatedRequest(context, assertion);

        return true;
    }

    Assertion validateTicketAssertion(TicketValidator validator, String ticket) {
        String serviceUrl = getServiceUrl();
        Assertion assertion;
        try {
            assertion = validator.validate(ticket, serviceUrl);
        } catch (TicketValidationException ex) {
            LOG.warn("Ticket validation failed", ex);
            return null;
        }

        if (assertion == null) {
            LOG.warn("Ticket validator returned no assertion: assuming a failed authentication");
            return null;
        }

        if (!assertion.isValid()) {
            LOG.warn("Failed authentication: Ticket assertion is invalid");
            return null;
        }

        return assertion;
    }

    private void enrichSuccessfullyAuthenticatedRequest(Context context, Assertion assertion) {
        populateUserDetails(context.getHttpRequest(), assertion);
    }

    private void populateUserDetails(HttpRequest request, Assertion assertion) {
        // add assertions to request attribute, in order to process groups with the CasGroupsProvider
        request.setAttribute(Assertion.class.getName(), assertion);
        UserDetails user = getUserDetails(request);
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

    private UserDetails getUserDetails(HttpRequest request) {
        UserDetails user = null;
        JakartaHttpRequestAttributeWrapper req = new JakartaHttpRequestAttributeWrapper(request);
        // get user attributes from request, which was previously added by the CasUserProvider
        try {
            user = (UserDetails)req.getAttribute(UserDetails.class.getName());
            LOG.debug("UserDetails: {}", user);
        } catch (Exception e) {
            LOG.info("Error on getting userdetails", e);
            // it is possible that the user stays null
            // this is an expected case and no error case
            // the null user will be returned and preconditions take care of the null user
        }
        return user;
    }

    private boolean isAuthenticateByProxyTicket(String aString) {
        if (StringUtils.isEmpty(aString)) {
            return false;
        }

        return aString.startsWith(PROXY_TICKET_STARTER);
    }

    private String getProxyTicket(String aString) {
        return aString.substring(PROXY_TICKET_STARTER.length());
    }

    private String getServiceUrl() {
        return SonarCasProperties.SONAR_SERVER_URL.mustGetString(configuration);
    }
}
