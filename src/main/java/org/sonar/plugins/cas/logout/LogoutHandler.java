package org.sonar.plugins.cas.logout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.http.Cookie;
import org.sonar.api.server.http.HttpRequest;
import org.sonar.api.server.http.HttpResponse;
import org.sonar.plugins.cas.session.CasSessionStore;
import org.sonar.plugins.cas.session.CasSessionStoreFactory;
import org.sonar.plugins.cas.util.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.sonar.plugins.cas.util.Cookies.JWT_SESSION_COOKIE;

/**
 * This class handles log-out related actions like checking for valid JWT cookies and invalidating JWT during logout.
 */
@ServerSide
public class LogoutHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LogoutHandler.class);

    private final Configuration configuration;
    private final CasSessionStore casSessionStore;

    public LogoutHandler(Configuration configuration, CasSessionStoreFactory casSessionStoreFactory) {
        this.configuration = configuration;
        this.casSessionStore = casSessionStoreFactory.getInstance();
    }

    public void logout(HttpRequest request, HttpResponse response) throws IOException, ParserConfigurationException, SAXException {
        String logoutAttributes = request.getParameter("logoutRequest");

        try (InputStream inputStream = new ByteArrayInputStream(logoutAttributes.getBytes())) {
            Element root = XMLParsing.getRootElementFromXML(inputStream);

            String sessionId = XMLParsing.getContentForTagName(root,"samlp:SessionIndex");

            LogoutRequest unmarshalled = new LogoutRequest();
            unmarshalled.sessionId = sessionId;

            String jwtId = casSessionStore.invalidateJwt(unmarshalled.sessionId);
            LOG.debug("Invalidate JWT {} with Service Ticket {}", jwtId, unmarshalled.sessionId);

            response.sendRedirect(getSonarServiceUrl());
        }
    }

    /**
     * Checks for a blacklisted JWT cookie and the requested URL
     *
     * @param request the request to check for user cookies
     * @return true if the user contains a blacklisted JWT cookie AND requests a page other than the login-page
     */
    public boolean isUserLoggedOutAndLogsInAgain(HttpRequest request) {
        boolean shouldUserBeLoggedOut = shouldUserBeLoggedOut(request.getCookies());
        boolean requestToLoginPage = isRequestToLoginPage(request);
        if (requestToLoginPage) {
            LOG.debug("User is already being redirected to the log-in page. Will do nothing.");
        }

        return shouldUserBeLoggedOut && !requestToLoginPage;
    }

    /**
     * Cleans up user's authentication cookies if the user accesses a non-login resource.
     * <p>
     * A user's cookies must be reset if and only if an invalid JWT token was found in the cookies. Otherwise it would
     * remove logged-in users authentication.
     *
     * @param request  the HTTP request is inspected for cookies and the context path in case of a redirect.
     * @param response the HTTP response that is going to be modified with delete-cookies if an invalid JWT cookie was
     *                 found. Also the a redirect to the log-in page is added.
     */
    public void handleInvalidJwtCookie(HttpRequest request, HttpResponse response) {
        boolean requestToLoginPage = isRequestToLoginPage(request);
        if (requestToLoginPage) {
            LOG.debug("User is already being redirected to the log-in page. Will do nothing.");
            return;
        }

        if (isUserLoggedOutAndLogsInAgain(request)) {
            LOG.debug("User authentication cookies will be removed because an invalid JWT token was found");
            // Security advice:
            // Do NOT remove the user's token from the session store. It must stay blacklisted until it is removed
            // during the expiration date check.
            removeAuthCookies(response, request.getContextPath());
        }
    }

    private boolean isRequestToLoginPage(HttpRequest request) {
        return request.getRequestURL().contains("/sessions/new");
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

        SimpleJwt storedJwt = casSessionStore.fetchStoredJwt(jwt);
        LOG.debug("Is the found JWT token {} invalid? {}", jwt.getJwtId(), storedJwt.isInvalid());

        return storedJwt.isInvalid();
    }

    private void removeAuthCookies(HttpResponse response, String contextPath) {
        boolean useSecureCookies = SonarCasProperties.USE_SECURE_REDIRECT_COOKIES.getBoolean(configuration, true);

        Cookie jwtCookie = Cookies.createDeletionCookie(JWT_SESSION_COOKIE, contextPath, useSecureCookies);
        response.addCookie(jwtCookie);

        Cookie xsrfCookie = Cookies.createDeletionCookie("XSRF-TOKEN", contextPath, useSecureCookies);
        response.addCookie(xsrfCookie);
    }

    private String getSonarServiceUrl() {
        String sonarUrl = SonarCasProperties.SONAR_SERVER_URL.mustGetString(configuration);
        return sonarUrl + "/sessions/init/sonarqube"; // cas corresponds to the value from getKey()
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "LogoutRequest", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
    private static class LogoutRequest {

        @XmlElement(name = "SessionIndex", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
        private String sessionId;
    }
}
