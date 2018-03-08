/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2015
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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections;

/**
 * This class encapsulates the state of an opened model, that is the current XML model,its current name and path
 * 
 * @author ns17
 */
public class ModelState {
    
    /**
     * The constructor for a model state.
     * @param name name of the model
     * @param xml xml of the model
     * @param path path of the model file
     * @param modified the modified flag value
     */
    public ModelState(String name, String xml, String path, boolean modified){
        this.name = name;
        this.xml = xml;
        this.path = path;
        this.modified = modified;
    }

    /**
     * The current xml of the model.
     */
    private String xml;

    /**
     * Getter method for the XML of the current model state
     *
     * @return the current xml
     */
    public String getXml() {
        return xml;
    }
    
    /**
     * Setter method for the XML of the current model state
     *
     * @param xml the new xml
     */
    public void setXML(String xml) {
        if (xml != null) {
            this.xml = xml;
        } else {
            throw new NullPointerException("XML of the model state cannot be null.");
        }
    }

    /**
     * The current name of the model.
     */
    private String name;

    /**
     * Getter method for the name of the current model state
     *
     * @return the current name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Setter method for the name of the current model state
     *
     * @param name the new name
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name;
        } else {
            throw new NullPointerException("Name of the model state cannot be null.");
        }
    }

    /**
     * The current path of the model.
     */
    private String path;

    /**
     * Getter method for the path of the current model state
     *
     * @return the current path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Setter method for the path of the current model state
     *
     * @param path the new path
     */
    public void setPath(String path) {
        if (path != null) {
            this.path = path;
        } else {
            throw new NullPointerException("Path of the model state cannot be null.");
        }
    }
    
    /**
     * The current flag saying whether the model is modified or not.
     */
    private boolean modified;
    
    /**
     * Getter method for the 'modified' flag of the current model state
     *
     * @return the current 'modified' flag
     */
    public boolean getModified() {
        return modified;
    }
    
    /**
     * Setter method for the 'modified' flag of the current model state
     *
     * @param modified the new flag
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
