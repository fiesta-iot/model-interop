/*******************************************************************************
 * Copyright (c) 2015 Institute for Pervasive Computing, ETH Zurich and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and re-implementation
 *    Francesco Corazza - HTTP cross-proxy
 ******************************************************************************/
package org.eclipse.californium.proxy.resources;

import java.util.List;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.proxy.CoapTranslator;
import org.eclipse.californium.proxy.TranslationException;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.EventCapture;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.COAPEvent;


/**
 * Resource that forwards a coap request with the proxy-uri option set to the
 * desired coap server.
 */
public class ProxyCoapClientResource extends ForwardingResource {

    /**
     * The address that this proxy forwards messages to
     */
    private final String UriForwarder;

    /**
    * The reference to the state machine push event interface, i.e.
    * the redirector creates events and passes them to the state machine.
    */
    private transient EventCapture stateMachine;


    /**
     * Construct a specific instance of this proxy for a given URI
     * @param name The proxy name (ID)
     * @param URI The URI of the target of the proxy
     * @param stm The test pointer observing the interaction where the intercepted messages are sent.
     */
    public ProxyCoapClientResource(String name, String URI, final EventCapture stm) {
            // set the resource hidden
            super(name, true);
            getAttributes().setTitle("Forward the requests to a CoAP server.");
            UriForwarder = URI;
            this.stateMachine = stm;
    }

    @Override
    public Response forwardRequest(Request request) {
        System.out.println("ProxyCoAP2CoAP forwards "+request);
            Request incomingRequest = request;

		// check the invariant: the request must have the proxy-uri set
//		if (!incomingRequest.getOptions().hasProxyUri()) {
//			LOGGER.warning("Proxy-uri option not set.");
//			return new Response(ResponseCode.BAD_OPTION);
//		}

		// remove the fake uri-path
		// FIXME: HACK // TODO: why? still necessary in new Cf?
		incomingRequest.getOptions().clearUriPath();
                incomingRequest.getOptions().setProxyUri(UriForwarder);
//                incomingRequest.setURI(UriForwarder);

		// create a new request to forward to the requested coap server
		Request outgoingRequest = null;
		try {
                    // create the new request from the original
                    outgoingRequest = CoapTranslator.getRequest(incomingRequest);
                    pre(incomingRequest, outgoingRequest);

//			// enable response queue for blocking I/O
//			outgoingRequest.enableResponseQueue(true);

                    // get the token from the manager // TODO: necessary?
//			outgoingRequest.setToken(TokenManager.getInstance().acquireToken());

                    // execute the request
                    LOGGER.finer("Sending coap request.");
//			outgoingRequest.execute();
                    LOGGER.info("ProxyCoapClient received CoAP request and sends a copy to CoAP target");
                    outgoingRequest.send();

                    // accept the request sending a separate response to avoid the
                    // timeout in the requesting client
                    LOGGER.finer("Acknowledge message sent");
		} catch (TranslationException e) {
			LOGGER.warning("Proxy-uri option malformed: " + e.getMessage());
			return new Response(CoapTranslator.STATUS_FIELD_MALFORMED);
		} catch (Exception e) {
			LOGGER.warning("Failed to execute request: " + e.getMessage());
			return new Response(ResponseCode.INTERNAL_SERVER_ERROR);
		}

		try {
			// receive the response // TODO: don't wait for ever
			Response receivedResponse = outgoingRequest.waitForResponse();

			if (receivedResponse != null) {
				LOGGER.finer("Coap response received.");

				// create the real response for the original request
				Response outgoingResponse = CoapTranslator.getResponse(receivedResponse);
                                post(outgoingResponse);
				return outgoingResponse;
			} else {
				LOGGER.warning("No response received.");
				return new Response(CoapTranslator.STATUS_TIMEOUT);
			}
		} catch (InterruptedException e) {
			LOGGER.warning("Receiving of response interrupted: " + e.getMessage());
			return new Response(ResponseCode.INTERNAL_SERVER_ERROR);
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
     * The pre method simply captures the event that occurs in an genuine
     * interaction between a HTTP client and service and then creates a
     * REST Event object which it pushes to the state machine.
     * @param request The Restlet Request object to read and build an event from
     */
    private void pre(final Request originalRequest, final Request incomingRequest) {
        try {
            /**
             * This method cannot fail (as it is part of redirect and must
             * completely transparent. Hence we capture exceptions and move
             * forward as a void method.
             */

            // Build the basic information
            final COAPEvent rReq = new COAPEvent();

            rReq.addParameter(new Parameter(COAPEvent.COAP_FROM, originalRequest.getSource().getHostAddress()));
            rReq.addParameter(new Parameter(COAPEvent.COAP_TOINTERFACE, incomingRequest.getURI()));
            rReq.addParameter(new Parameter(COAPEvent.COAP_TO, incomingRequest.getDestination().getHostAddress()));
            rReq.addParameter(new Parameter(COAPEvent.COAP_MSG, incomingRequest.getCode().toString()));

            // Build the headers from the HTTP headers
            OptionSet options = incomingRequest.getOptions();

            if(options.hasAccept()) {
                int accept = options.getAccept();
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Accept", "" + accept));
            }
            if(options.hasContentFormat()) {
                int cf = options.getContentFormat();
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Content-Format", "" + cf));
            }
            if(options.hasUriHost()) {
                String uHost = options.getUriHost();
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Uri-Host", uHost));
            }
            if(options.hasUriPort()) {
                Integer uPort = options.getUriPort();
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Uri-Host", "" + uPort));
            }
            if(options.hasMaxAge()) {
                Long maxAge = options.getMaxAge();
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Max-Age", "" + maxAge));
            }

            if(options.hasProxyScheme()) {
                String proxy = options.getProxyScheme();
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Proxy-Scheme", proxy));
            }
            if(options.hasProxyUri()) {
                String proxy = options.getProxyUri();
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Proxy-Uri", proxy));
            }
            if(options.hasSize1()) {
                Integer size = options.getSize1();
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Size1", "" + size));
            }

            rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "If-None-Match", "" + options.hasIfNoneMatch()));

            if(options.getIfMatchCount() > 0 ) {
                List<byte[]> ifMatch = options.getIfMatch();
                String ifMtachString="[";
                for(byte[] str: ifMatch) {
                    ifMtachString += str + ";";
                }
                ifMtachString += "]";
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "If-Match", "" + ifMtachString));
            }

            if(options.getLocationPathCount() > 0 ) {
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Location-Path", options.getLocationPathString()));
            }
            if(options.getURIPathCount()> 0 ) {
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Uri-Path", options.getUriPathString()));
            }
            if(options.getURIQueryCount() > 0 ) {
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Uri-Query", options.getUriQueryString()));
            }
            if(options.getLocationPathCount() > 0 ) {
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "Location-Query", options.getLocationQueryString()));
            }

            if(options.getETagCount()> 0 ) {
                List<byte[]> ifMatch = options.getETags();
                String eTagsString="[";
                for(byte[] str: ifMatch) {
                    eTagsString += str + ";";
                }
                eTagsString += "]";
                rReq.addParameter(new Parameter(COAPEvent.COAP_CONFIG_HEAD + "ETag", "" + eTagsString));
            }
            final byte[] msgContent = incomingRequest.getPayload();
            if(incomingRequest.getPayloadSize()>0) {
                if (!options.hasContentFormat()) {
                    rReq.addContent("TEXT/PLAIN", new String(msgContent));
                    rReq.addParameter(new Parameter("coap.content-format", "TEXT/PLAIN"));
                } else {
                    String mediaName = getContentFormatString(options.getContentFormat());
                    rReq.addContent(mediaName, new String(msgContent));

                }
            }
            if (this.stateMachine != null) {
                this.stateMachine.pushEvent(rReq);
            }
        } catch (Exception ex) {
            // Catch all exceptions so not as to interupt the RESTLet trace
            ServiceLogger.LOG.error("Invalid monitor redirect", ex);
            return;
        }
    }

    /**
     * Create a REST Event used by the interoperability tool state machine from
     * the RESTLET Response generated by invoking this REST Message.
     * @param response The HTTP msg response received
     * @return A generated Rest event object.
     * @throws InvalidRESTMessage Error creating event from message.
     */
    private void post(final Response response) {
        try{
            final COAPEvent rResp = new COAPEvent();

            /*
            * Create a REST event about the Service Response i.e. capture and
            * uniform the data to be understood by the state machine rule checker
            */
    //        rResp.addParameter(new Parameter(COAPEvent.RESPONSE_TIME, Long.toString(time)));
            if(response.getSource() != null) {
                rResp.addParameter(new Parameter(COAPEvent.COAP_FROM, response.getSource().getHostAddress()));
            }
            if(response.getDestination() != null) {
                rResp.addParameter(new Parameter(COAPEvent.COAP_TO, response.getDestination().getHostAddress()));
            }
            rResp.addParameter(new Parameter(COAPEvent.COAP_CODE, Integer.toString(response.getCode().value)));
            rResp.addParameter(new Parameter(COAPEvent.COAP_MSG, response.getCode().toString()));

            // Build the headers from the COAP options
            OptionSet options = response.getOptions();

            /**
             * Complete failure case - the request has not got a response
             */
            if(options == null)
                return ;

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
            if (this.stateMachine != null) {
                this.stateMachine.pushEvent(rResp);
            }
        }
        catch(Exception e) {
            return;
        }
    }
}
