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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator.JSONPathGeneratorEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XPathGenerator.XPathGeneratorEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;

/**
 * This class holds all the actions related to the XPath and JSONPath generator tools
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class ToolsActions {

    /**
     * Utility class, therefore use a private constructor.
     */
    private ToolsActions() {
        // empty implementation
    }

    /**
     * the action to open the XPath generator tool
     */
    public static class XPathAction extends AbstractAction {

         /**
         * reference to the editor
         */
        private final BasicGraphEditor editor;

        /**
         * constructor for this action, sets the editor reference
         *
         * @param editor the editor reference
         */
        public XPathAction(BasicGraphEditor editor){
            this.editor = editor;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int choice = (int) JOptionPane.showConfirmDialog(editor, "Do you want to load an existing XML file?",
                    "Load XML file", JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.CANCEL_OPTION){
                return;
            }
            else if (choice == JOptionPane.NO_OPTION){
                new XPathGeneratorEditor().initGUI("");
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
                    while (line != null){
                        sb.append(line);
                        line = br.readLine();
                    }
                    br.close();

                    new XPathGeneratorEditor().initGUI(sb.toString());
                }
                catch (IOException ex){
                    JOptionPane.showMessageDialog(editor, "Something went wrong, while reading your xml file.", "File error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }

    /**
     * the action to open the JSONPAth generator tool
     */
    public static class JSONPathAction extends AbstractAction {

        /**
         * reference to the editor
         */
        private final BasicGraphEditor editor;

        /**
         * constructor for this action, sets the editor reference
         *
         * @param editor the editor reference
         */
        public JSONPathAction(BasicGraphEditor editor){
            this.editor = editor;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int choice = (int) JOptionPane.showConfirmDialog(editor, "Do you want to load an existing JSON file?",
                    "Load JSON file", JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.CANCEL_OPTION) {
                return;
            }
            else if (choice == JOptionPane.NO_OPTION) {
                new JSONPathGeneratorEditor().initGUI("");
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

                    new JSONPathGeneratorEditor().initGUI(sb.toString());
                }
                catch (IOException ex){
                    JOptionPane.showMessageDialog(editor, "Something went wrong, while reading your json file.", "File error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }

}
