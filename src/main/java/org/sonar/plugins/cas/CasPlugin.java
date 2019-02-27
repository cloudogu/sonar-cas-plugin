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

import org.sonar.api.Plugin;
import org.sonar.plugins.cas.logout.CasSonarSignOutInjectorFilter;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;
import org.sonar.plugins.cas.session.SessionStoreCleaner;

import java.util.ArrayList;
import java.util.List;

/**
 * As bootstrapping element, {@link CasPlugin} registers all the extensions that actually make up the "cas plugin". The main entry points to look into as a
 * developer are
 * <ul>
 * <li>{@link CasIdentityProvider} for the browser based sso authentication</li>
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
        // called by SonarQube during initializing
    }

    public void define(Context context) {
        context.addExtensions(collectExtensions());
    }

    List<Object> collectExtensions() {
        List<Object> extensions = new ArrayList<>();

        if (isRealmEnabled()) {
            extensions.add(DevelopmentServerStartHandler.class);
            extensions.add(CasAttributeSettings.class);
            extensions.add(CasTicketValidatorFactory.class);
            extensions.add(CasRestClientFactory.class);
            extensions.add(CasSessionStoreFactory.class);
            extensions.add(SessionStoreCleaner.class);


            extensions.add(CasIdentityProvider.class);
            extensions.add(CasSecurityRealm.class);

            extensions.add(ForceCasLoginFilter.class);
            extensions.add(AuthenticationFilter.class);
            extensions.add(CasTokenRefreshFilter.class);

            extensions.add(CasSonarSignOutInjectorFilter.class);
        }

        return extensions;
    }

    private boolean isRealmEnabled() {
        return true;
    }
}
