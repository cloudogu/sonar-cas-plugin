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
import org.sonar.api.web.ServletFilter;

/**
 * This filter checks for users authenticated against JASIG CAS. That means a CAS_ASSERTION is stored in the user
 * session. If not, the client is redirected to the /session/new address to create a new user session.
 * @author Jan Boerner, TRIOLOGY GmbH
 */
public class ForceCasLoginFilter extends ServletFilter {

  public void init(FilterConfig filterConfig) throws ServletException {
    // nothing to do
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    if (!httpRequest.getServletPath().contains("/sessions/") && !httpRequest.getServletPath().contains("/cas/validate")) {
      HttpSession session = httpRequest.getSession(false);
      Assertion assertion = (null != session) ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)
          : null;
      if (null == assertion || null == assertion.getPrincipal()) {
        ((HttpServletResponse) response).sendRedirect(UrlPattern.create("/sessions/new").getUrl());
      }
    }
    chain.doFilter(httpRequest, response);
  }

  public void destroy() {
    // nothing to do
  }

}
