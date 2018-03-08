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

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.InterfaceData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;

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

public class ComponentForm extends JPanel {

    /**
     * the regular expression for validating the url of an interface,
     * the url should contain a port number and specify the protocol (http(s) or coap)
     */
    public final transient static String REGEX = "^(http|https|coap|tcp):\\/\\/[^:]+:[0-9]{1,5}(\\/.*)?$";

    /**
     * the regular expression for catching the port number only from the interface url
     */
    public final transient static String PORT_REGEX = ":[0-9]{1,5}";

    /**
     * The panel displaying the entered URLs of this component.
     */
    private final transient ComponentTableModel componentView;

    /**
     * Viewable data fields matched with the GUI element's data.
     */

    /**
     * Text field to enter the identifier of the component (system node).
     */
    private final transient JTextField ident;

    /**
     * Text field to enter the ip address of the component (system node).
     */
    private final transient JTextField address;

    /**
     * Text field to enter id of URL.
     */
    private final transient JTextField urlID = new JTextField();

    /**
     * Text field to enter the full URL.
     */
    private final transient JTextField url = new JTextField();

    /**
     * The form has a one-to-many relationship with an architecture node. The
     * form is a changing view of the selected node. This field stores the
     * current selected node (via setData() method).
     */
    private transient ArchitectureNode mirrorNode;

    private String getProtocol(BasicGraphEditor editor) {
        mxGraphModel model = (mxGraphModel) editor.getSystemGraph().getGraph().getModel();
        mxCell cellChanged = (mxCell) model.getCell(mirrorNode.getNodeLabelID());
        String componentType = cellChanged.getStyle();

        if(componentType.contains("coap")) {
            return "coap";
        }
        if(componentType.contains("mqtt")) {
            return "mqtt";
        }
        if(componentType.contains("soap")) {
            return "soap";
        }
        return "http";
    }

    /**
     * Create a form with the specified labels, tooltips, and sizes.
     * @param editor
     */
    public ComponentForm(BasicGraphEditor editor) {
        super(new BorderLayout());
        componentView = new ComponentTableModel();

        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        // Info Panel
        final JPanel listPane = new JPanel();
        final GridLayout gridLayout = new GridLayout(5 , 2);
        gridLayout.setHgap(5);
        gridLayout.setVgap(5);
        listPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        listPane.setLayout(gridLayout);

        final JLabel title = new JLabel(" Component Information", JLabel.LEFT);
        final Font font = title.getFont();
        final Map attributes = font.getAttributes();
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        title.setFont(font.deriveFont(attributes));

        listPane.add(title);
        listPane.add(new JLabel("", SwingConstants.LEFT));

        listPane.add(new JLabel("Name:",  JLabel.LEFT));
        listPane.add(new JLabel("Host Address:", JLabel.LEFT));
        ident = new JTextField();
        ident.setToolTipText("ID of the component");
        UndoRedoCustomizer.addUndoManager(ident);
        listPane.add(ident);

        
        address = new JTextField();
        address.setToolTipText("Enter the IP address or domain name of the component");
        UndoRedoCustomizer.addUndoManager(address);
        listPane.add(address);
        listPane.add(new JLabel(""));

        final JButton update = new JButton("Update");
        ButtonCustomizer.customizeButton(update);
        update.addActionListener((final ActionEvent event) -> {
            if (mirrorNode.getLabel().equalsIgnoreCase(ident.getText()) || !editor.getDataModel().archIdentExist(ident.getText())){
                mxGraphModel model = (mxGraphModel) editor.getSystemGraph().getGraph().getModel();
                mxCell cellChanged = (mxCell) model.getCell(mirrorNode.getNodeLabelID());
                cellChanged.setValue(ident.getText());
                editor.getSystemGraph().refresh();
                mirrorNode.setData(ident.getText(), address.getText());
            }
            else {
                JOptionPane.showMessageDialog(editor,
                        "Component id '" + ident.getText() + "' is already used. Please choose another label."
                        , "Renaming error", JOptionPane.ERROR_MESSAGE);
                ident.setText(mirrorNode.getLabel());
            }
        });

        final FocusListener focusListener = new FocusListener(){
            @Override
            public void focusGained(FocusEvent fe) {
                fe.getComponent().setBackground(new Color(230, 242, 255));
            }

            @Override
            public void focusLost(FocusEvent fe) {
                fe.getComponent().setBackground(UIManager.getColor("TextField.background"));
                update.doClick();
            }

        };
        ident.addFocusListener(focusListener);
        address.addFocusListener(focusListener);

        JPanel panel = this;
        KeyListener keyListener = new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ENTER){
                    panel.requestFocusInWindow();
                }
            }
        };
        ident.addKeyListener(keyListener);
        address.addKeyListener(keyListener);

        listPane.add(update);
        topPanel.add(listPane);

        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Interface add Panel
        final JPanel newIntfPane = new JPanel();
        newIntfPane.setLayout(gridLayout);
        newIntfPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        final JLabel stitle = new JLabel(" Add Interface");
        stitle.setFont(font.deriveFont(attributes));
        newIntfPane.add(stitle);
        newIntfPane.add(new JLabel("", JLabel.LEFT));

        newIntfPane.add(new JLabel("Name:", JLabel.LEFT));
        newIntfPane.add(new JLabel("URL:", JLabel.LEFT));

        UndoRedoCustomizer.addUndoManager(urlID);
        newIntfPane.add(urlID);
        urlID.addFocusListener(MessageForm.COLOUR_CHANGER);
        UndoRedoCustomizer.addUndoManager(url);
        newIntfPane.add(url);
        url.addFocusListener(MessageForm.COLOUR_CHANGER);

        final JButton addIntf = new JButton("Add");
        ButtonCustomizer.customizeButton(addIntf);
        addIntf.addActionListener((final ActionEvent event) -> {
            boolean hasPortNumber = Pattern.matches(ComponentForm.REGEX, url.getText());
            if (!hasPortNumber){
                JOptionPane.showMessageDialog(newIntfPane,
                        "The url of the interface is not valid. Keep in mind that a port number must be specified. For instance - 'http://127.0.0.1:8080/'",
                        "Invalid URL", JOptionPane.WARNING_MESSAGE);
                return;
            }
            else {
                Pattern p = Pattern.compile(ComponentForm.PORT_REGEX);
                Matcher m = p.matcher(url.getText());
                m.find();
                String portStr = url.getText().substring(m.start()+1, m.end());
                try {
                    Integer port = Integer.parseInt(portStr);
                    if (port > 65536){
                        JOptionPane.showMessageDialog(newIntfPane,
                                "The port number in the url must be between 0 and 65536.",
                                "Port number exceeding limit", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(newIntfPane,
                            "The specified port number in the url is invalid!",
                            "Invalid port number", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            for(InterfaceData data: mirrorNode.getData()){
                if (data.getRestID().equalsIgnoreCase(urlID.getText())){
                    JOptionPane.showMessageDialog(newIntfPane,
                            "An interface with this id already exists.",
                            "Interface error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            mirrorNode.addInterfaceData(urlID.getText(), url.getText(), getProtocol(editor));
            componentView.clearData();
            componentView.setData(mirrorNode);
            url.setText("");
            urlID.setText("");
        });
        newIntfPane.add(new JLabel(""));
        newIntfPane.add(addIntf);

        topPanel.add(newIntfPane);

        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        final JLabel tableTitle = new JLabel("Component Interfaces", JLabel.LEFT);
        tableTitle.setFont(font.deriveFont(attributes));
        topPanel.add(tableTitle);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        final JTable nodetable = new JTable(componentView);
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
                    JPopupMenu popup = new ChangeTable(editor, componentView, r, mirrorNode);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        final JScrollPane compScrollPane = JTable.createScrollPaneForTable(nodetable);

        add(topPanel, BorderLayout.NORTH);
        add(compScrollPane, BorderLayout.CENTER);

        this.addMouseListener(MessageForm.FOCUS_CHANGER);
        topPanel.addMouseListener(MessageForm.FOCUS_CHANGER);
        listPane.addMouseListener(MessageForm.FOCUS_CHANGER);
        newIntfPane.addMouseListener(MessageForm.FOCUS_CHANGER);
        nodetable.addMouseListener(MessageForm.FOCUS_CHANGER);
        compScrollPane.addMouseListener(MessageForm.FOCUS_CHANGER);
    }

    /**
     * Set the data input to the fields in the component form using the
     * information from the architecture node in the data model.
     * @param archNodeInput The architecture node data.
     */
    public final void setData(final ArchitectureNode archNodeInput) {
        mirrorNode = archNodeInput;
        ident.setText(archNodeInput.getLabel());
        address.setText(archNodeInput.getAddress());
        componentView.setData(archNodeInput);
    }

    /**
     * Reset the fields in the component form.
     */
    public final void clearData() {
        componentView.clearData();
        ident.setText("");
        address.setText("");
        urlID.setText("");
        url.setText("");
    }
}

