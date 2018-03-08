/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2015
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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data;

import java.io.Serializable;

/**
 * Enumeration of function types for guards: to extend add a new function
 * to the list; the UI will be updated automatically to handle inputs.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 * 
 * @author Paul Grace
 */
public final class Function implements Serializable {

    /**
     * Use private constructor as all methods are static.
     */
    private Function() {
    }

     /**
     * Can be equals, notequals, contains, lessthan, greaterthan, regex.
     */
    public enum FunctionType {

        /**
         * The equals function a==b.
         */
        Equals ("equal"),

        /**
         * The not equals function a!=b.
         */
        NotEquals ("notequal"),

        /**
         * Contains function, the set A contains B if
         * B is a member or field of A.
         */
        Contains ("contains"),

        /**
         * < function, a < b
         */
        LessThan ("lessthan"),

        /**
         * < function, a > b
         */
        GreaterThan ("greaterthan"),

        /**
         * The counter function - Index = NumIterations
         */
        Counter ("counter"),

        /**
         * The regex function - matching values with regular expressions
         */
        Regex("regex");

        /**
         * The string version of the type e.g. "equals"
         */
        private final String name;

        /**
         * Construct a new enumerated type.
         * @param typeEntry The string name of the type enumeration.
         */
        private FunctionType(final String typeEntry) {
            name = typeEntry;
        }

        /**
         * Evaluate if this string matches the named type. That is if type
         * is equals, and otherName input is equals it returns true.
         * @param otherName The string to evaluate.
         * @return boolean indication of evaluation.
         */
        public boolean equalsName(final String otherName) {
            return (otherName == null) ? false : name.equals(otherName);
        }

        @Override
        public String toString() {
           return name;
        }
    }

    /**
     * Static function to return an enumerated type from a string (a type not
     * an instance).
     * @param typeName The string name of the type to return.
     * @return The enumeration type e.g. FunctionType.Equals
     */
    public static FunctionType getFunction(final String typeName) {
        if (typeName.equalsIgnoreCase("equal")) {
            return FunctionType.Equals;
        }
        if (typeName.equalsIgnoreCase("counter")) {
            return FunctionType.Counter;
        }
        if (typeName.equalsIgnoreCase("notequal")) {
            return FunctionType.NotEquals;
        }
        if (typeName.equalsIgnoreCase("contains")) {
            return FunctionType.Contains;
        }
        if (typeName.equalsIgnoreCase("lessthan")) {
            return FunctionType.LessThan;
        }
        if (typeName.equalsIgnoreCase("greaterthan")) {
            return FunctionType.GreaterThan;
        }
        if (typeName.equalsIgnoreCase("regex")){
            return FunctionType.Regex;
        }
        return null;
    }

}
