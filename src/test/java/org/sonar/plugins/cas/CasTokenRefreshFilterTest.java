package org.sonar.plugins.cas;

import org.junit.Test;
import org.sonar.plugins.cas.util.SimpleJwt;

import static org.fest.assertions.Assertions.assertThat;

public class CasTokenRefreshFilterTest {

    @Test
    public void isTokenRefreshedShouldReturnTrueForEqualJwtIds() {
        SimpleJwt oldJwt = SimpleJwt.fromIdAndExpiration("asdf", 1L);
        SimpleJwt newJwt = SimpleJwt.fromIdAndExpiration("asdf", 2L);
        CasTokenRefreshFilter sut = new CasTokenRefreshFilter(null);

        boolean actual = sut.isTokenRefreshed(oldJwt, newJwt);

        assertThat(actual).isTrue();
    }
}