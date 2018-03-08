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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InteroperabilityReport;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MsgEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.UnexpectedEventException;

/**
 * A state can be one of three types in the state machine:
 * - A start node (1 and only 1 in each machine - has only outgoing transitions)
 * - A normal node (general intermediary node between transition in the
 *   machine i.e. there is at least one incoming and and at least one outgoing
 *   transition)
 * - A trigger node which must have a single transition for sending a message
 * - An end node (at least one in each machine - has only incoming transitions)
 *
 * Rules: i) The start node can be a trigger or normal state.
 * ii) An end node must not be a trigger node
 * ii) A node cannot be both a normal and trigger node
 *
 * This is the interface to query information about one of these individual
 * states in a state machine.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public interface State {

    /**
     * Enumeration defined for the three different states.
     */
    public enum StateType {
        /**
         * A start node of a state machine that waits on rest inputs.
         */
        START,
        /**
         * And end node of the state machine where termination occurs.
         */
        END,
        /**
         * A state to receive REST inputs.
         */
        NORMAL,
        /**
         * Two out transitions i) Counted trigger state: repeated action invocations
         * and ii) a counter=0 transition.
         */
        LOOP,
        /**
         * Two out transitions i) Counted trigger state: repeated action invocations
         * and ii) a counter=0 transition. Repeated loop on same event.
         */
        DATALOOP,
        /**
         * A state to send a single rest message as a test trigger.
         */
        TRIGGER,
        /**
         * A start node of a state machine that sends a test rest event trigger.
         */
        TRIGGERSTART;
    }

    /**
     * Return true if this is an end node state; otherwise return false.
     * @return boolean value indicating end node status.
     */
    boolean isEndNode();

    /**
     * Update the counter state value.
     * @param change The value to change e.g. 1 to add, -1 to subtract
     */
    void counter(int change);

    /**
     * Get the counter state value.
     * @return The counter value
     */
    int getCounter();

    /**
     * Get the success reporting state value.
     * @return The success field value
     */
    String getSuccess();

    /**
     * Get the interoperability reporting value.
     * @return The report value
     */
    String getReport();

    /**
     * Get the data loop reporting value.
     * @return The report value
     */
    String getEventLabel();

    /**
     * Return true if this is a start node state; otherwise return false.
     * @return boolean value indicating start node status.
     */
    boolean isStartNode();

    /**
     * Return true if this state is a trigger node; otherwise return false.
     * @return boolean status indicating trigger node status
     */
    boolean isTrigger();

    /**
     * Return true if this state is a loop node; otherwise return false.
     * @return boolean status indicating loop node status
     */
    boolean isLoop();

    /**
     * Return true if this state is a data loop node; otherwise return false.
     * @return boolean status indicating data loop node status
     */
    boolean isDataLoop();

    /**
     * Read the state label. Within a state machine, labels are
     * unique i.e. no two states can have the same label.
     * @return A string with the state label.
     */
    String getLabel();

    /**
     * Read the saved event. This is only available from none trigger nodes
     * and only after the state has been transitioned through.
     * @return The saved .
     */
    MsgEvent getStoredEvent();

    /**
     * Adds a guard transition between two states in the state machine. If both
     * states do not exist then an InvalidTransitionException is thrown.
     * @param newTransition The transition data to add. This is a list of guards
     * in the xml <guards> tag.
     * @throws InvalidTransitionException Error in the transition specification input.
     */
     void addTransition(Transition newTransition)
            throws InvalidTransitionException;

    /**
     * List the set of transitions possible from this state.
     * @return The list of transitions
     */
    List<Transition> getTransitions();

    /**
     * Evaluate a new event (a rest operation) against the set of transitions
     * at this state. If there is a complete match then the next state to
     * transition to is returned. If not - we have an error event (i.e.
     * an interoperability error) and hence we throw an unexpected event error.
     *
     * @param input The details of the occured event - a rest operations with
     * data and parameters to compare against the condition.
     * @param out As the transition is evaluated it reports actions to the
     * interoperability report passed here
     *
     * @return the state to move to based upon the event
     * @throws UnexpectedEventException Event detected that doesn't match the
     * behaviour described in the state machine.
     */
    String evaluateTransition(MsgEvent input, InteroperabilityReport out)
            throws UnexpectedEventException;

    /**
     * Evaluate a new event (a rest operation) against the set of transitions
     * at this state. If there is a complete match then the next state to
     * transition to is returned. If not - we have an error event (i.e.
     * an interoperability error) and hence we throw an unexpected event error.
     *
     * @param input The details of the occured event - a rest operations with
     * data and parameters to compare against the condition.
     * @param out As the transition is evaluated it reports actions to the
     * interoperability report passed here
     *
     * @return the state to move to based upon the event
     * @throws UnexpectedEventException Event detected that doesn't match the
     * behaviour described in the state machine.
     */
    String evaluateConditionalTransition(InteroperabilityReport out, String current)
            throws UnexpectedEventException;

    /**
     * Execute a rest event described by the trigger transition. That is there
     * is a single REST method to call and when this operation is called it
     * is executed in the state machine.
     * @param input The queue to store the result of the method invocation. That
     * is the queue of the overall state machine
     * @param outputReport As the transition is evaluated it reports actions to the
     * interoperability report passed here
     * @return the state to move to based upon the event
     * @throws UnexpectedEventException Event detected that doesn't match the
     * behaviour described in the state machine.
     */
    String executeTransition(BlockingQueue<MsgEvent> input, InteroperabilityReport outputReport)
            throws UnexpectedEventException;
 }