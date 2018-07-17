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
import org.sonar.api.security.Authenticator;
import org.sonar.api.security.ExternalGroupsProvider;
import org.sonar.api.security.ExternalUsersProvider;
import org.sonar.api.security.SecurityRealm;

/**
 * The {@link CasSecurityRealm} is only used for the authentication with username and password. The authentication
 * workflow consists of the following parts and is processed in the following order by the sonarqube realm
 * authenticator:
 *
 * <ol>
 * <li>{@link CasUserProvider}</li>
 * <li>{@link CasAuthenticator}</li>
 * <li>{@link CasGroupsProvider}</li>
 * </ol>
 *
 * This is needed for the non-browser clients (i. e. other plugins or CLI tools). For the browser based single sing on workflow have a look at {@link CasIdentifyProvider}.
 *
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
public class CasSecurityRealm extends SecurityRealm {

  private static final String KEY = "cas";

  private final CasUserProvider userProvider;
  private final CasAuthenticator authenticator;
  private final CasGroupsProvider groupsProvider;

  /**
   * Constructs a new {@link CasSecurityRealm}.
   * This constructor is called by dependency injection framework of sonarqube,
   *
   * @param configuration sonarqube configuration
   * @param settings cas attribute settings
   */
  public CasSecurityRealm(Configuration configuration, CasAttributeSettings settings) {
    this.userProvider = new CasUserProvider();
    this.authenticator = new CasAuthenticator(configuration, settings);
    this.groupsProvider = new CasGroupsProvider(settings);
  }

  @Override
  public Authenticator doGetAuthenticator() {
    return authenticator;
  }

  @Override
  public ExternalUsersProvider getUsersProvider() {
    return userProvider;
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
