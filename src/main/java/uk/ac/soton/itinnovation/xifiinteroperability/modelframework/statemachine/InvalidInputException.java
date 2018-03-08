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
 * Exception for invalid input to guard tests. The value being
 * evaluated should be checked. Where the value is coming from, and
 * in what format.
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class InvalidInputException extends Exception {

     /**
     * Exception generated when input to check against a guard is invalid.
     * @param errorMessage The specific error message in this instance
     */
    public InvalidInputException(final String errorMessage) {
        super(errorMessage);
    }

    /**
     * Exception generated when input to check against a guard is invalid.
     * @param errorMessage The specific error message in this instance
     * @param excep The sub exception to carry forward
     */
    public InvalidInputException(final String errorMessage, final Exception excep) {
        super(errorMessage, excep);
    }
}
