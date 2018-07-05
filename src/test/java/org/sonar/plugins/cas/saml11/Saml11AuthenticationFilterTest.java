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
package org.sonar.plugins.cas.saml11;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

public class Saml11AuthenticationFilterTest {
  @Test
  public void should_declare_pattern() {
    Saml11AuthenticationFilter filter = new Saml11AuthenticationFilter(new ConfigurationBridge(new MapSettings()));

    assertThat(filter.doGetPattern().getUrl()).isEqualTo("/sessions/new/*");
  }

  @Test
  public void should_create_cas_filter() throws Exception {
    final Configuration configuration = new ConfigurationBridge(new MapSettings()
      .setProperty("sonar.cas.sonarServerUrl", "http://localhost:9000")
      .setProperty("sonar.cas.casServerLoginUrl", "http://localhost:8080/cas/login")
      .setProperty("sonar.cas.casServerUrlPrefix", "http://localhost:8080/cas")
    );

    Saml11AuthenticationFilter filter = new Saml11AuthenticationFilter(configuration);
    filter.init(mock(FilterConfig.class, withSettings().defaultAnswer(Mockito.RETURNS_DEEP_STUBS)));

    assertThat(filter.getCasFilter()).isInstanceOf(org.jasig.cas.client.authentication.Saml11AuthenticationFilter.class);
  }

  @Test
  public void should_init_cas_filter_with_default_values() throws Exception {
    final Configuration configuration = new ConfigurationBridge(new MapSettings()
      .setProperty("sonar.cas.sonarServerUrl", "http://localhost:9000")
    );

    Filter casFilter = mock(Filter.class);
    Saml11AuthenticationFilter filter = new Saml11AuthenticationFilter(configuration, casFilter);

    filter.init(mock(FilterConfig.class));

    verify(casFilter).init(argThat(new BaseMatcher<FilterConfig>() {
      public boolean matches(Object o) {
        FilterConfig config = (FilterConfig) o;
        return verifyParam(config, "gateway", "false") &&
          verifyParam(config, "service", "http://localhost:9000/cas/validate") &&
          verifyParam(config, "casServerLoginUrl", null);
      }

      public void describeTo(Description description) {
      }
    }));
  }

  @Test
  public void should_init_cas_filter_with_settings() throws Exception {
    final Configuration configuration = new ConfigurationBridge(new MapSettings()
      .setProperty("sonar.cas.sonarServerUrl", "http://localhost:9000")
      .setProperty("sonar.cas.casServerLoginUrl", "http://localhost:8080/cas/login")
      .setProperty("sonar.cas.sendGateway", "true")
    );

    Filter casFilter = mock(Filter.class);
    Saml11AuthenticationFilter filter = new Saml11AuthenticationFilter(configuration, casFilter);

    filter.init(mock(FilterConfig.class));

    verify(casFilter).init(argThat(new BaseMatcher<FilterConfig>() {
      public boolean matches(Object o) {
        FilterConfig config = (FilterConfig) o;
        return verifyParam(config, "gateway", "true") &&
          verifyParam(config, "service", "http://localhost:9000/cas/validate") &&
          verifyParam(config, "casServerLoginUrl", "http://localhost:8080/cas/login");
      }

      public void describeTo(Description description) {
      }
    }));
  }

  private boolean verifyParam(FilterConfig config, String key, String value) {
    String param = config.getInitParameter(key);
    boolean ok = StringUtils.equals(value, param);
    if (!ok) {
      System.out.println("Parameter " + key + " has bad value: " + param);
    }

    return ok;
  }
}
