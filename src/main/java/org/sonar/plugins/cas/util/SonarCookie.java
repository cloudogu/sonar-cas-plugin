package org.sonar.plugins.cas.util;

import org.sonar.api.server.http.Cookie;

/**
 * Helper class for creating Sonar Cookies implementing Sonar Cookie Interface
 */
public class SonarCookie implements Cookie {
    private final String name;
    private final String value;
    private final String path;
    private final boolean isSecure;
    private final boolean isHttpOnly;
    private final int maxAge;

    public SonarCookie(String name, String value, String path, boolean secure, int maxAge, boolean isHttpOnly) {
        this.name = name;
        this.value = value;
        this.path = path;
        this.isSecure = secure;
        this.maxAge = maxAge;
        this.isHttpOnly = isHttpOnly;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public boolean isHttpOnly() {
        return isHttpOnly;
    }

    @Override
    public int getMaxAge() {
        return maxAge;
    }
}
