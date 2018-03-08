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

import java.util.List;
import java.util.Map;

/**
 * a wrapper a data structure, which stores a tree, with the categories of the repository and a map of the name of the models to their respective IDs
 * 
 * @author ns17
 */
public class TreeWrapper {
    
    /**
     * stores the categories tree with the leaf nodes being the models names
     */
    private final Tree categories;
    
    /**
     * a map, which links the models names to their respective IDs
     */
    private final Map<String, String> modelIDs;
    
    /**
     * a map, which links the categories to their models
     */
    private final Map<String, List<String>> categoriesModels;
    
    /**
     * a constructor for the tree wrapper
     * @param categories the categories of the reposiotory
     * @param modelIDs the models IDs
     * @param categoriesModels the categories models map
     */
    public TreeWrapper(Tree categories, Map<String, String> modelIDs, Map<String, List<String>> categoriesModels) {
        this.categories = categories;
        this.modelIDs = modelIDs;
        this.categoriesModels = categoriesModels;
    }
    
    /**
     * a getter for the repository categories
     * @return tree storing the categories
     */
    public Tree getCategories(){
        return this.categories;
    }
    
    /**
     * a getter for the models IDs
     * @return a map from the models names to the models IDs
     */
    public Map<String, String> getModelIDs(){
        return this.modelIDs;
    }
    
    /**
     * a getter for the categories models map
     * @return a map from a final subcategory to its models
     */
    public Map<String, List<String>> getCategoriesModels(){
        return this.categoriesModels;
    }
    
}
