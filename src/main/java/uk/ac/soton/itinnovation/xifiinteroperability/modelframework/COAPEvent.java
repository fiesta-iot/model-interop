/////////////////////////////////////////////////////////////////////////
//
// Â© University of Southampton IT Innovation Centre, 2017
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
import java.util.HashMap;
import java.util.Map;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;

/**
 * Capture the data held in each rest event. Note we use a builder pattern
 * rather than a single constructor. Information is extracted from
 * multiple sources, and the event is built up over time.
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class COAPEvent extends MsgEvent implements Serializable {

    /**
     * Portable serializable class.
     */
    public static final long serialVersionUID = 1L;


    /**
     * COAP Events have fixed constants for parameter values specific to
     * the tool (generally these represent the absolute base for COAP
     * exchange - from, to, message, ...
     */

    /** Where is the message from = IP address of the originator. */
    public static final String COAP_FROM = "coap.from";

     /**  Where is the message to = IP address of the location sent to. */
    public static final String COAP_TO = "coap.to";

    /** What is the URL path i.e. the HTTP interface of the request. */
    public static final String COAP_TOINTERFACE = "coap.tointerface";

     /**  What is the message type - get, put, delete, post ...*/
    public static final String COAP_MSG = "coap.msg";

    /** What is the result code of the message (200, 404). */
    public static final String COAP_CODE = "coap.code";

    /** A creatable header field. */
    public static final String COAP_CONFIG_HEAD = "coap.";

    /** A label for response time **/
    public static final String RESPONSE_TIME = "response-time";

    /**
     * The content of the message - optional, not all REST events will
     * have associated data.
     */
    private Content dataBody;

    /**
     * Getter for the data content of the event.
     * @return The data content.
     */
    public final Content getDataBody() {
        return dataBody;
    }

    /**
     * A receiving state stores the response time for the server
     * to react to the request. This can be used for QoS metric testing.
     */
    private long responseTime;

    /**
     * get the event time stamp
     * @return the time stamp in milliseconds
     */
    public long getResponseTime() {
        return this.responseTime;
    }

    /**
     * Set the event time response
     * @respTime the time stamp in milliseconds
     */
    public void setResponseTime(long respTime) {
        this.responseTime = respTime;
    }

    /**
     * Set the event content.
     * @param newBody Content to set.
     */
    public final void setDataBody(final Content newBody) {
        dataBody = newBody;
    }

    /**
     * Each rest events has a series of parameters e.g. HTTP headers,
     * Query parameters, even parameters in the body.
     */
    private final Map<String, Parameter> inputParams = new HashMap();

    /**
     * Retrieve the parameters list.
     * @return The list of parameters.
     */
    public final Map<String, Parameter> getParameterMap() {
        return this.inputParams;
    }

    /**
     * Add a new parameter. This must be unique with the param set (otherwise
     * it will overwrite the existing value.
     *
     * @param pNew The new parameter to add.
     * @return True if the parameter added successfully.
     */
    public final boolean addParameter(final Parameter pNew) {
        if (this.inputParams.put(pNew.getName(), pNew) == null) {
            return false;
        }
        return true;
    }

    /**
     * Add the event body.
     * @param type The type of the data content
     * @param body The data content itself.
     */
    public final void addContent(final String type, final String body) {
        setDataBody(new Content(type, body));
        addParameter(new Parameter("content", body));
    }

}
