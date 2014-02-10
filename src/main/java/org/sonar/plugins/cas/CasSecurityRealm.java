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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.security.Authenticator;
import org.sonar.api.security.ExternalGroupsProvider;
import org.sonar.api.security.ExternalUsersProvider;
import org.sonar.api.security.SecurityRealm;

public class CasSecurityRealm extends SecurityRealm {

  public static final String KEY = "cas";
  /** Provider for user groups that are delivered by the CAS attributes. */
  private ExternalGroupsProvider groupsProvider = null;
  private Map<String, List<String>> userGroupMapping;
  private final CasAttributeSettings settings;

  public CasSecurityRealm(final CasAttributeSettings settings) {
    this.settings = settings;
  }

  @Override
  public void init() {
    if (null != settings.getRoleAttributes()) {
      userGroupMapping = new HashMap<String, List<String>>();
      groupsProvider = new CasGroupsProvider(userGroupMapping);
    }
  }

  @Override
  public Authenticator doGetAuthenticator() {
    return new CasAuthenticator();
  }

  @Override
  public ExternalUsersProvider getUsersProvider() {
    return new CasUserProvider(settings, userGroupMapping);
  }

  @Override
  public ExternalGroupsProvider getGroupsProvider() {
    return groupsProvider;
  }

  @Override
  public String getName() {
    return KEY;
  }
}
