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

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.parser.SwaggerParser;
import java.util.ArrayList;
import java.util.List;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Function;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Function.FunctionType;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.Message;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

/**
 * This class represents a test generator used to create an API test from swagger specification
 * 
 * @author ns17
 */
public final class SwaggerTestGenerator extends AbstractTestGenerator {
    
    /**
     * reference to the specification for which a test model is being generated
     */
    private final Swagger spec;
    
    /**
     * constructs a swagger test generator
     * @param path the path of the API spec for which a test model is being generated
     * @throws InvalidSpecificationException if reading the spec is unsuccessful
     */
    public SwaggerTestGenerator(String path) throws InvalidSpecificationException{
        super();
        spec = readSpec(path);
        
        if (spec == null){
            throw new InvalidSpecificationException("Invalid Swagger specification.");
        }
    }
    
    /**
     * generates the server architecture node
     * @param serverID the id of the server
     * @param model the data model to use (error or spec model)
     * @return true if the server node was successfully built and false otherwise
     */
    @Override
    protected boolean generateServer(String serverID, DataModel model){
        if (spec.getHost() == null || spec.getHost().isEmpty()){
            return false;
        }
        
        final ArchitectureNode archNode = (ArchitectureNode) model.addNode(serverID, XMLStateMachine.INTERFACE_LABEL, XMLStateMachine.INTERFACE_LABEL);
        archNode.setData(XMLStateMachine.INTERFACE_LABEL, spec.getHost());
        
        // default scheme is https
        if (spec.getSchemes() == null || spec.getSchemes().isEmpty()){
            archNode.addInterfaceData("base1", "https://" + spec.getHost() + ":443" + spec.getBasePath(), "http");
            baseUrl = XMLStateMachine.COMPONENT_LABEL + ".interface.base1";
        }
        else{
            String scheme;
            final String id = "base";
            String httpsID = null;
            for (int i = 0; i < spec.getSchemes().size(); i++){
                scheme = spec.getSchemes().get(i).toValue();
                if (httpsID == null && scheme.equalsIgnoreCase("https")){
                    httpsID = Integer.toString(i+1);
                }
                if (spec.getBasePath() != null && !spec.getBasePath().isEmpty()){
                    archNode.addInterfaceData(id + (i+1), scheme + "://" + spec.getHost() + ":443" + spec.getBasePath(), "http");
                }
                else {
                    archNode.addInterfaceData(id + (i+1), scheme + "://" + spec.getHost() + ":443/", "http");
                }
            }

            if (httpsID == null){
                baseUrl = XMLStateMachine.COMPONENT_LABEL + ".interface.base1";
            }
            else{
                baseUrl = XMLStateMachine.COMPONENT_LABEL + ".interface.base" + httpsID;
            }
        }
        
        return true;
    }
    
    /**
     * generates a series of trigger events to test the API methods
     * @return true if the events were successfully generated and false otherwise
     */
    @Override
    protected boolean generateTriggeredEvents(){
        if (baseUrl == null){
            return false;
        }
        
        String triggerID = this.getNextID();
        String lastErrorTriggerID = triggerID;
        String lastPatternTriggerID = triggerID;
        specModel.addNode(triggerID, this.getNextLabel(), XMLStateMachine.TRIGGERSTART_LABEL);
        errorModel.addNode(triggerID, this.getNextLabel(), XMLStateMachine.TRIGGERSTART_LABEL);
        patternModel.addNode(triggerID, this.getNextLabel(), XMLStateMachine.TRIGGERSTART_LABEL);
        
        int i = 0;
        Path pathObject;
        List<String> patternResources = new ArrayList<>(); // this list is used to store all paths that will be used for generating rest pattern tests
        if (spec.getPaths() != null){
            for(String path : spec.getPaths().keySet()){
                pathObject = spec.getPaths().get(path);
                i += 1;

                // if get is defined, but there is no post for the url, check for a correct error handling when trying to POST 
                if (pathObject.getGet() != null && pathObject.getPost() == null){
                    lastErrorTriggerID = this.generateErrorEvent(path, "POST", "XML", lastErrorTriggerID, "405", false); // use default XML for data type - it shouldn't matter
                }

                if (pathObject.getGet() != null && pathObject.getPost() != null && pathObject.getPut() != null && pathObject.getDelete() != null){
                    patternResources.add(path);
                }
                
                // check for a get operation
                if (pathObject.getGet() != null) {
                    triggerID = this.generateEvent(path, pathObject.getGet(), "GET", triggerID,
                            i == spec.getPaths().size() && pathObject.getPost() == null
                            && pathObject.getPut() == null && pathObject.getDelete() == null);
                }

                // check for a post operation
                if (pathObject.getPost() != null) {
                    triggerID = this.generateEvent(path, pathObject.getPost(), "POST", triggerID,
                            i == spec.getPaths().size() && pathObject.getPut() == null && pathObject.getDelete() == null);
                }

                // check for a put operation
                if (pathObject.getPut() != null) {
                    triggerID = this.generateEvent(path, pathObject.getPut(), "PUT", triggerID,
                            i == spec.getPaths().size() && pathObject.getDelete() == null);
                }

                // check for a delete operation
                if (pathObject.getDelete() != null) {
                    triggerID = this.generateEvent(path, pathObject.getDelete(), "DELETE", triggerID, i == spec.getPaths().size());
                }
            }
                    
            // generate all REST pattern tests
            i = 0;
            for (String path: patternResources){
                i += 1;
                pathObject = spec.getPaths().get(path);
                // if all four methods are defined, generate a REST pattern test, GET, POST, GET, PUT, GET, DELETE, GET
                    lastPatternTriggerID = this.generateErrorEvent(path, "GET", "XML", lastPatternTriggerID, "404", false, patternModel); // test error for not created resource
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getPost(), "POST", lastPatternTriggerID, false, patternModel); // test create method for resources 
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getGet(), "GET", lastPatternTriggerID, false, patternModel); // test resource is created
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getPut(), "PUT", lastPatternTriggerID, false, patternModel); // test update method for resources
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getGet(), "GET", lastPatternTriggerID, false, patternModel); // test resource is updated
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getDelete(), "DELETE", lastPatternTriggerID, false, patternModel); // test delete method for resources
                    lastPatternTriggerID = this.generateErrorEvent(path, "GET", "XML", lastPatternTriggerID, "404", false, patternModel); // test resource is deleted
                    
                    // next, generate put only pattern
                    lastPatternTriggerID = this.generateErrorEvent(path, "GET", "XML", lastPatternTriggerID, "404", false, patternModel); // test error for nor created resource
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getPut(), "PUT", lastPatternTriggerID, false, patternModel); // test create method for resources 
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getGet(), "GET", lastPatternTriggerID, false, patternModel); // test resource is created
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getPut(), "PUT", lastPatternTriggerID, false, patternModel); // test update method for resources
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getGet(), "GET", lastPatternTriggerID, false, patternModel); // test resource is updated
                    lastPatternTriggerID = this.generateEvent(path, pathObject.getDelete(), "DELETE", lastPatternTriggerID, false, patternModel); // test delete method for resources
                    lastPatternTriggerID = this.generateErrorEvent(path, "GET", "XML", lastPatternTriggerID, "404", i == patternResources.size(), patternModel); // test resource is deleted
            }
        }
        
        // find a path that is not defined in the API and test it
        String errorPath = "/error-path";
        if (spec.getPaths() != null){
            while (spec.getPaths().keySet().contains(errorPath)){
                errorPath += errorPath;
            }
        }
        this.generateErrorEvent(errorPath, "GET", "XML", lastErrorTriggerID, "404", true);
        
        return true;
    }
    
    /**
     * generate the nodes for testing a single API method
     * @param path the path to test
     * @param operation the operation object to test
     * @param method the method being used, GET, POST, etc.
     * @param lastTriggerID the id of the last trigger state that was generated
     * @param isLastEvent true if this is the last event to be generated and false otherwise
     * @param model the model to use
     * @return the ID of the last graph node that was generated
     */
    private String generateEvent(String path, Operation operation, String method, String lastTriggerID, boolean isLastEvent){
        return generateEvent(path, operation, method, lastTriggerID, isLastEvent, specModel);
    }
    
    /**
     * generate the nodes for testing a single API method
     * @param path the path to test
     * @param operation the operation object to test
     * @param method the method being used, GET, POST, etc.
     * @param lastTriggerID the id of the last trigger state that was generated
     * @param isLastEvent true if this is the last event to be generated and false otherwise
     * @param model the model to use
     * @return the ID of the last graph node that was generated
     */
    private String generateEvent(String path, Operation operation, String method, String lastTriggerID, boolean isLastEvent, DataModel model){
        if (path.startsWith("/")){
            path = path.replaceFirst("/", "");
        }
        
        // build the path along with the query parameters, and find body parameter if any
        boolean queryAdded = false;
        BodyParameter param = null;
        for(Parameter par: operation.getParameters()){
            if (par.getRequired() && par.getIn().equalsIgnoreCase("query")){
                if (queryAdded){
                    path = path + "&" + par.getName() + "={queryValue}";
                }
                else {
                    path = path + "?" + par.getName() + "={queryValue}";
                    queryAdded = true;
                }
            }
            else if (par.getIn().equalsIgnoreCase("body") && param == null){
                param = (BodyParameter) par; // convert the parameter to body parameter
            }
        }
        
        // chech if there is a body to be build, (PUT, POST)
        String jsonRequestBody="";
        String xmlRequestBody = "";
        if (method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("POST")) {
            if (param != null) {
                try {
                    jsonRequestBody = "{";
                    xmlRequestBody = "<?xml version='1.0' encoding='UTF-8'?>\n";
                    xmlRequestBody += "<" + param.getName() + ">\n";
                    // if properties not empty, build a json object with these properties
                    for (String prop : param.getSchema().getProperties().keySet()) {
                        jsonRequestBody += "\n'" + prop + "' : 'value',";
                        xmlRequestBody += "<" + prop + ">value</" + prop + ">\n";
                    }
                    
                    // add  closing tags to xml request body
                    xmlRequestBody += "</" + param.getName() + ">";
                    // drop the last added comma of the json body
                    if (jsonRequestBody.endsWith(",")) {
                        jsonRequestBody = jsonRequestBody.substring(0, jsonRequestBody.length() - 1);
                    }
                    jsonRequestBody += "\n}";
                } catch (NullPointerException ex) {
                    jsonRequestBody = "";
                    xmlRequestBody = "";
                }
            }
        }
        
        // add the normal node 
        final String normalID = this.getNextID();
        model.addNode(normalID, this.getNextLabel(), XMLStateMachine.NORMAL_LABEL);
        
        // add the connection between the trigger node and the normal node
        final String triggerTransitionID = this.getNextID();
        final Message trigger = (Message) model.addConnection(triggerTransitionID, lastTriggerID, normalID);   
        if (operation.getConsumes() == null || operation.getConsumes().isEmpty()){
            trigger.updateMessage(baseUrl, path, method, "OTHER", "");
        }
        else if (operation.getConsumes().get(0).contains("xml")){
            trigger.updateMessage(baseUrl, path, method, "XML", xmlRequestBody);
            trigger.addHeader("Content-Type", "application/xml");
        }
        else if (operation.getConsumes().get(0).contains("json")){
            trigger.updateMessage(baseUrl, path, method, "JSON", jsonRequestBody);
            trigger.addHeader("Content-Type", "application/json");
        }
        else {
            trigger.updateMessage(baseUrl, path, method, "OTHER", "");
        }
        
        // build the list of headers
        operation.getParameters().forEach((par) -> {
            if (par.getRequired() && par.getIn().equalsIgnoreCase("header")){
                trigger.addHeader(par.getName(), "{headerValue}");
            }
            else if (par.getRequired() && par.getIn().equalsIgnoreCase("body")){
                trigger.updateMessage(trigger.getEndpoint(), trigger.getPath(), trigger.getHTTPMethod(), trigger.getDataType(), "{contentParameter}");
            }
        });
        
        // add the end node, this could be an end node if the model is built, or trigger node which continues with the model
        final String endID = this.getNextID();
        if (isLastEvent){
            final GraphNode endNode = (GraphNode) model.addNode(endID, this.getNextLabel(), XMLStateMachine.END_LABEL);
            endNode.addEndStateData(true, "");
        }
        else {
            model.addNode(endID, this.getNextLabel(), XMLStateMachine.TRIGGER_LABEL);
        }
        
        // add the guard connection or connections if multiple responses available        
        String guardTransitionID;
        Guard guard;
        Response response;
        for (String key : operation.getResponses().keySet()){
            response = operation.getResponses().get(key);
            guardTransitionID = this.getNextID();
            guard = (Guard) model.addConnection(guardTransitionID, normalID, endID);
            guard.addGuard(Function.FunctionType.Equals, "http.from", "component." + XMLStateMachine.INTERFACE_LABEL + ".address");
            guard.addGuard(Function.FunctionType.Equals, "http.code", key);
            guard.addGuard(Function.FunctionType.LessThan, "response-time", "2000");
           
            String dataType = "";
            // add content-type header
            if (operation.getProduces() != null && !operation.getProduces().isEmpty()){
                if (operation.getProduces().get(0).contains("xml")){
                    guard.addGuard(Function.FunctionType.Regex, "http.content-type", ".*application\\/xml.*");
                    dataType = "XML";
                }
                else if (operation.getProduces().get(0).contains("json")){
                    guard.addGuard(Function.FunctionType.Regex, "http.content-type", ".*application\\/json.*");
                    dataType = "JSON";
                }
            }

            // add Contains guards for the response body
            if (response.getSchema() != null && response.getSchema().getType().equalsIgnoreCase("object")){
                for (String prop : ((ObjectProperty) response.getSchema()).getProperties().keySet()){
                    if (dataType.equalsIgnoreCase("XML")){
                        guard.addGuard(FunctionType.Contains, "content[/*]", prop);
                    }
                    else if (dataType.equalsIgnoreCase("JSON")) {
                        guard.addGuard(FunctionType.Contains, "content[$]", prop);
                    }
                }
            }
            
            // check for expected response headers
            if (response.getHeaders() != null){
                for (String headerKey: response.getHeaders().keySet()){
                    guard.addGuard(Function.FunctionType.Equals, headerKey, "{headerValue}");
                }
            }
        }
        
        return endID;
    }
    
    /**
     * reads a swagger API specification and parses it to a java object
     * 
     * @param path the local path or URL to the swagger spec
     * @return a java object representing the parsed swagger specification
     */
    @Override
    public Swagger readSpec(String path){
        return new SwaggerParser().read(path);
    }
}
