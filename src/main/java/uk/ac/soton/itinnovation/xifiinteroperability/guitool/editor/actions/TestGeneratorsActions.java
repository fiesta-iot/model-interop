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

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.GraphGenerator;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.testgenerators.InvalidSpecificationException;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.testgenerators.RAMLTestGenerator;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.testgenerators.SwaggerTestGenerator;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidPatternException;

/**
 * The actions related to the test generators
 * 
 * @author ns17
 */
public class TestGeneratorsActions {
      /**
     * An action, which loads a Swagger based API specification and converts it to a testing model
     */
    public static class OpenSwaggerAPIaction extends AbstractAction {

        /**
         * The returned value may be adjusted if there is an opened model with this name.
         * 
         * @return the name to use when opening the model in the collections explorer
         */
        protected String modelName() {
            return "swagger-api-test.xml";
        }
        
        /**
         * The returned value may be adjusted if there exists a collection with this name.
         * 
         * @return the name to use for a collection when opening the generated models, in execution mode 
         */
        protected String collectionName() {
            return "swagger-api-test-collection";
        }
        
        /**
         * The returned value may be adjusted if there is an opened model with this name.
         * 
         * @return the name to use when opening the error handling model in the collections explorer
         */
        protected String errorModelName() {
            return "swagger-api-error-test.xml";
        }
        
        /**
         * The returned value may be adjusted if there is an opened model with this name.
         * 
         * @return the name to use when opening the pattern handling model in the collections explorer
         */
        protected String patternModelName() {
            return "swagger-api-pattern-test.xml";
        }
        
        /**
         * @return the filters allowed for a file representing a swagger API spec 
         */
        protected List<FileNameExtensionFilter> getApiFileFilters(){
            final List<FileNameExtensionFilter> filters = new ArrayList<>();
            filters.add(new FileNameExtensionFilter("*.yaml", "yaml"));
            filters.add(new FileNameExtensionFilter("*.yml", "yml"));
            filters.add(new FileNameExtensionFilter("*.json", "json"));
            return filters;
        }
        
        /**
         * reference to the graph editor
         */
        protected transient BasicGraphEditor editor;
                
        /**
         * constructor for this action
         * @param editor the editor reference
         */
        public OpenSwaggerAPIaction(BasicGraphEditor editor){
            this.editor = editor;
        }
        
        /**
         * opens an API specification file
         * @return the opened file object
         */
        protected File openSpecFile(){
            final JFileChooser f = new JFileChooser();
            f.setAcceptAllFileFilterUsed(false);
            getApiFileFilters().forEach((filter) -> {
                f.addChoosableFileFilter(filter);
            });
            final int check = f.showOpenDialog(editor);
            
            if (check != JFileChooser.APPROVE_OPTION){
                return null;
            }
            
            return f.getSelectedFile();
        }
        
        /**
         * generates the model using the graph generator and the test generator object passed as a parameter
         * @param model the test generated model
         * @return the collection this model is created into
         */
        protected String generateModel(final String model){
            
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
                String collection = editor.getCollectionsBrowserPanel().generateCollection(collectionName());
                final File openedFile = editor.getCollectionsBrowserPanel().openModelInCollection(modelName(), editor.getDataModel().getGraphXML(), collection, lastXML, lastModified).toFile();
                editor.setCurrentFile(openedFile);
                editor.setModified(false);
                editor.setRules();
                return collection;
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
            
            return null;
        }
        
        /**
         * the method performed on this action
         * @param ae the UI action event object
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (editor == null) {
                editor = EditorActions.getEditor(ae);
            }
            if (editor == null) {
                return;
            }
            
            final File swaggerSpec = this.openSpecFile();
            if (swaggerSpec == null){
                return;
            }
            
            final SwaggerTestGenerator generator;
            try {
                generator = new SwaggerTestGenerator(swaggerSpec.getAbsolutePath());
            }
            catch (InvalidSpecificationException ex){
                JOptionPane.showMessageDialog(editor, 
                        "An unexpected error ocurred while processing your swagger API specification. The test model could not be generated.",
                        "Processing error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!generator.buildModel()){
                JOptionPane.showMessageDialog(editor, 
                        "An unexpected error ocurred while processing your swagger API specification. The test model could not be generated.",
                        "Processing error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String collection = this.generateModel(generator.getModel());
            if (collection != null){
                editor.getCollectionsBrowserPanel().putModelInCollection(errorModelName(), generator.getErrorModel(), collection);
                editor.getCollectionsBrowserPanel().putModelInCollection(patternModelName(), generator.getPatternModel(), collection);
            }
        }
    }
    
    /**
     * An action, which loads a Swagger based API specification and converts it to a testing model
     */
    public static class OpenRamlAPIaction extends OpenSwaggerAPIaction {

        /**
         * The returned value may be adjusted if there is an opened model with this name.
         * 
         * @return the name to use when opening the model in the collections explorer
         */
        @Override
        protected String modelName() {
            return "raml-api-test.xml";
        }
        
        /**
         * The returned value may be adjusted if there exists a collection with this name.
         * 
         * @return the name to use for a collection when opening the generated models, in execution mode 
         */
        @Override
        protected String collectionName() {
            return "raml-api-test-collection";
        }
                
                
        /**
         * The returned value may be adjusted if there is an opened model with this name.
         * 
         * @return the name to use when opening the error handling model in the collections explorer
         */
        @Override
        protected String errorModelName() {
            return "raml-api-error-test.xml";
        }
        
        /**
         * The returned value may be adjusted if there is an opened model with this name.
         * 
         * @return the name to use when opening the pattern handling model in the collections explorer
         */
        @Override
        protected String patternModelName() {
            return "raml-api-pattern-test.xml";
        }
        
        /**
         * @return the filters allowed for a file representing a raml API spec 
         */
        @Override
        protected List<FileNameExtensionFilter> getApiFileFilters(){
            final List<FileNameExtensionFilter> filters = new ArrayList<>();
            filters.add(new FileNameExtensionFilter("*.raml", "raml"));
            return filters;
        }

        /**
         * same constructor as the one for the swagger API action
         * @param editor the editor reference
         */
        public OpenRamlAPIaction(BasicGraphEditor editor) {
            super(editor);
        }

        /**
         * the method performed on this action
         * @param ae the UI action event object
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (editor == null) {
                editor = EditorActions.getEditor(ae);
            }
            if (editor == null) {
                return;
            }
            
            final File swaggerSpec = this.openSpecFile();
            if (swaggerSpec == null){
                return;
            }
            
            final RAMLTestGenerator generator;
            try {
                generator = new RAMLTestGenerator(swaggerSpec.getAbsolutePath());
            }
            catch (InvalidSpecificationException ex){
                JOptionPane.showMessageDialog(editor, 
                        "An unexpected error ocurred while processing your RAML API specification. The test model could not be generated.",
                        "Processing error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!generator.buildModel()){
                JOptionPane.showMessageDialog(editor, 
                        "An unexpected error ocurred while processing your RAML API specification. The test model could not be generated.",
                        "Processing error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String collection = this.generateModel(generator.getModel());
            if (collection != null){
                editor.getCollectionsBrowserPanel().putModelInCollection(errorModelName(), generator.getErrorModel(), collection);
                editor.getCollectionsBrowserPanel().putModelInCollection(patternModelName(), generator.getPatternModel(), collection);
            }
        }
        
    }
}
