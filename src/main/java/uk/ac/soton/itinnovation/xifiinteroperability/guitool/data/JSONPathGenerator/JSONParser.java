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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * a JSONParser class used to parse a JSON tree created by the jackson library into a
 * custom made JSON Tree model used by the JSONPathGenerator
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class JSONParser {

    /**
     * a static method, which parses a JSON string to the custom JSON tree model using the jackson tree model
     * @param json the json string to parse
     * @return the root node of the custom JSON tree model - the node with '$' name
     * @throws InvalidJSONException in case of invalid json string
     */
    public final static JSONTreeNode parseJSON(String json) throws InvalidJSONException {
        JSONTreeNode root = JSONTreeNode.createNode(null, null, null, JSONTreeNode.NodeType.ROOT_NODE, null);
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(json);
            parseObject(root, rootNode);
        }
        catch (IOException ex){
            throw new InvalidJSONException("The parser cannot continue since the JSON string is invalid");
        }
        return root;
    }

    /**
     * a method, which tries to parse a string value to integer, double, boolean or null
     * if not successful, the value should be treated as a string
     * @param value the string value to parse
     * @return the parsed object
     */
    private static Object parseValue(String value) {
        try {
           return Integer.parseInt(value);
        }
        catch (NumberFormatException ex){
            try {
                return Double.parseDouble(value);
            }
            catch (NumberFormatException | NullPointerException exc){
                if (value == null || value.equalsIgnoreCase("null")){
                    return null;
                }
                else if (value.equalsIgnoreCase("true")){
                    return true;
                }
                else if (value.equalsIgnoreCase("false")){
                    return false;
                }
                else return value;
            }
        }
    }

    /**
     * a method used to traverse a json list and parse its values to the custom JSON tree model
     * @param parent the parent list node
     * @param list the jackson tree model list node
     */
    private static void parseList(JSONTreeNode parent, JsonNode list){
        Iterator<JsonNode> iterator = list.elements();
        JsonNode node;
        int index = 0;
        while (iterator.hasNext()){
            node = iterator.next();
            if (node.getNodeType() == JsonNodeType.ARRAY){
                JSONTreeNode arrayNode = JSONTreeNode.createNode(parent, null, null, JSONTreeNode.NodeType.ARRAY_NODE, index);
                parseList(arrayNode, node);
            }
            else if (node.getNodeType() == JsonNodeType.OBJECT){
                JSONTreeNode objectNode = JSONTreeNode.createNode(parent, null, null, JSONTreeNode.NodeType.OBJECT_NODE, index);
                parseObject(objectNode, node);
            }
            else {
                Object value;
                value = parseValue(node.asText());
                JSONTreeNode.createNode(parent, null, value, JSONTreeNode.NodeType.LEAF_NODE, index);
            }
            index += 1;
        }
    }

    /**
     * a method used to traverse a json object and parse its values to the custom JSON tree model
     * @param parent the parent object node
     * @param object the jackson tree model object node
     */
    private static void parseObject(JSONTreeNode parent, JsonNode object){
        Iterator<Map.Entry<String, JsonNode>> iterator = object.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            if (entry.getValue().getNodeType() == JsonNodeType.ARRAY) {
                JSONTreeNode arrayNode = JSONTreeNode.createNode(parent, entry.getKey(), null, JSONTreeNode.NodeType.ARRAY_NODE, null);
                parseList(arrayNode, entry.getValue());
            }
            else if (entry.getValue().getNodeType() == JsonNodeType.OBJECT) {
                JSONTreeNode objectNode = JSONTreeNode.createNode(parent, entry.getKey(), null, JSONTreeNode.NodeType.OBJECT_NODE, null);
                parseObject(objectNode, entry.getValue());
            }
            else {
                Object value;
                value = parseValue(entry.getValue().asText());
                JSONTreeNode.createNode(parent, entry.getKey(), value, JSONTreeNode.NodeType.LEAF_NODE, null);
            }
        }
    }
}
