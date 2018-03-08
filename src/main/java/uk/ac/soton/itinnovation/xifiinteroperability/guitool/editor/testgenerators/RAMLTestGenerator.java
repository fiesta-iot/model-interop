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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.testgenerators;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.bodies.Response;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;
import org.raml.v2.api.model.v10.resources.Resource;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Function;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Message;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

/**
 * This class represents a test generator used to create an API test from RAML specification
 * 
 * @author ns17
 */
public final class RAMLTestGenerator extends AbstractTestGenerator {

    /**
     * reference to the specification for which a test model is being generated
     */
    private final Api spec;
    
    /**
     * constructs a swagger test generator
     * @param path the path of the API spec for which a test model is being generated
     * @throws InvalidSpecificationException if reading the spec is unsuccessful
     */
    public RAMLTestGenerator(String path) throws InvalidSpecificationException{
        super();
        spec = readSpec(path);

        if (spec == null){
            throw new InvalidSpecificationException("Invalid RAML specification.");
        }
    }

    /**
     * generates the server architecture node
     * @param serverID the id of the server
     * @param model the data model to use (error or spec model)
     * @return true if the server node was successfully built and false otherwise
     */
    @Override
    protected boolean generateServer(String serverID, DataModel model) {
        System.out.println(spec.baseUri().value());
        
        URL url;
        try {
            url = new URL(spec.baseUri().value());
        }
        catch (MalformedURLException ex){
            return false;
        }
        
        final ArchitectureNode archNode = (ArchitectureNode) model.addNode(serverID, XMLStateMachine.INTERFACE_LABEL, XMLStateMachine.INTERFACE_LABEL);
        archNode.setData(XMLStateMachine.INTERFACE_LABEL, url.getHost());
        
        if (url.getPath() != null && !url.getPath().isEmpty()){
            archNode.addInterfaceData("rest", "https://" + url.getHost() + ":443" + url.getPath(), "http");
        }
        else {
            archNode.addInterfaceData("rest", "https://" + url.getHost() + ":443/", "http");
        }
        
        baseUrl = XMLStateMachine.COMPONENT_LABEL + ".interface.rest";
        
        return true;
    }

    /**
     * generates a series of trigger events to test the API methods
     * @return true if the events were successfully generated and false otherwise
     */
    @Override
    protected boolean generateTriggeredEvents() {
        if (baseUrl == null){
            return false;
        }
        
        // first, get all the resources using recursion and merge them together in a list, so that the trigger events can be generated after that
        List<Resource> allResources = new ArrayList<>();
        for (Resource resource: spec.resources()){
            allResources.add(resource);
            generateChildResources (resource, allResources);
        }
        
        // generate the first trigger state ID
        String triggerID = this.getNextID();
        String lastErrorTriggerID = triggerID;
        String lastPatternTriggerID = triggerID;
        specModel.addNode(triggerID, this.getNextLabel(), XMLStateMachine.TRIGGERSTART_LABEL);
        errorModel.addNode(triggerID, this.getNextLabel(), XMLStateMachine.TRIGGERSTART_LABEL);
        patternModel.addNode(triggerID, this.getNextLabel(), XMLStateMachine.TRIGGERSTART_LABEL);
        
        
        // generate test events for all resources
        final Set<String> availablePaths = new HashSet<>();
        int i, j;
        i = 0;
        Map<String, Method> allMethods;
        List<Resource> patternResources = new ArrayList<>(); // this will store the resources that will be used for REST pattern testing
        for(Resource resource: allResources){
            i += 1;
            availablePaths.add(resource.resourcePath());
            // check if all methods are defined (GET, POST, PUT, DELETE) and if so, generate test patterns
            allMethods = allDefined(resource);
            if (allMethods != null) {
               patternResources.add(resource);
            }
            
            boolean getDefined = false;
            boolean postDefined = false;
            j = 0;
            for (Method method : resource.methods()) {
                j += 1;
                if (method.method().equalsIgnoreCase("GET")) {
                    getDefined = true;
                } else if (method.method().equalsIgnoreCase("POST")) {
                    postDefined = true;
                }
                triggerID = generateEvent(resource, method, triggerID, i == allResources.size() && j == resource.methods().size());
            }

            if (getDefined && !postDefined) {
                lastErrorTriggerID = generateErrorEvent(resource.resourcePath(), "POST", "XML", lastErrorTriggerID, "405", false); // use default XML for data type - it shouldn't matter
            }
            
        }
        
        // generate all REST pattern tests
        i = 0;
        for (Resource resource : patternResources) {
            i += 1;
            allMethods = allDefined(resource);
            // typical REST pattern (GET-404, POST-create, GET-created, PUT-update, GET-updated, DELETE - delete, GET - 404)
            lastPatternTriggerID = generateErrorEvent(resource.resourcePath(), "GET", "XML", lastPatternTriggerID, "404", false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("POST"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("GET"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("PUT"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("GET"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("DELETE"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateErrorEvent(resource.resourcePath(), "GET", "XML", lastPatternTriggerID, "404", false, patternModel);

            // same pattern as the above but PUT-only  
            lastPatternTriggerID = generateErrorEvent(resource.resourcePath(), "GET", "XML", lastPatternTriggerID, "404", false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("PUT"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("GET"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("PUT"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("GET"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateEvent(resource, allMethods.get("DELETE"), lastPatternTriggerID, false, patternModel);
            lastPatternTriggerID = generateErrorEvent(resource.resourcePath(), "GET", "XML", lastPatternTriggerID, "404", i == patternResources.size(), patternModel);
        }
        
        // generate a non-existing method test
        String errorPath = "/error-path";
        while (availablePaths.contains(errorPath)){
            errorPath += errorPath;
        }
        generateErrorEvent(errorPath, "GET", "OTHER", lastErrorTriggerID, "404", true);
        
        return true;
    }
    
    /**
     * generates and collects all resources using recursion
     * 
     * @param resource the current resource
     * @param allResources the list with previously collected resources
     */
    private void generateChildResources (Resource resource, List<Resource> allResources) {
        for (Resource childResource : resource.resources()){
            allResources.add(childResource);
            generateChildResources(childResource, allResources);
        }
    }
        
    /**
     * generates events to test an API method, last argument assumed to be false
     * @param resource the resource object
     * @param method the method object
     * @param lastTriggerID the last trigger ID used in the model
     * @return the ID of the last node (end or trigger node)
     */
    private String generateEvent(Resource resource, Method method, String lastTriggerID){
        return this.generateEvent(resource, method, lastTriggerID, false);
    }
    
    /**
     * generates events to test an API method
     * @param resource the resource object
     * @param method the method object
     * @param lastTriggerID the last trigger ID used in the model
     * @param isLastEvent true if this will be the last API method to test
     * @return the ID of the last node (end or trigger node)
     */
    private String generateEvent (Resource resource, Method method, String lastTriggerID, boolean isLastEvent){
        return this.generateEvent(resource, method, lastTriggerID, isLastEvent, specModel);
    }
    
    /**
     * generates events to test an API method
     * @param resource the resource object
     * @param method the method object
     * @param lastTriggerID the last trigger ID used in the model
     * @param isLastEvent true if this will be the last API method to test
     * @param model the model to use
     * @return the ID of the last node (end or trigger node)
     */
    private String generateEvent(Resource resource, Method method, String lastTriggerID, boolean isLastEvent, DataModel model){
        String path = resource.resourcePath();
        if (path.startsWith("/")){
            path = path.replaceFirst("/", "");
        }
        
        // add the query parameters to the path
        if (path.endsWith("/")){
            path = path.substring(0, path.length()-1);
        }
        boolean queryAdded = false;
        if (method.queryParameters() != null){
            for (TypeDeclaration query : method.queryParameters()){
                if (queryAdded){
                    path += "&" + query.name() + "={query}";
                }
                else {
                    path += "?" + query.name() + "={query}";
                    queryAdded = true;
                }
            }
        }
        
        // add the normal node 
        final String normalID = this.getNextID();
        model.addNode(normalID, this.getNextLabel(), XMLStateMachine.NORMAL_LABEL);
        
        // check for a request body schema
        String requestBody = "";
        try {
            if (method.method().equalsIgnoreCase("PUT") || method.method().equalsIgnoreCase("POST")){
                requestBody = method.body().get(0).example().value();
            }
            else {
                requestBody = "";
            }
        }
        catch (NullPointerException ex){
            requestBody = "";
        }
        
        // add the connection between the trigger node and the normal node
        final String triggerTransitionID = this.getNextID();
        final Message trigger = (Message) model.addConnection(triggerTransitionID, lastTriggerID, normalID);
        if (method.body() == null || method.body().isEmpty()){
            trigger.updateMessage(baseUrl, path, method.method(), "OTHER", requestBody);
        }
        else if (method.body().get(0).displayName().value().contains("xml")){
            trigger.updateMessage(baseUrl, path, method.method(), "XML", requestBody);
            trigger.addHeader("Content-Type", "application/xml");
        }
        else if (method.body().get(0).displayName().value().contains("json")){
            trigger.updateMessage(baseUrl, path, method.method(), "JSON", requestBody);
            trigger.addHeader("Content-Type", "application/json");
        }
        else {
            trigger.updateMessage(baseUrl, path, method.method(), "OTHER", requestBody);
        }
        
         // add the end node, this could be an end node if the model is built, or trigger node which continues with the model
        final String endID = this.getNextID();
        if (isLastEvent){
            final GraphNode endNode = (GraphNode) model.addNode(endID, this.getNextLabel(), XMLStateMachine.END_LABEL);
            endNode.addEndStateData(true, "");
        }
        else {
            model.addNode(endID, this.getNextLabel(), XMLStateMachine.TRIGGER_LABEL);
        }
        
        String guardTransitionID;
        Guard guard;
        for (Response response: method.responses()){
            guardTransitionID = this.getNextID();
            guard = (Guard) model.addConnection(guardTransitionID, normalID, endID);
            guard.addGuard(Function.FunctionType.Equals, "http.from", "component." + XMLStateMachine.INTERFACE_LABEL + ".address");
            guard.addGuard(Function.FunctionType.Equals, "http.code", response.code().value());
            if (response.body() != null && !response.body().isEmpty()){
                if (response.body().get(0).displayName().value().contains("xml")){
                    guard.addGuard(Function.FunctionType.Regex, "http.content-type", ".*application\\/xml.*");
                    
                    // add contains guard if there is an example of the response content
                    try {
                        JSONObject json = new JSONObject(response.body().get(0).example().value());
                        Iterator iterator = json.keys();
                        while (iterator.hasNext()){
                            guard.addGuard(Function.FunctionType.Contains, "content[/*]", (String) iterator.next());
                        }
                    }
                    catch (NullPointerException | JSONException ex){
                        // if an exception is thrown either no example provided or schema is invalid, hence skipp this part
                    }
                }
                else if (response.body().get(0).displayName().value().contains("json")){
                    guard.addGuard(Function.FunctionType.Regex, "http.content-type", ".*application\\/json.*");
                    
                    // add contains guard if there is an example of the response content
                    try {
                        JSONObject json = new JSONObject(response.body().get(0).example().value());
                        Iterator iterator = json.keys();
                        while (iterator.hasNext()){
                            guard.addGuard(Function.FunctionType.Contains, "content[$]", (String) iterator.next());
                        }
                    }
                    catch (NullPointerException | JSONException ex){
                        // if an exception is thrown either no example provided or schema is invalid, hence skipp this part
                    }
                }
            }
            guard.addGuard(Function.FunctionType.LessThan, "response-time", "2000");
        }
        
        return endID;
    }

    /**
     * a method to check if all methods are defined in a resource (GET, PUT, POST, DELETE)
     * @param resource the resource to check
     * @return a list of all the method objects if all four are defined and null otherwise
     */
    private Map<String, Method> allDefined (Resource resource){
        Map<String, Method> defined = new HashMap<>();
        for (Method method : resource.methods()){
            if (method.method().equalsIgnoreCase("GET") || method.method().equalsIgnoreCase("PUT") 
                    || method.method().equalsIgnoreCase("POST") || method.method().equalsIgnoreCase("DELETE")){
                if (!defined.keySet().contains(method.method())){
                    defined.put(method.method().toUpperCase(), method);
                }
            }
        }
        
        return defined.size() == 4 ? defined : null;
    }
    
    /**
     * reads a RAML API specification and parses it to a java object
     * 
     * @param path the local path or URL to the RAML spec
     * @return a java object representing the parsed RAML specification
     */
    @Override
    public Api readSpec(String path) {
        RamlModelResult ramlModelResult = new RamlModelBuilder().buildApi(path);
        if (ramlModelResult.hasErrors()) {
            ramlModelResult.getValidationResults().forEach((validationResult) -> {
                System.out.println(validationResult.getMessage());
            });
            return null;
        } 
        else {
            Api api = ramlModelResult.getApiV10();
            return api;
        }
    }
}
