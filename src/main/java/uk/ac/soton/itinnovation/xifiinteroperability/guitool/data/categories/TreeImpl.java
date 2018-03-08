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

import java.util.PriorityQueue;
import java.util.Stack;

/**
 * implementation for the Tree data structure
 * 
 * @author ns17
 * 
 * @param <T> the type of data stored in the tree
 */
public class TreeImpl<T extends Comparable> implements Tree<T> {

    /**
     * the root of the tree
     */
    private final Node<T> root;
    
    /**
     * a constructor for the tree data structure
     * @param root the root value of the tree
     */
    public TreeImpl(T root){
        this.root = new NodeImpl<>(root);
    }
    
    /**
     * a method to add a new child to the tree
     * @param child the value of the child node
     * @param parent the value of the parent node
     * @return true if the child has been added and false otherwise
     */
    @Override
    public boolean addChild(T child, T parent){
        // performing breadth first search to find the parent node to which we will add the child node
        PriorityQueue<Node<T>> fringe = new PriorityQueue<>();
        Node<T> parentNode = this.root;
        
        boolean parentFound = false;
        while (true){
            if (parentNode.getValue().equals(parent)){
                parentFound = true;
                break;
            }
            else {
                parentNode.getChildren().forEach((childNode) -> {
                    fringe.add(childNode);
                });
                
                if (fringe.isEmpty()){
                    break;
                }
                
                parentNode = fringe.poll();
            }
        }
        
        // if the parent node has been found, add the child node to it, or if not found return false
        if (parentFound){
            parentNode.addChild(child);
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * a method to remove a child node in the tree
     * @param child the value of the child node to delete
     * @return true if the root has been deleted and false otherwise
     * @throws RootDeletionException when trying to delete the root of the tree
     */
    @Override
    public boolean removeChild(T child) throws RootDeletionException {
        // performing depth first search to find the child node to remove
        Stack<Node<T>> fringe = new Stack<>();
        Node<T> childNode = this.root;
        
        boolean nodeFound = false;
        while (true){
            if (childNode.getValue() == child){
                nodeFound = true;
                break;
            }
            else {
                childNode.getChildren().forEach((children) -> {
                    fringe.add(children);
                });
                
                if(fringe.isEmpty()){
                    break;
                }
                
                childNode = fringe.pop();
            }
        }
        
        // if the node has been found, remove it from the parent's childs, else return false
        if (nodeFound){
            if (childNode.isRoot()){
                throw new RootDeletionException("You cannot delete the root of the tree. You can only manipulate the value of the root node.");
            }
            return childNode.getParent().removeChild(child);
        }
        else {
            return false;
        }
    }
    
    /**
     * a getter for the root of the tree
     * @return the root node
     */
    @Override
    public Node<T> getRoot() {
        return root;
    }
    
}
