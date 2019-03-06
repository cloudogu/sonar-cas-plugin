package org.sonar.plugins.cas.util;

import javax.xml.bind.JAXB;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JwtFileUtil {
    private static final Charset CONTENT_CHARSET = StandardCharsets.US_ASCII;

    /**
     * Reads a file from <code>filePath</code> and returns a SimpleJwt for it.
     *
     * @param filePath the path to a file which contains an XML representation of the object to be created. Must not
     *                 be <code>null</code>.
     * @return a SimpleJwt for the data in the given file path.
     * @throws JwtFileConversionException Throws an exception if the file cannot be parsed as the desired type or when
     *                                    there happens errors during I/O.
     */
    public SimpleJwt unmarshal(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("FilePath must not be null.");
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, CONTENT_CHARSET)) {
            SimpleJwt unmarshalled = JAXB.unmarshal(reader, SimpleJwt.class);
            if (unmarshalled.getJwtId() == null) {
                String msg = "Cannot unmarshal path " + filePath + " to an instance of " +
                        SimpleJwt.class.getSimpleName() + ". The file does not seem to contain valid JWT data.";
                throw new JwtFileConversionException(msg);
            }
            return unmarshalled;
        } catch (Exception e) {
            String msg = "Cannot unmarshal path " + filePath + " to an instance of " + SimpleJwt.class.getName();
            throw new JwtFileConversionException(msg, e);
        }
    }

    /**
     * Writes a file from <code>jaxbObject</code> into the path <code>filePath</code>.
     *
     * @param filePath the path to a file which shall receive an XML representation of the object to be created. Must
     *                 not be <code>null</code>.
     * @param jwt      the object whose data is supposed to be serialized into an XML file. Must not be
     *                 <code>null</code>.
     * @throws JwtFileConversionException Throws an exception if the object cannot be parsed into the desired type or
     *                                    when there happens errors during I/O.
     */
    public void marshalIntoNewFile(Path filePath, SimpleJwt jwt) {
        if (filePath == null) {
            throw new IllegalArgumentException("FilePath must not be null.");
        }
        if (jwt == null) {
            throw new IllegalArgumentException("JWT must not be null.");
        }

        try (BufferedWriter writer = createWriterForNewFile(filePath)) {
            JAXB.marshal(jwt, writer);
            writer.flush();
        } catch (Exception e) {
            String msg = "Cannot marshal object " + jwt + " into file " + filePath;
            throw new JwtFileConversionException(msg, e);
        }
    }

    private BufferedWriter createWriterForNewFile(Path pathToNewFile) throws IOException {
        Path newFile = Files.createFile(pathToNewFile);
        return Files.newBufferedWriter(newFile, CONTENT_CHARSET);
    }

    private class JwtFileConversionException extends RuntimeException {
        JwtFileConversionException(String msg, Exception e) {
            super(msg, e);
        }

        JwtFileConversionException(String msg) {
            super(msg);
        }
    }
}
