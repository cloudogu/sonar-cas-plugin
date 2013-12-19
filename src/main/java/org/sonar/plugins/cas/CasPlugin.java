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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.sonar.api.ExtensionProvider;
import org.sonar.api.ServerExtension;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.Settings;
import org.sonar.plugins.cas.cas1.Cas1AuthenticationFilter;
import org.sonar.plugins.cas.cas1.Cas1ValidationFilter;
import org.sonar.plugins.cas.cas2.Cas2AuthenticationFilter;
import org.sonar.plugins.cas.cas2.Cas2ValidationFilter;
import org.sonar.plugins.cas.logout.CasLogoutRequestFilter;
import org.sonar.plugins.cas.logout.SonarLogoutRequestFilter;
import org.sonar.plugins.cas.saml11.Saml11AuthenticationFilter;
import org.sonar.plugins.cas.saml11.Saml11ValidationFilter;
import org.sonar.plugins.cas.util.IgnoreCert;

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
			List<Class> extensions = Lists.newArrayList();
			if (isRealmEnabled()) {
				Preconditions.checkState(settings.getBoolean("sonar.authenticator.createUsers"),
					"Property sonar.authenticator.createUsers must be set to true.");
				String protocol = settings.getString("sonar.cas.protocol");
				Preconditions.checkState(!Strings.isNullOrEmpty(protocol),
					"Missing CAS protocol. Values are: cas1, cas2 or saml11.");

				extensions.add(CasSecurityRealm.class);
				
				// The ignore certification validation should only be used in development (security risk)!
				if (settings.getBoolean("sonar.cas.disableCertValidation")) {
				  IgnoreCert.disableSslVerification();
				}

				if (StringUtils.isNotBlank(settings.getString(SonarLogoutRequestFilter.PROPERTY_CAS_LOGOUT_URL))) {
					extensions.add(CasLogoutRequestFilter.class);
					extensions.add(SonarLogoutRequestFilter.class);
				}
				
				if (settings.getBoolean("sonar.cas.forceCasLogin")) {
				  extensions.add(ForceCasLoginFilter.class);
				}

				if ("cas1".equals(protocol)) {
					extensions.add(Cas1AuthenticationFilter.class);
					extensions.add(Cas1ValidationFilter.class);
				} else if ("cas2".equals(protocol)) {
					extensions.add(Cas2AuthenticationFilter.class);
					extensions.add(Cas2ValidationFilter.class);
				} else if ("saml11".equals(protocol)) {
					extensions.add(Saml11AuthenticationFilter.class);
					extensions.add(Saml11ValidationFilter.class);
				} else {
					throw new IllegalStateException(
					    String.format("Unknown CAS protocol: %s. Valid values are: cas1, cas2 or saml11.", protocol));
				}

			}
			return extensions;
		}

		private boolean isRealmEnabled() {
			return CasSecurityRealm.KEY.equalsIgnoreCase(settings.getString("sonar.security.realm"));
		}
	}
}
