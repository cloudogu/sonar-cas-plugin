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
import org.sonar.api.config.Settings;

import javax.servlet.FilterConfig;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SettingsFilterConfigTest {
  @Test
  public void filterConfigIsProxyOfSettings() {
    Settings settings = new Settings()
        .setProperty("sonar.cas.foo", "one")
        .setProperty("sonar.cas.bar", "two");
    SettingsFilterConfig config = new SettingsFilterConfig(mock(FilterConfig.class), settings, "sonar.cas");

    assertThat(config.getInitParameter("foo")).isEqualTo("one");
    assertThat(config.getInitParameter("bar")).isEqualTo("two");
    assertThat(config.getInitParameter("sonar.cas.foo")).isNull();
    assertThat(config.getInitParameter("other")).isNull();
  }

  @Test
  public void getParameterNames() {
    Settings settings = new Settings()
        .setProperty("sonar.cas.foo", "one")
        .setProperty("sonar.cas.bar", "two");
    SettingsFilterConfig config = new SettingsFilterConfig(mock(FilterConfig.class), settings, "sonar.cas");

    assertThat(Collections.list(config.getInitParameterNames())).containsExactly("foo", "bar");
  }

  @Test
  public void getFilterName() {
    Settings settings = new Settings();
    FilterConfig initialConfig = mock(FilterConfig.class);
    when(initialConfig.getFilterName()).thenReturn("name");
    SettingsFilterConfig config = new SettingsFilterConfig(initialConfig, settings, "sonar.cas");

    assertThat(config.getFilterName()).isEqualTo("name");
  }
}
