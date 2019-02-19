package org.sonar.plugins.cas.session;

import org.apache.commons.io.FileUtils;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;

import static org.junit.Assert.*;

public class JwtTokenFileHandlerTest {

    private Path sessionStore;

    @Before
    public void setUp() throws Exception {
        sessionStore = Files.createTempDirectory("sessionStore");
        System.out.println("Create temp dir " + sessionStore.toString());
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
}