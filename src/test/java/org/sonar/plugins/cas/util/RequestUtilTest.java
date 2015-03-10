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

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class RequestUtilTest {


  @Test
  public void testGetBasicAuthentication() throws UnsupportedEncodingException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Basic d2lraTpwZWRpYQ==");
    
    Credentials creds = RequestUtil.getBasicAuthentication(request);
    assertNotNull(creds);
    assertEquals(creds.getUsername(), "wiki");
    assertEquals(creds.getPassword(), "pedia");
  }
  
  @Test
  public void testGetBasicAuthenticationWithoutAuthorization() throws UnsupportedEncodingException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    
    Credentials creds = RequestUtil.getBasicAuthentication(request);
    assertNull(creds);
  }
  
  @Test
  public void testGetBasicAuthenticationWithoutBasic() throws UnsupportedEncodingException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("Authorization")).thenReturn("Other d2lraTpwZWRpYQ==");
    
    Credentials creds = RequestUtil.getBasicAuthentication(request);
    assertNull(creds);
  }

}