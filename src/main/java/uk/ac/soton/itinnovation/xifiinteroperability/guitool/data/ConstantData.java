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
// Created for Project : XIFI (http://www.fi-xifi.eu)
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data;

import java.io.Serializable;

/**
 * Simple constant name/value pair for adding pattern constants to a start
 * node in the graph.
 *
 * @author pjg
 */
public class ConstantData implements Serializable {

    /**
     * Data value field name.
     */
    private String data;

    /**
     * Read the constant data field name.
     * @return The field name
     */
    public final String getFieldName() {
        return data;
    }

    /**
     * Set the field name of the constant data.
     * @param fName The new field name
     */
    public final void setFieldName(final String fName) {
        data = fName;
    }

    /**
     * The constant data field's value.
     */
    private String value;

    /**
     * Read the constant data field value.
     * @return The field name
     */
    public final String getFieldValue() {
        return value;
    }

    /**
     * Set the field value of the constant data.
     * @param fVal The new field name
     */
    public final void setFieldValue(final String fVal) {
        value = fVal;
    }

    /**
     * Construct a new object about constant data with a name value pair.
     * @param newData The name id of the constant.
     * @param val The initial value of the constant.
     */
    public ConstantData(final String newData, final String val) {
        this.data = newData;
        this.value = val;
    }

    /**
     * Create an xml representation of this constant data.
    *
    * @return The newly created XML representation of the constant data.
    */
    public final String generateTransitionXML() {
        final StringBuilder stringBuild = new StringBuilder();
        stringBuild.append("\n\t\t<data>");
        stringBuild.append("\n\t\t\t<name>").append(this.data).append("</name>");
        stringBuild.append("\n\t\t\t<value>").append(this.value).append("</value>");
        stringBuild.append("\n\t\t</data>");
        return stringBuild.toString();
    }

}
