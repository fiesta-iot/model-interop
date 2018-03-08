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

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

/**
 * A graph that creates new edges from a given template edge.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class CustomGraph extends mxGraph {
        /**
         * Holds the edge to be used as a template for inserting new edges.
         */
        private transient Object edgeTemplate;

        /**
         * Custom graph that defines the alternate edge style to be used when
         * the middle control point of edges is double clicked (flipped).
         */
        public CustomGraph() {
            super();
            setAlternateEdgeStyle("edgeStyle=mxEdgeStyle.ElbowConnector;elbow=vertical");
        }

        /**
        * Sets the edge template to be used to inserting edges.
        * @param template The edge template to use.
        */
        public final void setEdgeTemplate(final Object template) {
                edgeTemplate = template;
        }

        /**
         * Overrides the method to use the currently selected edge template for
         * new edges.
         *
         * @param parent The parent graph.
         * @param ident The identifier of the edge.
         * @param value The edge label
         * @param source The source node (from) of the edge
         * @param target The target node (to) of the edge.
         * @param style The edge style.
         * @return The created edge object
         */
        @Override
        public final Object createEdge(final Object parent, final String ident, final Object value,
                        final Object source, final Object target, final String style) {
                if (edgeTemplate != null) {
                        final mxCell edge = (mxCell) cloneCells(new Object[] {edgeTemplate})[0];
                        edge.setId(ident);

                        return edge;
                }

                return super.createEdge(parent, ident, value, source, target, style);
        }

        // Overrides method to disallow edge label editing
        @Override
        public final boolean isCellEditable(final Object cell) {
            return !getModel().isEdge(cell);
        }


}
