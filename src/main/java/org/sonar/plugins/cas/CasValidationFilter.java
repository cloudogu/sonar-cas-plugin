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

import org.jasig.cas.client.validation.AbstractTicketValidationFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.sonar.api.config.Settings;
import org.sonar.api.web.ServletFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class CasValidationFilter extends ServletFilter {
  private AbstractTicketValidationFilter validationFilter;
  private Settings settings;

  public CasValidationFilter(Settings settings) {
    this.settings = settings;
  }

  /**
   * Override to change URL. Default is /*
   */
  @Override
  public UrlPattern doGetPattern() {
    return UrlPattern.create("/cas/validate");
  }

  public void init(FilterConfig initialConfig) throws ServletException {
    SettingsFilterConfig config = new SettingsFilterConfig(initialConfig, settings, "sonar.cas.validation");
    validationFilter = new Cas20ProxyReceivingTicketValidationFilter() {
      @Override
      protected void onSuccessfulValidation(HttpServletRequest request, HttpServletResponse response, Assertion assertion) {
        super.onSuccessfulValidation(request, response, assertion);
        System.out.println("assertion principal name: " + assertion.getPrincipal().getName());
        for (Map.Entry<String, Object> entry : assertion.getPrincipal().getAttributes().entrySet()) {
          System.out.println("attribute " + entry.getKey() + ": " + entry.getValue());
        }
      }
    };
    validationFilter.init(config);
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    validationFilter.doFilter(servletRequest, servletResponse, filterChain);
  }

  public void destroy() {
    validationFilter.destroy();
  }
}
