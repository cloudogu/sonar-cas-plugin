package org.sonar.plugins.cas.session;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.Comparator;

import static org.junit.Assert.*;

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

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    public void isJwtStoredReturnsFalseForMissingFile() {
        boolean actual = sut.isJwtStored("1234");

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    public void storeShouldStoreNewJwtFile() throws IOException {
        long expiryDateIn60SecondsTime = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtId = "AWjne4xYY4T-z3CxdIRY";
        SimpleJwt jwt = SimpleJwt.fromIdAndExpiration(jwtId, expiryDateIn60SecondsTime);

        sut.store(jwtId, jwt);
        boolean actuallyStored = Files.exists(Paths.get(sessionStore + File.separator + jwtId));


        Assertions.assertThat(actuallyStored).isTrue();
    }
}