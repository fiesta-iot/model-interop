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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * a parser used to parse a categories tree represented in a JSON content
 * 
 * @author ns17
 */
public class CategoriesParser {
    
    // a object container used to store three values
    private static class Triple {
        
        // stores the first object
        private final Object first;
        
        // stores the second object
        private final Object second;
        
        // stored the third object
        private final Object third;
        
        // a constructor for the triple
        public Triple(Object first, Object second, Object third){
            this.first = first;
            this.second = second;
            this.third = third;
        }
        
        // getters for each of the elements in the triple
        public Object getFirst(){
            return this.first;
        }
        
        public Object getSecond(){
            return this.second;
        }
        
        public Object getThird(){
            return this.third;
        }
    }
    
    /**
     * a utility method to create a Triple object
     * @param first the first object in the tuple
     * @param second the second object in the tuple
     * @param third the third object in the tuple
     * @return a Triple object
     */
    private static Triple createTriple(Object first, Object second, Object third){
        return new Triple(first, second, third);
    }
    
    /**
     * a method, which extract the JSON categories tree from the server
     * @param serverUrl the URL of the server for a given repository
     * @return the JSON content representing the categories tree
     * @throws IOException 
     */
    public static String extractJSONcategoriesTree(String serverUrl) throws IOException{
        final URL categoriesUrl = new URL(serverUrl);
        final StringBuilder sb = new StringBuilder();
        
        final HttpURLConnection conn = (HttpURLConnection) categoriesUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        
        br.close();
        conn.disconnect();
        
        return sb.toString().trim();
    }
    
    /**
     * a static method used to parse a JSON formatted categories tree and return the data in a Tree data structure
     * @param categoriesTree the JSON formatted categories tree
     * @return a TreeWrapper object, which is a wrapper of the categories tree and the map linking models names to models IDs
     * @throws JSONException
     */
    public static TreeWrapper parseCategoriesTree(String categoriesTree) throws JSONException{
        
        JSONObject jsonTree = new JSONObject(categoriesTree);
        jsonTree.remove("id");
        jsonTree.remove("_id");
        Iterator rootIterator = jsonTree.keys();
        
        // the iterator must return just one key, which is the root of the tree
        if (rootIterator.hasNext()){
            String root = (String) rootIterator.next();
            Tree<String> categories = new TreeImpl<>(root);
            Map<String, String> modelsIDs = new HashMap<>();
            Map<String, List<String>> categoriesModels = new HashMap<>();
            
            // DFS iteration to parse the json tree
            Triple triple;
            String child;
            String parent = root;
            Stack<Triple> fringe = new Stack<>();
            JSONObject iteratedObject = jsonTree.getJSONObject(root);
            while (true){
                Iterator iterator = iteratedObject.keys();
                
                if (!iterator.hasNext()){
                    if (!categoriesModels.containsKey(parent))
                        categoriesModels.put(parent, new ArrayList<>());
                }
                
                while(iterator.hasNext()){
                    String iteratedKey = (String) iterator.next();
                    try {
                        fringe.add(createTriple(iteratedObject.getJSONObject(iteratedKey), iteratedKey, parent));
                    }
                    catch (JSONException ex){
                        if (categoriesModels.containsKey(parent)){
                           categoriesModels.get(parent).add(iteratedKey);
                        }
                        else {
                            categoriesModels.put(parent, new ArrayList<>());
                            categoriesModels.get(parent).add(iteratedKey);
                        }
                        // No need to add a new node in the tree if it doesn't represent a category
                        // Just store the key value pairs in a map, linking name to id
                        modelsIDs.put(iteratedKey, iteratedObject.getString(iteratedKey));
                    }
                }
                
                // if done iterating break of the loop
                if (fringe.isEmpty()){
                    break;
                }
                
                triple = fringe.pop();
                parent = (String) triple.getThird();
                child = (String) triple.getSecond();
                iteratedObject = (JSONObject) triple.getFirst();
                categories.addChild(child, parent);
                parent = child;
            }
            
            return new TreeWrapper(categories, modelsIDs, categoriesModels);
        }
        // if no root key found throw an exception
        else {
            throw new JSONException("The categories tree doesn't have a root key, hence is invalid.");
        }
    }
}
