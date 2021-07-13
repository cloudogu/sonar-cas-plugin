package org.sonar.plugins.cas.util;

import java.time.Instant;
import java.util.UUID;

public class SimpleJwtBuilder {
    private final Instant nowish;
    private String id;
    private long expirationEpochSeconds;

    public SimpleJwtBuilder() {
        this(Instant.now());
    }

    /**
     * for testability
     *
     * @param now
     */
    SimpleJwtBuilder(Instant now) {
        this.nowish = now;
    }

    public SimpleJwtBuilder withGeneratedId() {
        this.id = UUID.randomUUID().toString();
        return this;
    }

    public SimpleJwtBuilder withExpirationFromNow(long expirationInSeconds) {
        this.expirationEpochSeconds = nowish.plusSeconds(expirationInSeconds).getEpochSecond();
        return this;
    }

    public SimpleJwt build() {
        return SimpleJwt.fromIdAndExpiration(this.id, this.expirationEpochSeconds);
    }
}