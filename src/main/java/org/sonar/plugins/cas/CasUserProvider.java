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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.sonar.api.security.ExternalUsersProvider;
import org.sonar.api.security.UserDetails;

public class CasUserProvider extends ExternalUsersProvider {
  private final Map<String, List<String>> groupMappings;
  private final CasAttributeSettings settings;

  public CasUserProvider(final CasAttributeSettings settings, final Map<String, List<String>> userGroupMapping) {
    groupMappings = userGroupMapping;
    this.settings = settings;
  }

  @Override
  public UserDetails doGetUserDetails(final Context context) {
    UserDetails user = null;
    final Assertion assertion = (Assertion) context.getRequest().getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
    if (assertion != null && assertion.getPrincipal() != null) {
      user = new UserDetails();
      final Map<String,Object> attributes = assertion.getPrincipal().getAttributes();
      if (null != attributes && null != attributes.get(settings.getFullNameAttribute())) {
        user.setName((String) attributes.get(settings.getFullNameAttribute()));
      } else {
        user.setName(assertion.getPrincipal().getName());
      }
      if (null != attributes) {
        if (null != attributes.get(settings.geteMailAttribute())) {
          user.setEmail((String) attributes.get(settings.geteMailAttribute()));
        }
        if (null != settings.getRoleAttributes()) {
          final List<String> groups = new ArrayList<String>();
          for (final String role : settings.getRoleAttributes()) {
            final Object o = attributes.get(role);
            if (o instanceof String) {
              groups.add((String) o);
            } else if (o instanceof List) {
              for (final Object g : (List<?>) o) {
                if (g instanceof String) {
                  groups.add((String) g);
                }
              }
            }
          }
          groupMappings.put(user.getName(), groups);
        }
      }
    }
    return user;
  }
}
