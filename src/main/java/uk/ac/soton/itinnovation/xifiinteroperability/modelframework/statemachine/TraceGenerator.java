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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MsgEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;

/**
 * Create a trace of HTTP events from raw data held on a file.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public final class TraceGenerator {
    /**
     * Utility class with private constructor.
     */
    private TraceGenerator() {
        // empty implementation
    }

    /**
     * Generate a set of REST events that have been saved to a file.
     *
     * @param rawDataFile The file with raw msg events stored as a trace
     * @param sMachine The state machine pattern that the events will be tested
     * against,
     */
    public static void generateEvents(final String rawDataFile, final StateMachine sMachine) {

        ArrayList<MsgEvent> fileContent = null;

        // Open the trace file
        try {
            final InputStream file = new FileInputStream(rawDataFile);
            final InputStream input = new BufferedInputStream(file);
            final ObjectInputStream httpEvents = new ObjectInputStream(input);
            fileContent = (ArrayList<MsgEvent>) httpEvents.readObject();
        } catch (IOException e) {
            ServiceLogger.LOG.error("Unable to open trace file: " + rawDataFile, e);
            return;
        } catch (ClassNotFoundException ex) {
            ServiceLogger.LOG.error("Unable to open class file: " + rawDataFile, ex);
            return;
        }

        if (fileContent != null) {
            // Read an HTTP event one at a time until the end of the file
            for (MsgEvent e : fileContent) {
                // Notify the state machine of the event
                sMachine.pushEvent(e);
            }
        }
    }
}
