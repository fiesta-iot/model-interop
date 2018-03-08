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

package uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data;

/**
 * This class is used when evaluating XPaths or JSONPaths to encapsulate both the
 * boolean result and the path expression value in a single class
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class PathEvaluationResult {

    /**
     * Enumeration of 2 possible types for data format: XML or JSON
     */
    public enum DataFormat {

        XML(0), JSON(1);

        /**
         * The integer id of the type (specified above).
         */
	private final int type;

        /**
         * Private constructor.
         * @param type The type to data format.
         */
	private DataFormat(final int type) {
	    this.type = type;
	}
    }

    /**
     * The boolean result of the evaluation.
     */
    final private boolean result;

    /**
     * The value returned from parsing the XPath or JSONPath expression
     */
    final private Object exprValue;

    /**
     * The data format used in the evaluation
     *
     */
    final private DataFormat dataFormat;

     /**
     * Construct an invalid JSONPath exception with a given string message.
     * @param result The boolean result of the evaluation.
     * @param exprValue The value returned from parsing the XPath or JSONPath expression
     * @param dataFormat The data format used in the evaluation
     */
    public PathEvaluationResult(boolean result, Object exprValue, DataFormat dataFormat){
        this.result = result;
        this.exprValue = exprValue;
        this.dataFormat = dataFormat;
    }

    /**
     * Get the result of the evaluation.
     * @return The boolean result.
     */
    public boolean getResult(){
        return this.result;
    }

    /**
     * Get the value return from parsing the XPath or JSONPath expression
     * @return The Object exprValue
     */
    public Object getValue(){
        return this.exprValue;
    }

    /**
     * Get the data format used in the evaluation
     * @return The dataFormat, either XML or JSON
     */
    public DataFormat getType(){
        return this.dataFormat;
    }
}
