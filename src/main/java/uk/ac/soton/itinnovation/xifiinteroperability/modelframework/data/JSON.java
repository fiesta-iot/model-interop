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
// Created By : Paul Grace
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data.PathEvaluationResult.DataFormat;

/**
 * Methods for evaluating JSON Data. Utility class.
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public final class JSON {

    /**
     * Utility class. Private constructor.
     */
    private JSON() {
        // empty implementation.
    }

    /**
     * Assert that a JSON document reference (jsonpath expr) evaluates to
     * a given value.
     * @param jsondoc The document to check.
     * @param reference The JSON path expression.
     * @param value The required value.
     * @return PathEvaluationResult with the boolean result and the value of the JSONPath expression
     * @throws InvalidJSONPathException Thrown in case of an invalid JSONPath in a guard.
     */
    public static PathEvaluationResult assertJSON(final String jsondoc,
                        final String reference, final Object value) throws InvalidJSONPathException {
        try {
            final String xprVal = readValue(jsondoc, reference);
            final String jsonVal = ((String) value).toLowerCase(Locale.ENGLISH);
            return new PathEvaluationResult(jsonVal.equalsIgnoreCase(xprVal), xprVal, DataFormat.JSON);
        }
        catch (PathNotFoundException ex) {
            final String jsonVal = ((String) value);
            if(jsonVal.equalsIgnoreCase("null")){
                return new PathEvaluationResult(true, "null", DataFormat.JSON);
            }
            throw new InvalidJSONPathException("JSONPath '" + reference + "' is invalid or does not exist.");
        }
        catch (Exception ex) {
            throw new InvalidJSONPathException("JSONPath '" + reference + "' is invalid or does not exist.");
        }
    }

    /**
     * Compare a JSON document reference (jsonpath expr) against a particular value.
     * LESSTHAN or GREATERTHAN comparisons only, use assertJSON for EQUAL and NOTEQUAL comparisons
     *
     * @param jsondoc The document to check.
     * @param reference The JSON path expression.
     * @param value The required value.
     * @param comparisonType The type of the comparison, GREATERTHAN or LESSTHAN
     * @return PathEvaluationResult with the boolean result and the value of the JSONPath expression
     * @throws InvalidJSONPathException Thrown in case of an invalid JSONPath in a guard.
     */
    public static PathEvaluationResult compareJSON(final String jsondoc,
                        final String reference, final Object value, final Guard.ComparisonType comparisonType)
            throws InvalidJSONPathException {
        try {
            final String xprVal = readValue(jsondoc, reference);
            final String jsonVal = ((String) value);
            if (comparisonType == Guard.ComparisonType.GREATERTHAN){
                try{
                    double a = new Double(xprVal);
                    double b = new Double(jsonVal);
                    return new PathEvaluationResult(a > b, xprVal, DataFormat.JSON);
                } catch(Exception ex) {
                    return new PathEvaluationResult(false, xprVal, DataFormat.JSON);
                }
            }
            else if (comparisonType == Guard.ComparisonType.LESSTHAN){
                try{
                    double a = new Double(xprVal);
                    double b = new Double(jsonVal);
                    return new PathEvaluationResult(a < b, xprVal, DataFormat.JSON);
                } catch(Exception ex) {
                    return new PathEvaluationResult(false, xprVal, DataFormat.JSON);
                }
            }
            return new PathEvaluationResult(false, null, DataFormat.JSON);
        }
        catch (PathNotFoundException ex) {
            throw new InvalidJSONPathException("JSONPath '" + reference + "' is invalid or does not exist.");
        }
        catch (Exception ex) {
            throw new InvalidJSONPathException("JSONPath '" + reference + "' is invalid or does not exist.");
        }
    }

    /**
     * Check if a JSON document reference (jsonpath expr) matches a particular regex.
     * REGEX comparison
     *
     * @param jsondoc the json document to check
     * @param reference the JSON path expression
     * @param value the regular expression to check against
     * @return PathEvaluationResult with the boolean result and the value of the JSONPath expression
     * @throws InvalidJSONPathException Thrown in case of an invalid JSONPath in a guard.
     * @throws InvalidRegexException Thrown in case of an invalid regex
     */
    public static PathEvaluationResult regexJSON(final String jsondoc,
            final String reference, final Object value) throws InvalidJSONPathException, InvalidRegexException {
        try {
            final String xprVal = readValue(jsondoc, reference);
            final String jsonVal = ((String) value);
            boolean boolResult = Pattern.matches(jsonVal, xprVal);
            return new PathEvaluationResult(boolResult, xprVal, DataFormat.JSON);
        }
        catch (PathNotFoundException ex) {
            throw new InvalidJSONPathException("JSONPath '" + reference + "' is invalid or does not exist.");
        }
        catch (PatternSyntaxException ex) {
            throw new InvalidRegexException("There is a regex guard with an invalid regular expression.");
        }
        catch (Exception ex) {
            throw new InvalidJSONPathException("JSONPath '" + reference + "' is invalid or does not exist.");
        }
    }

    /**
     * Check if a JSON document reference (jsonpath expr) contains a particular key field.
     * CONTAINS evaluation
     *
     * @param jsondoc The document to check.
     * @param reference The JSON path expression.
     * @param value The required value.
     * @return PathEvaluationResult with the boolean result and the fields of the JSONPath expression (array list)
     * @throws InvalidJSONPathException Thrown in case of an invalid JSONPath in a guard.
     */
    public static PathEvaluationResult containsJSON(final String jsondoc,
                        final String reference, final Object value)
            throws InvalidJSONPathException {
        try {
            final Object document = Configuration.defaultConfiguration().jsonProvider().parse(jsondoc);
            Map<String, String> childFields = JsonPath.read(document, reference);
            List<String> childFieldsList = new ArrayList<>();

            boolean containsResult = false;
            for (String child: childFields.keySet()){
                childFieldsList.add(child);

                if (child.equalsIgnoreCase(value.toString())){
                    containsResult = true;
                }
            }
            return new PathEvaluationResult(containsResult, childFieldsList, DataFormat.JSON);
        }
        catch (ClassCastException ex) {
            /* if a ClassCastException is thrown, this means that the json evaluation most
            probably returned a list and not a map, hence there are no child fields */
            return new PathEvaluationResult(false, new ArrayList<>(), DataFormat.JSON);
        }
        catch (PathNotFoundException | InvalidJsonException ex) {
            throw new InvalidJSONPathException("JSONPath '" + reference + "' is invalid or does not exist.");
        }
    }

    /**
     * Validate a JSON document against a schema.
     * @param jsonDoc The full json document content as a string.
     * @param schemaIn The full json schema as a string.
     * @return true if the document validates.
     */
    public static boolean validateJSON(final String jsonDoc, final String schemaIn) {

        try {
            final JsonNode fstabSchema = JsonLoader.fromResource(schemaIn);

            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

            final JsonSchema schema = factory.getJsonSchema(fstabSchema);

            schema.validate(fstabSchema);
            return true;
        } catch (ProcessingException ex) {
            ServiceLogger.LOG.error("json input does not comply with schema", ex);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Error reading json data", ex);
        }
        return false;
    }

    /**
     * Read a JSON value from a doc based on a JSON Path expression.
     * @param jsondoc The json content.
     * @param pathexpr The json path expression.
     * @return The data value as a string (Can be typed later).
     */
    public static String readValue(final String jsondoc, final String pathexpr) {
        if (pathexpr.equalsIgnoreCase("*")) {
            return jsondoc;
        }
        return JsonPath.read(jsondoc, pathexpr).toString();
    }

    /**
     * Write a given value into a json doc at the given location by the
     * json path expression.
     * @param jsondoc The json document.
     * @param param The path expression.
     * @param val The value to write.
     * @return The newly edited json document.
     */
    public static String writeValue(final String jsondoc, final String param, final String val) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode jsn = objectMapper.readTree(jsondoc);

            ((ObjectNode) jsn).put(param, val);
            return objectMapper.writeValueAsString(jsn);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Couldn't write JSO value: " + ex.getMessage());
            return null;
        }
    }
}
