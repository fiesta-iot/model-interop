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
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables;

/**
 * The pattern input must conform to the XML schema. When data received
 * does not match the schema and the data cannot be processed then this
 * exception is thrown with a description of the required format.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class InvalidXMLInputException extends Exception {

    /**
     * Invalid input of xml data or xpath expression; evaluate the correctness
     * of the data.
     * @param exceptionMessage The error message to carry.
     */
    public InvalidXMLInputException(final String exceptionMessage) {
        super(exceptionMessage);
    }

    /**
     * Invalid input of xml data or xpath expression; evaluate the correctness
     * of the data. Carries the stack trace.
     * @param exceptionMessage The error message to carry.
     * @param excep The carried stack trace.
     */
    public InvalidXMLInputException(final String exceptionMessage, final Exception excep) {
        super(exceptionMessage, excep);
    }
}
