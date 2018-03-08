/**
 * Copyright 2005-2013 Restlet S.A.S.
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 *
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 *
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 *
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 *
 * Restlet is a registered trademark of Restlet S.A.S.
 */

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

package uk.ac.soton.itinnovation.xifiinteroperability.modelcomponent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;

/**
 * IT Innovation Modification
 * Author: Paul Grace
 * Contact: pjg@it-innovation.soton.ac.uk
 */
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.EventCapture;
import org.restlet.engine.header.Header;
import org.restlet.routing.Template;
import org.restlet.util.Series;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.RESTEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;

/**
 * Rewrites URIs then redirects the call or the client to a new destination.
 * There are various redirection modes that you can choose from: client-side
 * redirections ({@link #MODECLNTFOUND}, {@link #MODECLIENTPERM},
 * {@link #MODECLNTSEEOTHER}, {@link #MODECLIENTTEMP}) or
 * server-side redirections, similar to a reverse proxy (
 * {@link #MODESRVOUTBOUND} and {@link #MODESRVINBOUND}).<br>
 * <br>
 * When setting the redirection URIs, you can also used special URI variables to
 * reuse most properties from the original request as well as URI template
 * variables. For a complete list of properties, please see the {@link Resolver}
 * class. For example "/target?referer={fi}" would redirect to the relative URI,
 * inserting the referrer URI as a query parameter.<br>
 * <br>
 * To create a reverse proxy, a typically configuration will use the
 * {@link #MODESRVOUTBOUND} constant and a target URI like
 * "http://targetHost/targetRootPath/{rr}" to ensure that all child URIs are
 * properly redirected as well, "rr" appending the remaining part of the current
 * request URI that hasn't been routed yet.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 *
 * @see org.restlet.routing.Template
 * @see <a href="http://wiki.restlet.org/docs_2.1/375-restlet.html">User Guide -
 *      URI rewriting and redirection</a>
 * @author Jerome Louvel
 */
public final class AOPRedirector extends Restlet {
    /**
     * In this mode, the client is permanently redirected to the URI generated
     * from the target URI pattern, using the
     * {@link Status#REDIRECTION_PERMANENT} status. Note: this is a client-side
     * redirection.<br>
     *
     * @see Status#REDIRECTION_PERMANENT
     */
    public static final int MODECLIENTPERM = 1;

    /**
     * In this mode, the client is simply redirected to the URI generated from
     * the target URI pattern using the {@link Status#REDIRECTION_FOUND} status.
     * Note: this is a client-side redirection.<br>
     *
     * @see Status#REDIRECTION_FOUND
     */
    public static final int MODECLNTFOUND = 2;

    /**
     * In this mode, the client is simply redirected to the URI generated from
     * the target URI pattern using the {@link Status#REDIRECTION_SEE_OTHER}
     * status. Note: this is a client-side redirection.<br>
     *
     * @see Status#REDIRECTION_SEE_OTHER
     */
    public static final int MODECLNTSEEOTHER = 3;

    /**
     * In this mode, the client is temporarily redirected to the URI generated
     * from the target URI pattern using the
     * {@link Status#REDIRECTION_TEMPORARY} status. Note: this is a client-side
     * redirection.<br>
     *
     * @see Status#REDIRECTION_TEMPORARY
     */
    public static final int MODECLIENTTEMP = 4;

    /**
     * In this mode, the call is sent to {@link Application#getOutboundRoot()}
     * or if null to {@link Context#getClientDispatcher()}. Once the selected
     * client connector has completed the request handling, the response is
     * normally returned to the client. In this case, you can view the
     * {@link Redirector} as acting as a transparent server-side proxy. Note:
     * this is a server-side redirection.<br>
     * <br>
     * Warning: remember to add the required connectors to the parent
     * {@link Component} and to declare them in the list of required connectors
     * on the {@link Application#getConnectorService()} property.<br>
     * <br>
     * Note that in this mode, the headers of HTTP requests, stored in the
     * request's attributes, are removed before dispatching. Also, when a HTTP
     * response comes back the headers are also removed.
     *
     * @see Application#getOutboundRoot()
     * @see Context#getClientDispatcher()
     */
    public static final int MODESRVOUTBOUND = 6;

    /**
     * In this mode, the call is sent to {@link Context#getServerDispatcher()}.
     * Once the selected client connector has completed the request handling,
     * the response is normally returned to the client. In this case, you can
     * view the Redirector as acting as a transparent proxy Restlet. Note: this
     * is a server-side redirection.<br>
     * <br>
     * Warning: remember to add the required connectors to the parent
     * {@link Component} and to declare them in the list of required connectors
     * on the {@link Application#getConnectorService()} property.<br>
     * <br>
     * Note that in this mode, the headers of HTTP requests, stored in the
     * request's attributes, are removed before dispatching. Also, when a HTTP
     * response comes back the headers are also removed.
     *
     * @see Context#getServerDispatcher()
     */
    public static final int MODESRVINBOUND = 7;

    /** The target URI pattern. */
    private Map<String, String> targetTemplate;

    /** The redirection mode. */
    private int mode;

    /**
     * Added field. The reference to the state machine push event interface, i.e.
     * the redirector creates events and passes them to the state machine.
     */
    private transient EventCapture stateMachine;

    /**
     * Constructor for the client dispatcher mode.
     *
     * @param context
     *            The context.
     * @param targTemplate
     *            The template to build the target URI.
     * @param stm Event capture interface
     * @see org.restlet.routing.Template
     */
    public AOPRedirector(final Context context, final String targTemplate, final EventCapture stm) {
        this(context, targTemplate, MODESRVOUTBOUND, stm);
    }

    /**
     * Constructor.
     *
     * @param context
     *            The context.
     * @param targetPattern
     *            The pattern to build the target URI (using StringTemplate
     *            syntax and the CallModel for variables).
     * @param modeOp
     *            The redirection modeOp.
     * @param stm Event capture interface
     *
     */
    public AOPRedirector(final Context context, final String targetPattern, final int modeOp, final EventCapture stm) {
        super(context);

        this.targetTemplate = new HashMap();
        setTargetTemplate(targetPattern);
        this.mode = modeOp;
        this.stateMachine = stm;
    }

    /**
     * Sets the target URI pattern.
     *
     * @param targTemplate
     *            The target URI pattern.
     */
    public void setTargetTemplate(final String targTemplate) {
        try {
            final URL urlTarget = new URL(targTemplate);
            this.targetTemplate.put(urlTarget.getPath(), targTemplate);
        } catch (MalformedURLException ex) {
            ServiceLogger.LOG.error("Error invalid target URL");
        }
    }

    /**
     * Gets the target URI pattern.
     * @param targetUri The The original URL.
     * @return The path extracted from the URL.
     */
    public String getTargetTemplate(final URL targetUri) {
        try {
            final String intfName = "/" + targetUri.getPath().split("/")[1];
            return this.targetTemplate.get(intfName);
        } catch (Exception e) {
            return targetUri.toExternalForm();
        }

    }

    /**
     * Returns the redirection mode.
     *
     * @return The redirection mode.
     */
    public int getMode() {
        return this.mode;
    }

    /**
     * Returns the target reference to redirect to by automatically resolving
     * URI template variables found using the {@link Template} class using the
     * request and response as data models.
     *
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The target reference to redirect to.
     */
    protected Reference getTargetRef(final Request request, final Response response) {
        // Create the template

        final String targetTemp = getTargetTemplate(request.getOriginalRef().toUrl());
        if (targetTemp == null) {
            return null;
        }

        final Template rtemplate = new Template(targetTemp);

        // Return the formatted target URI
        if (new Reference(targetTemp).isRelative()) {
            // Be sure to keep the resource's base reference.
            return new Reference(request.getResourceRef(), rtemplate.format(request,
                    response));
        }
        return new Reference(rtemplate.format(request, response));
    }

    /**
     * Handles a call by redirecting using the selected redirection mode.
     *
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public synchronized void handle(final Request request, final Response response) {
        // Generate the target reference
        Reference targetRef = getTargetRef(request, response);
        if (targetRef == null) {
            targetRef = request.getResourceRef();
        }
        /**
         * IT Innovation Modification
         * Author: Paul Grace
         *
         * Call the pre method interceptor to observe/process the incoming
         * HTTP request message.
         */
        long reqTime = 0;
        try {
            pre(request);
            reqTime = System.currentTimeMillis();
        } catch (Exception ex) {
            /**
             * We have to catch exceptions and then pass them to the pattern
             * engine before they are lost in restlet specific handlers
             */
            this.stateMachine.logException(ex);
            /**
             * The handle method should continue as normal i.e. don't return
             * return.
             */
        }

        /**
         * End modification
         */
        switch (this.mode) {
        case MODECLIENTPERM:
            if (request.isLoggable()) {
                ServiceLogger.LOG.info(
                        "Permanently redirecting client to: " + targetRef);
            }

            response.redirectPermanent(targetRef);
            break;

        case MODECLNTFOUND:
            if (request.isLoggable()) {
                ServiceLogger.LOG.info(
                        "Redirecting client to found location: " + targetRef);
            }

            response.setLocationRef(targetRef);
            response.setStatus(Status.REDIRECTION_FOUND);
            break;

        case MODECLNTSEEOTHER:
            if (request.isLoggable()) {
                ServiceLogger.LOG.info(
                        "Redirecting client to another location: " + targetRef);
            }

            response.redirectSeeOther(targetRef);
            break;

        case MODECLIENTTEMP:
            if (request.isLoggable()) {
                ServiceLogger.LOG.info(
                        "Temporarily redirecting client to: " + targetRef);
            }

            response.redirectTemporary(targetRef);
            break;

        case MODESRVOUTBOUND:
            if (request.isLoggable()) {
                ServiceLogger.LOG.info(
                        "Redirecting via client dispatcher to: " + targetRef);
            }

            outboundServerRedirect(targetRef, request, response);
            break;

        case MODESRVINBOUND:
            if (request.isLoggable()) {
                ServiceLogger.LOG.info(
                        "Redirecting via server dispatcher to: " + targetRef);
            }

            inboundServerRedirect(targetRef, request, response);
            break;
        }

        /**
         * IT Innovation modification
         * Author: Paul Grace
         *
         * Call the post interceptor: void method as this may be made an
         * asynchronous call if necessary.
         */
        long responseTime = System.currentTimeMillis() - reqTime;
        try {
            post(request, response, responseTime);
        } catch (WrapperDeploymentException ex) {
            /**
             * We have to catch exceptions and then pass them to the pattern
             * engine before they are lost in restlet specific handlers
             */
            this.stateMachine.logException(ex);
            /**
             * The handle method should continue as normal i.e. don't return
             * return.
             */
        }
        /**
         * End modification
        /**
         * End modification
         */
    }

    /**
     * Redirects a given call to a target reference. In the default
     * implementation, the request HTTP headers, stored in the request's
     * attributes, are removed before dispatching. After dispatching, the
     * response HTTP headers are also removed to prevent conflicts with the main
     * call.
     *
     * @param targetRef
     *            The target reference with URI variables resolved.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void inboundServerRedirect(final Reference targetRef, final Request request,
            final Response response) {
        serverRedirect(getContext().getServerDispatcher(), targetRef, request,
                response);
    }

    /**
     * Redirects a given call to a target reference. In the default
     * implementation, the request HTTP headers, stored in the request's
     * attributes, are removed before dispatching. After dispatching, the
     * response HTTP headers are also removed to prevent conflicts with the main
     * call.
     *
     * @param targetRef
     *            The target reference with URI variables resolved.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void outboundServerRedirect(final Reference targetRef, final Request request,
            final Response response) {
        Restlet next = (getApplication() == null) ? null : getApplication()
                .getOutboundRoot();

        if (next == null) {
            next = getContext().getClientDispatcher();
        }

        serverRedirect(next, targetRef, request, response);
        if (response.getEntity() != null
                && !request.getResourceRef().getScheme()
                        .equalsIgnoreCase(targetRef.getScheme())) {
            // Distinct protocol, this data cannot be exposed.
            response.getEntity().setLocationRef((Reference) targetRef);
        }
    }

    /**
     * Optionally rewrites the response entity returned in the
     * {@link #MODESRVINBOUND} and {@link #MODESRVOUTBOUND} modes. By
     * default, it just returns the initial entity without any modification.
     *
     * @param initialEntity
     *            The initial entity returned.
     * @return The rewritten entity.
     */
    protected Representation rewrite(final Representation initialEntity) {
        return initialEntity;
    }

    /**
     * Redirects a given call on the server-side to a next Restlet with a given
     * target reference. In the default implementation, the request HTTP
     * headers, stored in the request's attributes, are removed before
     * dispatching. After dispatching, the response HTTP headers are also
     * removed to prevent conflicts with the main call.
     *
     * @param next
     *            The next Restlet to forward the call to.
     * @param targetRef
     *            The target reference with URI variables resolved.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected void serverRedirect(final Restlet next, final Reference targetRef,
            final Request request, final Response response) {
        if (next == null) {
            ServiceLogger.LOG.debug(
                    "No next Restlet provided for server redirection to "
                            + targetRef);
        } else {
            // Save the base URI if it exists as we might need it for
            // redirections
            final Reference resourceRef = request.getResourceRef();
            final Reference baseRef = resourceRef.getBaseRef();

            // Reset the protocol and let the dispatcher handle the protocol
            request.setProtocol(null);

            // Update the request to cleanly go to the target URI
            final int resourcePos = targetRef.toString().indexOf("/", 10);
            final StringBuffer url = new StringBuffer(targetRef.toString().substring(0, resourcePos) + resourceRef.getPath());
            if (request.getResourceRef().hasQuery()) {
                    url.append('?').append(request.getResourceRef().getQuery());
            }
            request.setResourceRef(new Reference(url.toString()));

            request.getAttributes().remove(HeaderConstants.ATTRIBUTE_HEADERS);
            next.handle(request, response);

            // Allow for response rewriting and clean the headers
            response.setEntity(rewrite(response.getEntity()));
            response.getAttributes().remove(HeaderConstants.ATTRIBUTE_HEADERS);
            request.setResourceRef(resourceRef);

            response.setLocationRef(url.toString());

            // In case of redirection, we may have to rewrite the redirect URI
            if (response.getLocationRef() != null) {
               final Template rTemplate = new Template(this.getTargetTemplate(resourceRef.getTargetRef().toUrl()));
                rTemplate.setLogger(getLogger());
                final int matched = rTemplate.parse(response.getLocationRef().toString(), request);

                if (matched > 0) {
                    final String remainingPart = (String) request.getAttributes()
                            .get("rr");

                    if (remainingPart != null) {
                        response.setLocationRef(baseRef.toString()
                                + remainingPart);
                    }
                }
            }
        }
    }

    /**
     * Sets the redirection modeOp.
     *
     * @param modeOp
     *            The redirection modeOp.
     */
    public void setMode(final int modeOp) {
        this.mode = modeOp;
    }


    /**
     * IT Innovation Modification: two new private methods pre and post
     * to implement interceptor behaviour around a http redirect
     */

    /**
     * Perform post processing on a HTTP response received after the completion
     * of a HTTP Invocation: Post, GET, etc.
     *
     * @param req The HTTP response message response received after the
     * invocation
     * @param response The HTTP response that the system has returned in response
     * to the request
     * @throws WrapperDeploymentException Error when reading the response from the wrapper.
     */
    private void post(final Request req, final Response response, long time) throws WrapperDeploymentException {

        /*
         * Create a REST event about the Service Response i.e. capture and
         * uniform the data to be understood by the state machine rule checker
         */
        final RESTEvent rResp = new RESTEvent();
        rResp.setResponseTime(time);
        rResp.addParameter(new Parameter(RESTEvent.RESPONSE_TIME, Long.toString(time)));
        rResp.addParameter(new Parameter(RESTEvent.HTTP_FROM, response.getServerInfo().getAddress()));
        rResp.addParameter(new Parameter(RESTEvent.HTTP_TO, req.getClientInfo().getAddress()));
        rResp.addParameter(new Parameter(RESTEvent.HTTP_MSG, RESTEvent.REPLY_LABEL));
        rResp.addParameter(new Parameter(RESTEvent.HTTP_CODE, Integer.toString(response.getStatus().getCode())));

        // Build the headers from the HTTP headers
        final Series<Header> headers = (Series<Header>) response.getAttributes().get("org.restlet.http.headers");
        if (headers != null) {
            for (Header h : headers) {
                rResp.addParameter(new Parameter(RESTEvent.HTTP_CONFIG_HEAD + h.getName().toLowerCase(), h.getValue()));
            }
        }
        // Build the body data structure
        /*
         * Extract the parameter content. RESTLET reads off the stream, so the
         * data must be rewritten or the response to the actual client
         * will be interfered with.
         */
        if (response.isEntityAvailable()) {
            final String contentType = response.getEntity().getMediaType().getName();
            final String msgBody = response.getEntityAsText();
            rResp.addParameter(new Parameter(RESTEvent.HTTP_CONFIG_HEAD + "content-type", contentType));
            rResp.addContent(contentType, msgBody);
            // Get the local parameters from the attached REST form
            try {
                final Form iForm = new Form(msgBody);
                for (org.restlet.data.Parameter parameter : iForm) {
                    final Parameter toCast = new Parameter(parameter.getName(), parameter.getValue());
                    rResp.addParameter(toCast);
                }
            } catch (Exception ex) {
                ServiceLogger.LOG.error("Error creating response event for state machine", ex);
            }
            response.setEntity(msgBody, response.getEntity().getMediaType());
        }

        // Push the create event
        try {
            if (this.stateMachine != null) {
                this.stateMachine.pushEvent(rResp);
            }
        } catch (NullPointerException ex) {
            throw new WrapperDeploymentException("Event capture interface not"
                    + "configured for the wrapper: " + response.getLocationRef().toString(), ex);
        }

    }

    /**
     * The pre method simply captures the event that occurs in an genuine
     * interaction between a HTTP client and service and then creates a
     * REST Event object which it pushes to the state machine.
     * @param request The Restlet Request object to read and build an event from
     */
    private void pre(final Request request) {
        try {
            /**
             * This method cannot fail (as it is part of redirect and must
             * completely transparent. Hence we capture exceptions and move
             * forward as a void method.
             */

            // Build the basic information
            final RESTEvent rReq = new RESTEvent();

            final String sTarget = this.getTargetTemplate(request.getOriginalRef().toUrl());
            if (sTarget == null) {
                return;
            }
            final URL target = new URL(sTarget);
            rReq.addParameter(new Parameter(RESTEvent.HTTP_FROM, request.getClientInfo().getAddress()));
            rReq.addParameter(new Parameter(RESTEvent.HTTP_TOINTERFACE, target.toExternalForm()));
            rReq.addParameter(new Parameter(RESTEvent.HTTP_TO, target.getHost()));
            rReq.addParameter(new Parameter(RESTEvent.HTTP_MSG, request.getMethod().getName()));

            // Build the headers from the HTTP headers
            final Series<Header> headers = (Series<Header>) request.getAttributes().get("org.restlet.http.headers");
            if (headers != null) {
                for (Header h : headers) {
                    rReq.addParameter(new Parameter(RESTEvent.HTTP_CONFIG_HEAD + h.getName().toLowerCase(), h.getValue()));
                }
            }
            // Build the message parameters from the FORM input
            final Form form = request.getResourceRef().getQueryAsForm();
            for (org.restlet.data.Parameter parameter : form) {
                final Parameter toCast = new Parameter("query." + parameter.getName(), parameter.getValue());
                rReq.addParameter(toCast);
            }

            // Build the body data structure
            if (request.isEntityAvailable()) {
                final String contentType = request.getEntity().getMediaType().getName();
                final String msgBody = request.getEntityAsText();
                rReq.addContent(contentType, msgBody);
                // Get the local parameters from the attached REST form
                final Form iForm = new Form(msgBody);
                for (org.restlet.data.Parameter parameter : iForm) {
                    final Parameter toCast = new Parameter(parameter.getName(), parameter.getValue());
                    rReq.addParameter(toCast);
                }
                request.setEntity(msgBody, request.getEntity().getMediaType());
            }

            // Push the constructed event to the state machine
            if (this.stateMachine != null) {
                this.stateMachine.pushEvent(rReq);
            }
        } catch (Exception ex) {
            // Catch all exceptions so not as to interupt the RESTLet trace
            ServiceLogger.LOG.error("Invalid monitor redirect", ex);
        }
    }

    /**
     * End modification
     */
}
