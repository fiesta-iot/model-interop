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

import java.util.List;
import java.util.Locale;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;

import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Architecture;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.InvalidPatternReferenceException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

/**
 * The RESTMessage object is a test event. It stores the content for a test
 * message to be used for firing test messages at a REST service. Hence, it
 * will contain information for a request message (POST, GET, etc..)
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Paul Grace
 */
public class COAPMessage extends ProtocolMessage{

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
    public final String getMethod() {
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
    public final String getURL() {
        return url;
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
    public COAPMessage(final String urlstr, final String pathstr, final String methodStr, final String type, final String body,
            final Parameter[] restHeaders, final Architecture arcContext) throws InvalidRESTMessage {

        this.stateMachine = arcContext;
        /**
         * Method must be either: get, post, put, delete
         */
        if (methodStr == null) {
            throw new InvalidRESTMessage("Method cannot be null");
        }

        if ((methodStr.equalsIgnoreCase(RESTMessage.GET_LABEL)) || (methodStr.equalsIgnoreCase(RESTMessage.POST_LABEL)) ||
                (methodStr.equalsIgnoreCase(RESTMessage.DELETE_LABEL)) || (methodStr.equalsIgnoreCase(RESTMessage.PUT_LABEL))) {
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

    public int getContentFormatNumber(String contentType) {
        switch (contentType) {
            case "text/plain": return 0;
            case "application/xml": return 41;
            case "application/json": return 50;
        }
        /**
         * Default to plain text for all other formats
         */
        return 0;
    }

    public String getContentFormatString(int contentType) {
        switch (contentType) {
            case 0: return "text/plain";
            case 41: return "application/xml";
            case 50: return "application/json";
        }
        /**
         * Default to plain text for all other formats
         */
        return "text/plain";
    }

    /**
     * Create an invocation i.e. use the data in the REST message to perform
     * a full client request.
     * @return The Rest event received after the invocation complete.
     * @throws UnexpectedEventException Event not matching the state machine description.
     */
    @Override
    public final COAPEvent invokeMessage() throws UnexpectedEventException {
        try {
            String rPath = parseData(this.path);
            this.url = url + rPath;

            /**
             * Create the COAP client
             */
            CoapClient client = new CoapClient(this.url);
            Request request;
            switch (method.toUpperCase()) {
                case "GET": request = new Request(Code.GET);
                            break;
                case "POST": request = new Request(Code.POST);
                             break;
                case "PUT": request = new Request(Code.PUT);
                            break;
                case "DELETE": request = new Request(Code.DELETE);
                            break;
                default:
                    request = new Request(Code.GET);
            }
            /**
             * Add the COAP options (Headers)
             */
            OptionSet optionHeaders = new OptionSet();
            for(Parameter header: headers) {
                String optionName = header.getName();
                switch (optionName) {
                    case "Content-Format":
                        optionHeaders.addOption(new Option(OptionNumberRegistry.CONTENT_FORMAT, getContentFormatNumber(header.getValue())));
                        break;
                    case "Accept":
                        optionHeaders.addOption(new Option(OptionNumberRegistry.ACCEPT, getContentFormatNumber(header.getValue())));
                        break;
                    case "Max-Age":
                        // Integer value duration in seconds between 0 and 136.1 years. Default is 60 seconds
                         optionHeaders.addOption(new Option(OptionNumberRegistry.MAX_AGE, Long.valueOf(header.getValue())));
                        break;
                    case "ETag":
                        optionHeaders.addETag(header.getValue().getBytes());
                        break;
                    case "If-Match":
                        optionHeaders.addIfMatch(header.getValue().getBytes());
                        break;
                    case "If-None-Match":
                        optionHeaders.addOption(new Option(OptionNumberRegistry.IF_NONE_MATCH, 0));
                        break;
                    case "Size1":
                        optionHeaders.addOption(new Option(OptionNumberRegistry.SIZE1, Integer.valueOf(header.getValue())));
                        break;

                }
            }

            /**
             * Where there is a body message to build.
             */
            if (this.dataBody.getData() != null) {
                if (this.dataBody.getData().length() > 0) {
                    this.dataBody.setData(parseData(this.dataBody.getData()));
                    if (this.dataBody.getType().equalsIgnoreCase(XML_LABEL)) {
                        request.setPayload(this.dataBody.getData());
                    } else if (this.dataBody.getType().equalsIgnoreCase(JSON_LABEL)) {
                        request.setPayload(this.dataBody.getData());
                    } else if (this.dataBody.getType().equalsIgnoreCase(OTHER_LABEL)) {
                        request.setPayload(this.dataBody.getData());
                    }
                }
            }

            request.setOptions(optionHeaders);
            long time = System.currentTimeMillis();
            CoapResponse response = client.advanced(request);
            time = System.currentTimeMillis() - time;
            return fromResponse(response, time);
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
    private COAPEvent fromResponse(final CoapResponse response, long time)
        throws InvalidRESTMessage {
        final COAPEvent rResp = new COAPEvent();

        /*
        * Create a REST event about the Service Response i.e. capture and
        * uniform the data to be understood by the state machine rule checker
        */
        rResp.addParameter(new Parameter(COAPEvent.RESPONSE_TIME, Long.toString(time)));
        rResp.addParameter(new Parameter(COAPEvent.COAP_FROM, response.advanced().getSource().getHostAddress()));
        if(response.advanced().getDestination() != null) {
            rResp.addParameter(new Parameter(COAPEvent.COAP_TO, response.advanced().getDestination().getHostAddress()));
        }
        rResp.addParameter(new Parameter(COAPEvent.COAP_CODE, Integer.toString(response.getCode().value)));

        String responseText = response.getResponseText();
        // Build the headers from the COAP options
        OptionSet options = response.getOptions();

        /**
         * Complete failure case - the request has not got a response
         */
        if(options == null)
            return rResp;

        if(options.hasAccept()) {
            int accept = options.getAccept();
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Accept", "" + accept));
        }
        if(options.hasContentFormat()) {
            int cf = options.getContentFormat();
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Content-Format", "" + cf));
        }
        if(options.hasUriHost()) {
            String uHost = options.getUriHost();
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Uri-Host", uHost));
        }
        if(options.hasUriPort()) {
            Integer uPort = options.getUriPort();
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Uri-Host", "" + uPort));
        }
        if(options.hasMaxAge()) {
            Long maxAge = options.getMaxAge();
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Max-Age", "" + maxAge));
        }

        if(options.hasProxyScheme()) {
            String proxy = options.getProxyScheme();
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Proxy-Scheme", proxy));
        }
        if(options.hasProxyUri()) {
            String proxy = options.getProxyUri();
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Proxy-Uri", proxy));
        }
        if(options.hasSize1()) {
            Integer size = options.getSize1();
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Size1", "" + size));
        }

        rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "If-None-Match", "" + options.hasIfNoneMatch()));

        if(options.getIfMatchCount() > 0 ) {
            List<byte[]> ifMatch = options.getIfMatch();
            String ifMtachString="[";
            for(byte[] str: ifMatch) {
                ifMtachString += str + ";";
            }
            ifMtachString += "]";
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "If-Match", "" + ifMtachString));
        }

        if(options.getLocationPathCount() > 0 ) {
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Location-Path", options.getLocationPathString()));
        }
        if(options.getURIPathCount()> 0 ) {
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Uri-Path", options.getUriPathString()));
        }
        if(options.getURIQueryCount() > 0 ) {
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Uri-Query", options.getUriQueryString()));
        }
        if(options.getLocationPathCount() > 0 ) {
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Location-Query", options.getLocationQueryString()));
        }

        if(options.getETagCount()> 0 ) {
            List<byte[]> ifMatch = options.getETags();
            String eTagsString="[";
            for(byte[] str: ifMatch) {
                eTagsString += str + ";";
            }
            eTagsString += "]";
            rResp.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "ETag", "" + eTagsString));
        }
        final byte[] msgContent = response.getPayload();

        if (!options.hasContentFormat()) {
            rResp.addContent("TEXT/PLAIN", new String(msgContent));
            rResp.addParameter(new Parameter("coap.content-format", "TEXT/PLAIN"));
        } else {
            String mediaName = getContentFormatString(options.getContentFormat());
            rResp.addContent(mediaName, new String(msgContent));

        }
        return rResp;
    }
}
