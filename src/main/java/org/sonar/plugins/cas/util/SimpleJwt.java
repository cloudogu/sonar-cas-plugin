package org.sonar.plugins.cas.util;

import java.time.Instant;

/**
 * This class provides a basic support of JSON Web Token (https://tools.ietf.org/html/rfc7519) for Sonar-CAS-Interaction.
 *
 * Instances are immutable and thread-safe.
 */
public class SimpleJwt {
    private final String jwtId;
    /**
     * the expiration date is given as epoch seconds in UTC
     */
    private final Instant expiration;
    /**
     * A invalid JWT is considered as invalid, that is the user has to re-login in order to get a new JWT.
     */
    private final boolean invalid;

    public String getJwtId() {
        return jwtId;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public boolean isExpired() {
        return expiration.isBefore(Instant.now());
    }

    public boolean isInvalid() {
        return invalid;
    }

    private SimpleJwt(String jwtId, Instant expiration, boolean invalid) {
        this.jwtId = jwtId;
        this.expiration = expiration;
        this.invalid = invalid;
    }

    /**
     * Takes this JWT and creates an invalidated copy
     * @return an invalidated copy
     */
    public SimpleJwt cloneAsInvalidated() {
        return new SimpleJwt(this.jwtId, this.expiration, true);
    }

    /**
     * Creates a valid JWT from a given ID and epoch second timesatmps
     * @param jwtId the JWT identifies uniqiely
     * @param expirationAsEpochSeconds
     * @return
     */
    static SimpleJwt fromIdAndExpiration(String jwtId, long expirationAsEpochSeconds) {
        if (jwtId == null || jwtId.trim().isEmpty()) {
            throw new IllegalArgumentException("jwtId must not be empty");
        }

        if (expirationAsEpochSeconds <= 0) {
            throw new IllegalArgumentException("expirationAsEpochSeconds must not be zero or negative.");
        }

        Instant exp = Instant.ofEpochSecond(expirationAsEpochSeconds);
        return new SimpleJwt(jwtId, exp, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleJwt other = (SimpleJwt) o;
        return jwtId.equals(other.jwtId) &&
                expiration.equals(other.expiration) &&
                invalid == other.invalid;
    }

    @Override
    public int hashCode() {
        return jwtId.hashCode();
    }

    @Override
    public String toString() {
        return "Jwt{" +
                "jti=" + jwtId +
                ", exp=" + expiration +
                ", invalid=" + invalid +
                '}';
    }
}
