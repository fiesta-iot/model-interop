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

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import org.w3c.dom.Node;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit.XMLDocument;

/**
 * GeneratorXMLDocument extends the XMLDocument in order to be compatible with
 * the XPath generator
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class GeneratorXMLDocument extends XMLDocument {

    /**
     * empty constructor, calls the parent constructor
     */
    public GeneratorXMLDocument(){
        super();
    }

    /**
     * overriding the createLeafElement to create a GeneratorLeafElement, which will
     * be compatible with the XPath generator
     *
     * @param parent the parent element
     * @param a the attribute set
     * @param p0 starting position
     * @param p1 ending position
     * @return
     */
    @Override
    protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
        Node node = (Node) a.getAttribute("node");
        String type = (String) a.getAttribute("type");
        if (node == null) {
            return new LeafElement(parent, a, p0, p1);
        }
        else {
            if (type.equals(XMLDocument.TAG_NAME_ELEMENT)){
                return new GeneratorLeafElement(parent, a, p0, p1, node);
            }
            else if (type.equals(XMLDocument.PLAIN_TEXT_ELEMENT)) {
                return new GeneratorLeafElement(parent, a, p0, p1, node.getFirstChild());
            }
            else if (type.equals(XMLDocument.ATTRIBUTE_NAME_ELEMENT)){
                return new GeneratorLeafElement(parent, a, p0, p1, node);
            }
            else {
                return new LeafElement(parent, a, p0, p1);
            }
        }
    }

    @Override
    protected Element createBranchElement(Element parent, AttributeSet a){
        return new BranchElement(parent, a);
    }

    /**
     * a new GeneratorLeafElement inner class which extends the LeafElement class
     * made to be compatible with the XPath generator
     *
     */
    public class GeneratorLeafElement extends LeafElement {

        /**
         * the GeneratorLeafElement stores the node the element is associated to
         */
        private final Node node;

        /**
         * a getter for the associated node
         * @return the respective Node reference
         */
        public Node getNode() {
            return node;
        }

        /**
         * a constructor for the GeneratorLeafElement, initialises a LeafElement
         * and stores the associated node
         *
         * @param parent the parent element
         * @param a the attribute set
         * @param p0 starting position
         * @param p1 ending position
         * @param node the associated node
         */
        public GeneratorLeafElement(Element parent, AttributeSet a, int p0, int p1, Node node) {
            super(parent, a, p0, p1);
            this.node = node;
        }
    }

}
