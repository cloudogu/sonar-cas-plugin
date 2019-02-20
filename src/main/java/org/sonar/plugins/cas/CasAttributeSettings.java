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

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.cas.util.SonarCasProperties;

import java.util.*;

/**
 * Parse the settings, provide attribute configuration and util methods for extracting the attribute values.
 *
 * @author Jan Boerner, TRIOLOGY GmbH
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
@ServerSide
public class CasAttributeSettings {

  private final Configuration settings;

  /**
   * Constructor.
   * @param configuration The sonar settings object.
   */
  public CasAttributeSettings(Configuration configuration) {
    settings = configuration;
  }

  /**
   * @return the roleAttributes
   */
  private List<String> getRoleAttributes() {
    final String str = settings.get(SonarCasProperties.ROLES_ATTRIBUTE.toString()).orElse(null);
    return null != str ? Arrays.asList(str.split("\\s*,\\s*")) : null;
  }


  /**
   * @return the fullNameAttribute
   */
  private String getFullNameAttribute() {
    return settings.get(SonarCasProperties.FULL_NAME_ATTRIBUTE.toString()).orElse("cn");
  }

  /**
   * @return the eMailAttribute
   */
  private String getMailAttribute() {
    return settings.get(SonarCasProperties.EMAIL_ATTRIBUTE.toString()).orElse("mail");
  }

  Set<String> getGroups(Map<String,Object> attributes) {
    Set<String> groups = null;
    for ( String key : getRoleAttributes() ) {
      Collection<String> roles = getCollectionAttribute(attributes, key);
      if (roles != null) {
        if (groups == null) {
          groups = new HashSet<>();
        }
        groups.addAll(roles);
      }
    }
    return groups;
  }

  String getEmail(Map<String,Object> attributes) {
    return getStringAttribute(attributes, getMailAttribute());
  }

  String getDisplayName(Map<String,Object> attributes) {
    return getStringAttribute(attributes, getFullNameAttribute());
  }

  private Collection<String> getCollectionAttribute(Map<String,Object> attributes, String key) {
    if (attributes.containsKey(key)) {
      return (Collection<String>) attributes.get(key);
    }
    return null;
  }

  private String getStringAttribute(Map<String,Object> attributes, String key) {
    if (attributes.containsKey(key)) {
      return (String) attributes.get(key);
    }
    return null;
  }

}
