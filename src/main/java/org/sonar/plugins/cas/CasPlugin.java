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
import org.sonar.api.Plugin;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.cas.logout.CasSonarSignOutInjectorFilter;
import org.sonar.plugins.cas.util.IgnoreCert;

import java.util.ArrayList;
import java.util.List;

/**
 * As bootstrapping element, {@link CasPlugin} registers all the extensions that actually make up the "cas plugin". The main entry points to look into as a
 * developer are
 * <ul>
 * <li>{@link CasIdentifyProvider} for the browser based sso authentication</li>
 * <li>{@link CasSecurityRealm} for the username and password authentication</li>
 * </ul>
 * <p>
 * TODO apply values from the configuration, but sonarqube does not allow injection on plugin entrypoints
 *
 * @author Jan Boerner, TRIOLOGY GmbH
 * @author Sebastian Sdorra, Cloudogu GmbH
 */
public final class CasPlugin implements Plugin {


    public CasPlugin() {
    }

    public CasPlugin(Configuration configuration) {
    }

    public void define(Context context) {
        context.addExtensions(collectExtensions());
    }

    List<Object> collectExtensions() {
        List<Object> extensions = new ArrayList<>();
        if (isRealmEnabled()) {

            // Preconditions.checkState(configuration.getBoolean(SonarCasPropertyNames.SONAR_CREATE_USERS.toString()).orElse(Boolean.FALSE),
            //              "Property " + SonarCasPropertyNames.SONAR_CREATE_USERS + " must be set to true.");
            // final String protocol = configuration.get(SonarCasPropertyNames.CAS_PROTOCOL.toString()).orElse(null);

            String protocol = "cas2";

            Preconditions.checkState(!Strings.isNullOrEmpty(protocol),
                    "Missing CAS protocol. Values are: cas1, cas2 or saml11.");

            // extensions.add(CasSecurityRealm.class);


            // The ignore certification validation should only be used in development (security risk)!
            // if (configuration.getBoolean(SonarCasPropertyNames.DISABLE_CERT_VALIDATION.toString()).orElse(Boolean.FALSE)) {
            IgnoreCert.disableSslVerification();
            // }

      /*if (StringUtils.isNotBlank(configuration.get(SonarLogoutRequestFilter.PROPERTY_CAS_LOGOUT_URL).orElse(null))) {
        extensions.add(CasLogoutRequestFilter.class);
        extensions.add(SonarLogoutRequestFilter.class);
      }*/

            //if (configuration.getBoolean(SonarCasPropertyNames.FORCE_CAS_LOGIN.toString()).orElse(Boolean.FALSE)) {
            // extensions.add(ForceCasLoginFilter.class);
            // }

      /*if ("cas1".equals(protocol)) {
        extensions.add(Cas1ValidationFilter.class);
        extensions.add(Cas1AuthenticationFilter.class);
      } else if ("cas2".equals(protocol)) {
        extensions.add(Cas2ValidationFilter.class);
        extensions.add(Cas2AuthenticationFilter.class);
      } else if ("saml11".equals(protocol)) {
        extensions.add(Saml11ValidationFilter.class);
        extensions.add(Saml11AuthenticationFilter.class);
      } else {
        throw new IllegalStateException(
                String.format("Unknown CAS protocol: %s. Valid values are: cas1, cas2 or saml11.", protocol));
      }*/

            extensions.add(CasIdentifyProvider.class);
            extensions.add(CasSecurityRealm.class);

            extensions.add(ForceCasLoginFilter.class);
            extensions.add(AuthenticationFilter.class);
            extensions.add(CasAttributeSettings.class);

            extensions.add(CasSonarSignOutInjectorFilter.class);
        }

        return extensions;
    }

    private boolean isRealmEnabled() {
        //Optional<String> realmConf = configuration.get("sonar.security.realm");
        //return (realmConf.isPresent() && CasSecurityRealm.KEY.equalsIgnoreCase(realmConf.get()));
        return true;
    }

}
