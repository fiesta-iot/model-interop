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
// Created By : Nikolay Stanchev
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the execution manager, which handles the remembering of values 
 * from previous tests while doing a multiple tests execution
 * 
 * @author ns17
 */
public final class MultiTestsExecutionManager {
    
    /**
     * a multilayer map linking test name to state label to header name to header value 
     */
    private Map<String, Map<String, Map<String, String>>> testsHeaders;
    
    /**
     * an accessor method for the tests headers triple layer map, retrieves a header value
     * @param testName the name of the test this header value refers to
     * @param stateLabel the name of the state where this header has been captured
     * @param headerName the header name 
     * @return the header value
     */
    public synchronized String getTestHeader(String testName, String stateLabel, String headerName){
        testName = testName.replace(".xml", "");
 
        // get the value associated with the tree values
        try {
            return testsHeaders.get(testName).get(stateLabel).get(headerName);
        }
        // in case of a null pointer exception, then this triple layer key is not associated to any value, return null
        catch (NullPointerException ex){
            return null;
        }
    }
    
    /**
     * a mutator method for putting a header value in the tests headers triple layer map
     * @param testName the name of the test this header value refers to
     * @param stateLabel the name of the state where this header has been captured
     * @param headerName the name of header to insert
     * @param headerValue the header value to insert
     */
    public synchronized void putTestHeader(String testName, String stateLabel, String headerName, String headerValue){
        testName = testName.replace(".xml", "");
        
        // check if a map for this test has been created, if not create it
        if (testsHeaders.get(testName) == null){
            testsHeaders.put(testName, new HashMap<>());
        }
        
        // check if a map for this test and this state has been created, if not create it
        if (testsHeaders.get(testName).get(stateLabel) == null){
            testsHeaders.get(testName).put(stateLabel, new HashMap<>());
        }
        
        // put the header name linked to the header value in the triple layer map
        testsHeaders.get(testName).get(stateLabel).put(headerName, headerValue);
    }
    
    /**
     * A method, which removes all references to a given test in the headers map
     * @param testName the test name to remove
     */
    public void removeAllTestHeaders (String testName){
        testName = testName.replace(".xml", "");
        
        // all mappings of header values to this test are deleted
        testsHeaders.remove(testName);
    }
    
    /**
     * A method, which removes all references to a given state label in a given test in the headers map
     * @param testName the test name
     * @param stateLabel the state label to remove
     */
    public void removeAllStateHeaders (String testName, String stateLabel){
        testName = testName.replace(".xml", "");
        
        if (testsHeaders.get(testName) != null){
            testsHeaders.get(testName).remove(stateLabel);
        }
    }
    
    /**
     * A method which removes references to a given headerID in a state in a given test in the headers map
     * @param testName the test name
     * @param stateLabel the state label
     * @param headerID the header ID to remove
     */
    public void removeHeaderID (String testName, String stateLabel, String headerID){
        testName = testName.replace(".xml", "");
        
        if (testsHeaders.get(testName) != null){
            if (testsHeaders.get(testName).get(stateLabel) != null){
                testsHeaders.get(testName).get(stateLabel).remove(headerID);
            }
        }
    }
    
    /**
     * a multilayer map linking test name to state label to captured content
     */
    private Map<String, Map<String, String>> testsContent;
    
    /**
     * an accessor method for the tests content two-layer map, retrieves event content
     * @param testName the name of the test this content refers to
     * @param stateLabel the name of the state where this content has been captured
     * @return the content associated with the given state label within the given test
     */
    public synchronized String getTestContent(String testName, String stateLabel){
        testName = testName.replace(".xml", "");
        
        // try retrieving the content
        try {
            return testsContent.get(testName).get(stateLabel);
        }
        // in case of a null pointer exception the test name and the state label are not associated with any content
        catch (NullPointerException ex){
            return null;
        }
    }
    
    /**
     * a mutator method for putting content in the tests content two-layer map
     * @param testName the test name the content refers to
     * @param stateLabel the name of the state where this content has been captured
     * @param content the content to be inserted
     */
    public synchronized void putTestContent(String testName, String stateLabel, String content){
        testName = testName.replace(".xml", "");
                
        // check if a map associated to the given test name has already been created, if not create it
        if (testsContent.get(testName) == null){
            testsContent.put(testName, new HashMap<>());
        }
        
        // link the test name and the state label with the given content
        testsContent.get(testName).put(stateLabel, content);       
    }
    
    /**
     * A method, which removes all references to a given test in the content map
     * @param testName the test name to remove
     */
    public void removeAllTestContent (String testName){
        testName = testName.replace(".xml", "");
        
        // all mappings of content to this test are deleted
        testsContent.remove(testName);
    }
    
    /**
     * A method, which removes all references to a given state label in a given test in the content map
     * @param testName the test name
     * @param stateLabel the state label to remove
     */
    public void removeAllStateContent (String testName, String stateLabel){
        testName = testName.replace(".xml", "");
        
        if (testsContent.get(testName) != null){
            testsContent.get(testName).remove(stateLabel);
        }
    }
    
    /**
     * a map linking test name to map of pattern values for this test
     */
    private Map<String, Map<String, String>> testsPatternValues;
    
    /**
     * an accessor method for the tests pattern values two-layer map, retrieves test pattern value
     * @param testName the name of the test this pattern value refers to
     * @param patternValueID the pattern value identifier
     * @return the pattern value 
     */
    public synchronized String getPatternValue(String testName, String patternValueID){
        testName = testName.replace(".xml", "");
        
        // try retrieving the associated pattern value with the given test name and pattern value identifier
        try {
            return testsPatternValues.get(testName).get(patternValueID);
        }
        // in case of a null pointer exception, the test name and the identifier are not associated to any pattern value, hence return null
        catch (NullPointerException ex){
            return null;
        }
    }
    
    /**
     * a mutator method for putting a pattern value in the tests pattern values two-layer map
     * @param testName the test name the pattern value refers to
     * @param patternValueID the identifier of the pattern value to insert
     * @param patternValue the actual pattern value to insert
     */
    public synchronized void putPatternValue(String testName, String patternValueID, String patternValue){
        testName = testName.replace(".xml", "");
        
        // if the given test name is not associated to anything, create a hash map and link it to the test name
        if (testsPatternValues.get(testName) == null){
            testsPatternValues.put(testName, new HashMap<>());
        }
        
        // link the given test name and pattern value identifier to the pattern value
        testsPatternValues.get(testName).put(patternValueID, patternValue);
    }
    
    /**
     * A method, which removes all references to a given test in the pattern values map
     * @param testName the test name to remove
     */
    public void removeAllTestPatternValues (String testName){
        testName = testName.replace(".xml", "");
        
        // all mappings of pattern values to this test are deleted
        testsPatternValues.remove(testName);
    }
    
    /**
     * A method which removes references to a given pattern value ID in a given test in the pattern values map
     * @param testName the test name
     * @param patternValueID the pattern value ID to remove
     */
    public void removePatternValueID (String testName, String patternValueID){
        testName = testName.replace(".xml", "");
        
        if (testsPatternValues.get(testName) != null){
            testsPatternValues.get(testName).remove(patternValueID);
        }
    }
    
    /**
     * a constructor for the execution manager
     */
    public MultiTestsExecutionManager(){
        this.resetMaps();
    }
    
    // resets the multilayer maps by creating new maps
    public synchronized void resetMaps(){
        testsHeaders = new HashMap<>();
        testsContent = new HashMap<>();
        testsPatternValues = new HashMap<>();
    }
}
