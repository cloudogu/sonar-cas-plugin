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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * This class provides Sonar CAS Plugin properties in a typed way..
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
    SESSION_STORE_PATH("sonar.cas.sessionStorePath", SonarPropertyType.STRING);

    private static final Logger LOG = LoggerFactory.getLogger(SonarCasProperties.class);

    String propertyKey;

    SonarPropertyType type;

    private static final Properties LOADED_PROPERTIES;

    static {
        String pathToPropertyFile = System.getProperty("SONAR_CAS_PLUGIN_PROPERTY_FILE");
        if (pathToPropertyFile == null) {
            throw new IllegalStateException("Could not load CAS plugin property file. This file is vital for the CAS" +
                    "plugin. Please provide a system property SONAR_CAS_PLUGIN_PROPERTY_FILE which points to a " +
                    "property file just like this: '/opt/sonarqube/conf/sonar.properties'");
        }
        LOADED_PROPERTIES = new Properties();
        try {
            LOADED_PROPERTIES.load(new FileReader(pathToPropertyFile));
        } catch (IOException e) {
            LOG.error("Could not initialize CAS properties because an error occurred.", e);
        }
    }

    SonarCasProperties(String propertyKey, SonarPropertyType type) {
        this.propertyKey = propertyKey;
        this.type = type;
    }

    public String getStringProperty() {
        assertPropertyType(SonarPropertyType.STRING);

        String property = LOADED_PROPERTIES.getProperty(propertyKey);
        logPropertyNotFound(property);
        return property;
    }

    public boolean getBooleanProperty() {
        assertPropertyType(SonarPropertyType.BOOLEAN);

        String property = LOADED_PROPERTIES.getProperty(propertyKey);
        logPropertyNotFound(property);

        return Boolean.valueOf(property);
    }

    public int getIntegerProperty() {
        assertPropertyType(SonarPropertyType.INTEGER);
        String property = LOADED_PROPERTIES.getProperty(propertyKey);
        logPropertyNotFound(property);

        return Integer.valueOf(property);
    }

    private void logPropertyNotFound(String property) {
        if (property == null) {
            LOG.error("Could not find necessary property {} for CAS configuration", propertyKey);
        }
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
}
