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
// Created By : Nikolay Stanchev - ns17@it-innovation.soton.ac.uk
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONEditorKit;

import java.awt.Color;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator.JSONTreeNode;

/**
 * a child class of the DefaultStyledDocument, sets some basic styles used for displaying the JSON file
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class JSONDocument extends DefaultStyledDocument {

    public final static String ROW_START_ELEMENT = "row_start_element";
    public final static String ROW_END_ELEMENT = "row_end_element";
    public final static String ROOT_ELEMENT = "start_element";
    public final static String KEY_ELEMENT = "key_element";
    public final static String INDENT_ELEMENT = "indent_element";
    public final static String JSON_KEY_NAME = "JSON_KEY_NAME";
    public final static String JSON_VALUE_NAME = "JSON_VALUE_NAME";

    public final static SimpleAttributeSet BRACKET_ATTRIBUTES = new SimpleAttributeSet();
    public final static SimpleAttributeSet CURLY_BRACKET_ATTRIBUTES = new SimpleAttributeSet();
    public final static SimpleAttributeSet COLON_ATTRIBUTES = new SimpleAttributeSet();
    public final static SimpleAttributeSet KEYNAME_ATTRIBUTES = new SimpleAttributeSet();
    public final static SimpleAttributeSet VALUE_ATTRIBUTES = new SimpleAttributeSet();
    public final static SimpleAttributeSet STRING_VALUE_ATTRIBUTES = new SimpleAttributeSet();

    static {
        StyleConstants.setForeground(BRACKET_ATTRIBUTES, new Color(119, 51, 255));

        StyleConstants.setForeground(CURLY_BRACKET_ATTRIBUTES, Color.MAGENTA.brighter());

        StyleConstants.setForeground(COLON_ATTRIBUTES, Color.darkGray.darker());

        StyleConstants.setForeground(KEYNAME_ATTRIBUTES, Color.RED);
        StyleConstants.setBold(KEYNAME_ATTRIBUTES, true);
        KEYNAME_ATTRIBUTES.addAttribute(AbstractDocument.ElementNameAttribute, JSON_KEY_NAME);

        StyleConstants.setForeground(VALUE_ATTRIBUTES, Color.BLUE);
        StyleConstants.setItalic(VALUE_ATTRIBUTES, true);
        VALUE_ATTRIBUTES.addAttribute(AbstractDocument.ElementNameAttribute, JSON_VALUE_NAME);

        StyleConstants.setForeground(STRING_VALUE_ATTRIBUTES, Color.MAGENTA.darker());
        StyleConstants.setItalic(STRING_VALUE_ATTRIBUTES, true);
        STRING_VALUE_ATTRIBUTES.addAttribute(AbstractDocument.ElementNameAttribute, JSON_VALUE_NAME);
    }

    public JSONDocument(){
        super();
    }

    @Override
    protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
        super.insert(offset, data);
    }

    /**
     * overriding the createLeafElement method to create a custom leaf element associated with a JSON node when possible
     * @param parent the parent element
     * @param a the attribute set
     * @param p0 start index
     * @param p1 end index
     * @return the new leaf element
     */
    @Override
    protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
        JSONTreeNode node = (JSONTreeNode) a.getAttribute("node");
        if (node == null) {
            return new LeafElement(parent, a, p0, p1);
        }
        else {
            return new GeneratorLeafElement(parent, a, p0, p1, node);
        }
    }

    /**
     * a custom representation of the LeafElement class, which stores the associated JSON node in a reference
     */
    public class GeneratorLeafElement extends LeafElement {

        private final JSONTreeNode node;

        public JSONTreeNode getNode() {
            return node;
        }

        public GeneratorLeafElement(Element parent, AttributeSet a, int p0, int p1, JSONTreeNode node) {
            super(parent, a, p0, p1);
            this.node = node;
        }
    }

}
