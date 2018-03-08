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

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Message;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;
import uk.ac.soton.itinnovation.xifiinteroperability.ConfigurationException;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Architecture;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.InvalidPatternReferenceException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;
import uk.ac.soton.itinnovation.xifiinteroperability.SystemProperties;

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
public class RESTMessage extends ProtocolMessage{


    /**
     * Constant for the XML message tag:  &ltget&gt.
     */
    public static final String GET_LABEL = "get";

    /**
     * Constant for the XML message tag:  &ltpost&gt.
     */
    public static final String POST_LABEL = "post";

    /**
     * Constant for the XML message tag:  &ltput&gt.
     */
    public static final String PUT_LABEL = "put";

    /**
     * Constant for the XML message tag:  &ltdelete&gt.
     */
    public static final String DELETE_LABEL = "delete";

    /**
     * Constant for the XML message tag:  &ltdelete&gt.
     */
    public static final String PUB_LABEL = "publish";

    /**
     * Constant for the XML message tag:  &ltdelete&gt.
     */
    public static final String SUB_LABEL = "subscribe";

    /**
     * Constant for the XML message tag:  &ltdelete&gt.
     */
    public static final String UNSUB_LABEL = "unsubscribe";


    /**
     * The body of the HTTP request. This is the data type (xml, json, etc.) and
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
     * The REST operation: "GET", "POST", "PUT", or "DELETE".
     */
    private transient String method;

    /**
     * Getter for the method field.
     * @return Return the method string.
     */
    @Override
    public String getMethod() {
        return method;
    }

    /**
     * The REST URL to send the request to.
     */
    private transient String url;

    /**
     * Getter for the full URL.
     * @return the url as a java url object.
     */
    @Override
    public String getURL() {
        return url;
    }

    /**
     * Getter for the full URL.
     * @param newURL the url as a java url object.
     */
    public void setURL(String newURL) {
        this.url = newURL;
    }

    /**
     * The path of the REST message.
     */
    private transient String path = "/";
    
    @Override
    public String getPath(){
        return path;
    }

    /**
     * Content/Application specific HTTP headers e.g. authentication.
     */
    private transient Parameter[] headers;

    /**
     * overriding the getter method for the headers to return the array of parameters
     * @return the headers array
     */
    @Override
    public Object getHeaders(){
        return headers;
    }


    /**
     * RESTLET constant used to set application headers.
     */
    private static final String HEADERS_KEY = "org.restlet.http.headers";

    /**
     * The state machine context of this message.
     */
    private final transient Architecture stateMachine;

    /**
     * Construct a REST Message.
     *
     * @param urlstr The full target URL of the request (including the path)
     * @param pathstr The REST resource path.
     * @param methodStr The REST method of the request (e.g. POST, GET, ...)
     * @param type The type of the data content e.g. xml, json
     * @param body The message body (for a GET there may be no content and this
     * may be null
     * @param restHeaders The list of headers to add to message. These must be HTTP
     * header values.
     * @param arcContext The architecture object providing the context of operation.
     * @throws InvalidRESTMessage Error constructing the REST message representation.
     */
    public RESTMessage(final String urlstr, final String pathstr, final String methodStr, final String type, final String body,
            final Parameter[] restHeaders, final Architecture arcContext) throws InvalidRESTMessage {

        this.stateMachine = arcContext;
        /**
         * Method must be either: get, post, put, delete
         */
        if (methodStr == null) {
            throw new InvalidRESTMessage("Method cannot be null");
        }

        if ((methodStr.equalsIgnoreCase(GET_LABEL)) || (methodStr.equalsIgnoreCase(POST_LABEL)) || (methodStr.equalsIgnoreCase(DELETE_LABEL)) || (methodStr.equalsIgnoreCase(PUT_LABEL))
                || (methodStr.equalsIgnoreCase(PUB_LABEL)) || (methodStr.equalsIgnoreCase(SUB_LABEL)) || (methodStr.equalsIgnoreCase(UNSUB_LABEL)) ) {
            this.method = methodStr.toLowerCase(Locale.ENGLISH);
        } else {
            throw new InvalidRESTMessage("Method: " + methodStr + " must be: get, put, delete or post");
        }

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
        if (pathstr != null) {
            this.path = pathstr;
        }
        if (restHeaders != null) {
            this.headers = restHeaders.clone();
        }
    }

    /**
     * RESTLET method to retrieve the headers from a message into a list.
     * @param message The HTTP message
     * @return Header list
     */
    @SuppressWarnings("unchecked")
    private static Series<Header> getMessageHeaders(final Message message) {
        final ConcurrentMap<String, Object> attrs = message.getAttributes();
        Series<Header> headers = (Series<Header>) attrs.get(HEADERS_KEY);
        if (headers == null) {
            headers = new Series(Header.class);
            final Series<Header> prev = (Series<Header>)
                attrs.putIfAbsent(HEADERS_KEY, headers);
            if (prev != null) { headers = prev; }
        }
        return headers;
    }

    /**
     * Parse the REST message input and replace the template with given input.
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
    public RESTEvent invokeMessage() throws UnexpectedEventException {
        try {
            String rPath = parseData(this.path);
            // Instantiate the client connector, and configure it.
            Client client = new Client(new Context(), Protocol.HTTPS);
            client.getContext().getParameters().add("useForwardedForHeader","false");

            this.url = url + rPath;
            System.out.println(this.url);
            final ClientResource clientRes =   new ClientResource(url);
            clientRes.setNext(client);
            System.out.println(clientRes.getReference().getHostDomain());
            System.out.println(clientRes.getReference().getHostPort());
            System.out.println(clientRes.getReference().getPath());
            if (headers != null) {
                for (Parameter param : headers) {
                    param.setValue(parseData(param.getValue()));
                    getMessageHeaders(clientRes.getRequest()).add(param.getName(), param.getValue());
                }
            }

            /**
             * Where there is a body message to build.
             */
            StringRepresentation entity = null;
            MediaType mediaType = null;
            if (this.dataBody.getData() != null) {
                if (this.dataBody.getData().length() > 0) {
                    this.dataBody.setData(parseData(this.dataBody.getData()));
                    entity = new StringRepresentation(this.dataBody.getData());
                    entity.setCharacterSet(null);
                    if (this.dataBody.getType().equalsIgnoreCase(XML_LABEL)) {
                        entity.setMediaType(MediaType.APPLICATION_XML);
                                clientRes.accept(MediaType.APPLICATION_XML);
                                mediaType = MediaType.APPLICATION_XML;
                    } else if (this.dataBody.getType().equalsIgnoreCase(JSON_LABEL)) {
                        entity.setMediaType(MediaType.APPLICATION_JSON);
//                        clientRes.accept(MediaType.APPLICATION_JSON);
                        for (Parameter param : headers) {
                            if (param.getName().equalsIgnoreCase("Accept")) {
                                String mType = param.getValue();
                                mediaType = MediaType.valueOf(mType);
                            }
                        }
                        if (mediaType == null) {
                            mediaType = MediaType.APPLICATION_JSON;

                        }
                        clientRes.accept(mediaType);
                    } else if (this.dataBody.getType().equalsIgnoreCase(OTHER_LABEL)) {
                        String applicationType = null;
                        for (Parameter param : headers) {
                            if (param.getName().equalsIgnoreCase("Content-type")) {
                                applicationType = param.getValue();
                            }
                            if (param.getName().equalsIgnoreCase("Accept")) {
                                String mType = param.getValue();
                                mediaType = MediaType.valueOf(mType);
                            }
                        }
                        if(applicationType != null) {
                            entity.setMediaType(MediaType.valueOf(applicationType));
                        }
                        if(mediaType != null) {
                            clientRes.accept(mediaType);
                        }
                    }
                }
            }
            long time=0;
            try {
                time = System.currentTimeMillis();
                switch(method) {
                    case GET_LABEL: clientRes.get();
                        break;
                    case POST_LABEL: clientRes.post(entity);
                        break;
                    case PUT_LABEL: clientRes.put(entity);
                       break;
                    case DELETE_LABEL: clientRes.delete();
                       break;
                 }
                time = System.currentTimeMillis() - time;
            } catch (ResourceException excep) {
                ServiceLogger.LOG.error("Error constructing HTTP message", excep);
            }
            Response response = clientRes.getResponse();
            return fromResponse(response, mediaType, time);
        } catch (InvalidRESTMessage ex) {
            throw new UnexpectedEventException(ex.getMessage(), ex);
        } catch (InvalidPatternReferenceException ex) {
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
    private static RESTEvent fromResponse(final Response response, MediaType acceptType, long time)
        throws InvalidRESTMessage {
        final RESTEvent rResp = new RESTEvent();
        try {
            /*
            * Create a REST event about the Service Response i.e. capture and
            * uniform the data to be understood by the state machine rule checker
            */

            rResp.addParameter(new Parameter(RESTEvent.RESPONSE_TIME, Long.toString(time)));
            rResp.addParameter(new Parameter(RESTEvent.HTTP_FROM, response.getServerInfo().getAddress()));
            rResp.addParameter(new Parameter(RESTEvent.HTTP_TO, SystemProperties.getIP()));
            rResp.addParameter(new Parameter(RESTEvent.HTTP_MSG, RESTEvent.REPLY_LABEL));
            rResp.addParameter(new Parameter(RESTEvent.HTTP_CODE, Integer.toString(response.getStatus().getCode())));

            // Build the headers from the HTTP headers
            final Series<Header> headers = (Series<Header>) response.getAttributes().get("org.restlet.http.headers");

            /**
             * Complete failure case - the request has not got a response
             */
            if(headers == null)
                return rResp;

            for (Header h : headers) {
                rResp.addParameter(new Parameter(RESTEvent.HTTP_CONFIG_HEAD + h.getName().toLowerCase(), h.getValue()));
            }

            final Representation msgContent = response.getEntity();
            MediaType mediaType = msgContent.getMediaType();
            if (mediaType != null) {
                rResp.addContent(mediaType.getName(), response.getEntityAsText());
            } else {
                mediaType = acceptType;
                rResp.addContent(mediaType.getName(), response.getEntityAsText());
                rResp.addParameter(new Parameter("http.content-type", mediaType.getName()));
            }

        } catch (ConfigurationException ex) {
            throw new InvalidRESTMessage("Failed to build event from HTTP Response", ex);
        } catch (IOException ex) {
            throw new InvalidRESTMessage("Failed to read message received", ex);
        }
        return rResp;
    }
}
