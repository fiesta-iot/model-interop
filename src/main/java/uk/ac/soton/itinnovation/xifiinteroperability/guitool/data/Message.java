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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A Message is the data structure that describes a transition between nodes
 * in the state machine; and it contains the active data required to construct
 * a HTTP message and operation that can be executed directly by the state
 * machine. This is the GUI representation of the message that is placed in the
 * form.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class Message extends AbstractGraphElement implements Serializable {

    /**
     * The target transition label of the message. When message is execute - the
     * state machine goes to this state.
     */
    private String targetLabel;

    /**
     * The urlEndpoint to send the message to.
     */
    private String urlEndpoint;

    /**
     * Get field for the message endpoint url.
     * @return The url endpoint.
     */
    public final String getEndpoint() {
        return urlEndpoint;
    }

    /**
     * The resource path of the REST operation.
     */
    private String path;

    /**
     * Get field for the message endpoint path.
     * @return The url endpoint.
     */
    public final String getPath() {
        return path;
    }

    /**
     * The http method type.
     */
    private String method;

    /**
     * Get the HTTP method name (GET, etc.).
     * @return The string describing the message.
     */
    public final String getHTTPMethod() {
        return method;
    }

    /**
     * The type of the message data e.g. XML or JSON.
     */
    private String datatype;

    /**
     * Get the data type of the message.
     * @return The type (xml, json)
     */
    public final String getDataType() {
        return datatype;
    }

    /**
     * The HTTP body content (of the datatype).
     */
    private String body;

    /**
     * Get the body of the HTTP message.
     * @return The HTTP body.
     */
    public final String getHTTPBody() {
        return body;
    }

    /**
     * The list of http headers (name value pairs).
     */
    private List<ConstantData> headers = new ArrayList();

    /**
     * Get the constant data elements i.e. the http headers.
     * @return The HTTP headers.
     */
    public final List<ConstantData> getConstantData() {
        return headers;
    }

    /**
     * a setter method for the list of headers,
     * used by the copy paste manager to override the existing list of headers with the copied one
     * @param newData the new list of headers
     */
    public final void setConstantData(List<ConstantData> newData){
        headers = newData;
    }

     /**
     * Create a new message transition structure.
     * @param idty The id of the connection
     * @param type The connection type
     * @param toElem  The target node id (in the connection)
     */
    public Message(final String idty, final String type, final AbstractGraphElement toElem) {
        super(idty, type);
        this.targetLabel = ((GraphNode) toElem).getLabel();
    }

    /**
     * Add the message data information.
     * @param url The url to send the message to
     * @param pthStr The resource path to build the http message
     * @param mthStr The http method type
     * @param type The data type of the message
     * @param bodymsg  The body content of the message
     */
    public final void updateMessage(final String url, final String pthStr, final String mthStr, final String type, final String bodymsg) {
        this.urlEndpoint = url;
        this.path = pthStr;
        this.method = mthStr;
        this.datatype = type;
        this.body = bodymsg;
    }

    /**
     * Add a new http header to the data message.
     * @param name The name of the HTTP header eg. Accept, Content-type
     * @param value The string value of the header
     */
    public final void addHeader(final String name, final String value) {
        this.headers.add(new ConstantData(name, value));
    }

    /**
     * remove an existing http header from the data message.
     * @param name the header to remove
     */
    public final void removeHeader(final String name){
        ConstantData toRemove = null;
        for (ConstantData header: this.headers){
            if (header.getFieldName().equalsIgnoreCase(name)){
                toRemove = header;
                break;
            }
        }

        if (toRemove != null){
            headers.remove(toRemove);
        }
    }

    /**
     * Update the target label id of the connection.
     * @param newTarget The label id of the
     */
    public final void setTarget(final String newTarget) {
        this.targetLabel = newTarget;
    }

     /**
     * Get the target label id of the connection.
     * @return The target label.
     */
    public final String getTarget() {
        return this.targetLabel;
    }

    /**
     * Generate the XML string to match this object's data format.
     * @return The produced XML string.
     */
    @Override
    public final String generateTransitionXML() {
        final StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("\n\t\t\t<transition>");
        sBuilder.append("\n\t\t\t\t<to>").append(this.targetLabel).append("</to>");
        sBuilder.append("\n\t\t\t\t<message>");
            sBuilder.append("\n\t\t\t\t\t<url>").append(this.urlEndpoint).append("</url>");
            sBuilder.append("\n\t\t\t\t\t<path>").append(this.path).append("</path>");
            sBuilder.append("\n\t\t\t\t\t<method>").append(this.method).append("</method>");
            sBuilder.append("\n\t\t\t\t\t<type>").append(this.datatype).append("</type>");
            sBuilder.append("\n\t\t\t\t\t<headers>");
            for (ConstantData e : this.headers) {
                sBuilder.append("\n\t\t\t\t\t\t<header>");
                sBuilder.append("\n\t\t\t\t\t\t\t<name>").append(e.getFieldName()).append("</name>");
                sBuilder.append("\n\t\t\t\t\t\t\t<value>").append(e.getFieldValue()).append("</value>");
                sBuilder.append("\n\t\t\t\t\t\t</header>");
            }
            sBuilder.append("\n\t\t\t\t\t</headers>");
            sBuilder.append("\n\t\t\t\t\t<body>");
            if (this.datatype != null) {
                if (!this.datatype.equalsIgnoreCase("xml")) {
                   sBuilder.append("<![CDATA[").append(this.body).append("]]>").append("</body>");
                } else {
                    sBuilder.append(this.body).append("</body>");
                }
            }
            else {
                sBuilder.append("</body>");
            }
        sBuilder.append("\n\t\t\t\t</message>");
        sBuilder.append("\n\t\t\t</transition>");
        return sBuilder.toString();
    }
}
