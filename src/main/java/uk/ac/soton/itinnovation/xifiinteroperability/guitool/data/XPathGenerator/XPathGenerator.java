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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XPathGenerator;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * a XPathGenerator, which takes a Node instance from an XML document and generates
 * an XPath, which leads to it.
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class XPathGenerator {

    /**
     * a static method, which generates an XPath by taking a Node instance as an argument
     * @param node the node to generate the XPath for
     * @return
     */
    public static String getXPath(Node node){
        String toAppend;
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
                toAppend = "/text()";
                break;
            case Node.ATTRIBUTE_NODE:
                toAppend = "@" + node.getNodeName();
                return getXPath(((Attr) node).getOwnerElement()) + toAppend;
            default:
                toAppend = "/" + node.getNodeName();
                break;
        }

        if (node.getParentNode() == null || node.getParentNode().getNodeType() == Node.DOCUMENT_NODE){
            return toAppend;
        }
        else {
            int index = 1;
            Node testNode = node.getPreviousSibling();
            while (testNode != null){
                if (testNode.getNodeType() == Node.ELEMENT_NODE && testNode.getNodeName().equals(node.getNodeName())){
                    index += 1;
                }
                testNode = testNode.getPreviousSibling();
            }

            testNode = node.getNextSibling();
            boolean hasNextNode = false;
            while (testNode != null){
                if (testNode.getNodeType() == Node.ELEMENT_NODE && testNode.getNodeName().equals(node.getNodeName())){
                    hasNextNode = true;
                    break;
                }
                testNode = testNode.getNextSibling();
            }

            if (!hasNextNode && index == 1){
                NamedNodeMap attrs = node.getAttributes();
                if (attrs != null && attrs.getLength() > 0) {
                    toAppend += "[@" + attrs.getNamedItem(attrs.item(0).getNodeName()) + "]";
                }
                return getXPath(node.getParentNode()) + toAppend;
            }
            else {
                return getXPath(node.getParentNode()) + toAppend + "[" + index + "]";
            }
        }
    }
}
