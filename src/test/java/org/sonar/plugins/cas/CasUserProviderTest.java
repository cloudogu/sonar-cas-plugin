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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.security.ExternalUsersProvider;
import org.sonar.api.security.UserDetails;

public class CasUserProviderTest {
  @Test
  public void should_get_username_from_cas_attribute() {
    final CasUserProvider provider = new CasUserProvider(new CasAttributeSettings(new ConfigurationBridge(new MapSettings())), null);
    final HttpServletRequest request = mock(HttpServletRequest.class);
    final Assertion casAssertion = mock(Assertion.class);
    when(casAssertion.getPrincipal()).thenReturn(new AttributePrincipalImpl("goldorak"));
    when(request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)).thenReturn(casAssertion);

    final ExternalUsersProvider.Context context = new ExternalUsersProvider.Context(null, request);
    final UserDetails user = provider.doGetUserDetails(context);

    assertThat(user.getName()).isEqualTo("goldorak");
  }

  @Test
  public void should_not_return_user_id_missing_cas_attribute() {
    final CasUserProvider provider = new CasUserProvider(null, null);
    final HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION)).thenReturn(null);

    final ExternalUsersProvider.Context context = new ExternalUsersProvider.Context(null, request);
    final UserDetails user = provider.doGetUserDetails(context);

    assertThat(user).isNull();
  }
}
