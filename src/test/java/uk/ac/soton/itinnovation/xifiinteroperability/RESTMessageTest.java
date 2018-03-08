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

import java.io.IOException;
import junit.framework.Assert;
import org.junit.Test;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.MsgEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.RESTEvent;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.RESTMessage;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidRESTMessage;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.UnexpectedEventException;

/**
 * Set of tests for ensuring that the code for managing state behaviour in
 * the state machine remains correct.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class RESTMessageTest {

    /**
     * A standard HTML page URL - IT Innovation homepage (can be changed).
     */
    private static final String TESTURL = "http://www.it-innovation.soton.ac.uk";

    /**
     * REST API URL - Use Flickr API (can be changed).
     */
    private static final String FLICKRURL = "https://api.flickr.com/services/rest";

    /**
     * String of 200 - the HTTP Success code.
     */
    private static final String HTTPOK = "200";

    /**
     * Test that the framework constructs a usable rest message i.e. the
     * get, post, put, delete operations all do as should.
     */
    @Test
    public final void testMessageStructure() {
        try {

            final String urlFull = TESTURL;

            RESTMessage msg = new RESTMessage(TESTURL, "/", RESTMessage.GET_LABEL, null, null, null, null);
            Assert.assertEquals(RESTMessage.GET_LABEL, msg.getMethod());
            Assert.assertEquals(urlFull, msg.getURL());

            msg = new RESTMessage(TESTURL, "/", RESTMessage.POST_LABEL, null, null, null, null);
            Assert.assertEquals(RESTMessage.POST_LABEL, msg.getMethod());

            msg = new RESTMessage(TESTURL, "/", RESTMessage.PUT_LABEL, null, null, null, null);
            Assert.assertEquals(RESTMessage.PUT_LABEL, msg.getMethod());

            msg = new RESTMessage(TESTURL, "/", RESTMessage.DELETE_LABEL, null, null, null, null);
            Assert.assertEquals(RESTMessage.DELETE_LABEL, msg.getMethod());

        } catch (InvalidRESTMessage ex) {
             ServiceLogger.LOG.info("Code correctly captures BadURL exception " + ex);
        }
    }

    /**
     * Test operation that doesn't exist. REST message only has 4 valid operations.
     */
    @Test
    public final void testBadMethod() {
        try {
            new RESTMessage(TESTURL, "/", "Gett", null, null, null, null);
            Assert.fail("Invalid method - should have failed");
        } catch (InvalidRESTMessage ex) {
             ServiceLogger.LOG.info("Code correctly captures the invlaid operation exception " + ex);
        }
    }

    /**
     * Test the RestMessage class with a null operation value.
     */
    @Test
    public final void testNullMethod() {
        try {
            new RESTMessage(TESTURL, "/", null, null, null, null, null);
            Assert.fail("Invalid method - should have failed");
        } catch (InvalidRESTMessage ex) {
             ServiceLogger.LOG.error("Code correctly captures null operation exception " + ex);
        }
    }

    /**
     * Test for invalid data types in the rest message.
     */
    @Test
    public final void testDataType() {
        try {
            new RESTMessage(TESTURL, "/", RESTMessage.POST_LABEL, "text", "body", null, null);
            Assert.fail("Invalid type - should have failed");
        } catch (InvalidRESTMessage ex) {
             ServiceLogger.LOG.info("Code correctly captures invalid data type exception " + ex);
        }
    }

    /**
     * Test a valid REST Get operation succeeds correctly.
     */
    @Test
    public final void testGetInvocation() {
        try {
            final RESTMessage msg = new RESTMessage(TESTURL, "/", RESTMessage.GET_LABEL, null, null, null, null);
            final MsgEvent responseEvent = msg.invokeMessage();
            Assert.assertEquals(MsgEvent.REPLY_LABEL, responseEvent.getParameterMap().get(RESTEvent.HTTP_MSG).getValue());
            Assert.assertEquals("text/html", responseEvent.getDataBody().getType());
            Assert.assertEquals("www.it-innovation.soton.ac.uk", responseEvent.getParameterMap().get(RESTEvent.HTTP_FROM).getValue());
            Assert.assertEquals(HTTPOK, responseEvent.getParameterMap().get(RESTEvent.HTTP_CODE).getValue());
            Assert.assertEquals(responseEvent.getParameterMap().get(RESTEvent.HTTP_TO).getValue(), SystemProperties.getIP());

        } catch (InvalidRESTMessage ex) {
            Assert.fail("Valid request, if GET fails check RESTMessage.invokeMessage code" + ex.getLocalizedMessage());
        } catch (UnexpectedEventException ex) {
            Assert.fail("Valid request, error during invocation response event is invalid" + ex.getLocalizedMessage());
        } catch (ConfigurationException ex) {
            Assert.fail("Valid request, could not configure GET operation" + ex.getLocalizedMessage());
        } catch (IOException ex) {
            Assert.fail("Valid request, error during invocation could not carry out GET" + ex.getLocalizedMessage());
        }

    }

    /**
     * Test a valid POST operation.
     */
    @Test
    public final void testPOSTInvocation() {
        try {
            final RESTMessage msg = new RESTMessage(FLICKRURL,
                    "/?method=flickr.test.echo&name=value&api_key=c05e3c0911a1e399cf42b086062dd5d2",
                    RESTMessage.POST_LABEL, "xml", "<name>value</name>", null, null);

            final RESTEvent responseEvent = msg.invokeMessage();
            Assert.assertEquals(MsgEvent.REPLY_LABEL, responseEvent.getParameterMap().get(RESTEvent.HTTP_MSG).getValue());
            Assert.assertEquals("text/xml", responseEvent.getDataBody().getType());
            Assert.assertEquals("api.flickr.com", responseEvent.getParameterMap().get(RESTEvent.HTTP_FROM).getValue());
            Assert.assertEquals(HTTPOK, responseEvent.getParameterMap().get(RESTEvent.HTTP_CODE).getValue());
            Assert.assertEquals(responseEvent.getParameterMap().get(RESTEvent.HTTP_TO).getValue(), SystemProperties.getIP());

        } catch (InvalidRESTMessage ex) {
            Assert.fail("Valid request, if POST test fails check RESTMessage.invokeMessage code" + ex.getLocalizedMessage());
        } catch (UnexpectedEventException ex) {
            Assert.fail("Error during operation, invalid response received" + ex.getLocalizedMessage());
        } catch (ConfigurationException ex) {
            Assert.fail("Error - could not configure POST operation" + ex.getLocalizedMessage());
        } catch (IOException ex) {
            Assert.fail("Error carrying out the remote invocation " + ex.getLocalizedMessage());
        }

    }

    /**
     * Test a more complex POST operation.
     */
    @Test
    public final void testPANDAPOSTInvocation() {
        try {
            RESTMessage msg = new RESTMessage(FLICKRURL,
                    "?method=flickr.panda.getList&api_key=c05e3c0911a1e399cf42b086062dd5d2",
                    RESTMessage.POST_LABEL, "xml", "<name>value</name>", null, null);

            RESTEvent responseEvent = msg.invokeMessage();
            Assert.assertEquals(MsgEvent.REPLY_LABEL, responseEvent.getParameterMap().get(RESTEvent.HTTP_MSG).getValue());
            Assert.assertEquals("text/xml", responseEvent.getDataBody().getType());
            Assert.assertEquals("api.flickr.com", responseEvent.getParameterMap().get(RESTEvent.HTTP_FROM).getValue());
            Assert.assertEquals(HTTPOK, responseEvent.getParameterMap().get(RESTEvent.HTTP_CODE).getValue());
            Assert.assertEquals(responseEvent.getParameterMap().get(RESTEvent.HTTP_TO).getValue(), SystemProperties.getIP());

            msg = new RESTMessage(FLICKRURL,
                    "/?method=flickr.panda.getPhotos&api_key=c05e3c0911a1e399cf42b086062dd5d2&panda_name=ling ling",
                    RESTMessage.POST_LABEL, "xml", "<name>valune</name>", null, null);
            responseEvent = msg.invokeMessage();
            Assert.assertEquals(MsgEvent.REPLY_LABEL, responseEvent.getParameterMap().get(RESTEvent.HTTP_MSG).getValue());
            Assert.assertEquals("text/xml", responseEvent.getDataBody().getType());
            Assert.assertEquals("api.flickr.com", responseEvent.getParameterMap().get(RESTEvent.HTTP_FROM).getValue());
            Assert.assertEquals(HTTPOK, responseEvent.getParameterMap().get(RESTEvent.HTTP_CODE).getValue());
            Assert.assertEquals(responseEvent.getParameterMap().get(RESTEvent.HTTP_TO).getValue(), SystemProperties.getIP());

        } catch (InvalidRESTMessage ex) {
            Assert.fail("Valid request, if complex POST test fails check RESTMessage.invokeMessage code");
        } catch (UnexpectedEventException ex) {
            Assert.fail("Error during invocation, unexpected response event occured " + ex.getLocalizedMessage());
        } catch (ConfigurationException ex) {
             Assert.fail("Could not configure POST operation " + ex.getLocalizedMessage());
        } catch (IOException ex) {
             Assert.fail("Error during the remote POST invocation " + ex.getLocalizedMessage());
        }
    }
}
