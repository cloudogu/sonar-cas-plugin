package org.sonar.plugins.cas.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

/**
 * This class provides a basic support of JSON Web Token (https://tools.ietf.org/html/rfc7519) for Sonar-CAS-Interaction.
 *
 * Instances are immutable and thread-safe.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SimpleJwt")
public class SimpleJwt {
    private static final SimpleJwt nullObject = new SimpleJwt("null JWT", 1L, true);

    @XmlElement
    private String jwtId;
    /**
     * the expiration date is given as epoch seconds in UTC
     */
    @XmlElement
    private long expiration;
    /**
     * A invalid JWT is considered as invalid, that is the user has to re-login in order to get a new JWT.
     */
    @XmlElement
    private boolean invalid;

    SimpleJwt() {
    }

    public static SimpleJwt getNullObject() {
        return nullObject;
    }

    public String getJwtId() {
        return jwtId;
    }

    public Instant getExpiration() {
        return Instant.ofEpochSecond(expiration);
    }

    public boolean isExpired() {
        return getExpiration().isBefore(Instant.now());
    }

    public boolean isInvalid() {
        return invalid;
    }

    private SimpleJwt(String jwtId, Instant expiration, boolean invalid) {
        this(jwtId, expiration.getEpochSecond(), invalid);
    }

    private SimpleJwt(String jwtId, long expiration, boolean invalid) {
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
     * Creates a valid JWT from a given ID and epoch second timestamps
     * @param jwtId the JWT identifies uniquely. Must not be null or the empty string.
     * @param expirationAsEpochSeconds the instant when the JWT expires. Must not be negative
     * @return a JWT
     */
    public static SimpleJwt fromIdAndExpiration(String jwtId, long expirationAsEpochSeconds) {
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
                expiration == other.expiration &&
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
