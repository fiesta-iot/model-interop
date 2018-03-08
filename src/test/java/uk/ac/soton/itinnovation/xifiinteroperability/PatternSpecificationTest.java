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
//	Created By :			Paul Grace
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InvalidPatternException;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.PatternValidation;
import uk.ac.soton.itinnovation.xifiinteroperability.utilities.FileUtils;

/**
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class PatternSpecificationTest {

    /**
     * Constant to the pattern schema location.
     */
    private final URL schemaUrl = FileUtils.getURL(SystemProperties.PATTERNSCHEMA);

    /**
     * Read a file's contents.
     * @param path The file to read.
     * @param encoding The file encoding.
     * @return The contents of the file.
     * @throws IOException Error during the reading.
     */
    public final String readFile(final String path, final Charset encoding)
        throws IOException {
        try {
            final URL resourceUrl = FileUtils.getURL(path);
            final Path resourcePath = Paths.get(resourceUrl.toURI());
            final byte[] encoded = Files.readAllBytes(resourcePath);
            return encoding.decode(ByteBuffer.wrap(encoded)).toString();
        } catch (URISyntaxException ex) {
            ServiceLogger.LOG.error("Cannot find resource file: " + path, ex);
            throw new IOException(ex);
        }
    }

    /**
     * Test a pattern against the specification to check that code still
     * verifies.
     */
    @Test
    public final void testValidApplicationPattern() {
        try {
            final String pattern = readFile("examples/COAP.xml", Charset.defaultCharset());

            final boolean valid = PatternValidation.validatePattern(pattern, schemaUrl);

            Assert.assertTrue("Correct pattern did not validate, check implementation of PatternValidation.validatePattern()", valid);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Unit test failed - coundn't read pattern");
        } catch (SAXException ex) {
            ServiceLogger.LOG.error("Invalid Pattern specification");
        } catch (InvalidPatternException ex) {
            ServiceLogger.LOG.error("There are more than one start nodes in the graph.");
        }
     }

    /**
     * Test a valid compliance pattern using the validator.
     */
    @Test
    public final void testValidAPIPattern() {
        try {
            final String pattern = readFile("examples/COAP.xml", Charset.defaultCharset());

            final boolean valid = PatternValidation.validatePattern(pattern, schemaUrl);

            Assert.assertTrue("Correct pattern did not validate, check implementation of PatternValidation.validatePattern()", valid);
        } catch (IOException ex) {
            ServiceLogger.LOG.error("Unit test failed - coundn't read pattern");
        } catch (SAXException ex) {
            ServiceLogger.LOG.error("Invalid Pattern specification");
        } catch (InvalidPatternException ex) {
            ServiceLogger.LOG.error("There are more than one start nodes in the graph.");
        }
    }

}
