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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cas.util.SonarCasPropertyNames;

/**
 * Parse the settings and provide attribute configuration.
 * @author Jan Boerner, TRIOLOGY GmbH
 */
public class CasAttributeSettings implements ServerExtension {
  private final Configuration settings;

  /**
   * Constructor.
   * @param configuration The sonar settings object.
   */
  public CasAttributeSettings(final Configuration configuration) {
    settings = configuration;
  }

  /**
   * @return the roleAttributes
   */
  public List<String> getRoleAttributes() {
    final String str = settings.get(SonarCasPropertyNames.ROLES_ATTRIBUTE.toString()).orElse(null);
    return null != str ? Arrays.asList(str.split("\\s*,\\s*")) : null;
  }


  /**
   * @return the fullNameAttribute
   */
  public String getFullNameAttribute() {
    return settings.get(SonarCasPropertyNames.FULL_NAME_ATTRIBUTE.toString()).orElse("cn");
  }

  /**
   * @return the eMailAttribute
   */
  public String geteMailAttribute() {
    return settings.get(SonarCasPropertyNames.EMAIL_ATTRIBUTE.toString()).orElse("mail");
  }

}
