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

import org.junit.Test;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class CasPluginTest {
  @Test
  public void enable_extensions_if_cas_realm_is_enabled() {
    Settings settings = new Settings().setProperty("sonar.security.realm", "CAS");
    List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).hasSize(3);
    assertThat(extensions).doesNotHaveDuplicates();
  }

  @Test
  public void disable_extensions_if_default_realm() {
    Settings settings = new Settings();
    List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).isEmpty();
  }

  @Test
  public void disable_extensions_if_cas_realm_is_disabled() {
    Settings settings = new Settings().setProperty("sonar.security.realm", "LDAP");
    List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).isEmpty();
  }

  @Test
  public void getExtensions() {
    assertThat(new CasPlugin().getExtensions()).containsExactly(CasPlugin.CasExtensions.class);
  }
}
