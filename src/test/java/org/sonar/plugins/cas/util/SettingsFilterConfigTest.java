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
package org.sonar.plugins.cas.util;

import com.google.common.collect.Maps;
import org.junit.Test;

import javax.servlet.FilterConfig;

import java.util.Collections;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SettingsFilterConfigTest {
  @Test
  public void filterConfigIsProxyOfSettings() {
    Map<String, String> settings = Maps.newHashMap();
    settings.put("bar", "two");
    settings.put("foo", "one");
    SettingsFilterConfig config = new SettingsFilterConfig(mock(FilterConfig.class), settings);

    assertThat(config.getInitParameter("foo")).isEqualTo("one");
    assertThat(config.getInitParameter("bar")).isEqualTo("two");
    assertThat(config.getInitParameter("other")).isNull();
  }

  @Test
  public void getParameterNames() {
    Map<String, String> settings = Maps.newHashMap();
    settings.put("bar", "two");
    settings.put("foo", "one");
    SettingsFilterConfig config = new SettingsFilterConfig(mock(FilterConfig.class), settings);

    assertThat(Collections.list(config.getInitParameterNames())).containsExactly("foo", "bar");
  }

  @Test
  public void getFilterName() {
    FilterConfig initialConfig = mock(FilterConfig.class);
    when(initialConfig.getFilterName()).thenReturn("name");
    SettingsFilterConfig config = new SettingsFilterConfig(initialConfig, Maps.<String, String>newHashMap());

    assertThat(config.getFilterName()).isEqualTo("name");
  }
}
