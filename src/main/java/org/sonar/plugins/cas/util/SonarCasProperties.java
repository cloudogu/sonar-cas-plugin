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

import org.sonar.api.config.Configuration;

/**
 * This class provides Sonar CAS Plugin properties in a typed way.
 *
 * @author Jan Boerner, TRIOLOGY GmbH
 */
public enum SonarCasProperties {
    /**
     * Should SONAR create new users? Must be set to TRUE.
     */
    SONAR_CREATE_USERS("sonar.authenticator.createUsers", SonarPropertyType.BOOLEAN),
    /**
     * Force CAS authentication (no anonymous access allowed)
     */
    FORCE_CAS_LOGIN("sonar.cas.forceCasLogin", SonarPropertyType.BOOLEAN),
    /**
     * cas1, cas2 or saml11
     */
    CAS_PROTOCOL("sonar.cas.protocol", SonarPropertyType.STRING),
    /**
     * Location of the CAS server login form, i.e. https://localhost:8443/cas/login
     */
    CAS_SERVER_LOGIN_URL("sonar.cas.casServerLoginUrl", SonarPropertyType.STRING),
    /**
     * CAS server root URL, i.e. https://localhost:8443/cas
     */
    CAS_SERVER_URL_PREFIX("sonar.cas.casServerUrlPrefix", SonarPropertyType.STRING),
    /**
     * Sonar server root URL, without ending slash.
     */
    SONAR_SERVER_URL("sonar.cas.sonarServerUrl", SonarPropertyType.STRING),
    /**
     * Optional CAS server logout URL. If set, sonar session will be deleted on CAS logout request.
     */
    CAS_SERVER_LOGOUT_URL("sonar.cas.casServerLogoutUrl", SonarPropertyType.STRING),
    /**
     * Specifies whether gateway=true should be sent to the CAS server. Default is false.
     */
    SEND_GATEWAY("sonar.cas.sendGateway", SonarPropertyType.BOOLEAN),
    /**
     * Attribute(s) holding the authorities (groups, roles, etc.) the user belongs to. Multiple
     * values should be separated with commas (e.g. 'groups,roles').
     */
    ROLES_ATTRIBUTE("sonar.cas.rolesAttributes", SonarPropertyType.STRING),
    /**
     * Attribute holding the user's full name.
     */
    FULL_NAME_ATTRIBUTE("sonar.cas.fullNameAttribute", SonarPropertyType.STRING),
    /**
     * Attribute holding the user's email address.
     */
    EMAIL_ATTRIBUTE("sonar.cas.eMailAttribute", SonarPropertyType.STRING),
    /**
     * The tolerance in milliseconds for drifting clocks when validating SAML 1.1 tickets.
     * Note that 10 seconds should be more than enough for most environments that have NTP time synchronization.
     * Default is 1000 milliseconds.
     */
    SAML11_TIME_TOLERANCE("sonar.cas.saml11.toleranceMilliseconds", SonarPropertyType.INTEGER),
    /**
     * Ignore certification validation errors. CAUTION! NEVER USE IN PROD! SECURITY RISK!
     */
    DISABLE_CERT_VALIDATION("sonar.cas.disableCertValidation", SonarPropertyType.BOOLEAN),
    /**
     * The expiration time of the cookie which helps to restore the originally requested SonarQube URL over the CAS authentication
     */
    URL_AFTER_CAS_REDIRECT_COOKIE_MAX_AGE_IN_SECS("sonar.cas.urlAfterCasRedirectCookieMaxAgeSeconds", SonarPropertyType.INTEGER),
    /**
     * Stores the path to the volume where session blacklists/whitelists are persistently stored. This store must be
     * persistent across server or container restarts or even container recreations in order to properly handle issued
     * authentications. Administrators may want to mount this as its own volume in order to scale with number of unexpired
     * sessions
     */
    SESSION_STORE_PATH("sonar.cas.sessionStorePath", SonarPropertyType.STRING),

    /**
     * The CAS session store stores JWT tokens which have an expiration date. These are kept for black- and whitelisting
     * JWTs from a user in order to prohibit attackers which gained access to a user's old JWT tokens.
     * Once these JWTs are expired they need to be removed from the store in a background job. This property defines the
     * interval in seconds between each clean up run. Do not set the interval too short (this could lead to unnecessary
     * CPU load) or too long (this could lead to unnecessary filesystem load).
     */
    SESSION_STORE_CLEANUP_INTERVAL_IN_SECS("sonar.cas.sessionStore.cleanUpIntervalInSeconds", SonarPropertyType.INTEGER);

    String propertyKey;

    SonarPropertyType type;

    SonarCasProperties(String propertyKey, SonarPropertyType type) {
        this.propertyKey = propertyKey;
        this.type = type;
    }

    /**
     * Returns a configuration value as string if the key was configured. Otherwise a {@link CasPropertyNotFoundException} is
     * thrown.
     *
     * @param config the SonarQube configuration object holds all configured properties
     * @return a configuration value as if the key was configured
     */
    public String mustGetString(Configuration config) {
        assertPropertyType(SonarPropertyType.STRING);

        return config.get(propertyKey).orElseThrow(() -> new CasPropertyNotFoundException(propertyKey));
    }

    /**
     * Returns a configuration value as string if the key was configured. Otherwise the given default value.
     *
     * @param config the SonarQube configuration object holds all configured properties
     * @return a configuration value as if the key was configured, otherwise the given default value.
     */
    public String getString(Configuration config, String defaultValue) {
        assertPropertyType(SonarPropertyType.STRING);

        return config.get(propertyKey).orElse(defaultValue);
    }

    /**
     * Returns a configuration value as boolean if the key was configured. Otherwise a {@link CasPropertyNotFoundException} is
     * thrown.
     *
     * @param config the SonarQube configuration object holds all configured properties
     * @return a configuration value as if the key was configured
     */
    public boolean mustGetBoolean(Configuration config) {
        assertPropertyType(SonarPropertyType.BOOLEAN);

        return config.getBoolean(propertyKey).orElseThrow(() -> new CasPropertyNotFoundException(propertyKey));
    }

    /**
     * Returns a configuration value as boolean if the key was configured. Otherwise the given default value.
     * thrown.
     *
     * @param config the SonarQube configuration object holds all configured properties
     * @return a configuration value as if the key was configured or the default value if the key was not configured.
     */
    public boolean getBoolean(Configuration config, boolean defaultValue) {
        assertPropertyType(SonarPropertyType.BOOLEAN);

        return config.getBoolean(propertyKey).orElse(defaultValue);
    }

    /**
     * Returns a configuration value as integer if the key was configured. Otherwise a {@link CasPropertyNotFoundException} is
     * thrown.
     *
     * @param config the SonarQube configuration object holds all configured properties
     * @return a configuration value as if the key was configured
     */
    public int mustGetInteger(Configuration config) {
        assertPropertyType(SonarPropertyType.INTEGER);

        return config.getInt(propertyKey).orElseThrow(() -> new CasPropertyNotFoundException(propertyKey));
    }

    /**
     * Returns a configuration value as integer if the key was configured. Otherwise the given default value.
     *
     * @param config the SonarQube configuration object holds all configured properties
     * @return a configuration value as if the key was configured or the default value if the key was not configured.
     */
    public int getInteger(Configuration config, int defaultValue) {
        assertPropertyType(SonarPropertyType.INTEGER);

        return config.getInt(propertyKey).orElse(defaultValue);
    }

    private void assertPropertyType(SonarPropertyType expectedType) {
        if (type != expectedType) {
            throw new RuntimeException("Cannot access property " + propertyKey + " as " + expectedType + ". It is of type " + type);
        }
    }

    @Override
    public String toString() {
        return propertyKey;
    }

    public enum SonarPropertyType {
        STRING,
        BOOLEAN,
        INTEGER
    }

    private static class CasPropertyNotFoundException extends RuntimeException {
        CasPropertyNotFoundException(String propertyKey) {
            super("Could not find Sonar property with key " + propertyKey + " when it was expected to be configured.");
        }
    }
}
