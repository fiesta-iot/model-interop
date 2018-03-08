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

/**
 * An event occurring during the execution of the state machine that does not
 * match the expected behaviour.
 * For example, a message not matching a guard.
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class UnexpectedEventException extends Exception {
    /**
     * Construct a new unexpected exception.
     */
    public UnexpectedEventException() {
        super();
    }

    /**
     * Error in the execution of the state machine. Event happened that
     * is not modelled.
     * @param exceptionMessage The qualifying error message.
     */
    public UnexpectedEventException(final String exceptionMessage) {
        super(exceptionMessage);
    }

    /**
     * Error in the execution of the state machine. Event happened that
     * is not modelled.
     * @param exceptionMessage The qualifying error message.
     * @param excep The stack trace.
     */
    public UnexpectedEventException(final String exceptionMessage, final Exception excep) {
        super(exceptionMessage, excep);
    }
}
