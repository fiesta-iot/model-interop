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

import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Function;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.AttributePanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator.JSONPathGeneratorEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XPathGenerator.XPathGeneratorEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;

/**
 * The form to input data attached to a guard transition. It appears in the
 * left hand panel when a guard transition is clicked in the graph.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */

public class GuardForm extends JPanel {

    /**
     * the html helper text for guard description
     */
    private final static String DESCRIPTION_HELPER = "<html><body>"
            + "<font size=+1><b><i>Guard description</i></b></font><br>"
            + "This can be either an HTTP field, a value from the content extracted using XPath or JSONPath depending on the content-type<br>"
            + " or a non-functional requirement - timeout for observing events or response time guard.<br><br>"
            + "1) Common HTTP fields:<br>"
            + "<ul><li>HTTP.from</li></li><li>HTTP.code</li><li>HTTP.msg</li>"
            + "<li>HTTP.date</li><li>HTTP.to</li><li>HTTP.expires</li><li>HTTP.content-type</li>"
            + "<li>HTTP.server</li><li>HTTP.transfer-encoding</li><li>HTTP.accept-ranges</li></ul>"
            + "2) Content extraction:<br>"
            + "<ul><li>Use the following format - <b>content[XPath/JSONPath]</b></li></ul>"
            + "3) Non-functional requirements:<br>"
            + "<ul><li>Response-time - to test the time it took to receive a response after a request was sent use<br>"
            + " <b>response-time</b> for guard description and specify the time in milliseconds as a guard value.</li>"
            + "<li>Timeout - to specify a maximum time a state should be waiting for an event to occur use <b>timeout</b> for<br>"
            + " guard description and specify the time in milliseconds as a guard value. Then choose the equal function.<br>"
            + " Keep in mind that timeout transitions can have only one guard, which is the timeout guard.</li></ul>"
            + "</body></html>";

    /**
     * the html helper text for guard value
     */
    private final static String VALUE_HELPER = "<html><body>"
            + "<font size=+1><b><i>Guard value</i></b></font><br>"
            + "This can be any value you want to compare against. "
            + "There are three special cases.<br><br>"
            + "1) If you want to use pattern data:"
            + "<ul><li>Use the following format - <b>$$patterndata.id$$</b> - replace 'id' "
            + "with the id of the pattern data you want to use</li>"
            + "<li>You can also click the right button of the mouse and select "
            + "the <b><i>'Insert pattern data'</i></b> option</li></ul>"
            + "2) If you want to use the address of a certain component:"
            + "<ul><li>Use the following format - "
            + "<b>component.id.address</b> - substite 'id' with the id of the component you want to use</li><br>"
            + "<li>Example : <b>component.fixer.address</b> - here fixer is the id of the component, for which"
            + " we want to use the address</li></ul>"
            + "3) If you want to use data from previous states - you can use content or "
            + "headers data from previous states:"
            + "<ul><li>Use the following format - "
            + "<b>$$state_label | {content or headers} | {XPath or JSONPath or header_id}$$</b>"
            + "</li><br>"
            + "<li>Example 1: <b>$$A2|content|//result/total_price$$</b><br>"
            + "Here, A2 is a label of a state with content of XML type and "
            + "the XPath '//result/total_price' is used to extract a value from the content.<br></li>"
            + "<li>Example 2: <b>$$A4|content|tenants[0].id$$</b><br>"
            + "Here, A4 is a label of a state with content of JSON type and "
            + "the JSONpath 'tenants[0].id' is used to extract a value from the content.<br></li>"
            + "<li> Example 3: <b>$$A1|headers|content-type$$</b><br>"
            + "Here, A1 is a label of a state with headers and the value of the header with "
            + "name 'content-type' is extracted.</li><br>"
            + "<li>You can also click the right button of the mouse and select "
            + "the <b><i>'Insert previous states data'</i></b> option</li></ul>"
            + "</body></html>";

    /**
     * the posible header fields for guard description
     */
    private final String[] headerFields = {"from", "code", "msg", "date", "to", "expires", "content-type",
        "server", "transfer-encoding", "accept-ranges"};

    /**
     * The user interface model i.e. this data is what this form is
     * viewing upon - list of guards on a particular transition.
     */
    private final transient GuardTransitionAttributeTable guardView;

    /**
     * Viewable data fields matched with the GUI element's data.
     */

    /**
     * The text area to input the identity data.
     */
    private final transient JTextField parameterToTest;

    /**
     * The text area to enter the machine address.
     */
    private final transient JTextField parameterValue;

    /**
     * the combo box for the guard function
     */
    private final JComboBox comboBox = new JComboBox();

    /**
     * The form has a one-to-many relationship with an architecture node. The
     * form is a changing view of the selected node. This field stores the
     * current selected node (via setData() method)
     */
    private transient Guard mirrorNode;

    /**
     * Create a form with the specified labels, tooltips, and sizes.
     * @param editor a basic graph editor
     */
    public GuardForm(final BasicGraphEditor editor) {
        super(new BorderLayout());
        guardView = new GuardTransitionAttributeTable();
        final JTable guardTable = new JTable(guardView);
        guardTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = guardTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < guardTable.getRowCount()) {
                    guardTable.setRowSelectionInterval(r, r);
                } else {
                    guardTable.clearSelection();
                }

                int rowindex = guardTable.getSelectedRow();
                if (rowindex < 0)
                    return;
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                    JPopupMenu popup = new ChangeTable(editor, guardView, r, mirrorNode);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

         // The guard table needs a dropbox input field
        guardTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(guardView.getGuardCombo()));

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Info Panel
        final JPanel listPane = new JPanel();
        final GridLayout gridLayout = new GridLayout(7 , 3);
        gridLayout.setHgap(5);
        gridLayout.setVgap(5);
        listPane.setLayout(gridLayout);

        final JLabel title = new JLabel(" Transition Tests", JLabel.LEFT);
        final Font font = title.getFont();
        final Map attributes = font.getAttributes();
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        title.setFont(font.deriveFont(attributes));

        // Row 1
        listPane.add(title);
        listPane.add(new JLabel("", JLabel.LEFT));
        listPane.add(new JLabel("", JLabel.LEFT));

        // Row 2
        listPane.add(new JLabel("Function:",  JLabel.RIGHT));
        comboBox.setModel(new DefaultComboBoxModel(Function.FunctionType.values()));
        comboBox.setSelectedItem(Function.FunctionType.Equals);
//        final JPanel comboPanel = new JPanel();
//        comboPanel.add(comboBox, BorderLayout.CENTER);
//        comboPanel.getRenderer().setHorizontalAlignment(JLabel.RIGHT);
        listPane.add(comboBox, JLabel.RIGHT);
        listPane.add(new JLabel("", JLabel.LEFT));

        // Row 3
        listPane.add(new JLabel("Parameter to test:",  JLabel.RIGHT));
        parameterToTest = new JTextField();
        UndoRedoCustomizer.addUndoManager(parameterToTest);
        comboBox.addItemListener((ItemEvent ie) -> {
            if (ie.getStateChange() == ItemEvent.SELECTED) {
                adjustGuardDescription();
            }
            else if (ie.getStateChange() == ItemEvent.DESELECTED){
                if (ie.getItem() == Function.FunctionType.Counter){
                    parameterToTest.setText("");
                }
            }
        });
        parameterToTest.setToolTipText("Click right button for selection dialog.");
        parameterToTest.addFocusListener(MessageForm.COLOUR_CHANGER);
        parameterToTest.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if (!parameterToTest.isEditable()){
                    return;
                }

                if (SwingUtilities.isRightMouseButton(e)){
                    String[] types = {"Header", "Message content", "Non-functional"};
                    String type = (String) JOptionPane.showInputDialog(topPanel,
                            "Please choose the type of data to generate:", "Selection dialog",
                            JOptionPane.PLAIN_MESSAGE, null, types, types[0]);

                    if (type != null && type.equals("Header")){
                        String[] protocols = {"http", "coap", "mqtt", "soap"};
                        String protocol = (String) JOptionPane.showInputDialog(topPanel,
                            "Please choose the type of protocol you want to use:", "Selection dialog",
                            JOptionPane.PLAIN_MESSAGE, null, protocols, protocols[0]);
                        if (protocol != null){
                            String header = (String) JOptionPane.showInputDialog(topPanel,
                                    "Please choose the header field you want to use:", "Selection dialog",
                                    JOptionPane.PLAIN_MESSAGE, null, headerFields, headerFields[0]);
                            if (header != null){
                                try {
                                    parameterToTest.getDocument().insertString(parameterToTest.getCaretPosition(), protocol + "." + header, null);
                                }
                                catch (BadLocationException ex) {
                                    JOptionPane.showMessageDialog(topPanel,
                                            "An error occured while inserting your selection.",
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                    else if (type != null && type.equals("Message content")) {
                        String[] paths = {"XML", "JSON"};
                        String path = (String) JOptionPane.showInputDialog(topPanel,
                                "Please choose the content data type you want to use:", "Selection dialog",
                                JOptionPane.PLAIN_MESSAGE, null, paths, paths[0]);

                        if (path != null && path.equals("XML")) {
                            int choice = (int) JOptionPane.showConfirmDialog(editor, "Do you want to load an existing XML file?",
                                    "Load XML file", JOptionPane.YES_NO_CANCEL_OPTION);
                            if (choice == JOptionPane.CANCEL_OPTION) {
                                return;
                            } else if (choice == JOptionPane.NO_OPTION) {
                                new XPathGeneratorEditor().initGUI(true, parameterToTest);
                                return;
                            }

                            final JFileChooser fChooser = new JFileChooser(System.getProperty("user.dir"));
                            FileNameExtensionFilter filter = new FileNameExtensionFilter("XML files (.xml)", "xml");
                            fChooser.setFileFilter(filter);
                            fChooser.setAcceptAllFileFilterUsed(false);

                            final int check = fChooser.showDialog(editor, "Choose xml file");

                            if (check == JFileChooser.APPROVE_OPTION) {
                                BufferedReader br;
                                try {
                                    br = new BufferedReader(new FileReader(fChooser.getSelectedFile()));
                                    StringBuilder sb = new StringBuilder();
                                    String line = br.readLine();
                                    while (line != null) {
                                        sb.append(line);
                                        line = br.readLine();
                                    }
                                    br.close();

                                    new XPathGeneratorEditor().initGUI(sb.toString(), true, parameterToTest);
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(editor, "Something went wrong, while reading your xml file.", "File error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                        else if (path != null && path.equals("JSON")){
                            int choice = (int) JOptionPane.showConfirmDialog(editor, "Do you want to load an existing JSON file?",
                                    "Load JSON file", JOptionPane.YES_NO_CANCEL_OPTION);
                            if (choice == JOptionPane.CANCEL_OPTION) {
                                return;
                            }
                            else if (choice == JOptionPane.NO_OPTION) {
                                new JSONPathGeneratorEditor().initGUI(true, parameterToTest);
                                return;
                            }

                            final JFileChooser fChooser = new JFileChooser(System.getProperty("user.dir"));
                            FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON files (.json)", "json");
                            fChooser.setFileFilter(filter);
                            fChooser.setAcceptAllFileFilterUsed(false);

                            final int check = fChooser.showDialog(editor, "Choose json file");

                            if (check == JFileChooser.APPROVE_OPTION) {
                                BufferedReader br;
                                try {
                                    br = new BufferedReader(new FileReader(fChooser.getSelectedFile()));
                                    StringBuilder sb = new StringBuilder();
                                    String line = br.readLine();
                                    while (line != null) {
                                        sb.append(line);
                                        line = br.readLine();
                                    }
                                    br.close();

                                    new JSONPathGeneratorEditor().initGUI(sb.toString(), true, parameterToTest);
                                }
                                catch (IOException ex) {
                                    JOptionPane.showMessageDialog(editor, "Something went wrong, while reading your json file.", "File error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                    else if (type != null && type.equals("Non-functional")){
                        String[] choices = {"Timeout", "Response-time"};
                        String choice = (String) JOptionPane.showInputDialog(topPanel,
                                "Please choose the type of non-functional requirement you want to use:", "Selection dialog",
                                JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
                        if (choice != null && choice.equals("Timeout")){
                            try {
                                parameterToTest.getDocument().insertString(parameterToTest.getCaretPosition(), "timeout", null);
                            }
                            catch (BadLocationException ex) {
                                JOptionPane.showMessageDialog(topPanel,
                                        "An error occured while inserting your selection.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                                }
                        }
                        else if (choice != null && choice.equals("Response-time")){
                            try {
                                parameterToTest.getDocument().insertString(parameterToTest.getCaretPosition(), "response-time", null);
                            }
                            catch (BadLocationException ex) {
                                JOptionPane.showMessageDialog(topPanel,
                                        "An error occured while inserting your selection.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                                }
                        }
                    }
                }
            }
        });
        listPane.add(parameterToTest);

        JButton descriptionButton = new JButton("Help");
        ButtonCustomizer.customizeButton(descriptionButton);
        descriptionButton.addActionListener((ActionEvent ae) -> {
            JOptionPane.showMessageDialog(listPane, DESCRIPTION_HELPER,
                    "Helper wizard", JOptionPane.INFORMATION_MESSAGE);
        });
        listPane.add(descriptionButton);

        // Row 4
        listPane.add(new JLabel("Required value:", JLabel.RIGHT));
        parameterValue = new JTextField();
        UndoRedoCustomizer.addUndoManager(parameterValue);
        parameterValue.setComponentPopupMenu(new FormPopUpMenu(editor, parameterValue));
        parameterValue.addFocusListener(MessageForm.COLOUR_CHANGER);
        listPane.add(parameterValue);
        JButton valueButton = new JButton("Help");
        ButtonCustomizer.customizeButton(valueButton);
        valueButton.addActionListener((ActionEvent ae) -> {
            JOptionPane.showMessageDialog(listPane, VALUE_HELPER,
                    "Helper wizard", JOptionPane.INFORMATION_MESSAGE);
        });
        listPane.add(valueButton);

        // Row 5
        listPane.add(new JLabel(""));

        final JButton update = new JButton("Add Test");
        ButtonCustomizer.customizeButton(update);
        update.addActionListener((final ActionEvent event) -> {
            if (parameterToTest.getText().equals("") || parameterValue.getText().equals("")){
                JOptionPane.showMessageDialog(editor, "Please fill values for both the guard description and the guard value!",
                        "Invalid guard", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (mirrorNode.hasTimeout()){
                JOptionPane.showMessageDialog(editor, "You cannot have a timeout transition with guards other than the timeout guard.",
                        "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (parameterToTest.getText().equalsIgnoreCase("timeout")){
                if (mirrorNode.getData().size() > 0){
                    JOptionPane.showMessageDialog(editor, "Timeout transitions can only have one guard for the timeout value. "
                            + "Delete your other guards first.", "Timeout transition erorr",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (((Function.FunctionType) comboBox.getSelectedItem()) != Function.FunctionType.Equals){
                    JOptionPane.showMessageDialog(editor, "The only function that can be used for a timeout guard is the 'equals' function.",
                            "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    Long timeout = Long.parseLong(parameterValue.getText());
                    if (timeout <= 0){
                        JOptionPane.showMessageDialog(editor, "The value for a timeout guard must be a positive integer.",
                                "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(editor, "The value for a timeout guard must be an integer representing the time in milliseconds",
                            "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (mirrorNode.hasCounter()) {
                JOptionPane.showMessageDialog(editor, "You cannot have a counter transition with guards other than the index guard.",
                        "Counter transition error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (parameterToTest.getText().equalsIgnoreCase("index")){
                if (mirrorNode.getData().size() > 0){
                    JOptionPane.showMessageDialog(editor, "Counter transitions can only have one guard for the index value. "
                            + "Delete your other guards first.", "Counter transition erorr",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (((Function.FunctionType) comboBox.getSelectedItem()) != Function.FunctionType.Counter){
                    JOptionPane.showMessageDialog(editor, "The only function that can be used for an index guard is the 'counter' function.",
                            "Counter transition error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                /**
                 * Removed check - counter can be evaluated to a prior state value.
                 */
//                try {
//                    Integer counter = Integer.parseInt(address.getText());
//                    if (counter <= 0){
//                        JOptionPane.showMessageDialog(editor, "The value for an index guard must be a positive integer.",
//                                "Counter transition error", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//                }
//                catch (NumberFormatException ex){
//                    JOptionPane.showMessageDialog(editor, "The value for an index guard must be an integer representing the number of iterations.",
//                            "Counter transition error", JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
            }

            if (parameterToTest.getText().equalsIgnoreCase("response-time")){
                try {
                    Long responseTime = Long.parseLong(parameterValue.getText());
                    if (responseTime <= 0){
                        JOptionPane.showMessageDialog(editor, "The value for a response-time guard must be a positive integer.",
                                "Transition error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(editor, "The value for a response-time guard must be an integer representing the time in milliseconds.",
                            "Transition error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (comboBox.getSelectedItem() == Function.FunctionType.Regex){
                try {
                    Pattern.compile(parameterValue.getText());
                }
                catch (PatternSyntaxException ex){
                    JOptionPane.showMessageDialog(editor, "The guard value is not a valid regular expression.",
                            "Regex error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            mirrorNode.addGuard((Function.FunctionType) comboBox.getSelectedItem(), parameterToTest.getText(), parameterValue.getText());
            guardView.clearData();
            guardView.setData(mirrorNode);
            adjustGuardDescription();
            if (comboBox.getSelectedItem() != Function.FunctionType.Counter){
                parameterToTest.setText("");
            }
            parameterValue.setText("");
        });

        listPane.add(update);
        listPane.add(new JLabel(""));


        // Row 6
        listPane.add(new JLabel(""));
        listPane.add(new JLabel(""));
        listPane.add(new JLabel(""));

        // Row 7
        final JLabel tableTitle = new JLabel("Current Tests", JLabel.LEFT);
        tableTitle.setFont(font.deriveFont(attributes));
        listPane.add(tableTitle);
        listPane.add(new JLabel(""));
        listPane.add(new JLabel(""));
        topPanel.add(listPane);

//        topPanel.add(Box.createRigidArea(new Dimension(0, 25)));
//
//        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));


        AttributePanel.setTableConsistentLookAndFeel(guardTable);
        final JScrollPane guardScrollPane = JTable.createScrollPaneForTable(guardTable);

        add(topPanel, BorderLayout.NORTH);
        add(guardScrollPane, BorderLayout.CENTER);

        this.addMouseListener(MessageForm.FOCUS_CHANGER);
        topPanel.addMouseListener(MessageForm.FOCUS_CHANGER);
        listPane.addMouseListener(MessageForm.FOCUS_CHANGER);
        comboBox.addMouseListener(MessageForm.FOCUS_CHANGER);
        guardTable.addMouseListener(MessageForm.FOCUS_CHANGER);
        guardScrollPane.addMouseListener(MessageForm.FOCUS_CHANGER);
    }

    /**
     * Update the data of this form. For a new graph selected element.
     * @param guardData The new graph element to view.
     */
    public final void setData(final Guard guardData) {
        mirrorNode = guardData;
        guardView.setMirrorNode(mirrorNode);
        guardView.setData(guardData);
        adjustGuardDescription();
    }

    /**
     * Clear the content of the form, i.e. replace all the text fields with
     * empty data.
     */
    public final void clearData() {
        guardView.clearData();
        parameterToTest.setText("");
        parameterValue.setText("");
        adjustGuardDescription();
    }

    private void adjustGuardDescription(){
        if (comboBox.getSelectedItem() == Function.FunctionType.Counter) {
            parameterToTest.setText("Index");
            parameterToTest.setEditable(false);
            parameterToTest.setToolTipText("Counter guards have a fixed value for guard description, which is Index.");
        }
        else {
            parameterToTest.setEditable(true);
            parameterToTest.setToolTipText("Click right button for selection dialog.");
        }
    }

}

