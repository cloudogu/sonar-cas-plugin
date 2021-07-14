package org.sonar.plugins.cas.util;

import java.time.Instant;
import java.util.UUID;

public class SimpleJwtBuilder {
    private final Instant now;
    private String id;
    private long expirationEpochSeconds;
    private String subject;
    private boolean invalid = false;

    public SimpleJwtBuilder() {
        this(Instant.now());
    }

    /**
     * for testability
     *
     * @param now a testable point in time
     */
    SimpleJwtBuilder(Instant now) {
        this.now = now;
    }

    public SimpleJwtBuilder withGeneratedId() {
        this.id = UUID.randomUUID().toString();
        return this;
    }

    public SimpleJwtBuilder withExpirationFromNow(long expirationInSeconds) {
        this.expirationEpochSeconds = now.plusSeconds(expirationInSeconds).getEpochSecond();
        return this;
    }

    public SimpleJwtBuilder withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public SimpleJwtBuilder withInvalid(boolean invalid) {
        this.invalid = invalid;
        return this;
    }

    public SimpleJwt build() {
        return new SimpleJwt(
                this.id,
                this.expirationEpochSeconds,
                this.invalid,
                this.subject);
    }
}