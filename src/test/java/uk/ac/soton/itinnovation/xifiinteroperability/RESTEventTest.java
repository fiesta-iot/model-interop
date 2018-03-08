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
//  Created By :			Paul Grace
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
public class RESTEventTest {

    /**
     * Constant URL for doing HTTP get operation test.
     */
    private static final String TESTURL = "http://www.it-innovation.soton.ac.uk";

    /**
     * Test POST input.
     */
    private static final String POSTINPUT = "post";

    /**
     * Test POST input.
     */
    private static final String GETINPUT = "get";

    /**
     * Test HTTP OK output.
     */
    private static final String HTTPOK = "200";

    /**
     * Flickr URL constant.
     */
    private static final String FLICKRURL = "https://api.flickr.com";

    /**
     * FLICKR REST API Endpoint.
     */
    private static final String FLICKRURLREST = "https://api.flickr.com/services/rest";

    /**
     * Test how valid RESTMessages are constructed. Correct input parameters
     * work; invalid parameters produce correct exception.
     */
    @Test
    public final void testEventStructure() {
        try {

            final String urlFull = TESTURL;

            RESTMessage rMsg = new RESTMessage(TESTURL, "/", GETINPUT, null, null, null, null);
            Assert.assertEquals(RESTMessage.GET_LABEL, rMsg.getMethod());
            Assert.assertEquals(rMsg.getURL(), urlFull);

            rMsg = new RESTMessage(TESTURL, "/", POSTINPUT, null, null, null, null);
            Assert.assertEquals(RESTMessage.POST_LABEL, rMsg.getMethod());

            rMsg = new RESTMessage(TESTURL, "/", "put", null, null, null, null);
            Assert.assertEquals(RESTMessage.PUT_LABEL, rMsg.getMethod());

            rMsg = new RESTMessage(TESTURL, "/", "delete", null, null, null, null);
            Assert.assertEquals(RESTMessage.DELETE_LABEL, rMsg.getMethod());

        } catch (InvalidRESTMessage ex) {
             ServiceLogger.LOG.info("Code captures exception " + ex);
        }
    }

    /**
     * Test a badly constructed rest message is not allowed.
     */
    @Test
    public final void testBadMethod() {
        try {
            new RESTMessage(TESTURL, "/", "Gett", null, null, null, null);
            Assert.fail("Invalid method - should have failed");
        } catch (InvalidRESTMessage ex) {
             ServiceLogger.LOG.info("Code correctly produces right exception " + ex);
        }
    }

    /**
     * Test null message exceptions correctly.
     */
    @Test
    public final void testNullMethod() {
        try {
            new RESTMessage(TESTURL, "/", null, null, null, null, null);
            Assert.fail("Invalid method - should have failed");
        } catch (InvalidRESTMessage ex) {
             ServiceLogger.LOG.info("Code correctly captures exception " + ex);
        }
    }

    /**
     * Test the rest message data types.
     */
    @Test
    public final void testDataType() {
        try {
            new RESTMessage(TESTURL, "/", POSTINPUT, "text", "body", null, null);
            Assert.fail("Invalid type - should have failed");
        } catch (InvalidRESTMessage ex) {
             ServiceLogger.LOG.info("Code correctly exceptions " + ex);
        }
    }

    /**
     * Test a full GET invocation.
     */
    @Test
    public final void testGetInvocation() {
        try {
            final RESTMessage rMsg = new RESTMessage(TESTURL, "/", GETINPUT, null, null, null, null);
            final MsgEvent responseEvent = rMsg.invokeMessage();
            Assert.assertEquals(MsgEvent.REPLY_LABEL, responseEvent.getParameterMap().get(RESTEvent.HTTP_MSG).getValue());
            Assert.assertEquals("text/html", responseEvent.getDataBody().getType());
            Assert.assertEquals("www.it-innovation.soton.ac.uk", responseEvent.getParameterMap().get(RESTEvent.HTTP_FROM).getValue());
            Assert.assertEquals(HTTPOK, responseEvent.getParameterMap().get(RESTEvent.HTTP_CODE).getValue());
            Assert.assertEquals(SystemProperties.getIP(), responseEvent.getParameterMap().get(RESTEvent.HTTP_TO).getValue());

        } catch (InvalidRESTMessage ex) {
            Assert.fail("Valid request, check RESTMessage.invokeMessage code" + ex.getLocalizedMessage());
        } catch (UnexpectedEventException | ConfigurationException | IOException ex) {
            Assert.fail("Valid request, test fail check RESTMessage.invokeMessage code" + ex.getLocalizedMessage());
        }

    }

    /**
     * Test a post operation.
     */
    @Test
    public final void testPOSTInvocation() {
        try {
            final RESTMessage rMsg = new RESTMessage(FLICKRURL, "/services/rest/?method=flickr.test.echo&name=value&api_key=c05e3c0911a1e399cf42b086062dd5d2",
                    POSTINPUT, "xml", "<name>valune</name>", null, null);

            final RESTEvent responseEvent = rMsg.invokeMessage();
            Assert.assertEquals(MsgEvent.REPLY_LABEL, responseEvent.getParameterMap().get(RESTEvent.HTTP_MSG).getValue());
            Assert.assertEquals("text/xml", responseEvent.getDataBody().getType());
            Assert.assertEquals("api.flickr.com", responseEvent.getParameterMap().get(RESTEvent.HTTP_FROM).getValue());
            Assert.assertEquals(HTTPOK, responseEvent.getParameterMap().get(RESTEvent.HTTP_CODE).getValue());
            Assert.assertEquals(responseEvent.getParameterMap().get(RESTEvent.HTTP_TO).getValue(), SystemProperties.getIP());

        } catch (InvalidRESTMessage ex) {
            Assert.fail("Valid request, check RESTMessage.invokeMessage" + ex.getLocalizedMessage());
        } catch (UnexpectedEventException | ConfigurationException | IOException ex) {
            Assert.fail("Valid request, if test fails check RESTMessage.invokeMessage" + ex.getLocalizedMessage());
        }
    }

    /**
     * Test a real Flickr operation.
     */
    @Test
    public final void testPANDAPOSTInvocation() {
        try {
            RESTMessage rMsg = new RESTMessage(FLICKRURLREST, "/?method=flickr.panda.getList&api_key=c05e3c0911a1e399cf42b086062dd5d2",
                    POSTINPUT, "xml", "<name>valune</name>", null, null);

            RESTEvent responseEvent = rMsg.invokeMessage();
            Assert.assertEquals(MsgEvent.REPLY_LABEL, responseEvent.getParameterMap().get(RESTEvent.HTTP_MSG).getValue());
            Assert.assertEquals("text/xml", responseEvent.getDataBody().getType());
            Assert.assertEquals("api.flickr.com", responseEvent.getParameterMap().get(RESTEvent.HTTP_FROM).getValue());
            Assert.assertEquals(HTTPOK, responseEvent.getParameterMap().get(RESTEvent.HTTP_CODE).getValue());
            Assert.assertEquals(responseEvent.getParameterMap().get(RESTEvent.HTTP_TO).getValue(), SystemProperties.getIP());

            rMsg = new RESTMessage(FLICKRURLREST, "/?method=flickr.panda.getPhotos&api_key=c05e3c0911a1e399cf42b086062dd5d2&panda_name=ling ling",
                    POSTINPUT, "xml", "<name>valune</name>", null, null);
            responseEvent = rMsg.invokeMessage();
            Assert.assertEquals(MsgEvent.REPLY_LABEL, responseEvent.getParameterMap().get(RESTEvent.HTTP_MSG).getValue());
            Assert.assertEquals("text/xml", responseEvent.getDataBody().getType());
            Assert.assertEquals("api.flickr.com", responseEvent.getParameterMap().get(RESTEvent.HTTP_FROM).getValue());
            Assert.assertEquals(HTTPOK, responseEvent.getParameterMap().get(RESTEvent.HTTP_CODE).getValue());
            Assert.assertEquals(responseEvent.getParameterMap().get(RESTEvent.HTTP_TO).getValue(), SystemProperties.getIP());

        } catch (InvalidRESTMessage ex) {
            Assert.fail("Valid request, if test fails check RESTMessage.invokeMessage code" + ex.getLocalizedMessage());
        } catch (UnexpectedEventException | ConfigurationException | IOException ex) {
            Assert.fail("Valid request, if test fails check RESTMessage.invokeMessage code" + ex.getLocalizedMessage());
        }

    }
}
