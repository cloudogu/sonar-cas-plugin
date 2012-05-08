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

import com.google.common.base.Strings;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;
import org.sonar.api.security.Authenticator;

public class CasAuthenticator extends Authenticator {

  @Override
  public boolean doAuthenticate(Context context) {
    Assertion assertion = (Assertion) context.getRequest().getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION);
    System.out.println("CasAuthenticator - doAuthenticate: " + assertion);
    return assertion != null && assertion.getPrincipal() != null && !Strings.isNullOrEmpty(assertion.getPrincipal().getName());
  }

}
