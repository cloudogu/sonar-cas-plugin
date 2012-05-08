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

import com.google.common.collect.ImmutableList;
import org.sonar.api.*;
import org.sonar.api.config.Settings;

import java.util.Collections;
import java.util.List;

public final class CasPlugin extends SonarPlugin {

  public List getExtensions() {
    return ImmutableList.of(CasExtensions.class);
  }

  public static final class CasExtensions extends ExtensionProvider implements ServerExtension {
    private Settings settings;

    public CasExtensions(Settings settings) {
      this.settings = settings;
    }

    @Override
    public Object provide() {
      if (CasSecurityRealm.KEY.equalsIgnoreCase(settings.getString("sonar.security.realm"))) {
        return ImmutableList.of(CasAuthenticationFilter.class, CasValidationFilter.class, CasSecurityRealm.class);
      }
      return Collections.emptyList();
    }
  }
}
