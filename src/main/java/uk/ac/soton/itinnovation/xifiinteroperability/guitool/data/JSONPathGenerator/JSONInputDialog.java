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
// Created By : Nikolay Stanchev - ns17@it-innovation.soton.ac.uk
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.ButtonCustomizer;

/**
 * An Input Dialog for JSON data
 *
 * @author ns17
 */
public class JSONInputDialog extends JDialog {
    final private JTextArea jsonInput = new JTextArea();

    public JSONInputDialog(){
        super(); // called for clarity reasons
    }

    /**
     * the initGUI method initialises the GUI components
     * @param editor the editor which launched the input dialog
     */
    public void initGUI(JSONPathGeneratorEditor editor){
        this.setTitle("JSON Input Dialog");
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());

        // this block of code reffers to the validation and insertion of the JSON input into the editor pane
        final JButton insert = new JButton("Validate and insert JSON");
        insert.addActionListener((ActionEvent ae) -> {
            editor.resetEditor(jsonInput.getText());
            dispose();
        });

        ButtonCustomizer.customizeButton(insert);
        buttonsPanel.add(insert);
        buttonsPanel.add(Box.createHorizontalGlue());
        this.add(buttonsPanel, BorderLayout.NORTH);

        final JScrollPane areaScrollPane = new JScrollPane(jsonInput);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(600, 600));
        this.add(areaScrollPane, BorderLayout.CENTER);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
