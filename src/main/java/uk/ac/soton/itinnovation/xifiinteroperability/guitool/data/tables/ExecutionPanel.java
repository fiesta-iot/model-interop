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

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyledEditorKit;
import net.minidev.json.JSONObject;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.RESTComponent;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONEditorKit.JSONEditorKit;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ObjectDeepCloner;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit.XMLEditorKit;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.ContentPopUpMenu;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BehaviourGraphComponent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.COAPEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.Content;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MQTTEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MsgEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.RESTEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.StateNode;

/**
 * A panel to display the behaviour and the system graph when running a test
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class ExecutionPanel extends JPanel {
    
    private static final String HEADERS_TAB_NAME = "Headers information";
    
    private static final String CONTENT_TAB_NAME = "Content information";

    /**
     * a reference to the editor
     */
    private final transient BasicGraphEditor editor;

    /**
     * a reference to the behaviour component
     */
    private transient mxGraphComponent behaviourComponent;

    /**
     * a reference to the system component
     */
    private transient mxGraphComponent systemComponent;

    /**
     * reference to the panel containing the two graphs
     */
    private transient final JPanel graphs;

    /**
     * reference to the table with the interfaces and their respective ports
     */
    private transient final JTable portsTable;

    /**
     * a flag, which represents whether the cells in the behaviour graph are selectable
     */
    private transient boolean isCellSelectable = false;

    /**
     * constructor for the panel, initialises the GUI components
     * @param editor the editor reference
     */
    public ExecutionPanel(BasicGraphEditor editor){
        super(new BorderLayout());

        this.editor = editor;

        graphs = new JPanel(new GridLayout(1, 1));
        graphs.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        behaviourComponent = new BehaviourGraphComponent(editor.getBehaviourGraph().getGraph());
        behaviourComponent.setEnabled(false);
        behaviourComponent.setBorder(BorderFactory.createLineBorder(new Color(128, 0, 0)));
        graphs.add(behaviourComponent);

//        systemComponent = new SystemGraphComponent(editor.getSystemGraph().getGraph());
//        systemComponent.setBorder(BorderFactory.createLineBorder(new Color(0, 66, 128)));
//        systemComponent.setEnabled(false);
//        graphs.add(systemComponent);

        // Creates a split pane to label the graph in the testing panel
        final JLabel gLabel = new JLabel(" Execution View");
        final Font font = gLabel.getFont();
        final Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        gLabel.setFont(boldFont);
        final JSplitPane outer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gLabel, graphs);
        outer.setDividerSize(0);
        add(outer, BorderLayout.CENTER);

        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Interface:", "Proxy running on port:"};
        Object[][] data = {};
        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        portsTable = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }

            @Override
            public String getToolTipText(MouseEvent e){
                String tooltip = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    tooltip = getValueAt(rowIndex, colIndex).toString();
                } catch (Exception ex) {
                    // no tool tip in case of exception
                }

                return tooltip;
            }
        };
        portsTable.setPreferredScrollableViewportSize(new Dimension(portsTable.getPreferredSize().width, portsTable.getRowHeight()*8));
        portsTable.setFillsViewportHeight(true);

        // add a KeyListener to the table, which will copy the original address but with the proxy port
        portsTable.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), "Copy");
        portsTable.getActionMap().put("Copy", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                int rowIndex = portsTable.getSelectedRow();

                try {
                    // replace the port of the interface url with the port url
                    String urlStr = portsTable.getValueAt(rowIndex, 0).toString();
                    String proxyPort = (String) portsTable.getValueAt(rowIndex, 1);
                    URL url = new URL(urlStr);
                    url = new URL(url.getProtocol(), url.getHost(), Integer.parseInt(proxyPort), url.getFile());

                    // copy the url
                    StringSelection stringSel = new StringSelection(url.toString());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSel, null);

                    // display a message for successcul copy of the url
                    JOptionPane.showMessageDialog(graphs, "Successfully copied the url of the interface. "
                            + "Keep in mind that the port number is the port of the proxy for this interface.\n" + url.toString(),
                            "Successful copy", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    // no copy if an exception is thrown
                }
            }
        });
        tablePanel.add(new JScrollPane(portsTable), BorderLayout.CENTER);

        add(tablePanel, BorderLayout.SOUTH);
    }

    /**
     * called when starting a new test, refreshes the graph components and the table of interfaces
     * @param restComponents a map linking component IDs to rest component objects
     */
    public void refreshGraph(Map<String, RESTComponent> restComponents){
        mxGraph behaviourGraph = new mxGraph((mxIGraphModel) ObjectDeepCloner.deepCopy(editor.getBehaviourGraph().getGraph().getModel()),
                editor.getBehaviourGraph().getGraph().getStylesheet()) {
            // not allowing transitions selection, that is edge sellection in the graph
            @Override
            public boolean isCellSelectable(Object cell) {
                if (model.isEdge(cell)) {
                    return false;
                }

                if (editor.isRunning()){
                    return isCellSelectable;
                }

                return super.isCellSelectable(cell);
            }

            // disable cell moving
            @Override
            public boolean isCellMovable(Object cell) {
                return false;
            }
        };
//        mxGraph systemGraph = new mxGraph((mxIGraphModel) ObjectDeepCloner.deepCopy(editor.getSystemGraph().getGraph().getModel()),
//                editor.getSystemGraph().getGraph().getStylesheet());
        behaviourComponent = new BehaviourGraphComponent(behaviourGraph){
            @Override
            public boolean isEnabled(){
                if (editor.isRunning()){
                    return isCellSelectable;
                }

                return super.isEnabled();
            }
        };
//        systemComponent = new SystemGraphComponent(systemGraph);

        graphs.removeAll();

        setListener(behaviourComponent); // set the listener for the behaviour graph, which will load the information of the captured event when clicking on a state
        behaviourComponent.setBorder(BorderFactory.createLineBorder(new Color(128, 0, 0)));
        graphs.add(behaviourComponent);

//        systemComponent.setEnabled(false);
//        systemComponent.setBorder(BorderFactory.createLineBorder(new Color(0, 66, 128)));
//        graphs.add(systemComponent);

        graphs.revalidate();
        graphs.repaint();

        List<List<String>> data = new ArrayList<>();
        restComponents.values().forEach((component) -> {
            component.getInterfaces().forEach((restInterface) -> {
                data.add(new ArrayList<>(Arrays.asList(restInterface.getURL(), Integer.toString(restInterface.getPort()))));
            });
        });

        refreshTable(data);
    }

    /**
     * refresh the table of interfaces and port numbers
     * @param data the new data to insert into the table
     */
    private void refreshTable(List<List<String>> data){
        refreshTable();
        data.forEach((row) -> {
            ((DefaultTableModel) portsTable.getModel()).addRow(row.toArray());
        });
    }

    /**
     * refresh the table without inserting new data into it
     */
    private void refreshTable(){
        DefaultTableModel model = (DefaultTableModel) portsTable.getModel();
        model.setRowCount(0);
    }

    /**
     * resets the Execution panel, by reseting the graph components
     */
    public void resetGraph(){
        behaviourComponent = new BehaviourGraphComponent(editor.getBehaviourGraph().getGraph());
//        systemComponent = new SystemGraphComponent(editor.getSystemGraph().getGraph());

        graphs.removeAll();

        behaviourComponent.setEnabled(false);
        behaviourComponent.setBorder(BorderFactory.createLineBorder(new Color(128, 0, 0)));
        graphs.add(behaviourComponent);

//        systemComponent.setEnabled(false);
//        systemComponent.setBorder(BorderFactory.createLineBorder(new Color(0, 66, 128)));
//        graphs.add(systemComponent);

        graphs.revalidate();
        graphs.repaint();

        // reset the table
        refreshTable();
    }

    /**
     * a method to set the current test state by changing the selection in the graph
     * @param labelID the labelID of the current state
     */
    public void setTestState(String labelID){
        isCellSelectable = true;
        mxGraph graph = behaviourComponent.getGraph();
        String guiID = editor.getDataModel().getNodeByLabel(labelID).getUIIdentifier();
        mxCell toSelect = (mxCell) ((mxGraphModel) graph.getModel()).getCell(guiID);
        graph.getSelectionModel().setCell(toSelect);
        isCellSelectable = false;
    }

    public void setListener(mxGraphComponent graphComponent){
        graphComponent.setConnectable(false);
        
        // references to the components in the captured event dialog
        JTabbedPane capturedEvtPane = new JTabbedPane();
        JTextArea capturedEvtBackupHeadersArea = new JTextArea();
        JEditorPane capturedEvtHeadersArea = new JEditorPane();
        JTextArea capturedEvtBackupContentArea = new JTextArea();
        JEditorPane capturedEvtContentArea = new JEditorPane();
        JLabel capturedEvtStateIDlabel = new JLabel();
        JLabel capturedEvtProtocolLabel = new JLabel();
        JLabel capturedEvtDataTypeLabel = new JLabel();
        JDialog capturedEvtDialog = this.initCapturedEventDialog(capturedEvtPane, capturedEvtContentArea, capturedEvtHeadersArea,
                capturedEvtBackupContentArea, capturedEvtBackupHeadersArea, capturedEvtStateIDlabel, capturedEvtProtocolLabel, capturedEvtDataTypeLabel);

        // references to the components in the triggered event dialog
        JTabbedPane triggeredEvtPane = new JTabbedPane();
        JTextArea triggeredEvtBackupHeadersArea = new JTextArea();
        JEditorPane triggeredEvtHeadersArea = new JEditorPane();
        JTextArea triggeredEvtBackupContentArea = new JTextArea();
        JEditorPane triggeredEvtContentArea = new JEditorPane();
        JTextArea triggeredEvtInfoArea = new JTextArea();
        JDialog triggeredEvtDialog = this.initTriggeredEventDialog(triggeredEvtPane, triggeredEvtContentArea, triggeredEvtHeadersArea,
                triggeredEvtBackupContentArea, triggeredEvtBackupHeadersArea, triggeredEvtInfoArea);
        
        graphComponent.getGraph().getSelectionModel().addListener(mxEvent.CHANGE, (Object sender, mxEventObject e) -> {
            graphComponent.validateGraph();

            // cannot access info while editor is running
            if (editor.isRunning()){
                return;
            }

            // dispone an already opened dialog
            if (capturedEvtDialog.isVisible()){
                capturedEvtDialog.dispose();
            }
            
            // dispone an already opened dialog
            if (triggeredEvtDialog.isVisible()){
                triggeredEvtDialog.dispose();
            }

            // if no cells selected, return without doing anything
            if (((mxGraphSelectionModel) sender).getCells().length == 0){
                return;
            }

            Object chosenCell = ((mxGraphSelectionModel) sender).getCells()[0];
            final String ident = ((mxCell) chosenCell).getId();
            GraphNode state = (GraphNode) editor.getDataModel().getNode(ident);

            StateNode stateNode = (StateNode) editor.getStateMachine().getState(state.getLabel());
            MsgEvent capturedEvent = stateNode.getStoredEvent();
            Map<String, Object> triggerInfo = stateNode.getTriggerInfo();
            
            // if no captured event check for a trigger event
            if (capturedEvent == null){
                if (triggerInfo == null){
                    JOptionPane.showMessageDialog(editor, "No captured event in this state.", "Warning", JOptionPane.WARNING_MESSAGE);
                    graphComponent.getGraph().clearSelection();
                    return;
                }
                
                // build the general info
                final StringBuilder generalInfo = new StringBuilder();
                final String fromLabel = (String) triggerInfo.get("source");
                generalInfo.append("Source node: ").append(fromLabel).append("\n");
                final String targetLabel = (String) triggerInfo.get("target");
                generalInfo.append("Target node: ").append(targetLabel).append("\n");
                final String protocol = (String) triggerInfo.get("protocol");
                generalInfo.append("Protocol: ").append(protocol).append("\n");
                final String targetURL = (String) triggerInfo.get("url");
                generalInfo.append("Sent to: ").append(targetURL).append("\n");
                
                String path = (String) triggerInfo.get("path");
                String pathOrTopic = "Path: ";
                if (path == null){
                    path = (String) triggerInfo.get("topic");
                    pathOrTopic = "Topic: ";
                }
                if (path != null){
                    generalInfo.append(pathOrTopic).append(path).append("\n");
                }
                
                final String method = (String) triggerInfo.get("method");
                generalInfo.append("Method: ").append(method).append("\n");
                final String contentType = triggerInfo.get("dataType") == null ? "No content sent." : (String) triggerInfo.get("dataType");
                generalInfo.append("Content type: ").append(contentType).append("\n");
                triggeredEvtInfoArea.setText(generalInfo.toString());
                
                // build the headers
                final Parameter[] headersArray = (Parameter[]) triggerInfo.get("headers");
                StringBuilder backupHeaders = new StringBuilder();
                String headers = "";
                if (headersArray != null){
                    List<String> headersList = new ArrayList<>();
                    String headerKey;
                    String headerValue;
                    for (Parameter header : headersArray) {
                        headerKey = header.getName();
                        headerValue = header.getValue();
                        headersList.add("\"" + headerKey + "\"" + ": \"" + adjustHeaderValue(headerValue) + "\"");
                        backupHeaders.append(headerKey).append(" : ").append(headerValue).append("\n");
                    }
                    headers = "{" + String.join(",", headersList) + "}";
                }
                
                // build the content
                final String content = (String) triggerInfo.get("content");
                
                // adjust the header and the content areas
                adjustHeadersArea(triggeredEvtPane, triggeredEvtHeadersArea, triggeredEvtBackupHeadersArea, headers, backupHeaders.toString());
                adjustContentArea(triggeredEvtPane, triggeredEvtContentArea, triggeredEvtBackupContentArea, contentType, content);
                
                graphComponent.getGraph().clearSelection();
            
                triggeredEvtDialog.setVisible(true);
                
                return;
            }

            Map<String, Parameter> headersMap = capturedEvent.getParameterMap();
            headersMap.remove("content");
            headersMap.remove("response-time");
            StringBuilder backupHeaders = new StringBuilder();
            List<String> headersList = new ArrayList<>();
            headersMap.keySet().forEach((header) -> {
                headersList.add("\"" + header + "\"" + ": \"" + adjustHeaderValue(headersMap.get(header).getValue()) + "\"");
                backupHeaders.append(header).append(" : ").append(headersMap.get(header).getValue()).append("\n");
            });
            String headers = "{" + String.join(",", headersList) + "}";
            
            Content contentObject = capturedEvent.getDataBody();
            String dataType = contentObject.getType();
            String content = contentObject.getData();
            
            String protocol = "";
            if (capturedEvent instanceof RESTEvent) {
                protocol = "HTTP";
            }
            else if (capturedEvent instanceof COAPEvent) {
                protocol = "COAP";
            }
            else if (capturedEvent instanceof MQTTEvent) {
                protocol = "MQTT";
            }

            adjustHeadersArea(capturedEvtPane, capturedEvtHeadersArea, capturedEvtBackupHeadersArea, headers, backupHeaders.toString());
            adjustContentArea(capturedEvtPane, capturedEvtContentArea, capturedEvtBackupContentArea, dataType, content);
            
            capturedEvtStateIDlabel.setText("State Identifier: " + state.getLabel());
            capturedEvtProtocolLabel.setText("Protocol: " + protocol);
            capturedEvtDataTypeLabel.setText("Media Data Type: " + dataType);
            
            graphComponent.getGraph().clearSelection();
            
            capturedEvtDialog.setVisible(true);
        });
    }
    
    /**
     * inits the JDialog used for displaying information about a captured event
     * @param pane reference to the tabbed pane in the dialog
     * @param contentArea reference to the JEditorPane containing the content
     * @param headersArea reference to the JEditorPane containing the headers
     * @param backupHeadersArea reference to the JTextArea containing the headers if they cannot be parsed as JSON
     * @param backupContentArea reference to the JTextArea containing the content if it cannot be parsed in XML or JSON
     * @param stateIDlabel reference to the JLabel for the state ID
     * @param protocolLabel reference to the JLabel for the protocol
     * @param dataTypeLabel reference to the JLabel for the data type used in the event
     * @return the initialized dialog
     */
    private JDialog initCapturedEventDialog(JTabbedPane pane, JEditorPane contentArea, 
            JEditorPane headersArea, JTextArea backupContentArea, JTextArea backupHeadersArea, JLabel stateIDlabel,
            JLabel protocolLabel, JLabel dataTypeLabel){
        // build the dialog for the events information
        JDialog dialog = new JDialog();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        dialog.add(mainPanel);
        
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.PAGE_AXIS));
        northPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.LINE_AXIS));
        customizeLabel(stateIDlabel);
        customizeLabel(protocolLabel);
        customizeLabel(dataTypeLabel);
        infoPanel.add(Box.createHorizontalGlue());
        infoPanel.add(stateIDlabel);
        infoPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        infoPanel.add(protocolLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        infoPanel.add(dataTypeLabel);
        infoPanel.add(Box.createHorizontalGlue());
        northPanel.add(infoPanel);
        northPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(northPanel, BorderLayout.NORTH);
        
        mainPanel.add(pane, BorderLayout.CENTER);

        // by default use a JEditorPane area to present the headers in json format, if unsuccessful - use a JTextArea and present as plain text
        backupHeadersArea.setEditable(false);
        backupHeadersArea.setLineWrap(true);
        headersArea.setEditable(false);
        JSONEditorKit kit = new JSONEditorKit(false, null, null);
        headersArea.setContentType("text/json");
        headersArea.setEditorKit(kit);
        kit.deinstall(headersArea);
        pane.addTab(HEADERS_TAB_NAME, new JScrollPane(headersArea));
        
        // by default use a JEditorPane area to present content in xml or json format, if unsuccessful - use a JTextArea and present as plain text
        backupContentArea.setEditable(false);
        backupContentArea.setLineWrap(true);
        contentArea.setEditable(false);
        contentArea.setComponentPopupMenu(new ContentPopUpMenu(contentArea));
        pane.addTab(CONTENT_TAB_NAME, new JScrollPane(contentArea));

        dialog.setSize(620, 480);
        dialog.setTitle("Information about captured event");
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        return dialog;
    }
    
    private JDialog initTriggeredEventDialog(JTabbedPane pane, JEditorPane contentArea, 
            JEditorPane headersArea, JTextArea backupContentArea, JTextArea backupHeadersArea, JTextArea infoArea){
        // build the dialog for the events information
        JDialog dialog = new JDialog();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        dialog.add(mainPanel);
        
        mainPanel.add(pane, BorderLayout.CENTER);
        
        // use a normal JTextArea for the general information like protocol, data type, url link, source state, target state
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        pane.addTab("General Information", new JScrollPane(infoArea));
        
         // by default use a JEditorPane area to present the headers in json format, if unsuccessful - use a JTextArea and present as plain text
        backupHeadersArea.setEditable(false);
        backupHeadersArea.setLineWrap(true);
        headersArea.setEditable(false);
        JSONEditorKit kit = new JSONEditorKit(false, null, null);
        headersArea.setContentType("text/json");
        headersArea.setEditorKit(kit);
        kit.deinstall(headersArea);
        pane.addTab(HEADERS_TAB_NAME, new JScrollPane(headersArea));
        
        // by default use a JEditorPane area to present content in xml or json format, if unsuccessful - use a JTextArea and present as plain text
        backupContentArea.setEditable(false);
        backupContentArea.setLineWrap(true);
        contentArea.setEditable(false);
        contentArea.setComponentPopupMenu(new ContentPopUpMenu(contentArea));
        pane.addTab(CONTENT_TAB_NAME, new JScrollPane(contentArea));
        
        dialog.setSize(620, 480);
        dialog.setTitle("Information about triggered event");
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        return dialog;
    }
    
    /**
     * this method customizes a JLabel style
     * @param label the label to customize
     */
    private void customizeLabel(JLabel label){
        label.setFont(new Font("serif", Font.BOLD, label.getFont().getSize() + 3));
        label.setForeground(new Color(55, 85, 135));
    }
    
    /**
     * this method tries to escape all illegal characters of a string and return a valid JSON string
     * @param header the header to format in json
     * @return the escaped string
     */
    private String adjustHeaderValue(String header){
        return JSONObject.escape(header);
    }
    
    /**
     * this method adjusts the editor pane based on the data type used, tries to parse json or xml content
     * @param contentArea the editor pane
     * @param dataType the data type to use (json, xml, etc.)
     * @param content the content to parse
     */
    private void adjustContentArea(JTabbedPane pane, JEditorPane contentArea, JTextArea backupContentArea, String dataType, String content){
        int index = pane.indexOfTab(CONTENT_TAB_NAME);
        
        StyledEditorKit kit;
        if (dataType.toLowerCase().contains("json")){
            kit = new JSONEditorKit(false,null,null);
            contentArea.setContentType("text/json");
            contentArea.setEditorKit(kit);
            kit.deinstall(contentArea);
        }
        else if (dataType.toLowerCase().contains("xml")){
            kit = new XMLEditorKit(null, false);
            contentArea.setContentType("text/xml");
            contentArea.setEditorKit(kit);
        }
        int caret = contentArea.getCaretPosition();
        contentArea.setDocument(contentArea.getEditorKit().createDefaultDocument());
        contentArea.setText(content);
        contentArea.setCaretPosition(caret);
        
        // if editor pane failed to parse content, use the text area
        if (contentArea.getText() == null || contentArea.getText().isEmpty()){
            if(content.length()>100000) {
                backupContentArea.setText(content.substring(0, 100000) + " .... message is too big to display");
            }
            else {
                backupContentArea.setText(content);
            }

            pane.setComponentAt(index, new JScrollPane(backupContentArea));
        }
        else {
            pane.setComponentAt(index, new JScrollPane(contentArea));
        }
    }
    
    /**
     * the method adjusts the hedaer area by putting the new headers in the editor pane
     * @param pane the tabbed pane to adjust
     * @param headersArea the editor pane
     * @param backupHeadersArea the text area used as a backup if the headers cannot be parsed as json data
     * @param headers the new headers formatted for the editor pane as a json data
     * @param backupHeaders the new headers formatted for the text area not the editor pane
     */
    private void adjustHeadersArea(JTabbedPane pane, JEditorPane headersArea, JTextArea backupHeadersArea, String headers, String backupHeaders) {
        int index = pane.indexOfTab(HEADERS_TAB_NAME);
        int caret = headersArea.getCaretPosition();
        headersArea.setDocument(headersArea.getEditorKit().createDefaultDocument());
        headersArea.setText(headers);
        headersArea.setCaretPosition(caret);
        
        // if editor pane failed to parse headers, use the text area
        if (headersArea.getText() == null || headersArea.getText().isEmpty()) {
            backupHeadersArea.setText(backupHeaders);
            pane.setComponentAt(index, new JScrollPane(backupHeadersArea));
        }
        else {
            pane.setComponentAt(index, new JScrollPane(headersArea));
        }
        
    }
}
