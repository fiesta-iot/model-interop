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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator.InvalidJSONException;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator.JSONParser;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator.JSONTreeNode;

/**
 * a custom JSONReader to parse the json in the editor pane
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class JSONReader {

    /**
     * a static instance of the reader
     */
    private static final JSONReader INSTANCE = new JSONReader();

    /**
     * empty and private constructor, use the static instance
     */
    private JSONReader(){}

    /**
     * a getter for the static instance
     * @return an instance of a JSONReader
     */
    public static  JSONReader getInstance(){
        return INSTANCE;
    }

    /**
     * the read method convers the InputStream to a json string and parses it into the custom tree model
     * @param is the input stream containing the json
     * @param d the text document
     * @param pos the starting position
     * @throws IOException
     * @throws BadLocationException
     */
    public void read(InputStream is, Document d, int pos) throws IOException, BadLocationException{
        if (!(d instanceof JSONDocument)) {
            return;
        }

        JSONDocument doc = (JSONDocument) d;

        try {
            String str = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            JSONTreeNode root = JSONParser.parseJSON(str);

            List<DefaultStyledDocument.ElementSpec> specs=new ArrayList<>();
            DefaultStyledDocument.ElementSpec spec=new DefaultStyledDocument.ElementSpec(new SimpleAttributeSet(), DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);

            writeJSONnode(doc, root, pos, specs);

            DefaultStyledDocument.ElementSpec[] data = new DefaultStyledDocument.ElementSpec[specs.size()];
            specs.toArray(data);
            doc.insert(pos, data);
        }
        catch (InvalidJSONException ex){
            JOptionPane.showMessageDialog(null, "Something went wrong while processing your json data.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * the writeJSONnode method, which recursively traverses the JSON tree and adds ElementSpecs for each node
     * @param doc the text document
     * @param node the JSONTreeNode
     * @param pos the position
     * @param specs the list to which to add new specs
     * @return
     */
    private int writeJSONnode(Document doc, JSONTreeNode node, int pos, List<DefaultStyledDocument.ElementSpec> specs){
        SimpleAttributeSet rootElementAttributes = new SimpleAttributeSet();
        rootElementAttributes.addAttribute(AbstractDocument.ElementNameAttribute, JSONDocument.ROOT_ELEMENT);
        SimpleAttributeSet keyElementAttributes = new SimpleAttributeSet();
        keyElementAttributes.addAttribute(AbstractDocument.ElementNameAttribute, JSONDocument.KEY_ELEMENT);
        SimpleAttributeSet rowStartAttributes = new SimpleAttributeSet();
        rowStartAttributes.addAttribute(AbstractDocument.ElementNameAttribute, JSONDocument.ROW_START_ELEMENT);
        SimpleAttributeSet rowEndAttributes = new SimpleAttributeSet();
        rowEndAttributes.addAttribute(AbstractDocument.ElementNameAttribute, JSONDocument.ROW_END_ELEMENT);
        SimpleAttributeSet indentElementAttributes = new SimpleAttributeSet();
        indentElementAttributes.addAttribute(AbstractDocument.ElementNameAttribute, JSONDocument.INDENT_ELEMENT);

        DefaultStyledDocument.ElementSpec spec;

        if (node.getNodeType() == JSONTreeNode.NodeType.ROOT_NODE) {
            spec = new DefaultStyledDocument.ElementSpec(rootElementAttributes, DefaultStyledDocument.ElementSpec.StartTagType);
            specs.add(spec);
        }
        else {
            spec = new DefaultStyledDocument.ElementSpec(indentElementAttributes, DefaultStyledDocument.ElementSpec.StartTagType);
            specs.add(spec);
        }

        spec=new DefaultStyledDocument.ElementSpec(rowStartAttributes, DefaultStyledDocument.ElementSpec.StartTagType);
        specs.add(spec);

        int offs = pos;
        switch(node.getNodeType()){
            case ROOT_NODE :
                spec = new DefaultStyledDocument.ElementSpec(JSONDocument.CURLY_BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "{".toCharArray(), 0, 1);
                specs.add(spec);
                break;

            case OBJECT_NODE:
                if (node.getNodeName() != null){
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "\"".toCharArray(), 0, 1);
                    specs.add(spec);
                    JSONDocument.KEYNAME_ATTRIBUTES.addAttribute("node", node);
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.KEYNAME_ATTRIBUTES.copyAttributes(), DefaultStyledDocument.ElementSpec.ContentType, node.getNodeName().toCharArray(), 0, node.getNodeName().length());
                    specs.add(spec);
                    JSONDocument.KEYNAME_ATTRIBUTES.removeAttribute("node");
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "\" : ".toCharArray(), 0, 4);
                    specs.add(spec);
                }

                spec = new DefaultStyledDocument.ElementSpec(JSONDocument.CURLY_BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "{".toCharArray(), 0, 1);
                specs.add(spec);
                break;

            case ARRAY_NODE:
                if (node.getNodeName() != null){
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "\"".toCharArray(), 0, 1);
                    specs.add(spec);
                    JSONDocument.KEYNAME_ATTRIBUTES.addAttribute("node", node);
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.KEYNAME_ATTRIBUTES.copyAttributes(), DefaultStyledDocument.ElementSpec.ContentType, node.getNodeName().toCharArray(), 0, node.getNodeName().length());
                    JSONDocument.KEYNAME_ATTRIBUTES.removeAttribute("node");
                    specs.add(spec);
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "\" : ".toCharArray(), 0, 4);
                    specs.add(spec);
                }

                spec = new DefaultStyledDocument.ElementSpec(JSONDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "[".toCharArray(), 0, 1);
                specs.add(spec);
                break;

            case LEAF_NODE:
                if (node.getNodeName() != null){
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "\"".toCharArray(), 0, 1);
                    specs.add(spec);
                    JSONDocument.KEYNAME_ATTRIBUTES.addAttribute("node", node);
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.KEYNAME_ATTRIBUTES.copyAttributes(), DefaultStyledDocument.ElementSpec.ContentType, node.getNodeName().toCharArray(), 0, node.getNodeName().length());
                    specs.add(spec);
                    JSONDocument.KEYNAME_ATTRIBUTES.removeAttribute("node");
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "\" : ".toCharArray(), 0, 4);
                    specs.add(spec);
                }

                if (node.getNodeValue() != null && node.getNodeValue() instanceof String){
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "\"".toCharArray(), 0, 1);
                    specs.add(spec);
                    JSONDocument.STRING_VALUE_ATTRIBUTES.addAttribute("node", node);
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.STRING_VALUE_ATTRIBUTES.copyAttributes(), DefaultStyledDocument.ElementSpec.ContentType, node.getNodeValue().toString().toCharArray(), 0, node.getNodeValue().toString().length());
                    specs.add(spec);
                    JSONDocument.STRING_VALUE_ATTRIBUTES.removeAttribute("node");
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "\"".toCharArray(), 0, 1);
                    specs.add(spec);
                }
                else {
                    if (node.getNodeValue() == null){
                        JSONDocument.VALUE_ATTRIBUTES.addAttribute("node", node);
                        spec = new DefaultStyledDocument.ElementSpec(JSONDocument.VALUE_ATTRIBUTES.copyAttributes(), DefaultStyledDocument.ElementSpec.ContentType, "null".toCharArray(), 0, 4);
                        specs.add(spec);
                        JSONDocument.VALUE_ATTRIBUTES.removeAttribute("node");
                    }
                    else {
                        JSONDocument.VALUE_ATTRIBUTES.addAttribute("node", node);
                        spec = new DefaultStyledDocument.ElementSpec(JSONDocument.VALUE_ATTRIBUTES.copyAttributes(), DefaultStyledDocument.ElementSpec.ContentType, node.getNodeValue().toString().toCharArray(), 0, node.getNodeValue().toString().length());
                        specs.add(spec);
                        JSONDocument.VALUE_ATTRIBUTES.removeAttribute("node");
                    }
                }
                break;

            default:
                break;
        }

        List<JSONTreeNode> childNodes = node.getChildNodes();
        if (childNodes != null && !childNodes.isEmpty()){
            spec=new DefaultStyledDocument.ElementSpec(rowEndAttributes, DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);
            for (JSONTreeNode child : childNodes){
                offs += writeJSONnode(doc, child, offs, specs);
            }

            spec=new DefaultStyledDocument.ElementSpec(rowStartAttributes, DefaultStyledDocument.ElementSpec.StartTagType);
            specs.add(spec);

            switch (node.getNodeType()) {
                case ROOT_NODE:
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.CURLY_BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "}".toCharArray(), 0, 1);
                    specs.add(spec);
                    break;

                case OBJECT_NODE:
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.CURLY_BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "}".toCharArray(), 0, 1);
                    specs.add(spec);
                    if (!node.isLast()){
                        spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, ",".toCharArray(), 0, 1);
                        specs.add(spec);
                    }
                    break;

                case ARRAY_NODE:
                    spec = new DefaultStyledDocument.ElementSpec(JSONDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, "]".toCharArray(), 0, 1);
                    specs.add(spec);
                    if (!node.isLast()) {
                        spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, ",".toCharArray(), 0, 1);
                        specs.add(spec);
                    }
                    break;

                default:
                    break;
            }

            spec = new DefaultStyledDocument.ElementSpec(rowEndAttributes, DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);
        }
        else {
            if (!node.isLast()){
                spec = new DefaultStyledDocument.ElementSpec(JSONDocument.COLON_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType, ",".toCharArray(), 0, 1);
                specs.add(spec);
            }
            spec = new DefaultStyledDocument.ElementSpec(rowEndAttributes, DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);
        }

        spec=new DefaultStyledDocument.ElementSpec(rowEndAttributes, DefaultStyledDocument.ElementSpec.EndTagType);
        specs.add(spec);

        return offs-pos;
    }

}
