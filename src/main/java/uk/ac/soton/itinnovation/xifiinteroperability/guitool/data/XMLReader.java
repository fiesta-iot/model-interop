/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2017
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
// Created By : Paul Grace
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data;

import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.InvalidXMLInputException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility class with static method to evaluata xpath expressions against
 * a given xml value.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public final class XMLReader {

    /**
     * Utility class, therefore implement a private constructor so class
     * cannot be instantiated.
     */
    private XMLReader() {
        // Empty implementation
    }

    /**
     * Read xml given the XML XPath expression.
     * @param xml The xml content to read the value from.
     * @param xpath A valid xpath expression.
     * @return The value of the xpath expression evaluated against the data.
     * @throws InvalidXMLInputException The error message identifying the input error.
     */
    public static String readXPATHValue(final String xml, final String xpath) throws InvalidXMLInputException {

        final DocumentBuilderFactory builderFactory =
                                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException pcExcep) {
            throw new InvalidXMLInputException("Error constructing the xml parser", pcExcep);
        }
        try {
            final Document xmlDocument = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            final XPath xPath =  XPathFactory.newInstance().newXPath();
            return xPath.compile(xpath).evaluate(xmlDocument);
        } catch (SAXException saxex) {
            throw new InvalidXMLInputException("Invalid XML data in the content parameter", saxex);
        } catch (XPathExpressionException xpathexcep) {
            throw new InvalidXMLInputException("Invalid XPath expression in xpath parameter", xpathexcep);
        } catch (IOException ioexcep) {
            throw new InvalidXMLInputException("Input or Output error", ioexcep);
        }
    }
}
