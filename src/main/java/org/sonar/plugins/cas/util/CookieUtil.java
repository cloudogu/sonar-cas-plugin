package org.sonar.plugins.cas.util;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;

public final class CookieUtil {

    /**
     * Creates a http-only cookie (that is, cookies not to be read by scripts) with a name, a value, and an expiration
     * date.
     *
     * @param name         the name identifies uniquely a cookie
     * @param value        the payload to be held by a cookie
     * @param maxAgeInSecs the max age determines the expiration of the cookie. An integer specifying the maximum age
     *                     of the cookie in seconds; if negative, means the cookie is only stored until the browser
     *                     exits; if zero, deletes the cookie. See {{@link Cookie}}.
     * @return a new cookie with the given attributes
     */
    public static Cookie createHttpOnlyCookie(String name, String value, int maxAgeInSecs) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAgeInSecs);

        return cookie;
    }

    /**
     * Creates a deletion cookie. A cookie is supposed to be deleted when the maximal age is set to
     * zero. Additionally the deletion cookie is set with an empty value. See {@link Cookie}.
     *
     * @param cookieName the cookie name identifies the cookie to be deleted. Must not be <code>null</code> or the
     *                   empty string.
     * @return a new cookie which is supposed to be deleted by the browser
     */
    public static Cookie createDeletionCookie(String cookieName) {
        if (StringUtils.isEmpty(cookieName)) {
            throw new IllegalArgumentException("Could not create cookie. CookieName must not be empty.");
        }

        return createHttpOnlyCookie(cookieName, "", 0);
    }

    /**
     * Finds a cookie by name and returns it. May return <code>null</code> if none is found
     * @param cookies the cookies to be searched, aka haystack
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
}
