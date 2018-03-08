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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import static org.fusesource.hawtbuf.Buffer.utf8;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Architecture;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.InvalidPatternReferenceException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

/**
 * The MQTTMessage object is a test event. It stores the content for a test
 * message to be used for firing test messages at a MQTT broker. Hence, it
 * will contain information for a request message (PUB/SUB/ etc.)
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Paul Grace
 */
public class MQTTMessage extends ProtocolMessage{

    /**
     * The body of the message. This is the data type (xml, json, etc.) and
     * the byte string of the content
     */
    private transient Content dataBody = new Content(null, null);

    /**
     * getter for the Content object of the message
     * @return the content of the message, data type and message body
     */
    @Override
    public Content getContent(){
        return this.dataBody;
    }

    /**
     * The MQTT operation: "PUBLISH", "SUBSCRIBE", "UNSUBSCRIBE".
     * Others (connect, disconnect, etc.) to be added later
     */
    private transient String method;

    /**
     * Getter for the method field.
     * @return Return the method string.
     */
    @Override
    public final String getMethod() {
        return method;
    }

    /**
     * The MQTT address/port to send the request to.
     */
    private transient String url;

    /**
     * Getter for the full URL.
     * @return the url as a java url object.
     */
    @Override
    public final String getURL() {
        return url;
    }

    /**
     * The topic of the MQTT message.
     */
    private transient String topic = "/";
    
    @Override
    public String getPath(){
        return topic;
    }

    /**
     * Content/Application specific HTTP headers e.g. authentication.
     */
    private transient Parameter[] headers;


    /**
     * The state machine context of this message.
     */
    private final transient Architecture stateMachine;

    /**
     * Construct a MQTT Message.
     *
     * @param urlstr The full target URL of the broker
     * @param topicstr The broker topic path.
     * @param methodStr The method of the request (e.g. PUBLISH, GET, ...)
     * @param type The type of the data content e.g. xml, json
     * @param body The message body (for a subscribe there may be no content and this
     * may be null
     * @param mqttHeaders The list of headers to add to message. These must be MQTT
     * @param arcContext The architecture object providing the context of operation.
     * @throws InvalidRESTMessage Error constructing the REST message representation.
     */
    public MQTTMessage(final String urlstr, final String topicstr, final String methodStr, final String type, final String body,
            final Parameter[] mqttHeaders, final Architecture arcContext) throws InvalidRESTMessage {

        this.stateMachine = arcContext;
        /**
         * Method must be either: get, post, put, delete
         */
        if (methodStr == null) {
            throw new InvalidRESTMessage("Method cannot be null");
        }

        this.method = methodStr.toLowerCase(Locale.ENGLISH);

        if (type != null) {
            if (type.equalsIgnoreCase(XML_LABEL) || type.equalsIgnoreCase(JSON_LABEL) || type.equalsIgnoreCase(OTHER_LABEL)) {
                this.dataBody = new Content(type, body);
            } else {
                throw new InvalidRESTMessage("Type: " + type + " must be: 'xml' or 'json'");
            }
        }

        if (urlstr.startsWith("component")) {
            this.url = XMLStateMachine.getURLEntryFromXML(urlstr, this.stateMachine);
        } else {
            this.url = urlstr;
        }
        if (topicstr != null) {
            this.topic = topicstr;
        }
        if (mqttHeaders != null) {
            this.headers = mqttHeaders.clone();
        }
    }

    /**
     * Parse the message input and replace the template with given input.
     * @param input The String input template.
     * @return A string with the parsed data evaluated against the template input
     * @throws InvalidPatternReferenceException Error in the Pattern specification doesn't fit with template.
     * @throws InvalidRESTMessage Error in the rest message object.
     */
    private String parseData(final String input) throws InvalidPatternReferenceException, InvalidRESTMessage {
        if (this.stateMachine == null) {
            return input;
        } else {
            return this.stateMachine.replacePatternValue(input);
        }
    }

    /**
     * Create an invocation i.e. use the data in the REST message to perform
     * a full client request.
     * @return The Rest event received after the invocation complete.
     * @throws UnexpectedEventException Event not matching the state machine description.
     */
    @Override
    public final MQTTEvent invokeMessage() throws UnexpectedEventException {
        try {

            URL urlToInvoke = new URL(this.url);
            MQTT mqtt = new MQTT();

            /**
             * Get the proxy from the list
             */
            mqtt.setHost(urlToInvoke.getHost(), urlToInvoke.getPort());

            BlockingConnection connection = mqtt.blockingConnection();
            connection.connect();

            Topic[] topics = {new Topic(utf8(this.topic), QoS.AT_LEAST_ONCE)};
            long time= System.currentTimeMillis();
            switch (method.toUpperCase()) {
                case "PUBLISH":
                    String topic = this.topic;
                    QoS qos = QoS.AT_MOST_ONCE;
                    boolean retain = false;
                    if (headers != null) {
                        for (Parameter param : headers) {
                            switch (param.getName().toLowerCase()) {
                                case "qos":
                                     switch(param.getValue()) {
                                         case "1":
                                            qos = QoS.AT_LEAST_ONCE;
                                            break;
                                         case "2":
                                             qos = QoS.EXACTLY_ONCE;
                                             break;
                                     }
                                     break;
                                case "retain-flag":
                                    if(param.getValue().equalsIgnoreCase("true")){
                                        retain=true;
                                    }
                                    break;
                            }
                        }
                    }
                    // There are two headers mqtt.qos and mqtt.retain
                    connection.publish(topic, this.dataBody.getData().getBytes(), qos, retain);
                    time = System.currentTimeMillis() - time;
//                    connection.disconnect();
                    return null;
                case "SUBSCRIBE":
                    byte[] values = connection.subscribe(topics);
                    time = System.currentTimeMillis() - time;
                    int[] intArray = new int[values.length];

                    // converting byteArray to intArray
                    for (int i = 0; i < values.length; i++) {
                        intArray[i] = values[i];
                    };
//                    connection.disconnect();
                    return fromSubResponse(intArray, time);
                case "UNSUBSCRIBE":
                    connection.unsubscribe(new String[]{this.topic});
                    time = System.currentTimeMillis() - time;
                    connection.disconnect();
                    return fromSubResponse(null, time);
            }
            throw new UnexpectedEventException("MQTT message, not publish, subscribe or unsubscribe");
        } catch (InvalidRESTMessage | InvalidPatternReferenceException ex) {
            throw new UnexpectedEventException(ex.getMessage(), ex);
        } catch (URISyntaxException ex) {
            throw new UnexpectedEventException(ex.getMessage(), ex);
        } catch (MalformedURLException ex) {
            throw new UnexpectedEventException(ex.getMessage(), ex);
        } catch (Exception ex) {
           throw new UnexpectedEventException(ex.getMessage(), ex);
        }
    }

    /**
     * Create a REST Event used by the interoperability tool state machine from
     * the RESTLET Response generated by invoking this REST Message.
     * @param response The HTTP msg response received
     * @return A generated Rest event object.
     * @throws InvalidRESTMessage Error creating event from message.
     */
    private MQTTEvent fromResponse(long time)
        throws InvalidRESTMessage {
        final MQTTEvent rResp = new MQTTEvent();

        /*
        * Create a REST event about the Service Response i.e. capture and
        * uniform the data to be understood by the state machine rule checker
        */
        rResp.addParameter(new Parameter("mqtt.msg", "unsubscribe"));
        rResp.addParameter(new Parameter(MQTTEvent.RESPONSE_TIME, Long.toString(time)));
        rResp.addParameter(new Parameter("mqtt.ack", "true"));

        rResp.addContent("application/octet-stream", "");
        return rResp;
    }

    /**
     * Create an MQTT SUBSCRIBE response event
     * @param returnCodes The set of return codes in an int array
     * @param time The time taken for the response to be received.
     * @return The abstracted MQTT event.
     * @throws InvalidRESTMessage
     */
    private MQTTEvent fromSubResponse(final int[] returnCodes, long time)
        throws InvalidRESTMessage {
        final MQTTEvent rResp = new MQTTEvent();

        /*
        * Create a REST event about the Service Response i.e. capture and
        * uniform the data to be understood by the state machine rule checker
        */
        rResp.addParameter(new Parameter("mqtt.msg", "subscribe"));
        rResp.addParameter(new Parameter(MQTTEvent.RESPONSE_TIME, Long.toString(time)));
        rResp.addParameter(new Parameter("mqtt.ack", "true"));

        if(returnCodes != null ) {
            for(int i=1; i<=returnCodes.length; i++) {
                rResp.addParameter(new Parameter("mqtt.returnvalue"+i, Integer.toString(returnCodes[i-1])));
            }
        }

        rResp.addContent("application/octet-stream", "");
        return rResp;
    }
}
