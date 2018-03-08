/*******************************************************************************
 * Created by Stanislav Lapitsky
 *
 * Reference to original source code http://java-sl.com/xml_editor_kit.html
 ******************************************************************************/

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
// Modified By : Nikolay Stanchev - ns17@it-innovation.soton.ac.uk
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit;

import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;

public class TagView extends BoxView {
    private boolean isExpanded=true;
    private final boolean isXMLStartTag;
    private final boolean isSecondTag;
    public static final int AREA_X_SHIFT = 20;
    public static final int AREA_SHIFT=10;

    public final boolean isStartTag(){
        return isXMLStartTag;
    }

    public final boolean isSecondTag(){
        return isSecondTag;
    }

    public TagView(Element elem, boolean start_tag, boolean second_tag) {
        super(elem, View.Y_AXIS);
        isXMLStartTag = start_tag;
        isSecondTag = second_tag;
        if (isXMLStartTag){
            setInsets((short)0,(short)0,(short)0,(short)0);
        }
        else if (isSecondTag){
            setInsets((short)0,(short)(AREA_SHIFT),(short)0,(short)0);
        }
        else {
            setInsets((short)0,(short)(AREA_SHIFT + AREA_X_SHIFT),(short)0,(short)0);
        }
    }

    @Override
    public float getAlignment(int axis) {
        return 0;
    }

    @Override
    public void paint(Graphics g, Shape alloc) {
        Rectangle a=alloc instanceof Rectangle ? (Rectangle)alloc : alloc.getBounds();
        Shape oldClip=g.getClip();
        if (!isExpanded()) {
            Area newClip=new Area(oldClip);
            newClip.intersect(new Area(a));
            g.setClip(newClip);
        }
        super.paint(g, a);
        if (!isXMLStartTag){
            if (getViewCount()>1) {
                g.setClip(oldClip);
                a.width--;
                a.height--;
                g.setColor(Color.lightGray);
                if (!isSecondTag){
                    a.x += AREA_X_SHIFT;
                }
                //collapse rect
                g.drawRect(a.x, a.y+AREA_SHIFT/2, AREA_SHIFT,AREA_SHIFT);

                if (!isExpanded()) {
                    g.drawLine(a.x+AREA_SHIFT/2, a.y+AREA_SHIFT/2+2, a.x+AREA_SHIFT/2, a.y+AREA_SHIFT/2+AREA_SHIFT-2);
                }
                else {
                    g.drawLine(a.x+AREA_SHIFT/2,  a.y+3*AREA_SHIFT/2, a.x+AREA_SHIFT/2,a.y+a.height);
                    g.drawLine(a.x+AREA_SHIFT/2,  a.y+a.height, a.x+AREA_SHIFT,a.y+a.height);
                }

                g.drawLine(a.x+2, a.y+AREA_SHIFT, a.x+AREA_SHIFT-2, a.y+AREA_SHIFT);
            }
        }
    }

    @Override
    public float getPreferredSpan(int axis) {
        if (isExpanded() || axis!=View.Y_AXIS) {
            return super.getPreferredSpan(axis);
        }
        else {
            View firstChild=getView(0);
            return getTopInset()+firstChild.getPreferredSpan(View.Y_AXIS);
        }
    }

    @Override
    public float getMinimumSpan(int axis) {
        if (isExpanded() || axis!=View.Y_AXIS) {
            return super.getMinimumSpan(axis);
        }
        else {
            View firstChild=getView(0);
            return getTopInset()+firstChild.getMinimumSpan(View.Y_AXIS);
        }
    }

    @Override
    public float getMaximumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    @Override
    protected int getNextEastWestVisualPositionFrom(int pos, Position.Bias b,
						    Shape a,
						    int direction,
						    Position.Bias[] biasRet)
	                                        throws BadLocationException {
        int newPos=super.getNextEastWestVisualPositionFrom(pos, b, a, direction, biasRet);
        if (!isExpanded()) {
            if (newPos>=getStartOffset() && newPos<getView(0).getView(0).getEndOffset()) {
                //first line of first child
                return newPos;
            }
            else if (newPos>=getView(0).getView(0).getEndOffset()) {
                if (direction==SwingConstants.EAST) {
                    newPos=Math.min(getDocument().getLength()-1, getEndOffset());
                }
                else {
                    newPos=getView(0).getView(0).getEndOffset()-1;
                }
            }
        }

        return newPos;
    }
}
