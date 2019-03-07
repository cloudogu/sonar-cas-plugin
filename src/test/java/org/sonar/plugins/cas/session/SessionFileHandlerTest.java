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

public class SessionFileHandlerTest {

    private Path sessionStore;
    private SessionFileHandler sut;

    @Before
    public void setUp() throws Exception {
        sessionStore = Files.createTempDirectory("sessionStore");
        sut = new SessionFileHandler(sessionStore.toString());
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(sessionStore.toFile());
    }

    @Test
    public void isJwtStoredReturnsTrueForExistingFile() throws IOException {
        Files.createFile(Paths.get(sessionStore.toString() + File.separator + "1234"));
        SessionFileHandler sut = new SessionFileHandler(sessionStore.toString());

        boolean actual = sut.isJwtStored("1234");

        assertThat(actual).isTrue();
    }

    @Test
    public void isJwtStoredReturnsFalseForMissingFile() {
        boolean actual = sut.isJwtStored("1234");

        assertThat(actual).isFalse();
    }

    @Test
    public void writeJwtFileShouldStoreNewJwtFile() {
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt jwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);

        sut.writeJwtFile(jwtId, jwt);
        boolean actuallyStored = Files.exists(sessionStore.resolve(jwtId));

        assertThat(actuallyStored).isTrue();
    }

    @Test
    public void readJwtFileShouldReturnJwt() {
        // given
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt originalJwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);
        // writeJwtFile a originalJwt file
        sut.writeJwtFile(jwtId, originalJwt);

        // when
        SimpleJwt restoredJwt = sut.readJwtFile(jwtId);

        // then
        assertThat(restoredJwt).isEqualTo(originalJwt);
        // for further tests see equals tests in SimpleJwtTest
    }

    @Test(expected = RuntimeException.class)
    public void readJwtFileShouldThrowException() {
        sut.readJwtFile("banana");
    }

    @Test
    public void replaceJwtFileShouldUpdateTheFilesContent() throws IOException {
        // given
        long expiryDateIn60SecondsTime = Instant.now().getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt originalJwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);
        sut.writeJwtFile(jwtId, originalJwt);
        SimpleJwt updatedJwt = originalJwt.cloneAsInvalidated();
        assertThat(updatedJwt).isNotEqualTo(originalJwt);

        // when
        sut.replaceJwtFile(jwtId, updatedJwt);

        // then
        SimpleJwt restoredUpdatedJwt = sut.readJwtFile(jwtId);
        assertThat(restoredUpdatedJwt).isEqualTo(updatedJwt);
        assertThat(restoredUpdatedJwt).isNotEqualTo(originalJwt);
    }

    @Test
    public void createServiceTicketFileShouldWriteSuccessfully() throws IOException {
        // given
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt originalJwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);

        // when
        sut.createServiceTicketFile("ST-55-HqpNCMS1MO2enGkAqwMo-a20226e06c07", originalJwt);

        // then
        String expectedFileName = "ST-55-HqpNCMS1MO2enGkAqwMo-a20226e06c07";
        boolean actuallyStored = Files.exists(Paths.get(sessionStore + File.separator + expectedFileName));
        assertThat(actuallyStored).isTrue();
    }

    @Test
    public void readServiceTicketFileShouldReturnId() throws IOException {
        // given
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt originalJwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);
        String grantingTicket = "ST-55-HqpNCMS1MO2enGkAqwMo-a20226e06c07";
        sut.createServiceTicketFile(grantingTicket, originalJwt);
        assertThat(Files.exists(Paths.get(sessionStore + File.separator + grantingTicket))).isTrue();

        // when
        String actual = sut.readServiceTicketFile(grantingTicket);

        // then
        assertThat(actual).isEqualTo(originalJwt.getJwtId());
    }

    @Test(expected = IOException.class)
    public void readServiceTicketFileShouldThrowException() throws IOException {
        sut.readServiceTicketFile("banana");
    }
}
