package org.sonar.plugins.cas.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.cas.util.JwtFileUtil;
import org.sonar.plugins.cas.util.SimpleJwt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class cleans up expired files created by the {@link FileSessionStore}.
 *
 * <p>For each log-in the FileSessionStore
 * creates both an JWT file and a Service Ticket file in order to manage the session. Once the JWT session is expired
 * (which is usually longer than the actual log-in duration) both files must be removed for house keeping reasons.
 * </p>
 */
class SessionFileRemover {
    private static final Logger LOG = LoggerFactory.getLogger(SessionFileRemover.class);
    private static final String SERVICE_TICKET_PREFIX = "ST-";
    private static final String JWT_FILE_REGEX = "[a-zA-Z0-9]{20}";
    private static final Pattern JWT_FILE_PATTERN = Pattern.compile(JWT_FILE_REGEX);
    private String sessionStorePath;

    SessionFileRemover(String sessionStorePath) {
        this.sessionStorePath = sessionStorePath;
    }

    /**
     * Finds expired JWTs and removes associated JWT and Service Ticket files.
     * @return the number of actually removed files for both JWT and service ticket files.
     */
    int cleanUp() {
    // TODO add batch sizes for chunk-wise processing
        List<String> expiredJwtIds = findExpiredJwts();

        Collection<Path> candidatesForRemoval = findFilesToBeRemoved(expiredJwtIds);

        return removeFile(candidatesForRemoval);
    }

    private List<String> findExpiredJwts() {
        Collection<Path> allJwtFiles = listAllJwtFiles();
        // makes sure we really have JWTs at hand and don't delete anything else
        List<SimpleJwt> jwts = parseJwtFiles(allJwtFiles);

        jwts = findExpiredJwts(jwts);

        return convertJwtsToIds(jwts);
    }

    private int removeFile(Collection<Path> candidatesForRemoval) {
        int removalCounter = 0;
        for (Path path : candidatesForRemoval) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                LOG.error("Could not delete file.", e);
            }
            ++removalCounter;
        }

        return removalCounter;
    }

    private Collection<Path> findFilesToBeRemoved(List<String> expiredJwtIds) {
        int expectedJwtAndServiceTicketCount = expiredJwtIds.size() * 2;
        List<Path> candidatesForRemoval = new ArrayList<>(expectedJwtAndServiceTicketCount);

        List<Path> jwtFiles = convertJwtIdsToPaths(expiredJwtIds);
        candidatesForRemoval.addAll(jwtFiles);

        Collection<Path> ticketFiles = findServiceTicketFilesByJwtId(expiredJwtIds);
        candidatesForRemoval.addAll(ticketFiles);

        return candidatesForRemoval;
    }

    List<String> convertJwtsToIds(List<SimpleJwt> jwts) {
        return jwts.stream()
                .map(SimpleJwt::getJwtId)
                .collect(Collectors.toList());
    }

    private List<Path> convertJwtIdsToPaths(List<String> expiredJwtIds) {
        // from earlier listing and parsing we know these expired files exist
        return expiredJwtIds.stream()
                .map(expiredId -> Paths.get(sessionStorePath, expiredId))
                .collect(Collectors.toList());
    }

    Collection<Path> findServiceTicketFilesByJwtId(List<String> expiredJwtIds) {
        Collection<Path> allServiceTickets = listAllServiceTickets();

        return filterServiceTicketsWithJwtIds(allServiceTickets, expiredJwtIds);
    }

    private Collection<Path> filterServiceTicketsWithJwtIds(Collection<Path> serviceTicketFiles, List<String> expiredJwtIds) {
        List<Path> ticketsToBeRemoved = new ArrayList<>();
        List<String> expiredJwtIdsCopy = new ArrayList<>(expiredJwtIds);

        for (Path ticketFile : serviceTicketFiles) {
            filterServiceTicketWithJwtIds(ticketsToBeRemoved, expiredJwtIdsCopy, ticketFile);
        }

        return ticketsToBeRemoved;
    }

    private void filterServiceTicketWithJwtIds(List<Path> ticketsToBeRemoved, List<String> jwtIds, Path ticketFile) {
        String ticketFileRaw = ticketFile.toString();
        String jwtIdToBeCompared;

        try {
            byte[] fileContent;
            fileContent = Files.readAllBytes(ticketFile);
            jwtIdToBeCompared = new String(fileContent, StandardCharsets.US_ASCII).trim();
        } catch (IOException e) {
            LOG.error("Could not filter Service Ticket " + ticketFileRaw + " for removal", e);
            return;
        }

        if (jwtIds.contains(jwtIdToBeCompared)) {
            LOG.debug("Found Service Ticket {} for removal", ticketFileRaw);
            ticketsToBeRemoved.add(ticketFile);
            // reduce search complexity
            jwtIds.remove(jwtIdToBeCompared);
        }
    }

    Collection<Path> listAllServiceTickets() {
        Stream<Path> list = listSessionStorePath();
        return list.filter(file -> file.getFileName().toString().startsWith(SERVICE_TICKET_PREFIX))
                .collect(Collectors.toList());
    }

    private Stream<Path> listSessionStorePath() {
        Path dir = Paths.get(sessionStorePath);
        try {
            return Files.list(dir);
        } catch (IOException e) {
            throw new RuntimeException("Could not list files in CAS file session writeJwtFile", e);
        }
    }

    void removeServiceTickets(String... serviceTicketFileNames) {
        for (String serviceTicketFileName : serviceTicketFileNames) {
            removeServiceTicket(serviceTicketFileName);
        }
    }

    private void removeServiceTicket(String serviceTicketFileName) {
        try {
            Path file = Paths.get(sessionStorePath, serviceTicketFileName);
            Files.delete(file);
        } catch (IOException e) {
            LOG.error("Could not delete Service Ticket file from file session writeJwtFile", e);
        }
    }

    Collection<Path> listAllJwtFiles() {
        return listSessionStorePath()
                .filter(file -> JWT_FILE_PATTERN.matcher(file.getFileName().toString()).matches())
                .collect(Collectors.toList());
    }

    List<SimpleJwt> parseJwtFiles(Collection<Path> foundFiles) {
        List<SimpleJwt> list = new ArrayList<>(foundFiles.size());

        for (Path file : foundFiles) {
            try {
                SimpleJwt jwt = new JwtFileUtil().unmarshal(file);
                list.add(jwt);
            } catch(Exception e) {
                LOG.error("Could not parse JWT file.", e);
            }
        }
        return list;
    }

    List<SimpleJwt> findExpiredJwts(List<SimpleJwt> unfiltered) {
        return unfiltered.stream()
                .filter(SimpleJwt::isExpired)
                .collect(Collectors.toList());
    }
}
