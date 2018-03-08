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
// Created By : Nikolay Stanchev
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data;

import java.io.Serializable;

/**
 * this class represents a transition between two component nodes, that is - a transition in the SystemGraphComponent
 * 
 * @author ns17
 */
public class ComponentTransition extends AbstractGraphElement implements Serializable {

    /**
     * the target label of this transition
     */
    private String target;
    
    /**
     * a getter for the target of this transition
     * @return the target node label
     */
    public String getTarget(){
        return target;
    }
    
    /**
     * a setter for the target of this transition
     * @param newTarget the new target label
     */
    public void setTarget (String newTarget){
        this.target = newTarget;
    }
    
    /**
     * constructor for a ComponentTransition
     * @param ident the id of the transition 
     * @param toNode the component node to which this transition leads
     */
    public ComponentTransition(String ident, ArchitectureNode toNode){
        super(ident, "link");
        this.target = toNode.getLabel();
    }
    
    /**
     * generates the XML for reconstructing this transition
     * @return the XML string for this transition
     */
    @Override
    public String generateTransitionXML() {
        final StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("\n\t\t\t<link>");
        strBuilder.append("\n\t\t\t\t<with>").append(this.target).append("</with>");
        strBuilder.append("\n\t\t\t</link>");
        return strBuilder.toString();
    }

}
