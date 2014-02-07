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

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.sonar.api.config.Settings;
import org.sonar.api.web.ServletFilter;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public abstract class AbstractCasFilter extends ServletFilter {
  private static final String PROPERTY_SONAR_SERVER_URL = SonarCasPropertyNames.SONAR_SERVER_URL.toString();
  private final Filter casFilter;
  private final Settings settings;

  public AbstractCasFilter(final Settings settings, final Filter casFilter) {
    this.settings = settings;
    this.casFilter = casFilter;
  }

  @Override
  public abstract UrlPattern doGetPattern();

  public final void init(final FilterConfig initialConfig) throws ServletException {
    final SettingsFilterConfig config = new SettingsFilterConfig(initialConfig, loadProperties());
    casFilter.init(config);
  }

  public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
    casFilter.doFilter(servletRequest, servletResponse, filterChain);
  }

  public final void destroy() {
    casFilter.destroy();
  }

  /**
   * Validate and load properties.
   * @return Map of properties.
   */
  protected Map<String, String> loadProperties() {
    final Map<String, String> properties = Maps.newHashMap();

    final String sonarUrl = settings.getString(PROPERTY_SONAR_SERVER_URL);
    Preconditions.checkState(!Strings.isNullOrEmpty(sonarUrl), "Missing property: " + PROPERTY_SONAR_SERVER_URL);
    Preconditions.checkState(!sonarUrl.endsWith("/"), "Property " + PROPERTY_SONAR_SERVER_URL + " must not end with slash: " + sonarUrl);
    properties.put("service", sonarUrl + "/cas/validate");

    doCompleteProperties(settings, properties);
    return properties;
  }

  protected abstract void doCompleteProperties(Settings settings, Map<String, String> properties);

  public final Filter getCasFilter() {
    return casFilter;
  }
}
