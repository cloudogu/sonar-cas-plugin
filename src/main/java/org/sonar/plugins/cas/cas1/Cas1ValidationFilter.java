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
package org.sonar.plugins.cas.cas1;

import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.validation.Cas10TicketValidationFilter;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cas.util.AbstractCasFilter;

import java.util.Map;

/**
 * This filter is used to manage CAS 1 protocol validation
 *
 * @author Guillaume Lamirand
 */
public final class Cas1ValidationFilter extends AbstractCasFilter {

  public Cas1ValidationFilter(Settings settings) {
    super(settings, new Cas10TicketValidationFilter());
  }

  @Override
  public UrlPattern doGetPattern() {
    return UrlPattern.create("/cas/validate");
  }

  @Override
  protected void doCompleteProperties(Settings settings, Map<String, String> properties) {
    properties.put("casServerUrlPrefix", settings.getString("sonar.cas.casServerUrlPrefix"));
    properties.put("gateway", StringUtils.defaultIfBlank(settings.getString("sonar.cas.sendGateway"), "false"));
    properties.put("redirectAfterValidation", "false");
    properties.put("useSession", "true");
    properties.put("exceptionOnValidationFailure", "true");
  }

}
