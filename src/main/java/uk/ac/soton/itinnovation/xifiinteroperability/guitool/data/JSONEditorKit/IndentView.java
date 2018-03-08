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
// Created By : Nikolay Stanchev - ns17@it-innovation.soton.ac.uk
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONEditorKit;

import javax.swing.text.BoxView;
import javax.swing.text.Element;

/**
 * a child class of the BoxView, sets some insets for JSON indentation
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class IndentView extends BoxView {

    /**
     * a constructor for the IndentView
     * @param elem the element associated with this view
     * @param indent boolean used to decide whether to indent the view or not
     */
    public IndentView(Element elem, boolean indent){
        super(elem, BoxView.Y_AXIS);

        int leftInset = indent ? 20 : 0;

        setInsets((short) 0, (short) (leftInset), (short) 0, (short) 0);
    }

    /**
     * a getter for the alignment of the view
     * @param axis the axis for which to return the alignment
     * @return the alignment
     */
    @Override
    public float getAlignment(int axis) {
        return 0;
    }

    /**
     * overriding the getter for the maximum span of the view to always return the preferred span
     * @param axis the axis for which to return the maximum span
     * @return the span
     */
    @Override
    public float getMaximumSpan(int axis) {
        return getPreferredSpan(axis);
    }
}
