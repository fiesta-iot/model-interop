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
// Created By : Paul Grace
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.table.AbstractTableModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.AbstractGraphElement;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Message;
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
public class ChangeTable extends JPopupMenu {

    private final AbstractTableModel delTable;
    private final int row;
    private final AbstractGraphElement mirrorNode;
    /**
     * Create a new pop up menu associated with a text field.
     * @param editor The editor context.
     * @param table to remove the row from.
     * @param r row where the menu is triggered
     * @param mirrorNode the AbstractGraphElement to change
     */
    public ChangeTable(final BasicGraphEditor editor, AbstractTableModel table, int r, AbstractGraphElement mirrorNode) {
        super();
        this.delTable = table;
        this.row = r;
        this.mirrorNode = mirrorNode;
        add(editor.bind("Delete", new DeleteAction()));
    }

     /**
     * Copy selected text from field.
     */
    public class DeleteAction extends AbstractAction {
        /**
         * Copy text from text field to clipboard buffer.
         * @param actEvent The UI event
         */
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            if (mirrorNode instanceof Guard){
                String labelGuard = delTable.getValueAt(row, 0).toString();
                ((GuardTransitionAttributeTable) delTable).removeRowData(row);
                ((Guard) mirrorNode).removeGuard(labelGuard);
            }
            else if (mirrorNode instanceof Message){
                String labelMessage = delTable.getValueAt(row, 0).toString();
                ((MessageTableModel) delTable).removeRowData(row);
                ((Message) mirrorNode).removeHeader(labelMessage);
            }
            else if (mirrorNode instanceof ArchitectureNode){
                String labelMessage = delTable.getValueAt(row, 0).toString();
                ((ComponentTableModel) delTable).removeRowData(row);
                ((ArchitectureNode) mirrorNode).removeInterfaceData(labelMessage);
            }
            else if (mirrorNode instanceof GraphNode){
                String labelMessage = delTable.getValueAt(row, 0).toString();
                ((GraphNodeAttributeTable) delTable).removeRowData(row);
                ((GraphNode) mirrorNode).removeConstantData(labelMessage);
            }
        }
    }



}
