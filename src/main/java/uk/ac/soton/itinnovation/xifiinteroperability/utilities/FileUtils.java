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

package uk.ac.soton.itinnovation.xifiinteroperability.utilities;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import uk.ac.soton.itinnovation.xifiinteroperability.ConfigurationException;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
import uk.ac.soton.itinnovation.xifiinteroperability.SystemProperties;

/**
 * Utility class of file reading operations.
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public final class FileUtils {

    /**
     * Java system property for the current executing directory.
     */
    public static final String THISDIR = System.getProperty("user.dir");

    /**
     * Java system property for a file SEPARATOR character.
     */
    public static final String SEPARATOR = System.getProperty("file.separator");

    /**
     * Utility class, simple constructor.
     */
    private FileUtils() {
        // No implementation needed
    }

    /**
     * Location of general resource file (not resource JAR files).
     * @param file The file to get the resource path of.
     * @return The path of the specified resource file.
     */
    public static Path resourceFiles(final String file) {
        return Paths.get(File.separator + "root" + File.separator
                + "InteroperabilityTool" + File.separator
                + "src" + File.separator + "main" + File.separator
                + "resources" + File.separator + file);
    }

    /**
     * Retrieve the URL of a file, given its filename.
     * @param fileName The file name descriptor.
     * @return The file reference in URL form.
     */
    public static URL getURL(final String fileName) {

        try {
            if (SystemProperties.readProperty("virtualised").equalsIgnoreCase("true")) {
                return resourceFiles(fileName).toUri().toURL();
            }
            return Thread.currentThread().getContextClassLoader().getResource(fileName);
        } catch (ConfigurationException ex) {
            ServiceLogger.LOG.error("Cannot read the daemon property; check Interoperability.properties file", ex);
        } catch (MalformedURLException ex) {
             ServiceLogger.LOG.error("Invalid file URL", ex);
        }
        return null;
    }
    /**
     * Read a file when operating in Daemon mode.
     * @param absolutePath The fixed absolute path of the file.
     * @param encoding The encoding of the file.
     * @return The contents of the file as a string.
     * @throws IOException IO exception when file cannot be read.
     */
    public static String readFileFromDaemon(final String absolutePath, final Charset encoding)
        throws IOException {

            final byte[] encoded = Files.readAllBytes(new File(resourceFiles(absolutePath).toString()).toPath());
            return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    /**
     * Read the contents of a file.
     * @param path The file path
     * @param encoding The file encoding.
     * @return The contents of the file.
     * @throws IOException IO exception reading the file.
     */
    public static String readFile(final String path, final Charset encoding)
            throws IOException {
        try {
            if (SystemProperties.readProperty("virtualised").equalsIgnoreCase("true")) {
                return readFileFromDaemon(path, encoding);
            }

            final URL resourceUrl = FileUtils.getURL(path);
            final Path resourcePath = Paths.get(resourceUrl.toURI());

            final byte[] encoded = Files.readAllBytes(resourcePath);
            return encoding.decode(ByteBuffer.wrap(encoded)).toString();
        } catch (URISyntaxException ex) {
            ServiceLogger.LOG.error("Cannot find resource file: " + path, ex);
        } catch (ConfigurationException ex) {
            ServiceLogger.LOG.error("Cannot read FIOPS daemon property, check Interoperability.properties file", ex);
        }
        return null;
    }
}
