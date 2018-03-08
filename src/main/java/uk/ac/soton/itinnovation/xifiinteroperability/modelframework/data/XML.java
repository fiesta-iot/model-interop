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

package uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data.PathEvaluationResult.DataFormat;

/**
 * Operations for evaluating XML data elements. These are typically applied
 * to assess <Guard> statements of patterns for HTTP responses with XML
 * message bodies.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public final class XML {

    /**
     * Utility class, hence use a private constructor.
     */
    private XML() {
        // no implementation required.
    }

    /**
     * XPATH based method to assert that particular expressions in an
     * XML data structure e.g. /Resp/Address/Street == Main St. Given an XML
     * doc and an expression, does this match the given value?
     *
     * @param xmlDoc The xml content to apply an XPATH expression to
     * @param reference The XPATH reference expression to evaluate
     * @param value The value to compare against
     * @return PathEvaluationResult with the boolean result and the value of the XPath expression
     * @throws InvalidXPathException Thrown in case of an invalid XPath in a guard.
     */
    public static PathEvaluationResult xmlAssert(final String xmlDoc, final String reference, final Object value)
            throws InvalidXPathException {
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
            domFactory.setNamespaceAware(true);
            final DocumentBuilder builder = domFactory.newDocumentBuilder();
            final InputSource source = new InputSource(new StringReader(xmlDoc));
            final Document doc = builder.parse(source);
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression expr = xpath.compile(reference);
            final boolean xPathExist = (boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
            if (!xPathExist){
                throw new InvalidXPathException("XPath '" + reference + "' is invalid or does not exist.");
            }
            final Object result = expr.evaluate(doc);
            return new PathEvaluationResult(result.equals(value.toString()), result, DataFormat.XML);
        } catch (SAXException ex) {
            ServiceLogger.LOG.error("Error parsing the xml document", ex);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Error buffering the xml string data", ex);
        } catch (ParserConfigurationException ex) {
            ServiceLogger.LOG.error("Error configuring the xml parser", ex);
        } catch (XPathExpressionException ex) {
            ServiceLogger.LOG.error("Error with invalid xml xpath expression", ex);
            throw new InvalidXPathException("XPath '" + reference + "' is invalid or does not exist.");
        }
        return new PathEvaluationResult(false, null, DataFormat.XML);
    }

    /**
     * XPATH based method to compare an expression in an XML data structure against
     * a particular value e.g. /Resp/Address/Street/Number > 0 or /Resp/Address/Street/Number < 15.
     * LESSTHAN or GREATERTHAN comparisons only, use xmlAssert for EQUAL and NOTEQUAL comparisons
     *
     * @param xmlDoc The xml content to apply an XPATH expression to
     * @param reference The XPATH reference expression to evaluate
     * @param value The value to compare against
     * @param comparisonType The type of the comparison, GREATERTHAN or LESSTHAN
     * @return PathEvaluationResult with the boolean result and the value of the XPath expression
     * @throws InvalidXPathException Thrown in case of an invalid XPath in a guard.
     */
    public static PathEvaluationResult xmlCompare(final String xmlDoc, final String reference, final Object value,
            final Guard.ComparisonType comparisonType) throws InvalidXPathException {
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
            domFactory.setNamespaceAware(true);
            final DocumentBuilder builder = domFactory.newDocumentBuilder();
            final InputSource source = new InputSource(new StringReader(xmlDoc));
            final Document doc = builder.parse(source);
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression expr = xpath.compile(reference);
            final boolean xPathExist = (boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
            if (!xPathExist){
                throw new InvalidXPathException("XPath '" + reference + "' is invalid or does not exist.");
            }
            final Object result = expr.evaluate(doc);
            if (comparisonType == Guard.ComparisonType.GREATERTHAN){
                try{
                    double a = new Double(result.toString());
                    double b = new Double(value.toString());
                    return new PathEvaluationResult(a > b, result, DataFormat.XML);
                } catch(Exception ex) {
                    return new PathEvaluationResult(false, result, DataFormat.XML);
                }
            }
            else if (comparisonType == Guard.ComparisonType.LESSTHAN){
                try{
                    double a = new Double(result.toString());
                    double b = new Double(value.toString());
                    return new PathEvaluationResult(a < b, result, DataFormat.XML);
                } catch(Exception ex) {
                    return new PathEvaluationResult(false, result, DataFormat.XML);
                }
            }
        } catch (SAXException ex) {
            ServiceLogger.LOG.error("Error parsing the xml document", ex);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Error buffering the xml string data", ex);
        } catch (ParserConfigurationException ex) {
            ServiceLogger.LOG.error("Error configuring the xml parser", ex);
        } catch (XPathExpressionException ex) {
            ServiceLogger.LOG.error("Error with invalid xml xpath expression", ex);
            throw new InvalidXPathException("XPath '" + reference + "' is invalid or does not exist.");
        }
        return new PathEvaluationResult(false, null, DataFormat.XML);
    }

    /**
     * XPATH based method to check if the value of an expression in an XML data structure
     * matches a given regular expression
     * REGEX comparison
     *
     * @param xmlDoc The xml content to apply an XPATH expression to
     * @param reference the XPATH reference expression to evaluate
     * @param value the regex to match against
     * @return the evaluation result and the value of the XPath expression
     * @throws InvalidXPathException Thrown in case of an invalid XPath in guard
     * @throws InvalidRegexException Thrown in case of an invalid regex in guard
     */
    public static PathEvaluationResult xmlRegex(final String xmlDoc, final String reference, final Object value)
            throws InvalidXPathException, InvalidRegexException {
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
            domFactory.setNamespaceAware(true);
            final DocumentBuilder builder = domFactory.newDocumentBuilder();
            final InputSource source = new InputSource(new StringReader(xmlDoc));
            final Document doc = builder.parse(source);
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression expr = xpath.compile(reference);
            final boolean xPathExist = (boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
            if (!xPathExist){
                throw new InvalidXPathException("XPath '" + reference + "' is invalid or does not exist.");
            }
            final Object result = expr.evaluate(doc);
            boolean boolResult = Pattern.matches(value.toString(), result.toString());
            return new PathEvaluationResult(boolResult, result, DataFormat.XML);
        } catch (SAXException ex) {
            ServiceLogger.LOG.error("Error parsing the xml document", ex);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Error buffering the xml string data", ex);
        } catch (ParserConfigurationException ex) {
            ServiceLogger.LOG.error("Error configuring the xml parser", ex);
        } catch (XPathExpressionException ex) {
            ServiceLogger.LOG.error("Error with invalid xml xpath expression", ex);
            throw new InvalidXPathException("XPath '" + reference + "' is invalid or does not exist.");
        } catch (PatternSyntaxException ex) {
            ServiceLogger.LOG.error("Error with invalid regular expression", ex);
            throw new InvalidRegexException("There is a regex guard with an invalid regular expression.");
        }
        return new PathEvaluationResult(false, null, DataFormat.XML);
    }

    /**
     * XPATH based method to check if an expression in an XML data structure
     * contains a particular node (field), .e.g //bookstore/book contains title
     * CONTAINS evaluation
     *
     * @param xmlDoc The xml content to apply an XPATH expression to
     * @param reference The XPATH reference expression to evaluate
     * @param value The value to compare against
     * @return PathEvaluationResult with the boolean result and the fields of the XPath expression (array list)
     * @throws InvalidXPathException Thrown in case of an invalid XPath in a guard.
     */
    public static PathEvaluationResult xmlContains(final String xmlDoc, final String reference, final Object value)
            throws InvalidXPathException {
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
            domFactory.setNamespaceAware(true);
            final DocumentBuilder builder = domFactory.newDocumentBuilder();
            final InputSource source = new InputSource(new StringReader(xmlDoc));
            final Document doc = builder.parse(source);
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression expr = xpath.compile(reference);
            final boolean xPathExist = (boolean) expr.evaluate(doc, XPathConstants.BOOLEAN);
            if (!xPathExist){
                throw new InvalidXPathException("XPath '" + reference + "' is invalid or does not exist.");
            }

            final Node resultNode = (Node) expr.evaluate(doc, XPathConstants.NODE);
            final NodeList resultNodeList = (NodeList) resultNode.getChildNodes();

            boolean containsResult = false;
            List<Node> elementNodesList = new ArrayList<>();
            /* the set is used to avoid duplicate child fields*/
            Set<String> elementNodesSet = new HashSet<>();
            for (int i=0; i<resultNodeList.getLength(); i++){
                if (resultNodeList.item(i).getNodeType() == Node.ELEMENT_NODE){
                    if (elementNodesSet.add(resultNodeList.item(i).getNodeName())){
                        elementNodesList.add(resultNodeList.item(i));

                        if (resultNodeList.item(i).getNodeName().equals(value.toString())){
                            containsResult = true;
                        }
                    }
                }
            }
            return new PathEvaluationResult(containsResult, elementNodesList, DataFormat.XML);
        } catch (SAXException ex) {
            ServiceLogger.LOG.error("Error parsing the xml document", ex);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Error buffering the xml string data", ex);
        } catch (ParserConfigurationException ex) {
            ServiceLogger.LOG.error("Error configuring the xml parser", ex);
        } catch (XPathExpressionException ex) {
            ServiceLogger.LOG.error("Error with invalid xml xpath expression", ex);
            throw new InvalidXPathException("XPath '" + reference + "' is invalid or does not exist.");
        }
        return new PathEvaluationResult(false, null, DataFormat.XML);
    }


    /**
     * Validate and xml document against the xml schema; throw exceptions
     * when the schema doesn't match.
     * @param xml The xml document to check.
     * @param schema The schema to test against.
     * @throws SAXException Error stating that it doesn't match.
     * @throws IOException Error during the processing of the operation.
     */
    private static void localValidate(final String xml, final Schema schema)
            throws SAXException, IOException {
        final StreamSource source = new StreamSource(new StringReader(xml));
        final Validator validator = schema.newValidator();
        validator.validate(source);
    }

    public static int getArraySize(final String xmlDoc, final String reference) {
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
            domFactory.setNamespaceAware(true);
            final DocumentBuilder builder = domFactory.newDocumentBuilder();
            final InputSource source = new InputSource(new StringReader(xmlDoc));
            final Document doc = builder.parse(source);
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression expr = xpath.compile(reference);


            Object result = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList products = (NodeList) result;
            return products.getLength();

        } catch (SAXException ex) {
            ServiceLogger.LOG.error("Error parsing the xml document", ex);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Error buffering the xml string data", ex);
        } catch (ParserConfigurationException ex) {
            ServiceLogger.LOG.error("Error configuring the xml parser", ex);
        } catch (XPathExpressionException ex) {
        }
        return 0;
    }

    /**
     * Public operation to check if an xml document (as a string) matches
     * a schema given at a URL.
     * @param xmlDoc The xml document to test.
     * @param schemaFile The URL location of the schema.
     * @return true if the doc conforms to the schema, false otherwise.
     */
    public static boolean xmlValidate(final String xmlDoc, final URL schemaFile) {
        final SchemaFactory schemaFactory = SchemaFactory
            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            final Schema schema = schemaFactory.newSchema(schemaFile);
            localValidate(xmlDoc, schema);
            return true;
        } catch (SAXException e) {
            ServiceLogger.LOG.error("Invalid Schema", e);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Cannot read schema", ex);
        }
        return false;
    }

    /**
     * Public operation to check if an xml document (as a string) matches
     * a schema given as a string.
     * @param xmlDoc The xml document to test.
     * @param schemaIn The complete schema in a string.
     * @return true if the doc conforms to the schema, false otherwise.
     */
    public static boolean xmlValidate(final String xmlDoc, final String schemaIn) {
        final StreamSource schemaFile = new StreamSource(new StringReader(schemaIn));
        final SchemaFactory schemaFactory = SchemaFactory
            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            final Schema schema = schemaFactory.newSchema(schemaFile);
            localValidate(xmlDoc, schema);
            return true;
        } catch (SAXException e) {
           ServiceLogger.LOG.error("Invalid Schema", e);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Unable to read schema", ex);
        }
        return false;
    }

    /**
     * Given an xpath expression: read that value from an xml string.
     * @param xmlDoc The xml string to read from.
     * @param pathexpr The xpath expression.
     * @return The xml value read.
     */
    public static String readValue(final String xmlDoc, final String pathexpr) {
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory
                    .newInstance();
            domFactory.setNamespaceAware(true);
            final DocumentBuilder builder = domFactory.newDocumentBuilder();
            final InputSource source = new InputSource(new StringReader(xmlDoc));
            final Document doc = builder.parse(source);
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression expr = xpath.compile(pathexpr);
            return expr.evaluate(doc);
        } catch (SAXException ex) {
            ServiceLogger.LOG.error("Error parsing the xml string", ex);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Error reading the xml into buffer", ex);
        } catch (ParserConfigurationException ex) {
            ServiceLogger.LOG.error("Error configuring the parser", ex);
        } catch (XPathExpressionException ex) {
            ServiceLogger.LOG.error("Invalid XML XPATH check", ex);
        }
        return null;
    }

    /**
     * Write a value in the xml string at a given xpath expression.
     * @param xmlDoc The document to write in to.
     * @param pathexpr The xpath expression.
     * @param val The value to write.
     * @return The edited xml document.
     */
    public static String writeValue(final String xmlDoc, final String pathexpr, final String val) {
         try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory
                    .newInstance();
            domFactory.setNamespaceAware(true);
            final DocumentBuilder builder = domFactory.newDocumentBuilder();
            final InputSource source = new InputSource(new StringReader(xmlDoc));
            final Document doc = builder.parse(source);
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression expr = xpath.compile(pathexpr);
            final Node param =  (Node) expr.evaluate(doc, XPathConstants.NODESET);
            param.setNodeValue(val);

            final TransformerFactory tFact = TransformerFactory.newInstance();
            final Transformer transformer = tFact.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (SAXException ex) {
            ServiceLogger.LOG.error("Error parsing xml string", ex);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Error buffering xml string", ex);
        } catch (ParserConfigurationException ex) {
            ServiceLogger.LOG.error("Error configuring xml parser", ex);
        } catch (XPathExpressionException ex) {
            ServiceLogger.LOG.error("Invalid XML XPATH check", ex);
        } catch (TransformerException ex) {
            ServiceLogger.LOG.error("Invalid option to write information", ex);
        }
        return null;
    }
}

