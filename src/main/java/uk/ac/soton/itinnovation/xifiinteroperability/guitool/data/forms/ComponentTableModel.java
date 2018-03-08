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

import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.InterfaceData;

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

public class ComponentTableModel extends AbstractTableModel {

    /**
     * The column headers of the table.
     */
    private static final String[] COLUMNHEADERS = {"REST Interface ID", "URL"};

    /**
     * The list of data attached to the view.
     */
    private transient List<InterfaceData> data = new ArrayList();

    @Override
    public final int getColumnCount() {
        return COLUMNHEADERS.length;
    }

    @Override
    public final int getRowCount() {
        return data.size();
    }

    @Override
    public final String getColumnName(final int col) {
        return COLUMNHEADERS[col];
    }

    /**
     * Get the table value at the rowNumber row, y column position.
     * @param rowNumber The row number
     * @param columnNumber The column number
     * @return The object value.
     */
    @Override
    public final Object getValueAt(final int rowNumber, final int columnNumber) {
        final InterfaceData row = data.get(rowNumber);
        if (columnNumber == 0) {
            return row.getRestID();
        } else if (columnNumber == 1) {
            return row.getRestURL();
        }
        return null;
    }

    /**
     * Change a value in the table at rown and column position.
     * @param value The new value.
     * @param rowNum The row number.
     * @param column The column number.
     */
    @Override
    public final void setValueAt(final Object value, int rowNum, final int column) {
        if (rowNum >= data.size()) {
            rowNum = data.size() - 1;
        }
        final InterfaceData row = data.get(rowNum);

        if (column == 0) {
            for(InterfaceData interfaceData: data){
                if (interfaceData.getRestID().equalsIgnoreCase((String) value)){
                    JOptionPane.showMessageDialog(null,
                            "An interface with this id already exists.",
                            "Interface error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            row.setRestID((String) value);
        }
        else if (column == 1) {
            String newUrl = (String) value;
            boolean hasPortNumber = Pattern.matches(ComponentForm.REGEX, newUrl);
            if (!hasPortNumber){
                JOptionPane.showMessageDialog(null,
                        "The url of the interface is not valid. Keep in mind that a port number must be specified. For instance - 'http://127.0.0.1:8080/'",
                        "Invalid URL",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            else {
                Pattern p = Pattern.compile(ComponentForm.PORT_REGEX);
                Matcher m = p.matcher(newUrl);
                m.find();
                String portStr = newUrl.substring(m.start()+1, m.end());
                try {
                    Integer port = Integer.parseInt(portStr);
                    if (port > 65536){
                        JOptionPane.showMessageDialog(null,
                                "The port number in the url must be between 0 and 65536.",
                                "Port number exceeding limit", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(null,
                            "The specified port number in the url is invalid!",
                            "Invalid port number", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            row.setRestURL((String) value);
        }

        fireTableCellUpdated(rowNum, column);
    }

    @Override
    public final boolean isCellEditable(final int row, final int col) {
       return true;
    }

    /**
     * Clear all the data in the table.
     */
    public final void clearData() {
        data.clear();
        data = new ArrayList();
        fireTableDataChanged();
    }

    /**
     * Add a full row to the table using the data in the interface data object.
     * @param newrow The row of information.
     */
    public final void addRowData(final InterfaceData newrow) {
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
     * Overwrite the table view with a full set of data from a new node in the
     * graph.
     * @param arcNode The selected node in the system graph.
     */
    public final void setData(final ArchitectureNode arcNode) {
        for (InterfaceData gd : arcNode.getData()) {
            addRowData(gd);
        }
    }

}