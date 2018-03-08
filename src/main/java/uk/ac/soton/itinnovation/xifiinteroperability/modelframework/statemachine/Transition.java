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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.ProtocolMessage;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.RESTMessage;

/**
 * A transition describes the transition from state of the machine
 * to another state in the machine.
 *
 * Each transition has a set of conditions (guards) that must be true i.e.
 * the transition is taken iff all conditions of the transition are matched
 * by an event that has a occurred.
 *
 * In the case of the Interoperability Test Engine: an event is a REST based
 * operation.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class Transition {

    /**
     * The label/id of the transition where directed to ie. the state label.
     */
    private transient String transitionLabel;

    /**
     * The list of guard transitions attached to the transition.
     */
    private transient List<Guard> conditions = new ArrayList();

    /**
     * The rest event where this transition is a message not a guard. The
     * transition cannot be both.
     */
    private transient ProtocolMessage event;

    /**
     * The value of the <report> tag in the transition to specify what could
     * have gone wrong/right.
     */
    private transient String interopReport;

    /**
     * Create a basic transition.
     * @param label The identifier of the transition's to i.e. the label of the
     * to state.
     */
    public Transition(final String label) {
        this.transitionLabel = label;
    }

    /**
     * Create a guard transition.
     * @param label The to label.
     * @param guards The list of guards to evaluate events against.
     */
    public Transition(final String label, final List<Guard> guards, String report) {
        this(label);
        this.conditions = guards;
        this.interopReport = report;
    }

    /**
     * Create a message transition.
     * @param label The to label.
     * @param rMesg The message to be sent when transition is triggered.
     */
    public Transition(final String label, final ProtocolMessage rMesg, String report) {
        this(label);
        this.event = rMesg;
        this.interopReport = report;
    }

    /**
     * Get the to label of the state transition is directed at.
     * @return The string id of the to state.
     */
    public final String readLabel() {
        return this.transitionLabel;
    }

    /**
     * Set the message trigger.
     * @param rMesg The Rest message to send.
     */
    public final void setTrigger(final RESTMessage rMesg) {
        this.event = rMesg;
    }

    /**
     * Get the REST message of the trigger.
     * @return The Rest message object.
     */
    public final ProtocolMessage getTrigger() {
        return this.event;
    }

    /**
     * Add a new guard to the transition.
     * @param guard The guard rule.
     * @throws InvalidGuardException Error in the guard specification.
     */
    public final void addGuard(final Guard guard) throws InvalidGuardException {
        this.conditions.add(guard);
    }

    /**
     * Get all the guards on the transition.
     * @return The list of guards.
     */
    public final List<Guard> listGuards() {
        return this.conditions;
    }

    /**
     * Get a guard with a specific id.
     * @param label The label/id of the guard.
     * @return The specific guard instance.
     */
    public final Guard getGuard(final String label) {
        final ListIterator<Guard> listIterate = this.conditions.listIterator();
        while (listIterate.hasNext()) {
            final Guard nextIter = listIterate.next();
            if (nextIter.getGuardLabel().equals(label)) {
                return nextIter;
            }
        }
        return null;
    }

    /**
     * Add the report field for the transition.
     * @param report The report string.
     */
    public final void addReport(final String report) {
        this.interopReport = report;
    }

    /**
     * Get the report field for this transition.
     * @return The report as a single string.
     */
    public final String getReport() {
        return this.interopReport;
    }
}
