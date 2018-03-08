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
package uk.ac.soton.itinnovation.xifiinteroperability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * The properties file is stored in the JAR resources location. N.b. Any changes
 * must then be followed by a re-build.
 *
 * This class provides the methods to read the properties.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author pjg
 */
public final class SystemProperties {

    /**
     * Private constructor to ensure that other instances are
     * not generated and we have a singleton type behaviour.
     */
    private SystemProperties() {
    }

    /**
     * Name of the configuration file. This will not change and need not be changed.
     */
    private static final String FILENAME = "Interoperability.properties";

    /**
     * To find our IP Address when hosted on a remote VM we can use an IP
     * checking service. Here we use the Amazon WS tool.
     */
    private static final String IPCHECKER = "http://checkip.amazonaws.com";

    /**
     * Constant property labels: change this code if changing the property
     * file structure/names.
     */

    /**
     * The pattern DTD schema describing a valid pattern.
     */
    public static final String PATTERNSCHEMA = "Pattern.xsd";

    /**
     * Find an available port. Tests if the port given is in use; if it is
     * available true is returned otherwise false.
     * @param port The port integer number to test
     * @return Whether the port number is available or not.
     */
     private static boolean available(final int port) {

        ServerSocket serverSock = null;
        DatagramSocket dataSock = null;
        try {
            serverSock = new ServerSocket(port);
            serverSock.setReuseAddress(true);
            dataSock = new DatagramSocket(port);
            dataSock.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (dataSock != null) {
                    dataSock.close();
                }

                if (serverSock != null) {

                        serverSock.close();
                }
            } catch (IOException e) {
                ServiceLogger.LOG.error("Unable to close sockets after port check");
            }
        }
    }

    /**
     * Read the default port value for the service from the properties file.
     * @return The integer value of the default port.
     */
    public static int getDefaultPort() {
        try {
             final String port = SystemProperties.readProperty("default_port");
             return Integer.parseInt(port);
        } catch (ConfigurationException ex) {
            ServiceLogger.LOG.error("System host problem" + ex.getMessage());
            return -1;
        }
    }

    /**
     * Read the default path value for the service from the properties file.
     * @return The string name of the path e.g. interoperability.
     */
    public static String getDefaultPath() {
        try {
             return SystemProperties.readProperty("default_service");
        } catch (ConfigurationException ex) {
            ServiceLogger.LOG.error("System host problem" + ex.getMessage());
            return null;
        }
    }

    /**
     * Check if the application is hosted with oauth protection.
     * @return true if the application is protected.
     */
    public static boolean isOAUTHProtected() {
        try {
             return Boolean.valueOf(SystemProperties.readProperty("oauth_protected"));
        } catch (ConfigurationException ex) {
            ServiceLogger.LOG.error("System host problem" + ex.getMessage());
            return true;
        }
    }

    /**
     * Gain access to the port number. If the port is already in use the method
     * will return false; otherwise the port can safely be used.
     *
     * @param port The port to find the nearest available to.
     * @return The next free port as an integer.
     * @throws ConfigurationException Exception thrown if properties aren't configured correctly.
     */
    public static int getAvailablePort(final int port) throws ConfigurationException {

        int availablePort = port;
        /**
         * to avoid automatic protocol selection by clients for 443 do not
         * create a proxy on port 443
         */
        if (port == 443) {
            availablePort = port + 1;
        }

        if (available(availablePort)) {
            return availablePort;
        } else {
            for (int i = availablePort + 1; i < Integer.parseInt(readProperty("max_port")); i++) {
                if (available(i)) {
                    return i;
                }
            }
            throw new ConfigurationException("Unable to find an available port");
        }
    }

    /**
     * Simple function to Return the value of the named configuration property.
     * @param propertyName The property to read
     * @return The value of the property
     * @throws ConfigurationException Exception when reading property.
     * @see uk.ac.soton.itinnovation.xifiinteroperability.ConfigurationException
     */
    public static String readProperty(final String propertyName)
                                            throws ConfigurationException {
        return readProperties().getProperty(propertyName);
    }

    /**
     * Returns the loaded list of system configuration properties.
     * @return The properties list.
     * @throws ConfigurationException Exception thrown when error reading the property file.
     * @see java.util.Properties
     */
    private static Properties readProperties() throws ConfigurationException {
        final Properties prop = new Properties();
    	InputStream input = null;

    	try {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILENAME);
            if (input == null) {
                final String error = "Unable to locate configuration file: " + FILENAME;
                ServiceLogger.LOG.error(error);
                throw new ConfigurationException(error);
            }
            //load a properties file from class path, inside static method
            prop.load(input);
            return prop;
    	} catch (IOException ex) {
            ServiceLogger.LOG.error("Error reading file: " + ex.getLocalizedMessage());
            throw new ConfigurationException(ex.getLocalizedMessage(), ex);
        } finally {
            if (input != null) {
                try {
                     input.close();
                } catch (IOException ex) {
                    ServiceLogger.LOG.error("Configuration error - check Interoperability.properties settings" + ex.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Function for obtaining the IP address from a virtual machine whose
     * remote IP address may be different from the one extracted from the
     * network card.
     *
     * Uses an IP checker service.
     *
     * @return The IPv4 String address of the machine running the Interopability Tool
     * @throws ConfigurationException Exception error when reading system properties.
     * @throws IOException when the interaction with the IP checker service fails.
     */
    public static String getIP() throws ConfigurationException, IOException {

        if (readProperty("virtualised").equalsIgnoreCase("true")) {
            final URL whatismyip = new URL(IPCHECKER);
            BufferedReader inBuffer = null;
            try {
                inBuffer = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
                return inBuffer.readLine();
            } finally {
                if (inBuffer != null) {
                    try {
                        inBuffer.close();
                    } catch (IOException e) {
                        ServiceLogger.LOG.error("Unable to obtain IP Address of machine", e);
                    }
                }
            }
        } else {
            try {
                // Where not virtualised we do a simple get local host address
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                ServiceLogger.LOG.error("Unable to generate a local host address "
                        + "- read installation guide for details to resolve error");
                throw new ConfigurationException("Could not configure address", ex);
            }
        }
    }
}

