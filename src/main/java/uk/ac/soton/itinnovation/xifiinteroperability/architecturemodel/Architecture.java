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
package uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.xml.sax.SAXException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelcomponent.WrapperDeploymentException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data.JSON;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data.XML;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InteroperabilityReport;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.PatternValidation;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLDocument;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.State;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.StateMachine;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.InvalidStateMachineException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidRESTMessage;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.SystemProperties;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections.CollectionsBrowserForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.ExecutionPanel;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidPatternException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MsgEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.utilities.FileUtils;

/**
 * An architecture is a representation of the system to test. This consists of
 connected services:
 1) A service is an enabler in Fi-WARE terms. Each service has a set
 of interfaces:
 2) A provided interface implemented as a REST interface with a url
 3) A required interface with invokes other REST interfaces.

 This class maintains an architectural representation as a data structure.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class Architecture {

    // The architecture is split into two parts: i) the description of the
    // services, ii) the behaviour model of the REST events exchanged between
    // them

    /**
     * The list of services in the architecture (either to test their api, or
     * to create a proxy of for application testing).
     */
    private final transient Map<String, RESTComponent> services;

    /**
     * a getter method for the services used in the architecture
     * @return the rest components IDs linked to rest component objects in a map
     */
    public Map<String, RESTComponent> getServices(){
        return services;
    }

    /**
     * The state machine representing the behaviour flow of the architecture
     * in terms of exchanged REST events for transitions between states.
     */
    private final transient StateMachine behaviourSequence;

    /**
     * reference to the collections explorer to handle multi tests execution
     */
    private final transient CollectionsBrowserForm collectionsExplorer;

    /**
     * User defined data.
     * Each architecture pattern can contain a set of data values defined by
     * the user in the <patterndata> tag
     */
    private final transient Map<String, String> dataConstants = new HashMap();

    /**
     * Accessor for the data constants field.
     * @return The current set of data constants.
     */
    public final Map<String, String> getDataConstants() {
        return dataConstants;
    }

    /**
     * Construct a new architecture from a given specification in XML. DebugMode is assumed to be false
     * @param xml The architecture specification (pattern).
     * @param report The report to output tests to.
     * @throws InvalidStateMachineException when the XML is invalid
     * @throws InvalidPatternException when there are more than one start nodes in the graph
     */
    public Architecture(final String xml, final InteroperabilityReport report) throws InvalidStateMachineException, InvalidPatternException{
        this(xml, report, false);
    }

    /**
     * Construct a new architecture from a given specification in XML.
     * Assumes execution panel as null
     *
     * @param xml The architecture specification (pattern).
     * @param report The report to output tests to.
     * @param debugMode whether the state machine is run in debug mode
     * @throws InvalidStateMachineException when the XML is invalid
     * @throws InvalidPatternException when there are more than one start nodes
     * in the graph
     */
    public Architecture(final String xml, final InteroperabilityReport report, final boolean debugMode) throws InvalidStateMachineException, InvalidPatternException {
        this(xml, report, debugMode, null, null);
    }

    /**
     * Construct a new architecture from a given specification in XML.
     * @param xml The architecture specification (pattern).
     * @param report The report to output tests to.
     * @param debugMode whether the state machine is run in debug mode
     * @param execPanel the execution panel to use for testing animations
     * @param collectionsForm the collection form, to handle multiple tests execution
     * @throws InvalidStateMachineException when the XML is invalid
     * @throws InvalidPatternException when there are more than one start nodes in the graph
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Architecture(final String xml, final InteroperabilityReport report, final boolean debugMode, final ExecutionPanel execPanel, final CollectionsBrowserForm collectionsForm) throws InvalidStateMachineException, InvalidPatternException {
        collectionsExplorer = collectionsForm;
        try {
            if (report == null) {
                this.behaviourSequence = new StateMachine(debugMode, execPanel, collectionsExplorer);
            } else {
                this.behaviourSequence = new StateMachine(report, debugMode, execPanel, collectionsExplorer);
            }
            // Validate the pattern
            final URL schemaUrl = FileUtils.getURL(SystemProperties.PATTERNSCHEMA);

            if (schemaUrl == null) {
                ServiceLogger.LOG.error("Pattern Schema not loaded");
                throw new InvalidStateMachineException("Could not load pattern.xsd");
            }
            try {
                if (!PatternValidation.validatePattern(xml, schemaUrl)) {
                    ServiceLogger.LOG.error("Not a valid pattern description");
                    throw new InvalidStateMachineException("The XML description of pattern is not valid");
                }
            }
            catch (InvalidPatternException ex){
                cleanup();
                throw new InvalidPatternException("Error in the Pattern xml" + ex.getMessage(), ex);
            }

            // Read the pattern from the xml string input parameter
            final Document pattern = XMLDocument.jDomReadXmlStream(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            final Element data = pattern.getRootElement().getChild(XMLStateMachine.DATA_TAG);
            if (data != null) {
                final List<Element> children = data.getChildren();
                for (Element dataElement : children) {
                    this.dataConstants.put(dataElement.getChildText(XMLStateMachine.DATA_NAME), dataElement.getChildText(XMLStateMachine.DATA_VALUE));
                    if (collectionsExplorer != null){
                        this.collectionsExplorer.getMultiTestsManager().putPatternValue(this.collectionsExplorer.getCurrentModel(),
                                dataElement.getChildText(XMLStateMachine.DATA_NAME), dataElement.getChildText(XMLStateMachine.DATA_VALUE));
                    }
                }
            }

            // Create the set of services that match the ADL description
            this.services = createServices(pattern.getRootElement().getChild("architecture"),
                    this.behaviourSequence);

            // Create the state machine describing the interoperability tests
            XMLStateMachine.createStateMachine(
                    pattern.getRootElement().getChild("behaviour"),
                    this.behaviourSequence, this);

        } catch (InvalidStateMachineException ex) {
            cleanup();
            ex.printStackTrace();
            throw new InvalidStateMachineException("Invalid behaviour specification " + ex.getMessage(), ex);
        } catch (SAXException ex) {
            cleanup();
            throw new InvalidStateMachineException("Error in the Pattern xml" + ex.getMessage(), ex);
        } catch (InvalidArchitectureException ex) {
            cleanup();
            throw new InvalidStateMachineException("Invalid architecture specification " + ex.getMessage(), ex);
        }
    }


    /**
     * State accessors. Access the two parts of the architecture using
     * two spearate operations: i) for components, ii) for the state machine.
     */

    /**
    * Get the set of components in the architecture.
    * @return The list of components in the architecture.
    */
    public final Map<String, RESTComponent> getArchitectureComponents() {
        return this.services;
    }

    /**
     * Get the state machine object representation.
     * @return The statemachine of this pattern.
     */
    public final StateMachine getStateMachine() {
         return this.behaviourSequence;
     }

    /**
     * The XML document has a <architecture> tag. Using this element only
     * we create a set of REST components that represent the service apis
     * and where necessary generate the correct proxy.
     *
     * @param doc The DOM element for the <architecture> tag in the specification
     * @param eventCap A pointer to the
     * @return A hashtable of the created components.
     * @throws InvalidArchitectureException Error indicator of an invalid specification.
     *
     */
    public static Map<String, RESTComponent> createServices(final Element doc,
             final EventCapture eventCap) throws InvalidArchitectureException {
      final HashMap<String, RESTComponent> components = new HashMap();
        try {
            final XPath xpa = XPath.newInstance("//" + XMLStateMachine.COMPONENT_LABEL);
            if (xpa == null) {
                throw new InvalidArchitectureException("Error in <architecture> specification");
            }
            final List<Element> xmlStates = xpa.selectNodes(doc);
            for (Element eltIndex : xmlStates) {
                final RESTComponent rComponent = new RESTComponent(eltIndex, eventCap);
                components.put(rComponent.getComponentID(), rComponent);
            }
        } catch (JDOMException ex) {
            ServiceLogger.LOG.error("Invalid Pattern specification" + ex.getMessage());
            throw new InvalidArchitectureException("Error in <architecture> specification", ex);
        }
        if (components.size() < 1) {
            throw new InvalidArchitectureException("Architecture must have at least one component");
        }
        return components;
     }

     /**
      * Begin testing of the architecture. This will start the state machine
      * executing; i.e. the pattern will be put into the first state and then
      * respond to event transitions that occur from this point forward.
      *
      * Once the pattern has been tested a report is generated and then returned
      * as a result of this method.
     * @return The interoperability report describing the reporting of
      */
     public final InteroperabilityReport executePattern() {
         return this.behaviourSequence.start();
     }


     /**
      * Release all resources used by this object i.e. after the pattern has
      * been used.
      * To ensure that the object is released by the garbage collector we need
      * to release each attached proxy (which may be still awaiting event
      * requests).
      */
     public final void cleanup() {
        if (services != null) {
            final Collection<RESTComponent> entrySet = services.values();
            for (RESTComponent rComponent : entrySet) {
                for (RESTInterface ri : rComponent.getInterfaces()) {
                    try {
                        ri.release();
                    } catch (WrapperDeploymentException ex) {
                        ServiceLogger.LOG.error("Error releasing REST Intf" + ri.getInterface(), ex);
                    }
                }
            }
            services.clear();
        }
     }

     /**
      * A value in the pattern can hold a reference to a value:
      * - Component Identifier
      * - Pattern data constant value
      * - State event value (value from a dynamic event in the state machine
      * Here we update the template with a real value.
      *
     * @param templateValue The value with a template reference
     * @return The value from the pattern.
     * @throws InvalidPatternReferenceException Error in the pattern specification
     * @throws InvalidRESTMessage Error in a rest message involved.
      */
     public final String replacePatternValue(final String templateValue)
            throws InvalidPatternReferenceException, InvalidRESTMessage {

         String toReplace = templateValue;
         if ((toReplace.contains("%%")) && (!toReplace.contains("%%%"))) {
             final int count = XMLStateMachine.charOccurences(toReplace, "%");
             if (count % 4 != 0) {
                 throw new InvalidPatternReferenceException("Invalid %% template input");
             }
             toReplace = createChange(toReplace, "\\%\\%");
         }
         if (toReplace.contains("$$")) {
             final int count = XMLStateMachine.charOccurences(toReplace, "$");
             if (count % 4 != 0) {
                 throw new InvalidPatternReferenceException("Invalid $$ template input");
             }

             return createChange(toReplace, "\\$\\$");
        } else if (templateValue.startsWith(XMLStateMachine.DATA_TAG)) {
            return getData(templateValue);
        } else if (templateValue.startsWith(XMLStateMachine.TEST_TAG)){
            return getPreviousTestData(templateValue);
        } else if (templateValue.startsWith(XMLStateMachine.COMPONENT_LABEL)) {
             return XMLStateMachine.getURLEntryFromXML(templateValue, this);
        } else if (templateValue.contains("|")) {
            return getStateValue(templateValue);
        }
        return templateValue;
     }

    /**
     * Retrieve a data value from a REST message stored at a particular state
     * in the state machine.
     * @param inputExpr The template expression describing the state in state (label) |
     * field (headers or content) | expression (xpath or jpath)
     * @return The evaluatated value.
     * @throws InvalidPatternReferenceException if not in correct format
     * state label | field label | data expression
     */
    private String getStateValue(final String inputExpr)
        throws InvalidPatternReferenceException {

        String exp = inputExpr;
        if (exp.contains("\\$\\$")) {
            exp = exp.replaceAll("\\$", "");
        }
        final String[] exprSplit = exp.split("\\|");
        if (exprSplit.length != 3) {
            throw new InvalidPatternReferenceException("Three paramaeter required: state | field | expresstion");
        }
        final State stateS = this.behaviourSequence.getState(exprSplit[0]);
        if (stateS == null) {
            throw new InvalidPatternReferenceException("State label does not match a state label in this architecture");
        }
        final MsgEvent rEv = stateS.getStoredEvent();
        if (rEv == null) {
            throw new InvalidPatternReferenceException("Error reading event - not yet occured");
        }
        try {
            switch (exprSplit[1]) {
                case "content":
                    final String content = rEv.getDataBody().getData();
                    System.out.println("body = " + content);
                    if (rEv.getDataBody().getType().equalsIgnoreCase("xml")) {
                        return XML.readValue(content, exprSplit[2]);
                    } else if (rEv.getDataBody().getType().equalsIgnoreCase("json")){
                        return JSON.readValue(content, "$." + exprSplit[2]);
                    } else if (rEv.getDataBody().getType().equalsIgnoreCase("application/xml")) {
                        return XML.readValue(content, exprSplit[2]);
                    } else if (rEv.getDataBody().getType().equalsIgnoreCase("application/json")){
                        return JSON.readValue(content, "$." + exprSplit[2]);
                    }
                    else {
                        return content;
                    }
                case "headers":
                    return rEv.getParameterMap().get(exprSplit[2]).getValue();
                default:
                    throw new InvalidPatternReferenceException("Field label must be 'content' or 'headers'");
            }
        } catch (Exception ex) {
            throw new InvalidPatternReferenceException("Could not read value - invalid expression", ex);
        }
    }

    /**
     * In a string replace all template inputs with evaluated values i.e. fill
     * in the parameters dynamically. A$$exp$$ becomes AB where exp evaluates
     * to B.
     * @param body The data body to read and fill
     * @return The fully evaluated data value
     * @throws InvalidPatternReferenceException Error applying change to pattern.
     */
    private String createChange(final String body, final String splitType) throws InvalidPatternReferenceException {
         String[] split = body.split(splitType);
         if (split == null) {
             throw new InvalidPatternReferenceException("Does not contain $ parameters");
         }
         for (int i = 1; i < split.length; i += 2) {
            final String expr = split[i];
            if (expr.contains("|") && !expr.startsWith(XMLStateMachine.TEST_TAG)) {
                split[i] = getStateValue(expr);
            } else if (expr.startsWith(XMLStateMachine.DATA_TAG)) {
                 split[i] = getData(expr);
            } else if (expr.startsWith(XMLStateMachine.TEST_TAG)){
                split[i] = getPreviousTestData(expr);
            } else if (expr.startsWith(XMLStateMachine.COMPONENT_LABEL)) {
                split[i] = getComponentValue(expr);
            } else if (expr.equalsIgnoreCase("counter")) {
                split[i] = ""+getCounterValue();
            }
         }
         final StringBuilder result = new StringBuilder();
         for (String str : split) {
             result.append(str);
         }
         return result.toString();
     }

    /**
     * Given the patterndata.field expression; read the value and return it.
     *
     * @param expression The data parameter in dot notation to return
     * @return The data value
     * @throws InvalidPatternReferenceException Error in the xml specification
     */
    public final String getData(final String expression)
            throws InvalidPatternReferenceException {
        final int indexElement = expression.indexOf('.') + 1;
        if (indexElement != XMLStateMachine.DATA_TAG.length() + 1) {
            throw new InvalidPatternReferenceException("Invalid data tag: " + expression.substring(0, indexElement));
        }
        final String dataName = expression.substring(indexElement);
        if (dataName != null) {
            final String dataValue = this.dataConstants.get(dataName);
            if (dataValue == null) {
                    throw new InvalidPatternReferenceException("Data field does not exist");
            }
            return dataValue;
        }
        throw new InvalidPatternReferenceException("Data field does not exist");
    }

    /**
     * this method fetches data from a previous test when doing a multiple test execution
     * @param expression the expression to fetch data for
     * @return the fetched data
     * @throws InvalidPatternReferenceException
     */
    public final String getPreviousTestData(final String expression) throws InvalidPatternReferenceException {
        final String[] split = expression.split("\\.", 4);

        if (split.length != 4){
            throw new InvalidPatternReferenceException("Invalid data reference for previous tests - " + expression + ".");
        }

        if (!split[0].equals(XMLStateMachine.TEST_TAG)){
            throw new InvalidPatternReferenceException("Invalid data reference for previous tests - " + expression + ".");
        }

        final String testID = split[1];

        // pattern data extraction, format $$test.{test-id}.patterndata.{data-id}$$
        if (split[2].equals(XMLStateMachine.DATA_TAG)){
            final String patternDataID = split[3];
            final String dataValue = this.collectionsExplorer.getMultiTestsManager().getPatternValue(testID, patternDataID);
            if (dataValue == null){
                throw new InvalidPatternReferenceException("No data value associated with the reference for previous tests data - " + expression + ".");
            }
            return dataValue;
        }
        // captured event content extraction, $$test.{test-id}.{label-id}.content|{xpath/jsonpath}$$
        else if (split[3].startsWith("content")){
            final String labelID = split[2];

            final String[] secondSplit = split[3].split("\\|", 2);
            if (secondSplit.length != 2){
                throw new InvalidPatternReferenceException("Invalid content data reference for previous tests - " + expression + ".");
            }

            if (!secondSplit[0].equals("content")){
                throw new InvalidPatternReferenceException("Invalid content data reference for previous tests - " + expression + ".");
            }

            final String path = secondSplit[1];
            final String data = this.collectionsExplorer.getMultiTestsManager().getTestContent(testID, labelID);
            if (data == null){
                throw new InvalidPatternReferenceException("No data value associated with the reference for previous tests data - " + expression + ".");
            }

            String value;
            if (data.startsWith("<?xml")){
                value = XML.readValue(data, path);
            }
            else {
                value = JSON.readValue(data, "$." + path);
            }

            if (value == null){
                 throw new InvalidPatternReferenceException("Content is not a valid XML/JSON format, or the path expression is invalid - " + expression + ".");
            }

            return value;
        }
        // captured event headers extraction, $$test.{test-id}.{label-id}.headers|{header-id}$$
        else if (split[3].startsWith("headers")){
            final String labelID = split[2];

            final String[] secondSplit = split[3].split("\\|", 2);
            if (secondSplit.length != 2){
                throw new InvalidPatternReferenceException("Invalid headers data reference for previous tests - " + expression + ".");
            }

            if (!secondSplit[0].equals("headers")){
                throw new InvalidPatternReferenceException("Invalid headers data reference for previous tests - " + expression + ".");
            }

            final String headerID = secondSplit[1];
            final String headerValue = this.collectionsExplorer.getMultiTestsManager().getTestHeader(testID, labelID, headerID);
            if (headerValue == null){
                throw new InvalidPatternReferenceException("No data value associated with the reference for previous tests data - " + expression + ".");
            }

            return headerValue;
        }
        
        throw new InvalidPatternReferenceException("Invalid reference for data from previous tests.");
    }

    /**
     * Obtain a component URL entry from the pattern/data content.
     * @param expression The data to retrieve the component URL from.
     * @return The component URL.
     * @throws InvalidPatternReferenceException Error in the XML expression.
     */
    private String getComponentValue(final String expression)
            throws InvalidPatternReferenceException {
        try {
            return XMLStateMachine.getURLEntryFromXML(expression, this);
        } catch (InvalidRESTMessage ex) {
            throw new InvalidPatternReferenceException("Invalid component expression", ex);
        }
    }

    /**
     * Obtain a state's counter value.
     * @return The counter value.
     * @throws InvalidPatternReferenceException Error in the XML expression.
     */
    private int getCounterValue()
            throws InvalidPatternReferenceException {
        State stateS = this.behaviourSequence.getCurrentState();
        return stateS.getCounter();

    }

}
