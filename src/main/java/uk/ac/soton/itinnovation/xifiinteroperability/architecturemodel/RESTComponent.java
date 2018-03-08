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

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

/**
 * Each architecture is made up of REST components. These exchange REST
 * messages using HTTP operations (typically).
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class RESTComponent {

    // Static constants related to the XML tag types in the pattern spec:

    /**
     * The unique identifier of the component within the specification. This is
     * not a globally unique ID used outside the tool e.g. a GUID.
     */
    private final transient String componentID;

    /**
     * Getter for the componentID.
     * @return The value of this architecture component's ID in the pattern.
     */
    public final String getComponentID() {
        return componentID;
    }

    /**
     * The public ip address of the component, such that it can be recongnised
     * and communicated with.
     *
     * - currently the tool recognises IPv4 addresses. However, the String type
     * will allow migration to both v4 and v6 addresses.
     */
    private final transient String ipAddress;

    /**
     * Getter for the ip address of this component.
     * @return The value of this architecture component's IP in the pattern.
     */
    public final String getipAddress() {
        return ipAddress;
    }

    /**
     * The list of 0 or more interface provided by the component e.g. the
     * URL of the REST interface where GET, POST, etc. operations are targetted.
     */
    private final transient List<RESTInterface> interfaces;

    /**
     * Getter for the list of 0 or more interface provided by the component e.g. the
     * URL of the REST interface where GET, POST, etc. operations are targeted.
     * @return The interfaces as a simple list.
     */
    public final List<RESTInterface> getInterfaces() {
        return interfaces;
    }

    /**
     * Construct a new component instance using the DOM <component> tag content.
     *
     * @param eltIndex The DOM element of the component.
     * @param capture The event capture interface.
     * @throws InvalidArchitectureException Error indicating error in specifying
     * .new architecture component.
     */
    public RESTComponent(final Element eltIndex, final EventCapture capture)
        throws InvalidArchitectureException {

        this.interfaces = new ArrayList();

        // Get the state label
        componentID = eltIndex.getChildText(XMLStateMachine.ID_LABEL);
        if (componentID == null) {
            throw new InvalidArchitectureException("Component ID canot be null");
        }
        ipAddress = eltIndex.getChildText(XMLStateMachine.ADDRESS_LABEL);
        if (ipAddress == null) {
            throw new InvalidArchitectureException("Component IP address cannot be null");
        }

        final List<Element> xmlStates = eltIndex.getChildren(XMLStateMachine.INTERFACE_LABEL);
        for (Element eltIntfIndex : xmlStates) {
            try {
                addInterface(new RESTInterface(eltIntfIndex, capture));
            } catch (InvalidInterfaceException ex) {
                throw new InvalidArchitectureException("Invalid Interface on Component", ex);
            }
        }
    }

    /**
     * Add an interface to this component.
     * @param newInterface The interface to add to the component's list.
     */
    private void addInterface(final RESTInterface newInterface) {
        interfaces.add(newInterface);
    }
}
