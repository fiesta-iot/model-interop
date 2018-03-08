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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphActions;
import com.mxgraph.view.mxGraph;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import org.xml.sax.SAXException;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.AbstractGraphElement;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ConstantData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GuardData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Message;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ObjectDeepCloner;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.ExecutionPanel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.InterfaceData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.AttributePanel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.EditorPopupMenu;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.EditorToolBar;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.GUIdentifier;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.MainDisplayPanel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.PatternCheckThread;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidPatternException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.PatternValidation;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

/**
 * The set of GUI actions e.g. save, open, etc. that correspond to operations
 * selected from the UI.
 */
public final class EditorActions {

    /**
     * Dialogue string about pattern verification.
     */
    private static final String VER_DIALOGUE = "Pattern verification";

    /**
     * Utility class, therefore use a private constructor.
     */
    private EditorActions() {
        // empty implementation
    }

    /**
     * Retrieve the editor where the event originated.
     * @param actionEvent The UI event
     * @return Returns the graph for the given action event.
     */
    public static BasicGraphEditor getEditor(final ActionEvent actionEvent) {
        if (actionEvent == null){
            return null;
        }
        
        if (actionEvent.getSource() instanceof Component) {
            Component component = ((Component) actionEvent.getSource()).getParent();
            if (component != null
                            && (component instanceof EditorPopupMenu)) {

                return ((EditorPopupMenu) component).getEditor();
            }

            while (component != null
                            && !(component instanceof BasicGraphEditor)) {
                    component = component.getParent();
            }

            return (BasicGraphEditor) component;
        }

        return null;
    }

    /**
     * A copy action for states and system components
     */
    public static class CopyComponentAction extends AbstractAction {

        /**
         * Editor for the action.
         */
        private final transient BasicGraphEditor editor;

        /**
         * The action method constructor.
         * @param edtr The editor context.
         */
        public CopyComponentAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
         * overriding the actionPerformed method to copy the chosen component
         * @param actionEvent the actual event
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent) {
            BasicGraphEditor editorReference = editor;
            if (editorReference == null){
                editorReference = EditorActions.getEditor(actionEvent);
            }
            final Component component = (Component) actionEvent.getSource();
            final mxGraphComponent msGraphComp = (mxGraphComponent) component;
            final Object[] selectionCells = msGraphComp.getGraph().getSelectionCells();
            final String nodeID = ((mxCell) selectionCells[0]).getId();
            AbstractGraphElement node = editorReference.getDataModel().getNode(GUIdentifier.getGUIdentifier(nodeID, msGraphComp));
            if (node instanceof GraphNode){
                HashMap<String, Object> data;
                GraphNode graphNode = (GraphNode) node;
                if (graphNode.getType().equalsIgnoreCase("end")){
                    // store the information of the end node
                    data = new HashMap<>();
                    data.put("success", graphNode.getEndStateSuccess());
                    data.put("report", graphNode.getEndStateReport());
                    editorReference.getCopyPasteManager().setData(data);
                }
                else if (graphNode.getType().equalsIgnoreCase("start") || (graphNode.getType().equalsIgnoreCase("triggerstart"))){
                    // store the information of the start node
                    data = new HashMap<>();
                    data.put("constantData", ObjectDeepCloner.deepCopy(graphNode.getConstantData()));
                    editorReference.getCopyPasteManager().setData(data);
                }
                editorReference.getCopyPasteManager().setLastType(graphNode.getType());
            }
            else if (node instanceof ArchitectureNode) {
                ArchitectureNode archNode = (ArchitectureNode) node;
                // store the information of the component node
                HashMap<String, Object> data = new HashMap<>();
                data.put("address", archNode.getAddress());
                data.put("interfaces", ObjectDeepCloner.deepCopy(archNode.getData()));
                editorReference.getCopyPasteManager().setData(data);
                if (archNode.getData().size() > 0){
                    editorReference.getCopyPasteManager().setLastType(XMLStateMachine.INTERFACE_LABEL);
                }
                else {
                    editorReference.getCopyPasteManager().setLastType(DataModel.CLIENT);
                }
            }
            else {
                // case of copying a transition
                AbstractGraphElement transition = editorReference.getDataModel().getTransition(nodeID);
                HashMap<String, Object> data = new HashMap<>();
                if (transition instanceof Guard){
                    // copy the information of the guard mesage
                    data.put("transitionType", "guard");
                    data.put("guards", ObjectDeepCloner.deepCopy(((Guard) transition).getData()));
                    editorReference.getCopyPasteManager().setData(data);
                }
                else if (transition instanceof Message){
                    // copy the information of the message transition
                    data.put("transitionType", "message");
                    data.put("url", ((Message) transition).getEndpoint());
                    data.put("path", ((Message) transition).getPath());
                    data.put("method", ((Message) transition).getHTTPMethod());
                    data.put("dataType", ((Message) transition).getDataType());
                    data.put("body", ((Message) transition).getHTTPBody());
                    data.put("headers", ObjectDeepCloner.deepCopy(((Message) transition).getConstantData()));
                    editorReference.getCopyPasteManager().setData(data);
                }
                return;
            }

            TransferHandler.getCopyAction().actionPerformed(actionEvent);
        }
    }

    /**
     * A paste action for states and system components
     */
    public static class PasteComponentAction extends AbstractAction {

        /**
         * Editor for the action.
         */
        private final transient BasicGraphEditor editor;

        /**
         * The action method constructor.
         * @param edtr The editor context.
         */
        public PasteComponentAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
         * overriding the actionPerformed method to paste the chosen component
         * @param actionEvent the actual event
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent) {
            BasicGraphEditor editorReference = editor;
            if (editorReference == null) {
                editorReference = EditorActions.getEditor(actionEvent);
            }

            // check for a copied transition
            if (editorReference.getCopyPasteManager().getData() != null &&
                    editorReference.getCopyPasteManager().getData().containsKey("transitionType")){
                Object[] cells = editorReference.getBehaviourGraph().getGraph().getSelectionCells();
                if (cells.length != 1){
                    return;
                }
                final String ident = ((mxCell) cells[0]).getId();
                AbstractGraphElement transition = editorReference.getDataModel().getTransition(ident);
                if (transition instanceof Guard){
                    if (!editorReference.getCopyPasteManager().getData().get("transitionType").toString().equalsIgnoreCase("guard")){
                        // a dialog to notify an imposible pasting of message data to guard transition
                        JOptionPane.showMessageDialog(editor,
                                "You cannot paste the data of a message transition into a guard transition.",
                                "Pasting transition data error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Map<String, Object> data = editorReference.getCopyPasteManager().getData();
                    List<GuardData> newData = (List<GuardData>) ObjectDeepCloner.deepCopy(data.get("guards"));
                    ((Guard) transition).setData(newData);
                    editorReference.updateTableView(ident);
                }
                else if (transition instanceof Message){
                    if (!editorReference.getCopyPasteManager().getData().get("transitionType").toString().equalsIgnoreCase("message")){
                        // a dialog to notify an imposible pasting of guard data to message transition
                        JOptionPane.showMessageDialog(editor,
                                "You cannot paste the data of a guard transition into a message transition.",
                                "Pasting transition data error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Map<String, Object> data = editorReference.getCopyPasteManager().getData();
                    ((Message) transition).updateMessage((String) data.get("url"), (String) data.get("path"), (String) data.get("method"),
                            (String) data.get("dataType"), (String) data.get("body"));
                    List<ConstantData> headers = (List<ConstantData>) ObjectDeepCloner.deepCopy(data.get("headers"));
                    ((Message) transition).setConstantData(headers);
                    editorReference.updateTableView(ident);
                }

                return;
            }

            // check for a copied ordinary state  - normal, loop, trigger
            if (editorReference.getCopyPasteManager().getLastGUIid() == null ){
                TransferHandler.getPasteAction().actionPerformed(actionEvent);
                return;
            }

            TransferHandler.getPasteAction().actionPerformed(actionEvent);
            String guiId = editorReference.getCopyPasteManager().getLastGUIid();
            AbstractGraphElement node = editorReference.getDataModel().getNode(guiId);
            if (node instanceof GraphNode) {
                GraphNode graphNode = (GraphNode) node;
                if (graphNode.getType().equalsIgnoreCase("end")) {
                    // paste the information associated with the copied end node
                    Map<String, Object> data = editorReference.getCopyPasteManager().getData();
                    graphNode.addEndStateData((Boolean) data.get("success"), (String) data.get("report"));
                }
                else if (graphNode.getType().equalsIgnoreCase("dataloop")) {
                    // paste the information associated with the copied end node
                    Map<String, Object> data = editorReference.getCopyPasteManager().getData();
                    graphNode.addEventStateData((String) data.get("event"));
                }
                else if (graphNode.getType().equalsIgnoreCase("start") || (graphNode.getType().equalsIgnoreCase("triggerstart"))){
                    // paste the information associated with the copied start node
                    Map<String, Object> data = editorReference.getCopyPasteManager().getData();
                    List<ConstantData> constantDataList = (List<ConstantData>) ObjectDeepCloner.deepCopy(data.get("constantData"));
                    graphNode.setConstantData(constantDataList);
                }
            }
            else if (node instanceof ArchitectureNode){
                // paste the information associated with the node component
                ArchitectureNode archNode = (ArchitectureNode) node;
                Map<String, Object> data = editorReference.getCopyPasteManager().getData();
                archNode.setData(archNode.getLabel(), (String) data.get("address"));
                List<InterfaceData> interfaces = (List<InterfaceData>) ObjectDeepCloner.deepCopy(data.get("interfaces"));
                archNode.setInterfaceData(interfaces);
            }
            editorReference.updateTableView(guiId);
        }
    }

    /**
     * Close UI when close/exit selected.
     */
    public static class ExitAction extends AbstractAction {
        /**
         * Editor for the action.
         */
        private final transient BasicGraphEditor editor;

        /**
         * The action method constructor.
         * @param edtr The editor context.
         */
        public ExitAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
         * When the action is performed do the following. Close the editor.
         * @param actionEvent Event info (not used).
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent) {
                if (editor != null) {
                        editor.exit();
                }
        }
    }

    /**
     * Perform the execution action. This is the execution of the pattern and
     * corresponding testing framework.
     */
    public static class ExecuteAction extends AbstractAction {

        /**
         * a reference to the editor
         */
        private final transient BasicGraphEditor editorReference;

        /**
         * boolean to show, whether to ask the user for the run mode or directly go with execution mode
         */
        private final boolean askUser;
        
        /**
         * a constructor for the editor which also sets the run mode of the tests
         * @param editor the editor reference
         * @param askUser whether to ask the user for the run mode or directly go with execution mode
         */
        public ExecuteAction(BasicGraphEditor editor, boolean askUser){
            this.editorReference = editor;
            this.askUser = askUser;
        }
        
        /**
         * A constructor to set the editor reference
         * @param editor the reference to the editor
         */
        public ExecuteAction(BasicGraphEditor editor){
            this.editorReference = editor;
            askUser = true;
        }

        /**
         * an empty constructor in case a reference to the editor is not needed
         */
        public ExecuteAction(){
            this.editorReference = null;
            askUser = true;
        }

        /**
         * The method to start the execution of the pattern.
         * @param actionEvent The received UI event.
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent) {
            final BasicGraphEditor editor;
            BasicGraphEditor test = getEditor(actionEvent);
            if (test == null){
                editor = this.editorReference;
            }
            else {
                editor = test;
            }

            if (editor.isRunning()){
                JOptionPane.showMessageDialog(editor,
                        "There is currently another test running. Either let the test finish or stop it before starting a new one.",
                        "Multiple test executions", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // check the running mode of the test
            final boolean debugMode;
            if (askUser){
                String[] choices = {"Execution mode", "Step-by-step mode"};
                String mode = (String) JOptionPane.showInputDialog(null,
                        "Which mode do you want to use to run the interoperability test?",
                        "Test running mode", JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
                if (mode == null){
                    return;
                }
                debugMode = mode.equals("Step-by-step mode");
            }
            else {
                debugMode = false;
            }
            
            editor.getCodePanel().getTestingPanel().clearTestingPanel();
            final CardLayout cardLayout = (CardLayout) editor.getMainArea().getLayout();
            cardLayout.show(editor.getMainArea(), MainDisplayPanel.REPORTPANEL);

            try {
                final PatternCheckThread checkThread = new PatternCheckThread(editor.getDataModel().getGraphXML(),
                        editor.getCodePanel().getTestingPanel().getInteroperabilityReport(), editor, debugMode);

                // adjusting the listeners of the tool bar buttons
                EditorToolBar toolBar = (EditorToolBar) ((BorderLayout) editor.getLayout()).getLayoutComponent(BorderLayout.NORTH);
                JButton stopButton = (JButton) toolBar.getComponentAtIndex(toolBar.getStopButtonIndex());
                ActionListener[] listeners = stopButton.getActionListeners();
                if (listeners != null && listeners.length >= 1){
                    stopButton.removeActionListener(listeners[listeners.length-1]);
                }
                stopButton.addActionListener((ActionEvent e) -> {
                    try {
                        if (checkThread.getArch().getStateMachine().isFinished()) {
                            JOptionPane.showMessageDialog(editor, "The test has either finished or has been stopped.",
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        checkThread.getArch().getStateMachine().stop();
                    } catch (NullPointerException ex) {}
                });

                if (debugMode){
                    JButton nextButton = (JButton) toolBar.getComponentAtIndex(toolBar.getNextButtonIndex());
                    ActionListener[] actionListeners = nextButton.getActionListeners();
                    if (actionListeners != null && actionListeners.length >= 1) {
                        nextButton.removeActionListener(actionListeners[actionListeners.length-1]);
                    }
                    nextButton.addActionListener((ActionEvent e) -> {
                        try {
                            if (checkThread.getArch().getStateMachine().isFinished()) {
                                JOptionPane.showMessageDialog(editor, "The test has either finished or has been stopped.",
                                        "Warning", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            checkThread.getArch().getStateMachine().next();
                        } catch (NullPointerException ex) {}
                    });
                }

                editor.setRunning(true);
                editor.getBehaviourGraph().getGraph().clearSelection();
                editor.getSystemGraph().getGraph().clearSelection();
                editor.updateTableView(AttributePanel.EXECUTION);
                checkThread.start();
            } catch (HeadlessException ex) {
                JOptionPane.showMessageDialog(editor,
                        "Pattern is not valid: " + ex.getMessage(),
                        VER_DIALOGUE,
                        JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    /**
     * Empty action for the initialization of the stop button
     */
    public static class EmptyAction extends AbstractAction {
        /**
         * empty action, hence empty method
         * @param actionEvent
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent){
            // skip
        }
    }

    /**
     * Perform the showing last reports action.
     */
    public static class ReportsAction extends AbstractAction {

        /**
         * The editor context for action to be associated with.
         */
        private final transient BasicGraphEditor editor;

        /**
         * Create instance of the XML action with the editor context.
         * @param edtr The GUI editor context for the action.
         */
        public ReportsAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
         * The method to switch to a panel with all previous reports.
         * @param actionEvent The received UI event.
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent) {
            final CardLayout cardLayout = (CardLayout) editor.getMainArea().getLayout();
            cardLayout.show(editor.getMainArea(), MainDisplayPanel.PREVIOUSREPORTS);
            editor.getCodePanel().getReportsPanel().resetTabbedPane();

            for(Component comp: editor.getAttributePanel().getComponents()){
                if (comp.isVisible() && comp instanceof ExecutionPanel){
                    editor.updateTableView(null);
                    editor.getBehaviourGraph().getGraph().clearSelection();
                    break;
                }
            }
        }
    }

    /**
     * Interface selection to verify the specification of the pattern.
     */
    public static class VerifyAction extends AbstractAction {

        /**
         * The editor context for action to be associated with.
         */
        private transient BasicGraphEditor editor;

        /**
         * Create instance of the verify action with the editor context.
         * @param edtr The GUI editor context for the action.
         */
        public VerifyAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
         * Method to verify if the graphical specification is valid.
         * @param actionEvent The UI action.
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent) {
            if (editor == null) {
                editor = getEditor(actionEvent);
            }
            try {
                final String xml = editor.getDataModel().getGraphXML();
                if (PatternValidation.validatePattern(xml)) {
                    JOptionPane.showMessageDialog(editor,
                        "Pattern is verified as correct.",
                        VER_DIALOGUE,
                        JOptionPane.PLAIN_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(editor,
                        "Pattern is not valid",
                        VER_DIALOGUE,
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (SAXException ex) {
                JOptionPane.showMessageDialog(editor,
                        "Pattern is not valid: " + ex.getMessage(),
                        VER_DIALOGUE,
                        JOptionPane.ERROR_MESSAGE);
            }
            catch (InvalidPatternException ex){
                JOptionPane.showMessageDialog(editor,
                        "Pattern is not valid: There are more than one start or triggerstart nodes in your pattern.",
                        VER_DIALOGUE,
                        JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    /**
     * GUI action to open up the XML panel and view it in the editor.
     */
    public static class XMLAction extends AbstractAction {

        /**
         * The editor context for action to be associated with.
         */
        private transient BasicGraphEditor editor;

        /**
         * Create instance of the XML action with the editor context.
         * @param edtr The GUI editor context for the action.
         */
        public XMLAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
         * XML action selected.
         * @param actionEvent The UI event selecting the XML panel to be shown.
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent) {
            if (editor == null) {
                editor = getEditor(actionEvent);
            }
            editor.getCodePanel().getXMLPanel().displayXMLSpecification();
            final CardLayout cardLayout = (CardLayout) editor.getMainArea().getLayout();
            cardLayout.show(editor.getMainArea(), MainDisplayPanel.CODEPANEL);

            final CardLayout sideLayout = (CardLayout) editor.getAttributePanel().getLayout();
            sideLayout.show(editor.getAttributePanel(), "empty");

            for(Component comp: editor.getAttributePanel().getComponents()){
                if (comp.isVisible() && comp instanceof ExecutionPanel){
                    editor.updateTableView(null);
                    editor.getBehaviourGraph().getGraph().clearSelection();
                    break;
                }
            }
        }
    }

    /**
     * Switch to the Graph view in the user interface.
     */
    public static class GraphAction extends AbstractAction {

        /**
         * The editor context for action to be associated with.
         */
        private transient BasicGraphEditor editor;

        /**
         * Create an instance of the graph selected action.
         *
         * @param edtr The UI editor context for the action.
         */
        public GraphAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
         * The show graph action has been selected.
         *
         * @param actionEvent The UI event of the selection.
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent) {
            if (editor == null) {
                editor = getEditor(actionEvent);
            }
            final CardLayout cardLayout = (CardLayout) editor.getMainArea().getLayout();
            cardLayout.show(editor.getMainArea(), MainDisplayPanel.GRAPHPANEL);

            final CardLayout sideLayout = (CardLayout) editor.getAttributePanel().getLayout();
            sideLayout.show(editor.getAttributePanel(), "empty");

            for(Component comp: editor.getAttributePanel().getComponents()){
                if (comp.isVisible() &&  comp instanceof ExecutionPanel){
                    editor.updateTableView(null);
                    editor.getBehaviourGraph().getGraph().clearSelection();
                    break;
                }
            }
        }
    }

    /**
     * Switch to the current Test view in the user interface.
     */
    public static class TestViewAction extends AbstractAction {

        /**
         * The editor context for action to be associated with.
         */
        private final transient BasicGraphEditor editor;

        /**
         * constructor for the TestAction
         *
         * @param edtr The UI editor context for the action.
         */
        public TestViewAction(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
         * the action to be performed
         * @param ae the actual action event
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            final BasicGraphEditor editorReference;
            BasicGraphEditor test = getEditor(ae);
            if (test == null){
                editorReference = this.editor;
            }
            else {
                editorReference = test;
            }
            final CardLayout cardLayout = (CardLayout) editorReference.getMainArea().getLayout();
            cardLayout.show(editorReference.getMainArea(), MainDisplayPanel.REPORTPANEL);

            editor.updateTableView(AttributePanel.EXECUTION);
        }

    }

    /**
     * An action to clear all memory cache used in multiple test execution, that is all data
     * captured from previous tests is deleted
     */
    public static class ClearMemoryMapAction extends AbstractAction {
        
         /**
         * The editor context for action to be associated with.
         */
        private final transient BasicGraphEditor editor;

        /**
         * constructor for this action
         *
         * @param edtr The UI editor context for the action.
         */
        public ClearMemoryMapAction (final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }
        
         
        /**
         * the action to be performed
         * @param ae the actual action event
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            final BasicGraphEditor editorReference;
            BasicGraphEditor test = getEditor(ae);
            if (test == null){
                editorReference = this.editor;
            }
            else {
                editorReference = test;
            }
            
            editorReference.getCollectionsBrowserPanel().getMultiTestsManager().resetMaps();
            
            JOptionPane.showMessageDialog(editorReference, "All memory maps are now reset and captured data is deleted.", 
                    "Memory maps cleared", JOptionPane.INFORMATION_MESSAGE);
        }

    }
    
    /**
     * The delete action - to delete elements from the graph view and data
     * model.
     */
    @SuppressWarnings("serial")
    public static class Delete extends AbstractAction {

        /**
         * The editor context for action to be associated with.
         */
        private BasicGraphEditor editor;

        /**
         * Create an instance of the delete action in this UI context.
         *
         * @param edtr The UI editor context.
         */
        public Delete(final BasicGraphEditor edtr) {
            super();
            this.editor = edtr;
        }

        /**
         * The delete action has been selected via mouse press or keyboard
         * input.
         *
         * @param actionEvent The UI event.
         */
        @Override
        public final void actionPerformed(final ActionEvent actionEvent) {
            if (editor == null) {
                editor = getEditor(actionEvent);
            }
            final DataModel dModel = editor.getDataModel();

            final mxGraphComponent graphComponent = editor.getBehaviourGraph();
            final mxGraphComponent arcgraphComponent = editor.getSystemGraph();
            final mxGraph graph = graphComponent.getGraph();
            final mxGraph graph2 = arcgraphComponent.getGraph();

            Object[] selectionCells = graph.getSelectionCells();
            for (int i = 0; i < graph.getSelectionCount(); i++) {
                dModel.deleteNode(((mxCell) selectionCells[i]).getId());
            }
            graph.removeCells(selectionCells);

            selectionCells = graph2.getSelectionCells();
            for (int i = 0; i < graph2.getSelectionCount(); i++) {
                final String identf = ((mxCell) selectionCells[i]).getId();
                dModel.deleteNode(GUIdentifier.setArchID(identf));
            }
            graph2.removeCells(selectionCells);

            editor.getXmlUndoManager().add(dModel.getState());

            mxGraphActions.getDeleteAction().actionPerformed(actionEvent);
            graph.setSelectionCells(new Object[0]);
            graph2.setSelectionCells(new Object[0]);
            editor.updateTableView(null);
        }
    }

}
