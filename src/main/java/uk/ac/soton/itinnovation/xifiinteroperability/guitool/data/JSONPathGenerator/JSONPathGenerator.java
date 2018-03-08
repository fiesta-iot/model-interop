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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator;

/**
 * a JSON path generator, which uses the custom JSON tree model to generate a JSON path for a given node
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class JSONPathGenerator {

    /**
     * a method to generate a JSON path from a JSONTreeNode object
     * @param node the node for which a JSON path is generated
     * @return the JSON path as a string
     */
    public static String getJSONPath(JSONTreeNode node){
        String toAppend = "";
        switch(node.getNodeType()){
            case LEAF_NODE:
            case ARRAY_NODE:
            case OBJECT_NODE:
                if (node.getParent().getNodeType() == JSONTreeNode.NodeType.ARRAY_NODE){
                    toAppend = "[" + node.getIndex() + "]";
                }

                if (node.getNodeName() != null){
                    toAppend += "." + node.getNodeName();
                }

                break;
            case ROOT_NODE:
                toAppend = node.getNodeName();
                return toAppend;

            default:
                break;
        }

        if (node.getParent() != null){
            return getJSONPath(node.getParent()) + toAppend;
        }
        else {
            return toAppend;
        }
    }

}
