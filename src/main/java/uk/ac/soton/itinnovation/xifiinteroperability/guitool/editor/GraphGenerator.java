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


package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.view.mxGraph;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Function;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Message;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidPatternException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;


/**
 * Code to generate XML documents from a drawn graphical representation. That is
 * a state machine diagram to an xml specification. The methods also do the reverse
 * i.e. take an xml specification and draw a graph.
 * @author pjg
 */
public class GraphGenerator {

    /**
     * The behaviour Graph to/from convert to xml.
     */
    private final transient mxGraph graphPanel;

    /**
     * The system graph to/from convert to xml.
     */
    private final transient mxGraph archPanel;

    /**
     * The data model elements to leverage in specifying the xml.
     */
    private final transient DataModel dataModel;

    /**
     * The position to draw on horizontal axis.
     */
    private transient double currentHorizontal = 75.0;

    /**
     * The position to draw on the vertical axis.
     */
    private transient double currentVertical = 50.0;

    /**
     * Reference to UI object
     */
    private final transient BasicGraphEditor UIEditor;
    /**
     * Configure the graph/specification generator.
     * @param editor The graph UI object to extract elements from.
     */
    public GraphGenerator(final BasicGraphEditor editor) {
        this.graphPanel = editor.getBehaviourGraph().getGraph();
        this.archPanel = editor.getSystemGraph().getGraph();
        this.dataModel = editor.getDataModel();
        this.UIEditor = editor;
    }

    /**
     * Given a xml string create a java object of the document.
     * @param xml The xml input
     * @return The java object of the document.
     * @throws SAXException XML exception
     * @throws IOException input exception
     * @throws ParserConfigurationException XML parser set up problem.
     */
    public static Document loadXMLFromString(final String xml) throws SAXException, IOException, ParserConfigurationException  {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final InputSource inSource = new InputSource(new StringReader(xml));
        return builder.parse(inSource);
    }

    private void addMessageTransition(final String fromID, final Element eElement, mxCell myCell3) throws XMLInputException {
        final Message message = (Message) dataModel.getTransition(myCell3.getId());
        final NodeList msgDataList = eElement.getElementsByTagName("message");

        if (msgDataList == null || msgDataList.getLength() == 0) {
            throw new XMLInputException("The <message> tag is missing, or incorrect in the " + fromID + "<transition>");
        }

        final Node msgData = msgDataList.item(0);

        if (((Element) msgData).getElementsByTagName("url") == null) {
            throw new XMLInputException("The <url> tag is missing, or incorrect in the " + fromID + "<transition>");
        }
        final String url = ((Element) msgData).getElementsByTagName("url").item(0).getTextContent();

        if (((Element) msgData).getElementsByTagName("path").item(0) == null) {
            throw new XMLInputException("The <path> tag is missing, or incorrect in the " + fromID + "<transition>");
        }
        final String path = ((Element) msgData).getElementsByTagName("path").item(0).getTextContent();

        if (((Element) msgData).getElementsByTagName("method") == null) {
            throw new XMLInputException("The <method> tag is missing, or incorrect in the " + fromID + "<transition>");
        }
        final String method = ((Element) msgData).getElementsByTagName("method").item(0).getTextContent();

        if (((Element) msgData).getElementsByTagName("type") == null) {
            throw new XMLInputException("The <type> tag is missing, or incorrect in the " + fromID + "<transition>");
        }
        final String type = ((Element) msgData).getElementsByTagName("type").item(0).getTextContent();

        if (((Element) msgData).getElementsByTagName("body") == null) {
            throw new XMLInputException("The <body> tag is missing, or incorrect in the " + fromID + "<transition>");
        }
        final NodeList bodyTag = ((Element) msgData).getElementsByTagName("body");

        String body = "";
        if (bodyTag.getLength() > 0) {
            body = bodyTag.item(0).getTextContent();
        }
        message.updateMessage(url, path, method, type, body);

        final NodeList headers = eElement.getElementsByTagName("header");
        for (int j = 0; j < headers.getLength(); j++) {
            final String param = ((Element) headers.item(j)).getElementsByTagName("name").item(0).getTextContent();
            final String value = ((Element) headers.item(j)).getElementsByTagName("value").item(0).getTextContent();
            message.addHeader(param, value);
        }
    }

    /**
     * Generate a series of transitions from the xml input and then add them
     * to the data model which forms the basis of the newly drawn graph.
     *
     * @param fromID The source ID of the transition - the GUI id.
     * @param eElement The xml description of the transition.
     */
    private void generateTransitions(final String fromID, final Element eElement)
        throws XMLInputException{

        try {
            // Get the info about the two from nodes in the graph to draw connection.
            final String toNode = eElement.getElementsByTagName("to").item(0).getTextContent();
            if (toNode == null) {
                throw new XMLInputException("The <to> tag is missing, or incorrect in the " + fromID + "<transition>");
            }
            final GraphNode toID = (GraphNode) dataModel.getNodeByLabel(toNode);
            final GraphNode from = (GraphNode) dataModel.getNode(fromID);
            final Object parent = graphPanel.getDefaultParent();

            // Insert the edge into the visual graph
            final mxCell myCell1 = (mxCell) ((mxGraphModel) graphPanel.getModel()).getCell(from.getUIIdentifier());
            final mxCell myCell2 = (mxCell) ((mxGraphModel) graphPanel.getModel()).getCell(toID.getUIIdentifier());
            final mxCell myCell3 = (mxCell) graphPanel.insertEdge(parent, null, "", myCell1, myCell2);

            // update the data model with new connection
            dataModel.addConnection(myCell3.getId(), fromID, toID.getUIIdentifier());

            // Add the attribute data to the connection (message/guard data)
            final String nodeType = from.getType();
            if (nodeType.equalsIgnoreCase(XMLStateMachine.TRIGGER_LABEL) || nodeType.equalsIgnoreCase(XMLStateMachine.TRIGGERSTART_LABEL)) {
                addMessageTransition(fromID, eElement, myCell3);
            }
            else if (nodeType.equalsIgnoreCase(XMLStateMachine.LOOP_LABEL) && eElement.getElementsByTagName("guards").getLength() == 0) {
                addMessageTransition(fromID, eElement, myCell3);
            }
            else {
                final Guard guardData = (Guard) dataModel.getTransition(myCell3.getId());
                if (guardData != null) {
                    Node item = eElement.getElementsByTagName("guards").item(0);
                    if (item != null) {
                        final NodeList guards = item.getChildNodes();

                        for (int i = 0; i < guards.getLength(); i++) {
                            final Node currentItem = guards.item(i);
                            if (currentItem.getNodeType() != 3) {
                                final String fName = currentItem.getNodeName();
                                final String param = ((Element) currentItem).getElementsByTagName("param").item(0).getTextContent();
                                final String value = ((Element) currentItem).getElementsByTagName("value").item(0).getTextContent();
                                guardData.addGuard(Function.getFunction(fName), param, value);
                            }
                        }
                    }
                }
            }

        } catch (DOMException ex) {
            ServiceLogger.LOG.error("Error creating graph - could not generate xml transitions", ex);
        }
    }

    /**
     * generates a component transition, a.k.a. a link between two component nodes
     * @param fromID source identifier
     * @param eElement the transition xml element
     * @throws XMLInputException
     */
    private void generateComponentTransition(final String fromID, final Element eElement) throws XMLInputException{
        try {
            // Get the info about the two from nodes in the graph to draw connection.
            final String toNode = eElement.getElementsByTagName("with").item(0).getTextContent();
            if (toNode == null) {
                throw new XMLInputException("The <with> tag is missing, or incorrect in the " + fromID + "<link>");
            }
            final ArchitectureNode toID = (ArchitectureNode) dataModel.getNodeByLabel(toNode);
            final ArchitectureNode from = (ArchitectureNode) dataModel.getNode(fromID);
            final Object parent = archPanel.getDefaultParent();

            // Insert the edge into the visual graph
            final mxCell myCell1 = (mxCell) ((mxGraphModel) archPanel.getModel()).getCell(GUIdentifier.removeArchID(from.getUIIdentifier()));
            final mxCell myCell2 = (mxCell) ((mxGraphModel) archPanel.getModel()).getCell(GUIdentifier.removeArchID(toID.getUIIdentifier()));
            final mxCell myCell3 = (mxCell) archPanel.insertEdge(parent, null, "", myCell1, myCell2);

            // update the data model with new connection
            dataModel.addComponentConnection(GUIdentifier.setArchID(myCell3.getId()), fromID, toID.getUIIdentifier());

        } catch (DOMException ex) {
            ServiceLogger.LOG.error("Error creating graph - could not generate xml component links", ex);
        }
    }

    /**
    * The XML input of the REST Interface transfered into an element in the
    * system graph.
    * @param eElement The xml of a rest interface.
    */
    public final void generateRESTInterfaceVertex(final Element eElement) {

        try {
            // Insert the node into the graph ui
            final mxGeometry geo1 = new mxGeometry(currentHorizontal, currentVertical, 50, 50);
            final String label = eElement.getElementsByTagName("id").item(0).getTextContent();

            final Node interfaceData = eElement.getElementsByTagName(XMLStateMachine.INTERFACE_LABEL).item(0);
            mxCell port1 = null;
            if (interfaceData == null) {
                port1 = new mxCell(
                    label, geo1,
                    "image;image=/images/client.png");
            }
            else {
                final String protocol = eElement.getElementsByTagName("protocol").item(0).getTextContent();
                if(protocol.equalsIgnoreCase("http")) {
                    port1 = new mxCell(
                    label, geo1,
                    "image;image=/images/http.png");
                }
                else if (protocol.equalsIgnoreCase("coap")) {
                    port1 = new mxCell(
                    label, geo1,
                    "image;image=/images/coap.png");
                }
                else if (protocol.equalsIgnoreCase("mqtt")) {
                    port1 = new mxCell(
                    label, geo1,
                    "image;image=/images/mqtt.png");
                }
            }

            port1.setVertex(true);
            final mxCell restul = (mxCell) archPanel.addCell(port1);
            dataModel.addNode(restul.getId(), label, XMLStateMachine.INTERFACE_LABEL);
            final ArchitectureNode gNode = (ArchitectureNode) dataModel.getNode(GUIdentifier.setArchID(restul.getId()));

            gNode.setData(label,
                    eElement.getElementsByTagName("address").item(0).getTextContent());

            if (interfaceData != null) {
                final NodeList interfacesData = eElement.getElementsByTagName(XMLStateMachine.INTERFACE_LABEL);
                for(int i=0; i<interfacesData.getLength(); i++){
                    final Element interfa = (Element) interfacesData.item(i);
                    // protocol is optional - so check before retrieving
                    String protocolTag = null;
                    if(interfa.getElementsByTagName("protocol").item(0) != null) {
                        protocolTag = interfa.getElementsByTagName("protocol").item(0).getTextContent();
                    }

                    gNode.addInterfaceData(interfa.getElementsByTagName("id").item(0).getTextContent(),
                            interfa.getElementsByTagName("url").item(0).getTextContent(), protocolTag );
                }
                this.currentHorizontal += 100;
            }

        } catch (DOMException ex) {
            ServiceLogger.LOG.error("Error creating graph - could not generate xml nodes", ex);
        }
    }

     /**
    * The XML input of the State Machine Node (state) transformed into a GUI
    * behaviour element.
    * @param eElement The xml of the state node to draw.
    * @return the identifier of the generated graph node.
     * @throws InvalidPatternException The xml pattern is invalid.
    */
    private String generateStateVertex(final Element eElement)
            throws InvalidPatternException {

        try {
            final String nodeType = eElement.getElementsByTagName("type").item(0).getTextContent();
            final String label = eElement.getElementsByTagName("label").item(0).getTextContent();

            final mxGeometry geo1 = new mxGeometry(currentHorizontal, currentVertical, 50, 50);
            mxCell nNode = null;

            switch(nodeType.toLowerCase()) {
                case XMLStateMachine.START_LABEL: nNode = new mxCell(
                    nodeType, geo1,
                    "image;image=/images/event_end.png");
                    break;
                case XMLStateMachine.TRIGGERSTART_LABEL: nNode = new mxCell(
                    nodeType, geo1,
                    "image;image=/images/event_triggerstart.png");
                    break;
                case XMLStateMachine.END_LABEL: nNode = new mxCell(
                    nodeType, geo1,
                    "image;image=/images/terminate.png");
                    break;
                case XMLStateMachine.TRIGGER_LABEL: nNode = new mxCell(
                    nodeType, geo1,
                    "image;image=/images/link.png");
                    break;
                case XMLStateMachine.LOOP_LABEL: nNode = new mxCell(
                    nodeType, geo1,
                    "image;image=/images/loop.png");
                    break;
                case XMLStateMachine.DATALOOP_LABEL: nNode = new mxCell(
                    nodeType, geo1,
                    "image;image=/images/dataloop.png");
                    break;
                case XMLStateMachine.NORMAL_LABEL: nNode = new mxCell(
                    nodeType, geo1,
                    "image;image=/images/event.png");
                    break;
                default:
                    throw new InvalidPatternException("State is not one of the six types");
            }

            // Insert the node into the graph ui
            if (nNode != null) {
                nNode.setValue(label);
                nNode.setVertex(true);
            }
            final mxCell restul = (mxCell) graphPanel.addCell(nNode);

            dataModel.addNode(restul.getId(), label, nodeType);
            final GraphNode grphNode = (GraphNode) dataModel.getNode(restul.getId());
            grphNode.setLabel(label);

            if (nodeType.equalsIgnoreCase("dataloop")) {
                String event = null;
                NodeList elementsByTagName = eElement.getElementsByTagName("event");
                if (elementsByTagName.getLength() > 0 ) {
                    event = elementsByTagName.item(0).getTextContent();
                }

                grphNode.addEventStateData(event);
            }

            if (nodeType.equalsIgnoreCase("end")) {
                boolean success = true;
                String report = "Interoperability test succeeded";
                NodeList elementsByTagName = eElement.getElementsByTagName("success");
                if (elementsByTagName.getLength() > 0 ) {
                    String successTxt = elementsByTagName.item(0).getTextContent();
                    success = Boolean.valueOf(successTxt);
                }

                elementsByTagName = eElement.getElementsByTagName("report");
                if (elementsByTagName.getLength() > 0) {
                    report = elementsByTagName.item(0).getTextContent();
                }
                grphNode.addEndStateData(success, report);
            }

            this.currentHorizontal += 100;
            if (nodeType.equalsIgnoreCase("start") || nodeType.equalsIgnoreCase("triggerstart")) {
                return restul.getId();
            } else {
                return null;
            }
        } catch (DOMException ex) {
            throw new InvalidPatternException("XML description of state is invalid", ex);
        }
    }

    /**
     * a method to ensure the uniqueness of all components when importing a graph
     * @param dom the document of the graph
     * @throws InvalidPatternException thrown if an id is not unique and the user rejects to change it
     */
    private void ensureComponentsUniqueness(final Document dom) throws InvalidPatternException {
        Set<String> usedIDs = new HashSet<>();
        Set<String> followingIDs;
        NodeList nList = dom.getElementsByTagName("component");

        for (int i = 0; i < nList.getLength(); i++) {
            followingIDs = new HashSet<>();
            for(int k=i+1; k<nList.getLength(); k++){
                followingIDs.add(((Element) nList.item(k)).getElementsByTagName("id").item(0).getTextContent().toLowerCase());
            }

            final Element eElement = (Element) nList.item(i);
            String label = eElement.getElementsByTagName("id").item(0).getTextContent();
            label = label.replaceAll("\\s+", "_");
            String title = "Unique component id error : ID '" + label + "'";
            String originalLabel = label.toLowerCase();
            if (this.dataModel.getComponentByLabel(label) != null || usedIDs.contains(label.toLowerCase()) || followingIDs.contains(label.toLowerCase())){
                label = JOptionPane.showInputDialog(UIEditor,
                        "Component id '" + label + "' is not unique, please choose a diferent id before importing",
                        title, JOptionPane.ERROR_MESSAGE);
                if (label != null){
                    label = label.replaceAll("\\s+", "_");
                }

                while (label != null && (this.dataModel.getComponentByLabel(label) != null || usedIDs.contains(label.toLowerCase()) || followingIDs.contains(label.toLowerCase()))) {
                    label = JOptionPane.showInputDialog(UIEditor,
                            "The new component id '" + label + "' is also not unique, please choose a diferent id before importing",
                            title, JOptionPane.ERROR_MESSAGE);
                    if (label != null) {
                        label = label.replaceAll("\\s+", "_");
                    }
                }
            }

            if (label != null) {
                usedIDs.add(label.toLowerCase());

                if (!originalLabel.equalsIgnoreCase(label)) {
                    eElement.getElementsByTagName("id").item(0).setTextContent(label);

                    /* going to the url of all states with url element to check if they contain
                       the original label of the renamed component */
                    NodeList testStates = dom.getElementsByTagName("state");
                    for (int j=0; j<testStates.getLength(); j++){
                        final Element testState = (Element) testStates.item(j);
                        if (testState.getElementsByTagName("url").getLength() > 0){
                            Node urlAddress = testState.getElementsByTagName("url").item(0);
                            String url = urlAddress.getTextContent().toLowerCase();
                            if (url.contains(originalLabel)){
                                urlAddress.setTextContent(url.replace(originalLabel, label));
                            }
                        }
                    }
                }
            }
            else {
                throw new InvalidPatternException("Component '" + originalLabel + "' is not unique.");
            }
        }
    }

    /**
     * a method to ensure the uniqueness of all state labels when importing a graph
     * @param dom the document of the graph to import
     * @throws InvalidPatternException thrown when a state's label is not unique but
     * the user rejects to change it
     */
    private void ensureStatesUniqueness(final Document dom) throws InvalidPatternException {
        Set<String> usedLabels = new HashSet<>();
        Set<String> followingLabels;
        NodeList nList = dom.getElementsByTagName("state");

        for (int i = 0; i < nList.getLength(); i++) {
            followingLabels = new HashSet<>();
            for(int k=i+1; k<nList.getLength(); k++){
                followingLabels.add(((Element) nList.item(k)).getElementsByTagName("label").item(0).getTextContent().toLowerCase());
            }

            final Element eElement = (Element) nList.item(i);

            String label = eElement.getElementsByTagName("label").item(0).getTextContent();
            label = label.replaceAll("\\s+", "_");
            String title = "Unique state label error : Label '" + label + "'";
            String originalLabel = label.toLowerCase();
            if (this.dataModel.getNodeByLabel(label) != null || usedLabels.contains(label.toLowerCase()) || followingLabels.contains(label.toLowerCase())){
                label = JOptionPane.showInputDialog(UIEditor,
                        "State label '" + label + "' is not unique, please choose a diferent label before importing",
                        title, JOptionPane.ERROR_MESSAGE);
                if (label != null){
                    label = label.replaceAll("\\s+", "_");
                }

                while (label != null && (this.dataModel.getNodeByLabel(label) != null || usedLabels.contains(label.toLowerCase()) || followingLabels.contains(label.toLowerCase()))) {
                    label = JOptionPane.showInputDialog(UIEditor,
                            "The new state label '" + label + "' is also not unique, please choose a diferent label before importing",
                            title, JOptionPane.ERROR_MESSAGE);
                    if (label != null) {
                        label = label.replaceAll("\\s+", "_");
                    }
                }
            }

            if (label != null){
                usedLabels.add(label.toLowerCase());

                if (!originalLabel.equalsIgnoreCase(label)) {
                    eElement.getElementsByTagName("label").item(0).setTextContent(label);

                    /* going through all transitions and rename the 'to' tag of those with
                       the original label of the renamed state */
                    NodeList transitions = dom.getElementsByTagName("to");
                    for(int j=0; j<transitions.getLength(); j++){
                        Node transition = transitions.item(j);
                        if (transition.getTextContent().equalsIgnoreCase(originalLabel)){
                            transition.setTextContent(label);
                        }
                    }
                }
            }
            else {
                throw new InvalidPatternException("State '" + originalLabel + "' is not unique.");
            }
        }
    }

    /**
     * a method to ensure the uniqueness of a start or triggerstart node
     * @param dom the document of the graph to import
     * @throws InvalidPatternException thrown in case of more than one start nodes
     */
    private void ensureStartStateUniqueness(Document dom) throws InvalidPatternException {
        NodeList nList = dom.getElementsByTagName("state");

        for (int i = 0; i < nList.getLength(); i++) {
            final Element eElement = (Element) nList.item(i);
            String type = eElement.getElementsByTagName("type").item(0).getTextContent();
            if (dataModel.containsStart() && (type.equalsIgnoreCase("triggerstart") || type.equalsIgnoreCase("start"))){
                throw new InvalidPatternException("There are more than one start nodes in the graph!");
            }
        }
    }

     /**
     * Public method to create a visual graph from the xml specification. There
     * are two graph views displayed in the GUI: 1) the system graph, and 2)
     * the behaviour graph.
     * @param dom the parsed xml as a dom document.
     * @throws InvalidPatternException Invalid pattern input to method.
     */
    public final void importGraph(final Document dom) throws InvalidPatternException {
        try {
            ensureComponentsUniqueness(dom);
            ensureStatesUniqueness(dom);
        }
        catch (InvalidPatternException ex){
            return;
        }

        try {
            ensureStartStateUniqueness(dom);
        }
        catch (InvalidPatternException ex){
            JOptionPane.showMessageDialog(UIEditor,
                    "Warning! There are more than one start nodes in the graph. "
                            + "Your pattern will not be verified as correct when testing. "
                            + "Please ensure you have only one start or triggerstart node when running the test.",
                    "Duplicate start nodes", JOptionPane.WARNING_MESSAGE);
        }

        createGraph(dom);
    }

    /**
     * Public method to create a visual graph from the xml specification. There
     * are two graph views displayed in the GUI: 1) the system graph, and 2)
     * the behaviour graph.
     * @param dom the parsed xml as a dom document.
     * @throws InvalidPatternException Invalid pattern input to method.
     */
    public final void createGraph(final Document dom) throws InvalidPatternException {

        NodeList nList = dom.getElementsByTagName("component");
        for (int i = 0; i < nList.getLength(); i++) {
            generateRESTInterfaceVertex((Element) nList.item(i));
        }

        // generate links between component nodes
        for (int i =0; i < nList.getLength(); i++){
            final NodeList links = ((Element) nList.item(i)).getElementsByTagName("link");
            final String label = ((Element) nList.item(i)).getElementsByTagName("id").item(0).getTextContent();
            final String srcID = dataModel.getNodeByLabel(label).getUIIdentifier();
            for (int j = 0; j < links.getLength(); j++) {
                try{
                    generateComponentTransition(srcID , (Element) links.item(j));
                } catch (XMLInputException ex) {
                     JOptionPane.showMessageDialog(UIEditor,
                        ex.getLocalizedMessage(),
                        "Import Graph Error",
                        JOptionPane.PLAIN_MESSAGE);
                        this.graphPanel.clearSelection();
                        this.archPanel.clearSelection();
                        return;
                }
            }
        }

        this.currentHorizontal = 75.0;
        this.currentVertical += 100.0;

        String startID = null;
        nList = dom.getElementsByTagName("state");
        for (int i = 0; i < nList.getLength(); i++) {
           final String result = generateStateVertex((Element) nList.item(i));
           if (result != null) {
               startID = result;
           }
        }

        for (int i = 0; i < nList.getLength(); i++) {
            final NodeList transitions = ((Element) nList.item(i)).getElementsByTagName("transition");
            final String label = ((Element) nList.item(i)).getElementsByTagName("label").item(0).getTextContent();
            final String srcID = dataModel.getNodeByLabel(label).getUIIdentifier();
            for (int j = 0; j < transitions.getLength(); j++) {
                try{
                    generateTransitions(srcID , (Element) transitions.item(j));
                } catch (XMLInputException ex) {
                     JOptionPane.showMessageDialog(UIEditor,
                        ex.getLocalizedMessage(),
                        "Import Graph Error",
                        JOptionPane.PLAIN_MESSAGE);
                        this.graphPanel.clearSelection();
                        this.archPanel.clearSelection();
                        return;
                }
            }
        }

        // Add the pattern data
        final NodeList patternTag = dom.getElementsByTagName("patterndata");
        if (patternTag.getLength() > 0) {
            final NodeList pattern = dom.getElementsByTagName("patterndata").item(0).getChildNodes();
            final GraphNode start = (GraphNode) dataModel.getNode(startID);
            for (int i = 0; i < pattern.getLength(); i++) {
                final Node currentItem = pattern.item(i);
                if (currentItem.getNodeType() != 3) {
                    final String param = ((Element) currentItem).getElementsByTagName("name").item(0).getTextContent();
                    final String value = ((Element) currentItem).getElementsByTagName("value").item(0).getTextContent();
                    start.addConstantData(param, value);
                }
            }
        }
    }
}
