package org.sonar.plugins.cas.util;

import org.junit.Test;

import java.time.Instant;

import static org.fest.assertions.Assertions.assertThat;

public class SimpleJwtBuilderTest {

    private static final long EPOCH_SECOND_2021_07_13_14_15_16_GMT = 1626185716L;
    private static final int fiveMinutesInSec = 300;

    @Test
    public void build() {
        SimpleJwt actual = new SimpleJwtBuilder(Instant.ofEpochSecond(EPOCH_SECOND_2021_07_13_14_15_16_GMT))
                .withGeneratedId()
                .withExpirationFromNow(fiveMinutesInSec)
                .build();

        assertThat(actual.getExpiration().toString()).isEqualTo("2021-07-13T14:20:16Z");
        assertThat(actual.getJwtId()).isNotEmpty().matches("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}");
    }
}