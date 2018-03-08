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

package uk.ac.soton.itinnovation.xifiinteroperability.modelframework;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Architecture;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.InvalidPatternReferenceException;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data.InvalidRegexException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.InvalidInputException;

/**
 * A guard is a rule applied to an event that evaluates to true or false.
 * A transition can only be realised where
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class Guard {

    /**
     * Enumeration of possible types: 3 at present. If a new function is
     * added to the XML specification then this class must be updated.
     */
    public enum ComparisonType {
        /**
         * 7 types: =, !=, set contains, <, >, counter and regex.
         */
	EQUALS(0), NOTEQUALS(1), CONTAINS(2), LESSTHAN(3), GREATERTHAN(4), COUNTER (5), REGEX(6);

        /**
         * The integer id of the type (specified above).
         */
	private final int code;

        /**
         * Private constructor.
         * @param comparitor The type to construct.
         */
	private ComparisonType(final int comparitor) {
	    this.code = comparitor;
	}

	@Override
	public String toString() {
	    return Integer.toString(code);
	}

        /**
         * Getter for the type integer.
         * @return The integer type.
         */
	public final int getType() {
	    return code;
	}
    }

    /**
     * The type of the guard. That is the function.
     */
    private final transient ComparisonType guardCondType;
    /**
     * The data type of the guarded value.
     */
    private final transient Class dataType;

    /**
     * Get the comparison type.
     * @return The type enumeration.
     */
    public final ComparisonType getType() {
        return guardCondType;
    }

    /**
     * What will be used as the comparitor for the guard. If V is the tested
     * value; then when V is applied to the function the evaluation against
     * this compareto value must be true.
     */
    private transient String compareTo;

    /**
     * Return the comparison value that this guard is evaluating.
     * @return The guard value to compare against.
     */
    public final String getGuardCompare() {
        return compareTo;
    }

    /**
     * Setter for the comparitor.
     * @param compare The new compare to value.
     */
    public final void setGuardCompare(final String compare) {
        compareTo = compare;
    }

    /**
     * The label of the guard. That is usually the parameter being evaluated.
     */
    private transient String guardCondition;


    /**
     * Return the parameter that this guard is evaluating.
     * @return The guard parameter label.
     */
    public final String getGuardLabel() {
        return guardCondition;
    }

    public final void setGuardLabel(String newLabel) {
        guardCondition = newLabel;
    }

    /**
     * Construct the guard. Note, all elements are translated to lowercase for
     * case independent matching. This is because, there may be little
     * standardisation of http fields.
     *
     * @param label The guard label.
     * @param type The guard type.
     * @param condition The condition e.g. =
     * @param comparitor What to compare a value against.
     * @param arc Overall architecture context.
     * @throws InvalidGuard Exception indicating guard could not be produced from the inputs
     */
    public Guard(final String label, final Class type, final ComparisonType condition,
            final String comparitor, final Architecture arc) throws InvalidGuard {
        this.guardCondition = label;

        String testCondition = comparitor;
        if (testCondition.startsWith(XMLStateMachine.DATA_TAG)) {
            try {
                testCondition = arc.getData(testCondition);
            } catch (InvalidPatternReferenceException ex) {
                throw new InvalidGuard("Error in guard expression", ex);
            }
        }
        else if (testCondition.startsWith(XMLStateMachine.TEST_TAG)) {
            try {
                testCondition = arc.getPreviousTestData(testCondition);
            }
            catch (InvalidPatternReferenceException ex){
                throw new InvalidGuard("Error in guard expression", ex);
            }
        }

        this.guardCondType = condition;
        this.compareTo = testCondition;
        this.dataType = type;
    }

    /**
     * Evaluate this guard against the provided input.
     * @param input The input to test the guard against.
     * @return The result of the evaluated guard against the input.
     * @throws InvalidInputException Error in input and exception thrown during compare.
     * @throws InvalidRegexException Thrown in case of an invalid regex syntax
     */
    public final boolean evaluate(final Object input) throws InvalidInputException, InvalidRegexException {
        Object toCompare = input;
        if (dataType == String.class) {
            toCompare = ((String) input);
        }

        switch(this.guardCondType) {
            case EQUALS:
                if (dataType == String.class) {
                    return ((String) toCompare).equalsIgnoreCase(this.compareTo);
                }
                return toCompare.equals(this.compareTo);
            case NOTEQUALS:
                if (dataType == String.class) {
                    return !((String) toCompare).equalsIgnoreCase(this.compareTo);
                }
                return !toCompare.equals(this.compareTo);
            case COUNTER:
                Integer aC = new Integer(this.compareTo);
                Integer bC = (Integer) toCompare;
                return aC==bC;
            case LESSTHAN:
                try{
                    double a = new Double(this.compareTo);
                    double b = new Double(toCompare.toString());
                    return b < a;
                } catch(Exception ex) {
                    return false;
                }
            case GREATERTHAN:
                try{
                    double a = new Double(this.compareTo);
                    double b = new Double(toCompare.toString());
                    return b > a;
                } catch(Exception ex) {
                    return false;
                }
            case CONTAINS:
                 final HashMap<String, Parameter> heads = (HashMap<String, Parameter>) input;
                 return heads.containsKey(this.compareTo);
            case REGEX:
                try {
                    return Pattern.matches(this.compareTo, toCompare.toString());
                }
                catch (PatternSyntaxException ex){
                    throw new InvalidRegexException("There is a regex guard with an invalid regular expression.");
                }
             default:
                 throw new InvalidInputException("Unknown condition type");
        }
    }
}

