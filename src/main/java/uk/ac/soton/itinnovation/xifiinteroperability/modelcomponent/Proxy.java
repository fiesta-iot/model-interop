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
//	Created By :			Paul Grace
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.xifiinteroperability.modelcomponent;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.routing.Redirector;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.EventCapture;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.SystemProperties;

/**
 * Proxy wrapper around a REST Service Endpoint. Takes an existing
 * REST service and constructs a new redirect service.
 *
 * Pre and Post behaviour can then be added around the redirect call.
 * Essentially this is a delegator pattern around existing restful services
 * without needing to re-implement or redeploy the actual service. For
 * example you can delegate calls to the Google API.
 *
 * N.b. this is written in such a way so not to be a security threat. Original
 * calls to the API are not redirected - applications must target this
 * proxy.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class Proxy extends Application {

    /**
     * The proxy wrapper. This is the final instance, hence the data
     * cannot be changed once the proxy component has been instantiated.
     * However, its deployment can be started and stopped.
     */
    private final transient Component proxyComponent;

    /**
     * The generated URL of the wrapper which is used by remote applications
     * to interact with the target API via the wrapper.
     */
    private transient URL proxyURL;


    /**
     * Construct a new instance of a wrapper around a REST API interface
     * instance at a given URI location. For example to construct a wrapper
     * of the Flickr Rest API listening on the localhost at 8111:
     *
     * Proxy("http://api.flickr.com/services/rest", Protocol.http, 8111);
     *
     * @param target The host address of the REST API in String format e.g.
     * "http://www.myrestapiexample.com:8080"
     * @param protocol The protocol to use to wrap the interface e.g. a http
     * wrappper is Protocol.http
     * @param port The port number the interface wrapper is to be deployed upon
     * on e.g. 10345 n.b. this need not match the port of the real interface.
     * @param stm The state machine interface to push events to
     * @throws InvalidWrapperException error creating the proxy.
     * @throws Exception General error in the redirect process.
     */
    public Proxy(final URL target, final org.restlet.data.Protocol  protocol,
            final int port, final EventCapture stm)
                                    throws InvalidWrapperException, Exception {
        super();
        proxyComponent = new Component();
        proxyComponent.getClients().add(protocol);
        proxyComponent.getClients().add(Protocol.HTTPS);

        // Create the proxy Restlet
        final AOPRedirector proxy = new AOPRedirector(
                proxyComponent.getContext().createChildContext(),
                target.toExternalForm(),
                Redirector.MODE_SERVER_OUTBOUND, stm);

        // Set the component roots
        proxyComponent.getDefaultHost().attach("", proxy);
        proxyComponent.getServers().add(protocol, port);

        try {
            proxyURL = new URL(protocol.getTechnicalName() + "://" + SystemProperties.getIP() + ":" + port);
        } catch (MalformedURLException ex) {
            throw new InvalidWrapperException("Could not create a proxy URL for the given parameters", ex);
        } catch (UnknownHostException ex) {
            throw new InvalidWrapperException("Unknown host in the given parameters", ex);
        }
    }

    /**
     * Return the URL of the wrapper. That is display the interface of the proxy
     * that applications can interact with. This is a web restful interface
     * and hence we use a URL as opposed to a general URI.
     * @return The URL of the hosted wrapper interface
     * @see java.net.URL
     */
    public final URL getWrapperURL() {
        return this.proxyURL;
    }

    /**
     * Start the operation of the proxy wrapper. Until this is called the
     * proxy will not respond to API calls. If there is an error starting the
     * proxy then an exception is generated.
     * @throws WrapperDeploymentException Error during start of proxy.
     */
    public final void startup() throws WrapperDeploymentException {
        try {
            // Create the server connectors
            proxyComponent.start();

        } catch (Exception ex) {
            ServiceLogger.LOG.error("Error starting REST Interface proxy:"
                    + this.proxyURL.toExternalForm(), ex);
            throw new WrapperDeploymentException(ex.getMessage(), ex);
        }
    }

    /**
     * Stop the operation of the proxy wrapper. After this is called the proxy
     * will not respond to incoming requests.
     * @throws WrapperDeploymentException Error during shutdown of proxy.
     */
    public final void shutdown() throws WrapperDeploymentException {
        try {
            // Create the server connectors
            Thread.sleep(1000);
            proxyComponent.stop();
        } catch (Exception ex) {
            ServiceLogger.LOG.error("Error shutting down REST Interface proxy:"
                    + this.proxyURL.toExternalForm(), ex);
            throw new WrapperDeploymentException(ex.getMessage(), ex);
        }
    }
}
