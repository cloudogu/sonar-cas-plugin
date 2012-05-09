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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.api.web.ServletFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AbstractCasFilterTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void is_proxy() throws Exception {
    Filter target = mock(Filter.class);
    AbstractCasFilter filter = new FakeFilter(new Settings(), target);
    filter.doFilter(mock(HttpServletRequest.class), mock(HttpServletResponse.class), mock(FilterChain.class));
    verify(target).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));

    filter.destroy();
    verify(target).destroy();
  }

  @Test
  public void sonar_url_is_mandatory() throws Exception {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Missing property");

    Filter target = mock(Filter.class);
    AbstractCasFilter filter = new FakeFilter(new Settings(), target);
    filter.init(mock(FilterConfig.class));
  }

  @Test
  public void sonar_url_must_not_end_with_slash() throws Exception {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("must not end with slash");

    Filter target = mock(Filter.class);
    Settings settings = new Settings().setProperty("sonar.cas.sonarServerUrl", "http://foo/");
    AbstractCasFilter filter = new FakeFilter(settings, target);
    filter.init(mock(FilterConfig.class));
  }

  @Test
  public void init_cas_service() throws Exception {
    Settings settings = new Settings().setProperty("sonar.cas.sonarServerUrl", "http://localhost:9000");
    AbstractCasFilter filter = new FakeFilter(settings, mock(Filter.class));

    Map<String, String> properties = filter.loadProperties();

    assertThat(properties.get("service")).isEqualTo("http://localhost:9000/cas/validate");
  }

  private static class FakeFilter extends AbstractCasFilter {
    FakeFilter(Settings settings, Filter casFilter) {
      super(settings, casFilter);
    }

    @Override
    public ServletFilter.UrlPattern doGetPattern() {
      return null;
    }

    @Override
    protected void doCompleteProperties(Settings settings, Map<String, String> properties) {
    }
  }
}
