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
// Created By : Nikolay Stanchev
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.modelframework.data;

/**
 * Error identifying an invalid regular expression in a guard
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class InvalidRegexException extends Exception {
    /**
     * Construct an invalid regex exception with a given string message.
     * @param exceptionMessage The error message passed to this exception.
     */
    public InvalidRegexException(final String exceptionMessage) {
        super(exceptionMessage);
    }

    /**
     * Construct an invalid regex exception with a given error message, and a
     * previously caught exception.
     * @param exceptionMessage The error message passed to this exception.
     * @param excep The prior caught exception.
     */
    public InvalidRegexException(final String exceptionMessage, final Exception excep) {
        super(exceptionMessage, excep);
    }
}
