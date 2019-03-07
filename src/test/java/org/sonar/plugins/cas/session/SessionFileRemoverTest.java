package org.sonar.plugins.cas.session;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.fest.assertions.Assertions.assertThat;

public class SessionFileRemoverTest {

    private static final String RANDOM_SERVICE_TICKET_PREFIX = "ST-1234567_";
    private Path sessionStore;
    private SessionFileRemover sut;

    @Before
    public void setUp() throws Exception {
        sessionStore = Files.createTempDirectory("sessionStore");
        String sessionStorePath = sessionStore.toString();
        sut = new SessionFileRemover(sessionStorePath);
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(sessionStore.toFile());
    }

    @Test
    public void cleanUpShouldRemoveJwtAndCorrespondingSTFiles() {
        long future = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtFile1 = "AWkLDwrSoTPaa1Du9LQ1";
        String jwtFile2 = "AWkLDwrSoTPaa1Du9LQ2";
        String jwtFile3 = "AWkLDwrSoTPaa1Du9LQ3";
        createJwtFiles(future, jwtFile1, jwtFile2, jwtFile3);

        long expired = Instant.now().minusSeconds(60).getEpochSecond();
        String old1 = "AWkLDwrSoTPaa1Du9LQ4";
        String old2 = "AWkLDwrSoTPaa1Du9LQ5";
        String old3 = "AWkLDwrSoTPaa1Du9LQ6";
        createJwtFiles(expired, old1, old2, old3);

        createServiceTicketFiles(jwtFile1, jwtFile2, jwtFile3, old1, old2, old3);

        // when
        sut.cleanUp();

        // then
        Collection<Path> actualServiceTicketFiles = sut.listAllServiceTickets();
        Path expectedST1 = createServiceTicketPath(jwtFile1);
        Path expectedST2 = createServiceTicketPath(jwtFile2);
        Path expectedST3 = createServiceTicketPath(jwtFile3);
        assertThat(actualServiceTicketFiles).containsOnly(expectedST1, expectedST2, expectedST3);

        Collection<Path> actualJwtFiles = sut.listAllJwtFiles();
        Path expectedJwt1 = sessionStore.resolve(jwtFile1);
        Path expectedJwt2 = sessionStore.resolve(jwtFile2);
        Path expectedJwt3 = sessionStore.resolve(jwtFile3);
        assertThat(actualJwtFiles).containsOnly(expectedJwt1, expectedJwt2, expectedJwt3);
    }

    @Test
    public void findServiceTicketsShouldIterateAllServiceTickets() throws IOException {
        createServiceTicketFiles("id1", "id2", "id3");
        Path path = sessionStore.resolve("theXFile");
        Files.write(path, "I want to believe".getBytes());
        assertThat(countExistingFiles()).isEqualTo(3 + 1);

        Collection<Path> files = sut.listAllServiceTickets();

        Path expected1 = createServiceTicketPath("id1");
        Path expected2 = createServiceTicketPath("id2");
        Path expected3 = createServiceTicketPath("id3");
        assertThat(files).containsOnly(expected1, expected2, expected3);
    }

    @Test
    public void findServiceTicketsShouldIterateOnlyServiceTickets() {
        createServiceTicketFiles("id1", "id2", "id3");
        assertThat(countExistingFiles()).isEqualTo(3);

        Collection<Path> files = sut.listAllServiceTickets();

        Path expected1 = createServiceTicketPath("id1");
        Path expected2 = createServiceTicketPath("id2");
        Path expected3 = createServiceTicketPath("id3");
        assertThat(files).contains(expected1, expected2, expected3);
    }

    @Test
    public void removeServiceTicketsShouldRemoveFile() {
        createServiceTicketFiles("id1", "id2", "id3");

        sut.removeServiceTickets(RANDOM_SERVICE_TICKET_PREFIX + "id2");

        Collection<Path> files = sut.listAllServiceTickets();
        Path expected1 = createServiceTicketPath("id1");
        Path expected3 = createServiceTicketPath("id3");
        assertThat(files).containsOnly(expected1, expected3);
    }

    private Path createServiceTicketPath(String id1) {
        return sessionStore.resolve(RANDOM_SERVICE_TICKET_PREFIX + id1);
    }

    @Test
    public void removeServiceTicketsShouldNotThrowExceptionWhenFileNotFound() {
        createServiceTicketFiles("id1", "id2", "id3");

        sut.removeServiceTickets(RANDOM_SERVICE_TICKET_PREFIX + "okthisisweird");

        Collection<Path> files = sut.listAllServiceTickets();
        Path expected1 = createServiceTicketPath("id1");
        Path expected2 = createServiceTicketPath("id2");
        Path expected3 = createServiceTicketPath("id3");
        assertThat(files).containsOnly(expected1, expected2, expected3);
    }

    @Test
    public void removeServiceTicketsShouldRemoveSeveralFilesAtOnce() {
        createServiceTicketFiles("id1", "id2", "id3");

        sut.removeServiceTickets(RANDOM_SERVICE_TICKET_PREFIX + "id2", RANDOM_SERVICE_TICKET_PREFIX + "id3");

        Collection<Path> files = sut.listAllServiceTickets();
        Path expected1 = createServiceTicketPath("id1");
        assertThat(files).containsOnly(expected1);
    }

    @Test
    public void listJwtFilesShouldListFilesMatchingAPattern() {
        long now = Instant.now().getEpochSecond();
        String id1 = "AWkLDwrSoTPaa1Du9LQ1";
        String id2 = "AWkLDwrSoTPaa1Du9LQ2";
        String id3 = "AWkLDwrSoTPaa1Du9LQ3";
        createJwtFiles(now, id1, id2, id3);
        assertThat(countExistingFiles()).isEqualTo(3);

        Collection<Path> actual = sut.listAllJwtFiles();

        Path expected1 = sessionStore.resolve(id1);
        Path expected2 = sessionStore.resolve(id2);
        Path expected3 = sessionStore.resolve(id3);
        assertThat(actual).containsOnly(expected1, expected2, expected3);
    }

    @Test
    public void listJwtFilesShouldNotListServiceTickets() {
        long now = Instant.now().getEpochSecond();
        String id1 = "AWkLDwrSoTPaa1Du9LQ1";
        String id2 = "AWkLDwrSoTPaa1Du9LQ2";
        String id3 = "AWkLDwrSoTPaa1Du9LQ3";
        createJwtFiles(now, id1, id2, id3);
        createServiceTicketFiles("id4", "id5", "id6");
        assertThat(countExistingFiles()).isEqualTo(3 + 3);

        Collection<Path> actual = sut.listAllJwtFiles();

        Path expected1 = sessionStore.resolve(id1);
        Path expected2 = sessionStore.resolve(id2);
        Path expected3 = sessionStore.resolve(id3);
        assertThat(actual).containsOnly(expected1, expected2, expected3);
    }

    @Test
    public void listJwtFilesShouldNotListSimilarUnknownFiles() {
        long now = Instant.now().getEpochSecond();
        String tooLong = "AWkLDwrSoTPaa1Du9LQ191aZzdhnaskdu27283jd";
        String id1ShouldMatch = "AWkLDwrSoTPaa1Du9LQ1";
        String oneCharTooShort = "AWkLDwrSoTPaa1Du9LQ";
        String wayToShort = "A";
        createJwtFiles(now, tooLong, id1ShouldMatch, oneCharTooShort, wayToShort);
        createServiceTicketFiles("id4", "id5", "id6");
        assertThat(countExistingFiles()).isEqualTo(4 + 3);

        Collection<Path> actual = sut.listAllJwtFiles();

        Path expected1 = sessionStore.resolve(id1ShouldMatch);
        assertThat(actual).containsOnly(expected1);
    }

    @Test
    public void parseJwtFilesShouldReturnJwts() {
        long now = Instant.now().getEpochSecond();
        String jwtFile1 = "AWkLDwrSoTPaa1Du9LQ1";
        String jwtFile2 = "AWkLDwrSoTPaa1Du9LQ2";
        String jwtFile3 = "AWkLDwrSoTPaa1Du9LQ3";
        createJwtFiles(now, jwtFile1, jwtFile2, jwtFile3);
        Path inputFile1 = sessionStore.resolve(jwtFile1);
        Path inputFile2 = sessionStore.resolve(jwtFile2);
        Path inputFile3 = sessionStore.resolve(jwtFile3);
        Collection<Path> foundFiles = Arrays.asList(inputFile1, inputFile2, inputFile3);

        List<SimpleJwt> actual = sut.parseJwtFiles(foundFiles);

        List<SimpleJwt> expected = createJwts(now, jwtFile1, jwtFile2, jwtFile3);
        assertThat(actual.toArray()).containsOnly(expected.toArray());
    }

    @Test
    public void findExpiredJwtsShouldReturnOnlyExpiredJwts() {
        long future = Instant.now().plusSeconds(60).getEpochSecond();
        String jwtFile1 = "AWkLDwrSoTPaa1Du9LQ1";
        String jwtFile2 = "AWkLDwrSoTPaa1Du9LQ2";
        String jwtFile3 = "AWkLDwrSoTPaa1Du9LQ3";
        createJwtFiles(future, jwtFile1, jwtFile2, jwtFile3);

        long expired = Instant.now().minusSeconds(60).getEpochSecond();
        String old1 = "AWkLDwrSoTPaa1Du9LQ4";
        String old2 = "AWkLDwrSoTPaa1Du9LQ5";
        String old3 = "AWkLDwrSoTPaa1Du9LQ6";
        createJwtFiles(expired, old1, old2, old3);

        Path inputFile1 = sessionStore.resolve(jwtFile1);
        Path inputFile2 = sessionStore.resolve(jwtFile2);
        Path inputFile3 = sessionStore.resolve(jwtFile3);
        Path oldFile1 = sessionStore.resolve(old1);
        Path oldFile2 = sessionStore.resolve(old2);
        Path oldFile3 = sessionStore.resolve(old3);
        Collection<Path> foundFiles = Arrays.asList(inputFile1, inputFile2, inputFile3, oldFile1, oldFile2, oldFile3);
        List<SimpleJwt> unfiltered = sut.parseJwtFiles(foundFiles);

        List<SimpleJwt> actual = sut.findExpiredJwts(unfiltered);

        List<SimpleJwt> expected = createJwts(expired, old1, old2, old3);
        assertThat(actual.toArray()).containsOnly(expected.toArray());
    }

    @Test
    public void convertJwtsToIdsShouldReturnListOfIds() {
        long now = Instant.now().getEpochSecond();
        List<SimpleJwt> input = createJwts(now, "id1", "id2");

        List<String> actual = sut.convertJwtsToIds(input);

        assertThat(actual).containsExactly("id1", "id2");
    }

    @Test
    public void convertJwtsToIdsShouldReturnEmptyList() {
        List<String> actual = sut.convertJwtsToIds(emptyList());

        assertThat(actual).isEmpty();
    }

    @Test
    public void listServiceTicketsToBeRemovedShouldReturnMatchingTicketFiles() {
        long timeHasNoMatter = Instant.now().getEpochSecond();
        String jwtFile1 = "AWkLDwrSoTPaa1Du9LQ1";
        String jwtFile2 = "AWkLDwrSoTPaa1Du9LQ2";
        String jwtFile3 = "AWkLDwrSoTPaa1Du9LQ3";
        String jwtDontFindMe = "AWkLDwrSoTPaa1Du9LQ4";
        createJwtFiles(timeHasNoMatter, jwtFile1, jwtFile2, jwtFile3, jwtDontFindMe);
        createServiceTicketFiles(jwtFile1, jwtFile2, jwtFile3, jwtDontFindMe);
        List<String> expiredJwtIds = Arrays.asList(jwtFile1, jwtFile2, jwtFile3);

        Collection<Path> actualFiles = sut.findServiceTicketFilesByJwtId(expiredJwtIds);

        Path expected1 = sessionStore.resolve(RANDOM_SERVICE_TICKET_PREFIX + jwtFile1);
        Path expected2 = sessionStore.resolve(RANDOM_SERVICE_TICKET_PREFIX + jwtFile2);
        Path expected3 = sessionStore.resolve(RANDOM_SERVICE_TICKET_PREFIX + jwtFile3);
        assertThat(actualFiles).containsOnly(expected1, expected2, expected3);
    }

    @Test
    public void readingExceptionShouldNotThrowException() throws IOException {
        long timeHasNoMatter = Instant.now().getEpochSecond();
        String jwtFile1 = "AWkLDwrSoTPaa1Du9LQ1";
        String jwtFile2 = "AWkLDwrSoTPaa1Du9LQ2";
        String jwtFile3 = "AWkLDwrSoTPaa1Du9LQ3";
        Collection<Path> existingFiles = createJwtFiles(timeHasNoMatter, jwtFile1, jwtFile2, jwtFile3);
        Path bogusFile = sessionStore.resolve("AWkLDwrSoTPaa1Du9LQ4");
        byte[] bogusInput = "<banana><potassium value=\"9001\" /></banana>".getBytes();
        Files.write(bogusFile, bogusInput);
        ArrayList<Path> filesList = new ArrayList<>();
        filesList.add(bogusFile); // first file produces an exception
        filesList.addAll(existingFiles);

        // when
        List<SimpleJwt> actualJwts = sut.parseJwtFiles(filesList);

        // then no exception and correct data
        List<SimpleJwt> expected = createJwts(timeHasNoMatter, jwtFile1, jwtFile2, jwtFile3);
        assertThat(actualJwts.toArray()).containsOnly(expected.toArray());
    }

    // utility methods

    private long countExistingFiles() {
        try {
            return Files.list(sessionStore).count();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<Path> createJwtFiles(long expirationDate, String... jwtIds) {
        Collection<Path> createdJwtPaths = new ArrayList<>();

        try {
            for (String id : jwtIds) {
                SessionFileHandler fileHandler = new SessionFileHandler(sessionStore.toString());
                fileHandler.writeJwtFile(id, SimpleJwt.fromIdAndExpiration(id, expirationDate));

                createdJwtPaths.add(sessionStore.resolve(id));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return createdJwtPaths;
    }

    private List<SimpleJwt> createJwts(long expirationDate, String... jwtIds) {
        List<SimpleJwt> list = new ArrayList<>();
        for (String id : jwtIds) {
            list.add(SimpleJwt.fromIdAndExpiration(id, expirationDate));
        }

        return list;
    }

    private void createServiceTicketFiles(String... fileSuffices) {
        try {
            for (String fileSuffix : fileSuffices) {
                String filename = RANDOM_SERVICE_TICKET_PREFIX + fileSuffix;
                byte[] jwtIdBytes = fileSuffix.getBytes();
                Path path = sessionStore.resolve(filename);
                Files.write(path, jwtIdBytes);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}