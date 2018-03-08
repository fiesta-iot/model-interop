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

/**
 * The RESTMessage object is a test event. It stores the content for a test
 * message to be used for firing test messages at a REST service. Hence, it
 * will contain information for a request message (POST, GET, etc..)
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public abstract class ProtocolMessage implements Serializable {

    /**
     * Constant for the XML message tag:  &ltmessage&gt.
     */
    public static final String MESSAGE_LABEL = "message";

    /**
     * Constant for the XML message tag:  &lturl&gt.
     */
    public static final String URL_LABEL = "url";

    /**
     * Constant for the XML message tag:  &ltpath&gt.
     */
    public static final String PATH_LABEL = "path";

    /**
     * Constant for the XML message tag:  &ltbody&gt.
     */
    public static final String BODY_LABEL = "body";

    /**
     * Constant for the XML message tag:  &ltname&gt.
     */
    public static final String HEADERNAME_LABEL = "name";

    /**
     * Constant for the XML message tag:  &ltmethod&gt.
     */
    public static final String METHOD_LABEL = "method";

    /**
     * Constant for the XML message tag:  &ltxml&gt.
     */
    public static final String XML_LABEL = "xml";

    /**
     * Constant for the OTHER message tag:  &ltxml&gt.
     */
    public static final String OTHER_LABEL = "other";

    /**
     * Constant for the XML message tag:  &ljson&gt.
     */
    public static final String JSON_LABEL = "json";

    /**
     * Constant for the XML message tag:  &lttype&gt.
     */
    public static final String TYPE_LABEL = "type";

    /**
     * Constant for the XML message tag:  &ltheaders&gt.
     */
    public static final String HEADERS_LABEL = "headers";

    /**
     * Constant for the XML message tag:  &ltheaderval&gt.
     */
    public static final String HEADERVALUE_LABEL = "value";

    /**
     * The body of the HTTP request. This is the data type (xml, json, etc.) and
     * the byte string of the content
     */
    private transient Content dataBody = new Content(null, null);

    /**
     * getter for the Content object of the message
     * @return the content of the message, data type and message body
     */
    public Content getContent(){
        return dataBody;
    }

    /**
     * a getter method for the headers of a message, by default there are no headers
     * @return null since by default we assume the protocol doesn't support headers, any message should override this method to ensure headers are returned
     */
    public Object getHeaders(){
        return null;
    }

    /**
     * a getter method for the path of a message, we don't know whether this would be a path (HTTP) or a topic (MQTT)
     * @return null since by default we don't know what should be returned, any message should override this method to ensure correct result is returned
     */
    public String getPath(){
        return null;
    }
    
    /**
     * The REST operation: "GET", "POST", "PUT", or "DELETE".
     */
    private transient String method;

    /**
     * Getter for the method field.
     * @return Return the method string.
     */
    public String getMethod() {
        return method;
    }

    /**
     * The REST URL to send the request to.
     */
    private String url;

    /**
     * Getter for the full URL.
     * @return the url as a java url object.
     */
    public String getURL() {
        return url;
    }

    /**
     * Getter for the full URL.
     * @newURL the url as a java url object.
     */
    public void setURL(String newURL) {
        this.url = newURL;
    }


    /**
     * Create an invocation i.e. use the data in the REST message to perform
     * a full client request.
     * @return The Rest event received after the invocation complete.
     * @throws UnexpectedEventException Event not matching the state machine description.
     */
    public MsgEvent invokeMessage() throws UnexpectedEventException {
        return null;
    }

}
