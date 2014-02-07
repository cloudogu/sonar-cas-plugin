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

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import com.google.common.collect.Iterators;

public final class SettingsFilterConfig implements FilterConfig {

  private final FilterConfig initialConfig;
  private final Map<String, String> properties;

  public SettingsFilterConfig(final FilterConfig initialConfig, final Map<String, String> properties) {
    this.initialConfig = initialConfig;
    this.properties = properties;
  }

  public String getFilterName() {
    return initialConfig.getFilterName();
  }

  public ServletContext getServletContext() {
    return initialConfig.getServletContext();
  }

  public String getInitParameter(final String s) {
    return properties.get(s);
  }

  public Enumeration<String> getInitParameterNames() {
    return Iterators.asEnumeration(properties.keySet().iterator());
  }
}
