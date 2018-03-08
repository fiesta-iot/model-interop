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
 * The system graph component is the graph of clients and interfaces.
 * This is the UI element - to display and handle drag and drop of templates
 * into the graph.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public  class SystemGraphComponent extends mxGraphComponent {

    /**
     * Reference to the single Data Model used by the tool. This Element
     * is a view onto the system graph portion of the data model.
     */
    private transient DataModel dataModel;

    private BasicGraphEditor editor;

    /**
     * Construct the UI component using the graph of the system elements.
     * @param graph The parent graph of this GUI element holding the client
     * and interface nodes (non-connected graph).
     */
    public SystemGraphComponent(final mxGraph graph) {
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
        final Document doc = mxUtils.loadDocument(Thread.currentThread().getContextClassLoader().getResource("gui/default-style.xml")
                        .toString());
        codec.decode(doc.getDocumentElement(), graph.getStylesheet());

        // Sets the background to white
        getViewport().setOpaque(true);
        getViewport().setBackground(Color.WHITE);
    }

    /**
     * Set the data model reference.
     * @param dModel The global data model.
     */
    public final void setDataModel(final DataModel dModel) {
        this.dataModel = dModel;
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
    public final Object[] importCells(final Object[] cells, final double dxPos, final double dyPos,
                    final Object origTarget, final Point location) {
        Object target = origTarget;
        String type = null;
        if (target == null && cells.length == 1) {
            type = (String) ((mxCell) cells[0]).getValue();
            if (location == null){
                // in case of copy pasting take the type of the CopyPasteManager
                type = editor.getCopyPasteManager().getLastType();
            }

            if (!(type.equalsIgnoreCase(DataModel.CLIENT) || type.equalsIgnoreCase(XMLStateMachine.INTERFACE_LABEL))) {
                JOptionPane.showMessageDialog(this.getParent(),
                    "Behaviour elements not allowed in architecture graph",
                    "Design error",
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (location != null){
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
        mxCell newNode = (mxCell) cells[0];
        String label = null;
        if (newNode != null) {
            label = (String) newNode.getValue();
        }

        if (dataModel.archIdentExist(label)){
            // generating a unique ID
            int i = 1;
            String testLabel = "component" + i;
            while (dataModel.archIdentExist(testLabel)){
                i += 1;
                testLabel = "component" + i;
            }
            label = (String) JOptionPane.showInputDialog(this.getParent(),
                        "Please choose a label identifier for this component",
                        "Component Identifier",
                        JOptionPane.PLAIN_MESSAGE,
                        null, null, testLabel);
            if (label != null){
                label = label.replaceAll("\\s+", "_");
            }

            while (label != null && dataModel.archIdentExist(label)){
                label = (String) JOptionPane.showInputDialog(this.getParent(),
                        "Please chooose a different label identifier for this component",
                        "Component Identifier",
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
                dataModel.addNode(((mxCell) newCells[0]).getId(), label, type);
                if (location == null){
                    // in case of copy-pasting set the GUI id of the pasted component in the CopyPasteManager
                    editor.getCopyPasteManager().setLastGUIid(GUIdentifier.ARCHVIEW + ((mxCell) newCells[0]).getId());
                }
                editor.getXmlUndoManager().add(this.dataModel.getState());
            }
            return newCells;
        }

        return null;
    }

}

