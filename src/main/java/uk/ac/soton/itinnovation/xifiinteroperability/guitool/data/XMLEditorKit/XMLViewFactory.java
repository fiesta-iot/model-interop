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

class XMLViewFactory implements ViewFactory {
    @Override
    public View create(Element elem) {
        String kind = elem.getName();
        if (kind != null) {
            switch (kind) {
                case AbstractDocument.ContentElementName:
                    return new LabelView(elem);
                case XMLDocument.TAG_NAME_ELEMENT:
                    return new TagNameView(elem);
                case XMLDocument.ATTRIBUTE_NAME_ELEMENT:
                    return new AttributeNameView(elem);
                case XMLDocument.TAG_ELEMENT:
                    return new TagView(elem, false, false);
                case XMLDocument.SECOND_TAG_ELEMENT:
                    return new TagView(elem, false, true);
                case XMLDocument.START_TAG_ELEMENT:
                    return new TagView(elem, true, false);
                case XMLDocument.TAG_ROW_START_ELEMENT:
                case XMLDocument.TAG_ROW_END_ELEMENT:
                    return new BoxView(elem, View.X_AXIS) {
                        @Override
                        public float getAlignment(int axis) {
                            return 0;
                        }
                        @Override
                        public float getMaximumSpan(int axis) {
                            return getPreferredSpan(axis);
                        }
                    };
                case AbstractDocument.SectionElementName:
                    return new BoxView(elem, View.Y_AXIS);
                case StyleConstants.ComponentElementName:
                    return new ComponentView(elem);
                case StyleConstants.IconElementName:
                    return new IconView(elem);
                case XMLDocument.PLAIN_TEXT_ELEMENT:
                    return new PlainTextView(elem);
                default:
                    break;
            }
        }
        // default to text display
        return new LabelView(elem);
    }
}
