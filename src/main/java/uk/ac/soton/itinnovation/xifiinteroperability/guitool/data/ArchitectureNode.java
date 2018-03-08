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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An architecture node is the data representation of the two graph elements:
 1) Interface: a rest interface described by its url
 2) A client: a rest user described by its address location.
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @see AbstractGraphElement
 * @author Paul Grace
 */
public class ArchitectureNode extends AbstractGraphElement implements Serializable {

    /**
     * the list of transitions for a component node
     */
    private final List<AbstractGraphElement> transitions;
    
    /**
     * The address of the interface or client - typically the IP address in string.
     */
    private String address;

    /**
     * Getter for the distributed system node address.
     * @return The endpoint address id.
     */
    public final String getAddress() {
        return address;
    }

    /**
     * The id of the graph label of this component node
     */
    private final String nodeLabelID;

    /**
     * Getter for the id of the node graph label
     * @return the nodeLabelID of the node
     */
    public final String getNodeLabelID(){
        return nodeLabelID;
    }

    /**
     * The list of data parameters attached to the node. These are the
     * descriptive params adding additional information e.g. urls, ...
     */
    private List<InterfaceData> data = new ArrayList();

    /**
     * Getter for the data parameters.
     * @return The list of data name, value pairs.
     */
    public final List<InterfaceData> getData() {
        return data;
    }

    /**
     * a setter for the interface data list,
     * used by the copy paste manager to totally override the existing list of interface data
     * with the copied one
     * @param data the new list with interface data
     */
    public final void setInterfaceData(List<InterfaceData> data){
        this.data = data;
    }

    /**
     * Constructor for the node about the system element.
     * @param idnty The label identifying the system node.
     * @param label The component label identifier, same as the GUI label for the component
     * @param type The type of node, client or interface.
     * @param nodeLabelID id of the node graph label
     */
    public ArchitectureNode(final String idnty, final String label, final String type, final String nodeLabelID) {
        super(idnty, label, type);
        this.address = "127.0.0.1";
        this.nodeLabelID = nodeLabelID;
        this.transitions = new ArrayList<>();
    }

    /**
     * Add a new transition between two component nodes
     * @param transition the id of the transition
     */
    public final void addTransition(final AbstractGraphElement transition){
        this.transitions.add(transition);
    }
    
    /**
     * get a transition by id
     * @param id The id of the connection to read
     * @return The graph element data structure describing the transition
     */
    public final AbstractGraphElement getTransition(final String id) {
        for (AbstractGraphElement e : this.transitions) {
            if (e.getUIIdentifier().equalsIgnoreCase(id)) {
                return e;
            }
        }
        return null;
    }
    
    /**
     * Delete a transition by id
     * @param id  The transition id to delete
     */
    public final void deleteTransition(final String id) {
        AbstractGraphElement toDelete = null;
        for (AbstractGraphElement e : this.transitions) {
            if (e.getUIIdentifier().equalsIgnoreCase(id)) {
                toDelete = e;
                break;
            }
        }
        this.transitions.remove(toDelete);
    }
    
    /**
     * a method to delete a transition by giving its target label as argument
     * @param toLabel the target label
     */
    public final void deleteTransitionByLabel(String toLabel){
        AbstractGraphElement toDelete = null;
        for (AbstractGraphElement e: this.transitions){
            if (((ComponentTransition) e).getTarget().equalsIgnoreCase(toLabel)){
                toDelete = e;
                break;
            }
        }

        if (toDelete != null) {
            this.transitions.remove(toDelete);
        }
    }
    
    /**
     * Get the number of transitions from this node.
     * @return Integer count of transitions from this node.
     */
    public final int getNumberTransitions() {
        return this.transitions.size();
    }
    
    /**
     * Get the indexed transition e.g. first, second.
     * @param index The index of the graph element.
     * @return transition data object.
     */
    public final AbstractGraphElement getTransition(final int index) {
        return (AbstractGraphElement) this.transitions.get(index);
    }

    /**
     * Add new input data about a REST Interface.
     * @param idnty The data identifier of the interface e.g. "itf1"
     * @param url The fully qualified REST url as a string.
     * @param protocol The different protocol type
     */
    public final void addInterfaceData(final String idnty, final String url, final String protocol) {
        data.add(new InterfaceData(idnty, url, protocol));
    }

    /**
     * a method which removes interface data from the component
     * @param restID the id of the interface to remove
     */
    public final void removeInterfaceData(final String restID){
        InterfaceData toRemove = null;
        for(InterfaceData interfaceData: this.data){
            if (interfaceData.getRestID().equalsIgnoreCase(restID)){
                toRemove = interfaceData;
                break;
            }
        }

        if (toRemove != null){
            data.remove(toRemove);
        }
    }
    
    /**
     * Change the data information about the ID and the address.
     * @param idnty The ID of the interface or client
     * @param addr The address of the interface or client
     */
    public final void setData(final String idnty, final String addr) {
        this.setLabel(idnty);
        this.address = addr;
    }

    /**
     * Generate the xml representation of this system node to form part of the
     * pattern. It will be a component tag.
     * @return The xml specification of the xml component tag.
     */
    @Override
    public final String generateTransitionXML() {
        final StringBuilder strBuilder = new StringBuilder();
        // Add the ident
        strBuilder.append("\n\t\t<component>");
        strBuilder.append("\n\t\t\t<id>").append(this.getLabel()).append("</id>");
        strBuilder.append("\n\t\t\t<address>").append(this.address).append("</address>");

        // Add the transitions
        transitions.forEach((t) -> {
            strBuilder.append(t.generateTransitionXML());
        });
        
        // Add the interfaces
        for (InterfaceData dfield : this.data) {
            strBuilder.append("\n\t\t\t\t<interface>");
            strBuilder.append("\n\t\t\t\t\t<id>").append(dfield.getRestID()).append("</id>");
            strBuilder.append("\n\t\t\t\t\t<url>").append(dfield.getRestURL()).append("</url>");
            strBuilder.append("\n\t\t\t\t\t<protocol>").append(dfield.getProtocol()).append("</protocol>");
            strBuilder.append("\n\t\t\t\t</interface>");
        }

        strBuilder.append("\n\t\t</component>");
        return strBuilder.toString();
    }

    /**
     * Get the component type - in this case it is a component as part of the
     * system graph.
     * @return The component label
     */
    @Override
    public final String getType() {
        return "component";
    }
}
