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
package org.sonar.plugins.cas.logout;

import org.sonar.api.config.Configuration;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cas.util.AbstractCasFilter;

import java.util.Map;

/**
 * This filter will handle logout request coming from CAS
 *
 * @author Guillaume Lamirand
 */
public class CasLogoutRequestFilter extends AbstractCasFilter {

  public CasLogoutRequestFilter(final Configuration configuration) {
    super(configuration, new CasSonarSingleSignOutFilter());
  }

  @Override
  public UrlPattern doGetPattern() {
    return UrlPattern.create("/sessions/logout");
  }

  @Override
  protected void doCompleteProperties(final Configuration configuration, final Map<String, String> properties) {
    // Nothing to complete
  }

}
