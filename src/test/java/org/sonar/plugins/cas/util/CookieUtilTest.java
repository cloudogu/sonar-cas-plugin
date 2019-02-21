package org.sonar.plugins.cas.util;

import org.fest.assertions.Assertions;
import org.junit.Test;

import javax.servlet.http.Cookie;

import static org.fest.assertions.Assertions.assertThat;

public class CookieUtilTest {

    @Test
    public void createHttpOnlyCookie() {
        Cookie cookie = CookieUtil.createHttpOnlyCookie("key", "value", 3600);

        assertThat(cookie.getName()).isEqualTo("key");
        assertThat(cookie.getValue()).isEqualTo("value");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(3600);
    }

    @Test
    public void createDeletionCookie() {
        Cookie cookie = CookieUtil.createDeletionCookie("key");

        assertThat(cookie.getName()).isEqualTo("key");
        assertThat(cookie.getValue()).isEqualTo("");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(0);
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
