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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.soton.itinnovation.xifiinteroperability.SystemProperties;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.DataModelState;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.GUIdentifier;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;
import static uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine.START_LABEL;
import static uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine.TRIGGERSTART_LABEL;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.InvalidTransitionException;

/**
 * The Pattern Editor is built upon a data model. That is, the graph edited
 * visually corresponds to a graph data structure. This data structure
 * is concretised using XML. The XML pattern can be used to execute the
 * automated testing tool.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class DataModel {

    /**
     * The constant label for a client graph element.
     */
    public static final String CLIENT = "client";

    /**
     * Each pattern may contain 0 or more component elements. Note
     * a trigger-based graph may not require component elements. Although
     * for completeness may include the interface to comply check
     */
    private transient List<ArchitectureNode> archElements;

    public List<String> getRestUrls(){
        List<String> restUrls = new ArrayList<>();

        archElements.forEach((archNode) -> {
            archNode.getData().forEach((data) -> {
                restUrls.add("component." + archNode.getLabel() + "." + data.getRestID());
            });
        });

        return restUrls;
    }

    /**
     * The Graph is a set of nodes (vertices).
     * @see GraphNode
     */
    private transient List<GraphNode> graphElements;

    /**
     * a getter for all graph elements
     * @return all graph nodes
     */
    public final List<GraphNode> getGraphElements(){
        return graphElements;
    }

    /**
     * Index of connection IDs to the source node.
     */
    private transient Map<String, GraphNode> connectionIndex;
    
    /**
     * maps transition IDs to their respective source component node
     */
    private transient Map<String, ArchitectureNode> componentsTransitions;

    /**
     * Boolean to represent if the model has a start state.
     * Only one start state is allowed
     */
    private boolean hasStart;

    /**
     * Get the boolean, which represents if there is a start node in the data model
     * @return the boolean hasStart
     */
    public boolean containsStart(){
        return this.hasStart;
    }

    /**
     * Initialse the data model.
     */
    public DataModel() {
       this.graphElements = new ArrayList();
       this.connectionIndex = new HashMap<>();
       this.componentsTransitions = new HashMap<>();
       this.archElements = new ArrayList();
       this.hasStart = false;
   }

   /**
    * Get the data node specified by the UI ID from the graph. That is,
    * select the node data.
    *
    * @param nodeID The User Interface generated ID.
    * @return The data
    */
   public final AbstractGraphElement getNode(final String nodeID) {
       for (GraphNode e : this.graphElements) {
           if (e.getUIIdentifier().equalsIgnoreCase(nodeID)) {
                   return e;
           }
       }
       for (ArchitectureNode e : this.archElements) {
           if (e.getUIIdentifier().equalsIgnoreCase(nodeID)) {
                   return e;
           }
       }
       return null;
   }

    /**
     * a getter for the start or tigger start node in the graph if it exists
     *
     * @return the graph start node
     */
    public final GraphNode getStartNode() {
        for(GraphNode node: this.graphElements){
            if (node.getType().equalsIgnoreCase("start") || node.getType().equalsIgnoreCase("triggerstart")){
                return node;
            }
        }
        return null;
    }

   /**
    * Get the data node specified by the label from the graph. That is,
    * select the node data.
    *
    * @param label The state label.
    * @return The data
    */
   public final AbstractGraphElement getNodeByLabel(final String label) {
       for (GraphNode e : this.graphElements) {
           if (e.getLabel().equalsIgnoreCase(label)) {
               return e;
           }
       }
       
       for (ArchitectureNode e: this.archElements){
           if (e.getLabel().equalsIgnoreCase(label)){
               return e;
           }
       }
       
       return null;
   }

    /**
    * Get the data node specified by the label from the graph. That is,
    * select the node data.
    *
    * @param label The state label.
    * @return The data
    */
   public final AbstractGraphElement getComponentByLabel(final String label) {
       for (ArchitectureNode e : this.archElements) {
           if (e.getLabel().equalsIgnoreCase(label)) {
               return e;
           }
       }
       return null;
   }

   /**
    * Retrieve a transition description from the graph based on the UI
    * identifier.
    * @param ident GUI identifier; in mxGraph = integer > 0
    * @return  The graph element transition info.
    */
   public final AbstractGraphElement getTransition(final String ident) {
       final GraphNode grpNode = this.connectionIndex.get(ident);
       if (grpNode == null) {
           final ArchitectureNode archNode = this.componentsTransitions.get(ident);
           if (archNode == null) {
               return null;
           }
           else {
               return archNode.getTransition(ident);
           }
       } 
       else {
           return grpNode.getTransition(ident);
       }
   }
   
   /**
    * Convert the graph data into an XML representation that matches the
    * schema of the Interoperability Pattern Testing tool input.
    * @return The XML as a String
    */
   public final String getGraphXML() {
       final StringBuilder graphXML = new StringBuilder();
       graphXML.append("<?xml version=\"1.0\"?>\n<pattern xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
               + "xsi:noNamespaceSchemaLocation =\"" + SystemProperties.PATTERNSCHEMA + "\">");

       for (GraphNode e : this.graphElements) {
            if (e.getConstantData().size() > 0) {
                graphXML.append(e.generatePatternDataXML());
            }
        }

       if (this.archElements.size() > 0) {
            graphXML.append("\n\t<architecture>");
            for (ArchitectureNode e : this.archElements) {
                graphXML.append("\t\t").append(e.generateTransitionXML());
            }
            graphXML.append("\n\t</architecture>");
       }
       if (this.graphElements.size() > 0) {
            graphXML.append("\n\t<behaviour>");
            for (GraphNode e : this.graphElements) {
                graphXML.append("\t\t").append(e.generateTransitionXML());
            }
            graphXML.append("\n\t</behaviour>");
       }
       graphXML.append("\n</pattern>");
       String xml = graphXML.toString();
       xml = xml.replaceAll("&(?!amp;)", "&amp;");
       return xml;
   }

   /**
    * Add a new node to the data model.
    * @param ident The identifier of the new node element.
    * @param label The label of the new node element.
    * @param type. The type of the node (client or interface).
    * @return the abstract graph element that has been created
    */
   public final AbstractGraphElement addNode(final String ident, final String label, final String type) {
       AbstractGraphElement element;
       switch(type) {
           case XMLStateMachine.INTERFACE_LABEL:
               element = new ArchitectureNode(GUIdentifier.setArchID(ident), label, type, ident);
               this.archElements.add((ArchitectureNode) element);
               break;
           case CLIENT:
               element = new ArchitectureNode(GUIdentifier.setArchID(ident), label, type, ident);
               this.archElements.add((ArchitectureNode) element);
               break;
           case START_LABEL:
               element = new GraphNode(ident, label, type);
               this.hasStart = true;
               this.graphElements.add((GraphNode) element);
               break;
           case TRIGGERSTART_LABEL:
               element = new GraphNode(ident, label, type);
               this.hasStart = true;
               this.graphElements.add((GraphNode) element);
               break;
           default:
               element = new GraphNode(ident, label, type);
               this.graphElements.add((GraphNode) element);
       }
       
       return element;
   }

   /**
    * a method to add pre-made architecture nodes
    * @param archNode the pre-made architecture node to add
    */
   public final void addArchNode(ArchitectureNode archNode){
       this.archElements.add(archNode);
   }

   /**
    * Check if a label identifier is already in use with another arch node
    * @param ident The label identifier to check
    * @return boolean to represent if the identification label  is already in use
    */
   public final boolean archIdentExist(final String ident){
       return this.archElements.stream().anyMatch((archNode) -> (archNode.getLabel().equalsIgnoreCase(ident)));
   }

   /**
    * Check if a label is already in use with another graph node
    * @param ident The label to check
    * @return boolean to represent if the label is already in use
    */
   public final boolean graphIdentExist(final String ident) {
       return this.graphElements.stream().anyMatch((graphNode) -> (graphNode.getLabel().equalsIgnoreCase(ident)));
   }
   /**
    * Remove the identified element from the data model.
    * @param ident The element to remove.
    */
   public final void deleteNode(final String ident) {
       // check if this is a transition to delete
       GraphNode toDelete = this.connectionIndex.get(ident);
       if (toDelete != null) {
           toDelete.deleteTransition(ident);
           this.connectionIndex.remove(ident);
           return;
       }
       // check if this is a graph node to delete
       for (GraphNode e : this.graphElements) {
           if (e.getUIIdentifier().equalsIgnoreCase(ident)) {
                toDelete = e;
                break;
           }
       }
       if (toDelete != null) {
           if (toDelete.getType().equals(XMLStateMachine.START_LABEL) || toDelete.getType().equals(XMLStateMachine.TRIGGERSTART_LABEL)){
               this.hasStart = false;
           }
           this.graphElements.remove(toDelete);

           /* deleting all transitiong going FROM this Node */
           List<String> transitionsToRemove = new ArrayList<>();
           for(String index: this.connectionIndex.keySet()){
               if (this.connectionIndex.get(index).getLabel().equals(toDelete.getLabel())){
                   transitionsToRemove.add(index);
               }
           }
           
           for(String index: transitionsToRemove){
               this.connectionIndex.remove(index);
           }

           /* deleting all transitions going TO this Node */
           transitionsToRemove = new ArrayList<>();
           /* set is used to avoid double checking the same transitions from a given node*/
           Set<String> visitedNodes = new HashSet<>();
           for(GraphNode sourceNode: this.connectionIndex.values()){
               if (visitedNodes.add(sourceNode.getUIIdentifier())) {
                   /* going through all transitions and checking if the target is the node we are deleting */
                   for (int index = 0; index < sourceNode.getNumberTransitions(); index++) {
                       AbstractGraphElement transition = sourceNode.getTransition(index);
                       if (transition instanceof Message) {
                           Message messageTransition = (Message) transition;
                           if (messageTransition.getTarget().equalsIgnoreCase(toDelete.getLabel())) {
                               transitionsToRemove.add(messageTransition.getUIIdentifier());
                           }
                       } else if (transition instanceof Guard) {
                           Guard guardTransition = (Guard) transition;
                           if (guardTransition.getTarget().equalsIgnoreCase(toDelete.getLabel())) {
                               transitionsToRemove.add(guardTransition.getUIIdentifier());
                           }
                       }
                   }
               }
           }
           /* deleting the transitions to be removed */
           transitionsToRemove.stream().map((transitionToRemove) -> {
               this.connectionIndex.get(transitionToRemove).deleteTransition(transitionToRemove);
               return transitionToRemove;
           }).forEachOrdered((transitionToRemove) -> {
               this.connectionIndex.remove(transitionToRemove);
           });
       }
       else {
           // check if this is an architecture node to delete
           ArchitectureNode aDelete = null;
           for (ArchitectureNode e : this.archElements) {
                if (e.getUIIdentifier().equalsIgnoreCase(ident)) {
                    aDelete = e;
                    break;
                }
            }
           if (aDelete != null){
                this.archElements.remove(aDelete);
                
                /* deleting all transitiong going FROM this Node */
                List<String> transitionsToRemove = new ArrayList<>();
                for(String index: this.componentsTransitions.keySet()){
                    if (this.componentsTransitions.get(index).getLabel().equals(aDelete.getLabel())){
                        transitionsToRemove.add(index);
                    }
                }

                for(String index: transitionsToRemove){
                    this.componentsTransitions.remove(index);
                }
                
                /* deleting all transitions going TO this Node */
                transitionsToRemove = new ArrayList<>();
                /* set is used to avoid double checking the same transitions from a given node*/
                Set<String> visitedNodes = new HashSet<>();
                for(ArchitectureNode sourceNode: this.componentsTransitions.values()){
                    if (visitedNodes.add(sourceNode.getUIIdentifier())) {
                        /* going through all transitions and checking if the target is the node we are deleting */
                        for (int index = 0; index < sourceNode.getNumberTransitions(); index++) {
                            AbstractGraphElement transition = sourceNode.getTransition(index);
                            ComponentTransition link = (ComponentTransition) transition;
                            if (link.getTarget().equalsIgnoreCase(aDelete.getLabel())) {
                                transitionsToRemove.add(link.getUIIdentifier());
                            }
                        }
                    }
                }
                /* deleting the transitions to be removed */
                transitionsToRemove.stream().map((transitionToRemove) -> {
                    this.componentsTransitions.get(transitionToRemove).deleteTransition(transitionToRemove);
                    return transitionToRemove;
                }).forEachOrdered((transitionToRemove) -> {
                    this.componentsTransitions.remove(transitionToRemove);
                });
           }
        }
   }
   
   /**
    * Add a new connection in the model.
    * @param connID The identifier of the connection.
    * @param srcID The ID of the source node of the connection.
    * @param trgtID The ID of the target node of the connection.
    * @return the created connection
    */
   public final AbstractGraphElement addConnection(final String connID, final String srcID, final String trgtID) {
       // use the type of the src to determine connection type
       final String type = getNode(srcID).getType();
       final String endType = getNode(trgtID).getType();
       // if src is a start or normal
      if (type.equalsIgnoreCase(XMLStateMachine.LOOP_LABEL) && endType.equalsIgnoreCase(XMLStateMachine.NORMAL_LABEL)) {
           final AbstractGraphElement connection = new Message(connID, "message", getNode(trgtID));
           final GraphNode src = (GraphNode) getNode(srcID);
           this.connectionIndex.put(connID, src);
           src.addTransition(connection);
           return connection;
       }
       else if (type.equalsIgnoreCase(XMLStateMachine.TRIGGER_LABEL) || type.equalsIgnoreCase(XMLStateMachine.TRIGGERSTART_LABEL)) {
           final AbstractGraphElement connection = new Message(connID, "message", getNode(trgtID));
           final GraphNode src = (GraphNode) getNode(srcID);
           this.connectionIndex.put(connID, src);
           src.addTransition(connection);
           return connection;
       } else {
           final AbstractGraphElement connection = new Guard(connID, "guard", getNode(trgtID));
           final GraphNode src = (GraphNode) getNode(srcID);
           this.connectionIndex.put(connID, src);
           src.addTransition(connection);
           return connection;
       }
   }
   
   /**
    * add a connection in the system graph component
    * @param connID the id of the transition
    * @param srcID the id of the source node
    * @param trgtID the id of the target node
    */
   public final void addComponentConnection(final String connID, final String srcID, final String trgtID){
       ArchitectureNode source = (ArchitectureNode) getNode(srcID);
       ArchitectureNode target = (ArchitectureNode) getNode(trgtID);
       this.componentsTransitions.put(connID, source);
       final AbstractGraphElement connection = new ComponentTransition(connID, target);
       source.addTransition(connection);
   }

   /**
    * Trace through the graph and change all references to the old label to
    * the new label.
    *
    * @param original The original state label
    * @param newLabel The new state label.
    */
   public final void updateConnectionLabel(final String original, final String newLabel) {
       for (GraphNode e : this.graphElements) {
            for (int i = 0; i < e.getNumberTransitions(); i++) {
                final AbstractGraphElement transition = e.getTransition(i);
                if (transition.getClass().toString().contains("Guard")) {
                    final Guard gTrans = (Guard) transition;
                    if (gTrans.getTarget().equalsIgnoreCase(original)) {
                        gTrans.setTarget(newLabel);
                    }
                } else if (transition.getClass().toString().contains("Message")) {
                    final Message gTrans = (Message) transition;
                    if (gTrans.getTarget().equalsIgnoreCase(original)) {
                        gTrans.setTarget(newLabel);
                    }
                }
            }
        }
   }
   
   /**
    * updates all transition which have their target a component node that has been renamed
    * @param original
    * @param newLabel 
    */
   public final void updateComponentLinkLabel(final String original, final String newLabel){
       for (ArchitectureNode e : this.archElements) {
            for (int i = 0; i < e.getNumberTransitions(); i++) {
                final AbstractGraphElement transition = e.getTransition(i);
                final ComponentTransition linkTransition = (ComponentTransition) transition;
                if (linkTransition.getTarget().equalsIgnoreCase(original)) {
                    linkTransition.setTarget(newLabel);
                }
            }
        }
   }

   /**
    * Update a connection in the graph to the new target label. Essentially
    * changing a connection from pointing between 2 nodes to other nodes.
    * @param connID The Id of the connection
    * @param srcID The id of the connection source.
    * @param trgtID The id of the connection target.
    * @throws InvalidTransitionException Error where trying to change to a connection
    * that doesn't exist in the graph.
    */
   public final void updateConnection(final String connID, final String srcID, final String trgtID) throws InvalidTransitionException {
        final GraphNode connx = this.connectionIndex.get(connID);
        if (connx == null) {
            final ArchitectureNode source = this.componentsTransitions.get(connID);
            if (source == null){
                return;
            }
            
            if (source.getUIIdentifier().equalsIgnoreCase(srcID)){
                // updating target of transition
                final ComponentTransition link = (ComponentTransition) source.getTransition(connID);
                if (link != null){
                    link.setTarget(getNode(trgtID).getLabel());
                }
            }
            else {
                source.deleteTransition(connID);
                this.componentsTransitions.put(connID, (ArchitectureNode) getNode(srcID));
            }
            
            return;
        }
        if (connx.getUIIdentifier().equalsIgnoreCase(srcID)) {
            final String type = connx.getType();
            if (type.equalsIgnoreCase(XMLStateMachine.START_LABEL) || type.equalsIgnoreCase(XMLStateMachine.NORMAL_LABEL)) {
                final Guard grd = (Guard) connx.getTransition(connID);
                if (grd != null) {
                    grd.setTarget(getNode(trgtID).getLabel());
                            }
           } else if (type.equalsIgnoreCase(XMLStateMachine.TRIGGER_LABEL) || type.equalsIgnoreCase(XMLStateMachine.TRIGGERSTART_LABEL)) {
                final Message msg = (Message) connx.getTransition(connID);
                if (msg != null) {
                    msg.setTarget(getNode(trgtID).getLabel());
                }
           }
        } else {
            String type = connx.getType();
            if (type.equalsIgnoreCase(XMLStateMachine.START_LABEL) || type.equalsIgnoreCase(XMLStateMachine.NORMAL_LABEL)) {
                type = getNode(srcID).getType();
                if (!(type.equalsIgnoreCase(XMLStateMachine.START_LABEL) || type.equalsIgnoreCase(XMLStateMachine.NORMAL_LABEL))) {
                    throw new InvalidTransitionException("Cannot reconnect to this type of src node");
                }
                final Guard grd = (Guard) connx.getTransition(connID);
                if (grd != null) {
                    ((GraphNode) getNode(srcID)).addTransition(grd);
                    connx.deleteTransition(grd.getUIIdentifier());
                    this.connectionIndex.put(connID, (GraphNode) getNode(srcID));
                }
            } else if (type.equalsIgnoreCase(XMLStateMachine.TRIGGER_LABEL) || type.equalsIgnoreCase(XMLStateMachine.TRIGGERSTART_LABEL)) {
                type = getNode(srcID).getType();
                if (!(type.equalsIgnoreCase(XMLStateMachine.TRIGGER_LABEL) || type.equalsIgnoreCase(XMLStateMachine.TRIGGERSTART_LABEL))) {
                    throw new InvalidTransitionException("Cannot reconnect to this type of src node");
                }
                final Message msg = (Message) connx.getTransition(connID);
                if (msg != null) {
                    ((GraphNode) getNode(srcID)).addTransition(msg);
                    connx.deleteTransition(msg.getUIIdentifier());
                    this.connectionIndex.put(connID, (GraphNode) getNode(srcID));
                }
            } else {
                throw new InvalidTransitionException("Cannot reconnect to this type of src node");
            }
        }

   }

   /**
    * Empty the data model. Method is typically used when the graph GUI needs
    * to be reset i.e. new graph, open graph, etc.
    */
   public final void clearData() {
       this.archElements.clear();
       this.connectionIndex.clear();
       this.componentsTransitions.clear();
       this.graphElements.clear();
       this.hasStart = false;
   }

    public final void updateState(DataModelState state) {
        if (state == null) {
            return;
        }

        this.hasStart = state.getHasStart();

        this.graphElements = new ArrayList<>();
        state.getGraphElements().forEach((graphNode) -> {
            this.graphElements.add((GraphNode) ObjectDeepCloner.deepCopy(graphNode));
        });

        this.archElements = new ArrayList<>();
        state.getArchitectureElements().forEach((archNode) -> {
            this.archElements.add((ArchitectureNode) ObjectDeepCloner.deepCopy(archNode));
        });

        this.connectionIndex = new HashMap<>();
        for(String index: state.getConnectionIndex().keySet()){
            for (GraphNode testNode: this.graphElements){
                if (testNode.getLabel().equals(state.getConnectionIndex().get(index).getLabel())){
                    this.connectionIndex.put(index, testNode);
                    break;
                }
            }
        }
        
        this.componentsTransitions = new HashMap<>();
        for(String index: state.getComponentsTransitions().keySet()){
            for (ArchitectureNode testNode: this.archElements){
                if (testNode.getLabel().equals(state.getComponentsTransitions().get(index).getLabel())){
                    this.componentsTransitions.put(index, testNode);
                    break;
                }
            }
        }
    }

   public final DataModelState getState() {
       return new DataModelState(this.graphElements, this.archElements, this.connectionIndex, this.componentsTransitions, this.hasStart);
   }

}
