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
//	Created By :			Paul Grace
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.State;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.StateNode;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.InvalidStateMachineException;

/**
 * Set of tests for ensuring that the code for managing state behaviour in
 * the state machine remains correct.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class StateNodeTest {

    /**
     * Constant for node names.
     */
    private static final String TESTVALUE = "test";

    /**
     * Constant for display msg on failure.
     */
    private static final String OUTPUTMESSAGE = "Code should not have failed - check state constructor";

    /**
     * Test if nodes match types in the state machine.
     */
    @Test
    public final void testStartNode() {
        try {
            new StateNode(null, StateNode.StateType.START, null, null, null, null);
            Assert.fail("StateNode didn't throw invalidStateMachineException");
        } catch (InvalidStateMachineException e) {
            ServiceLogger.LOG.info("Code correctly captures exception " + e);
        }

        try {
            new StateNode(TESTVALUE, null, null, null, null, null);
            Assert.fail("StateNode didn't throw invalidStateMachineException");
        } catch (InvalidStateMachineException e) {
            ServiceLogger.LOG.info("Code correctly captures exception " + e);
        }

        try {
            new StateNode("t", StateNode.StateType.START, null, null, null, null);
            Assert.fail("StateNode didn't throw invalidStateMachineException");
        } catch (InvalidStateMachineException e) {
            ServiceLogger.LOG.info("Code correctly captures exception " + e);
        }

        State sFirst;
        try {
            sFirst = new StateNode(TESTVALUE, StateNode.StateType.START, null, null, null, null);
            Assert.assertEquals(sFirst.isEndNode(), false);
            Assert.assertEquals(sFirst.isStartNode(), true);
            Assert.assertEquals(sFirst.getLabel(), TESTVALUE);
            Assert.assertEquals(sFirst.isTrigger(), false);
            Assert.assertNotNull(sFirst.getTransitions());
            Assert.assertEquals(sFirst.getTransitions().size(), 0);
        } catch (InvalidStateMachineException ex) {
            Assert.fail(OUTPUTMESSAGE);
        }

    }

    /**
     * Test the end node code.
     */
    @Test
    public final void testEndNode() {
        try {
            final State sFirst = new StateNode("test", StateNode.StateType.END, null, null, null, null);

            Assert.assertEquals(sFirst.isEndNode(), true);
            Assert.assertEquals(sFirst.isStartNode(), false);
            Assert.assertEquals(sFirst.isTrigger(), false);
        } catch (InvalidStateMachineException ex) {
            Assert.fail(OUTPUTMESSAGE);
        }
    }

    /**
     * Test the normal node code.
     */
    @Test
    public final void testNormalNode() {
        try {
            State sFirst = new StateNode(TESTVALUE, StateNode.StateType.NORMAL, null, null, null, null);

            Assert.assertEquals(sFirst.isEndNode(), false);
            Assert.assertEquals(sFirst.isStartNode(), false);
            Assert.assertEquals(sFirst.isTrigger(), false);
        } catch (InvalidStateMachineException ex) {
            Assert.fail(OUTPUTMESSAGE);
        }
    }

    /**
     * Test the trigger node code.
     */
    @Test
    public final void testTriggerNode() {
        try {
            State sFirst = new StateNode(TESTVALUE, StateNode.StateType.TRIGGER, null, null, null, null);

            Assert.assertEquals(sFirst.isEndNode(), false);
            Assert.assertEquals(sFirst.isStartNode(), false);
            Assert.assertEquals(sFirst.isTrigger(), true);
        } catch (InvalidStateMachineException ex) {
            Assert.fail(OUTPUTMESSAGE);
        }
    }

    /**
     * Test the triggerstart node code.
     */
    @Test
    public final void testTriggerStartNode() {
        try {
            State sFirst = new StateNode(TESTVALUE, StateNode.StateType.TRIGGERSTART, null, null, null, null);

            Assert.assertEquals(sFirst.isEndNode(), false);
            Assert.assertEquals(sFirst.isStartNode(), true);
            Assert.assertEquals(sFirst.isTrigger(), true);
        } catch (InvalidStateMachineException ex) {
            Assert.fail(OUTPUTMESSAGE);
        }
    }

    /**
     * Test the trigger node code.
     */
    @Test
    public final void addTriggerTransition() {
        try {

            final State sFirst = new StateNode(TESTVALUE, StateNode.StateType.TRIGGERSTART, null, null, null, null);
            Assert.assertEquals(sFirst.isEndNode(), false);
            Assert.assertEquals(sFirst.isStartNode(), true);
            Assert.assertEquals(sFirst.isTrigger(), true);
        } catch (InvalidStateMachineException ex) {
            Assert.fail(OUTPUTMESSAGE);
        }
    }

}
