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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.EventCapture;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InteroperabilityReport;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MsgEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.UnexpectedEventException;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections.CollectionsBrowserForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.ExecutionPanel;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.Guard;

/**
 * The java data representation of a set of states that form a state machine.
 * This executable in response to discreet events.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class StateMachine implements EventCapture {
    /**
     * References to two states that we need to operate the state
     * machine: the first state, and the current state.
     */

    /**
     * Each state machine can have only one current state.
     */
    private transient State currentState;

    /**
     * Each state machine has only one start state.
     */
    private transient State firstState;

    /**
     * Synchronised blocking queue for execution input. That is,
     * the only input to this machine comes via this queue. The RESTLET
     * framework captures REST operations and pushes them as events to
     * this queue.
     */
    private final transient BlockingQueue<MsgEvent> eventQueue;

    /**
     * Each state machine is a set of states. We use a hash map to
     * perform transitions. Label - toState. Query toState. Set
     * currentState to this state. etc...
     */
    private transient Map<String, State> states;

    /**
     * Each state machine when executed traces an output report.
     */
    private final transient InteroperabilityReport outputReport;
    
    /**
     * a getter for the test report
     * @return the report object
     */
    public InteroperabilityReport getReport(){
        return outputReport;
    }

    /**
     * a boolean to represent if the test was manually stopped by the user
     */
    private transient boolean stopped;

    /**
     * a setter for the stopped attribute, stops the state machine
     */
    public void stop(){
        stopped = true;
    }

    /**
     * a boolean to represent if we are in debug mode or not
     */
    private volatile boolean debugMode;

    /**
     * a boolean to represent if next button is clicked
     */
    private volatile boolean nextClicked;

    /**
     * a method to force the state machine to continue execution
     */
    public void next(){
        nextClicked = true;
    }

    /**
     * a boolean to represent if the test has ended
     */
    private transient boolean finished;

    /**
     * a getter for the finished attribute
     * @return True if the test has finished or has been stopped
     */
    public boolean isFinished(){
        return finished;
    }

    /**
     * a reference to the execution panel used for testing animation
     */
    private transient ExecutionPanel execPanel;
    
    /**
     * a reference to the collections explorer panel used for multiple tests execution
     */
    private transient CollectionsBrowserForm collectionsExplorer;

    /**
     * a getter for the collections explorer reference
     * @return the collectionsExplorer reference
     */
    public CollectionsBrowserForm getCollectionsExplorer(){
        return collectionsExplorer;
    }
    
    /**
     * Construct a new state machine and create and interoperability report.
     * @param debugMode whether the state machine is in debug mode or not
     */
    public StateMachine(boolean debugMode) {
        this.eventQueue = new ArrayBlockingQueue(50);
        outputReport = new InteroperabilityReport();
        stopped = false;
        this.debugMode = debugMode;
        nextClicked = true;
        finished = false;
    }

    /**
     * the same constructor as the one above, however it also sets the execution panel reference
     * @param debugMode whether the state machine is in debug mode or not
     * @param execPanel reference to the execution panel
     * @param collectionsExplorer reference to the collections explorer
     */
    public StateMachine(boolean debugMode, ExecutionPanel execPanel, CollectionsBrowserForm collectionsExplorer){
        this(debugMode);
        this.execPanel = execPanel;
        this.collectionsExplorer = collectionsExplorer;
    }

    /**
     * Create a new state machine with the interoperability report output
     * already provided.
     * @param rep The interoperability report reference.
     * @param debugMode whether the state machine is in debug mode or not
     * @param execPanel reference to the execution panel
     * @param collectionsExplorer reference to the collections explorer
     */
    public StateMachine(final InteroperabilityReport rep, boolean debugMode, ExecutionPanel execPanel, CollectionsBrowserForm collectionsExplorer) {
        this.eventQueue = new ArrayBlockingQueue(50);
        outputReport = rep;
        stopped = false;
        this.debugMode = debugMode;
        nextClicked = true;
        finished =false;
        this.execPanel = execPanel;
        this.collectionsExplorer = collectionsExplorer;
    }

    /**
     * Construct a new state machine with a first state and the
     * remaining set of states.
     *
     * @param firstSt The label of the first state.
     * @param stateSet The set of states to create a machine from.
     */
    public final void inputContent(final String firstSt, final Map<String, State> stateSet) {
        this.states = new HashMap();
        for (State s : stateSet.values()) {
            addState(s);
        }
        this.firstState = getState(firstSt);
    }


    /**
     * Assign a state as the current executable position in the state machine.
     * @param state The current position state.
     */
    public final void setCurrentState(final State state) {
        this.currentState = state;
    }

    /**
     * Return the current executable position in the state machine.
     * @return state The current position state.
     */
    public final State getCurrentState() {
        return this.currentState;
    }

    /**
     * Get the start state. Used for beginning the execution phase.
     * @return The fixed start state reference.
     */
    public final State getStartState() {
        return firstState;
    }

    /**
     * Add a new state to the state machine.
     * @param newState The state object to add.
     */
    public final void addState(final State newState) {
	this.states.put(newState.getLabel().toLowerCase(), newState);
    }

    /**
     * Event interface implemenation. When a discrete event is detected it is
     * sent here to move the state machine execution.
     * @param restEvent The new event detected in the distributed system e.g. a
     * rest message.
     */
    @Override
    public final void pushEvent(final MsgEvent restEvent) {
        try {
            this.eventQueue.put(restEvent);
        } catch (InterruptedException ex) {
            ServiceLogger.LOG.debug("Event queue interupted", ex);
        }
    }

    /**
     * Report an exception. Potentially this method could be added into
     * the execution phase, so the state machine can deal with exceptions.
     * @param excep The exception observed.
     */
    @Override
    public final void logException(final Exception excep) {
        outputReport.println("Interoperability Error: " + excep.getMessage());
    }

    /**
     * Start the trace and begin outputting the event tests that correspond
     * to the state machine checks.
     * @return The string version of the output report.
     */
    public final InteroperabilityReport start()	{
	currentState = this.firstState;
        if (currentState == null) {
            outputReport.println("Invalid test model -> no valid start state");
            outputReport.setSuccess("false");
            outputReport.addReport("{\"Begin Testing\":\"Error in test model\"");
            return outputReport;
        }

        if(execPanel != null) {
            execPanel.setTestState(currentState.getLabel());
        }

        outputReport.clear();
        outputReport.println("Test started - run the application");
        outputReport.println("----------------------------------");
        outputReport.println("Starting trace at Node:" + currentState.getLabel());

        while (!(currentState.isEndNode() || stopped)) {
            if (debugMode && !nextClicked){
                continue;
            }
            try {
                if (currentState.isTrigger()) {
                    currentState = getState(currentState.executeTransition(this.eventQueue, outputReport));
                }
                else if (currentState.isLoop()) {
                    /**
                     * Evaluate the counter
                     */
                    String tState = currentState.evaluateConditionalTransition(outputReport, currentState.getLabel());
                    if(tState.equalsIgnoreCase(currentState.getLabel())) {
                        /**
                         * Current state - Trigger transition
                         */
                        currentState.counter(1);
                        currentState = getState(currentState.executeTransition(this.eventQueue, outputReport));
                    }
                    else {
                        State nextState = getState(tState);
                        if(nextState.isDataLoop()) {
                            MsgEvent event = getState(nextState.getEventLabel()).getStoredEvent();
                            while(true) {
                                String stateLabel = currentState.evaluateTransition(event, outputReport);

                                String cState = currentState.evaluateConditionalTransition(outputReport, currentState.getLabel());
                                if(!stateLabel.equalsIgnoreCase(cState)){
                                    break;
                                }
                                State testState = getState(stateLabel);
                                if(!testState.getTransitions().isEmpty()){
                                    tState = testState.evaluateTransition(event, outputReport);
                                    currentState.counter(1);
                                    break;
                                }

                                currentState.counter(1);
                            }
//                            tState = currentState.evaluateConditionalTransition(outputReport, currentState.getLabel());
                        }
                        currentState = getState(tState);
                    }
                }
                else {
                    // check for a timeout guard
                    Long timeout = null;
                    Transition timeoutTransition = null;
                    for(Transition transition: currentState.getTransitions()){
                        List<Guard> guards = transition.listGuards();
                        if (guards.size() == 1){
                            Guard guard = guards.get(0);
                            if (guard.getGuardLabel().equalsIgnoreCase("timeout")){
                                timeout = Long.parseLong(guard.getGuardCompare());
                                timeoutTransition = transition;
                                break;
                            }
                        }
                    }

                    MsgEvent event = null;
                    if (timeout == null){
                        while(event == null){
                            event = this.eventQueue.poll(2000, TimeUnit.MILLISECONDS);
                            if (stopped){
                                break;
                            }
                        }
                        if (stopped){
                            continue;
                        }
                        currentState = getState(currentState.evaluateTransition(event, outputReport));
                    }
                    else {
                        TimerDialog timerDialog = new TimerDialog();
                        timerDialog.initGUI(timeout);
                        event = this.eventQueue.poll(timeout, TimeUnit.MILLISECONDS);
                        if (event == null){
                            currentState = getState(timeoutTransition.readLabel());
                        }
                        else {
                            timerDialog.dispose();
                            currentState = getState(currentState.evaluateTransition(event, outputReport));
                        }
                    }
                    
                    if (currentState == null) {
                        ServiceLogger.LOG.error("Invalid state machine - could not find next state");
                        outputReport.setSuccess("false");
                        outputReport.addReport("{\"Test trace\":\"Invalid state machine - could not find next state, check traces\"");
                        finished = true;
                        return outputReport;
                    }
                    outputReport.println("Transition Success - move to state:" + currentState.getLabel());
                }
                nextClicked = false;
                if(execPanel != null) {
                    execPanel.setTestState(currentState.getLabel());
                }
            } catch (UnexpectedEventException ex) {
               logException(ex);
               outputReport.setSuccess("false");
               outputReport.addReport("{\"Test trace\":\""+ ex.getLocalizedMessage() + "\"");
               finished = true;
               return outputReport;
            } catch (InterruptedException ex) {
                ServiceLogger.LOG.error("Error processing events", ex);
                outputReport.setSuccess("false");
                outputReport.addReport("{\"Test trace\":\""+ ex.getLocalizedMessage() + "\"");
                finished = true;
                return outputReport;
            } catch (Exception ex){
                outputReport.setSuccess("false");
                outputReport.addReport("{\"Test trace\":\""+ ex.getLocalizedMessage() + "\"");
                outputReport.println("An unexpected error occurred, while running your pattern. Please double check your test model.");
                finished = true;
                return outputReport;
            }
        }

        if (!stopped){
            outputReport.setSuccess(currentState.getSuccess());
            outputReport.addReport(currentState.getReport());
            outputReport.println("End node reached --> Interoperability Testing Complete");
        }
        else {
            outputReport.setSuccess("false");
            outputReport.println("The test execution was stopped.");
        }
        finished = true;
        return outputReport;
    }

    /**
     * Given the label id, retrieve a state from the state machine.
     * @param label The id of the state - the label.
     * @return The state object.
     */
    public final State getState(final String label) {
        return this.states.get(label.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Create a string representation of the state machine object.
     * @return The string description.
     */
    @Override
    public final String toString() {
	String sep = "";
	final StringBuilder sBuilder = new StringBuilder();
	for (String key : states.keySet()) {
	    sBuilder.append(sep)
		.append(states.get(key).toString());
	    sep = "\n";
	}
	return sBuilder.toString();
    }
}