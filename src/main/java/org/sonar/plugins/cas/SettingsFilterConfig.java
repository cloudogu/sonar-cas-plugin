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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.sonar.api.config.Settings;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.List;

class SettingsFilterConfig implements FilterConfig {

  private FilterConfig initialConfig;
  private Settings settings;
  private String propertyPrefix;

  SettingsFilterConfig(FilterConfig initialConfig, Settings settings, String propertyPrefix) {
    this.initialConfig = initialConfig;
    this.settings = settings;
    this.propertyPrefix = propertyPrefix;
  }

  public String getFilterName() {
    return initialConfig.getFilterName();
  }

  public ServletContext getServletContext() {
    return initialConfig.getServletContext();
  }

  public String getInitParameter(String s) {
    return settings.getString(propertyPrefix + "." + s);
  }

  public Enumeration getInitParameterNames() {
    List<String> keys = Lists.newArrayList();
    for (String completeKey : settings.getKeysStartingWith(propertyPrefix + ".")) {
      keys.add(completeKey.substring(propertyPrefix.length() + 1));
    }
    return Iterators.asEnumeration(keys.listIterator());
  }
}
