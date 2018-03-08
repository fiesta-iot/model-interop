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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XPathGenerator;

import java.util.ArrayList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit.XMLDocument;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit.XMLReader;

/**
 * a GeneratorXMLReader, which extends the XMLReader class so that it is compatible
 * with the XPath Generator
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class GeneratorXMLReader extends XMLReader{
    /**
     * static instance of the reader
     */
    static GeneratorXMLReader instance = new GeneratorXMLReader();

    /**
     * private constructor, instance of the class available with the getInstance method
     */
    private GeneratorXMLReader(){}

    /**
     * a getter for an instance of the class
     * @return the static instance of the XMLReader
     */
    public static XMLReader getInstance() {
        return instance;
    }

    /**
     * Overriding the XMLReader class to include the associated XML Node in the
     * attribute set used to create each text element
     * @param doc document to insert the text into
     * @param node XMLNode to get the information from
     * @param pos starting position
     * @param specs array list of ElementSpec instances used in creating the text elements
     * @return
     * @throws BadLocationException
     */
    @Override
    public int writeNode(Document doc, Node node, int pos, ArrayList<DefaultStyledDocument.ElementSpec> specs)  throws BadLocationException{
        SimpleAttributeSet xmlTagAttrs=new SimpleAttributeSet();
        xmlTagAttrs.addAttribute(AbstractDocument.ElementNameAttribute, GeneratorXMLDocument.START_TAG_ELEMENT);
        SimpleAttributeSet secondTagAttrs=new SimpleAttributeSet();
        secondTagAttrs.addAttribute(AbstractDocument.ElementNameAttribute, GeneratorXMLDocument.SECOND_TAG_ELEMENT);
        SimpleAttributeSet tagAttrs=new SimpleAttributeSet();
        tagAttrs.addAttribute(AbstractDocument.ElementNameAttribute, GeneratorXMLDocument.TAG_ELEMENT);
        SimpleAttributeSet tagRowStartAttrs=new SimpleAttributeSet();
        tagRowStartAttrs.addAttribute(AbstractDocument.ElementNameAttribute, GeneratorXMLDocument.TAG_ROW_START_ELEMENT);
        SimpleAttributeSet tagRowEndAttrs=new SimpleAttributeSet();
        tagRowEndAttrs.addAttribute(AbstractDocument.ElementNameAttribute, GeneratorXMLDocument.TAG_ROW_END_ELEMENT);

        DefaultStyledDocument.ElementSpec spec;
        if (node != null && node.getNodeType() == Node.DOCUMENT_NODE){
            spec=new DefaultStyledDocument.ElementSpec(xmlTagAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
            secondTag = true;
        }
        else {
            if (!secondTag){
                spec=new DefaultStyledDocument.ElementSpec(tagAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
            }
            else {
                spec = new DefaultStyledDocument.ElementSpec(secondTagAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
                secondTag = false;
            }
        }
        specs.add(spec);
        spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
        specs.add(spec);

        int offs=pos;
//        <
        spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"<".toCharArray(), 0, 1);
        specs.add(spec);

//        tag name
        if (node instanceof org.w3c.dom.Document && doc.getLength()==0) {
            org.w3c.dom.Document dd=(org.w3c.dom.Document)node;
            String xml_tag = "xml";
            String xml_version_attr = "version";
            String xml_version_value = dd.getXmlVersion();

            // start tag name
            spec = new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    "?".toCharArray(), 0, 1);
            specs.add(spec);

            spec = new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.TAGNAME_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    xml_tag.toCharArray(), 0, xml_tag.length());
            specs.add(spec);

            spec = new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    " ".toCharArray(), 0, 1);
            specs.add(spec);

            spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.ATTRIBUTENAME_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    xml_version_attr.toCharArray(), 0, xml_version_attr.length());
            specs.add(spec);

            spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    "=\"".toCharArray(), 0, 2);
            specs.add(spec);

            spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.ATTRIBUTEVALUE_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    xml_version_value.toCharArray(), 0, xml_version_value.length());
            specs.add(spec);

            spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    "\"".toCharArray(), 0, 1);
            specs.add(spec);

            spec = new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    "?".toCharArray(), 0, 1);
            specs.add(spec);
        }
        else {
            GeneratorXMLDocument.TAGNAME_ATTRIBUTES.addAttribute("node", node);
            GeneratorXMLDocument.TAGNAME_ATTRIBUTES.addAttribute("type", XMLDocument.TAG_NAME_ELEMENT);
            spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.TAGNAME_ATTRIBUTES.copyAttributes(),
                    DefaultStyledDocument.ElementSpec.ContentType,
                    node.getNodeName().toCharArray(), 0, node.getNodeName().length());
            GeneratorXMLDocument.TAGNAME_ATTRIBUTES.removeAttribute("node");
            GeneratorXMLDocument.TAGNAME_ATTRIBUTES.removeAttribute("type");
            specs.add(spec);
        }

        NamedNodeMap attrs=node.getAttributes();
        if (attrs!=null && attrs.getLength()>0) {
            for (int i=0; i<attrs.getLength(); i++) {
                Node attr=attrs.item(i);
                String name=attr.getNodeName();
                String value=attr.getNodeValue();
//                " "
                spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType," ".toCharArray(), 0, 1);
                specs.add(spec);
//                attr name
                GeneratorXMLDocument.ATTRIBUTENAME_ATTRIBUTES.addAttribute("node", attr);
                GeneratorXMLDocument.ATTRIBUTENAME_ATTRIBUTES.addAttribute("type", XMLDocument.ATTRIBUTE_NAME_ELEMENT);
                spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.ATTRIBUTENAME_ATTRIBUTES.copyAttributes(), DefaultStyledDocument.ElementSpec.ContentType,name.toCharArray(), 0, name.length());
                specs.add(spec);
                GeneratorXMLDocument.ATTRIBUTENAME_ATTRIBUTES.removeAttribute("node");
                GeneratorXMLDocument.ATTRIBUTENAME_ATTRIBUTES.removeAttribute("type");
//                ="
                spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"=\"".toCharArray(), 0, 2);
                specs.add(spec);
//                attr value
                spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.ATTRIBUTEVALUE_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,value.toCharArray(), 0, value.length());
                specs.add(spec);
//                "
                spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"\"".toCharArray(), 0, 1);
                specs.add(spec);
            }
        }

        org.w3c.dom.NodeList list=node.getChildNodes();
        if (list!=null && list.getLength()>0) {
//            >
            spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,">".toCharArray(), 0, 1);
            specs.add(spec);
            spec=new DefaultStyledDocument.ElementSpec(tagRowEndAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);

            for (int i=0; i<list.getLength(); i++) {
                Node n=list.item(i);
                if (n instanceof Element) {
                    Element child=(Element)n;
                    offs+=writeNode(doc, child, offs, specs);
                }
                else if (n.getNodeType()==Node.COMMENT_NODE) {
                   // skip comments
                }
                else {
                    if (n.getNodeValue() != null && !n.getNodeValue().trim().isEmpty()){
                        //plain text
                        spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
                        specs.add(spec);

                        GeneratorXMLDocument.PLAIN_ATTRIBUTES.addAttribute("node", node);
                        GeneratorXMLDocument.PLAIN_ATTRIBUTES.addAttribute("type", XMLDocument.PLAIN_TEXT_ELEMENT);
                        spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.PLAIN_ATTRIBUTES.copyAttributes(), DefaultStyledDocument.ElementSpec.ContentType,n.getNodeValue().toCharArray(), 0, n.getNodeValue().length());
                        specs.add(spec);
                        GeneratorXMLDocument.PLAIN_ATTRIBUTES.removeAttribute("node");
                        GeneratorXMLDocument.PLAIN_ATTRIBUTES.removeAttribute("type");

                        spec=new DefaultStyledDocument.ElementSpec(tagRowEndAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
                        specs.add(spec);
                    }
                }
            }
            spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
            specs.add(spec);
            if (node instanceof org.w3c.dom.Document) {
                spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.TAGNAME_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType," ".toCharArray(), 0, 1);
                specs.add(spec);
            }
            else {
    //            </
                spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"</".toCharArray(), 0, 2);
                specs.add(spec);
    //            tag name
                    spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.TAGNAME_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,node.getNodeName().toCharArray(), 0, node.getNodeName().length());
                    specs.add(spec);
    //            />
                spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,">".toCharArray(), 0, 1);
                specs.add(spec);
            }
            spec=new DefaultStyledDocument.ElementSpec(tagRowEndAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);
        }
        else {
//            />
            spec=new DefaultStyledDocument.ElementSpec(GeneratorXMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"/>".toCharArray(), 0, 2);
            specs.add(spec);
            spec=new DefaultStyledDocument.ElementSpec(new SimpleAttributeSet(), DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);
        }

        spec=new DefaultStyledDocument.ElementSpec(tagAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
        specs.add(spec);

        return offs-pos;
    }
}
