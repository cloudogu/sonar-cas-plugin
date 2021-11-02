package org.sonar.plugins.cas.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JwtFiles {

    private JwtFiles() {
    }

    /**
     * Reads a file from <code>filePath</code> and returns a SimpleJwt for it.
     *
     * @param filePath the path to a file which contains an XML representation of the object to be created. Must not
     *                 be <code>null</code>.
     * @return a SimpleJwt for the data in the given file path.
     * @throws JwtFileConversionException Throws an exception if the file cannot be parsed as the desired type or when
     *                                    there happens errors during I/O.
     */
    public static SimpleJwt unmarshal(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("FilePath must not be null.");
        }

        try (InputStream reader = Files.newInputStream(filePath)) {
            SimpleJwt unmarshalled = unmarshal(reader);
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

    protected static SimpleJwt unmarshal(InputStream input) {
        try {
            Element root = XMLParsing.getRootElementFromXML(input);
            String jwtId = XMLParsing.getContentForTagName(root, "jwtId");
            long expiration = Long.parseLong(XMLParsing.getContentForTagName(root, "expiration"));
            boolean invalid = Boolean.parseBoolean(XMLParsing.getContentForTagName(root, "invalid"));
            return new SimpleJwt(jwtId, expiration, invalid);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            String msg = "Cannot unmarshal input to an instance of " + SimpleJwt.class.getName();
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
    public static void marshalIntoNewFile(Path filePath, SimpleJwt jwt) {
        if (filePath == null) {
            throw new IllegalArgumentException("FilePath must not be null.");
        }
        if (jwt == null) {
            throw new IllegalArgumentException("JWT must not be null.");
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = transformerFactory.newTransformer();

            DocumentBuilder docBuilder = XMLParsing.createSecureBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("SimpleJwt");
            doc.appendChild(rootElement);

            Element id = doc.createElement("jwtId");
            id.appendChild(doc.createTextNode(jwt.getJwtId()));
            rootElement.appendChild(id);

            Element expiration = doc.createElement("expiration");
            expiration.appendChild(doc.createTextNode(Long.toString(jwt.getExpiration().getEpochSecond())));
            rootElement.appendChild(expiration);

            Element invalid = doc.createElement("invalid");
            invalid.appendChild(doc.createTextNode(Boolean.toString(jwt.isInvalid())));
            rootElement.appendChild(invalid);

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath.toString()));
            transformer.transform(source, result);
        } catch (Exception e) {
            String msg = "Cannot marshal object " + jwt + " into file " + filePath;
            throw new JwtFileConversionException(msg, e);
        }
    }

    private static class JwtFileConversionException extends RuntimeException {
        JwtFileConversionException(String msg, Exception e) {
            super(msg, e);
        }

        JwtFileConversionException(String msg) {
            super(msg);
        }
    }
}
