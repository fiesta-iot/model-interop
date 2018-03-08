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

import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.swing.text.*;
import javax.swing.text.Document;
import java.io.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;


public class XMLReader {
    static XMLReader instance=new XMLReader();
    protected boolean secondTag = false;
    protected XMLReader() {}
    public static XMLReader getInstance() {
        return instance;
    }
    public void read(InputStream is, Document d, int pos) throws IOException, BadLocationException{
        if (!(d instanceof XMLDocument)) {
            return;
        }

        XMLDocument doc=(XMLDocument)d;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setCoalescing(false);
        dbf.setValidating (false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(false);


        try {
            //Using factory get an instance of document builder
            javax.xml.parsers.DocumentBuilder dbXML = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            org.w3c.dom.Document dom = dbXML.parse(is);

            ArrayList<DefaultStyledDocument.ElementSpec> specs=new ArrayList<>();
            DefaultStyledDocument.ElementSpec spec=new DefaultStyledDocument.ElementSpec(new SimpleAttributeSet(), DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);

            if (doc.getLength()==0) {
                writeNode(doc,dom, pos, specs);
            }
            else {
                writeNode(doc,dom.getDocumentElement(), pos, specs);
            }

            DefaultStyledDocument.ElementSpec[] data = new DefaultStyledDocument.ElementSpec[specs.size()];
            specs.toArray(data);
            doc.insert(pos, data);

        } catch(SAXException pce) {
            JOptionPane.showMessageDialog(null, "Something went wrong while processing your xml data.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch(ParserConfigurationException | IOException pce) {
            JOptionPane.showMessageDialog(null, "Something went wrong while processing your xml data.", "Error", JOptionPane.ERROR_MESSAGE);
            throw new IOException(pce.getMessage());
        }
    }

    public int writeNode(Document doc, Node node, int pos, ArrayList<DefaultStyledDocument.ElementSpec> specs)  throws BadLocationException{
        SimpleAttributeSet xmlTagAttrs=new SimpleAttributeSet();
        xmlTagAttrs.addAttribute(AbstractDocument.ElementNameAttribute, XMLDocument.START_TAG_ELEMENT);
        SimpleAttributeSet secondTagAttrs=new SimpleAttributeSet();
        secondTagAttrs.addAttribute(AbstractDocument.ElementNameAttribute, XMLDocument.SECOND_TAG_ELEMENT);
        SimpleAttributeSet tagAttrs=new SimpleAttributeSet();
        tagAttrs.addAttribute(AbstractDocument.ElementNameAttribute, XMLDocument.TAG_ELEMENT);
        SimpleAttributeSet tagRowStartAttrs=new SimpleAttributeSet();
        tagRowStartAttrs.addAttribute(AbstractDocument.ElementNameAttribute, XMLDocument.TAG_ROW_START_ELEMENT);
        SimpleAttributeSet tagRowEndAttrs=new SimpleAttributeSet();
        tagRowEndAttrs.addAttribute(AbstractDocument.ElementNameAttribute, XMLDocument.TAG_ROW_END_ELEMENT);

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
        spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"<".toCharArray(), 0, 1);
        specs.add(spec);

//        tag name
        if (node instanceof org.w3c.dom.Document && doc.getLength()==0) {
            org.w3c.dom.Document dd=(org.w3c.dom.Document)node;
            String xml_tag = "xml";
            String xml_version_attr = "version";
            String xml_version_value = dd.getXmlVersion();

            // start tag name
            spec = new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    "?".toCharArray(), 0, 1);
            specs.add(spec);

            spec = new DefaultStyledDocument.ElementSpec(XMLDocument.TAGNAME_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    xml_tag.toCharArray(), 0, xml_tag.length());
            specs.add(spec);

            spec = new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    " ".toCharArray(), 0, 1);
            specs.add(spec);

            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.ATTRIBUTENAME_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    xml_version_attr.toCharArray(), 0, xml_version_attr.length());
            specs.add(spec);

            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    "=\"".toCharArray(), 0, 2);
            specs.add(spec);

            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.ATTRIBUTEVALUE_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    xml_version_value.toCharArray(), 0, xml_version_value.length());
            specs.add(spec);

            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    "\"".toCharArray(), 0, 1);
            specs.add(spec);

            spec = new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES,
                    DefaultStyledDocument.ElementSpec.ContentType,
                    "?".toCharArray(), 0, 1);
            specs.add(spec);
        }
        else {
            if (node.getNodeName().equals("state") || node.getNodeName().equals("component") || node.getNodeName().equals("data")
                    || node.getNodeName().equals("transition") || node.getNodeName().equals("interface")){
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.REMOVETAG_ATTRIBUTES,
                        DefaultStyledDocument.ElementSpec.ContentType,
                        node.getNodeName().toCharArray(), 0, node.getNodeName().length());
            }
            else if (node.getNodeName().equals("patterndata") || node.getNodeName().equals("architecture")
                    || node.getNodeName().equals("behaviour") || node.getNodeName().equals("label")
                    || node.getNodeName().equals("address")){
                 spec = new DefaultStyledDocument.ElementSpec(XMLDocument.ADDTAG_ATTRIBUTES,
                        DefaultStyledDocument.ElementSpec.ContentType,
                        node.getNodeName().toCharArray(), 0, node.getNodeName().length());
            }
            else {
                spec = new DefaultStyledDocument.ElementSpec(XMLDocument.TAGNAME_ATTRIBUTES,
                        DefaultStyledDocument.ElementSpec.ContentType,
                        node.getNodeName().toCharArray(), 0, node.getNodeName().length());
            }
            specs.add(spec);
        }

        NamedNodeMap attrs=node.getAttributes();
        if (attrs!=null && attrs.getLength()>0) {
            for (int i=0; i<attrs.getLength(); i++) {
                Node attr=attrs.item(i);
                String name=attr.getNodeName();
                String value=attr.getNodeValue();
//                " "
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType," ".toCharArray(), 0, 1);
                specs.add(spec);
//                attr name
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.ATTRIBUTENAME_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,name.toCharArray(), 0, name.length());
                specs.add(spec);
//                ="
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"=\"".toCharArray(), 0, 2);
                specs.add(spec);
//                attr value
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.ATTRIBUTEVALUE_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,value.toCharArray(), 0, value.length());
                specs.add(spec);
//                "
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"\"".toCharArray(), 0, 1);
                specs.add(spec);
            }
        }

        org.w3c.dom.NodeList list=node.getChildNodes();
        if (list!=null && list.getLength()>0) {
//            >
            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,">".toCharArray(), 0, 1);
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

                        spec=new DefaultStyledDocument.ElementSpec(XMLDocument.PLAIN_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,n.getNodeValue().toCharArray(), 0, n.getNodeValue().length());
                        specs.add(spec);

                        spec=new DefaultStyledDocument.ElementSpec(tagRowEndAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
                        specs.add(spec);

                    }
                }
            }
            spec=new DefaultStyledDocument.ElementSpec(tagRowStartAttrs, DefaultStyledDocument.ElementSpec.StartTagType);
            specs.add(spec);
            if (node instanceof org.w3c.dom.Document) {
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.TAGNAME_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType," ".toCharArray(), 0, 1);
                specs.add(spec);
            }
            else {
    //            </
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"</".toCharArray(), 0, 2);
                specs.add(spec);
    //            tag name
                    spec=new DefaultStyledDocument.ElementSpec(XMLDocument.TAGNAME_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,node.getNodeName().toCharArray(), 0, node.getNodeName().length());
                    specs.add(spec);
    //            />
                spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,">".toCharArray(), 0, 1);
                specs.add(spec);
            }
            spec=new DefaultStyledDocument.ElementSpec(tagRowEndAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);
        }
        else {
//            />
            spec=new DefaultStyledDocument.ElementSpec(XMLDocument.BRACKET_ATTRIBUTES, DefaultStyledDocument.ElementSpec.ContentType,"/>".toCharArray(), 0, 2);
            specs.add(spec);
            spec=new DefaultStyledDocument.ElementSpec(new SimpleAttributeSet(), DefaultStyledDocument.ElementSpec.EndTagType);
            specs.add(spec);
        }

        spec=new DefaultStyledDocument.ElementSpec(tagAttrs, DefaultStyledDocument.ElementSpec.EndTagType);
        specs.add(spec);

        return offs-pos;
    }
}
