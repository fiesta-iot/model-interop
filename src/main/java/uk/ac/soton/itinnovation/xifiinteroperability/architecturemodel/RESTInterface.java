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

package uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel;

import io.moquette.server.MQTTProxy;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.proxy.resources.ForwardingResource;
import org.eclipse.californium.proxy.resources.ProxyCoapClientResource;
import org.jdom.Element;
import uk.ac.soton.itinnovation.xifiinteroperability.ConfigurationException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelcomponent.InvalidWrapperException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelcomponent.Proxy;
import uk.ac.soton.itinnovation.xifiinteroperability.modelcomponent.WrapperDeploymentException;
import uk.ac.soton.itinnovation.xifiinteroperability.SystemProperties;

/**
 * A RESTInterface is a representation of a HTTP REST endpoint. The input
 * is taken directly from the XML ADL pattern specification.
 *
 This generates a RESTLET supported proxy that captures all events (REST
 messages sent to that interface), and then forwards them to a component
 capturing and processing these events (for the Interoperability tool this
 is state machine processing engine).

 A RESTInterface describes a valid url otherwise a InvalidInterfaceException
 is thrown.

 Where a proxy cannot be created for a given interface (e.g. due to protocol
 and port issues, or firewall problems) then a WrapperDeploymentException
 is thrown
 *
 * @author Paul Grace
 */
public class RESTInterface {

    // Static constants that match the data to the XML specifcation.

    /** XML tag string url to relate to the URL field. */
    private static final String URLXMLTAG = "url";

    /** XML tag string interface id to relate to the URL field. */
    private static final String INTERFACEIDXMLTAG = "id";

    /** XML tag string protocol to relate to the URL field. */
    private static final String PROTOCOLXMLTAG = "protocol";

    /**
     * Data content specific to the building of the interface representation
        - The url to use the interface
        - The port to access the interface.
     */
    private final transient String url;

    /**
     * Access the URL field value of this object.
     * @return The URL field in full form.
     */
    public final String getURL() {
        return url;
    }

    /**
     * The unique identifier of this interface. Specified by the user.
     */
    private final transient String interfaceID;
    private CoapServer coapProxy = null;
    private MQTTProxy mqttProxy = null;

    /**
     * Access the Identifier field value of this object.
     * @return The unique id of the interface.
     */
    public final String getInterface() {
        return interfaceID;
    }

    /**
     * The unique identifier of this interface. Specified by the user.
     */
    private final transient String protocol;

    /**
     * Access the protocol field value of this object.
     * @return The protocol (http, coap, etc.).
     */
    public final String getProtocol() {
        return protocol;
    }

    /**
     * The port number of the interface. This is not accessed outside the class.
     */
    private final transient int port;

    /**
     * a getter for the port number of the interface
     * @return the port number attribute
     */
    public int getPort(){
        return port;
    }

    /**
     * Proxy representations of the interfaces Proxy for normal HTTP; SSLProxyBack
     * for https urls.
     */
    private transient Proxy interfaceRedirect;

    /**
     * Access to the proxy reference of this interface.
     * @return The proxy object that is managing the redirects to the specified
     * interface.
     */
    public final Proxy getInterfaceProxy() {
        return interfaceRedirect;
    }

    /**
     * Each interface captures REST events; these can be forwarded on via
     * this eventcallback interface. Typically the state machine will read
     * events from this reference.
     */
    private final transient EventCapture pushEvents;

    /**
     * Return the interface for passing events to the statemachine.
     * @return The event interface.
     */
    public final EventCapture getEventInterface() {
        return pushEvents;
    }

    /**
     * Create a data object describing a REST interface in the architectural
     * pattern.
     *
     * @param eltIntIndex The XML DOM element containing the <url>tag value
     * @param capture The notification interface where events from this interface
     * are sent to.
     * @throws InvalidInterfaceException Error creating the proxy from specification.
     */
    public RESTInterface(final Element eltIntIndex, final EventCapture capture)
            throws InvalidInterfaceException {
        try {
            url = eltIntIndex.getChildText(URLXMLTAG);
            String changedURL = url;
            if(url.startsWith("coap")) {
                changedURL = url.replaceFirst("coap", "http");
            }

            URL urlInFormat = new URL(changedURL);
            this.interfaceID = eltIntIndex.getChildText(INTERFACEIDXMLTAG);
            this.protocol = eltIntIndex.getChildText(PROTOCOLXMLTAG);

            this.port = SystemProperties.getAvailablePort(urlInFormat.getPort());

            this.pushEvents = capture;

            addProxy(urlInFormat);

        } catch (MalformedURLException ex) {

            throw new InvalidInterfaceException("REST interface specification error (must be http://address:port)", ex);
        } catch (ConfigurationException ex) {
            throw new InvalidInterfaceException("Specified port fail", ex);
        } catch (InvalidWrapperException ex) {
            throw new InvalidInterfaceException("Could not create a proxy", ex);
        }
    }

    /**
     * Build the proxy for this interface, based on the protocol type (http or
     * https).
     * @throws InvalidWrapperException Error building the proxy.
     */
    private void addProxy(URL urlIn) throws InvalidWrapperException {
        try {
            switch (this.protocol) {
                case "coap":
                    String newUrl = "coap://" + urlIn.getHost() + ":" + port + urlIn.getPath();
                    String path = null;
                    if(urlIn.getPath()!=null) {
                        path = urlIn.getPath();
                        if(path.length()>1) {
                            path = path.substring(1);
                        }
                    }
                    ForwardingResource coap2coap = new ProxyCoapClientResource(path, newUrl, this.pushEvents);

                    // Create CoAP Server on PORT with proxy resources form CoAP to CoAP and HTTP
                    coapProxy = new CoapServer(port);
                    coapProxy.add(coap2coap);
                    coapProxy.start();
                    break;
                case "mqtt":
                    String mqttUrl = "tcp://" + urlIn.getHost() + ":" + port;
                    System.out.println("URL: " + mqttUrl);

                    mqttProxy = new MQTTProxy(urlIn.getHost(), urlIn.getPort(), this.pushEvents);
                    mqttProxy.startServer(pushEvents);
                    break;
                default: interfaceRedirect = new Proxy(urlIn, org.restlet.data.Protocol.HTTP,
                    port, pushEvents);
                    interfaceRedirect.startup();
            }
        } catch (Exception ex) {
            throw new InvalidWrapperException(ex.getMessage(), ex);
        }
    }

    /**
     * Release the resources employed by the proxy of the interface.
     * @throws WrapperDeploymentException Error releasing the proxy resources.
     */
    public final void release() throws WrapperDeploymentException {
        if (this.interfaceRedirect != null) {
            this.interfaceRedirect.shutdown();
        }
        if (this.coapProxy != null) {
            coapProxy.stop();
            coapProxy.destroy();
        }
        if (this.mqttProxy != null) {
            mqttProxy.stopServer();
        }
    }
}
