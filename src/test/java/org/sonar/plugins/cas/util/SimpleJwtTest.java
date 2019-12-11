package org.sonar.plugins.cas.util;

import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXB;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
        assertThat(jwt.isInvalid()).isFalse();

        SimpleJwt invalidatedJwt = jwt.cloneAsInvalidated();

        assertThat(invalidatedJwt.getJwtId()).isEqualTo(jwt.getJwtId());
        assertThat(invalidatedJwt.getExpiration()).isEqualTo(jwt.getExpiration());
        assertThat(invalidatedJwt.isInvalid()).isTrue();
    }

    @Test
    public void equalsShouldBeTrueForEqualObjects() {
        long expiryDateIn60SecondsTime = 1L;
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt jwt1 = new SimpleJwt(jwtId, expiryDateIn60SecondsTime, true);
        SimpleJwt jwt2 = new SimpleJwt(jwtId, expiryDateIn60SecondsTime, true);

        boolean actualId = jwt1.equals(jwt1);
        boolean actual1 = jwt1.equals(jwt2);
        boolean actual2 = jwt2.equals(jwt1);

        assertThat(jwt1.getJwtId()).isEqualTo(jwt2.getJwtId());
        assertThat(jwt1.getExpiration()).isEqualTo(jwt2.getExpiration());
        assertThat(jwt1.isInvalid()).isEqualTo(jwt2.isInvalid());
        assertThat(actualId).isTrue();
        assertThat(actual1).isTrue();
        assertThat(actual2).isTrue();
        assertThat(actual1).isEqualTo(actual2);
    }

    @Test
    public void equalsShouldReturnFalseForUnequalInstances() {
        //compare the 1st against all others
        SimpleJwt jwt1 = new SimpleJwt("A", 1L, true);

        SimpleJwt jwt2 = new SimpleJwt("B", 1L, true);
        SimpleJwt jwt3 = new SimpleJwt("A", 2L, true);
        SimpleJwt jwt4 = new SimpleJwt("A", 1L, false);

        assertThat(jwt1).isNotEqualTo(jwt2);
        assertThat(jwt1).isNotEqualTo(jwt3);
        assertThat(jwt1).isNotEqualTo(jwt4);
    }

    @Test
    public void equalsShouldReturnFalseForEverythingElse() {
        //compare the 1st against all others
        SimpleJwt jwt1 = new SimpleJwt("A", 1L, true);

        assertThat(jwt1).isNotEqualTo(null);
        assertThat(jwt1).isNotEqualTo("banana");
        assertThat(jwt1).isNotEqualTo(new Object());
    }

    @Test
    public void hashCodeShouldReturnSameHashOnEqualObjects() {
        long expiryDateIn60SecondsTime = 1L;
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt jwt1 = new SimpleJwt(jwtId, expiryDateIn60SecondsTime, true);
        SimpleJwt jwt2 = new SimpleJwt(jwtId, expiryDateIn60SecondsTime, true);

        int actualHash1 = jwt1.hashCode();
        int actualHash2 = jwt2.hashCode();

        assertThat(actualHash1).isEqualTo(actualHash2);
    }

    @Test
    public void hashCodeShouldReturnDifferentHashOnDifferentObjectIds() {
        long expiryDateIn60SecondsTime = 1L;
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt jwt1 = new SimpleJwt(jwtId, expiryDateIn60SecondsTime, true);
        SimpleJwt jwt2 = new SimpleJwt("totally different", expiryDateIn60SecondsTime, true);

        int actualHash1 = jwt1.hashCode();
        int actualHash2 = jwt2.hashCode();

        assertThat(actualHash1).isNotEqualTo(actualHash2);
    }

    @Test
    public void hashCodeShouldReturnSameHashOnDifferentObjectsButSameIds() {
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt jwt1 = new SimpleJwt(jwtId, 123456798L, true);
        SimpleJwt jwt2 = new SimpleJwt(jwtId, 1L, false);

        int actualHash1 = jwt1.hashCode();
        int actualHash2 = jwt2.hashCode();

        assertThat(actualHash1).isEqualTo(actualHash2);
    }
}