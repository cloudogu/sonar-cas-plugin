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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sonar.api.security.ExternalGroupsProvider;
import org.sonar.api.utils.SonarException;


/**
 * External group provider implementation for CAS group attributes.
 * It provides groups that are delivered by the CAS server for the known users.
 * @author Jan Boerner, TRIOLOGY GmbH
 */
public class CasGroupsProvider extends ExternalGroupsProvider {

  Map<String, List<String>> groupMappings;

  /**
   * Constructs the CasGroupsProvider
   * @param userGroupMapping List of known user to group mappings.
   */
  public CasGroupsProvider(final Map<String, List<String>> userGroupMapping) {
    groupMappings = userGroupMapping;
  }

  /* (non-Javadoc)
   * @see org.sonar.api.security.ExternalGroupsProvider#doGetGroups(java.lang.String)
   */
  @Override
  public Collection<String> doGetGroups(final String username) {
    final Collection<String> result = new ArrayList<String>();
    try {
      final List<String> groups = groupMappings.get(username);
      result.addAll(groups);
    } catch (final NullPointerException e) {
      throw new SonarException("Unable to retrieve groups for user " + username, e);
    }
    return result;
  }

}
