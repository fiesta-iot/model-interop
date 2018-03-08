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

import com.mxgraph.swing.mxGraphComponent;

/**
 * Utility operations to handle the mxGraph identifiers across the view
 * and data representations.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public final class GUIdentifier {

    /**
     * Value to add to ID generated in the architecture panel. mxGraph
     * uses integers in both panels, hence we need to distinguish.
     */
    public static final String ARCHVIEW = "a_";

    /**
     * Private constructor for static class.
     */
    private GUIdentifier() {

    }

    /**
     * Given the id from mxGraph i.e. the returned id, and the panel from which
     * the id originated generate the unique id in the multi-panel UI.
     * @param ident The id from mxGraph
     * @param graphComponent The panel
     * @return The unique ID String.
     */
    public static String getGUIdentifier(final String ident, final mxGraphComponent graphComponent) {
        final String view = graphComponent.getClass().toString();
        if (view.contains("SystemGraphComponent")) {
            return ARCHVIEW + ident;
        }
        return ident;
    }

    /**
     * Append the arch constant pre statement.
     * @param ident The mxGraph UID to add to.
     * @return The appended ID.
     */
    public static String setArchID(final String ident) {
        return ARCHVIEW + ident;
    }
    
    /**
     * remove the arch constant
     * @param ident the ident to adjust
     * @return the clean UID in the mx graph
     */
    public static String removeArchID(final String ident){
        if (ident.startsWith(ARCHVIEW)){
            return ident.replace(ARCHVIEW, "");
        }
        return ident;
    }
}
