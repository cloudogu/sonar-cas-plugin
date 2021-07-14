package org.sonar.plugins.cas.util;

import org.junit.Test;
import org.sonar.plugins.cas.AuthTestData;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.sonar.plugins.cas.util.Cookies.JWT_SESSION_COOKIE;

public class JwtProcessorTest {
    private static final String SONARQUBE_JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9" +
            ".eyJqdGkiOiJBV2poSm1xc3RwTWJfYmNkRXlZQSIsInN1YiI6ImFkbWluIiwiaWF0Ijox" +
            "NTQ5OTY1NjE3LCJleHAiOjE1NTAyMjQ4MTcsImxhc3RSZWZyZXNoVGltZSI6MTU0OTk2" +
            "NTYxNzcyMiwieHNyZlRva2VuIjoiaHZpcGRyMzBkamdic2lwY2E0ZmZhMmdwYm4ifQ" +
            ".QTCPErWDrzDcZBUuGje1vjbjJbp11rsBZ6z5ZBIaoR0";
    private static final String PROXYTICKET_JWT_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
            ".eyJzdWIiOiJhZG1pbiIsImV4cCI6MTYyNTczNTQxMSwianRpIjoiMmQxMjZkMDktMmFmNi00NTI4LWJh" +
            "YmQtZmJjMGI3NmY4OWQ5In0" +
            ".K90vJ0sapAPX6LywhdC68Y7RaArJ_iqxukJQ9Yc5Py8";
    private static final String USUAL_COOKIE_ATTRIBUTES = "Max-Age=259200; Expires=Fri, 15-Feb-2019 10:10:57 GMT; Path=/; HttpOnly";

    @Test
    public void getJwtTokenShouldReturnToken() {
        String jwtHeader = "JWT-SESSION=" + SONARQUBE_JWT_TOKEN + "; " + USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Collections.singleton(jwtHeader);

        SimpleJwt actualToken = JwtProcessor.mustGetJwtTokenFromResponseHeaders(headers);

        String expectedToken = "AWjhJmqstpMb_bcdEyYA";
        assertThat(actualToken.getJwtId()).isEqualTo(expectedToken);
    }

    @Test
    public void getJwtTokenShouldReturnToken_twoCookies() {
        String jwtHeader = "JWT-SESSION=" + SONARQUBE_JWT_TOKEN + "; " + USUAL_COOKIE_ATTRIBUTES;
        String unrelatedHeader = "TOTALLY=unrelated" + USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Arrays.asList(jwtHeader, unrelatedHeader);

        SimpleJwt actualToken = JwtProcessor.mustGetJwtTokenFromResponseHeaders(headers);

        String expectedToken = "AWjhJmqstpMb_bcdEyYA";
        assertThat(actualToken.getJwtId()).isEqualTo(expectedToken);
    }

    @Test(expected = IllegalStateException.class)
    public void getJwtTokenShouldReturnToken_noCookies() {
        String unrelatedHeader = "TOTALLY=unrelated" + USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Collections.singletonList(unrelatedHeader);

        JwtProcessor.mustGetJwtTokenFromResponseHeaders(headers);
    }

    @Test
    public void mustFilterJwtTokenShouldReturnJwtCookie() {
        String jwtHeader = "JWT-SESSION=" + SONARQUBE_JWT_TOKEN + "; " + USUAL_COOKIE_ATTRIBUTES;
        String unrelatedHeader = "TOTALLY=unrelated" + USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Arrays.asList(jwtHeader, unrelatedHeader);

        String rawJwtCookie = JwtProcessor.mustFilterJwtCookie(headers);

        assertThat(rawJwtCookie).isEqualTo(jwtHeader);
    }

    @Test(expected = IllegalStateException.class)
    public void mustFilterJwtTokenShouldThrowIllegalStateException() {
        String unrelatedHeader = "TOTALLY=unrelated" +
                USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Collections.singletonList(unrelatedHeader);

        JwtProcessor.mustFilterJwtCookie(headers);
    }

    @Test
    public void filterJwtTokenShouldReturnEmptyStringForNoJwt() {
        String unrelatedHeader = "TOTALLY=unrelated" + USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Collections.singletonList(unrelatedHeader);

        String rawJwtCookie = JwtProcessor.filterJwtCookie(headers);

        assertThat(rawJwtCookie).isEqualTo("");
    }

    @Test
    public void filterJwtTokenShouldReturnEmptyStringForNoCookieHeaderAtAll() {
        Collection<String> headers = Collections.emptyList();

        String rawJwtCookie = JwtProcessor.filterJwtCookie(headers);

        assertThat(rawJwtCookie).isEqualTo("");
    }

    @Test
    public void getJwtTokenFromResponseHeadersShouldReturnJwtToken() {
        String cookieHeader1 = "SomeImportanInfo=wow; Version=1; Skin=new;";
        String jwtHeader = JWT_SESSION_COOKIE + "=" + AuthTestData.getJwtToken();
        String cookieHeader3 = "Save$200OnBread=true;";

        Collection<String> headers = Arrays.asList(cookieHeader1, jwtHeader, cookieHeader3);

        SimpleJwt actual = JwtProcessor.getJwtTokenFromResponseHeaders(headers);

        assertThat(actual).isEqualTo(AuthTestData.JWT_TOKEN);
    }

    @Test
    public void encodeProxyTicketJwtShouldReturnJwtString() {
        long expDate_2021_07_08_09_10_11 = 1625735411L;
        String jwtId = "2d126d09-2af6-4528-babd-fbc0b76f89d9";
        SimpleJwt input = new SimpleJwt(jwtId, expDate_2021_07_08_09_10_11, false, "admin");

        String actual = JwtProcessor.encodeProxyTicketJwt(input, "ST-0123456789");

        assertThat(actual).isEqualTo(PROXYTICKET_JWT_TOKEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeProxyTicketJwtShouldThrowExceptionOnEmptyTicketString() {
        long expDate_2021_07_08_09_10_11 = 1625735411L;
        String jwtId = "2d126d09-2af6-4528-babd-fbc0b76f89d9";
        SimpleJwt input = new SimpleJwt(jwtId, expDate_2021_07_08_09_10_11, false, "admin");

        JwtProcessor.encodeProxyTicketJwt(input, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void encodeProxyTicketJwtShouldThrowExceptionOnNullTicket() {
        long expDate_2021_07_08_09_10_11 = 1625735411L;
        String jwtId = "2d126d09-2af6-4528-babd-fbc0b76f89d9";
        SimpleJwt input = new SimpleJwt(jwtId, expDate_2021_07_08_09_10_11, false, "admin");

        JwtProcessor.encodeProxyTicketJwt(input, null);
    }
}
