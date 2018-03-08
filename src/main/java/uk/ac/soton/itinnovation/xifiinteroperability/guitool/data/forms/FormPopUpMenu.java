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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;

/**
 * The menu that pops up when right click on a field in the UI form. Generally
 * applies cut/copy/paste actions to the menu.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class FormPopUpMenu extends JPopupMenu {

    /**
     * The textfield this pop up instance works with.
     */
    private final transient JTextComponent bufferField;

    /**
     * reference to the data model
     */
    private final DataModel dataModel;

    /**
     * Create a new pop up menu associated with a text field.
     * @param editor The editor context.
     * @param txtField The text field to cut/copy/paste.
     */
    public FormPopUpMenu(final BasicGraphEditor editor, final JTextComponent txtField) {
        super();
        add(editor.bind("Insert pattern data", new InsertPatternDataAction()));
        add(editor.bind("Insert previous states data", new InsertPreviousStatesDataAction()));
        add(editor.bind("Copy", new CopyAction()));
        add(editor.bind("Paste", new PasteAction()));
        add(editor.bind("Cut", new CutAction()));
        this.bufferField = txtField;
        this.bufferField.setToolTipText("Click right button for helpers.");
        this.dataModel = editor.getDataModel();
    }

     /**
     * Copy selected text from field.
     */
    public class CopyAction extends AbstractAction {
        /**
         * Copy text from text field to clipboard buffer.
         * @param actEvent The UI event
         */
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            bufferField.copy();

        }
    }

    /**
     * The text field cut action.
     */
    public class CutAction extends AbstractAction {
        /**
         * Cut text from text field to clipboard buffer.
         * @param actEvent The UI event
         */
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            bufferField.cut();

        }
    }

    /**
     * The textfield paste action.
     */
    public class PasteAction extends AbstractAction {
        /**
         * Paste text from clipboard to text field.
         * @param actEvent The UI event
         */
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            bufferField.paste();

        }
    }

    /**
     * insert action for inserting pattern specific data
     */
    public class InsertPatternDataAction extends AbstractAction {
        /**
         * Choose pattern data and insert it
         * @param actEvent the UI event
         */
        @Override
        public final void actionPerformed(final ActionEvent actEvent){
            GraphNode startNode = dataModel.getStartNode();
            List<String> patternDataIDs = new ArrayList<>();
            startNode.getConstantData().forEach((data) -> {
                patternDataIDs.add(data.getFieldName());
            });
            if (patternDataIDs.size() > 0){
                Object result = JOptionPane.showInputDialog(bufferField,
                        "Please choose the ID of the pattern data you want to insert.",
                        "Inserting pattern data", JOptionPane.PLAIN_MESSAGE,
                        null, patternDataIDs.toArray(), patternDataIDs.get(0));

                if (result == null){
                    return;
                }
                String selected = result.toString();
                selected = "$$patterndata." + selected + "$$";
                try {
                    bufferField.getDocument().insertString(bufferField.getCaretPosition(), selected, null);
                } catch (BadLocationException ex) {
                    // empty block since we shouldn't be entering this catch block
                }
            }
            else {
                JOptionPane.showMessageDialog(bufferField,
                        "You haven't added any pattern data, yet.",
                        "No pattern data found", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * insert data from previous states
     */
    public class InsertPreviousStatesDataAction extends AbstractAction {
        /**
         * Choose previous states data and insert it
         * @param ae the UI event
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            StringBuilder expression = new StringBuilder();
            expression.append("$$");
            List<GraphNode> states = dataModel.getGraphElements();
            if (states.isEmpty()){
                JOptionPane.showMessageDialog(bufferField,
                        "There are no states available.",
                        "No states found", JOptionPane.WARNING_MESSAGE);
            }

            Object chosenState = JOptionPane.showInputDialog(bufferField,
                    "Please choose a state.", "Choosing state",
                    JOptionPane.PLAIN_MESSAGE, null, states.toArray(), states.get(0));

            if (chosenState == null){
                return;
            }

            expression.append(chosenState.toString());
            expression.append("|");

            String[] dataTypes = {"content", "headers"};
            Object chosenType = JOptionPane.showInputDialog(bufferField,
                    "Please choose what data you want to extract.", "Choosing type",
                    JOptionPane.PLAIN_MESSAGE, null, dataTypes, dataTypes[0]);

            if (chosenType == null){
                return;
            }

            expression.append(chosenType.toString().toLowerCase());
            expression.append("|");

            String identifier;
            if (chosenType.toString().equalsIgnoreCase("content")){
                Object path = JOptionPane.showInputDialog(bufferField, "Please type your XPath or JSONPath",
                        "Choosing path", JOptionPane.PLAIN_MESSAGE);
                if (path == null){
                    return;
                }
                identifier = path.toString();
            }
            else {
                Object id = JOptionPane.showInputDialog(bufferField,
                        "Please type the id of the header",
                        "Choosing ID", JOptionPane.PLAIN_MESSAGE);
                if (id == null){
                    return;
                }
                identifier = id.toString();
            }

            expression.append(identifier);
            expression.append("$$");

            try {
                bufferField.getDocument().insertString(bufferField.getCaretPosition(), expression.toString(), null);
            } catch (BadLocationException ex) {
                // empty block since we shouldn't be entering this catch block
            }
        }
    }
}
