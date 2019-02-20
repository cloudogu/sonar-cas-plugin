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
import org.sonar.api.config.Configuration;
import org.sonar.api.web.ServletFilter;

import javax.servlet.*;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractCasFilter extends ServletFilter {
    private final Filter casFilter;
    private final Configuration configuration;

    /** called with injection by SonarQube during server initialization */
    public AbstractCasFilter(final Configuration configuration, final Filter casFilter) {
        this.configuration = configuration;
        this.casFilter = casFilter;
    }

    @Override
    public abstract UrlPattern doGetPattern();

    public final void init(final FilterConfig initialConfig) throws ServletException {
        final SettingsFilterConfig config = new SettingsFilterConfig(initialConfig, loadProperties());
        casFilter.init(config);
    }

    public final void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        casFilter.doFilter(servletRequest, servletResponse, filterChain);
    }

    public final void destroy() {
        casFilter.destroy();
    }

    /**
     * Validate and load properties.
     *
     * @return Map of properties.
     */
    protected Map<String, String> loadProperties() {
        final Map<String, String> properties = Maps.newHashMap();

        String propertySonarServerURL = SonarCasProperties.SONAR_SERVER_URL.mustGetString(configuration);

        properties.put("service", propertySonarServerURL + "/cas/validate");

        doCompleteProperties(configuration, properties);
        return properties;
    }

    protected abstract void doCompleteProperties(Configuration configuration, Map<String, String> properties);
}
