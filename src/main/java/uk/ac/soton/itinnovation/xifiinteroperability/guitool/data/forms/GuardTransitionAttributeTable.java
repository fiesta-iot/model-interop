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

import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Function.FunctionType;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GuardData;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Function;

/**
 * The Guard transition attribute table is the list of rules that
 * are currently attached to the transition.
 * Hence, the table consists of rows of guards. The columns relate to
 * the three data elements of a guard:
 * 1) The function to evaluate (equals, notequals, contains, ...)
 * 2) The parameter name
 * 3) The value that the parameter guard must match
 *
 * The table is displayed as part of the GuardForm object.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class GuardTransitionAttributeTable extends AbstractTableModel {

    /**
     * The table uses a comboBox to input the function.
     */
    private final transient JComboBox comboBox = new JComboBox();

    /**
     * Get the reference to the combobox of the guard inputs.
     * @return The combobox inputs.
     */
    public final JComboBox getGuardCombo() {
        return comboBox;
    }

    /**
     * The table column header names.
     */
    private final transient String[] columnNames = {"Parameter", "Function", "Value"};

    /**
     * The displayed data rows.
     */
    private transient List<GuardData> data = new ArrayList();

    /**
     * the node for which the form refers
     */
    private Guard mirrorNode;

    /**
     * a setter for the mirrorNode
     * @param mirrorNode the new mirror node
     */
    public void setMirrorNode(Guard mirrorNode){
        this.mirrorNode = mirrorNode;
    }

    /**
     * Create a new table of guards.
     */
    public GuardTransitionAttributeTable() {
        super();
        // Initialise the input values for the combo box
        comboBox.setModel(new DefaultComboBoxModel(FunctionType.values()));
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
        final GuardData row = data.get(rowVal);
        switch (colVal) {
            case 0: return row.getGuardData();
            case 1: return row.getFuntionType();
            case 2: return row.getGuardValue();
            default:
                return null;
        }
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
    public final void setValueAt(final Object value, int rowVal, final int colVal) {
        if (rowVal >= data.size()){
            rowVal = data.size() - 1;
        }
        final GuardData row = data.get(rowVal);
        switch (colVal) {
            case 0:
                String strValue = (String) value;
                if (strValue == null || strValue.equals("")){
                    JOptionPane.showMessageDialog(comboBox, "Please fill a value for the guard description!",
                            "Invalid guard", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (strValue.equalsIgnoreCase("timeout")){
                    if (mirrorNode.getData().size() > 1){
                        JOptionPane.showMessageDialog(comboBox, "Timeout transitions can only have one guard for the timeout value. "
                                + "Delete your other guards first.", "Timeout transition erorr",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    else {
                        if (row.getFuntionType() != FunctionType.Equals) {
                            JOptionPane.showMessageDialog(comboBox, "A timeout guard can only be used with the 'equals' function.",
                                    "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        else {
                            try {
                                Long timeout = Long.parseLong(row.getGuardValue());
                                if (timeout <= 0) {
                                    JOptionPane.showMessageDialog(comboBox, "The value for a timeout guard must be a positive integer.",
                                            "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            catch (NumberFormatException ex){
                                JOptionPane.showMessageDialog(comboBox, "A timeout guard can only have integers as its guard value.",
                                        "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                }
                else if (strValue.equalsIgnoreCase("index")){
                    if (mirrorNode.getData().size() > 1){
                        JOptionPane.showMessageDialog(comboBox, "Counter transitions can only have one guard for the index value. "
                                + "Delete your other guards first.", "Counter transition erorr",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    else {
                        if (row.getFuntionType() != FunctionType.Counter) {
                            JOptionPane.showMessageDialog(comboBox, "A counter guard can only be used with the 'counter' function.",
                                    "Counter transition error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        else {
                            /**
                             * Removed check - counter can be evaluated to a prior state value.
                             */
//                            try {
//                                Integer counter = Integer.parseInt(row.getGuardValue());
//                                if (counter <= 0) {
//                                    JOptionPane.showMessageDialog(comboBox, "The value for an index guard must be a positive integer.",
//                                            "Counter transition error", JOptionPane.ERROR_MESSAGE);
//                                    return;
//                                }
//                            }
//                            catch (NumberFormatException ex){
//                                JOptionPane.showMessageDialog(comboBox, "A counter guard can only have integers as its guard value.",
//                                        "Counter transition error", JOptionPane.ERROR_MESSAGE);
//                                return;
//                            }
                        }
                    }
                }
                else if (strValue.equalsIgnoreCase("response-time")){
                    try {
                        Long responseTime = Long.parseLong((row.getGuardValue()));
                        if (responseTime <= 0){
                            JOptionPane.showMessageDialog(comboBox, "The value for a response-time guard must be a positive integer.",
                                    "Transition error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(comboBox, "The value for a response-time guard must be an integer representing the time in milliseconds.",
                                "Transition error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                else if (!strValue.equalsIgnoreCase("index")){
                    if (row.getFuntionType() == Function.FunctionType.Counter){
                        JOptionPane.showMessageDialog(comboBox, "You cannot use a counter function with a guard description different than 'Index'",
                                "Counter guard error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                row.setGuardData(strValue);
                break;
            case 1:
                if (row.getGuardData().equalsIgnoreCase("timeout")) {
                    if (((FunctionType) value) != FunctionType.Equals){
                        JOptionPane.showMessageDialog(comboBox, "The only function that can be used for a timeout guard is the 'equals' function.",
                                "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                else if (row.getGuardData().equalsIgnoreCase("index")) {
                    if (((FunctionType) value) != FunctionType.Counter){
                        JOptionPane.showMessageDialog(comboBox, "The only function that can be used for an index guard is the 'counter' function.",
                                "Counter transition error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                else if (((FunctionType) value) == FunctionType.Regex) {
                    try {
                        Pattern.compile(row.getGuardValue());
                    }
                    catch (PatternSyntaxException ex){
                        JOptionPane.showMessageDialog(comboBox, "The guard value is not a valid regular expression.",
                                "Regex error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                else if (((FunctionType) value) == FunctionType.Counter) {
                    if (row.getGuardData().equalsIgnoreCase("index")){
                        try {
                            Integer counter = Integer.parseInt((String) value);
                            if (counter <= 0) {
                                JOptionPane.showMessageDialog(comboBox, "The value for a counter guard must be a positive integer.",
                                        "Counter transition error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(comboBox, "The value for a counter guard must be an integer representing the number of iterations.",
                                    "Counter transition error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    else {
                        JOptionPane.showMessageDialog(comboBox, "The guard description for a counter guard must be 'index'.",
                                "Counter transition error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                row.setFunctionType((FunctionType) value);
                break;
            case 2:
                if (value == null || ((String) value).equals("")){
                    JOptionPane.showMessageDialog(comboBox, "Please fill a guard value!",
                            "Invalid guard", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (row.getGuardData().equalsIgnoreCase("timeout")) {
                    try {
                        Long timeout = Long.parseLong((String) value);
                        if (timeout <= 0) {
                            JOptionPane.showMessageDialog(comboBox, "The value for a timeout guard must be a positive integer.",
                                    "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(comboBox, "The value for a timeout guard must be an integer representing the time in milliseconds.",
                                "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                else if (row.getGuardData().equalsIgnoreCase("index")) {
//                    try {
//                        Integer counter = Integer.parseInt((String) value);
//                        if (counter <= 0) {
//                            JOptionPane.showMessageDialog(comboBox, "The value for an index guard must be a positive integer.",
//                                    "Counter transition error", JOptionPane.ERROR_MESSAGE);
//                            return;
//                        }
//                    }
//                    catch (NumberFormatException ex){
//                        JOptionPane.showMessageDialog(comboBox, "The value for an index guard must be an integer representing the number of iterations.",
//                                "Counter transition error", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
                }
                else if (row.getGuardData().equalsIgnoreCase("response-time")){
                    try {
                        Long responseTime = Long.parseLong((String) value);
                        if (responseTime <= 0){
                            JOptionPane.showMessageDialog(comboBox, "The value for a response-time guard must be a positive integer.",
                                    "Transition error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(comboBox, "The value for a response-time guard must be an integer representing the time in milliseconds.",
                                "Transition error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                else if (row.getFuntionType() == FunctionType.Regex){
                    try {
                        Pattern.compile((String) value);
                    }
                    catch (PatternSyntaxException ex){
                        JOptionPane.showMessageDialog(comboBox, "The guard value is not a valid regular expression.",
                                "Regex error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                row.setGuardValue((String) value);
        }
        fireTableCellUpdated(rowVal, colVal);
    }

    /**
     * Clear the table of data i.e. clear the view.
     */
    public final void clearData() {
        data.clear();
        data = new ArrayList();
        fireTableDataChanged();
    }

    /**
     * Add a new guard to the table view.
     * @param newrow The new guard to display.
     */
    public final void addRowData(final GuardData newrow) {
        data.add(newrow);
        fireTableDataChanged();
    }

    /**
     * Remove a guard from the table view.
     * @param row The guard to remove.
     */
    public final void removeRowData(final int row) {
        data.remove(row);
        fireTableRowsDeleted(row,row);
    }

    /**
     * Reset the table with a new set of guard data i.e. refresh the table
     * view of a given data set.
     * @param nGuard The data set to display in the table.
     */
    public final void setData(final Guard nGuard) {
        for (GuardData gd : nGuard.getData()) {
            addRowData(gd);
        }
    }
}