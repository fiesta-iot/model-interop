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
import java.awt.*;

public class XMLDocument extends DefaultStyledDocument {
    public final static String START_TAG_ELEMENT="start_tag_element";
    public final static String SECOND_TAG_ELEMENT="second_tag_element";
    public final static String TAG_ELEMENT="tag_element";
    public final static String TAG_ROW_START_ELEMENT="tag_row_start_element";
    public final static String TAG_ROW_END_ELEMENT="tag_row_end_element";
    public final static String PLAIN_TEXT_ELEMENT="plain_text_element";
    public final static String TAG_NAME_ELEMENT = "tag_name_element";
    public final static String ATTRIBUTE_NAME_ELEMENT = "attribute_name_element";

    public final static SimpleAttributeSet BRACKET_ATTRIBUTES=new SimpleAttributeSet();
    public final static SimpleAttributeSet TAGNAME_ATTRIBUTES=new SimpleAttributeSet();
    public final static SimpleAttributeSet ATTRIBUTENAME_ATTRIBUTES=new SimpleAttributeSet();
    public final static SimpleAttributeSet ATTRIBUTEVALUE_ATTRIBUTES=new SimpleAttributeSet();
    public final static SimpleAttributeSet PLAIN_ATTRIBUTES=new SimpleAttributeSet();
    public final static SimpleAttributeSet REMOVETAG_ATTRIBUTES = new SimpleAttributeSet();
    public final static SimpleAttributeSet ADDTAG_ATTRIBUTES = new SimpleAttributeSet();

    static {
        StyleConstants.setBold(TAGNAME_ATTRIBUTES, true);
        StyleConstants.setForeground(TAGNAME_ATTRIBUTES, Color.BLUE.darker());
        TAGNAME_ATTRIBUTES.addAttribute(AbstractDocument.ElementNameAttribute, TAG_NAME_ELEMENT);

        StyleConstants.setBold(REMOVETAG_ATTRIBUTES, true);
        StyleConstants.setForeground(REMOVETAG_ATTRIBUTES, Color.BLUE.darker());
        REMOVETAG_ATTRIBUTES.addAttribute(AbstractDocument.ElementNameAttribute, TAG_NAME_ELEMENT);

        StyleConstants.setBold(ADDTAG_ATTRIBUTES, true);
        StyleConstants.setForeground(ADDTAG_ATTRIBUTES, Color.BLUE.darker());
        ADDTAG_ATTRIBUTES.addAttribute(AbstractDocument.ElementNameAttribute, TAG_NAME_ELEMENT);

        StyleConstants.setBold(ATTRIBUTENAME_ATTRIBUTES, true);
        StyleConstants.setForeground(ATTRIBUTENAME_ATTRIBUTES, Color.RED.brighter());
        ATTRIBUTENAME_ATTRIBUTES.addAttribute(AbstractDocument.ElementNameAttribute, "attribute_name_element");

        StyleConstants.setItalic(ATTRIBUTEVALUE_ATTRIBUTES, true);
        StyleConstants.setForeground(ATTRIBUTEVALUE_ATTRIBUTES, Color.GREEN.darker());

        StyleConstants.setFontSize(PLAIN_ATTRIBUTES, StyleConstants.getFontSize(PLAIN_ATTRIBUTES)-1);
        StyleConstants.setBold(PLAIN_ATTRIBUTES, true);
        PLAIN_ATTRIBUTES.addAttribute(AbstractDocument.ElementNameAttribute, PLAIN_TEXT_ELEMENT);
    }

    public XMLDocument() {
        super();
    }

    @Override
    protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
        super.insert(offset, data);
    }

}
