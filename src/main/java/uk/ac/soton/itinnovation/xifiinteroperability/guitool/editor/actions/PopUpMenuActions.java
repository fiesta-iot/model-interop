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
import com.mxgraph.view.mxGraph;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.InterfaceData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.DataModelState;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.EditorPopupMenu;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.GUIdentifier;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

/**
 * The set of GUI actions e.g. save, open, etc. that correspond to operations
 * selected from the UI.
 */
public final class PopUpMenuActions {

    /**
     * History action in UI to perform undo and redo using the action cache.
     */
    public static class HistoryAction extends AbstractAction {
            /**
             * Undo or redo action.
             */
            private final transient boolean undo;

            /**
             * The GUI editor context.
             */
            private transient BasicGraphEditor editor;

            /**
             * Create the action instance.
             * @param undoOp Whether this is undo (T) or redo (F)
             * @param edtr The editor context.
             */
            public HistoryAction(final boolean undoOp, final BasicGraphEditor edtr) {
                super();
                this.undo = undoOp;
                this.editor = edtr;
            }

            @Override
            public final void actionPerformed(final ActionEvent actEvent) {
                    if (editor == null) {
                      editor = EditorActions.getEditor(actEvent);
                }
                if (editor != null) {
                    if (undo && editor.getXmlUndoManager().canUndo()) {
                        editor.getUndoManager().undo();
                        DataModelState state = editor.getXmlUndoManager().undo();
                        if (state != null){
                            editor.getDataModel().clearData();
                            editor.getDataModel().updateState(state);
                        }
                    } 
                    else if (!undo && editor.getXmlUndoManager().canRedo()) {
                        editor.getUndoManager().redo();
                        DataModelState state = editor.getXmlUndoManager().redo();
                        if (state != null){
                            editor.getDataModel().clearData();
                            editor.getDataModel().updateState(state);
                        }
                    }
                editor.updateTableView(null);
                editor.getCodePanel().getXMLPanel().displayXMLSpecification();
                final mxGraphComponent graphComponent = editor.getBehaviourGraph();
                final mxGraphComponent arcgraphComponent = editor.getSystemGraph();
                final mxGraph graph = graphComponent.getGraph();
                final mxGraph graph2 = arcgraphComponent.getGraph();
                graph.setSelectionCells(new Object[0]);
                graph2.setSelectionCells(new Object[0]);
            }
        }
    }

    /**
     * Action to zoom in on graph.
     */
    public static class ZoomInAction extends AbstractAction {
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            final Component component = ((Component) actEvent.getSource()).getParent();
            final Component cComponent = ((EditorPopupMenu) component).getInvoker();
            final mxGraphComponent mxGraphComp = (mxGraphComponent) cComponent;
            mxGraphComp.zoomIn();
        }
    }

    /**
     * Zoom out the graph view when selected.
     */
    public static class ZoomOutAction extends AbstractAction {
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            final Component component = ((Component) actEvent.getSource()).getParent();
            final Component cComp = ((EditorPopupMenu) component).getInvoker();
            final mxGraphComponent msGraphComp = (mxGraphComponent) cComp;
            msGraphComp.zoomOut();
        }
    }

    /**
     * Copy the URL of a node in the system graph. Only applies to interface
     * nodes. Applying this action to any other node will have no effect.
     */
    public static class CopyURLAction extends AbstractAction {
        @Override
        public final void actionPerformed(final ActionEvent actEvent) {
            final Component component = ((Component) actEvent.getSource()).getParent();
            final Component cComp = ((EditorPopupMenu) component).getInvoker();
            final mxGraphComponent msGraphComp = (mxGraphComponent) cComp;
            final BasicGraphEditor editor = EditorActions.getEditor(actEvent);

            final Object[] selectionCells = msGraphComp.getGraph().getSelectionCells();
            final String nodeID = ((mxCell) selectionCells[0]).getId();

            final ArchitectureNode selNode = (ArchitectureNode) editor.getDataModel().getNode(GUIdentifier.setArchID(nodeID));
            String urlToCopy;
            if (selNode.getData().size() > 0) {
                Object[] urls = selNode.getData().toArray();
                InterfaceData chosenInterface = (InterfaceData) JOptionPane.showInputDialog( 
                        editor,
                        "Please choose the ID of the rest URL you want to copy",
                        "Copy URL",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        urls,
                        selNode.getData().get(0));
                if (chosenInterface != null){
                    urlToCopy = XMLStateMachine.COMPONENT_LABEL + "." + selNode.getLabel() + "." + chosenInterface.getRestID();
                    final StringSelection stringSelection = new StringSelection(urlToCopy);
                    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            }
            else {
                JOptionPane.showMessageDialog(
                        editor,
                        "There are no URLs to copy from this component",
                        "No URLs",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
