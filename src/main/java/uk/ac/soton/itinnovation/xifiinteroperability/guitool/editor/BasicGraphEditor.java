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

/**
 * Modified by pjg@it-innovation.soton.ac.uk
 *
 * Created the Interoperability tool from the sample Graph Editor
 * client application.
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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor;

import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections.FileLocker;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.mxOrganicLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.model.mxGraphModel.mxTerminalChange;
import com.mxgraph.model.mxGraphModel.mxValueChange;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphOutline;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxResources;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.AbstractGraphElement;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Message;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections.CollectionsBrowserForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.ExecutionPanel;
import static uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.AttributePanel.EXECUTION;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.StateMachine;

/**
    Interoperability Tool: A software developer tool to test whether applications
    interoperate with HTTP RESTful services and FI-WARE enablers.
    Copyright (C) 2014  IT Innovation, University of Southampton

    Interoperability Tool is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Interoperability Tool is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Interoperability Tool.  If not, see <http://www.gnu.org/licenses/>.
 */

public class BasicGraphEditor extends JPanel {

    /**
    * There are two Panels within the editor: i) the main panel, where graphs
    * are drawn, XML displayed (code panel); ii) the panel where attribute
    * data is added.
    */

    /**
     * The Attribute panel for adding data to graph nodes and transitions.
     */
    private final transient AttributePanel ctablePanel;

    /**
     * a getter method for the test animation panel
     * @return the panel which displays the graph during test execution
     */

    private final ExecutionPanel execPanel;
    public ExecutionPanel getExecPanel(){
        return execPanel;
    }

    /**
     * The code panel for viewing the graph, xml, testing etc.
     */
    private final transient MainDisplayPanel codetablePanel;

    /**
     * Getter for the code panel section of the GUI.
     * @return The reference to the code panel elements.
     */
    public final MainDisplayPanel getCodePanel() {
        return codetablePanel;
    }

    /**
     * The tabbed panel for the code/graph/testing. Variable is used to switch
     * the view between "xml/graph/test"
     */
    private final transient JPanel codePanel;

    /**
     * Get the main area of the GUI where the graphs, code and xml is displayed.
     * @return Reference to the JPanel area of the graph, xml output.
     */
    public final JPanel getMainArea() {
        return codePanel;
    }

    /**
     * Adds required resources for i18n
     */
    static {
        try {
            mxResources.add("gui/editor");
        } catch (Exception e) {
            ServiceLogger.LOG.error("Could not load resources for GUI", e);
        }
    }

    /**
     * The editor has two graph components, which are views to create/edit
     * graphs.
     */
    private final transient mxGraphComponent graphComponent;     // behaviour graph

    /**
     * Getter for the behaviour graph GUI model i.e. the mxGraph component.
     * @return The mxGraph reference for the GUI drawn behaviour model.
     */
    public final mxGraphComponent getBehaviourGraph() {
        return graphComponent;
    }

    /**
     * The graph outline element of mxGraph for the state machine behaviour.
     */
    private final transient mxGraphOutline behavGrphOutline;

    /**
     * Graph component for the set of elements in the distributed system.
     */
    private final transient mxGraphComponent arcGraphComponent;

    /**
     * Get the graph component for the elements of the distributed system.
     * @return The reference to the mxGraph of the dis system model.
     */
    public final mxGraphComponent getSystemGraph() {
        return arcGraphComponent;
    }

    /**
     * The mxGraph outline of the distributed system gui model.
     */
    private final transient  mxGraphOutline systemGrphOutline;

    /**
     * The attribute panel is a set of cards for adding attibutes to GUI
     * elements.
     */
    private final transient JPanel attributePanel = new JPanel(new CardLayout());

    /**
     * Getter for the attribute card panel.
     * @return The Jpanel reference to the attributes card panel.
     */
    public final JPanel getAttributePanel() {
        return sidePanel;
    }

    /**
     * gets the collections browser
     * @return the extended JPanel handling the multiple opened models and spreading them in collections
     */
    public final CollectionsBrowserForm getCollectionsBrowserPanel(){
        return ctablePanel.getBrowserForm();
    }
    
    /**
     * gets the JPanel in the attribute panel responsible for changing the cards in the Card layout
     * @return 
     */
    public final JPanel getFormPanel() {
        return ctablePanel.getAttribuePanel();
    }

    /**
     * The palette tabbed pane. Contains the two palettes - one for components
     * and one for behaviour symbols
     */
    private final transient JTabbedPane libraryPane;

    /**
     * Handle undo events.
     */
    private final transient mxUndoManager undoManager;

    /**
     * handles the copy pasting of graph components
     */
    private final transient CopyPasteManager copyPasteManager;

    /**
     * a getter method for the copy-paste manager
     * @return the copyPasteManager reference
     */
    public CopyPasteManager getCopyPasteManager(){
        return copyPasteManager;
    }

    /**
     * handles the certification services
     */
    private final transient CertificationManager certificationManager;

    /**
     * a getter method for the certification manager
     * @return the certificationManager reference
     */
    public CertificationManager getCertificationManager(){
        return certificationManager;
    }

    /**
     * Handle XML undo events
     */
    private final transient XMLUndoManager xmlUndoManager;

    /**
     * get the xmlUndoManager
     * @return the xmlUndoManager
     */
    public XMLUndoManager getXmlUndoManager(){
        return xmlUndoManager;
    }

    /**
     * a method to reset the Undo managers
     */
    public void resetUndoManagers(){
        undoManager.clear();
        xmlUndoManager.clear();
    }

    /**
     * Frame title.
     */
    private final transient String appTitle;

    /**
     * The status text at the bottom left of the frame.
     */
    private final transient JLabel statusBar;

    /**
     * All the collected file locks of the workspace, saved so that they could be referenced when releasing locks
     */
    private transient List<FileLock> workspaceLocks;
    
    /**
     * Locks the current workspace location
     * @return true if workspace has been locked successfully
     */
    public boolean lockWorkspace(){
        workspaceLocks = FileLocker.lockDir(workspaceLocation);
        return workspaceLocks == null;
    }
    
    /**
     * Unlocks the current workspace location
     * @return true if workspace has been unlocked successfully
     */
    public boolean unlockWorkspace(){
        if (workspaceLocks == null){
            return true;
        }
        
        boolean unlocked = FileLocker.releaseDirLocks(workspaceLocks);
        if (unlocked){
            workspaceLocks = null;
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * This method adds a new file lock to the list of available locks, to be used
     * when a new file is added to the workspace, that needs to be locked
     * @param fileToLock the file to lock
     */
    public void apppendLock(File fileToLock){
        FileLocker.addLock(fileToLock, workspaceLocks);
    }
    
    /**
     * This method checks if the workspace is being locked.
     * @return true if the workspace folder is locked and must be unlocked before modifying files in it
     */
    public boolean isWorkspaceLocked(){
        return workspaceLocks != null;
    }
    
    /**
     * this is the location of the workspace, that is the folder where all models opened
     * in the default collection are stored
     */
    private transient Path workspaceLocation;
    
    /**
     * getter for the workspace location
     * @return the workspace location
     */
    public Path getWorkspace(){
        return workspaceLocation;
    }
    
    /**
     * setter for the workspace location, checks if the chosen location is the workspace folder itself,
     * if not, it creates a workspace folder in the chosen location
     * @param workspace the chosen workspace
     */
    public void setWorkspace(String workspace){
        final File location;
        // check if the chosen folder is an already created workspace
        if (workspace.endsWith(CollectionsBrowserForm.DEFAULT_WORKSPACE)){
            location = new File(workspace);
        }
        else {
            // check if the chosen folder is a location for an already created workspace, if not create the workspace directory
            location = new File(workspace, CollectionsBrowserForm.DEFAULT_WORKSPACE);
            if (!location.exists())
                location.mkdir();
        }
        
        workspaceLocation = location.toPath();
        
        // create the default collection folder, including the config file used for locking
        final File defaultCollection = new File(location, CollectionsBrowserForm.DEFAULT_COLLECTION);
        if (!defaultCollection.exists())
            defaultCollection.mkdir();
        final File configFile = new File(defaultCollection, ".config");
        if (!configFile.exists()){
            try {
                configFile.createNewFile();
            } catch (IOException ex) {}
        }
        
        // get all available collections in the workspace (that is all directories)
        // this is done to ensure that if user is using old workspace, all collections are opened again
        File[] collections = location.listFiles((File file) -> file.isDirectory());
        
        // open each collection in the workspace
        for (File collection : collections){
            FileActions.OpenCollectionAction.openCollection(this, collection, false);
        }
        
        this.lockWorkspace();
    }
    
    /**
     * Only 1 file can be edited at a time. This is the reference to the current
     * open/save file.
     */
    private transient File currentFile;

    /**
     * Flag indicating whether the current graph has been modified.
     */
    private boolean modified = false;

    /**
     * a boolean to signify is there is currently a test running or not
     */
    private boolean testRunning = false;

    /**
     * a getter for the testRunning attribute
     * @return true if there is currently a test running, false otherwise
     */
    public synchronized boolean isRunning() {
        return testRunning;
    }

    /**
     * a setter for the testRunning attribute
     * @param testRunning represents whether there is a test currently running
     */
    public synchronized void setRunning(boolean testRunning) {
        this.testRunning = testRunning;
    }

    /**
     * a setter for the stateMachine reference
     * @param stateMachine the stateMachine reference for the current test
     */
    public void setStateMachine(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }

    /**
     * a reference to the last state machine that was used for testing
     */
    private StateMachine stateMachine = null;

    /**
     * a getter for the reference of  the last state machine in use
     * @return the last StateMachine used for testing
     */
    public StateMachine getStateMachine(){
        return stateMachine;
    }

    private final JPanel sidePanel;

    /**
     * a method which resets the whole editor, called when opening new models
     */
    public void resetEditor(){
        this.getDataModel().clearData();
        this.resetUndoManagers();
        this.setRunning(false);
        this.updateTableView(null);
        this.getXmlUndoManager().add(this.getDataModel().getState());
        this.getCertificationManager().resetURL();
        this.getExecPanel().resetGraph();
    }

    /**
     * Add a listener to react to undo events.
     */
    private final transient mxIEventListener undoHandler = new mxIEventListener() {
        @Override
        public void invoke(final Object source, final mxEventObject evt) {
            List<mxUndoableEdit.mxUndoableChange> changes = ((mxUndoableEdit) evt
                    .getProperty("edit")).getChanges();

            if (changes.size() == 4){
                // this is to avoid adding invalid transitions as an event for the undo manager
                if (changes.get(0) instanceof mxChildChange && changes.get(3) instanceof mxChildChange){
                    if (changes.get(1) instanceof mxTerminalChange && changes.get(2) instanceof mxTerminalChange){
                        return;
                    }
                }
            }

            undoManager.undoableEditHappened((mxUndoableEdit) evt
                    .getProperty("edit"));

            if (changes.size() == 1){
                // manually adding event when there is just a change in the location of a node
                if (changes.get(0) instanceof mxGeometryChange){
                    xmlUndoManager.add(dataModel.getState());
                }
                // manually adding event when there is just a change in the transition target node
                else if (changes.get(0) instanceof mxTerminalChange){
                    xmlUndoManager.add(dataModel.getState());
                }
                else if (changes.get(0) instanceof mxValueChange){
                    xmlUndoManager.add(dataModel.getState());
                }
            }
        }
    };

    /**
     * Add a listener to respond to event changes. State that the graph has
     * changed
     */
    private final transient mxIEventListener changeTracker = new mxIEventListener() {
        @Override
        public void invoke(final Object source, final mxEventObject evt) {
                setModified(true);
        }
    };

    /**
     * The editor corresponds to a pattern. The drawn pattern is reflected
     * in a data structure, maintained by the DataModel.
     */
    private final transient DataModel dataModel;

    /**
     * Getter for the data model underpinning the GUI graph views.
     * @return The reference to the data model.
     */
    public final DataModel getDataModel() {
        return dataModel;
    }

    /**
     * Getter for the history model underpinning the GUI graph views.
     * @return The reference to the history model.
     */
    public final mxUndoManager getUndoModel() {
        return this.undoManager;
    }

    /**
     * The constructor for a graphical editor.
     * @param nTitle The frame title.
     * @param behaveGraph The behaviour graph behaveGraph.
     * @param systemGraph The system graph behaveGraph.
     */
    public BasicGraphEditor(final String nTitle, final mxGraphComponent behaveGraph,
            final mxGraphComponent systemGraph) {
        super();

        // Stores and updates the frame title
        this.appTitle = nTitle;
        this.dataModel = new DataModel();

        // Stores a reference to the graph and creates the command history
        graphComponent = behaveGraph;
        arcGraphComponent = systemGraph;
        graphComponent.getGraph().getSelectionModel().setSingleSelection(true);
        arcGraphComponent.getGraph().getSelectionModel().setSingleSelection(true);

        final mxGraph graph = graphComponent.getGraph();
        final mxGraph sysGraph = arcGraphComponent.getGraph();
        undoManager = createUndoManager();
        xmlUndoManager = new XMLUndoManager();
        copyPasteManager = new CopyPasteManager();
        certificationManager = new CertificationManager();
        xmlUndoManager.add(dataModel.getState());

        // Do not change the scale and translation after files have been loaded
        graph.setResetViewOnRootChange(false);
        sysGraph.setResetViewOnRootChange(false);

        // Updates the modified flag if the graph model changes
        graph.getModel().addListener(mxEvent.CHANGE, changeTracker);
        sysGraph.getModel().addListener(mxEvent.CHANGE, changeTracker);

        // Adds the command history to the model and view
        graph.getModel().addListener(mxEvent.UNDO, undoHandler);
        sysGraph.getModel().addListener(mxEvent.UNDO, undoHandler);
        graph.getView().addListener(mxEvent.UNDO, undoHandler);
        sysGraph.getView().addListener(mxEvent.UNDO, undoHandler);

        // Keeps the selection in sync with the command history
        final mxIEventListener undoHandle = new mxIEventListener() {
            @Override
            public void invoke(final Object source, final mxEventObject evt) {
                    List<mxUndoableChange> changes = ((mxUndoableEdit) evt
                                    .getProperty("edit")).getChanges();
                    graph.setSelectionCells(graph
                                    .getSelectionCellsForChanges(changes));
                    sysGraph.setSelectionCells(sysGraph
                                    .getSelectionCellsForChanges(changes));
            }
        };

        undoManager.addListener(mxEvent.UNDO, undoHandle);
        undoManager.addListener(mxEvent.REDO, undoHandle);

        // Creates the graph outline behaveGraph
        behavGrphOutline = new mxGraphOutline(graphComponent);
        systemGrphOutline = new mxGraphOutline(arcGraphComponent);

        // Creates the library pane that contains the tabs with the palettes
        libraryPane = new JTabbedPane();

        /*
        * Construct the main GUI and its elements within. Two split panes
        * 1) The left hand side - is a vertical split pane
        * 2) The right hand side is a horizonal split pane of two graphs
        */

        /**
        * The tabbed panel of attribute cards for each graph element.
        * Variable is used to switch view when graph elements are selected.
        */

        sidePanel = new JPanel(new CardLayout());
//        sidePanel.setLayout(new BorderLayout());

        ctablePanel = new AttributePanel(attributePanel, this, libraryPane);
        sidePanel.add(attributePanel, "empty");
        execPanel = new ExecutionPanel(this);
        sidePanel.add(execPanel, EXECUTION);

        // Create the code panel to the right side
        codePanel = new JPanel(new CardLayout());
        codetablePanel = new MainDisplayPanel(codePanel, dataModel, this);

        // Creates the left split pane that contains the library with the
        // palettes and the card attributes
//        final JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//                        libraryPane, topPanel);
//        inner.setDividerLocation(100);
//        inner.setResizeWeight(0.3);
//        inner.setOneTouchExpandable(false);
//        inner.setContinuousLayout(false);
//        inner.setDividerSize(2);
//        inner.setBorder(null);

        // Creates the right split pane that contains the inner split pane and
        // the graph behaveGraph on the right side of the window
        final JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePanel,
                        codePanel);
        //outer.setOneTouchExpandable(false);
        outer.setResizeWeight(0.2);
        outer.setOneTouchExpandable(false);
        outer.setContinuousLayout(false);
        outer.setDividerLocation(225);
        outer.setDividerSize(2);
        outer.setBorder(null);

        // Creates the status bar at the bottom of the GUI
        statusBar = createStatusBar();

        // Display some useful information about repaint events
        installRepaintListener();

        // Puts everything together
        setLayout(new BorderLayout());
        add(outer, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        installToolBar();

        // Installs rubberband selection and handling for some special
        // keystrokes such as F2, Control-C, -V, X, A etc.
        installHandlers();
        installListeners();
        updateTitle();
    }

    /**
     * Install the undo manager within the GUI frame.
     * @return The undo manager of mxGraph
     */
    private mxUndoManager createUndoManager() {
        return new mxUndoManager();
    }

    /**
     * Install the rubberband and keyboard handlers.
     */
    protected final void installHandlers() {
        /**
         * Allow dragging of rectangle selection. User creates a box and
         * all elements are selected.
         */
        new mxRubberband(graphComponent);
        new mxRubberband(arcGraphComponent);

        new EditorKeyboardHandler(graphComponent);
        new EditorKeyboardHandler(arcGraphComponent);
    }

    /**
     * Install the tool bar at the top of the GUI.
     */
    protected final void installToolBar() {
        add(new EditorToolBar(this, JToolBar.HORIZONTAL), BorderLayout.NORTH);
    }

    /**
     * Install the status bar at the bottom of the GUI.
     * @return the created status bar label
     */
    protected final JLabel createStatusBar() {
        final JLabel status = new JLabel(mxResources.get("ready"));
        status.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        return status;
    }

    /**
     * Add repaint listeners to the 2 graphs.
     */
    protected final void installRepaintListener() {
        graphComponent.getGraph().addListener(mxEvent.REPAINT,
            new mxIEventListener() {
                @Override
                public void invoke(final Object source, final mxEventObject evt) {
                    final String buffer = (graphComponent.getTripleBuffer() != null) ? ""
                                    : " (unbuffered)";
                    final mxRectangle dirty = (mxRectangle) evt
                                    .getProperty("region");

                    if (dirty == null) {
                            status("Repaint all" + buffer);
                    } else {
                            status("Repaint: x=" + (int) (dirty.getX()) + " y="
                                            + (int) (dirty.getY()) + " w="
                                            + (int) (dirty.getWidth()) + " h="
                                            + (int) (dirty.getHeight()) + buffer);
                    }
                }
            });

        arcGraphComponent.getGraph().addListener(mxEvent.REPAINT,
            new mxIEventListener() {
                @Override
                public void invoke(final Object source, final mxEventObject evt) {
                        final String buffer = (arcGraphComponent.getTripleBuffer() != null) ? ""
                                        : " (unbuffered)";
                        final mxRectangle dirty = (mxRectangle) evt
                                        .getProperty("region");

                        if (dirty == null) {
                                status("Repaint all" + buffer);
                        } else {
                                status("Repaint: x=" + (int) (dirty.getX()) + " y="
                                                + (int) (dirty.getY()) + " w="
                                                + (int) (dirty.getWidth()) + " h="
                                                + (int) (dirty.getHeight()) + buffer);
                        }
                }
            });
	}

    /**
     * Add a new palette to library panel.
     * @param title Title of the new palette
     * @return Return the created palette to be inserted into the panel
	 */
    public final EditorPalette insertPalette(final String title) {
            final EditorPalette palette = new EditorPalette();
            final JScrollPane scrollPane = new JScrollPane(palette);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            libraryPane.add(title, scrollPane);

            // Updates the widths of the palettes if the container size changes
            libraryPane.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(final ComponentEvent event) {
                        final int width = scrollPane.getWidth()
                                 - scrollPane.getVerticalScrollBar().getWidth();
                        palette.setPreferredWidth(width);
                }
            });

            return palette;
    }


    /**
     * When a right click is made on the graph - display the simple mini menu.
     * @param msEvent The mouse click event
     * @param gOutline The graph outline
     * @param gComponent The graph component where the click is
     */
    protected final void showOutlinePopupMenu(final MouseEvent msEvent, final mxGraphOutline gOutline,
            final mxGraphComponent gComponent) {
        final Point pPoint = SwingUtilities.convertPoint(msEvent.getComponent(), msEvent.getPoint(),
                        graphComponent);
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem(
                        mxResources.get("magnifyPage"));
        item.setSelected(gOutline.isFitPage());

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                    gOutline.setFitPage(!gOutline.isFitPage());
                    gOutline.repaint();
            }
        });

        final JCheckBoxMenuItem item2 = new JCheckBoxMenuItem(
                        mxResources.get("showLabels"));
        item2.setSelected(gOutline.isDrawLabels());

        item2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                    gOutline.setDrawLabels(!gOutline.isDrawLabels());
                    gOutline.repaint();
            }
        });

        final JCheckBoxMenuItem item3 = new JCheckBoxMenuItem(
                        mxResources.get("buffering"));
        item3.setSelected(behavGrphOutline.isTripleBuffered());

        item3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                    gOutline.setTripleBuffered(!gOutline.isTripleBuffered());
                    gOutline.repaint();
            }
        });

        final JPopupMenu menu = new JPopupMenu();
        menu.add(item);
        menu.add(item2);
        menu.add(item3);
        menu.show(gComponent, pPoint.x, pPoint.y);
        msEvent.consume();
    }


    /**
     * Show the pop up menu in the passed graph component.
     * @param msEvent Mouse event to determine point location of popup
     * @param gComponent The graph component to display menu in
     */
    protected final void showGraphPopupMenu(final MouseEvent msEvent, final mxGraphComponent gComponent) {
        final Point pPoint = SwingUtilities.convertPoint(msEvent.getComponent(), msEvent.getPoint(),
                        gComponent);
        final EditorPopupMenu menu = new EditorPopupMenu(BasicGraphEditor.this);
        menu.show(gComponent, pPoint.x, pPoint.y);
        msEvent.consume();
    }

    /**
     * When the mouse location changes, update the status label.
     * @param msEvent Mouse event with new location
     */
    protected final void mouseLocationChanged(final MouseEvent msEvent) {
        status(msEvent.getX() + ", " + msEvent.getY());
    }

    /**
     * Install the basic event listeners in the base GUI.
     */
    protected final void installListeners() {

        // Installs the popup menu in the outline
        behavGrphOutline.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent msEvent) {
                mouseReleased(msEvent);
            }

            @Override
            public void mouseReleased(final MouseEvent msEvent) {
                if (msEvent.isPopupTrigger()) {
                        showOutlinePopupMenu(msEvent, behavGrphOutline, graphComponent);
                }
            }
        });

        // Installs the popup menu in the outline
        systemGrphOutline.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent msEvent) {
                mouseReleased(msEvent);
            }

            @Override
            public void mouseReleased(final MouseEvent msEvent) {
                if (msEvent.isPopupTrigger()) {
                    showOutlinePopupMenu(msEvent, systemGrphOutline, arcGraphComponent);
                }
            }
        });

        // Installs the popup menu in the graph component
        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent msEvent) {
                mouseReleased(msEvent);
            }

            @Override
            public void mouseReleased(final MouseEvent msEvent) {
                if (msEvent.isPopupTrigger()) {
                    showGraphPopupMenu(msEvent, graphComponent);
                }
            }
        });

        // Installs the popup menu in the graph component
        arcGraphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent msEvent) {
                mouseReleased(msEvent);
            }
            @Override
            public void mouseReleased(final MouseEvent msEvent) {
                if (msEvent.isPopupTrigger()) {
                    showGraphPopupMenu(msEvent, arcGraphComponent);
                }
            }
        });

        // Installs a mouse motion listener to display the mouse location
        graphComponent.getGraphControl().addMouseMotionListener(
            new MouseMotionListener() {
                /*
                 * (non-Javadoc)
                 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
                 */
                @Override
                public void mouseDragged(final MouseEvent msEvent) {
                    mouseLocationChanged(msEvent);
                }

                /*
                 * (non-Javadoc)
                 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
                 */
                @Override
                public void mouseMoved(final MouseEvent msEvent) {
                    mouseDragged(msEvent);
                }

        });
        // Installs a mouse motion listener to display the mouse location
        arcGraphComponent.getGraphControl().addMouseMotionListener(
            new MouseMotionListener() {
                /*
                 * (non-Javadoc)
                 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
                 */
                @Override
                public void mouseDragged(final MouseEvent msEvent) {
                    mouseLocationChanged(msEvent);
                }

                /*
                 * (non-Javadoc)
                 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
                 */
                @Override
                public void mouseMoved(final MouseEvent msEvent) {
                    mouseDragged(msEvent);
                }

        });
    }

    /**
     * Set the current file related to the pattern data being edited.
     * @param file The file to set as the file store for the GUI data
     */
    public final void setCurrentFile(final File file) {
        final File oldValue = currentFile;
        currentFile = file;

        firePropertyChange("currentFile", oldValue, file);

        if (oldValue != file) {
            updateTitle();
        }
    }

    /**
     * Find out where the file storing the data is.
     * @return The current pattern's File reference
     */
    public final File getCurrentFile() {
        return currentFile;
    }

    /**
     * Change the modified status of the underlying data graph.
     * @param mod The new modified status.
     */
    public final void setModified(final boolean mod) {
        final boolean oldValue = this.modified;
        this.modified = mod;

        firePropertyChange("modified", oldValue, modified);

        if (oldValue != modified) {
                updateTitle();
        }
    }

    /**
     * @return whether or not the current graph has been modified
     */
    public final boolean isModified() {
        return modified;
    }


    /**
     *
     * @return the undo manager for the graph
     */
    public final mxUndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * Bind an editor action to a named reference.
     * @param name The name of the action
     * @param action The action code
     * @return a new Action bound to the specified string name
     */
    public final Action bind(final String name, final Action action) {
        return bind(name, action, null);
    }

    /**
     * Bind an action to a named reference and icon.
     * @param name The name of the reference
     * @param action The code of the action
     * @param iconUrl the reference of the icon image file
     * @return a new Action bound to the specified string name and icon
     */
    @SuppressWarnings("serial")
    public final Action bind(final String name, final Action action, final String iconUrl) {
        final AbstractAction newAction = new AbstractAction(name, (iconUrl != null) ? new ImageIcon(
                        BasicGraphEditor.class.getResource(iconUrl)) : null) {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                    action.actionPerformed(new ActionEvent(actionEvent.getSource(), actionEvent
                                    .getID(), actionEvent.getActionCommand()));
            }
        };

        newAction.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.SHORT_DESCRIPTION));

        return newAction;
    }

    /**
     * Change the GUI status message.
     * @param msg The new status message
     */
    public final  void status(final String msg) {
        statusBar.setText(msg);
    }

    /**
     * Change the Frame title - used when the filename is changed.
     */
    public final void updateTitle() {
        final JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

        if (frame != null) {
            final StringBuffer title = new StringBuffer((currentFile != null) ? currentFile
                            .getAbsolutePath() : mxResources.get("newDiagram"));

            if (modified) {
                title.append('*');
            }
            title.append('-').append(appTitle);
            frame.setTitle(title.toString());
        }
    }

    /**
     * Display the about dialogue.
     */
    public final void about() {
        final JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

        if (frame != null) {
            final EditorAboutFrame about = new EditorAboutFrame(frame);
            about.setModal(true);

            // Centers inside the application frame
            final int xAxis = frame.getX() + (frame.getWidth() - about.getWidth()) / 2;
            final int yAxis = frame.getY() + (frame.getHeight() - about.getHeight()) / 2;
            about.setLocation(xAxis, yAxis);

            // Shows the modal dialog and waits
            about.setVisible(true);
        }
    }

    /**
     * Exit the GUI and shut down.
     */
    public final void exit() {
        final JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

        if (frame != null) {
                frame.dispose();
        }
    }

    /**
     * For the UI window look a feel, change to the given description.
     * @param clazz The description of the new look and feel.
     */
    public final void setLookAndFeel(final String clazz) {
        final JFrame frame = (JFrame) SwingUtilities.windowForComponent(this);

        if (frame != null) {
            try {
                UIManager.setLookAndFeel(clazz);
                SwingUtilities.updateComponentTreeUI(frame);

                // Needs to assign the key bindings again
                new EditorKeyboardHandler(graphComponent);
                new EditorKeyboardHandler(arcGraphComponent);
            } catch (ClassNotFoundException ex) {
                ServiceLogger.LOG.error("Error creating look and feel class", ex);
            } catch (InstantiationException ex) {
                ServiceLogger.LOG.error("Error instantiating configuration", ex);
            } catch (IllegalAccessException ex) {
                ServiceLogger.LOG.error("Access error", ex);
            } catch (UnsupportedLookAndFeelException e1) {
                ServiceLogger.LOG.error("Error changing look and feel; invalid description of new look and feel " + e1.getMessage());
            }
        }
    }

    /**
     * Create the main frame for the GUI.
     * @param menuBar The menu bar with the drop down menus
     * @return The created JFrame
     */
    public final JFrame createFrame(final JMenuBar menuBar) {
        final JFrame frame = new JFrame();
        frame.getContentPane().add(this);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                if (workspaceLocation != null){
                    int check = JOptionPane.showConfirmDialog(frame,
                            "All unsaved data including patterns and previous test reports would be lost. Do you want to continue ?",
                            "Confirm exit", JOptionPane.YES_NO_OPTION);
                    if (check == JOptionPane.YES_OPTION){
                        System.exit(0);
                    }
                }
                else {
                    System.exit(0);
                }
            }
        });
        frame.setJMenuBar(menuBar);
        frame.setSize(870, 640);

        // Updates the frame title
        updateTitle();

        return frame;
    }

    /**
     * Creates an action that executes the specified layout.
     *
     * @param key Key to be used for getting the label from mxResources and also
     * to create the layout instance for the commercial graph editor example.
     * @param animate use animation?
     * @return an action that executes the specified layout
     */
    @SuppressWarnings("serial")
    public final Action graphLayout(final String key, final boolean animate) {
        final mxIGraphLayout layout = createLayout(key, animate);

        if (layout != null) {
            return new AbstractAction(mxResources.get(key)) {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                    final mxGraph graph = graphComponent.getGraph();
                    Object cell = graph.getSelectionCell();

                    if (cell == null || graph.getModel().getChildCount(cell) == 0) {
                            cell = graph.getDefaultParent();
                    }

                    graph.getModel().beginUpdate();
                    try {
                        final long timeZero = System.currentTimeMillis();
                        layout.execute(cell);
                        status("Layout: " + (System.currentTimeMillis() - timeZero) + " ms");
                    } finally {
                        final mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

                        morph.addListener(mxEvent.DONE, new mxIEventListener() {
                                @Override
                                public void invoke(final Object sender, final mxEventObject evt) {
                                        graph.getModel().endUpdate();
                                }
                        });
                        morph.startAnimation();
                    }
                }
            };
        } else {
            return new AbstractAction(mxResources.get(key)) {
                @Override
                public void actionPerformed(final ActionEvent actionEvent) {
                        JOptionPane.showMessageDialog(graphComponent,
                                        mxResources.get("noLayout"));
                }
            };
        }
    }

    /**
     * Creates a layout instance for the given identifier.
     * Takes the graph elements and lays them out according to
     * a graph structure on the page.
     *
     * @param ident Layout identifier.
     * @param animate animate?
     * @return The new graph layout.
     */
    protected final mxIGraphLayout createLayout(final String ident, final boolean animate) {
        mxIGraphLayout layout = null;

        if (ident != null) {
            final mxGraph graph = graphComponent.getGraph();

            switch (ident) {
                case "verticalHierarchical":
                    layout = new mxHierarchicalLayout(graph);
                    break;
                case "horizontalHierarchical":
                    layout = new mxHierarchicalLayout(graph, JLabel.WEST);
                    break;
                case "verticalTree":
                    layout = new mxCompactTreeLayout(graph, false);
                    break;
                case "horizontalTree":
                    layout = new mxCompactTreeLayout(graph, true);
                    break;
                case "organicLayout":
                    layout = new mxOrganicLayout(graph);
                    break;
                case "verticalStack":
                    layout = new mxStackLayout(graph, false) {
                        /**
                         * Overrides the empty implementation to return the size of the
                         * graph control.
                         */
                        @Override
                        public mxRectangle getContainerSize() {
                            return graphComponent.getLayoutAreaSize();
                        }
                    };
                    break;
                case "horizontalStack":
                    layout = new mxStackLayout(graph, true) {
                        /**
                         * Overrides the empty implementation to return the size of the
                         * graph control.
                         */
                        @Override
                        public mxRectangle getContainerSize() {
                            return graphComponent.getLayoutAreaSize();
                        }
                    };
                    break;
                case "circleLayout":
                    layout = new mxCircleLayout(graph);
                    break;
                default:
                    layout = new mxCompactTreeLayout(graph, false);
                    break;
            }
        }
        return layout;
    }

    /**
     * Change the data table view panel to contain the attributes of
     * the given graph element. When an element is clicked we must update
     * the info panel. Part of the panels involve JTables and these are
     * updated here.
     *
     * @param uiID The user interface id of the element in the drawn graph
     */
    public final void updateTableView(final String uiID) {
        /* two null arguments are passed if a new file is opened, this clears the table */
        if (uiID == null){
            ((CardLayout) sidePanel.getLayout()).show(sidePanel, "Empty");
            ((CardLayout) getFormPanel().getLayout()).show(getFormPanel(), "EmptyPanel");
            return;
        }

        if (uiID.equalsIgnoreCase(AttributePanel.EXECUTION)) {
            ((CardLayout) sidePanel.getLayout()).show(sidePanel, AttributePanel.EXECUTION);
            return;
        }

        // Get the attribut information
        AbstractGraphElement transition = dataModel.getNode(uiID);

        if (transition == null) {
            transition = dataModel.getTransition(uiID);
        }

        /**
         * Figure out what we are switching to and then make the
         * correct view update
         */
        if (transition != null) {
            switch(transition.getType()) {
                case "start":
                    if (ctablePanel.getNodeForm() != null) {
                        ctablePanel.getNodeForm().clearData();
                        ctablePanel.getNodeForm().setData((GraphNode) transition);
                    }
                    break;
                case "triggerstart":
                    if (ctablePanel.getNodeForm() != null) {
                        ctablePanel.getNodeForm().clearData();
                        ctablePanel.getNodeForm().setData((GraphNode) transition);
                    }
                    break;
                case "end":
                    if (ctablePanel.getEndForm() != null) {
                        ctablePanel.getEndForm().clearData();
                        ctablePanel.getEndForm().setData((GraphNode) transition);
                    }
                    break;
                case "dataloop":
                    if (ctablePanel.getDataLoopForm()!= null) {
                        ctablePanel.getDataLoopForm().clearData();
                        ctablePanel.getDataLoopForm().setData((GraphNode) transition);
                    }
                    break;
                case "guard":
                    if (ctablePanel.getGuardForm() != null) {
                        ctablePanel.getGuardForm().clearData();
                        ctablePanel.getGuardForm().setData((Guard) transition);
                    }
                    break;
                case "component":
                    if (ctablePanel.getComponentForm() != null) {
                        ctablePanel.getComponentForm().clearData();
                        ctablePanel.getComponentForm().setData((ArchitectureNode) transition);
                    }
                    break;
                case "message":
                    if (ctablePanel.getMessageComponent() != null) {
                        ctablePanel.getMessageComponent().clearData();
                        ctablePanel.getMessageComponent().setData((Message) transition);
                    }
                    break;
                }
        }
    }

     /**
     * Set the rules for drawing in the the 2 graphs. For example, a start
     * node cannot have an incoming conncection.
     */
    public final void setRules() {
//        mxMultiplicity[] multiplicities = new mxMultiplicity[4];
//        // Source node does not want any incoming connections
//
//        multiplicities[0] = new mxMultiplicity(false, "start", null, null, 0,
//                        "0", null, "Start Node Must Have No Incoming Edge", null, true);
//        // Trigger start node does not want any incoming connections
//        multiplicities[1] = new mxMultiplicity(false, "triggerstart", null, null, 0,
//                        "0", null, "Trigger Start Node Must Have No Incoming Edge", null, true);
//        // Trigger start has 1 outgoing connection to either a end or normal node
//        multiplicities[2] = new mxMultiplicity(true, "triggerstart", null, null, 1,
//                        "1", Arrays.asList(new String[] {"normal", "end" }),
//                        "triggerstart Must Have 1 outgoing conection only",
//                        "triggerstart Must Connect to End or Normal", true);
//        // Trigger node has 1 outgoing connection to either a end or normal node
//        multiplicities[3] = new mxMultiplicity(true, "trigger", null, null, 1,
//                        "1", Arrays.asList(new String[] {"normal", "end" }),
//                        "trigger Must Have 1 outgoing conection only",
//                        "trigger Must Connect to End or Normal", true);
//        getBehaviourGraph().getGraph().setMultiplicities(multiplicities);

        // Set the rules about the architecture specification
//        mxMultiplicity[] multiplicities2 = new mxMultiplicity[4];
//        // A client cannot have any connections
//        multiplicities2[0] = new mxMultiplicity(false, "client", null, null, 0,
//                        "0", null, "A Client cannot be connected", null, true);
//        multiplicities2[1] = new mxMultiplicity(true, "client", null, null, 0,
//                        "0", null, "A Client cannot be connected", null, true);
//        // An interface cannot have any connections
//        multiplicities2[2] = new mxMultiplicity(false, "interface", null, null, 0,
//                        "0", null, "An interface cannot be connected", null, true);
//        multiplicities2[3] = new mxMultiplicity(true, "interface", null, null, 0,
//                        "0", null, "An interface cannot be connected", null, true);
//
//        getSystemGraph().getGraph().setMultiplicities(multiplicities2);
    }
}