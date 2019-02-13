package org.sonar.plugins.cas.util;

import org.fest.assertions.Assertions;
import org.junit.Test;

import java.time.Instant;

import static org.fest.assertions.Assertions.assertThat;

public class SimpleJwtTest {

    @Test
    public void isExpired_alreadyExpired() {
        long expiryDate60SecondsAgo = Instant.now().minusSeconds(60).getEpochSecond();
        SimpleJwt expiredJwt = SimpleJwt.fromIdAndExpiration("id", expiryDate60SecondsAgo);

        boolean expired = expiredJwt.isExpired();

        assertThat(expired).isTrue();
    }

    @Test
    public void isExpired_exiredTheSameSecond() {
        long literallyThisSecond = Instant.now().getEpochSecond();
        SimpleJwt expiredJwt = SimpleJwt.fromIdAndExpiration("id", literallyThisSecond);

        boolean expired = expiredJwt.isExpired();

        assertThat(expired).isTrue();
    }

    @Test
    public void isExpired_notExpired() {
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        SimpleJwt expiredJwt = SimpleJwt.fromIdAndExpiration("id", expiryDateIn60SecondsTime);

        boolean expired = expiredJwt.isExpired();

        assertThat(expired).isFalse();
    }

    @Test
    public void CloneAsInvalidShouldReturnAnIdenticalCopyButTheInvalidFlag() {
        long now = Instant.now().getEpochSecond();
        SimpleJwt jwt = SimpleJwt.fromIdAndExpiration("id", now);
        assertThat(jwt.isInvalid()).isTrue();

        SimpleJwt invalidatedJwt = jwt.cloneAsInvalidated();

        assertThat(invalidatedJwt.getJwtId()).isEqualTo(jwt.getJwtId());
        assertThat(invalidatedJwt.getExpiration()).isEqualTo(jwt.getExpiration());
        assertThat(invalidatedJwt.isInvalid()).isTrue();
    }
}