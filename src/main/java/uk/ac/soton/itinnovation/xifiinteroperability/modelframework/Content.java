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

import java.io.Serializable;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data.JSON;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data.XML;
/**
 * Content refers to the content of a message. In HTTP and REST terms this is
 * very unstructured - it is simply a data element (set of bytes) with an
 * qualified types: application/xml, application/json, etc.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class Content implements Serializable {

    /**
     * Class is serializable so make portable.
     */
    public static final long serialVersionUID = 1L;

    /**
     * The type of the message content - JSON, XML (HTTP Media Type).
     */
    private String type;

    /**
     * Get the content type.
     * @return The name of the type as a string (HTTP Media Type).
     */
    public final String getType() {
        return type;
    }

    /**
     * Change the type of the data to the new media type.
     * @param newMediaType The new type description.
     */
    public final void setType(final String newMediaType) {
        type = newMediaType;
    }

    /**
     * The data content.
     */
    private String data;

    /**
     * Get the content value.
     * @return The content as a string (HTTP message body).
     */
    public final String getData() {
        return data;
    }

    /**
     * Change the data to the newcontent.
     * @param newContent The updated message content.
     */
    public final void setData(final String newContent) {
        data = newContent;
    }

    /**
     * Construct a new data object for the content.
     * @param newType The type of the content.
     * @param newData The actual message content.
     */
    public Content(final String newType, final String newData) {
        this.data = newData;
        this.type = newType;
    }

    /**
     * Change the value of the data using a path expression on the input value.
     * That is change all or part of the message content.
     * @param pathLocation The path expression to apply
     * @param value The new content value to insert.
     * @return The changed string value.
     */
    public final String setField(final String pathLocation, final String value) {
        if (type.equalsIgnoreCase("json")) {
            return JSON.writeValue(data, pathLocation, value);
        } else {
            return XML.writeValue(data, pathLocation, value);
        }
    }

    /**
     * Read part of the content field value based upon the path expression
     * e.g. xpath of jsonpath expression.
     * @param pathLocation The field subset to read
     * @return The value of the data at the path location.
     */
    public final String getFieldValue(final String pathLocation) {
        if (type.equalsIgnoreCase("json")) {
            return JSON.readValue(data, pathLocation);
        } else {
            return XML.readValue(data, pathLocation);
        }
    }
}
