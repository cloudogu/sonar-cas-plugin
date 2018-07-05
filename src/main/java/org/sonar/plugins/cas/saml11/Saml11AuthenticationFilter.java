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

import com.google.common.annotations.VisibleForTesting;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.cas.util.AbstractCasFilter;
import org.sonar.plugins.cas.util.SonarCasPropertyNames;

import javax.servlet.Filter;
import java.util.Map;

public final class Saml11AuthenticationFilter extends AbstractCasFilter {

  public Saml11AuthenticationFilter(final Configuration configuration) {
    this(configuration, new org.jasig.cas.client.authentication.Saml11AuthenticationFilter());
  }

  @VisibleForTesting
  Saml11AuthenticationFilter(final Configuration configuration, final Filter casFilter) {
    super(configuration, casFilter);
  }

  @Override
  public UrlPattern doGetPattern() {
    return UrlPattern.create("/sessions/new/*");
  }

  @Override
  protected void doCompleteProperties(final Configuration configuration, final Map<String, String> properties) {
      properties.put("casServerLoginUrl", configuration.get("sonar.cas.casServerLoginUrl").orElse(null));
    properties.put("gateway", configuration.get(SonarCasPropertyNames.SEND_GATEWAY.toString()).orElse("false"));
  }

}