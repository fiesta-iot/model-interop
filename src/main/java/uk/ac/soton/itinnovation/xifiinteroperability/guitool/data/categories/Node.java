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

import java.util.Collection;

/**
 * a basic interface for a Node in the Tree data structure
 * 
 * @author ns17
 * 
 * @param <T> a node is a generic data structure, implementation should be based on operations and not on type of the data contained
 */
public interface Node<T extends Comparable> extends Comparable<Node<T>> {
    
    /**
     * a method to get the level of this node in the tree
     * @return the level this node is found at
     */
    public int getLevel();
    
    /**
     * a method to return the value
     * @return the value of the node 
     */
    public T getValue();
    
    /**
     * a setter for the value of the node
     * @param value 
     */
    public void setValue(T value);
    
    /**
     * a method to check if a node is a root node
     * @return True if the node is a root node and false otherwise
     */
    public boolean isRoot();
    
    /**
     * a method to get the reference to the parent node
     * @return reference to the parent node object and null if this is a root node
     */
    public Node<T> getParent();
    
    /**
     * a method to return the children nodes
     * @return a collection of children nodes
     */
    public Collection<Node<T>> getChildren();
    
    /**
     * a method to append a new child
     * @param child the value of the child node to append
     */
    public void addChild(T child);

    /**
     * a method to remove a child
     * @param child the value of the child node to remove
     * @return true if the node has been removed and false otherwise
     */
    public boolean removeChild(T child);
    
    /**
     * overriding this method so that nodes can be compared
     * @param node the node to compare to
     * @return a comparing value between the two nodes
     */
    @Override
    public int compareTo(Node<T> node);
    
    /**
     * a string representation of the node
     * @return the value of the node
     */
    @Override
    public String toString();
}
