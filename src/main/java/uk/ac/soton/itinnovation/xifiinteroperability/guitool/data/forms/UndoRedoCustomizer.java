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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

/**
 * This class is used to add undo/redo manager to all text fields of components, states, etc.
 * 
 * @author ns17
 */
public class UndoRedoCustomizer {
    
    private final static String UNDO = "UNDO";
    
    private final static String REDO = "REDO";
    
    /**
     * this class adds an undo manager and all the appropriate listeners to a text component
     * @param textField the text field to use
     */
    public static void addUndoManager(JTextComponent textField){
        final UndoManager undoManager = new UndoManager();
        textField.getDocument().addUndoableEditListener(undoManager);
        
        // add the action to the action map - undo
        textField.getActionMap().put(UNDO, new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (undoManager.canUndo()){
                    undoManager.undo();
                }
            }
        });

        // add the keystroke to the input map - undo
        textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), UNDO);

        // add the action to the action map - redo
        textField.getActionMap().put(REDO, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        });

        // add the keystroke to the input map - redo
        textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), REDO);

    }
}
