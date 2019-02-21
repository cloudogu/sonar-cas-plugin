package org.sonar.plugins.cas.logout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.util.CookieUtil;
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
import java.io.IOException;
import java.io.StringReader;

public class LogoutHandler {
    public static final String JWT_SESSION_COOKIE = "JWT-SESSION";
    private static final Logger LOG = LoggerFactory.getLogger(LogoutHandler.class);
    private final CasSessionStore casSessionStore;

    public LogoutHandler(CasSessionStore casSessionStore) {
        this.casSessionStore = casSessionStore;
    }

    public void logout(String logoutRequestRaw) {
        LogoutRequest logoutRequest = JAXB.unmarshal(new StringReader(logoutRequestRaw), LogoutRequest.class);
        String jwtId = casSessionStore.invalidateJwt(logoutRequest.sessionId);

        LOG.debug("Invalidate JWT {} with Service Ticket {}", jwtId, logoutRequest.sessionId);
    }

    /**
     * Cleans up user's cookies and redirects to the log-in page when a blacklisted JWT token was found. If no JWT or
     * an valid JWT was found this method leaves the request and response as-is.
     * <p>
     * A user's cookies must be reset if and only if an invalid JWT token was found in the cookies. Otherwise it would
     * remove logged-in users authentication.
     *
     * @param request  the HTTP request is inspected for cookies and the context path in case of a redirect.
     * @param response the HTTP response that is going to be modified with delete-cookies if an invalid JWT cookie was
     *                 found. Also the a redirect to the log-in page is added.
     * @return <code>true</code> if the filtering mechanism should be aborted, otherwise false.
     * @throws IOException this exception can occur during modification of the response.
     */
    public boolean handleInvalidJwtCookie(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean shouldUserBeLoggedOut = shouldUserBeLoggedOut(request.getCookies());

        if (shouldUserBeLoggedOut) {
            LOG.debug("User authentication cookies will be removed because an invalid JWT token was found");
            // Security advice:
            // Do NOT remove the user's token from the session store. It must stay blacklisted until it is removed
            // during the expiration date check.
            removeAuthCookies(response);
            redirectToLogin(request, response);
        }

        return shouldUserBeLoggedOut;
    }

    /**
     * Checks for an invalid (blacklisted) JWT cookie.
     *
     * @param cookies all sent cookies that are supposed to be inspected
     * @return <code>true</code> if a JWT token was found in the cookies which is also invalid, otherwise false.
     */
    private boolean shouldUserBeLoggedOut(Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            return false;
        }

        SimpleJwt jwt = JwtProcessor.getJwtTokenFromCookies(cookies);

        boolean isStored = casSessionStore.isJwtStored(jwt);
        if (!isStored) {
            return false;
        }

        SimpleJwt storedJwt = casSessionStore.getJwtById(jwt);
        LOG.debug("Is the found JWT token {} invalid? {}", jwt.getJwtId(), storedJwt.isInvalid());

        return storedJwt.isInvalid();
    }

    private void removeAuthCookies(HttpServletResponse response) {
        Cookie jwtCookie = CookieUtil.createDeletionCookie(JWT_SESSION_COOKIE);
        response.addCookie(jwtCookie);

        Cookie xsrfCookie = CookieUtil.createDeletionCookie("XSRF-TOKEN");
        response.addCookie(xsrfCookie);
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String redirectToLoginUrl = "/sessions/new";
        LOG.debug("Found unauthenticated request to {}. Redirecting to {}", request.getRequestURL(), redirectToLoginUrl);

        response.sendRedirect(request.getContextPath() + redirectToLoginUrl);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "LogoutRequest", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
    private static class LogoutRequest {

        @XmlElement(name = "SessionIndex", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
        private String sessionId;
    }
}
