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

import java.lang.reflect.Field;
import java.util.StringTokenizer;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;

/**
 * Utility class for evaluating primitive data structures as opposed to xml
 * and JSON.
 * Can be used against parsed data structures. That is any data structure
 * with a parser to a Java object can still be evaluated by the interoperability
 * framework.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public final class Primitive {

    /**
     * Utility class, therefore private constructor to avoid instantiation.
     */
    private Primitive() {
        // Empty implementation.
    }

    /**
     * Validate that the type of the data value is of the specified type.
     * Primitive.validate will check against both primitive types and Java
     * classes.
     * Primitive types are:
     *      java.lang.String, java.lang.integer, ...
     * Class values are the full canonical name of the class (including
     * package reference).
     * @param content The data to validate i.e. the primitive or structured
     * Java object.
     * @param typecheck The required valid type e.g. "java.lang.string"
     * @return true if the type of the object matches
     */
    public static boolean validate(final Object content, final String typecheck) {
        return content.getClass().getCanonicalName().equalsIgnoreCase(typecheck);
    }

    /**
     * XPATH based method to assert that particular expressions in an
     * primitive data object e.g. //Data/fieldA/fieldB ==
     * Data.fieldA.fieldB == Main St.
     *
     * @param data The content to check
     * @param reference The expression (Xpath like for now i.e. /bb/cc/dd)
     * @param value Value to be asserted
     * @return true or false as a result of the test
     */
    public static boolean assertPrimitive(final Object data, final String reference, final Object value) {

        // Take the xpath expression and break it into objects and fields
        final StringTokenizer tokenize = new StringTokenizer(reference, "//");
        Object fValue = data;
        while (tokenize.hasMoreTokens()) {
            try {
                // Use reflection to extract the corresponding data object
                final Field chap = fValue.getClass().getDeclaredField(tokenize.nextToken());
                chap.setAccessible(true);
                fValue = chap.get(fValue);
            } catch (NoSuchFieldException ex) {
                ServiceLogger.LOG.error("The data object does not contain the xpath field reference", ex);
                return false;
            } catch (SecurityException ex) {
                ServiceLogger.LOG.error("Security Error:", ex);
                return false;
            } catch (IllegalArgumentException ex) {
                ServiceLogger.LOG.error("Input parameters are incorrect:", ex);
            } catch (IllegalAccessException ex) {
               ServiceLogger.LOG.error("Illegal Access to data:", ex);
            }
        }
        return fValue.equals(value);
    }
}
