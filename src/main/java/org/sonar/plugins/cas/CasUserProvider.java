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

import org.sonar.api.security.Authenticator;
import org.sonar.api.security.ExternalGroupsProvider;
import org.sonar.api.security.ExternalUsersProvider;
import org.sonar.api.security.UserDetails;

/**
 * This provider returns only an empty user object, because of the realm authentication order of sonar:
 *
 * - {@link ExternalUsersProvider#doGetUserDetails}
 * - {@link org.sonar.api.security.Authenticator#doAuthenticate(Authenticator.Context)}}
 * - {@link org.sonar.api.security.ExternalGroupsProvider#doGetGroups(ExternalGroupsProvider.Context)}
 *
 * We are not able to authenticate the user in the {@link ExternalUsersProvider}, because of the missing password for
 * the user. Without the authentication we are not able to fetch the assertions of the user, so we return an empty
 * {@link UserDetails} object and we also store it in the request. The {@link UserDetails} object will be later filled
 * by the {@link CasAuthenticator}.
 *
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
public class CasUserProvider extends ExternalUsersProvider {

  @Override
  public UserDetails doGetUserDetails(final Context context) {
    // add empty UserDetails object to request, the UserDetails are filled by the CasAuthenticator
    UserDetails user = new UserDetails();
    context.getRequest().setAttribute(UserDetails.class.getName(), user);
    return user;
  }
}
