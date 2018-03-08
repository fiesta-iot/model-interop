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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor;

import com.mxgraph.io.mxCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import java.awt.Color;
import java.awt.Point;
import javax.swing.JOptionPane;
import org.w3c.dom.Document;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

/**
 * The UI element holding the behaviour graph (state machine).
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class BehaviourGraphComponent extends mxGraphComponent {

    /**
     * Maintain a local copy of the data model. This is a reference only.
     */
    private transient DataModel dataModel;

    private transient BasicGraphEditor editor;

    /**
     * Constant: file location of the GUI style - in resources location.
     */
    private static final String STYLEFILE = "gui/default-style.xml";

    /**
     * Construct a view of the behaviour graph. We are setting up mxGraph
     * to respond to the edits made in the right hand side window of the GUI.
     * @param graph The underlying mxGraph of the behaviour portion.
     */
    public BehaviourGraphComponent(final mxGraph graph) {
            super(graph);
            graph.setAllowDanglingEdges(false);
            graph.setDisconnectOnMove(false);

            // Sets switches typically used in an editor
            setPageVisible(false);
            setGridVisible(false);
            setToolTips(true);

            getConnectionHandler().setCreateTarget(true);

            // Loads the defalt stylesheet from an external file
            final mxCodec codec = new mxCodec();
            final Document doc = mxUtils.loadDocument(
                    Thread.currentThread().getContextClassLoader().getResource(STYLEFILE).toString());
            codec.decode(doc.getDocumentElement(), graph.getStylesheet());

            // Sets the background to white
            getViewport().setOpaque(true);
            getViewport().setBackground(Color.WHITE);
    }

    /**
     * Set reference to the global data model.
     * @param data The data model reference.
     */
    public final void setDataModel(final DataModel data) {
        this.dataModel = data;
    }

    public final void setEditor(final BasicGraphEditor editor){
        this.editor = editor;
    }

    /**
     * Overrides drop behaviour to set the cell style if the target
     * is not a valid drop target and the cells are of the same
     * type (eg. both vertices or both edges).
     * @param cells The imported GUI cells.
     * @param dxPos The position of the new cell on the x axis
     * @param origTarget The target object.
     * @param dyPos The position of new cell on the y axis
     * @param location The location of the mouse cursor.
     * @return The successfully dropped cells.
     */
    @Override
    public final Object[] importCells(final Object[] cells, final double dxPos,
            final double dyPos, final Object origTarget, final Point location) {

        Object target = origTarget;

        if (origTarget == null && cells.length == 1) {
            final String type = (String) ((mxCell) cells[0]).getValue();
            if (type.equalsIgnoreCase(DataModel.CLIENT) || type.equalsIgnoreCase(XMLStateMachine.INTERFACE_LABEL)) {
                JOptionPane.showMessageDialog(this.getParent(),
                    "Architecture elements not allowed in behavior graph",
                    "Design error",
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }

            if (type.equals(XMLStateMachine.START_LABEL) || type.equals(XMLStateMachine.TRIGGERSTART_LABEL)){
                if (dataModel.containsStart()){
                    JOptionPane.showMessageDialog(this.getParent(),
                            "Only one start node allowed in the graph",
                            "Design error",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }

            if (location != null) {
                target = getCellAt(location.x, location.y);

                if (target instanceof mxICell && cells[0] instanceof mxICell) {
                    final mxICell targetCell = (mxICell) target;
                    final mxICell dropCell = (mxICell) cells[0];

                    if (targetCell.isVertex() == dropCell.isVertex()
                            || targetCell.isEdge() == dropCell.isEdge()) {
                        final mxIGraphModel model = graph.getModel();
                        model.setStyle(target, model.getStyle(cells[0]));
                        graph.setSelectionCell(target);
                        return null;
                    }
                }
            }
        }
        final mxCell newNode = (mxCell) cells[0];
        String label = null;
        if (newNode != null) {
            label = (String) newNode.getValue();
        }
        String type = label;
        if (location == null){
            // in case of copy-pasting take the type from the CopyPasteManager
            type = editor.getCopyPasteManager().getLastType();
        }

        if (dataModel.graphIdentExist(label)){
            // generating a unique ID
            int i = 1;
            String testLabel = "state" + i;
            while (dataModel.graphIdentExist(testLabel)){
                i += 1;
                testLabel = "state" + i;
            }
            label = (String) JOptionPane.showInputDialog(this.getParent(),
                        "Please choose a label for this state",
                        "State Label",
                        JOptionPane.PLAIN_MESSAGE,
                        null, null, testLabel);
            if (label != null){
                label = label.replaceAll("\\s+", "_");
            }

            while (label != null && dataModel.graphIdentExist(label)){
                label = (String) JOptionPane.showInputDialog(this.getParent(),
                        "Please chooose a different label for this state",
                        "State Label",
                        JOptionPane.ERROR_MESSAGE,
                        null, null, testLabel);
                if (label != null){
                    label = label.replaceAll("\\s+", "_");
                }
            }
        }

        if (label != null){
            newNode.setValue(label);
            final Object[] newCells = super.importCells(cells, dxPos, dyPos, target, location);
            if (newCells[0] != null) {
                this.dataModel.addNode(((mxCell) newCells[0]).getId(), label, type);
                if (location == null){
                    // in case of copy-pasting set the GUI id of the pasted component in the CopyPasteManager
                    editor.getCopyPasteManager().setLastGUIid(((mxCell) newCells[0]).getId());
                }
                editor.getXmlUndoManager().add(this.dataModel.getState());
            }
            return newCells;
        }
        return null;
    }

}


