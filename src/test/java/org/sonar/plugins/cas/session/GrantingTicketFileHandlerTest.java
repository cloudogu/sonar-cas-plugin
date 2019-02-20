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

public class GrantingTicketFileHandlerTest {


    private Path sessionStore;
    private GrantingTicketFileHandler sut;

    @Before
    public void setUp() throws Exception {
        sessionStore = Files.createTempDirectory("sessionStore");
        sut = new GrantingTicketFileHandler(sessionStore.toString());
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(sessionStore.toFile());
    }

    @Test
    public void store() throws IOException {
        // given
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt originalJwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);

        // when
        sut.store("ST-55-HqpNCMS1MO2enGkAqwMo-a20226e06c07", originalJwt);

        // then
        String expectedFileName = "ST-55-HqpNCMS1MO2enGkAqwMo-a20226e06c07";
        boolean actuallyStored = Files.exists(Paths.get(sessionStore + File.separator + expectedFileName));
        assertThat(actuallyStored).isTrue();
    }

    @Test
    public void getShouldReturnId() throws IOException {
        // given
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt originalJwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);
        String grantingTicket = "ST-55-HqpNCMS1MO2enGkAqwMo-a20226e06c07";
        sut.store(grantingTicket, originalJwt);
        assertThat(Files.exists(Paths.get(sessionStore + File.separator + grantingTicket))).isTrue();

        // when
        String actual = sut.get(grantingTicket);

        // then
        assertThat(actual).isEqualTo(originalJwt.getJwtId());
    }

    @Test(expected = IOException.class)
    public void getShouldThrowException() throws IOException {
        sut.get("banana");
    }
}
