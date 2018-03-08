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

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Message;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.InterfaceData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;

/**
 * The message form for inputing data to a trigger out transition. This allows
 * the: Header, message body and URL to be created.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class MessageForm extends JPanel {

    /**
     * a focus listener used to change background color of JTextFields when focused
     */
    public static final FocusListener COLOUR_CHANGER = new FocusListener() {
        @Override
        public void focusGained(FocusEvent fe) {
            fe.getComponent().setBackground(new Color(230, 242, 255));
        }

        @Override
        public void focusLost(FocusEvent fe) {
            fe.getComponent().setBackground(UIManager.getColor("TextField.background"));
        }
    };

    /**
     * a static mouse listener used to request the focus on click
     */
    public static final MouseListener FOCUS_CHANGER = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent me) {
            if (me.getSource() instanceof JComponent) {
                ((JComponent) me.getSource()).requestFocusInWindow();
            }
        }
    };

    /**
     * a static variable for html content with common http headers
     */
    private final static String HEADERS = "<html><body><p><b>Common headers:</b></p><ul>"
            + "<li><i>Accept</i></li><li><i>Accept-Charset</i></li><li><i>Accept-Encoding</i></li>"
            + "<li><i>Accept-Language</i></li><li><i>Accept-Datetime</i></li><li><i>Authorization</i></li>"
            + "<li><i>Cookie</i></li><li><i>Cache-Control</i></li><li><i>Connection</i></li>"
            + "<li><i>Content-Length</i></li><li><i>Content-Type</i></li><li><i>Date</i></li><li><i>Forwarded</i></li>"
            + "<li><i>From</i></li><li><i>Host</i></li><li><i>Max-Forwards</i></li><li><i>Origin</i></li>"
            + "<li><i>Proxy-Authorization</i></li><li><i>Range</i></li><li><i>Referer</i></li><li><i>User-Agent</i></li>"
            + "<li><i>Upgrade</i></li><li><i>Via</i></li><li><i>Warning</i></li>"
            + "</ul>...</body></html>";

    /**
     * The form first contains text fields to input the main content about
     * the message.
     */

    /**
     * Text field to enter the urlEndpoint destination of the message.
     */
    private final transient JComboBox url;

    /**
     * Label which shows the actual url of the pointer
     */
    private JLabel pointerLinkLabel;

    /**
     * dynamic label - resource topic if protocol is mqtt and resource path if not
     */
    private JLabel pathLabel;

     /**
     * Text field to enter the resource path destination of the message.
     */
    private final transient JTextField path;

    /**
     * Text field to enter the http body content.
     */
    private final transient JTextArea body;

    /**
     * Text field to enter the http method contentType.
     */
    private final transient JComboBox method;

    /**
     * the original methods of the MessageForm
     */
    private final transient String[] originalMethods;

    /**
     * mqtt methods for the message form
     */
    private final transient String[] mqttMethods = {"PUBLISH", "SUBSCRIBE", "UNSUBSCRIBE"};

    /**
     * Text field to enter the http body content.
     */
    private final transient JComboBox contentType;

    /**
     * The Message form has a table list of header fields to
     * be added to the message. This is a simple table
     * of header and values.
     */
    private final transient MessageTableModel messageView;

    /**
     * The text field to enter the new header field name.
     */
    private final transient JTextField header = new JTextField();

    /**
     * The text field to enter the new header field value.
     */
    private final transient JTextField headerValue = new JTextField();

    /**
     * the button which adds a header to the message
     */
    private final transient JButton addHeader;

    /**
     * reference to the editor.
     */
    private BasicGraphEditor editor;

    /**
     * Each instance of a form reflects the values in the data model. This
     * is a reference to that data structure.
     */
    private transient Message mirrorNode;

    /**
     * Create a new message form.
     * @param methodCombo The list of message methods to go into the method field
     * @param dataTypes The list of data types that are selectable.
     * @param editor The context of the form.
     */
    public MessageForm(final String[] methodCombo, final String[] dataTypes,
            final BasicGraphEditor editor) {
        super(new BorderLayout());
        messageView = new MessageTableModel();
        this.editor = editor;

        ToolTipManager.sharedInstance().setInitialDelay(75);
        ToolTipManager.sharedInstance().setDismissDelay(ToolTipManager.sharedInstance().getDismissDelay()*2);

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Info Panel
        final JPanel listPane = new JPanel();
        GridLayout gridLayout = new GridLayout(7 , 2);
        gridLayout.setHgap(5);
        gridLayout.setVgap(5);
//        listPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        listPane.setLayout(gridLayout);

        final JLabel title = new JLabel(" Test Message Content", JLabel.LEFT);
        final Font font = title.getFont();
        final Map attributes = font.getAttributes();
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        title.setFont(font.deriveFont(attributes));

        listPane.add(title);
        listPane.add(new JLabel("", JLabel.LEFT));

        listPane.add(new JLabel("Interface target:",  JLabel.RIGHT));
        List<String> restUrls = editor.getDataModel().getRestUrls();
        if (restUrls.size() > 0){
            url = new JComboBox<>(editor.getDataModel().getRestUrls().toArray().clone());
        }
        else {
            url = new JComboBox<>();
            url.addItem("No url pointers available..");
        }
        url.setToolTipText("Select a component interface where this message will be sent to");
        listPane.add(url);

        JLabel pointerLabel = new JLabel("Full URL: ", JLabel.RIGHT);
        pointerLabel.setFont(new Font("serif", Font.ITALIC + Font.BOLD, pointerLabel.getFont().getSize() + 1));
        listPane.add(pointerLabel);
        pointerLinkLabel = new JLabel("N/A");
        pointerLinkLabel.setFont(new Font("serif", Font.ITALIC + Font.BOLD, pointerLinkLabel.getFont().getSize()));
        pointerLinkLabel.setOpaque(true);
        pointerLinkLabel.setBackground(new Color(230, 242, 255));
        pointerLinkLabel.setToolTipText(pointerLinkLabel.getText());
        pointerLinkLabel.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                JOptionPane.showMessageDialog(listPane,
                        "The url pointer is pointing to the following link: " + pointerLinkLabel.getText(),
                        "URL Pointer information", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        listPane.add(pointerLinkLabel);

        pathLabel = new JLabel("Resource path:", JLabel.RIGHT);
        listPane.add(pathLabel);
        path = new JTextField();
        UndoRedoCustomizer.addUndoManager(path);
        path.setToolTipText("Enter the path of the method i.e. complete the full URL for the request");
        path.setComponentPopupMenu(new FormPopUpMenu(editor, path));
        listPane.add(path);

        listPane.add(new JLabel("Method:", JLabel.RIGHT));
        method = new JComboBox(methodCombo.clone());
        this.originalMethods = methodCombo;
        listPane.add(method);

        url.addItemListener((ItemEvent ie) -> {
            if (ie.getStateChange() == ItemEvent.SELECTED){
                String selected = url.getSelectedItem().toString();

                if (!selected.contains("component")){
                    return;
                }

                pointerLinkLabel.setText(getUrlPointerLink());
                pointerLinkLabel.setToolTipText(pointerLinkLabel.getText());

                this.adjustMQTTfields();
            }
        });

        listPane.add(new JLabel("Type:", JLabel.RIGHT));
        contentType = new JComboBox(dataTypes.clone());
        listPane.add(contentType);
        listPane.add(Box.createRigidArea(new Dimension(0, 1)));
        listPane.add(Box.createRigidArea(new Dimension(0, 1)));

        body = new JTextArea(5, 20);
        UndoRedoCustomizer.addUndoManager(body);
        body.setLineWrap(true);
        body.setComponentPopupMenu(new FormPopUpMenu(editor, body));
        final JPanel msgIntfPane = new JPanel();
        gridLayout = new GridLayout(1 , 2);
        msgIntfPane.setLayout(gridLayout);
        msgIntfPane.add(new JLabel("Message body:", JLabel.RIGHT));
        msgIntfPane.add(new JScrollPane(body));

        // Headers add Panel
        final JPanel newIntfPane = new JPanel();
        gridLayout = new GridLayout(7 , 2);
        newIntfPane.setLayout(gridLayout);

        final JButton update = new JButton("Update Message");
        ButtonCustomizer.customizeButton(update);
        update.addActionListener((final ActionEvent event) -> {
            if (url.getSelectedItem().toString().contains("component")){
                mirrorNode.updateMessage(url.getSelectedItem().toString(),
                        path.getText(), method.getSelectedItem().toString(),
                        contentType.getSelectedItem().toString(), body.getText());
            }
            else {
                JOptionPane.showMessageDialog(listPane,
                        "Invalid component url pointer.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        newIntfPane.add(new JLabel(""));
        newIntfPane.add(update);

        final JLabel stitle = new JLabel(" Add Message Header");
        stitle.setFont(font.deriveFont(attributes));
        newIntfPane.add(stitle);
        newIntfPane.add(new JLabel("", JLabel.LEFT));

        newIntfPane.add(new JLabel(" Header ", JLabel.LEFT));
        newIntfPane.add(new JLabel(" Value", JLabel.LEFT));

        UndoRedoCustomizer.addUndoManager(header);
        header.setToolTipText(null);
        newIntfPane.add(header);

        UndoRedoCustomizer.addUndoManager(headerValue);
        headerValue.setComponentPopupMenu(new FormPopUpMenu(editor, headerValue));
        newIntfPane.add(headerValue);

        final JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.LINE_AXIS));
        checkBoxPanel.add(new JLabel("  Show suggestions on hover:  "));
        JCheckBox checkBox = new JCheckBox();
        checkBox.addActionListener((ActionEvent ae) -> {
            AbstractButton check = (AbstractButton) ae.getSource();
            if (check.getModel().isSelected()){
                header.setToolTipText(HEADERS);
            }
            else {
                header.setToolTipText(null);
            }
        });
        checkBoxPanel.add(checkBox);
        newIntfPane.add(checkBoxPanel);

        addHeader = new JButton("Add Header");
        ButtonCustomizer.customizeButton(addHeader);
        addHeader.addActionListener((final ActionEvent event) -> {
            mirrorNode.addHeader(header.getText(), headerValue.getText());
            messageView.clearData();
            messageView.setData(mirrorNode);
            header.setText("");
            headerValue.setText("");
        });
        newIntfPane.add(addHeader);


        final JLabel tableTitle = new JLabel(" Message Headers", JLabel.LEFT);
        final Font font2 = tableTitle.getFont();
        final Map attributes2 = font.getAttributes();
        attributes2.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        tableTitle.setFont(font2.deriveFont(attributes2));


        newIntfPane.add(Box.createRigidArea(new Dimension(0, 5)));
        newIntfPane.add(Box.createRigidArea(new Dimension(0, 5)));
        newIntfPane.add(tableTitle);
        newIntfPane.add(new JLabel("", JLabel.LEFT) );
//                topPanel.add(newIntfPane);

        final JTable nodetable = new JTable(messageView);
        nodetable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = nodetable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < nodetable.getRowCount()) {
                    nodetable.setRowSelectionInterval(r, r);
                } else {
                    nodetable.clearSelection();
                }

                int rowindex = nodetable.getSelectedRow();
                if (rowindex < 0)
                    return;

                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                    JPopupMenu popup = new ChangeTable(editor, messageView, r, mirrorNode);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        final JScrollPane messageScrollPane = JTable.createScrollPaneForTable(nodetable);

        add(messageScrollPane, BorderLayout.CENTER);

//        topPanel.add(messageScrollPane);
        JScrollPane pane = new JScrollPane(listPane);
        topPanel.add(listPane);
        topPanel.add(msgIntfPane);
        topPanel.add(newIntfPane);
        add(topPanel, BorderLayout.NORTH);
//        add(messageScrollPane, BorderLayout.SOUTH);

        final FocusListener focusListener = new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
                if (fe.getComponent() instanceof JTextField || fe.getComponent() instanceof JTextArea){
                    fe.getComponent().setBackground(new Color(230,242,255));
                }
            }

            @Override
            public void focusLost(FocusEvent fe) {
                if (fe.getComponent() instanceof JTextField || fe.getComponent() instanceof JTextArea){
                    fe.getComponent().setBackground(UIManager.getColor("TextField.background"));
                }

                if (url.getSelectedItem().toString().contains("component")){
                    mirrorNode.updateMessage(url.getSelectedItem().toString(),
                            path.getText(), method.getSelectedItem().toString(),
                            contentType.getSelectedItem().toString(), body.getText());
                }
            }

        };

        url.addFocusListener(focusListener);
        path.addFocusListener(focusListener);
        method.addFocusListener(focusListener);
        contentType.addFocusListener(focusListener);
        body.addFocusListener(focusListener);


        JPanel panel = this; // reference to the panel
        KeyListener keyListener = new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER){
                    panel.requestFocusInWindow();
                }
            }
        };
        url.addKeyListener(keyListener);
        path.addKeyListener(keyListener);

        this.addMouseListener(FOCUS_CHANGER);
        topPanel.addMouseListener(FOCUS_CHANGER);
        nodetable.addMouseListener(FOCUS_CHANGER);
        messageScrollPane.addMouseListener(FOCUS_CHANGER);

        header.addFocusListener(COLOUR_CHANGER);
        headerValue.addFocusListener(COLOUR_CHANGER);
    }

    /**
     * Update the form text fields with the data from a network message. This
     * is a http message at present.
     * @param msg The HTTP message data to use to update text boxes.
     */
    public final void setData(final Message msg) {
        mirrorNode = msg;
        if (msg.getEndpoint() == null){
            url.setSelectedIndex(0);
        }
        else {
            url.setSelectedItem(msg.getEndpoint().toLowerCase(Locale.ENGLISH));
        }

        this.adjustMQTTfields();

        path.setText(msg.getPath());

        if (msg.getHTTPMethod() == null) {
            method.setSelectedItem(0);
        } else {
            method.setSelectedItem(msg.getHTTPMethod().toUpperCase(Locale.ENGLISH));
        }
        if (msg.getDataType() == null) {
            method.setSelectedItem(0);
        } else {
            contentType.setSelectedItem(msg.getDataType().toUpperCase(Locale.ENGLISH));
        }
        body.setText(msg.getHTTPBody());
        messageView.setData(msg);
    }

    /**
     * Empty all the fields in the GUI form, so that is becomes and empty form.
     */
    public final void clearData() {
        messageView.clearData();
        List<String> restUrls = editor.getDataModel().getRestUrls();
        if (restUrls.size() > 0){
            url.removeAllItems();
            restUrls.forEach((restUrl) -> {
                url.addItem(restUrl);
            });
            url.setSelectedIndex(0);
            this.adjustMQTTfields();
        }
        else {
            url.removeAllItems();
            url.addItem("No url pointers available..");
            url.setSelectedIndex(0);
            pathLabel.setText("Resource Path");
            method.removeAllItems();
            for (String item : this.originalMethods) {
                method.addItem(item);
            }
            addHeader.setEnabled(true);
            addHeader.setToolTipText(null);
        }

        pointerLinkLabel.setText(getUrlPointerLink());
        pointerLinkLabel.setToolTipText(pointerLinkLabel.getText());
        path.setText("");
        method.setSelectedIndex(0);
        contentType.setSelectedIndex(0);
        body.setText("");
    }

    /**
     * adjusts the dynamic gui fields when using mqtt interface
     */
    private void adjustMQTTfields(){
        if (url.getSelectedItem() != null) {
            String[] vals = url.getSelectedItem().toString().split("\\.");
            if (vals.length == 3) {
                String componentLabel = vals[1];
                ArchitectureNode component = (ArchitectureNode) editor.getDataModel().getComponentByLabel(componentLabel);
                mxGraphModel model = (mxGraphModel) editor.getSystemGraph().getGraph().getModel();
                mxCell cellChanged = (mxCell) model.getCell(component.getNodeLabelID());
                String protocol = cellChanged.getStyle();

                if (protocol.contains("mqtt")) {
                    pathLabel.setText("Resource Topic");
                    method.removeAllItems();
                    for (String item: this.mqttMethods){
                        method.addItem(item);
                    }
                    addHeader.setEnabled(false);
                    addHeader.setToolTipText("The MQTT protocol doesn't support header fields. All headers will be ignored.");
                    return;
                }
            }
        }

        pathLabel.setText("Resource Path");
        method.removeAllItems();
        for (String item : this.originalMethods) {
            method.addItem(item);
        }
        addHeader.setEnabled(true);
        addHeader.setToolTipText(null);
    }

    private String getUrlPointerLink(){
        String link = "N/A";
        if (url.getSelectedItem().toString().contains("component")){
            String[] idParts = url.getSelectedItem().toString().split("\\.");
            ArchitectureNode archNode = (ArchitectureNode) editor.getDataModel().getComponentByLabel(idParts[1]);
            for (InterfaceData data: archNode.getData()){
                if (data.getRestID().equals(idParts[2])){
                    link = data.getRestURL();
                    break;
                }
            }
        }

        return link;
    }
}
