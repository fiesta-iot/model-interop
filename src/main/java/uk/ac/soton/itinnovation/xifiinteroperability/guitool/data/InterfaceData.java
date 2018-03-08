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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data;

import java.io.Serializable;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.InvalidXMLInputException;


/**
 * A REST Interface is an identifier and the url description.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class InterfaceData implements Serializable {

    /**
     * Unique label to identify the REST interface within the GUI and the pattern
     * e.g.
     */
    private String restID;

    /**
     * Getter for the ID of the REST interface.
     * @return The id of the REST Interface.
     */
    public final String getRestID() {
        return restID;
    }

    /**
     * Set the name field to identify the Rest interface.
     * @param restidentify The new id of the rest interface.
     */
    public final void setRestID(final String restidentify) {
        restID = restidentify;
    }

    private String protocol;

    /**
     * Getter for the Protocol of the REST interface.
     * @return The id of the REST Interface.
     */
    public final String getProtocol() {
        return protocol;
    }

    /**
     * Set the interface protocol.
     * @param newProtocol The new protocol.
     */
    public final void setProtocol(final String newProtocol) {
        protocol = newProtocol;
    }

    /**
     * The full URL of the rest interface in String form.
     */
    private String url;


    /**
     * Getter for the URL of the REST interface.
     * @return The URL of the REST Interface.
     */
    public final String getRestURL() {
        return url;
    }

    /**
     * Set the url field for the Rest interface.
     * @param resturl The new id of the rest interface.
     */
    public final void setRestURL(final String resturl) {
        url = resturl;
    }


    /**
     * Create a new interface data element.
     * @param ident The named identifier of the REST interface.
     * @param addr The full url string of the interface.
     */
    public InterfaceData(final String ident, final String addr, String protocol) {
        this.restID = ident;
        this.url = addr;
        if (protocol == null)
            this.protocol = "http";
        else
            this.protocol = protocol;
    }

    /**
     * Overriding the toString method
     * @return the restID of the InterfaceData
     */
    @Override
    public String toString(){
        return this.restID;
    }

    /**
    * Generate the XML content for this data.
    *
    * @return The XML pattern string for this data object.
    */
    public final String generateTransitionXML() {
        final StringBuilder strBuild = new StringBuilder();
        strBuild.append("\n<ID>").append(this.restID).append("</ID>").append("<URL>").append(this.url).append("</URL>")
                .append("<protocol>").append(this.protocol).append("</protocol>");
        return strBuild.toString();
    }

    /**
     * Transform an xml data into a new Interface Data object.
     * @param xml The xml string with the interface data
     * @return a new InterfaceData object
     * @throws InvalidXMLInputException error in the XML input.
     */
    public static InterfaceData readXML(final String xml) throws
            InvalidXMLInputException {

        try {
            return new InterfaceData(
                    XMLReader.readXPATHValue(xml, "//ID"),
                    XMLReader.readXPATHValue(xml, "//URL"),
                    XMLReader.readXPATHValue(xml, "//protocol"));
        } catch (Exception ex) {
            throw new InvalidXMLInputException("InterfaceData XML must be <ID>id</ID><URL>url</URL><protocol>protocol</protocol>", ex);
        }

    }

}
