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

import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;

import org.sonar.api.config.Configuration;
import org.sonar.api.config.Settings;
import org.sonar.api.web.ServletFilter;

import org.sonar.plugins.cas.util.Credentials;
import org.sonar.plugins.cas.util.RequestUtil;
import org.sonar.plugins.cas.util.RestAuthenticator;

/**
 * This filter checks for users authenticated against JASIG CAS. That means a CAS_ASSERTION is stored in the user
 * session. If not, the client is redirected to the /session/new address to create a new user session.
 * 
 * @author Jan Boerner, TRIOLOGY GmbH
 * @author Sebastian Sdorra, TRIOLOGY GmbH
 */
public class ForceCasLoginFilter extends ServletFilter {

  /** Array of request URLS that should not be redirected to the login page. */
  private static final List<String> WHITE_LIST = Arrays.asList(
    new String[] {"/sessions/", "/cas/validate", "/api/", "/batch_bootstrap/", "/deploy/", "/batch"}
  );

  private final RestAuthenticator restAuthenticator;

  public ForceCasLoginFilter(Configuration configuration) {
    this.restAuthenticator = new RestAuthenticator(configuration);
  }
  
  public void init(final FilterConfig filterConfig) {
    // nothing to do
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    
    final HttpServletRequest httpRequest = RequestUtil.toHttp(request);
    
    // authenticate non browser clients
    if (!RequestUtil.isBrowser(httpRequest)){
      Credentials credentials = RequestUtil.getBasicAuthentication(httpRequest);
      if (credentials != null) {
        restAuthenticator.authenticate(credentials, httpRequest);
      }
    }
    
    if (!isInWhiteList(httpRequest.getServletPath())) {
      final HttpSession session = httpRequest.getSession();
      final Assertion assertion = (null != session) ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)
          : null;
      if (null == assertion || null == assertion.getPrincipal()) {
        ((HttpServletResponse) response).sendRedirect(UrlPattern.create("sessions/new").getUrl());
        return;
      }
    }
    chain.doFilter(httpRequest, response);
  }
  
  public void destroy() {
    // nothing to do
  }

  /**
   * Looks for the given value if it or parts of it are containing in the white list.
   * @param entry Entry to look for in white list.
   * @return true if found, false otherwise.
   */
  private boolean isInWhiteList(final String entry) {
    if (null != entry) {
      for (final String item : WHITE_LIST) {
        if (entry.contains(item)) {
          return true;
        }
      }
    }
    return false;
  }

}
