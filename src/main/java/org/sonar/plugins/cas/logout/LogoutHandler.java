package org.sonar.plugins.cas.logout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.cas.CasSessionStore;
import org.sonar.plugins.cas.util.JwtProcessor;
import org.sonar.plugins.cas.util.SimpleJwt;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;

public class LogoutHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LogoutHandler.class);

    public void logout(String logoutRequestRaw) {
        LogoutRequest logoutRequest = JAXB.unmarshal(new StringReader(logoutRequestRaw), LogoutRequest.class);
        CasSessionStore.invalidateJwt(logoutRequest.sessionId);
    }

    public void invalidateLoginCookiesIfNecessary(HttpServletRequest request, HttpServletResponse response) {
        boolean removeJwtCookie = shouldLogoutUser(request.getCookies());

        if (removeJwtCookie) {
            LOG.debug("User authentication cookies will be removed because an invalid JWT token was found.");
            removeAuthCookies(response);
        }
    }

    private boolean shouldLogoutUser(Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            return false;
        }

        SimpleJwt jwt = JwtProcessor.getJwtTokenFromCookies(cookies);
        boolean isStored = CasSessionStore.isJwtStored(jwt);
        if (!isStored) {
            return false;
        }

        SimpleJwt storedJwt = CasSessionStore.getJwtById(jwt);
        return storedJwt.isInvalid();
    }

    private void removeAuthCookies(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT-SESSION", "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        cookie = new Cookie("XSRF-TOKEN", "");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "LogoutRequest", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
    private static class LogoutRequest {

        @XmlElement(name = "SessionIndex", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
        private String sessionId;

    }
}
