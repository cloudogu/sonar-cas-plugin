package org.sonar.plugins.cas.session;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import static org.fest.assertions.Assertions.assertThat;

public class JwtTokenFileHandlerTest {

    private Path sessionStore;
    private JwtTokenFileHandler sut;

    @Before
    public void setUp() throws Exception {
        sessionStore = Files.createTempDirectory("sessionStore");
        sut = new JwtTokenFileHandler(sessionStore.toString());
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(sessionStore.toFile());
    }

    @Test
    public void isJwtStoredReturnsTrueForExistingFile() throws IOException {
        Files.createFile(Paths.get(sessionStore.toString() + File.separator + "1234"));
        JwtTokenFileHandler sut = new JwtTokenFileHandler(sessionStore.toString());

        boolean actual = sut.isJwtStored("1234");

        assertThat(actual).isTrue();
    }

    @Test
    public void isJwtStoredReturnsFalseForMissingFile() {
        boolean actual = sut.isJwtStored("1234");

        assertThat(actual).isFalse();
    }

    @Test
    public void storeShouldStoreNewJwtFile() throws IOException {
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt jwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);

        sut.store(jwtId, jwt);
        boolean actuallyStored = Files.exists(Paths.get(sessionStore + File.separator + jwtId));

        assertThat(actuallyStored).isTrue();
    }

    @Test
    public void getShouldReturnJwt() throws IOException {
        // given
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt originalJwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);
        // store a originalJwt file
        sut.store(jwtId, originalJwt);

        // when
        SimpleJwt restoredJwt = sut.get(jwtId);

        // then
        assertThat(restoredJwt).isEqualTo(originalJwt);
        // for further tests see equals tests in SimpleJwtTest
    }

    @Test(expected = IOException.class)
    public void getShouldThrowException() throws IOException {
        sut.get("banana");
    }

    @Test
    public void replaceShouldUpdateTheFilesContent() throws IOException {
        // given
        long expiryDateIn60SecondsTime = Instant.now().getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt originalJwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);
        sut.store(jwtId, originalJwt);
        SimpleJwt updatedJwt = originalJwt.cloneAsInvalidated();
        assertThat(updatedJwt).isNotEqualTo(originalJwt);

        // when
        sut.replace(jwtId, updatedJwt);

        // then
        SimpleJwt restoredUpdatedJwt = sut.get(jwtId);
        assertThat(restoredUpdatedJwt).isEqualTo(updatedJwt);
        assertThat(restoredUpdatedJwt).isNotEqualTo(originalJwt);
    }
}
