package org.sonar.plugins.cas.util;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;

public class JwtProcessorTest {
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJqdGkiOiJBV2poSm1xc3RwTWJfYmNkRXlZQSIsInN1YiI6ImFkbWluIiwiaWF0Ijox" +
            "NTQ5OTY1NjE3LCJleHAiOjE1NTAyMjQ4MTcsImxhc3RSZWZyZXNoVGltZSI6MTU0OTk2" +
            "NTYxNzcyMiwieHNyZlRva2VuIjoiaHZpcGRyMzBkamdic2lwY2E0ZmZhMmdwYm4ifQ." +
            "QTCPErWDrzDcZBUuGje1vjbjJbp11rsBZ6z5ZBIaoR0";
    private static final String USUAL_COOKIE_ATTRIBUTES = "Max-Age=259200; Expires=Fri, 15-Feb-2019 10:10:57 GMT; Path=/; HttpOnly";

    @Test
    public void getJwtTokenShouldReturnToken() throws IOException {
        String jwtHeader = "JWT-SESSION=" + JWT_TOKEN + "; " + USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Collections.singleton(jwtHeader);

        SimpleJwt actualToken = JwtProcessor.getJwtTokenFromRequestHeaders(headers);

        String expectedToken = "AWjhJmqstpMb_bcdEyYA";
        assertThat(actualToken.getJwtId()).isEqualTo(expectedToken);
    }

    @Test
    public void getJwtTokenShouldReturnToken_twoCookies() throws IOException {
        String jwtHeader = "JWT-SESSION=" + JWT_TOKEN + "; " + USUAL_COOKIE_ATTRIBUTES;
        String unrelatedHeader = "TOTALLY=unrelated" + USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Arrays.asList(jwtHeader, unrelatedHeader);

        SimpleJwt actualToken = JwtProcessor.getJwtTokenFromRequestHeaders(headers);

        String expectedToken = "AWjhJmqstpMb_bcdEyYA";
        assertThat(actualToken.getJwtId()).isEqualTo(expectedToken);
    }

    @Test(expected = IllegalStateException.class)
    public void getJwtTokenShouldReturnToken_noCookies() throws IOException {
        String unrelatedHeader = "TOTALLY=unrelated" + USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Collections.singletonList(unrelatedHeader);

        JwtProcessor.getJwtTokenFromRequestHeaders(headers);
    }

    @Test
    public void filterJwtTokenShouldReturnJwtCookie() {
        String jwtHeader = "JWT-SESSION=" + JWT_TOKEN + "; " + USUAL_COOKIE_ATTRIBUTES;
        String unrelatedHeader = "TOTALLY=unrelated" + USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Arrays.asList(jwtHeader, unrelatedHeader);

        String rawJwtCookie = JwtProcessor.filterJwtCookie(headers);

        assertThat(rawJwtCookie).isEqualTo(jwtHeader);
    }

    @Test(expected = IllegalStateException.class)
    public void filterJwtTokenShouldThrowIllegalStateException() {
        String unrelatedHeader = "TOTALLY=unrelated" +
                USUAL_COOKIE_ATTRIBUTES;
        Collection<String> headers = Collections.singletonList(unrelatedHeader);


        JwtProcessor.filterJwtCookie(headers);
    }
}