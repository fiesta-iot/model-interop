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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.categories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * implementation for the Node data structure
 * 
 * @author ns17
 * 
 * @param <T> the type of values the node stores
 */
public class NodeImpl<T extends Comparable> implements Node<T>, Comparable<Node<T>>{
    
    /**
     * a property to store the level of the node
     */
    private int level;
    
    /**
     * a reference to the parent node
     */
    private Node<T> parent;
    
    /**
     * the value stored in this node
     */
    private T value;
    
    /**
     * a list containing the children nodes
     */
    private List<Node<T>> children;
    
    /**
     * a constructor for the node data structure with no parent reference
     * @param value the value of the node
     */
    public NodeImpl(T value){
        this(value, null);
        this.level = 0;
    }
    
    /**
     * a constructor for the node data structure
     * @param value the value of the node
     * @param parent the reference of the parent node
     */
    public NodeImpl(T value, Node<T> parent){
        this.value = value;
        this.parent = parent;
        this.children = new ArrayList<>();
        if (parent != null){
            this.level = parent.getLevel() + 1;
        }
    }

    /**
     * a getter for the level of this node
     * @return 
     */
    @Override
    public int getLevel(){
        return this.level;
    }
    
    /**
     * gets the value of the node
     * @return the value of the node
     */
    @Override
    public T getValue() {
        return this.value;
    }

    /**
     * sets the value of the node
     * @param value the new value of the node
     */
    @Override
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * gets the parent reference of the node
     * @return the reference to the parent node object
     */
    @Override
    public Node<T> getParent() {
        return this.parent;
    }

    /**
     * gets the children nodes 
     * @return list containing all the children node objects
     */
    @Override
    public Collection<Node<T>> getChildren() {
        return this.children;
    }

    /**
     * appends a new child to the list of children nodes
     * @param child the value of the new child node to append
     */
    @Override
    public void addChild(T child) {
        Node<T> childNode = new NodeImpl(child, this);
        this.children.add(childNode);
    }

    /**
     * removes a child from the children nodes
     * @param child the value of the child node to remove
     * @return true if the child has been removed from the list and false otherwise
     */
    @Override
    public boolean removeChild(T child) {
        Node childToRemove = null;
        for (Node childNode: this.children){
            if (childNode.getValue() == child){
                childToRemove = childNode;
                break;
            }
        }
        
        if (childToRemove != null){
            return this.children.remove(childToRemove);
        }
        
        return false;
    }
    
    /**
     * a method to check if a given node is a root, that is - has no parent
     * @return true if the node doesn't have a reference to the parent node
     */
    @Override
    public boolean isRoot(){
        return this.getParent() == null;
    }

    /**
     * using the compare value of the node values
     * @param n the node to compare to
     * @return the compare value between the two nodes values
     */
    @Override
    public int compareTo(Node<T> n) {
        return this.value.compareTo(n.getValue());
    }
    
    /**
     * string representation of the node
     * @return the string value of the node
     */
    @Override
    public String toString(){
        return (String) this.value;
    }
}
