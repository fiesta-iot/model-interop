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

package uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine;

/**
 * Timeout condition: states the length of time for
 * a particular guard condition.
 * Once the timeout fires then it specifies the state in
 * the timed automaton to transition to.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class Timeout {
    /**
     * The length of the timeout guard in milliseconds.
     */
    private final transient int timeoutLength;

    /**
     * Get the length of the timeout.
     * @return The timeout length.
     */
    public final int getTimeoutLength() {
        return timeoutLength;
    }

    /**
     * The label of the state in the timed automaton to transition to.
     */
    private final transient String timeoutTo;

    /**
     * Get the transition to field on timeout.
     * @return The state label.
     */
    public final String getTimeoutState() {
        return timeoutTo;
    }

    /**
     * Create a new timeout guard.
     * @param timeout The timeout length.
     * @param failTo The condition state to exception to.
     */
    public Timeout(final int timeout, final String failTo) {
        this.timeoutLength = timeout;
        this.timeoutTo = failTo;
    }
}
