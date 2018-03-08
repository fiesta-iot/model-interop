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
// Created By : Nikolay Stanchev
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.io.FileUtils;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.MultiTestsExecutionManager;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions;

/**
 * This panel represents the collections browser. It shows all the open models in the tool, either separated in user defined collections or
 * all bundled within the 'default' collection.
 *
 * @author ns17
 */
public class CollectionsBrowserForm extends JPanel {

    /**
     * reference to the editor
     */
    private final BasicGraphEditor editor;

    /**
     * handles the execution of multiple tests by storing captured and declared values from all past tests
     */
    private final MultiTestsExecutionManager execManager = new MultiTestsExecutionManager();

    /**
     * a getter for the multiple tests execution manager
     * @return reference to the multi-tests execution manager object
     */
    public MultiTestsExecutionManager getMultiTestsManager(){
        return execManager;
    }

    /**
     * reference to the JTree component
     */
    private final JTree tree;

    /**
     * package-private getter for the tree, only classes in the collections package should access it
     * @return the tree node object
     */
    JTree getTree(){
        return tree;
    }

    /**
     * reference to the root in the tree
     */
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Available collections");

    /**
     * package-private getter for the root, only classes in the collections package should access it
     * @return the root node object
     */
    DefaultMutableTreeNode getRoot(){
        return root;
    }

    /**
     * the name of the default collection
     */
    public static final String DEFAULT_COLLECTION = "workspace";

    /**
     * the name of the default workspace folder
     */
    public static final String DEFAULT_WORKSPACE = "workspace";

    /**
     * maps opened models to their respective model XML and the path of the file of this XML
     */
    private final Map<String, ModelState> openedModels = new HashMap<>();

    /**
     * package private getter for the map of opened models, only classes in the collections package
     * should access it
     * @return the map of opened models
     */
    Map<String, ModelState> getOpenedModels() {
        return openedModels;
    }

    /**
     * gets the first name that is not contained in the opened models map
     * @param name the original name
     * @return the generated name
     */
    private String getNextAvailableName(String name){
        int i = 1;
        name = name.trim();
        String newName = name.replaceAll(".xml", "");
        while (openedModels.containsKey(name)){
            name = newName + "(" + i + ")" + ".xml";
            i += 1;
        }

        return name;
    }

    /**
     * gets the first name that is not contained in the collections set
     * @param name the original name
     * @return the generated name
     */
    private String getNextAvailableCollectionName(String name){
        int i = 1;
        name = name.trim();
        String newName = name.replaceAll(".xml", "");
        while (collectionExists(name)){
            name = newName + "(" + i + ")";
            i += 1;
        }

        return name;
    }

    /**
     * a set to store the available collections so that duplicate categories are not allowed
     */
    private final Set<String> collections = new HashSet<>();

    /**
     * fetch the available collections in the explorer as an array
     * @return the array of available collections
     */
    Object[] getCollectionsArray(){
        return collections.toArray();
    }

    /**
     * checks if a given collection already exists
     * @param collection the name of the collection
     * @return true if it exists and false otherwise
     */
    public boolean collectionExists(String collection){
        return collections.contains(collection);
    }

    /**
     * the current model that is opened
     */
    private String currentModel = "";

    /**
     * a getter for the current model that is opened and scheduled for execution
     * @return the current test model
     */
    public synchronized String getCurrentModel(){
        return currentModel;
    }

    /**
     * a setter for the current model, package private so that only classes in the
     * collections package can set the current model
     * @param model the new currently opened model
     */
    synchronized void setCurrentModel(String model){
        currentModel = model;
    }

    /**
     * a mutator method for the JTree, adds a new collection in the browser
     * @param newCollection the collection to add
     * @param showDialogs true if you want a dialog to be shown in case of already existing collection and false otherwise
     * @return true if the collection has been added and false otherwise
     */
    public boolean addCollection (String newCollection, boolean showDialogs) {
        newCollection = newCollection.replaceAll(".xml", ""); // make sure that collection doesn't include .xml so that it is not mistaken for a model
        newCollection = newCollection.trim();
        if (!collections.contains(newCollection)){
            collections.add(newCollection);
            addCollectionFile(newCollection);
            final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            final DefaultMutableTreeNode child = new DefaultMutableTreeNode(newCollection);
            model.insertNodeInto(child, root, root.getChildCount());
            return true;
        }
        else {
            if (showDialogs){
                JOptionPane.showMessageDialog(tree, "A collection with this name already exists.", "Duplicate collection name", JOptionPane.WARNING_MESSAGE);
            }
            return false;
        }
    }

    /**
     * This method generates a collection in the explorer, that is given a name, fetches the next available name for the collection
     * and then creates it. Used when generating a collection for the models for API tests.
     *
     * @param newCollection the required new collection's name
     * @return the adjusted name of the collection
     */
    public String generateCollection (String newCollection){
        newCollection = this.getNextAvailableCollectionName(newCollection);  // get next available name

        this.addCollection(newCollection, false);  // add the collection with no pop up dialogs

        return newCollection;  // return the adjusted name of the collection
    }

    /**
     * a mutator method for the JTree, adds a new collection in the browser, assumes by default that a dialog must be shown in case of an existing collection
     * @param newCollection the collection to add
     * @return true if the collection has been added and false otherwise
     */
    public boolean addCollection (String newCollection){
        return addCollection(newCollection, true);
    }

    /**
     * an auxiliary method for adding new collections, which creates the actual collection folder in
     * the workspace location, including the .config file used to lock empty collections
     *
     * @param newCollection the name of the new collection
     */
    private void addCollectionFile (String newCollection){
        final File collection = editor.getWorkspace().resolve(newCollection).toFile();
        if (!collection.exists())
            collection.mkdir();

        final File configFile = new File(collection, ".config");
        if (!configFile.exists()){
            try {
                configFile.createNewFile();
                editor.apppendLock(configFile);
            } catch (IOException ex) {}
        }
        else {
            editor.apppendLock(configFile);
        }
    }

    /**
     * saves a collection folder, that is save all the models located in this collection
     * @param collectionToSave the name of the collection to save
     */
    public void saveCollection(String collectionToSave) {
        // check if workspace is locked and unlock it if necessary
        final boolean locked = editor.isWorkspaceLocked();
        if (locked) {
            editor.unlockWorkspace();
        }

        final File workspace = editor.getWorkspace().toFile();
        final File collection = new File(workspace, collectionToSave);

        // this check is optional, that is the collection will for sure exist because it was created by the tool and it is found
        // in the collections explorer, unless the collection was deleted during the usage of the tool with the local file system
        if (!collection.exists()) {
            collection.mkdir();
        }

        String model;
        String name;
        File modelFile;
        FileWriter fw = null;
        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i).toString().equals(collectionToSave)) {
                for (int j = 0; j < root.getChildAt(i).getChildCount(); j++) {
                    name = root.getChildAt(i).getChildAt(j).toString();
                    model = openedModels.get(name).getXml();
                    modelFile = new File(collection, name);

                    if (editor.getCurrentFile() != null && modelFile.getName().equalsIgnoreCase(editor.getCurrentFile().getName())) {
                        editor.setModified(false);
                    }

                    try {
                        fw = new FileWriter(modelFile);
                        fw.write(model);
                        openedModels.get(name).setModified(false);
                    } catch (IOException ex) {
                        // continue in case of an exception by skipping this model
                    } finally {
                        if (fw != null) {
                            try {
                                fw.close();
                            } catch (IOException ex) {
                                // nothing to do here
                            }
                        }
                    }
                }

                break;
            }
        }

        // if workspace was initially locked, lock it again
        if (locked) {
            editor.lockWorkspace();
        }
    }

    /**
     * this method removes a collection
     * @param collectionToRemove the name of the collection to remove
     */
    public void removeCollection (String collectionToRemove){
        for (int i=0; i<root.getChildCount(); i++){
            TreeNode node = root.getChildAt(i);
            if (node.toString().equals(collectionToRemove)){
                // remove all models from the opened models, when removing a collection
                for (int j = 0; j < node.getChildCount(); j++){
                    openedModels.remove(node.getChildAt(j).toString());
                }

                // remove the collection itself
                ((DefaultTreeModel) tree.getModel()).removeNodeFromParent((DefaultMutableTreeNode) node);
                collections.remove(collectionToRemove);
                removeCollectionFile(collectionToRemove);

                // if current model is in the collecion to be removed, open the first model that is available so that the current model doesn't hang up
                for (int k=0; k<node.getChildCount(); k++){
                    if (currentModel.equals(node.getChildAt(k).toString())){
                        if (!openedModels.isEmpty()){
                            //  switch to the first available model in the collections
                            final String firstModel = (String) openedModels.keySet().toArray()[0];
                            FileActions.OpenAction.openFromCollection(editor, openedModels.get(firstModel).getXml(), openedModels.get(firstModel).getPath(),
                                    openedModels.get(firstModel).getModified());
                            setCurrentModel(firstModel);
                        }

                        break;
                    }
                }

                break;
            }
        }
    }

    /**
     * an auxiliary method for removing collections, which deletes the actual collection folder in
     * the workspace location
     *
     * @param collectionToRemove the name of the collection to delete
     */
    private void removeCollectionFile (String collectionToRemove){
        // check if the workspace is locked and unlock it before modifying
        final boolean locked = editor.isWorkspaceLocked();
        if (locked){
            editor.unlockWorkspace();
        }

        final File collection = editor.getWorkspace().resolve(collectionToRemove).toFile();
        try {
            FileUtils.deleteDirectory(collection);
        }
        catch (IOException | IllegalArgumentException ex) {
            // in case of an exception, do nothing
        }

        // if the workspace was initially locked, lock it again after modification
        if (locked){
            editor.lockWorkspace();
        }
    }

    /**
     * this method renames a collection
     * @param oldCollection the old collection's name
     * @param newCollection the new collection's name
     */
    public void renameCollection (String oldCollection, String newCollection){
        newCollection = newCollection.replaceAll(".xml", ""); // make sure that collection doesn't include .xml so that it is not mistaken for a model
        newCollection = newCollection.trim();
        if (!collections.contains(newCollection)){
            for (int i=0; i<root.getChildCount(); i++){
                TreeNode node = root.getChildAt(i);
                if (node.toString().equals(oldCollection)){
                    ((DefaultMutableTreeNode) node).setUserObject(newCollection);
                    ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                    collections.remove(oldCollection);
                    collections.add(newCollection);
                    // rename the collection file and update the saved paths for all models in this collection
                    File newCollectionFile = renameCollectionFile(oldCollection, newCollection);
                    File adjustedModel;
                    String modelName;
                    for (int j=0; j<node.getChildCount(); j++){
                        modelName = node.getChildAt(j).toString();
                        adjustedModel = new File(newCollectionFile, modelName);
                        openedModels.get(modelName).setPath(adjustedModel.getAbsolutePath());
                        if (currentModel != null && currentModel.equals(modelName)){
                            editor.setCurrentFile(adjustedModel);
                        }
                    }

                    break;
                }
            }
        }
        else {
            JOptionPane.showMessageDialog(tree, "A collection with this name already exists.", "Duplicate collection name", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * this method renames the actual file (directory) representing the collection
     * @param oldCollection the old name of the collection
     * @param newCollection the new name of the collection
     */
    private File renameCollectionFile(String oldCollection, String newCollection){
        // check if the workspace is locked and unlock it before modifying
        final boolean locked = editor.isWorkspaceLocked();
        if (locked){
            editor.unlockWorkspace();
        }

        final File oldCollectionFile = editor.getWorkspace().resolve(oldCollection).toFile();
        final File newCollectionFile = editor.getWorkspace().resolve(newCollection).toFile();
        try {
            FileUtils.moveDirectory(oldCollectionFile, newCollectionFile);
        } catch (IOException ex) {
            // do nothing on exception
        }

        // if the workspace was initially locked, lock it again after modification
        if (locked){
            editor.lockWorkspace();
        }

        return newCollectionFile;
    }

    /**
     * opens a new model in the default collection
     * @param name the name of the opened file
     * @param xml the name of the opened XML
     * @param lastXML the model of the previously opened XML
     * @param lastModified the last state of the modified flag
     * @return the path object of the file of the model added in the collection
     */
    public Path openDefaultModel(String name, String xml, String lastXML, boolean lastModified){
        return openModelInCollection(name, xml, DEFAULT_COLLECTION, lastXML, lastModified);
    }

    /**
     * this method opens a model in a certain collection
     * @param name the name of the opened model
     * @param xml the xml of the model
     * @param collection the collection to open this model into
     * @param lastXML the model of the previously opened XML
     * @param lastModified the last state of the modified flag
     * @return the path object of the file of the model added in the collection
     */
    public Path openModelInCollection(String name, String xml, String collection, String lastXML, boolean lastModified) {
        // check that the extension is added, so that opened models can be separated from categories
        if (!name.endsWith(".xml")){
            name = name + ".xml";
        }

        // get the next available name for a model
        name = getNextAvailableName(name);

        // save last model and put the new model in the map
        if (currentModel != null && openedModels.containsKey(currentModel) && lastXML != null) {
            openedModels.get(currentModel).setXML(lastXML);
            openedModels.get(currentModel).setModified(lastModified);
        }

        final Path pathObject = createModelFile(collection, name, xml);
        final String path = pathObject.toString();

        openedModels.put(name, new ModelState(name, xml, path, false));
        currentModel = name;

        // insert new model in the tree, first find the collection node
        final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode collectionNode = null;
        for (int j=0; j < root.getChildCount(); j++){
            TreeNode node = root.getChildAt(j);
            if (node.toString().equals(collection)){
                collectionNode = (DefaultMutableTreeNode) node;
                break;
            }
        }

        // if collection node found, insert child nodes
        if (collectionNode != null){
            final DefaultMutableTreeNode child = new DefaultMutableTreeNode(name);
            model.insertNodeInto(child, collectionNode, collectionNode.getChildCount());
        }

        return pathObject;
    }

    /**
     * puts a model in a collection without opening it in the tool
     * @param name the name of the model
     * @param xml the xml content of the model
     * @param collection the collection to put the model into
     */
    public void putModelInCollection (String name, String xml, String collection) {
        // check that the extension is added, so that opened models can be separated from categories
        if (!name.endsWith(".xml")){
            name = name + ".xml";
        }

        // get the next available name for a model
        name = getNextAvailableName(name);

        String path = createModelFile(collection, name, xml).toString();

        // put the new model in the map
        openedModels.put(name, new ModelState(name, xml, path, false));

        // insert new model in the tree, first find the collection node
        final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode collectionNode = null;
        for (int j=0; j < root.getChildCount(); j++){
            TreeNode node = root.getChildAt(j);
            if (node.toString().equals(collection)){
                collectionNode = (DefaultMutableTreeNode) node;
                break;
            }
        }

        // if collection node found, insert child nodes
        if (collectionNode != null){
            final DefaultMutableTreeNode child = new DefaultMutableTreeNode(name);
            model.insertNodeInto(child, collectionNode, collectionNode.getChildCount());
        }


    }

    /**
     * creates an actual xml file representing the model in the specified collection
     * @param collection the collection name
     * @param model the model name
     * @param xml the xml of the model
     */
    private Path createModelFile(String collection, String model, String xml){
        final File modelFile = new File(editor.getWorkspace().resolve(collection).toFile(), model);
        try {
            if (!modelFile.exists()){
                modelFile.createNewFile();
            }

            // programatic save of the model must be put here, instead of just creating an empty xml file with the name of the model
            writeModelToFile(modelFile, xml);

            // lock this file, so that it is only used by the tool
            editor.apppendLock(modelFile);

            return modelFile.toPath();
        }
        catch (IOException ex) {
            return null;
        }
    }

    /**
     * copies the xml model into a newly created model file
     * @param modelFile the file to use
     * @param modelXML the xml of the model
     */
    private void writeModelToFile(File modelFile, String modelXML){
        FileWriter fw = null;
        try {
            fw = new FileWriter(modelFile);
            fw.write(modelXML);
        }
        catch (IOException ex) {
            // do nothing in case of an exception, that is don't copy the model
        }
        finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex) {
                    // nothing to do here
                }
            }
        }
    }

    /**
     * A method to rename a model in the collections explorer
     * @param modelToRename the old name of the model to rename
     * @param newModelName the new name of the model
     */
    public void renameModel(String modelToRename, String newModelName){
        // check that the extension is added, so that opened models can be separated from categories
        if (!newModelName.endsWith(".xml")){
            newModelName = newModelName + ".xml";
        }

         if (modelToRename.equalsIgnoreCase(newModelName)){
            return;  // don't do anything if renaming to the same name
        }


        // first, find and remove the node for the model from its collection
        TreeNode collection = null;
        TreeNode model = null;
        boolean found = false;
        for (int i = 0; i < root.getChildCount(); i++) {
            collection = root.getChildAt(i);
            for (int j = 0; j < collection.getChildCount(); j++) {
                model = collection.getChildAt(j);
                if (model.toString().equals(modelToRename)) {
                    found = true;
                    break;
                }
            }

            if (found){
                break;
            }
        }

        // get the next available name for a model
        newModelName = getNextAvailableName(newModelName);

        // rename the model node
        ((DefaultMutableTreeNode) model).setUserObject(newModelName);
        ((DefaultTreeModel) tree.getModel()).nodeChanged(model);

        execManager.removeAllTestContent(modelToRename);
        execManager.removeAllTestHeaders(modelToRename);
        execManager.removeAllTestPatternValues(modelToRename);

        // rename the model file and get the new path of the model
        final String modelNewPath = renameModelFile(modelToRename, newModelName, collection.toString());

        final ModelState state = openedModels.get(modelToRename);
        state.setName(newModelName);
        state.setPath(modelNewPath);
        // put the new model with its respective path and xml into the opened models map
        openedModels.put(newModelName, state);
        // remove the model to be renamed from the opened models
        openedModels.remove(modelToRename);
    }

    /**
     * An auxiliary method, which renames the actual file representing the model
     * @param modelToRename the old name of the model to rename
     * @param newModelName the new name of the model
     * @param modelCollection the collection this model is stored in
     */
    private String renameModelFile(String modelToRename, String newModelName, String modelCollection){
        // check if the workspace is locked and unlock it before modifying
        final boolean locked = editor.isWorkspaceLocked();
        if (locked){
            editor.unlockWorkspace();
        }

        final File oldModelFile = editor.getWorkspace().resolve(modelCollection).resolve(modelToRename).toFile();
        final File newModelFile = editor.getWorkspace().resolve(modelCollection).resolve(newModelName).toFile();
        try {
            FileUtils.moveFile(oldModelFile, newModelFile);
        } catch (IOException ex) {
            // do nothing on exception
        }

        // if the workspace was initially locked, lock it again after modification
        if (locked){
            editor.lockWorkspace();
        }

        editor.setCurrentFile(newModelFile);

        return newModelFile.getAbsolutePath();
    }

    /**
     * moves a model from its old collection to a new collection
     * @param modelToMove the name of the model to move
     * @param newCollection the name of the collection to move the model into
     */
    public void moveModel(String modelToMove, String newCollection){
        // first, find and remove the node for the model from its collection
        TreeNode collection = null;
        TreeNode model = null;
        boolean found = false;
        for (int i = 0; i < root.getChildCount(); i++) {
            collection = root.getChildAt(i);
            for (int j = 0; j < collection.getChildCount(); j++) {
                model = collection.getChildAt(j);
                if (model.toString().equals(modelToMove)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                break;
            }
        }

        // if moving to the same collection do nothing
        if (collection.toString().equals(newCollection)) {
            return;
        }

        // then, remove the model from the old collection
        ((DefaultTreeModel) tree.getModel()).removeNodeFromParent((DefaultMutableTreeNode) model);

        // then add the model into the new collection
        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i).toString().equals(newCollection)) {
                final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                final DefaultMutableTreeNode insertCollection = (DefaultMutableTreeNode) root.getChildAt(i);
                treeModel.insertNodeInto((DefaultMutableTreeNode) model, insertCollection, root.getChildAt(i).getChildCount());
                break;
            }
        }

        final File newModelFile = moveModelFile(model.toString(), collection.toString(), newCollection);
        if (newModelFile != null){
            openedModels.get(model.toString()).setPath(newModelFile.getAbsolutePath());
            // if moving the currently opened model, update the title of the tool to show the relevant path of the model
            if (currentModel.equals(modelToMove)){
                editor.setCurrentFile(newModelFile);
            }
        }
    }

    /**
     * moves the actual file of the model from one collection to another (that is from one directory to another)
     * @param modelToMove the name of the model to move
     * @param oldCollection the name of the old collection
     * @param newCollection the name of the new collection
     */
    private File moveModelFile(String modelToMove, String oldCollection, String newCollection){
        // check if the workspace is locked and unlock it before modifying
        final boolean locked = editor.isWorkspaceLocked();
        if (locked){
            editor.unlockWorkspace();
        }

        final Path modelFile = editor.getWorkspace().resolve(oldCollection).resolve(modelToMove);
        final Path newCollectionFile = editor.getWorkspace().resolve(newCollection);
        final File newModelFile = new File(newCollectionFile.toFile(), modelToMove);

        boolean successful;
        try {
            FileUtils.moveFile(modelFile.toFile(), newModelFile);
            successful = true;
        } catch (IOException ex) {
            // skip in case of an exception
            successful = false;
        }

        // if the workspace was initially locked, lock it again after modification
        if (locked){
            editor.lockWorkspace();
        }

        return successful ? newModelFile : null;
    }

    /**
     * a method which removes a model from the collections tree
     * @param modelToRemove the name of the model to remove
     */
    public void removeModel(String modelToRemove){
        if (modelToRemove == null){
            return;
        }

        openedModels.remove(modelToRemove);
        TreeNode collection;
        TreeNode model;
        boolean found = false;
        for (int i = 0; i < root.getChildCount(); i++) {
            collection = root.getChildAt(i);
            for (int j = 0; j < collection.getChildCount(); j++) {
                model = collection.getChildAt(j);
                if (model.toString().equals(modelToRemove)) {
                    ((DefaultTreeModel) tree.getModel()).removeNodeFromParent((DefaultMutableTreeNode) model);
                    removeModelFile(model.toString(), collection.toString());
                    found = true;
                    break;
                }
            }

            if (found)
                break;

        }

        // if the model to remove is the currently opened model, switch to the first available model in the collections
        if (currentModel.equals(modelToRemove) && !openedModels.isEmpty()){
            final String firstModel = (String) openedModels.keySet().toArray()[0];
            FileActions.OpenAction.openFromCollection(editor, openedModels.get(firstModel).getXml(), openedModels.get(firstModel).getPath(),
                    openedModels.get(firstModel).getModified());
            setCurrentModel(firstModel);
        }
    }

    /**
     * removes the model from a collection, this method deletes the actual file of the model
     * @param modelToRemove
     * @param modelCollection
     */
    private void removeModelFile (String modelToRemove, String modelCollection){
        // check if the workspace is locked and unlock it before modifying
        final boolean locked = editor.isWorkspaceLocked();
        if (locked){
            editor.unlockWorkspace();
        }

        final Path modelFile = editor.getWorkspace().resolve(modelCollection).resolve(modelToRemove);

        try {
            Files.delete(modelFile);
        } catch (IOException ex) {
            // skip in case of an exception
        }

        // if the workspace was initially locked, lock it again after modification
        if (locked){
            editor.lockWorkspace();
        }
    }

    /**
     * saves the new xml model for some identifier name, otherwise adds the new model to default collection
     * @param name the name of the model
     * @param path the path of this model
     * @param xml the model
     * @param saveAsClicked whether this method is performed after a Save As option clicked
     * @return returns the path of the model saved in the workspace if saveAs button clicked or null if an ordinary save is performed
     */
    public Path saveModel (String name, String path, String xml, boolean saveAsClicked){
        if (saveAsClicked) {
            // if save as clicked remove the last opened model, since the new one will be the same
            removeModel(currentModel);
            this.currentModel = name;

            final Path openedModel = openDefaultModel(name, xml, null, false);

            return openedModel;
        }

        if (openedModels.keySet().contains(name)){
            openedModels.get(name).setXML(xml);
            openedModels.get(name).setPath(path);
            openedModels.get(name).setModified(false);
        } else {
            openedModels.put(name, new ModelState(name, xml, path, false));
        }

        return null;
    }

    /**
     * constructs the CategoriesBrowserForm
     * @param editor reference to the editor
     */
    public CollectionsBrowserForm(BasicGraphEditor editor) {
        super(new BorderLayout());  // called for clarity reasons

        this.editor = editor;
        this.collections.add(DEFAULT_COLLECTION);

        // build the three popup menus used to respond to node selection
        final TreeEditingMenu popupRoot = new RootEditingMenu(editor, this);
        final TreeEditingMenu popupCollection = new CollectionEditingMenu(editor, this);
        final TreeEditingMenu popupModel = new ModelEditingMenu(editor, this);

        // init the JTree component representing the explorer
        root.add(new DefaultMutableTreeNode(DEFAULT_COLLECTION));
        tree = new JTree(root);

        // change the selection model to allow the selection of models only when control key is pressed
        tree.setSelectionModel(new DefaultTreeSelectionModel(){
            private TreePath[] getModelNodes(TreePath[] originalPaths) {
                if (this.getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION){
                    return originalPaths;
                }

                List<TreePath> models = new ArrayList<>();
                for (TreePath path: originalPaths){
                    if (path.getLastPathComponent().toString().endsWith(".xml")){
                        models.add(path);
                    }
                }
                // return only those paths that select a model
                return models.toArray(originalPaths);
            }

            @Override
            public void setSelectionPaths(TreePath[] pPaths) {
                super.setSelectionPaths(getModelNodes(pPaths));
            }

            @Override
            public void addSelectionPaths(TreePath[] pPaths) {
                super.addSelectionPaths(getModelNodes(pPaths));
            }
        });

        // customize the tree
        tree.setShowsRootHandles(true);
        tree.setBackground(new Color(247, 250, 255));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new CustomisedTreeCellRenderer("/images/graph16.png", "/images/open16.png", "/images/bricks.png", root.toString()));

        // add event listeners to the tree
        tree.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                // check if selection is out of the bounds of the tree
                final int clickedRow = tree.getRowForLocation(e.getX(), e.getY());
                if (clickedRow == -1){
                    tree.clearSelection();
                    return;
                }

                if (e.getClickCount() == 2){
                    final String clicked = tree.getPathForRow(clickedRow).getLastPathComponent().toString();
                    if (clicked.contains(".xml")) {
                        if (currentModel != null && openedModels.containsKey(currentModel)) {
                            openedModels.get(currentModel).setXML(editor.getDataModel().getGraphXML());
                            openedModels.get(currentModel).setModified(editor.isModified());
                        }

                        FileActions.OpenAction.openFromCollection(editor, openedModels.get(clicked).getXml(), openedModels.get(clicked).getPath(),
                                openedModels.get(clicked).getModified());
                        currentModel = clicked;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e){
                final int row = tree.getRowForLocation(e.getX(), e.getY());
                if (row == 0){
                    if (e.isPopupTrigger()) {
                        popupRoot.show((JComponent) e.getSource(), e.getX(), e.getY());
                    }
                }
                else if (row > 0){
                    if (e.isPopupTrigger()){
                        final String clicked = tree.getPathForRow(row).getLastPathComponent().toString();
                        tree.setSelectionPath(tree.getPathForRow(row));

                        if (!clicked.contains(".xml")){
                            popupCollection.setLastClicked(clicked);
                            popupCollection.show((JComponent) e.getSource(), e.getX(), e.getY());
                        }
                        else {
                            popupModel.setLastClicked(clicked);
                            popupModel.show((JComponent) e.getSource(), e.getX(), e.getY());
                        }
                    }
                }
            }
        });

        tree.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTROL)
                    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_CONTROL){
                    return;
                }

                int check = JOptionPane.showConfirmDialog(tree, "Do you want to execute the selected tests?", "Executing selected tests", JOptionPane.YES_NO_OPTION);
                if (check == JOptionPane.YES_OPTION){
                    TreePath[] paths = tree.getSelectionPaths();
                    List<String> testsToExecute = new ArrayList<>();
                    String model;
                    for (TreePath path: paths){
                        model = path.getLastPathComponent().toString();
                        if (model != null && model.endsWith(".xml")){
                            testsToExecute.add(model);
                        }
                    }

                    tree.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);

                    check = JOptionPane.showConfirmDialog(tree,
                            "<html><p>Do you want all tests to run in execution mode without stopping for confirmation to continue ?</p>"
                            + "<ul><li>Click Yes to execute all tests with no interventions.</li>"
                            + "<li>Click No to execute the tests one by one with debuging capability.</li>"
                            + "<li>Click Cancel to stop the execution.</li><ul></html>",
                            "Test mode", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (check == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                    final boolean askUser = check == JOptionPane.NO_OPTION;

                    final StringBuilder summaryReportTrace = new StringBuilder();
                    summaryReportTrace.append("Interoperability tests - Summary report\n\n\n");

                    final Thread runThread = new Thread() {
                        @Override
                        public void run() {
                            String test;
                            int continueExecuting;
                            int failures = 0;
                            for (int i=0; i < testsToExecute.size(); i++) {
                                test = testsToExecute.get(i);

                                // if the test to execute is not the current opened model, swith to the test that is going to be executed
                                if (!test.equals(currentModel)) {
                                    if (currentModel != null && openedModels.containsKey(currentModel)) {
                                        openedModels.get(currentModel).setXML(editor.getDataModel().getGraphXML());
                                        openedModels.get(currentModel).setModified(editor.isModified());
                                    }

                                    FileActions.OpenAction.openFromCollection(editor, openedModels.get(test).getXml(), openedModels.get(test).getPath(),
                                            openedModels.get(test).getModified());
                                    currentModel = test;
                                }

                                // executed the current test
                                new EditorActions.ExecuteAction(editor, askUser).actionPerformed(null);

                                while (editor.isRunning()) {
                                    // wait until the test is finished before proceeding with the next one
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException ex) {
                                        // continue looping untill test has finished running
                                    }
                                }

                                summaryReportTrace.append("Test number ").append(i+1).append("\n");
                                summaryReportTrace.append("\tTest identifier - ").append(test).append("\n");
                                summaryReportTrace.append("\tTest execution passed - ").append(editor.getStateMachine().getReport().getSuccess()).append("\n\n");
                                failures = editor.getStateMachine().getReport().getSuccess().equalsIgnoreCase("true") ? failures : failures + 1;

                                if (askUser && i != testsToExecute.size() - 1) {
                                    continueExecuting = JOptionPane.showConfirmDialog(editor, "Do you want to continue executing the rest of the tests?",
                                            "Continue executing", JOptionPane.YES_NO_OPTION);
                                    if (continueExecuting == JOptionPane.NO_OPTION) {
                                        break;
                                    }
                                } else if (i == testsToExecute.size() - 1) {
                                     if (failures == 0) {
                                        summaryReportTrace.append("Successful test execution\n");
                                        summaryReportTrace.append(i+1).append(" tests were executed with no failures detected\n");
                                    }
                                    else {
                                        summaryReportTrace.append("Unsuccessful test execution\n");
                                        final String failureString = failures == 1 ? " failure" : " failures";
                                        summaryReportTrace.append(i+1).append(" tests were executed with ").append(failures).append(failureString).append(" detected\n");
                                    }

                                    // don't reset the test data maps, we want to keep them in memory (e.g. similar to cache)
                                    // execManager.resetMaps();

                                    final int check = JOptionPane.showConfirmDialog(editor, "The execution of the tests has been completed. Do you want to view the summary test report?",
                                            "Completed execution", JOptionPane.YES_NO_OPTION);
                                    editor.getCodePanel().getReportsPanel().addTabReport(summaryReportTrace.toString());
                                    if (check == JOptionPane.YES_OPTION) {
                                        final CardLayout sideLayout = (CardLayout) editor.getAttributePanel().getLayout();
                                        sideLayout.show(editor.getAttributePanel(), "empty");
                                        new EditorActions.ReportsAction(editor).actionPerformed(null);
                                    }
                                }
                            }
                        }
                    };
                    runThread.start();
                }
                else {
                    tree.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
                }
            }
        });

        add(new JScrollPane(tree));
    }
}
