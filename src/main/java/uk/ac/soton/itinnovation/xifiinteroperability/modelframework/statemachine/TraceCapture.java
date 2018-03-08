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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.EventCapture;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MsgEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;

/**
 * Capture a series of event, these can then be written to a file so
 * that they can be replayed multiple times through the state machines.
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class TraceCapture implements EventCapture {

    /**
     * The list of events in the repeatable trace.
     */
    private final transient List<MsgEvent> resourceName;

    /**
     * The file location of the trace.
     */
    private final transient String fileName;

    /**
     * Create a trace capture object.
     * @param fileLocation The location to store the trace to.
     */
    public TraceCapture(final String fileLocation) {
        this.resourceName = new ArrayList();
        this.fileName = fileLocation;
    }

    @Override
    public final void pushEvent(final MsgEvent httpMessage) {
        resourceName.add(httpMessage);
    }

    @Override
    public final void logException(final Exception excep) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Serialise the trace and store it to file.
     */
    public final void storeTrace() {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try {
            fout = new FileOutputStream(this.fileName);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(this.resourceName);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Unable to write trace to file", ex);
        }  finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (IOException ex) {
                ServiceLogger.LOG.error("Unable to write trace to file", ex);
            }
        }

    }
}
