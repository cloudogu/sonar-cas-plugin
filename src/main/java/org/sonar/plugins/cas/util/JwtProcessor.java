package org.sonar.plugins.cas.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtProcessor {

    private static final String JWT_ID = "jti";
    private static final String JWT_EXPIRATION_DATE = "exp";

    public static SimpleJwt getJwtTokenFromRequestHeaders(Collection<String> headers) {
        String rawToken = filterJwtCookie(headers);
        String token = removeHeader(rawToken);
        token = decodeJwtPayload(token);

        return createJwt(token);
    }

    static String filterJwtCookie(Collection<String> headers) {
        String jwtCookie = headers.stream().filter(header -> header.startsWith("JWT-SESSION=")).collect(Collectors.joining());

        if (jwtCookie.isEmpty()) {
            throw new IllegalStateException("Could not find JWT cookie in current request");
        }

        return jwtCookie;
    }

    private static String removeHeader(String rawToken) {
        return rawToken.substring("JWT-SESSION=".length());
    }

    /**
     * Convert the JWT and return its payload as JSON string.
     *
     * @param jwt the full base64 encoded JWT
     * @return the JWT's payload as JSON string.
     */
    static String decodeJwtPayload(String jwt) {
        String jwtPartDelimiter = ".";
        int payloadStart = jwt.indexOf(jwtPartDelimiter) + 1;
        int payloadEnd = jwt.lastIndexOf(jwtPartDelimiter);
        String tokenBase64 = jwt.substring(payloadStart, payloadEnd);

        byte[] decode = Base64.getDecoder().decode(tokenBase64);
        return new String(decode, StandardCharsets.UTF_8);
    }

    static SimpleJwt createJwt(String token) {
        Map map = null;
        try {
            map = new ObjectMapper().readValue(token, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Exception during JWT parsing", e);
        }
        String jwtId = map.get(JWT_ID).toString();
        String expirationDateRaw = map.get(JWT_EXPIRATION_DATE).toString();
        long expirationDate = Long.valueOf(expirationDateRaw);

        return SimpleJwt.fromIdAndExpiration(jwtId, expirationDate);
    }

    public static SimpleJwt getJwtTokenFromCookies(Cookie[] cookies) {
        Cookie cookie = CookieUtil.findCookieByName(cookies, "JWT-TOKEN");
        if (cookie == null) {
            return SimpleJwt.getNullObject();
        }

        String token = decodeJwtPayload(cookie.getValue());

        return createJwt(token);
    }
}
