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

/**
 * Each element of the data model is an implementation of the abstract class
 * AbstractGraphElement which represents an element of the drawn graph.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public abstract class AbstractGraphElement implements Serializable{

    /**
     * The user interface generates an ID for each graph element drawn. This
     * is used to relate the UI to the data model
     */
    private final String uiIdentifier;

    /**
     * Each data element has a short label description e.g. node1, connection2,
     */
    private String label;

    /**
     * The type of the graph element: node, connection, interface, etc.
     */
    private final String type;

    /**
     * Create a new Graph element data node. This is a label and a type with
     * the UI identifier from mx graph.
     * @param ident The mxGraph generated unique UI identifier.
     * @param newType The type of the node.
     */
    public AbstractGraphElement(final String ident, final String newType) {
        this.uiIdentifier = ident;
        this.type = newType;
        this.label = newType + ident;
    }

    /**
     * Create a new Graph element data node. This constructor takes the label as
     * argument.
     * @param ident The mxGraph generated unique UI identifier.
     * @param label
     * @param newType The type of the node.
     */
    public AbstractGraphElement(final String ident, final String label, final String newType){
        this(ident, newType);
        this.label = label;
    }

    /**
     * Translate the data structure into the XML content as described
     * by the schema: "Pattern.xsd".
     * @return An xml representation of the graph element.
     */
    public abstract String generateTransitionXML();

    /**
     * Each node in the graph has a unique identifier that observes
     * its relationship to the graphical representation of the graph.
     * @return the data elements UI identifier.
     */
    public final String getUIIdentifier() {
        return this.uiIdentifier;
    }

    /**
     * Get the type of the data node.
     * @return data elements UI type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the label of the data node.
     * @return the label value
     */
    public final String getLabel() {
        return this.label;
    }

    /**
     * Set the label of the node. This is editable by outside classes so that
     * the node label can be changed at runtime.
     * @param newLabel The new label displayed against the graph element.
     */
    public final void setLabel(final String newLabel) {
       this.label = newLabel;
    }
}
