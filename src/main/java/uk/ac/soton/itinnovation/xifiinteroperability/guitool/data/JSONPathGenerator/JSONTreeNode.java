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

import java.util.ArrayList;
import java.util.List;

/**
 * A JSONTreeNode class, which mimics the implementation of the java XML Node class
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class JSONTreeNode {

    /**
     * NodeType enum represents the type of the JSONTreeNode
     */
    public enum NodeType {
        /**
         * A leaf node is a node with no child nodes
         */
        LEAF_NODE,

        /**
         * An object node is a node, which represents a JSON object - a map
         */
        OBJECT_NODE,

        /**
         * An array node is a node, which represents an array
         */
        ARRAY_NODE,

        /**
         * A root node is a node with no parent, its name must be '$'
         */
        ROOT_NODE
    }

    /**
     * the parent of the node
     */
    private JSONTreeNode parent;

    /**
     * a getter for the parent of the node
     * @return the parent of this node
     */
    public final JSONTreeNode getParent(){
        return this.parent;
    }

    /**
     * a list with all child nodes
     */
    private final List<JSONTreeNode> children;

    /**
     * a getter for the list of child nodes
     * @return all child nodes
     */
    public List<JSONTreeNode> getChildNodes() {
        return children;
    }

    /**
     * the node name
     */
    private final String name;

    /**
     * a getter for the node name
     * @return the name of the node, which will be used for generating the XPath
     */
    public final String getNodeName(){
        return name;
    }

    /**
     * the value of the node - must be either a String, a boolean, a number or an array of these
     */
    private final Object value;

    /**
     * a getter for the node value
     * @return the assigned value to this node
     */
    public final Object getNodeValue(){
        return value;
    }

    /**
     * the index of the node, if its parent is an array node,
     * this must be null if the parent is not an array node
     */
    private final Integer index;

    public final Integer getIndex() {
        return index;
    }

    /**
     * the type of the node
     */
    private NodeType type;

    /**
     * a getter for the node type
     * @return the node's type
     */
    public final NodeType getNodeType(){
        return type;
    }

    /**
     * a boolean, which represents if a node is a last child node of its parent
     */
    private boolean isLast;

    /**
     * a getter method to check if a node is the last child node of its parent
     * @return the isLast attribute
     */
    public boolean isLast(){
        return isLast;
    }

    /**
     * a setter method for the isLast attribute
     * @param isLast the new isLast boolean
     */
    public void setIsLast(boolean isLast){
        this.isLast = isLast;
    }

    /**
     * a constructor for a JSONTreeNode
     * @param parent the node's parent
     * @param name the node's name
     * @param value the node's value
     * @param type the node's type
     * @param index the node's index if its parent is an array node
     */
    private JSONTreeNode(JSONTreeNode parent, String name, Object value, NodeType type, Integer index) {
        this.parent = parent;
        this.name = name;
        this.value = value;
        this.type = type;
        this.index = index;
        this.isLast = false;

        children = new ArrayList<>();

        if (parent != null) {
            parent.appendChild(this);
        }
    }

    /**
     * a constructor for a JSONTreeNode with no parent and value specified (null)
     * this method is used to construct the root node
     *
     * @param name the node's name
     */
    private JSONTreeNode(String name){
        this(null, name, null, NodeType.ROOT_NODE, null);
    }

    /**
     * a constructor for a JSONTreeNode with no value
     * @param parent the node's parent
     * @param name the node's name
     * @param type the node's type
     * @param index the node's index if its parent is an array node
     */
    private JSONTreeNode(JSONTreeNode parent, String name, NodeType type, Integer index){
        this(parent, name, null, type, index);
    }

    /**
     * a static method which creates instances of this class based on the type of the node
     * @param parent the node's parent
     * @param name the node's name
     * @param value the node's value
     * @param type the node's type
     * @param index the node's index if its parent is an array node
     * @return
     */
    public static JSONTreeNode createNode(JSONTreeNode parent, String name, Object value, NodeType type, Integer index){
        if (parent == null || parent.getNodeType() != NodeType.ARRAY_NODE){
            index = null;
        }
        switch (type){
            case LEAF_NODE:
                return new JSONTreeNode(parent, name, value, type, index);
            case ARRAY_NODE:
                return new JSONTreeNode(parent, name, type, index);
            case OBJECT_NODE:
                return new JSONTreeNode(parent, name, type, index);
            case ROOT_NODE:
                return new JSONTreeNode("$");
            default:
                return null;
        }
    }

    /**
     * a method to add a child to this node
     * @param node the child node
     */
    private void appendChild(JSONTreeNode node){
        if (children.size() > 0){
            children.get(children.size()-1).setIsLast(false);
        }

        children.add(node);
        node.setIsLast(true);
    }

}
