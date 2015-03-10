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
package org.sonar.plugins.cas.util;

import com.google.common.base.Strings;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas10TicketValidator;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.Saml11TicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;

/**
 * Cas rest authentication.
 *
 * @author Sebastian Sdorra, TRIOLOGY GmbH
 */
public class RestAuthenticator {
  
  
  private static final Logger LOG = LoggerFactory.getLogger(RestAuthenticator.class);
  
  private final Settings settings;
  private final String casServerUrlPrefix;
  private final String serviceUrl;
  private final String casProtocol;

  public RestAuthenticator(Settings settings) {
    this.settings = settings;
    casServerUrlPrefix = getCasServerUrlPrefix();
    serviceUrl = getServiceUrl();
    casProtocol = Strings.nullToEmpty(getCasProtocol()).toLowerCase(Locale.ENGLISH);
  }
  
  private String getCasServerUrlPrefix() {
    return settings.getString(SonarCasPropertyNames.CAS_SERVER_URL_PREFIX.toString());
  }
  
  private String getServiceUrl() {
    return settings.getString(SonarCasPropertyNames.SONAR_SERVER_URL.toString());
  }
  
  private String getCasProtocol() {
    return settings.getString(SonarCasPropertyNames.CAS_PROTOCOL.toString());
  }
  
  public void authenticate(Credentials credentials, HttpServletRequest request) {
    try {
      CasRestClient crc = new CasRestClient(casServerUrlPrefix, serviceUrl);
      String ticket = crc.createServiceTicket(credentials.getUsername(), credentials.getPassword());
      Assertion assertion = createTicketValidator().validate(ticket, serviceUrl);
      if (assertion != null) {
        LOG.info("successful authentication via cas rest api");
        request.setAttribute(org.jasig.cas.client.util.AbstractCasFilter.CONST_CAS_ASSERTION, assertion);
      } else {
        LOG.warn("ticket validator returned no assertion");
      }
    } catch (CasAuthenticationException ex) {
      LOG.warn("authentication failed", ex);
    }
    catch (TicketValidationException ex) {
      LOG.warn("ticket validation failed", ex);
    }
  }
  
  private TicketValidator createTicketValidator() {
    TicketValidator validator;
    if ( "saml11".equals(casProtocol) ){
      validator = createSaml11TicketValidator();
    } else if ( "cas1".equalsIgnoreCase(casProtocol) ){
      validator = createCas10TicketValidator();
    } else if ( "cas2".equalsIgnoreCase(casProtocol) ){
      validator = createCas20ServiceTicketValidator();
    } else {
      throw new IllegalStateException("unknown cas protocol ".concat(casProtocol));
    }
    return validator;
  }
  
  private Saml11TicketValidator createSaml11TicketValidator() {
    /** TODO pass parameters **/
    return new Saml11TicketValidator(getCasServerUrlPrefix());
  }
  
  private Cas10TicketValidator createCas10TicketValidator() {
    /** TODO pass parameters **/
    return new Cas10TicketValidator(getCasServerUrlPrefix());
  }
  
  private Cas20ServiceTicketValidator createCas20ServiceTicketValidator() {
    /** TODO pass parameters **/
    return new Cas20ServiceTicketValidator(getCasServerUrlPrefix());
  }
  
}
