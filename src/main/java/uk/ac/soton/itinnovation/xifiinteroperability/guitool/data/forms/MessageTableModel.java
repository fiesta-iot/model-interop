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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ConstantData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Message;

/**
 * Table related to the data attached to message transitions. This contains
 * the HTTP header information.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class MessageTableModel extends AbstractTableModel {

    /** The column headers are attribute values. */
    private final transient String[] columnNames = {"Header",
                        "Value"};

    /**
     * The list of constant data attached to the view: table of name
     * value pairs.
     */
    private transient List<ConstantData> data = new ArrayList();

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
    public final Class getColumnClass(final int colVal) {
        return getValueAt(0, colVal).getClass();
    }

    @Override
    public final boolean isCellEditable(final int row, final int col) {
       return true;
    }

    @Override
    public final void setValueAt(final Object value, final int rowVal, final int colVal) {
        final ConstantData row = data.get(rowVal);
        if (colVal == 0) {
            row.setFieldName((String) value);
        } else if (colVal == 1) {
            row.setFieldValue((String) value);
        }
        fireTableCellUpdated(rowVal, colVal);
    }

    /**
     * Clear the information in the message table.
     */
    public final void clearData() {
        data.clear();
        data = new ArrayList();
        fireTableDataChanged();
    }

    /**
     * Add a new header row to the table.
     * @param newrow The http header information.
     */
    public final void addRowData(final ConstantData newrow) {
        data.add(newrow);
        fireTableDataChanged();
    }

    /**
     * Remove a header from the table view.
     * @param row The header to remove.
     */
    public final void removeRowData(final int row) {
        data.remove(row);
        fireTableRowsDeleted(row,row);
    }

    /**
     * Change the view of the table to the new message header data set.
     * @param msgVal The new header data set.
     */
    public final void setData(final Message msgVal) {
        for (ConstantData gd : msgVal.getConstantData()) {
            addRowData(gd);
        }
    }

}