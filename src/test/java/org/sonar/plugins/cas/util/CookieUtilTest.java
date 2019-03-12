package org.sonar.plugins.cas.util;

import org.fest.assertions.Assertions;
import org.junit.Test;

import javax.servlet.http.Cookie;

import static org.fest.assertions.Assertions.assertThat;

public class CookieUtilTest {

    @Test
    public void cookieBuilderShouldCreateHttpOnlyCookie() {
        Cookie cookie = new CookieUtil.HttpOnlyCookieBuilder()
                .name("key")
                .value("value")
                .contextPath("/")
                .maxAgeInSecs(3600)
                .build();

        assertThat(cookie.getName()).isEqualTo("key");
        assertThat(cookie.getValue()).isEqualTo("value");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(3600);
    }

    @Test
    public void createDeletionCookie() {
        Cookie cookie = CookieUtil.createDeletionCookie("key", "http://server.url/");

        assertThat(cookie.getName()).isEqualTo("key");
        assertThat(cookie.getValue()).isEqualTo("");
        assertThat(cookie.getPath()).isEqualTo("http://server.url/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDeletionCookieShouldThrowException() {
        CookieUtil.createDeletionCookie("key", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_nameMustNotNull() {
        new CookieUtil.HttpOnlyCookieBuilder()
                .name(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_nameMustNotEmpty() {
        new CookieUtil.HttpOnlyCookieBuilder()
                .name("")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_valueMustNotNull() {
        new CookieUtil.HttpOnlyCookieBuilder()
                .name("name")
                .value(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_contextPathMustNotNull() {
        new CookieUtil.HttpOnlyCookieBuilder()
                .name("name")
                .value("value")
                .contextPath(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void cookieBuildShouldThrowException_contextPathMustNotEmpty() {
        new CookieUtil.HttpOnlyCookieBuilder()
                .name("name")
                .value("value")
                .contextPath("")
                .build();
    }

    @Test
    public void cookieBuildShouldBuildCookie_valueMayBeEmpty() {
        Cookie actual = new CookieUtil.HttpOnlyCookieBuilder()
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
        Cookie c1 = new Cookie("key1", "value1");
        Cookie c2 = new Cookie("key2", "value2");
        Cookie[] cookies = {c1, c2};

        Cookie actual = CookieUtil.findCookieByName(cookies, "key1");

        Assertions.assertThat(actual).isEqualTo(c1);
    }

    @Test
    public void findCookieByNameShouldReturnNullIfNotFound() {
        Cookie c1 = new Cookie("key1", "value1");
        Cookie c2 = new Cookie("key2", "value2");
        Cookie[] cookies = {c1, c2};

        Cookie actual = CookieUtil.findCookieByName(cookies, "key3");

        Assertions.assertThat(actual).isNull();
    }

    @Test
    public void findCookieByNameShouldHandleNoCookiesGracefully() {
        Cookie[] cookiesCollection1 = {};

        Cookie actual = CookieUtil.findCookieByName(cookiesCollection1, "key1");

        Assertions.assertThat(actual).isNull();

        actual = CookieUtil.findCookieByName(null, "key1");

        Assertions.assertThat(actual).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void findCookieByNameShouldThrowException() {
        CookieUtil.findCookieByName(null, null);
    }
}
