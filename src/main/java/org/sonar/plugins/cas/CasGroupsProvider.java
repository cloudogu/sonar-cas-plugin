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

import com.google.common.base.Preconditions;
import org.jasig.cas.client.validation.Assertion;
import org.sonar.api.security.ExternalGroupsProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * The groups provider is the last step in the authentication chain for username and password based CAS authentication.
 * The provider will consume the CAS assertion from the {@link HttpServletRequest} and extract all role and group attribute values.
 * <p/>
 * The assertion has been added to the request by the {@link CasAuthenticator}, which is called before the groups provider.
 *
 * @author Jan Boerner, TRIOLOGY GmbH
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
class CasGroupsProvider extends ExternalGroupsProvider {

  private final CasAttributeSettings settings;

  CasGroupsProvider(CasAttributeSettings settings) {
    this.settings = settings;
  }

  @Override
  public Collection<String> doGetGroups(Context context) {
    Assertion assertion = (Assertion) context.getRequest().getAttribute(Assertion.class.getName());
    Preconditions.checkState(assertion != null, "could not find assertions in the request");

    return settings.getGroups(assertion.getPrincipal().getAttributes());
  }
}
