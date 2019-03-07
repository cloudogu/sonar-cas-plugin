package org.sonar.plugins.cas.util;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.fest.assertions.Assertions.assertThat;

public class JwtFilesTest {

    private Path sessionStore;

    @Before
    public void setUp() throws Exception {
        sessionStore = Files.createTempDirectory("sessionStore");
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(sessionStore.toFile());
    }

    @Test
    public void unmarshalReturnsParsedFile() {
        SimpleJwt jwt = SimpleJwt.fromIdAndExpiration("1234", 1L);
        Path newFile = sessionStore.resolve("1234");
        JwtFiles.marshalIntoNewFile(newFile, jwt);
        Path file = sessionStore.resolve("1234");

        // when
        SimpleJwt actual = JwtFiles.unmarshal(file);

        // then
        assertThat(actual).isEqualTo(jwt);
    }

    @Test(expected = RuntimeException.class)
    public void unmarshalThrowsExceptionOnBogusFile() throws IOException {
        Path bogusFile = sessionStore.resolve("bogus.xml");
        byte[] bogusInput = "<banana><potassium value=\"9001\" /></banana>".getBytes();
        Files.write(bogusFile, bogusInput);

        JwtFiles.unmarshal(bogusFile);
    }

    @Test
    public void marshalWritesFile() {
        SimpleJwt jwt = SimpleJwt.fromIdAndExpiration("1234", 1L);
        Path newFile = sessionStore.resolve("1234");

        JwtFiles.marshalIntoNewFile(newFile, jwt);

        Path expectedFile = sessionStore.resolve("1234");
        boolean exists = Files.exists(expectedFile);
        assertThat(exists).isTrue();
    }
}
