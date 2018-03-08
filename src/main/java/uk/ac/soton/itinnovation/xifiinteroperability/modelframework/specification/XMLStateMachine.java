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

package uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Architecture;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.Parameter;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.RESTComponent;
import uk.ac.soton.itinnovation.xifiinteroperability.architecturemodel.RESTInterface;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.COAPMessage;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MQTTMessage;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.Guard;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MsgEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.RESTMessage;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.State;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.StateMachine;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.StateNode;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.Transition;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.InvalidStateMachineException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.InvalidStateTypeException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine.InvalidTransitionException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidGuard;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidRESTMessage;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.ProtocolMessage;

/**
 * XML operations specific to the creation of a state machine in memory
 * from an xml document describing the states see the schema: statemachine.xsd.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public final class XMLStateMachine {

    /**
     * XML state tag label constant.
     */
    public static final String STATE_LABEL = "state";

    /**
     * XML success tag label constant.
     */
    public static final String SUCCESS_LABEL = "success";

    /**
     * XML transition tag label constant.
     */
    public static final String TRANSITION_LABEL = "transition";

    /**
     * XML label tag label constant.
     */
    public static final String LABEL_LABEL = "label";

    /**
     * XML type tag label constant.
     */
    public static final String STATE_TYPE = "type";

    /**
     * XML start type content label constant.
     */
    public static final String START_LABEL = "start";

    /**
     * XML END_LABEL type content label constant.
     */
    public static final String END_LABEL = "end";

    /**
     * XML NORMAL_LABEL type content label constant.
     */
    public static final String NORMAL_LABEL = "normal";

    /**
     * XML TRIGGER_LABEL type content label constant.
     */
    public static final String TRIGGER_LABEL = "trigger";

    /**
     * XML TRIGGERSTART_LABEL type content label constant.
     */
    public static final String TRIGGERSTART_LABEL = "triggerstart";

    /**
     * XML LOOP Label type content label constant.
     */
    public static final String LOOP_LABEL = "loop";

    /**
     * XML LOOP Label type content label constant.
     */
    public static final String DATALOOP_LABEL = "dataloop";

    /**
     * XML COMPONENT_LABEL type content label constant.
     */
    public static final String COMPONENT_LABEL = "component";

    /**
     * XML INTERFACE_LABEL type content label constant.
     */
    public static final String INTERFACE_LABEL = "interface";

        /**
     * ID tag in the XML specification.
     */
    public static final String ID_LABEL = "id";

    /**
     * ADDRESS tag in the XML specification.
     */
    public static final String ADDRESS_LABEL = "address";


    /**
     * XML TO_LABEL type content label constant.
     */
    public static final String TO_LABEL = "to";

    /**
     * Label in XML specification of the state machine architecture
     * - component tag <component>
     */

    /**
     * XML patterndata  label constant.
     */
    public static final String DATA_TAG = "patterndata";

    /**
     * XML name label constant.
     */
    public static final String DATA_NAME = "name";

    /**
     * XML value label constant.
     */
    public static final String DATA_VALUE = "value";

    /**
     * This tag is used when referencing values from previous test while executing multiple tests,
     * e.g $$test.{test_name}.patterndata.{id}$$
     */
    public static final String TEST_TAG = "test";

    /**
     * Utility class with a private constructor.
     */
    private XMLStateMachine() {
        // no implementation.
    }

    /**
     * Each state in the state machine is one of three types (start, end,
     * normal) - this matches to three strings in the xml schema. Here
     * we generate the java state type from this string
     *
     * @param textInput The string input from the xml content
     * @return A matching state type to the string. Note an invalid string
     * generates and exception.
     *
     * @see State.stateType
     * @throws InvalidStateTypeException Error in given state type.
     */
     private static State.StateType getStateType(final String textInput)
            throws InvalidStateTypeException {
         // Make sure function operates independent of case
         switch (textInput.toLowerCase(Locale.ENGLISH)) {
             case START_LABEL: return State.StateType.START;
             case END_LABEL: return State.StateType.END;
             case NORMAL_LABEL: return State.StateType.NORMAL;
             case TRIGGER_LABEL: return State.StateType.TRIGGER;
             case TRIGGERSTART_LABEL: return State.StateType.TRIGGERSTART;
             case LOOP_LABEL: return State.StateType.LOOP;
             case DATALOOP_LABEL: return State.StateType.DATALOOP;
             default: // Input hasn't matched so we must through an invalid input exception
                throw new InvalidStateTypeException(textInput
                    + "must be either: {start, end, normal, trigger, triggerstart}");
         }

     }


     /**
      * Retrieve the list of guards attached to a particular transition. That
      * is, this operation can be used to extract the interoperability tests
      * that must conform to true if the transition is to be followed.
      *
      * @param transition The specified transition in the JDOM version of the
      * state machine
      * @param archDesc Reference to the current architecture pattern.
      * @return The list of guards in the xml desciption.
      * @see Guard
      * @throws InvalidTransitionException error in the transition specification.
      */
     private static List<Guard> getGuards(final Element transition, final Architecture archDesc)
        throws InvalidTransitionException {

         final List<Guard> arrayOfGuards = new ArrayList();
         try {
            final List<Element> tOut = transition.getChild(MsgEvent.GUARDS_LABEL).getChildren();
            for (Element eltIndex : tOut) {
                String guardValue = eltIndex.getChildTextTrim(MsgEvent.VALUE_LABEL);
                if (guardValue.startsWith(COMPONENT_LABEL)) {
                    guardValue = getURLEntryFromXML(guardValue, archDesc);
                }
                if (eltIndex.getName().equalsIgnoreCase(MsgEvent.EQUALS_LABEL)) {
                    arrayOfGuards.add(new Guard(
                        eltIndex.getChildTextTrim(MsgEvent.PARAM_LABEL),
                        String.class,
                        Guard.ComparisonType.EQUALS,
                        guardValue, archDesc));
                }
                else if (eltIndex.getName().equalsIgnoreCase(MsgEvent.CONTAINS_LABEL)) {
                    arrayOfGuards.add(new Guard(
                        eltIndex.getChildTextTrim(MsgEvent.PARAM_LABEL),
                        Map.class,
                        Guard.ComparisonType.CONTAINS,
                        guardValue, archDesc));
                }
                else if (eltIndex.getName().equalsIgnoreCase(MsgEvent.NOTEQUALS_LABEL)) {
                    arrayOfGuards.add(new Guard(
                        eltIndex.getChildTextTrim(MsgEvent.PARAM_LABEL),
                        String.class,
                        Guard.ComparisonType.NOTEQUALS,
                        guardValue, archDesc));
                }
                else if (eltIndex.getName().equalsIgnoreCase(MsgEvent.GREATERTHAN_LABEL)) {
                    arrayOfGuards.add(new Guard(
                            eltIndex.getChildTextTrim(MsgEvent.PARAM_LABEL),
                            String.class,
                            Guard.ComparisonType.GREATERTHAN,
                            guardValue, archDesc));
                }
                else if (eltIndex.getName().equalsIgnoreCase(MsgEvent.LESSTHAN_LABEL)){
                    arrayOfGuards.add(new Guard(
                            eltIndex.getChildTextTrim(MsgEvent.PARAM_LABEL),
                            String.class,
                            Guard.ComparisonType.LESSTHAN,
                            guardValue, archDesc));
                }
                else if (eltIndex.getName().equalsIgnoreCase(MsgEvent.REGEX)) {
                    arrayOfGuards.add(new Guard(
                            eltIndex.getChildTextTrim(MsgEvent.PARAM_LABEL),
                            String.class,
                            Guard.ComparisonType.REGEX,
                            guardValue, archDesc));
                }
                else if (eltIndex.getName().equalsIgnoreCase("counter")) {
                    arrayOfGuards.add(new Guard(
                        eltIndex.getChildTextTrim(MsgEvent.PARAM_LABEL),
                        String.class,
                        Guard.ComparisonType.COUNTER,
                        guardValue, archDesc));
                }

            }
         } catch (InvalidRESTMessage ex) {
             ServiceLogger.LOG.error("Invalid Rest Message specification");
             throw new InvalidTransitionException("Error in message spec", ex);
         } catch (InvalidGuard ex) {
             ServiceLogger.LOG.error("Invalid guard specification");
             throw new InvalidTransitionException("Error in guard spec", ex);
         }

         return arrayOfGuards;
     }

     /**
      * Check if a transition is a message or a guard.
      *
      * @param msgRoot The rool element of the xml structure
      * @return True is this is a message transition. False if a guard.
      * @throws InvalidRESTMessage Error during parsing of XML into a rest message
      */
     private static boolean isMessageTransition(final Element msgRoot)
        throws InvalidRESTMessage {

         final Element tOut = msgRoot.getChild(RESTMessage.MESSAGE_LABEL);
         if(tOut == null)
             return false;
         else
             return true;
     }

     /**
      * Create a REST message object from an XML Message content.
      *
      * @param msgRoot The rool element of the xml structure
      * @param archDesc Reference to the parent architecture of this operation context.
      * @return The created REST message object.
      * @throws InvalidRESTMessage Error during parsing of XML into a rest message
      */
     private static ProtocolMessage getMessage(final Element msgRoot, final Architecture archDesc)
        throws InvalidRESTMessage {

        final Element tOut = msgRoot.getChild(ProtocolMessage.MESSAGE_LABEL);
        if (tOut == null) {
            throw new InvalidRESTMessage("XML input does not contain correct structure");
        }

        String urlRef = tOut.getChildTextTrim(ProtocolMessage.URL_LABEL);
        String url = null;
        if (urlRef == null) {
            throw new InvalidRESTMessage("XML input does not contain correct structure");
        } else {
            url = getURLEntryFromXML(urlRef, archDesc);
        }

        final String method = tOut.getChildTextTrim(ProtocolMessage.METHOD_LABEL);
        final String type = tOut.getChildTextTrim(ProtocolMessage.TYPE_LABEL);
        final String body = (String) tOut.getChildText(ProtocolMessage.BODY_LABEL);
        final String path = (String) tOut.getChildText(ProtocolMessage.PATH_LABEL);

        String protocol = getProtocolFromXML(urlRef, archDesc);
        switch(protocol) {
            case "http": return new RESTMessage(url, path, method, type, body, getHeaders(msgRoot), archDesc);
            case "coap": return new COAPMessage(url, path, method, type, body, getHeaders(msgRoot), archDesc);
            case "mqtt": return new MQTTMessage(url, path, method, type, body, getHeaders(msgRoot), archDesc);
        }
        return new RESTMessage(url, path, method, type, body, getHeaders(msgRoot), archDesc);
     }

     /**
      * Operation returns the protocol associated with a URL identifier
      * in the deployment model.
      *
      * @param entry The string that represents the url explicitly or implicitly
      * @param archDesc The architecture where the referenced url is stored.
      * @return The value of the reference pointer in the specification e.g. http.
     * @throws InvalidRESTMessage Error message while parsing a URL entry in XML
      */
     public static String getProtocolFromXML(final String entry, final Architecture archDesc)
                    throws InvalidRESTMessage {

        /**
         * If this is not a referenced "component." then simply return the string
         */
        if (!entry.startsWith(COMPONENT_LABEL)) {
            return "http";
        }

        final String initial = entry;
        final StringTokenizer tokenize = new StringTokenizer(initial, ".");
        // needs to be 3 elements
        if (tokenize.countTokens() != 3) {
            return "http";
        }

        tokenize.nextToken();

        final RESTComponent rComp = archDesc.getArchitectureComponents().get(tokenize.nextToken());
        final String delim = tokenize.nextToken();
        // find interface
        final List<RESTInterface> riList = rComp.getInterfaces();
        for (RESTInterface ri : riList) {
            if (ri.getInterface().equalsIgnoreCase(delim)) {
                return ri.getProtocol();
            }
        }

        return initial;
    }

     /**
      * Operation determines whether the string url is a pointer to a
      * element in the architecture description.
      * If entry starts with "component." then it is a pointer to an arch
      * component. If it is an interface, ...
      *
      * @param entry The string that represents the url explicitly or implicitly
      * @param archDesc The architecture where the referenced url is stored.
      * @return The value of the reference pointer in the specification.
     * @throws InvalidRESTMessage Error message while parsing a URL entry in XML
      */
     public static String getURLEntryFromXML(final String entry, final Architecture archDesc)
                    throws InvalidRESTMessage {

        /**
         * If this is not a referenced "component." then simply return the string
         */
        if (!entry.startsWith(COMPONENT_LABEL)) {
            return entry;
        }

        final String initial = entry;
        final StringTokenizer tokenize = new StringTokenizer(initial, ".");
        // needs to be 3 elements
        if (tokenize.countTokens() != 3) {
            return initial;
        }

        tokenize.nextToken();

        final RESTComponent rComp = archDesc.getArchitectureComponents().get(tokenize.nextToken());
        final String delim = tokenize.nextToken();
        if (delim.equalsIgnoreCase(ADDRESS_LABEL)) {
            return rComp.getipAddress();
        } else {
            // find interface
            final List<RESTInterface> riList = rComp.getInterfaces();
            for (RESTInterface ri : riList) {
                if (ri.getInterface().equalsIgnoreCase(delim)) {
                    return ri.getURL();
                }
            }
        }
        return initial;
    }

    private static String getReport(final Element transition) {
        Element reportTag = transition.getChild(MsgEvent.REPORT_LABEL);
        if (reportTag!=null) {
            return reportTag.getText();
        }
        return null;
    }

    private static String getEvent(final Element transition) {
        Element evTag = transition.getChild("event");
        if (evTag!=null) {
            return evTag.getText();
        }
        return null;
    }

     /**
      * Get the list of headers in the XML specification with headers tag.
      * @param root The part of the xml document to parse
      * @return The set of parameters that correspond to the specified HTTP headers.
      */
     private static Parameter[] getHeaders(final Element root) {

        Element headerListNode = root.getChild(RESTMessage.MESSAGE_LABEL).getChild(RESTMessage.HEADERS_LABEL);
        if (headerListNode != null) {
            final List<Element> tOut = headerListNode.getChildren();
            final Parameter[] results = new Parameter[tOut.size()];
            int index = 0;
            for (Element eltIndex : tOut) {
               final String headerName = eltIndex.getChildText(RESTMessage.HEADERNAME_LABEL);
               final String headerValue = eltIndex.getChildText(RESTMessage.HEADERVALUE_LABEL);
               if ((headerName != null) && (headerValue != null)) {
                   results[index++] = new Parameter(headerName, headerValue);
               }
            }
            return results;
        }

         // There are no headers so we return a zero sized array
         return new Parameter[0];
     }

     /**
      * Add the transitions to a given state from the information given in
      * the xml specification.
      *
      * @param elState The xml specification to find transitions in.
      * @param states The current set of states being produced.
      * @param archDesc The overall architecture reference context.
      * @throws InvalidTransitionException Error parsing the information into
      * a data structure about the transitions.
      */
     private static void addTransitions(final Element elState, final Map<String,
             State> states, final Architecture archDesc) throws InvalidTransitionException {

         final State fromState = states.get(elState.getChildText(LABEL_LABEL));
         if (fromState.isEndNode()) {
             return;
         }

         final List<Element> tOut = elState.getChildren(TRANSITION_LABEL);
         for (Element eltIndex : tOut) {
             final String toLabel = eltIndex.getChildText(TO_LABEL);
             if (states.get(elState.getChildText(LABEL_LABEL)) == null) {
                     throw new InvalidTransitionException("To state does not exist in state machine");
             }
             try {
                 if (isMessageTransition(eltIndex)) {
                     fromState.addTransition(new Transition(toLabel, getMessage(eltIndex, archDesc), getReport(eltIndex)));
                 } else {
                    fromState.addTransition(new Transition(toLabel, getGuards(eltIndex, archDesc), getReport(eltIndex)));
                 }
             } catch (InvalidTransitionException ex) {
                 throw new InvalidTransitionException("Invalid transition specification", ex);
             } catch (InvalidRESTMessage ex) {
                 throw new InvalidTransitionException("Invalid REST message specification", ex);
             }
         }
     }

     /**
      * Based on the XML states list - generate the set of states.
      * This method also calls functions to build the transition map.
      *
      * @param doc The XML document with a fully formed state machine as
      * input. An invalid state machine input will generate an exception.
      * @param arch The overall architecture context of the operation.
      * @return The set of created states from the xml spec.
      * @throws InvalidStateMachineException Error caused by invalid behaviour specification.
      */
     private static Map<String, State> createStates(final Element doc, final Architecture arch)
             throws InvalidStateMachineException {

        final Map<String, State> states = new HashMap();
        try {

            final XPath xpa = XPath.newInstance("//" + STATE_LABEL);

            final List<Element> xmlStates = xpa.selectNodes(doc);
            for (Element eltIndex : xmlStates) {
                // Get the state label
                final String label = eltIndex.getChildText(LABEL_LABEL);
                final State.StateType type = getStateType(eltIndex.getChildText(STATE_TYPE));
                final String report = eltIndex.getChildText(MsgEvent.REPORT_LABEL);
                final String success = eltIndex.getChildText(SUCCESS_LABEL);
                final String event = eltIndex.getChildText("event");

                states.put(label, new StateNode(label, type, arch, event, report, success));
            }
            for (Element eltIndex2 : xmlStates) {
                addTransitions(eltIndex2, states, arch);
            }
        } catch (JDOMException ex) {
            throw new InvalidStateMachineException("Invalid XML input", ex);
        } catch (InvalidStateTypeException ex) {
            throw new InvalidStateMachineException("Invalid state type input", ex);
        } catch (InvalidTransitionException ex) {
            throw new InvalidStateMachineException("Invalid transition specification", ex);
        }
        return states;
     }

     /**
      * Search through the state diagram for the first state. There must be
      * one and only one first state.
      *
      * @param states The set of state machine states to search
      * @return The state label of the first state. Null if there is no first
      * state
      */
     private static String getFirstState(final Map<String, State> states) {
         final Iterator<State> statesMap = states.values().iterator();
         while (statesMap.hasNext()) {
             final State next = statesMap.next();
             if (next.isStartNode()) {
                 return next.getLabel();
             }
         }
         return null;
     }

     /**
      * Create a state machine index.e. the in memory state machine graph from
      * a given XML document instance. This state machine can be executed
      * to test the interoperability of an operating REST composition
      * configuration.
      *
      * @param doc The XML instance of the state machine domain language to
      * be turned into an in-memory state machine
     * @param sMach The State Machine to fill with content.
     * @param inst The parent architecture context.
     * @throws InvalidStateMachineException error during the parsing of the state
     * machine from the XML.
     */
    public static void createStateMachine(final Element doc, final StateMachine sMach,
            final Architecture inst) throws InvalidStateMachineException {

         /**
          * First parse the xml documents to create the state set with
          * transitions between them.
          */
         final Map<String, State> states = createStates(doc, inst);
         final String firstLabel = getFirstState(states);
         if (firstLabel == null) {
             throw new InvalidStateMachineException("State machine: <behaviour> "
                     + "description does not contain a start node");
         }
         /**
          * Build the StateMachine type with the identified first state and
          * state set.
          */
         sMach.inputContent(firstLabel, states);
    }

    /**
     * Find the number of occurences of a character in a given string.
     * @param tocheck The String to evaluate.
     * @param cInst The character (substring) to check for.
     * @return The number of occurences.
     */
    public static int charOccurences(final String tocheck, final String cInst) {
        return tocheck.length() - tocheck.replace(cInst, "").length();
    }
}
