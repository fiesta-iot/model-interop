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
// Created By : Nikolay Stanchev
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;

/**
 * Panel of the UI for displaying previous test reports.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */

public class PreviousReportsPanel extends JPanel {

    /**
     * The tabbed pane to hold all the tabs with the previous reports.
     * Used only if the TestingOutputPanel is used to present previous reports
     */
    private final transient JTabbedPane tabbedPane = new JTabbedPane();

    /**
     * A hash map to store all previous reports, linked to a name of the report.
     * Used only if the TestingOutputPanel is used to present previous reports
     */
    private final transient Map<String, String> previousReports = new HashMap<>();

    /**
     * A getter method for the previous reports map
     * @return a reference to the previousReports map
     */
    public final Map<String, String> getPreviousReports(){
        return previousReports;
    }

    /**
     * An editor used for saving test reports
     */
    public final BasicGraphEditor editor;

    /**
     * A getter method for the editor
     * @return the editor reference
     */
    public BasicGraphEditor getEditor(){
        return editor;
    }

    /**
     * A JLabel used for heading of the panel
     */
    private final transient JLabel title;

    /**
     * Create the panel for previous reports
     * @param editor the editor used for saving test reports
     */
    public PreviousReportsPanel(BasicGraphEditor editor) {
        super(new BorderLayout());
        title = new JLabel();
        final JButton clearButton = new JButton("Clear all reports");
        clearButton.addActionListener((ActionEvent ae) -> {
            final int check = JOptionPane.showConfirmDialog(editor, "Are you sure you want to remove all your previous test reports?", 
                    "Confirm", JOptionPane.OK_CANCEL_OPTION);
            if (check != JOptionPane.OK_OPTION){
                return;
            }
            
            this.clearTabbedPane();
        });
        
        this.editor = editor;

        final JPanel northPanel = new JPanel(); 
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.LINE_AXIS));
        northPanel.add(title);
        northPanel.add(Box.createRigidArea(new Dimension(50, 0)));
        northPanel.add(clearButton);
        
        add(northPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * add a new report tab to the tabbed pane
     * @param report the report that the tab will show
     */
    public synchronized final void addTabReport(String report){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String name = "report - " + sdf.format(cal.getTime());

        previousReports.put(name, report);
        JTextArea consoleOutput = new JTextArea();
        consoleOutput.setBorder(new CompoundBorder(new LineBorder(Color.BLUE),
                new EmptyBorder(1, 3, 1, 1)));
        consoleOutput.setLineWrap(true);
        consoleOutput.setWrapStyleWord(true);
        consoleOutput.setColumns(30);
        consoleOutput.setEditable(false);
        consoleOutput.setText(report);
        JScrollPane areaScrollPane = new JScrollPane(consoleOutput);
        areaScrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(1000, 1000));
        tabbedPane.addTab(name, null, areaScrollPane, "Previous report");
        tabbedPane.setTabComponentAt(tabbedPane.indexOfTab(name), new ButtonTabComponent(tabbedPane, this));
        setPanelTitle();
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
    }

    /**
     * Set the tabbed pane to show the text of the first tab
     */
    public final void resetTabbedPane(){
        if (!previousReports.isEmpty()){
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
        }
        setPanelTitle();
    }

    /**
     * Set the title text
     */
    public synchronized final void setPanelTitle(){
        String html1 = "<html><b>";
        String html2 = "</b></html>";
        String titleLabel;
        if (!previousReports.isEmpty()){
            if (previousReports.size() == 1){
                titleLabel = html1 + "Currently, you have " + previousReports.size() + " previous test report." + html2;
            }
            else {
                titleLabel = html1 + "Currently, you have " + previousReports.size() + " previous test reports." + html2;
            }
        }
        else {
            titleLabel = html1 + "Currently, you don't have any previous test reports." + html2;
        }
        title.setText(titleLabel);
    }

    /**
     * clear the tabbed pane
     */
    public final void clearTabbedPane(){
        previousReports.clear();
        tabbedPane.removeAll();
        setPanelTitle();
    }
}
