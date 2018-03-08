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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor;

import java.util.Map;

/**
 * a manager class to handle the copying and pasting of graph components data
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class CopyPasteManager {

    /**
     * the GUI id of the most recently created GUI component
     */
    private String lastGUIid;

    /**
     * a getter method for the last GUI identifier
     * @return the gui id of the most recently created GUI component
     */
    public String getLastGUIid(){
        return lastGUIid;
    }

    /**
     * a setter method for the last GUI identifier
     * @param GUIid the gui id of the most recently created GUI component
     */
    public void setLastGUIid(String GUIid){
        lastGUIid = GUIid;
    }

    /**
     * the type of the most recently created GUI component
     */
    private String lastType;

    /**
     * a getter method for the last component type
     *
     * @return the type of the most recently created GUI component
     */
    public String getLastType() {
        return lastType;
    }

    /**
     * a setter method for the last component type
     *
     * @param type the type of the most recently created GUI component
     */
    public void setLastType(String type) {
        lastType = type;
    }


    /**
     * a map object to save the data that has to be passed when pasting the copied element
     */
    private Map data;

    /**
     * a getter method for the saved data
     * @return the saved data from the last copied component
     */
    public Map getData(){
        return data;
    }

    /**
     * a setter method for the saved data
     * @param data the new data that has to be saved
     */
    public void setData(Map data){
        this.data = data;
    }

    /**
     * A constructor for the CopyPasteManager class
     */
    public CopyPasteManager(){
        // empty implementation, nothing to initialise
    }

}
