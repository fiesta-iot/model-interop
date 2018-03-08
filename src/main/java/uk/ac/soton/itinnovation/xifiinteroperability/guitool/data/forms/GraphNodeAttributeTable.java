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

import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ConstantData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;

/**
 * Table related to the data attached to graph nodes. Essentially, there
 * is little need for data attached to nodes at present (although in future
 * this may be extended e.g. time outs in timed automata.
 *
 * Attach name value pairs for constant values used in the graph.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class GraphNodeAttributeTable extends AbstractTableModel {

    /**
     * The column headers are attribute values.
     */
    private final transient String[] columnNames = {"Attribute",
                        "Value"};

    /**
     * The list of data attached to the view.
     */
    private transient List<ConstantData> data = new ArrayList();
    
    /**
     * reference to the editor
     */
    private transient final BasicGraphEditor editor;
    
    
    public GraphNodeAttributeTable(BasicGraphEditor editor){
        super();
        
        this.editor = editor;
    }

    @Override
    public final int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public final int getRowCount() {
        return data.size();
    }

    @Override
    public final String getColumnName(final int col) {
        return columnNames[col];
    }

    @Override
    public final Object getValueAt(final int rowVal, final int colVal) {
        final ConstantData row = data.get(rowVal);
        if (colVal == 0) {
            return row.getFieldName();
        } else if (colVal == 1) {
            return row.getFieldValue();
        }
        return null;
    }

    @Override
    public final  Class getColumnClass(final int colVal) {
        return getValueAt(0, colVal).getClass();
    }

    @Override
    public final boolean isCellEditable(final int row, final int col) {
       return true;
    }

    @Override
    public final  void setValueAt(final Object value, final int rowVal, final int colVal) {
        final ConstantData row = data.get(rowVal);
        if (colVal == 0) {
            // if setting the same value do nothing
            if (row.getFieldName().equalsIgnoreCase((String) value)){
                return;
            }
            
            // check for duplicate pattern variable IDs
            for (ConstantData data : data) {
                if (data.getFieldName().equalsIgnoreCase((String) value)) {
                    JOptionPane.showMessageDialog(editor,
                            "Variable with this name already exists.",
                            "Variable data error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            editor.getCollectionsBrowserPanel().getMultiTestsManager().removePatternValueID(editor.getCollectionsBrowserPanel().getCurrentModel(), row.getFieldName());
            row.setFieldName((String) value);
        } else if (colVal == 1) {
            row.setFieldValue((String) value);
        }
        fireTableCellUpdated(rowVal, colVal);
    }

    /**
     * Clear the entries in the node attribute table.
     */
    public final  void clearData() {
        data.clear();
        data = new ArrayList();
        fireTableDataChanged();
    }

    /**
     * Add a new attribute to the table.
     * @param newrow The row with tht attribute data.
     */
    public final  void addRowData(final ConstantData newrow) {
        data.add(newrow);
        fireTableDataChanged();
    }

    /**
     * Remove a full row from the table using the data in the interface data object.
     * @param row The row of information.
     */
    public final void removeRowData(final int row) {
        data.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /**
     * Set the table to view the given attributes.
     * @param gNode The attribute information to be displayed in table form.
     */
    public final  void setData(final GraphNode gNode) {
        for (ConstantData gd : gNode.getConstantData()) {
            addRowData(gd);
        }
    }
}