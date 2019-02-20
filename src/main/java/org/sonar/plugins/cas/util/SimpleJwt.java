package org.sonar.plugins.cas.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

/**
 * This class provides a basic support of <a href="https://tools.ietf.org/html/rfc7519">JSON Web Token</a> for
 * Sonar-CAS-Interaction.
 * <p>
 * Instances are immutable and thread-safe.
 * </p>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SimpleJwt")
public final class SimpleJwt {
    private static final SimpleJwt nullObject = new SimpleJwt("jwt-null-object", 1L, true);

    /**
     * The id uniquely identifies a token. It must not be <code>null</code> or the empty string.
     */
    @XmlElement
    private String jwtId;
    /**
     * The expiration date is given as epoch seconds in UTC and determines the point in time when the token is deleted
     * for good. It must be strictly positive.
     */
    @XmlElement
    private long expiration;
    /**
     * A JWT being considered invalid provides a way to reduce the possibility to login when a user already logged out
     * but an attacker might have gained access to the JWT token. The user has to re-login in order to get a new JWT.
     */
    @XmlElement
    private boolean invalid;

    /**
     * Constructor used by JAXB initialization
     */
    @SuppressWarnings("unused")
    SimpleJwt() {
    }

    SimpleJwt(String jwtId, long expiration, boolean invalid) {
        this.jwtId = jwtId;
        this.expiration = expiration;
        this.invalid = invalid;
    }

    public static SimpleJwt getNullObject() {
        return nullObject;
    }

    public String getJwtId() {
        return jwtId;
    }

    Instant getExpiration() {
        return Instant.ofEpochSecond(expiration);
    }

    public boolean isExpired() {
        return getExpiration().isBefore(Instant.now());
    }

    public boolean isInvalid() {
        return invalid;
    }

    /**
     * Takes this JWT and creates an invalidated copy
     *
     * @return an invalidated copy
     */
    public SimpleJwt cloneAsInvalidated() {
        return new SimpleJwt(this.jwtId, this.expiration, true);
    }

    /**
     * Creates a valid JWT from a given ID and epoch second timestamps
     *
     * @param jwtId                    the JWT identifies uniquely. Must not be null or the empty string.
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

        return new SimpleJwt(jwtId, expirationAsEpochSeconds, false);
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
