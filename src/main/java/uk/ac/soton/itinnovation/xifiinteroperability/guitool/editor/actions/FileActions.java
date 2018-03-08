/*
Copyright (c) 2001-2014, JGraph Ltd
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the JGraph nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL JGRAPH BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.jsoup.Jsoup;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.categories.CategoriesParser;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.categories.TreeGUI;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.categories.TreeWrapper;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.ModelsTableDialog;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.DefaultFileFilter;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidPatternException;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.GraphGenerator;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.PatternValidation;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

/**
 * Modification of mxGraph Action operations to open, close, new and save
 * files.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class FileActions {

    /**
     * Clear the editor information of data and history.
     * @param editor reference to the editor we are clearing
     */
    public static void resetEditor(BasicGraphEditor editor) {
        editor.getCodePanel().getTestingPanel().clearTestingPanel();

        final mxGraph graph = editor.getBehaviourGraph().getGraph();
        final mxCell root = new mxCell();
        root.insert(new mxCell());
        graph.getModel().setRoot(root);

        final mxGraph agraph = editor.getSystemGraph().getGraph();
        final mxCell root2 = new mxCell();
        root2.insert(new mxCell());
        agraph.getModel().setRoot(root2);

        editor.setModified(false);
        editor.resetEditor();
    }

    /**
     * Save the current graph specification to file. Takes the xml specification
     * and writes this to a user selected file location.
     *
     */
    public static class SaveAction extends AbstractAction {

        /**
         * Remember the directory location for saving files to.
         */
        private transient String lastDir = null;

        /**
         * a boolean, which represents if the saveAs button was clicked
         */
        private final transient boolean saveAsClicked;

        /**
         * Editor context - the editor where we are saving files.
         */
        private transient BasicGraphEditor editor;

        /**
         * Saving XML files - constant for the file type.
         */
        private static final String XMLFILE = ".xml";

        /**
         * Create a new concrete action for saving files.
         * @param edtr The editor context information.
         * @param saveAsClicked a boolean to show if Save As button was clicked
         * or just the Save button
         */
        public SaveAction(final BasicGraphEditor edtr, boolean saveAsClicked) {
            super();
            this.editor = edtr;
            this.saveAsClicked = saveAsClicked;
        }

        /**
         * When the action is selected via the UI perform the file save.
         * @param actEvent The UI event e.g. button click.
         */
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            if (editor == null) {
                editor = EditorActions.getEditor(actEvent);
            }

            if (editor != null) {
                // check if the workspace is locked and unlock it if necessary
                final boolean locked = editor.isWorkspaceLocked();
                if (locked){
                    editor.unlockWorkspace();
                }

                FileFilter selectedFilter;
                final DefaultFileFilter xmlPngFilter = new DefaultFileFilter(XMLFILE,
                                "XML " + mxResources.get("file") + " (" + XMLFILE + ")");

                final StringBuffer filename = new StringBuffer();

                if (editor.getCurrentFile() == null || saveAsClicked) {
                    java.io.FileWriter fWriter = null;
                    try {
                        String wDir;
                        if (lastDir != null) {
                            wDir = lastDir;
                        } else if (editor.getCurrentFile() != null) {
                            wDir = editor.getCurrentFile().getParent();
                        } else {
                            wDir = System.getProperty("user.dir");
                        }

                        final JFileChooser fChooser = new JFileChooser(wDir);
                        // Adds the default file format
                        final FileFilter defaultFilter = xmlPngFilter;
                        fChooser.addChoosableFileFilter(defaultFilter);
                        // Adds special vector graphics formats and HTML
                        fChooser.addChoosableFileFilter(new DefaultFileFilter(".xml",
                                "mxGraph Editor " + mxResources.get("file")
                                        + " (.xml)"));
                        // Adds a filter for each supported image format
                        Object[] imageFormats = ImageIO.getReaderFormatNames();
                        // Finds all distinct extensions
                        final HashSet<String> formats = new HashSet();

                        for (Object imageFormat : imageFormats) {
                            final String ext = imageFormat.toString().toLowerCase(Locale.ENGLISH);
                            formats.add(ext);
                        }
                        imageFormats = formats.toArray();
                        for (Object imageFormat : imageFormats) {
                            final String ext = imageFormat.toString();
                            fChooser.addChoosableFileFilter(new DefaultFileFilter("."
                                    + ext, ext.toUpperCase(Locale.ENGLISH) + " "
                                            + mxResources.get("file") + " (." + ext + ")"));
                        }
                        // Adds filter that accepts all supported image formats
                        fChooser.addChoosableFileFilter(new DefaultFileFilter.ImageFileFilter(
                                            mxResources.get("allImages")));
                        fChooser.setFileFilter(defaultFilter);
                        final int rcheck = fChooser.showDialog(null, mxResources.get("save"));

                        if (rcheck != JFileChooser.APPROVE_OPTION) {
                            return;
                        } else {
                            lastDir = fChooser.getSelectedFile().getParent();
                        }
                        filename.append(fChooser.getSelectedFile().getAbsolutePath());
                        selectedFilter = fChooser.getFileFilter();
                        if (selectedFilter instanceof DefaultFileFilter) {
                            final String ext = ((DefaultFileFilter) selectedFilter)
                                    .getExtension();

                            if (!filename.toString().toLowerCase(Locale.ENGLISH).endsWith(ext)) {
                                filename.append(ext);
                            }
                        }
                        if (new File(filename.toString()).exists()
                                && JOptionPane.showConfirmDialog(editor.getBehaviourGraph(),
                                        mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION) {
                            return;
                        }
                        final String xml = editor.getDataModel().getGraphXML();
                        fWriter = new java.io.FileWriter(filename.toString());
                        try {
                            editor.setModified(false);
                            final File chosenFile = new File(filename.toString());
                            final Path openedModel = editor.getCollectionsBrowserPanel().saveModel(chosenFile.getName(), chosenFile.getAbsolutePath(), xml, true);
                            editor.setCurrentFile(openedModel.toFile());

                            fWriter.write(xml);

                            // basically, we need to make sure that model is saved in the local workspace as well in addition to the other location chosen at the
                            // saveAs click operation
                            java.io.FileWriter additionalWriter = new java.io.FileWriter(openedModel.toFile());
                            additionalWriter.write(xml);
                            additionalWriter.close();
                        } finally {
                            fWriter.close();
                        }
                    } catch (IOException ex) {
                        ServiceLogger.LOG.error("Error writing specification to file", ex);
                    } finally {
                        try {
                            if (fWriter != null) {
                                fWriter.close();
                            }
                        } catch (IOException ex) {
                            ServiceLogger.LOG.error("Error closing file stream", ex);
                        }
                    }
                }
                else {
                    filename.append(editor.getCurrentFile().getAbsolutePath());
                    final String xml = editor.getDataModel().getGraphXML();
                    java.io.FileWriter fWrite = null;
                    try {
                        fWrite = new java.io.FileWriter(filename.toString());
                        try {
                            editor.setModified(false);
                            final File chosenFile = new File(filename.toString());
                            editor.setCurrentFile(chosenFile);
                            editor.getCollectionsBrowserPanel().saveModel(chosenFile.getName(), chosenFile.getAbsolutePath(), xml , false);

                            fWrite.write(xml);
                        } finally {
                            fWrite.close();
                        }
                    } catch (IOException ex) {
                        ServiceLogger.LOG.error("Error saving file", ex);
                    } finally {
                        try {
                            if (fWrite != null) {
                                fWrite.close();
                            }
                        } catch (IOException ex) {
                            ServiceLogger.LOG.error("Error saving file", ex);
                        }
                    }
                }

                // if workspace was initially locked, lock it again
                if (locked){
                    editor.lockWorkspace();
                }
            }
        }
    }

    /**
     * Create a new graph. Essentially clear all the data models and views
     * from the UI.
     */
    public static class NewAction extends AbstractAction {
        /**
         * The editor context.
         */
        private transient BasicGraphEditor editor;

        /**
         * the collection to add the new model into
         */
        private transient final String collection;

        /**
         * Create a new action.
         * @param edtr The editor context.
         */
        public NewAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
            this.collection = null;
        }

        /**
         * Creats a new action with a specified collection to add the model into
         * @param editor the editor reference
         * @param collection the collection to add the model into
         */
        public NewAction(final BasicGraphEditor editor, String collection){
            super();
            this.editor = editor;
            this.collection = collection;
        }

        /**
         * Perform the new action on a UI Event.
         * @param actEvent The UI event - e.g. button pressed.
         */
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            if (editor == null) {
                editor = EditorActions.getEditor(actEvent);
            }
            if (editor == null) {
                return;
            }

            if (!editor.isModified() || JOptionPane.showConfirmDialog(editor,
                        mxResources.get("loseChanges")) == JOptionPane.YES_OPTION) {

                final String lastXML = editor.getDataModel().getGraphXML();
                final boolean lastModified = editor.isModified();

                FileActions.resetEditor(editor);

                editor.setModified(false);
                final File openedModel;
                if (collection == null){
                    openedModel = editor.getCollectionsBrowserPanel().openDefaultModel("new.xml", editor.getDataModel().getGraphXML(), lastXML, lastModified).toFile();
                }
                else {
                    openedModel = editor.getCollectionsBrowserPanel().openModelInCollection("new.xml", editor.getDataModel().getGraphXML(), collection, lastXML, lastModified).toFile();
                }
                editor.setCurrentFile(openedModel);

                editor.getSystemGraph().zoomAndCenter();
                editor.getBehaviourGraph().zoomAndCenter();

                /**
                 * Clear the xml specification page.
                 */
                editor.getCodePanel().getXMLPanel().displayXMLSpecification();
                /**
                 * Clear the testing page.
                 */
                editor.getCodePanel().getTestingPanel().clearTestingPanel();
                /**
                 * Clear the previous reports panel
                 */
                editor.getCodePanel().getReportsPanel().clearTabbedPane();
            }
        }
    }

    /**
     * Action to open up a view of specification from a file.
     */

    public static class OpenAction extends AbstractAction {
        /**
         * The last used directory for dialogue.
         */
        private transient String lastDir;

        /**
         * The editor context of this action.
         */
        private transient BasicGraphEditor editor;

        /**
         * the collection to add the opened model into
         */
        private transient final String collection;

        /**
         * Create a new open action.
         * @param edtr The UI context of this operation.
         */
        public OpenAction(final BasicGraphEditor edtr) {
            this (edtr, null);
        }

        public OpenAction (final BasicGraphEditor edtr, final String collection) {
            super();
            this.editor = edtr;
            this.collection = collection;
        }

        /**
         * a static method used to open a model from the ones saved in the collections browser, it requires the editor reference and the saved model XML
         * @param editor the reference to the editor
         * @param xml the model XML
         * @param path the path of the model file
         * @param modified the modified flag of the model
         */
        public static final void openFromCollection (final BasicGraphEditor editor, final String xml, final String path, boolean modified){
            try {
                final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                final Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
                final GraphGenerator gGenerate = new GraphGenerator(editor);

                if (!path.isEmpty()){
                    final File file = new File(path);
                    if (file.exists()){
                        editor.setCurrentFile(file);
                    }
                    else {
                        editor.setCurrentFile(null);
                    }
                }
                else {
                    editor.setCurrentFile(null);
                }

                FileActions.resetEditor(editor);
                gGenerate.createGraph(doc);
                editor.getXmlUndoManager().add(editor.getDataModel().getState());

                mxHierarchicalLayout layout = new mxHierarchicalLayout(editor.getBehaviourGraph().getGraph());
                layout.execute(editor.getBehaviourGraph().getGraph().getDefaultParent());
                layout = new mxHierarchicalLayout(editor.getSystemGraph().getGraph());
                layout.execute(editor.getSystemGraph().getGraph().getDefaultParent());

                editor.setModified(modified);
                editor.getCodePanel().getXMLPanel().displayXMLSpecification();
                editor.setRules();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading file: Invalid Pattern specification", "Pattern error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (ParserConfigurationException ex) {
                JOptionPane.showMessageDialog(editor, "Error Parsing the xml document", "Pattern error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (SAXException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading xml content: Invalid Pattern specification", "Pattern error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (InvalidPatternException ex) {
                JOptionPane.showMessageDialog(editor, "Error in pattern data: Invalid Pattern specification", "Pattern error", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
        * Reads XML file in xml format and update the tool data model and views.
        * @param file The file to open.
        * @throws java.io.IOException Error in the opening or reading of file.
         */
        protected final void openXmlPng(final File file)
                        throws IOException {
            try {
                final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                final Document doc = dBuilder.parse(file);
                final GraphGenerator gGenerate = new GraphGenerator(editor);

                String lastXML = editor.getDataModel().getGraphXML();
                final boolean lastModified = editor.isModified();

                FileActions.resetEditor(editor);
                gGenerate.createGraph(doc);
                editor.getXmlUndoManager().add(editor.getDataModel().getState());

                mxHierarchicalLayout layout = new mxHierarchicalLayout(editor.getBehaviourGraph().getGraph());
                layout.execute(editor.getBehaviourGraph().getGraph().getDefaultParent());
                layout = new mxHierarchicalLayout(editor.getSystemGraph().getGraph());
                layout.execute(editor.getSystemGraph().getGraph().getDefaultParent());

                final File openedModel;
                if (collection == null){
                    openedModel = editor.getCollectionsBrowserPanel().openDefaultModel(file.getName(), editor.getDataModel().getGraphXML(), lastXML, lastModified).toFile();
                }
                else {
                    openedModel = editor.getCollectionsBrowserPanel().openModelInCollection(file.getName(), editor.getDataModel().getGraphXML(), collection, lastXML, lastModified).toFile();
                }
                editor.setCurrentFile(openedModel);
                editor.setModified(false);

                editor.getCodePanel().getXMLPanel().displayXMLSpecification();
                editor.setRules();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading file: Invalid pattern specification or the file cannot be opened.", "Pattern error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (ParserConfigurationException ex) {
                JOptionPane.showMessageDialog(editor, "Error Parsing the xml document", "Pattern error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (SAXException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading xml content: Invalid Pattern specification", "Pattern error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (InvalidPatternException ex) {
                JOptionPane.showMessageDialog(editor, "Error in pattern data: Invalid Pattern specification", "Pattern error", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * Perform open file on UI event action.
         * @param actEvent The UI event.
         */
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            if (editor == null) {
                editor = EditorActions.getEditor(actEvent);
                if (editor == null) {
                    return;
                }
            }

            if (!editor.isModified() || JOptionPane.showConfirmDialog(editor,
                    mxResources.get("loseChanges")) == JOptionPane.YES_OPTION) {

                final String wDir = (lastDir != null) ? lastDir : System
                                .getProperty("user.dir");

                final JFileChooser fChoose = new JFileChooser(wDir+"//src//main//resources//examples");

                // Adds file filter for supported file format
                final DefaultFileFilter defaultFilter = new DefaultFileFilter(
                    ".xml", mxResources.get("allSupportedFormats")
                                    + " (.xml)") {

                    @Override
                    public boolean accept(final File file) {
                            String lcase = file.getName().toLowerCase(Locale.ENGLISH);

                            return super.accept(file)
                                            || lcase.endsWith(".xml");
                    }
                };
                fChoose.addChoosableFileFilter(defaultFilter);

                fChoose.addChoosableFileFilter(new DefaultFileFilter(".xml",
                                "mxGraph Editor " + mxResources.get("file")
                                                + " (.xml)"));

                fChoose.setFileFilter(defaultFilter);

                final int rChck = fChoose.showDialog(null,
                                mxResources.get("openFile"));

                if (rChck == JFileChooser.APPROVE_OPTION) {
                        lastDir = fChoose.getSelectedFile().getParent();

                    try {
                        if (fChoose.getSelectedFile().getAbsolutePath()
                                .toLowerCase().endsWith(".xml")) {
                            openXmlPng(fChoose.getSelectedFile());
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                                editor.getBehaviourGraph(),
                                ex.toString(),
                                mxResources.get("error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * Action to open a template model - API test model or Interoperability test
     * model
     */
    public static class OpenTemplateAction extends AbstractAction {

        /**
         * a reference to the editor
         */
        private BasicGraphEditor editor;

        /**
         * a constructor for the OpenTemlate action
         *
         * @param editor
         */
        public OpenTemplateAction(final BasicGraphEditor editor) {
            this.editor = editor;
        }

        /**
         * generates the data model for a simple API test template
         * @param protocol http or coap
         * @return
         */
        private DataModel generateAPItemplate(String protocol){
            // Create the API template model
            DataModel dataModel = new DataModel();
            dataModel.addNode("1", XMLStateMachine.TRIGGERSTART_LABEL, XMLStateMachine.TRIGGERSTART_LABEL);
            dataModel.addNode("2", XMLStateMachine.NORMAL_LABEL, XMLStateMachine.NORMAL_LABEL);
            dataModel.addNode("3", XMLStateMachine.END_LABEL, XMLStateMachine.END_LABEL);
            ArchitectureNode archNode = (ArchitectureNode) dataModel.addNode("4", XMLStateMachine.INTERFACE_LABEL, XMLStateMachine.INTERFACE_LABEL);
            archNode.addInterfaceData("rest", protocol.toLowerCase() + "://127.0.0.1:8000/", protocol.toLowerCase());
            dataModel.addConnection("5", "1", "2");
            dataModel.addConnection("6", "2", "3");

            return dataModel;
        }

        /**
         * generates the data model for a simple interoperability test template
         */
        private DataModel generateInteropTemplate(String protocol){
            // Create the interoperability template model
            DataModel dataModel = new DataModel();
            dataModel.addNode("1", XMLStateMachine.START_LABEL, XMLStateMachine.START_LABEL);
            dataModel.addNode("2", XMLStateMachine.NORMAL_LABEL, XMLStateMachine.NORMAL_LABEL);
            dataModel.addNode("3", XMLStateMachine.END_LABEL, XMLStateMachine.END_LABEL);
            dataModel.addNode("4", XMLStateMachine.INTERFACE_LABEL, XMLStateMachine.INTERFACE_LABEL);
            dataModel.addNode("7", DataModel.CLIENT, DataModel.CLIENT);
            ArchitectureNode archNode = (ArchitectureNode) dataModel.getComponentByLabel(XMLStateMachine.INTERFACE_LABEL);
            archNode.addInterfaceData("rest", protocol.toLowerCase() + "://127.0.0.1:8000/", protocol.toLowerCase());
            dataModel.addConnection("5", "1", "2");
            dataModel.addConnection("6", "2", "3");

            return dataModel;
        }

        /**
         * a method which loads the string model into the tool
         *
         * @param model the model to open
         */
        private void openModel(String model) {
            try {
                final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                final Document doc = dBuilder.parse(new InputSource(new StringReader(model)));
                final GraphGenerator gGenerate = new GraphGenerator(editor);

                final String lastXML = editor.getDataModel().getGraphXML();
                final boolean lastModified = editor.isModified();

                editor.setCurrentFile(null);
                FileActions.resetEditor(editor);
                gGenerate.createGraph(doc);
                editor.getXmlUndoManager().add(editor.getDataModel().getState());

                mxHierarchicalLayout layout = new mxHierarchicalLayout(editor.getBehaviourGraph().getGraph());
                layout.execute(editor.getBehaviourGraph().getGraph().getDefaultParent());
                layout = new mxHierarchicalLayout(editor.getSystemGraph().getGraph());
                layout.execute(editor.getSystemGraph().getGraph().getDefaultParent());

                editor.getCodePanel().getXMLPanel().displayXMLSpecification();
                final File openedFile = editor.getCollectionsBrowserPanel().openDefaultModel("template.xml", editor.getDataModel().getGraphXML(), lastXML, lastModified).toFile();
                editor.setCurrentFile(openedFile);
                editor.setRules();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading file: Invalid Pattern specification", "Pattern error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (ParserConfigurationException ex) {
                JOptionPane.showMessageDialog(editor, "Error Parsing the xml document", "Pattern error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (SAXException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading xml content: Invalid Pattern specification", "Pattern error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (InvalidPatternException ex) {
                JOptionPane.showMessageDialog(editor, "Error in pattern data: Invalid Pattern specification", "Pattern error", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * The action to be performed
         *
         * @param ae the actual action event
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (editor == null) {
                editor = EditorActions.getEditor(ae);
            }
            if (editor == null) {
                return;
            }

            if (editor.isModified() && JOptionPane.showConfirmDialog(editor,
                    mxResources.get("loseChanges")) != JOptionPane.YES_OPTION) {
                return;
            }

            String[] options = {"HTTP API test template", "COAP API test template", "HTTP Interoperability test template", "COAP Interoperability test template"};
            String choice = (String) JOptionPane.showInputDialog(editor,
                    "Please choose the type of template you want to generate.",
                    "Model from template", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice == null){
                return;
            }

            if (choice.equalsIgnoreCase(options[0])){
                // Create the API template model
                DataModel dataModel = generateAPItemplate("http");

                // generate the model
                openModel(dataModel.getGraphXML());
            }
            else if (choice.equalsIgnoreCase(options[1])){
                // Create the API template model
                DataModel dataModel = generateAPItemplate("coap");

                // generate the model
                openModel(dataModel.getGraphXML());
            }
            else if (choice.equalsIgnoreCase(options[2])){
                // Create the interoperability template model
                DataModel dataModel = generateInteropTemplate("http");

                // generate the model
                openModel(dataModel.getGraphXML());
            }
            else if (choice.equalsIgnoreCase(options[3])){
                // Create the interoperability template model
                DataModel dataModel = generateInteropTemplate("coap");

                // generate the model
                openModel(dataModel.getGraphXML());
            }
        }
    }

    /**
     * Action to open a model from web repositories
     */
    public static class OpenFromWebAction extends AbstractAction {

        /**
         * a reference to the editor
         */
        private BasicGraphEditor editor;

        private boolean certification;

        /**
         * a constructor for the OpenFromWebAction, assumes no certification
         * @param editor the editor reference
         */
        public OpenFromWebAction(final BasicGraphEditor editor){
            this(editor, false);
        }

        /**
         * a constructor for the OpenFromWebAction, which also indicates whether this model is opened for certification
         * @param editor the editor reference
         * @param certification whether this model is opened for certification or not
         */
        public OpenFromWebAction(final BasicGraphEditor editor, boolean certification){
            this.editor = editor;
            this.certification = certification;
        }

        /**
         * a method, which reads the config file of the tool to get the available repositories to download model from
         */
        private Map<String, String> getConfigUrls(){
            Map<String, String> urls = new HashMap<>();

            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(BasicGraphEditor.class.getResourceAsStream("/.config"), StandardCharsets.UTF_8));
                String line = br.readLine();
                while (line != null){
                    if (!(line.startsWith("#") || line.equals(""))){
                        String[] splitString = line.split("->");
                        String url = splitString[0].trim();
                        String icon = "Not provided";
                        if (splitString.length == 2){
                            icon = splitString[1].trim();
                        }

                        if (checkUrlAvailability(url)){
                            urls.put(url, icon);
                        }
                    }
                    line = br.readLine();
                }
                br.close();
            }
            catch (IOException | NullPointerException ex){
                JOptionPane.showMessageDialog(editor, "There is something wrong with the config file of the tool. Either it is corrupted or doesn't exist.",
                        "Config file error",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }

            return urls;
        }

        /**
         * checks if a given url is available by making an http request to it and expecting response code 200
         * @param url the url to check
         * @return true if url is available and false otherwise
         */
        private boolean checkUrlAvailability(String url){
            HttpURLConnection conn = null;
            try {
                conn =  (HttpURLConnection) new URL(url).openConnection();
                conn.connect();
                if (conn.getResponseCode() == 200){
                    conn.disconnect();
                    return true;
                }
                else {
                    conn.disconnect();
                    return false;
                }
            }
            catch (IOException ioe){
                if (conn != null){
                    conn.disconnect();
                }
                return false;
            }
        }

        /**
         * a method which loads the string model into the tool
         * @param model
         */
        private void openModel(String model){
            try {
                final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                final Document doc = dBuilder.parse(new InputSource(new StringReader(model)));
                final GraphGenerator gGenerate = new GraphGenerator(editor);

                final String lastXML = editor.getDataModel().getGraphXML();
                final boolean lastModified = editor.isModified();

                FileActions.resetEditor(editor);
                gGenerate.createGraph(doc);
                editor.getXmlUndoManager().add(editor.getDataModel().getState());

                mxHierarchicalLayout layout = new mxHierarchicalLayout(editor.getBehaviourGraph().getGraph());
                layout.execute(editor.getBehaviourGraph().getGraph().getDefaultParent());
                layout = new mxHierarchicalLayout(editor.getSystemGraph().getGraph());
                layout.execute(editor.getSystemGraph().getGraph().getDefaultParent());

                editor.getCodePanel().getXMLPanel().displayXMLSpecification();
                final File openedFile = editor.getCollectionsBrowserPanel().openDefaultModel("web-model.xml", editor.getDataModel().getGraphXML(), lastXML, lastModified).toFile();
                editor.setCurrentFile(openedFile);
                editor.setRules();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading file: Invalid Pattern specification", "Pattern error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (ParserConfigurationException ex) {
                JOptionPane.showMessageDialog(editor, "Error Parsing the xml document", "Pattern error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (SAXException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading xml content: Invalid Pattern specification", "Pattern error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (InvalidPatternException ex) {
                JOptionPane.showMessageDialog(editor, "Error in pattern data: Invalid Pattern specification", "Pattern error", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * a method which replaces the **Value** strings in the xml model
         */
        private String replaceValues(String model){
            int startIndex = model.indexOf("**");
            int endIndex;
            while (startIndex != -1) {
                endIndex = model.indexOf("**", startIndex + 2);
                String originalValue = model.substring(startIndex+2, endIndex);
                int horizontalBarIndex = originalValue.indexOf("|");
                String newVal;
                if (horizontalBarIndex >= 1){
                    newVal = (String) JOptionPane.showInputDialog(editor, originalValue,
                            originalValue.substring(0, horizontalBarIndex) + " - input for model", JOptionPane.PLAIN_MESSAGE,
                            null, null, originalValue.substring(0, horizontalBarIndex));
                }
                else {
                    newVal = (String) JOptionPane.showInputDialog(editor, originalValue,
                            "Input value for model", JOptionPane.PLAIN_MESSAGE);
                }
                if (newVal == null){
                    return null;
                }
                model = model.replace(model.substring(startIndex + 2, endIndex), newVal);
                model = model.replaceFirst("\\*\\*", "").replaceFirst("\\*\\*", "");
                startIndex = model.indexOf("**");
            }
            return model;
        }

        /**
         * a method which intialises the repository chooser dialog
         * @param urls the urls to choose from
         */
        private void initRepositoryChooser(Map<String, String> urls){
            JDialog chooserDialog = new JDialog();
            chooserDialog.setTitle("Repository chooser");
            chooserDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            chooserDialog.setLayout(new BorderLayout());

            String[] columns = {"Repository Icon", "Repository URL"};
            Object[][] data = new Object[urls.size()][2];
            int i = 0;
            for(String url : urls.keySet()){
                try {
                    data[i][0] = new ImageIcon(BasicGraphEditor.class.getResource(urls.get(url)));
                }
                catch (NullPointerException ex){
                    data[i][0] = "Not provided";
                }
                data[i][1] = url;
                i += 1;
            }

            JTable repositoryTable = new JTable(data, columns) {
                @Override
                public boolean isCellEditable(int row, int column){
                    return false;
                }

                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                      Component component = super.prepareRenderer(renderer, row, column);
                      int rendererWidth = component.getPreferredSize().width;
                      TableColumn tableColumn = getColumnModel().getColumn(column);
                      tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                      return component;
                }

                @Override
                public String getToolTipText(MouseEvent e) {
                    String tip = null;
                    java.awt.Point p = e.getPoint();
                    int row = rowAtPoint(p);
                    int col = columnAtPoint(p);

                    try {
                        tip = (String) getValueAt(row, col);
                    } catch (RuntimeException e1) {}

                    return tip;
                }

                @Override
                public Class getColumnClass(int column){
                    if (column == 0) return ImageIcon.class;
                    return Object.class;
                }
            };

            for (int row = 0; row < repositoryTable.getRowCount(); row++){
                int rowHeight = repositoryTable.getRowHeight();
                Component comp = repositoryTable.prepareRenderer(repositoryTable.getCellRenderer(row, 0), row, 0);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
                repositoryTable.setRowHeight(row, rowHeight);
            }

            JScrollPane scrollPane = new JScrollPane(repositoryTable);
            chooserDialog.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
            buttonsPanel.add(Box.createHorizontalGlue());
            JButton loadButton = new JButton("Load chosen repository");
            loadButton.addActionListener((ActionEvent ae) -> {
                boolean opened;
                try {
                    opened = this.initCategoriesChooser((String) repositoryTable.getValueAt(repositoryTable.getSelectedRow(), 1));
                }
                catch (NullPointerException ex){
                    opened = false;
                }

                if (opened){
                    chooserDialog.dispose();
                }
            });
            buttonsPanel.add(loadButton);
            buttonsPanel.add(Box.createRigidArea(new Dimension(40, 0)));
            JTextField userUrl = new JTextField();
            buttonsPanel.add(userUrl);
            userUrl.setVisible(false);
            JButton manualButton = new JButton("Manually enter repository URL");
            manualButton.addActionListener((ActionEvent ae) -> {
                if (!userUrl.isVisible()){
                    userUrl.setVisible(true);
                    manualButton.setText("Load typed repository");
                }
                else {
                    boolean opened = this.initCategoriesChooser(userUrl.getText());
                    if (opened) {
                        chooserDialog.dispose();
                    }
                }
            });
            buttonsPanel.add(manualButton);
            buttonsPanel.add(Box.createHorizontalGlue());
            chooserDialog.add(buttonsPanel, BorderLayout.SOUTH);

            chooserDialog.pack();
            chooserDialog.setLocationRelativeTo(null);
            chooserDialog.setVisible(true);
        }

        /**
         * a method which initialises a JTable with the available models in the repository
         */
        private void initTable(String[] columns, Object[][] data, Map<String, String> modelsIDs){
            final ModelsTableDialog tableDialog = new ModelsTableDialog();

            // initialising the JTable and overriding the isCellEditable method to not allow editing
            final JTable table = new JTable(new DefaultTableModel(data, columns)) {
                @Override
                public boolean isCellEditable(int row, int column){
                    return false;
                }
            };

            // setting the rendered of the description column to a custom one which wraps long text
            table.getColumnModel().getColumn(1).setCellRenderer(new CustomCellRenderer());
            // setting the table to fill the viewport height
            table.setFillsViewportHeight(true);

            // add a listener to the table to react on double clicks over a name of a model
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        JTable target = (JTable) e.getSource();
                        int row = target.getSelectedRow();
                        int column = target.getSelectedColumn();
                        // check for a non populated row
                        if (row > table.getRowCount()) {
                            return;
                        }

                        // only clicks on the name of the model are allowed
                        if (column != 0) {
                            return;
                        }

                        // extract the model and open it
                        String urlStr = modelsIDs.get((String) table.getValueAt(row, column));
                        StringBuilder response = new StringBuilder();
                        String jsonResponse;
                        try {
                            URL url = new URL(urlStr);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String line;
                            while ((line = br.readLine()) != null) {
                                response.append(line);
                            }
                            br.close();

                            jsonResponse = response.toString();
                            Map<String, Object> jsonMap = new ObjectMapper().readValue(jsonResponse, HashMap.class);
                            String model = (String) jsonMap.get("model");
                            String testName = (String) jsonMap.get("name");
                            if (model == null) {
                                return;
                            }

                            // close the dialog and open the model
                            tableDialog.dispose();
                            model = replaceValues(model);
                            if (model != null) {
                                openModel(model);

                                // if opened from the certification tab set the last url and the name of the test in the certification manager
                                if (certification) {
                                    editor.getCertificationManager().setInfo(urlStr, testName);
                                }
                            }
                        } catch (MalformedURLException ex) {
                            JOptionPane.showMessageDialog(editor,
                                    "There is something wrong with the URL of the repository.",
                                    "Invalid URL", JOptionPane.ERROR_MESSAGE);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(editor,
                                    "There is something wrong with the repository of the model you supplied.",
                                    "Invalid model repository", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            tableDialog.setTable(table, data);
            tableDialog.setVisible(true);
        }

        /**
         * initialises the categories chooser, that is the TreeGUI component
         * @param repositoryUrl the url of the repository
         * @return true if dialog was opened and false otherwise
         */
        private boolean initCategoriesChooser(String repositoryUrl){
            String categoriesUrl = repositoryUrl + "/categories/";
            try {
                String categoriesTree = CategoriesParser.extractJSONcategoriesTree(categoriesUrl);
                TreeWrapper wrapper = CategoriesParser.parseCategoriesTree(categoriesTree);

                TreeGUI.initDialog(new TreeGUI(wrapper.getCategories(), wrapper.getCategoriesModels(), repositoryUrl, this)).setVisible(true);

                return true;
            }
            catch (IOException | JSONException ex) {
                return this.initAvailableModels(repositoryUrl);
            }
        }

        /**
         * this method initialises the model chooser dialog
         * @param repositoryUrl the url of the loaded repository
         * @return true if a repository was opened and false otherwise
         */
        private boolean initAvailableModels(String repositoryUrl){
            return this.initAvailableModels(repositoryUrl, null);
        }

        /**
         * this method initialises the model chooser dialog
         * @param repositoryUrl the url of the loaded repository
         * @param modelsToInclude optional argument which limits the models to include to only those models that are in the list
         * @return true if a repository was opened and false otherwise
         */
        public boolean initAvailableModels(String repositoryUrl, List<String> modelsToInclude){

            if (repositoryUrl == null || repositoryUrl.equals("")){
                return false;
            }

            /**
             * extract the models from the repository and initialise a table with the name and description
             * of the available models
             */
            StringBuilder response = new StringBuilder();
            String jsonResponse;
            try {
                URL url = new URL(repositoryUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                jsonResponse = response.toString();
                Object[] jsonArray = new ObjectMapper().readValue(jsonResponse, Object[].class);
                String[] columns = {"Name", "Description"};
                Object[][] data;
                if (modelsToInclude != null){
                    data = new String[modelsToInclude.size()][2];
                }
                else {
                    data = new String[jsonArray.length][2];
                }
                Map<String, String> modelsIDs = new HashMap<>();
                int index = 0;
                for(Object model : jsonArray){
                    Map<String, Object> modelMap = (HashMap<String, Object>) model;
                    // using Jsoup to remove all html tags and get just the text description
                    String name = Jsoup.parse((String) modelMap.get("name")).text();
                    if (modelsToInclude == null || modelsToInclude.contains(name)){
                        data[index][0] = name;
                        data[index][1] = Jsoup.parse((String) modelMap.get("description")).text();
                        if (repositoryUrl.endsWith("/")){
                            modelsIDs.put(name, repositoryUrl + ((String) modelMap.get("id")));
                        }
                        else {
                            modelsIDs.put(name, repositoryUrl + "/" + ((String) modelMap.get("id")));
                        }
                        index += 1;
                    }
                }

                initTable(columns, data, modelsIDs);
                return true;
            }
            catch (MalformedURLException ex){
                JOptionPane.showMessageDialog(editor,
                        "There is something wrong with the URL of the repository.",
                        "Invalid URL", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            catch (IOException ex){
                JOptionPane.showMessageDialog(editor,
                        "There is something wrong with the repository of the model you supplied.",
                        "Invalid model repository", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        /**
         * The action to be performed
         * @param ae the actual action event
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (editor == null) {
                editor = EditorActions.getEditor(ae);
            }
            if (editor == null) {
                return;
            }

            if (editor.isModified() && JOptionPane.showConfirmDialog(editor,
                    mxResources.get("loseChanges")) != JOptionPane.YES_OPTION) {
                return;
            }

            Map<String, String> urls = this.getConfigUrls();

            if (urls == null){
                JOptionPane.showMessageDialog(editor, "There was an error while reading the repository urls from the configuration file of the tool.",
                        "Config file error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.initRepositoryChooser(urls);
        }

        /**
         * a custom cell rendered for the JTable , which uses a JTextArea set to wrap long text
         */
        private class CustomCellRenderer extends JTextArea implements TableCellRenderer {

            /**
             * the constructor for the CustomCellRendered
             */
            private CustomCellRenderer() {
                // adjusts the wrapping settings of the cell
                setLineWrap(true);
                setWrapStyleWord(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                setText(value.toString());
                setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
                // adjust the height of the table cell to fit the long text
                if (table.getRowHeight(row) != getPreferredSize().height) {
                    table.setRowHeight(row, getPreferredSize().height);
                }
                return this;
            }
        }
    }

     /**
     * Action to import a specification from a file.
     */

    public static class ImportAction extends AbstractAction {
        /**
         * The last used directory for dialogue.
         */
        private transient String lastDir;

        /**
         * The editor context of this action.
         */
        private transient BasicGraphEditor editor;

        /**
         * Create a new import action.
         * @param edtr The UI context of this operation.
         */
        public ImportAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
        * Reads XML file in xml format and update the tool data model and views.
        * @param file The file to open.
        * @throws java.io.IOException Error in the opening or reading of file.
         */
        protected final void importXmlPng(final File file)
                        throws IOException {
            try {
                final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                final Document doc = dBuilder.parse(file);
                final GraphGenerator gGenerate = new GraphGenerator(editor);

                gGenerate.importGraph(doc);

                mxHierarchicalLayout layout = new mxHierarchicalLayout(editor.getBehaviourGraph().getGraph());
                layout.execute(editor.getBehaviourGraph().getGraph().getDefaultParent());
                layout = new mxHierarchicalLayout(editor.getSystemGraph().getGraph());
                layout.execute(editor.getSystemGraph().getGraph().getDefaultParent());
                editor.getCodePanel().getXMLPanel().displayXMLSpecification();
                editor.setRules();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading file: Invalid Pattern specification", "Pattern error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (ParserConfigurationException ex) {
                JOptionPane.showMessageDialog(editor, "Error Parsing the xml document", "Pattern error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (SAXException ex) {
                JOptionPane.showMessageDialog(editor, "Error reading xml content: Invalid Pattern specification", "Pattern error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (InvalidPatternException ex) {
                JOptionPane.showMessageDialog(editor, ex.getMessage(), "Pattern error", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * Perform open file on UI event action.
         * @param actEvent The UI event.
         */
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            if (editor == null) {
                editor = EditorActions.getEditor(actEvent);
                if (editor == null) {
                    return;
                }
            }

//            final mxGraph graph = editor.getBehaviourGraph().getGraph();
//            // Check modified flag and display save dialog
//            editor.getCodePanel().getTestingPanel().clearTestingPanel();
//
//            final mxCell root = new mxCell();
//            root.insert(new mxCell());
//            graph.getModel().setRoot(root);
//
//            final mxGraph agraph = editor.getSystemGraph().getGraph();
//            final mxCell root2 = new mxCell();
//            root2.insert(new mxCell());
//            agraph.getModel().setRoot(root2);

            final String wDir = (lastDir != null) ? lastDir : System
                            .getProperty("user.dir");

            final JFileChooser fChoose = new JFileChooser(wDir);

            // Adds file filter for supported file format
            final DefaultFileFilter defaultFilter = new DefaultFileFilter(
                ".xml", mxResources.get("allSupportedFormats")
                                + " (.xml)") {

                    @Override
                    public boolean accept(final File file) {
                            String lcase = file.getName().toLowerCase(Locale.ENGLISH);

                            return super.accept(file)
                                            || lcase.endsWith(".xml");
                    }
                };
            fChoose.addChoosableFileFilter(defaultFilter);

            fChoose.addChoosableFileFilter(new DefaultFileFilter(".xml",
                            "mxGraph Editor " + mxResources.get("file")
                                            + " (.xml)"));

            fChoose.setFileFilter(defaultFilter);

            final int rChck = fChoose.showDialog(null,
                            mxResources.get("openFile"));

            if (rChck == JFileChooser.APPROVE_OPTION) {
                        lastDir = fChoose.getSelectedFile().getParent();

                        try {
                            if (fChoose.getSelectedFile().getAbsolutePath()
                                            .toLowerCase().endsWith(".xml")) {
                                    importXmlPng(fChoose.getSelectedFile());
                            }
                        } catch (IOException ex) {
                                JOptionPane.showMessageDialog(
                                                editor.getBehaviourGraph(),
                                                ex.toString(),
                                                mxResources.get("error"),
                                                JOptionPane.ERROR_MESSAGE);
                        }
            }
        }
    }

    /**
     * an action which opens all valid test models in a folder and loads them into the tool
     */
    public static class OpenCollectionAction extends AbstractAction {

        /**
         * the editor reference
         */
        private transient BasicGraphEditor editor;

        /**
         * constructor for this action
         * @param editor the editor reference
         */
        public OpenCollectionAction(BasicGraphEditor editor){
            this.editor = editor;
        }

        /**
         * reads the xml content of a file
         * @param toRead the file to read
         * @return the xml content
         * @throws FileNotFoundException
         * @throws InvalidPatternException
         * @throws SAXException
         * @throws IOException
         */
        private static String readXML(File toRead) throws FileNotFoundException, IOException, SAXException, InvalidPatternException {
//            final BufferedReader br = new BufferedReader(new FileReader(toRead));
//            final StringBuilder sb = new StringBuilder();
//            String line;
//            while ((line = br.readLine()) != null){
//                sb.append(line);
//            }
//
//            br.close();

//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder docBuilder;
//            try {
//                docBuilder = dbFactory.newDocumentBuilder();
//
//                Document xmlDom = docBuilder.parse(toRead);
                Path path = Paths.get(toRead.getAbsolutePath());
                byte[] data = Files.readAllBytes(path);
                String xmlAsString = new String(data);

                if (!PatternValidation.validatePattern(xmlAsString.toString())){
                    throw new IOException("XML content is not a valid testing model.");
                }

                return xmlAsString;

        }

        public static void openCollection(BasicGraphEditor editor, File dir, boolean showDialog){
            // get all the files in the give folder with an .xml extension
            final File[] files = dir.listFiles((File dir1, String name) -> name.endsWith(".xml"));

            if (files.length == 0 && showDialog){
                JOptionPane.showMessageDialog(editor, "No models were found in this directory!", "No models found", JOptionPane.WARNING_MESSAGE);
                return;
            }

            final String collection = dir.getName();
            editor.getCollectionsBrowserPanel().addCollection(collection, false);

            // traverse all found xml files by leaving the first one to be opened in the tool
            String model;
            for (File file : files) {
                try {
                    model = readXML(file);
                    editor.getCollectionsBrowserPanel().putModelInCollection(file.getName(), model, collection);
                }
                catch (Exception ex){
                    // if an exception is thrown skip the model,
                    // a model will be skipped if it doesn't comply with the xsd schema of the tool or it is just incorrect XML
                }
            }
        }

        /**
         * the actual action method
         * @param ae the action event object
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (editor == null) {
                editor = EditorActions.getEditor(ae);
            }
            if (editor == null) {
                return;
            }

            final JFileChooser f = new JFileChooser();
            f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            final int check = f.showOpenDialog(editor);

            if (check != JFileChooser.APPROVE_OPTION){
                return;
            }

            // get all the files in the give folder with an .xml extension
            final File dir = f.getSelectedFile();
            if (dir.getParentFile().getPath().equalsIgnoreCase(editor.getWorkspace().toFile().getPath())){
                JOptionPane.showMessageDialog(editor, "You cannot open an existing collection in your workspace.", "Workspace clash", JOptionPane.WARNING_MESSAGE);
                return;
            }

            openCollection(editor, dir, true);
        }
    }
}
