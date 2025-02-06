package org.sonar.plugins.cas.util;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.sonar.api.server.http.Cookie;


import static org.fest.assertions.Assertions.assertThat;

public class CookiesTest {

    @Test
    public void cookieBuilderShouldCreateHttpOnlyCookie() {
        Cookie cookie = new Cookies.HttpOnlyCookieBuilder()
                .name("key")
                .value("value")
                .contextPath("/")
                .maxAgeInSecs(3600)
                .secure(true)
                .build();

        assertThat(cookie.getName()).isEqualTo("key");
        assertThat(cookie.getValue()).isEqualTo("value");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(3600);
        assertThat(cookie.isSecure()).isTrue();
    }

    @Test
    public void createDeletionCookie() {
        Cookie cookie = Cookies.createDeletionCookie("key", "http://server.url/", true);

        assertThat(cookie.getName()).isEqualTo("key");
        assertThat(cookie.getValue()).isEqualTo("");
        assertThat(cookie.getPath()).isEqualTo("http://server.url/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(0);
        assertThat(cookie.isSecure()).isTrue();

        Cookie cookie2 = Cookies.createDeletionCookie("key", "http://server.url/", false);

        assertThat(cookie2.isSecure()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDeletionCookieShouldThrowException() {
        Cookies.createDeletionCookie("key", null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_nameMustNotNull() {
        new Cookies.HttpOnlyCookieBuilder()
                .name(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_nameMustNotEmpty() {
        new Cookies.HttpOnlyCookieBuilder()
                .name("")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_valueMustNotNull() {
        new Cookies.HttpOnlyCookieBuilder()
                .name("name")
                .value(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_contextPathMustNotNull() {
        new Cookies.HttpOnlyCookieBuilder()
                .name("name")
                .value("value")
                .contextPath(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_contextPathMustNotEmpty() {
        new Cookies.HttpOnlyCookieBuilder()
                .name("name")
                .value("value")
                .contextPath("")
                .build();
    }

    @Test
    public void cookieBuildShouldBuildCookie_valueMayBeEmpty() {
        Cookie actual = new Cookies.HttpOnlyCookieBuilder()
                .name("test")
                .value("")
                .contextPath("/")
                .build();

        assertThat(actual.getName()).isEqualTo("test");
        assertThat(actual.getValue()).isEqualTo("");
        assertThat(actual.getPath()).isEqualTo("/");
        assertThat(actual.isHttpOnly()).isTrue();
        assertThat(actual.getMaxAge()).isEqualTo(0); //implicitly set by default int value
    }

    @Test
    public void findCookieByNameShouldReturnCookie() {
        Cookie c1 = new SonarCookie("key1", "value1", "/test", true, 10, true);
        Cookie c2 = new SonarCookie("key2", "value2", "/test", true, 10, true);
        Cookie[] cookies = {c1, c2};

        Cookie actual = Cookies.findCookieByName(cookies, "key1");

        Assertions.assertThat(actual).isEqualTo(c1);
    }

    @Test
    public void findCookieByNameShouldReturnNullIfNotFound() {
        Cookie c1 = new SonarCookie("key1", "value1", "/test", true, 10, true);
        Cookie c2 = new SonarCookie("key2", "value2", "/test", true, 10, true);
        Cookie[] cookies = {c1, c2};

        Cookie actual = Cookies.findCookieByName(cookies, "key3");

        Assertions.assertThat(actual).isNull();
    }

    @Test
    public void findCookieByNameShouldHandleNoCookiesGracefully() {
        Cookie[] cookiesCollection1 = {};

        Cookie actual = Cookies.findCookieByName(cookiesCollection1, "key1");

        Assertions.assertThat(actual).isNull();

        actual = Cookies.findCookieByName(null, "key1");

        Assertions.assertThat(actual).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void findCookieByNameShouldThrowException() {
        Cookies.findCookieByName(null, null);
    }
}
