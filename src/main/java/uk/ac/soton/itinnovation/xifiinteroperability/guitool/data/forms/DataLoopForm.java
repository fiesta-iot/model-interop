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

public class DataLoopForm extends JPanel {

    private final static String HELPER = "<html><body>"
            + "<font size=+1><b><i>Data Loop</i></b></font><br><br>"
            + "A data loop refers to looping on a particular data event.<br>"
            + "Here you can specify which event to loop on. This must be an "
            + "event captured at a previous state in the model.<br>"

            + "Simply enter the label of the state where the event was captured."
            + "</body></html>";

    /**
     * The text field capturing the node identity field.
     */
    private final transient JTextArea eventInput = new JTextArea();


    /**
     * The form has a one-to-many relationship with an architecture node. The
     * form is a changing view of the selected node. This field stores the
     * current selected node (via setData() method)
     */
    private transient GraphNode mirrorEndNode;

    /**
     * Create a form with the specified labels, tooltips, and sizes.
     */
    public DataLoopForm() {
        /**
         * Create the form properties with a border layout.
         */
        super(new BorderLayout());

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        topPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.add(Box.createHorizontalGlue());
        labelPanel.add(new JLabel("Data event state:"));
        labelPanel.add(Box.createHorizontalGlue());
        topPanel.add(labelPanel);

        topPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel textAreaPanel = new JPanel();
        textAreaPanel.setLayout(new BoxLayout(textAreaPanel, BoxLayout.X_AXIS));
        textAreaPanel.add(Box.createHorizontalGlue());
        textAreaPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        UndoRedoCustomizer.addUndoManager(eventInput);
        eventInput.setLineWrap(true);
        eventInput.setRows(1);
        eventInput.setToolTipText("Atach report data to the end node.");
        textAreaPanel.add(new JScrollPane(eventInput));
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
            mirrorEndNode.addEventStateData(eventInput.getText());
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
                mirrorEndNode.addEventStateData(eventInput.getText());
            }
        };
        eventInput.addFocusListener(focusListener);

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
        this.eventInput.setText(grphNode.getEventLabel());
    }

    /**
     * Reset the content of the node form. Clear all the text fields.
     */
    public final void clearData() {
        this.eventInput.setText("");
    }


}

