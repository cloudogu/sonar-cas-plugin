package org.sonar.plugins.cas.util;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;

public final class CookieUtil {
    public static final String JWT_SESSION_COOKIE = "JWT-SESSION";
    public static final String COOKIE_NAME_URL_AFTER_CAS_REDIRECT = "redirectAfterCasLogin";

    /**
     * Creates a deletion cookie. A cookie is supposed to be deleted when the maximal age is set to
     * zero. Additionally the deletion cookie is set with an empty value. See {@link Cookie}.
     *
     * @param cookieName the cookie name identifies the cookie to be deleted. Must not be <code>null</code> or the
     *                   empty string.
     * @return a new cookie which is supposed to be deleted by the browser
     */
    public static Cookie createDeletionCookie(String cookieName, String contextPath) {
        if (StringUtils.isEmpty(cookieName)) {
            throw new IllegalArgumentException("Could not create cookie. CookieName must not be empty.");
        }

        return new HttpOnlyCookieBuilder()
                .name(cookieName)
                .value("")
                .contextPath(contextPath)
                .maxAgeInSecs(0)
                .build();
    }

    /**
     * Finds a cookie by name and returns it. May return <code>null</code> if none is found
     *
     * @param cookies    the cookies to be searched, aka haystack
     * @param cookieName the name of the cookie, aka needle
     * @return a cookie if found, otherwise null
     */
    public static Cookie findCookieByName(Cookie[] cookies, String cookieName) {
        if (StringUtils.isEmpty(cookieName)) {
            throw new IllegalArgumentException("Cookie name must not be blank");
        }
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Creates a http-only cookie (that is, cookies not to be read by scripts) with a name, a value, an expiration date,
     * and a context path for proper browser handling.
     */
    public static class HttpOnlyCookieBuilder {
        private String name;
        private String value;
        private String contextPath;
        private int maxAge;

        /**
         * @param name the name identifies uniquely a cookie. Must not be empty.
         */
        public HttpOnlyCookieBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * @param value the payload to be held by a cookie, may be the empty String but not <code>null</code>.
         */
        public HttpOnlyCookieBuilder value(String value) {
            this.value = value;
            return this;
        }

        /**
         * @param contextPath the context path is a URI which defines the path to the SonarQube server. Must not be
         *                    empty.
         */
        public HttpOnlyCookieBuilder contextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        /**
         * @param maxAge the max age determines the expiration of the cookie. An integer specifying the maximum age
         *               of the cookie in seconds; if negative, means the cookie is only stored until the browser
         *               exits; if zero, deletes the cookie. See {{@link Cookie}}.
         */
        public HttpOnlyCookieBuilder maxAgeInSecs(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Cookie build() {
            if (StringUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Could not create cookie. Cookie name must not be empty.");
            }
            if (value == null) {
                throw new IllegalArgumentException("Could not create cookie. Cookie value must not be null.");
            }
            if (StringUtils.isEmpty(contextPath)) {
                throw new IllegalArgumentException("Could not create cookie. Context path must not be empty.");
            }

            Cookie cookie = new Cookie(name, value);
            cookie.setMaxAge(maxAge);
            cookie.setPath(contextPath);
            cookie.setHttpOnly(true);

            return cookie;
        }
    }
}
