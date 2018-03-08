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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XPathGenerator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import static uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit.XMLDocument.ATTRIBUTENAME_ATTRIBUTES;
import static uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit.XMLDocument.ATTRIBUTEVALUE_ATTRIBUTES;
import static uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit.XMLDocument.PLAIN_ATTRIBUTES;
import static uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit.XMLDocument.TAGNAME_ATTRIBUTES;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.ButtonCustomizer;

/**
 * An editor, which loads an xml file and generates XPath on click on elements
 * It also accepts user input for the xml data
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class XPathGeneratorEditor extends JDialog{

    /**
     * the editor pane for the xml data
     */
    private JEditorPane editorPane;

    public XPathGeneratorEditor(){
        super(); // called for clarity reasons
    }

    /**
     * initialise the editor only requiring xml content, might be emppty, e.g. ""
     * @param xml the xml to initialise the editor with
     */
    public void initGUI (String xml){
        this.initGUI(xml, false, null);
    }
    
    /**
     * the initGUI method but without requiring an xml data argument
     *
     * @param insertPath a boolean which represents whether the generated
     * path expression should be inserted in the text field (helper mode)
     * @param textField the text field where the generated path should be inserted if appropriate
     */
    public void initGUI(boolean insertPath, JTextField textField){
        this.initGUI("", insertPath, textField);
    }

    /**
     * the initGUI method which initialises the GUI components
     * @param xml a string which contains the initial xml data to load
     * @param insertPath a boolean which represents whether the generated
     * path expression should be inserted in the text field (helper mode)
     * @param textField the text field where the generated path should be inserted if appropriate
     */
    public void initGUI(String xml, boolean insertPath, JTextField textField) {
        this.setTitle("XPath Generator");
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        editorPane = new JEditorPane("text/xml", xml);
        final JScrollPane areaScrollPane = new JScrollPane(editorPane);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(850, 850));
        this.add(areaScrollPane, BorderLayout.CENTER);
        editorPane.setEditable(false);
        editorPane.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
                new EmptyBorder(1, 3, 1, 1)));
        editorPane.setEditorKit(new GeneratorXMLEditorKit(insertPath, textField, this));
        int caretPosition = editorPane.getCaretPosition();
        editorPane.setText(xml);
        editorPane.setCaretPosition(caretPosition);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.PAGE_AXIS));
        northPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.add(Box.createHorizontalGlue());

        // this block of code reffers to manually inserting XML data
        JButton pasteXML = new JButton("Type or paste xml input");
        pasteXML.addActionListener((ActionEvent ae) -> {
            new XMLInputDialog().initGUI(this);
        });
        ButtonCustomizer.customizeButton(pasteXML);
        buttonsPanel.add(pasteXML);

        buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // this block of code reffers to inserting XML data from file
        JButton open = new JButton("Open a different xml file");
        open.addActionListener((ActionEvent e) -> {
            final JFileChooser fChooser = new JFileChooser(System.getProperty("user.dir"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("XML files (.xml)", "xml");
            fChooser.setFileFilter(filter);
            fChooser.setAcceptAllFileFilterUsed(false);

            final int check = fChooser.showDialog(buttonsPanel, "Choose xml file");

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

                    resetEditor(sb.toString());
                }
                catch (IOException ex){
                    JOptionPane.showMessageDialog(buttonsPanel, "Something went wrong, while reading your xml file.", "File error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        ButtonCustomizer.customizeButton(open);
        buttonsPanel.add(open);

        buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // this block of code reffers to toggling between highlight modes
        JButton highlight = new JButton("Highlight tags");
        ButtonCustomizer.customizeButton(highlight);
        buttonsPanel.add(highlight);
        highlight.addActionListener((ActionEvent e) -> {
            if (highlight.getText().startsWith("Highlight")){
                highlight.setText("Remove highlight from keys and values");
                StyleConstants.setBackground(PLAIN_ATTRIBUTES, new Color(225, 234, 234));
                StyleConstants.setBackground(TAGNAME_ATTRIBUTES, new Color(204, 255, 255));
                StyleConstants.setBackground(ATTRIBUTENAME_ATTRIBUTES, new Color(241, 218, 218));
                StyleConstants.setBackground(ATTRIBUTEVALUE_ATTRIBUTES, new Color(230, 255, 230));
                resetEditor();
            }
            else {
                highlight.setText("Highlight keys and values");
                StyleConstants.setBackground(PLAIN_ATTRIBUTES, Color.WHITE);
                StyleConstants.setBackground(TAGNAME_ATTRIBUTES, Color.WHITE);
                StyleConstants.setBackground(ATTRIBUTENAME_ATTRIBUTES, Color.WHITE);
                StyleConstants.setBackground(ATTRIBUTEVALUE_ATTRIBUTES, Color.WHITE);
                resetEditor();
            }
        });
        buttonsPanel.add(Box.createHorizontalGlue());
        northPanel.add(buttonsPanel);
        northPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
        labelPanel.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("Click on a tag, attribute or text element to generate the XPath");
        label.setFont(new Font("serif", Font.ITALIC + Font.BOLD, label.getFont().getSize() + 2));
        labelPanel.add(label);
        labelPanel.add(Box.createHorizontalGlue());
        northPanel.add(labelPanel);
        northPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        this.add(northPanel, BorderLayout.NORTH);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * a method which resets the editor, this is used when toggling between highlight modes
     */
    private void resetEditor(){
        try {
            int caret = editorPane.getCaretPosition();
            String text = editorPane.getDocument().getText(0, editorPane.getDocument().getLength());
            text = text.replaceAll("&(?!amp;)", "&amp;");
            editorPane.setDocument(editorPane.getEditorKit().createDefaultDocument());
            editorPane.setText(text);
            editorPane.setCaretPosition(caret);
        }
        catch (BadLocationException ex){
            JOptionPane.showMessageDialog(this, "An error occured while reseting the text in the editor", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * a method which resets the editor with new xml data
     * @param xml the new xml data to insert into the editor pane
     */
    void resetEditor(String xml) {
        xml = xml.replaceAll("&(?!amp;)", "&amp;");
        int caret = editorPane.getCaretPosition();
        editorPane.setDocument(editorPane.getEditorKit().createDefaultDocument());
        editorPane.setText(xml);
        editorPane.setCaretPosition(caret);
    }
}
