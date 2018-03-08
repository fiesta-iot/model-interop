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

import java.util.ArrayList;
import java.util.List;

/**
 * Undo manager for the data model state, used to keep track with the graph undo manager
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class XMLUndoManager {

    /**
     * a list to keep track of all undoable data model states
     */
    private List<DataModelState> undoXMLs;

    /**
     * a list to keep track of all redoable data model states
     */
    private List<DataModelState> redoXMLs;

    /**
     * constructor initialises the lists
     */
    public XMLUndoManager(){
        undoXMLs = new ArrayList<>();
        redoXMLs = new ArrayList<>();
    }

    /**
     * a method to clear this undo manager
     */
    public void clear(){
        undoXMLs = new ArrayList<>();
        redoXMLs = new ArrayList<>();
    }

    /**
     * undo method, returns null if undoXMLs is empty
     * @return the state before the last added data model state in undoXMLs and adds
     * the last one in the redoXMLs by removing it from the undoXMLs
     */
    public DataModelState undo(){
        if (undoXMLs.size() < 2){
            return null;
        }
        DataModelState modelState = undoXMLs.get(undoXMLs.size()-2);
        redoXMLs.add(undoXMLs.remove(undoXMLs.size()-1));
        return modelState;
    }

    /**
     * redo method, returns null if redoXMLs is empty
     * @return last added data model state in redoXMLs and adds it to the undoXMLs
     * by removing it from the redoXMLs
     */
    public DataModelState redo(){
        if (redoXMLs.isEmpty()){
            return null;
        }

        DataModelState lastUndoneXML = redoXMLs.remove(redoXMLs.size()-1);
        undoXMLs.add(lastUndoneXML);
        return lastUndoneXML;
    }

    public boolean canUndo(){
        return this.undoXMLs.size() >= 2;
    }

    public boolean canRedo(){
        return !this.redoXMLs.isEmpty();
    }

    /**
     * add an data model state to the list of data model states
     * @param state the data model state
     */
    public void add(DataModelState state){
        undoXMLs.add(state);

        redoXMLs = new ArrayList<>();
    }
}
