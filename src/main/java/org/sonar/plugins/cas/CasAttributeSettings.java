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



/**
 * Singleton object holding the attribute mappings.
 * @author Jan Boerner, TRIOLOGY GmbH
 */
public class CasAttributeSettings {
  /**   The singleton instance. */
  private static CasAttributeSettings INSTANCE;
  private String roleAttributes = "groups,roles";
  private String fullNameAttribute = "cn";
  private String eMailAttribute = "mail";

  /** Constructor. */
  private CasAttributeSettings() {   }

  static {
    INSTANCE = new CasAttributeSettings();
  }

  /**
   * @return Singleton-Instance-Getter.
   */
  public static CasAttributeSettings getInstance() {
    if(INSTANCE == null) {
      throw new AssertionError("INSTANCE == null");
    }
    return INSTANCE;
  }


  /**
   * @return the roleAttributes
   */
  public String getRoleAttributes() {
    return roleAttributes;
  }


  /**
   * @param roleAttributes the roleAttributes to set
   */
  public void setRoleAttributes(final String roleAttributes) {
    this.roleAttributes = roleAttributes;
  }


  /**
   * @return the fullNameAttribute
   */
  public String getFullNameAttribute() {
    return fullNameAttribute;
  }

  /**
   * @param fullNameAttribute the fullNameAttribute to set
   */
  public void setFullNameAttribute(final String fullNameAttribute) {
    this.fullNameAttribute = fullNameAttribute;
  }


  /**
   * @return the eMailAttribute
   */
  public String geteMailAttribute() {
    return eMailAttribute;
  }


  /**
   * @param eMailAttribute the eMailAttribute to set
   */
  public void seteMailAttribute(final String eMailAttribute) {
    this.eMailAttribute = eMailAttribute;
  }


}
