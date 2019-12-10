package org.sonar.plugins.cas.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class XMLParsing {
    private XMLParsing() {
    }

    public static DocumentBuilder createSecureBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder();
    }

    public static Element getRootElementFromXML(InputStream input) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = createSecureBuilder();
        Document document = builder.parse(input);
        document.getDocumentElement().normalize();
        return document.getDocumentElement();
    }

    public static String getContentForTagName(Element element, String tagName) {
        return element.getElementsByTagName(tagName).item(0).getTextContent();
    }
}
