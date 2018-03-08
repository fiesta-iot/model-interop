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

import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * Table related to the data attached to end states in the graph nodes.
 *
 * The form supports the entry of success field and reason information.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */

public class EndForm extends JPanel {

    private final static String HELPER = "<html><body>"
            + "<font size=+1><b><i>End state report</i></b></font><br><br>"
            + "When creating a pattern, you are allowed to have more than one end states.<br>"
            + "That's why you can also specify whether reaching a spcific end state should be treated as success or not.<br>"
            + "You can also attach a test report to the end state to explain the reason it should be treated "
            + "as a success or not.<br><br>"
            + "This is useful if, for instance, you have a working pattern, but the interoperability test "
            + "fails due to authorization.<br>"
            + "To avoid the failure of the test, you can just create another end node, set its "
            + "success property to false and provide an <br> explanatory test report to clarify that if the end node is reached there is a problem "
            + "with the authorization.<br><br>"
            + "If you do not need this feature. just set the end state success attribute to true with an empty test report.</body></html>";

    /**
     * Viewable data fields matched with the GUI element's data.
     */

    private final transient JComboBox successBox = new JComboBox();
    /**
     * The text field capturing the node identity field.
     */
    private final transient JTextArea reasonInput = new JTextArea();


    /**
     * The form has a one-to-many relationship with an architecture node. The
     * form is a changing view of the selected node. This field stores the
     * current selected node (via setData() method)
     */
    private transient GraphNode mirrorEndNode;

    /**
     * Create a form with the specified labels, tooltips, and sizes.
     */
    public EndForm() {
        /**
         * Create the form properties with a border layout.
         */
        super(new BorderLayout());

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        topPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel successPanel = new JPanel();
        successPanel.setLayout(new BoxLayout(successPanel, BoxLayout.X_AXIS));
        successPanel.add(Box.createHorizontalGlue());
        successPanel.add(new JLabel("Success:  "));
        successBox.addItem(true);
        successBox.addItem(false);
        successPanel.add(successBox);
        successPanel.add(Box.createHorizontalGlue());
        topPanel.add(successPanel);

        topPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(new JLabel("Test Report:"));
        labelPanel.add(Box.createHorizontalGlue());
        topPanel.add(labelPanel);

        topPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel textAreaPanel = new JPanel();
        textAreaPanel.setLayout(new BoxLayout(textAreaPanel, BoxLayout.X_AXIS));
        textAreaPanel.add(Box.createHorizontalGlue());
        textAreaPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        reasonInput.setLineWrap(true);
        reasonInput.setRows(5);
        reasonInput.setToolTipText("Atach report data to the end node.");
        UndoRedoCustomizer.addUndoManager(reasonInput);
        textAreaPanel.add(new JScrollPane(reasonInput));
        textAreaPanel.add(Box.createHorizontalGlue());
        textAreaPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        topPanel.add(textAreaPanel);

        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        final JButton update = new JButton("Update end state");
        ButtonCustomizer.customizeButton(update);
        update.addActionListener((final ActionEvent event) -> {
            mirrorEndNode.addEndStateData((Boolean) successBox.getSelectedItem(), reasonInput.getText());
        });
        buttonPanel.add(update);
        buttonPanel.add(Box.createRigidArea(new Dimension(12, 0)));
        final JButton helper = new JButton("Helper wizard");
        ButtonCustomizer.customizeButton(helper);
        buttonPanel.add(helper);
        helper.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(buttonPanel, HELPER, "Helper wizard",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        buttonPanel.add(Box.createHorizontalGlue());
        topPanel.add(buttonPanel);

        topPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        FocusListener focusListener = new FocusListener(){
            @Override
            public void focusGained(FocusEvent fe) {
                if (fe.getComponent() instanceof JTextArea){
                    fe.getComponent().setBackground(new Color(230,242,255));
                }
            }

            @Override
            public void focusLost(FocusEvent fe) {
                if (fe.getComponent() instanceof JTextArea){
                    fe.getComponent().setBackground(UIManager.getColor("TextField.background"));
                }

                mirrorEndNode.addEndStateData((Boolean) successBox.getSelectedItem(), reasonInput.getText());
            }
        };
        reasonInput.addFocusListener(focusListener);
        successBox.addFocusListener(focusListener);

        add(topPanel, BorderLayout.NORTH);

        this.addMouseListener(MessageForm.FOCUS_CHANGER);
        topPanel.addMouseListener(MessageForm.FOCUS_CHANGER);
    }

    /**
     * Set the data of the form. That is, fill in the fields with the
     * data stored in the object parameter.
     * @param grphNode The data to fill the form in with. This is a node data
     * element from the pattern.
     */
    public final void setData(final GraphNode grphNode) {
        mirrorEndNode = grphNode;
        this.successBox.setSelectedItem(grphNode.getEndStateSuccess());
        this.reasonInput.setText(grphNode.getEndStateReport());
    }

    /**
     * Reset the content of the node form. Clear all the text fields.
     */
    public final void clearData() {
        this.successBox.setSelectedItem(null);
        this.reasonInput.setText("");
    }


}

