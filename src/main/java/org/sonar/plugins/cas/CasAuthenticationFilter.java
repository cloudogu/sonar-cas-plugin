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

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.validation.AbstractTicketValidationFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.sonar.api.config.Settings;
import org.sonar.api.web.ServletFilter;

import javax.servlet.*;
import java.io.IOException;

public class CasAuthenticationFilter extends ServletFilter {
  private AuthenticationFilter authenticationFilter;
  private Settings settings;

  public CasAuthenticationFilter(Settings settings) {
    this.settings = settings;
  }

  @Override
  public UrlPattern doGetPattern() {
    return UrlPattern.create("/sessions/new/*");
  }

  public void init(FilterConfig filterConfig) throws ServletException {
    SettingsFilterConfig config = new SettingsFilterConfig(filterConfig, settings, "sonar.cas.authentication");
    authenticationFilter = new AuthenticationFilter();
    authenticationFilter.init(config);
}

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    authenticationFilter.doFilter(servletRequest, servletResponse, filterChain);
  }

  public void destroy() {
    authenticationFilter.destroy();
  }
}
