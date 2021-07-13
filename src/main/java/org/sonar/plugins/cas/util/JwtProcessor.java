package org.sonar.plugins.cas.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.sonar.plugins.cas.util.Cookies.JWT_SESSION_COOKIE;

public class JwtProcessor {
    private static final String JWT_ID = "jti";
    private static final String JWT_EXPIRATION_DATE = "exp";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static SimpleJwt mustGetJwtTokenFromResponseHeaders(Collection<String> headers) {
        String rawToken = mustFilterJwtCookie(headers);
        String token = removeHeader(rawToken);
        token = decodeJwtPayload(token);

        return createJwt(token);
    }

    static String mustFilterJwtCookie(Collection<String> headers) {
        String jwtCookie = filterJwtCookie(headers);

        if (jwtCookie.isEmpty()) {
            throw new IllegalStateException("Could not find JWT cookie in current request");
        }

        return jwtCookie;
    }

    public static SimpleJwt getJwtTokenFromResponseHeaders(Collection<String> headers) {
        String rawToken = filterJwtCookie(headers);
        if (rawToken.isEmpty()) {
            return SimpleJwt.getNullObject();
        }
        String token = removeHeader(rawToken);
        token = decodeJwtPayload(token);

        return createJwt(token);
    }

    static String filterJwtCookie(Collection<String> headers) {
        return headers.stream()
                .filter(header -> header.startsWith(JWT_SESSION_COOKIE + "="))
                .collect(Collectors.joining());
    }

    private static String removeHeader(String rawToken) {
        return rawToken.substring((JWT_SESSION_COOKIE + "=").length());
    }

    /**
     * Convert the JWT and return its payload as JSON string.
     *
     * @param jwt the full base64 encoded JWT
     * @return the JWT's payload as JSON string.
     */
    private static String decodeJwtPayload(String jwt) {
        String jwtPartDelimiter = ".";
        int payloadStart = jwt.indexOf(jwtPartDelimiter) + 1;
        int payloadEnd = jwt.lastIndexOf(jwtPartDelimiter);
        String tokenBase64 = jwt.substring(payloadStart, payloadEnd);

        byte[] decode = Base64.getDecoder().decode(tokenBase64);
        return new String(decode, StandardCharsets.UTF_8);
    }

    static String encodeJwtPayload(SimpleJwt jwt) {
        return "";
    }

    private static SimpleJwt createJwt(String token) {
        Map map;
        try {
            map = OBJECT_MAPPER.readValue(token, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Exception during JWT parsing", e);
        }
        String jwtId = map.get(JWT_ID).toString();
        String expirationDateRaw = map.get(JWT_EXPIRATION_DATE).toString();
        long expirationDate = Long.valueOf(expirationDateRaw);

        return SimpleJwt.fromIdAndExpiration(jwtId, expirationDate);
    }

    public static SimpleJwt getJwtTokenFromCookies(Cookie[] cookies) {
        Cookie cookie = Cookies.findCookieByName(cookies, JWT_SESSION_COOKIE);
        if (cookie == null) {
            return SimpleJwt.getNullObject();
        }

        String token = decodeJwtPayload(cookie.getValue());

        return createJwt(token);
    }
}
