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

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.ServerExtension;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cas.cas1.Cas1AuthenticationFilter;
import org.sonar.plugins.cas.cas2.Cas2AuthenticationFilter;
import org.sonar.plugins.cas.logout.CasLogoutRequestFilter;
import org.sonar.plugins.cas.logout.SonarLogoutRequestFilter;
import org.sonar.plugins.cas.saml11.Saml11AuthenticationFilter;

public class CasPluginTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void enable_extensions_if_cas_realm_is_enabled() {
    final Settings settings = new Settings()
    .setProperty("sonar.security.realm", "cas")
    .setProperty("sonar.authenticator.createUsers", "true")
    .setProperty("sonar.cas.protocol", "cas2");
    final List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).hasSize(4);
    assertThat(extensions).doesNotHaveDuplicates();
    assertThat(extensions).contains(Cas2AuthenticationFilter.class);
  }

  @Test
  public void enable_cas1_extensions() {
    final Settings settings = new Settings()
    .setProperty("sonar.security.realm", "cas")
    .setProperty("sonar.authenticator.createUsers", "true")
    .setProperty("sonar.cas.protocol", "cas1");
    final List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).hasSize(4);
    assertThat(extensions).doesNotHaveDuplicates();
    assertThat(extensions).contains(Cas1AuthenticationFilter.class);
  }

  @Test
  public void enable_saml11_extensions() {
    final Settings settings = new Settings()
    .setProperty("sonar.security.realm", "cas")
    .setProperty("sonar.authenticator.createUsers", "true")
    .setProperty("sonar.cas.protocol", "saml11");
    final List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).hasSize(4);
    assertThat(extensions).doesNotHaveDuplicates();
    assertThat(extensions).contains(Saml11AuthenticationFilter.class);
  }

  @Test
  public void fail_if_unknown_protocol() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Unknown CAS protocol");

    final Settings settings = new Settings()
    .setProperty("sonar.security.realm", "cas")
    .setProperty("sonar.authenticator.createUsers", "true")
    .setProperty("sonar.cas.protocol", "other");
    new CasPlugin.CasExtensions(settings).provide();
  }

  @Test
  public void property_createUsers_must_be_true() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Property sonar.authenticator.createUsers must be set to true");

    final Settings settings = new Settings()
    .setProperty("sonar.security.realm", "cas")
    .setProperty("sonar.authenticator.createUsers", "false")
    .setProperty("sonar.cas.protocol", "saml11");

    new CasPlugin.CasExtensions(settings).provide();
  }

  @Test
  public void fail_if_missing_protocol() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Missing CAS protocol");

    final Settings settings = new Settings()
    .setProperty("sonar.security.realm", "cas")
    .setProperty("sonar.authenticator.createUsers", "true");
    new CasPlugin.CasExtensions(settings).provide();
  }

  @Test
  public void disable_extensions_if_default_realm() {
    final Settings settings = new Settings();
    final List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).isEmpty();
  }

  @Test
  public void disable_extensions_if_cas_realm_is_disabled() {
    final Settings settings = new Settings().setProperty("sonar.security.realm", "LDAP");
    final List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).isEmpty();
  }

  @Test
  public void getExtensions() {
    assertThat(new CasPlugin().getExtensions()).containsExactly(CasPlugin.CasExtensions.class);
  }

  @Test
  public void should_disable_logout_by_default() {
    final Settings settings = new Settings()
    .setProperty("sonar.security.realm", "cas")
    .setProperty("sonar.authenticator.createUsers", "true")
    .setProperty("sonar.cas.protocol", "saml11");
    final List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).excludes(CasLogoutRequestFilter.class);
    assertThat(extensions).excludes(SonarLogoutRequestFilter.class);
  }

  @Test
  public void should_enable_logout_by_default() {
    final Settings settings = new Settings()
    .setProperty("sonar.security.realm", "cas")
    .setProperty("sonar.authenticator.createUsers", "true")
    .setProperty(SonarLogoutRequestFilter.PROPERTY_CAS_LOGOUT_URL, "http://localhost:8080/cas/logout")
    .setProperty("sonar.cas.protocol", "saml11");
    final List<ServerExtension> extensions = (List<ServerExtension>) new CasPlugin.CasExtensions(settings).provide();

    assertThat(extensions).contains(CasLogoutRequestFilter.class, SonarLogoutRequestFilter.class);
  }
}
