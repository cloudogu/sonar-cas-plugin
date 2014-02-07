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


/**
 * Collection of all known Sonar CAS Plugin property names.
 * @author Jan Boerner, TRIOLOGY GmbH
 */
public enum SonarCasPropertyNames {
  /** Should SONAR create new users? Must be set to TRUE. */
  SONAR_CREATE_USERS("sonar.authenticator.createUsers"),
  /** Force CAS authentication (no anonymous access allowed) */
  FORCE_CAS_LOGIN("sonar.cas.forceCasLogin"),
  /** cas1, cas2 or saml11 */
  CAS_PROTOCOL("sonar.cas.protocol"),
  /** Location of the CAS server login form, i.e. https://localhost:8443/cas/login */
  CAS_SERVER_LOGIN_URL("sonar.cas.casServerLoginUrl"),
  /** CAS server root URL, i.e. https://localhost:8443/cas */
  CAS_SERVER_URL_PREFIX("sonar.cas.casServerUrlPrefix"),
  /** Sonar server root URL, without ending slash. */
  SONAR_SERVER_URL("sonar.cas.sonarServerUrl"),
  /** Optional CAS server logout URL. If set, sonar session will be deleted on CAS logout request. */
  CAS_SERVER_LOGOUT_URL("sonar.cas.casServerLogoutUrl"),
  /** Specifies whether gateway=true should be sent to the CAS server. Default is false. */
  SEND_GATEWAY("sonar.cas.sendGateway"),
  /**
   * Attribute(s) holding the authorities (groups, roles, etc.) the user belongs to. Multiple
   * values should be separated with commas (e.g. 'groups,roles').
   */
  ROLES_ATTRIBUTE("sonar.cas.rolesAttributes"),
  /** Attribute holding the user's full name. */
  FULL_NAME_ATTRIBUTE("sonar.cas.fullNameAttribute"),
  /** Attribute holding the user's email address. */
  EMAIL_ATTRIBUTE("sonar.cas.eMailAttribute"),
  /**
   * The tolerance in milliseconds for drifting clocks when validating SAML 1.1 tickets.
   * Note that 10 seconds should be more than enough for most environments that have NTP time synchronization.
   * Default is 1000 milliseconds.
   */
  SAML11_TIME_TOLERANCE("sonar.cas.saml11.toleranceMilliseconds"),
  /** Ignore certification validation errors. CAUTION! NEVER USE IN PROD! SECURITY RISK! */
  DISABLE_CERT_VALIDATION("sonar.cas.disableCertValidation");

  String name;

  SonarCasPropertyNames(final String pName) {
    name = pName;
  }

  @Override
  public String toString() {
    return name;
  }
}
