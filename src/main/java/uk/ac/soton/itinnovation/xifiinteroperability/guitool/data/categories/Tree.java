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

/**
 * a basic Tree data structure with the required methods
 * 
 * @author ns17
 * 
 * @param <T> the type of elements stored in the tree
 */
public interface Tree<T extends Comparable> {
    
    /**
     * a getter method for the root of the tree
     * @return the root of the tree - a Node object
     */
    public Node<T> getRoot();
    
    
    /**
     * a method to add a child in the tree
     * @param child the value of the child node to add
     * @param parent the value of the parent node
     * @return  true if the child node has been added and false otherwise
     */
    public boolean addChild(T child, T parent);
    
    /**
     * a method to remove a child in the tree
     * @param child the value of the child node to remove
     * @return  true if the child node has been removed and false otherwise
     * @throws RootDeletionException when trying to delete the root of the tree
     */
    public boolean removeChild(T child) throws RootDeletionException;
}
