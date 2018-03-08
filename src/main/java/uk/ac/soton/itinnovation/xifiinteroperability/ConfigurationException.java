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

package uk.ac.soton.itinnovation.xifiinteroperability;

/**
 * Exception that occurs when there is a problem configuring the
 * Interoperability tool before hosting as a service. Generally, this will be
 * because the properties file may contain invalid entries and/or insufficient
 * entries.
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 *
 */
public class ConfigurationException extends Exception {

    /**
     * Exception that adds a configuration error message for clarity.
     * @param errorMsg The specific error explanation.
     * @param excep The caught exception.
     */
    public ConfigurationException(final String errorMsg, final Exception excep) {
        super(errorMsg, excep);
    }
    /**
     * Contains the configuration error message without keeping the prior
     * caught exception data.
     * @param errorMsg The error message to attach to the exception.
     */
    public ConfigurationException(final String errorMsg) {
        super(errorMsg);
    }
}
