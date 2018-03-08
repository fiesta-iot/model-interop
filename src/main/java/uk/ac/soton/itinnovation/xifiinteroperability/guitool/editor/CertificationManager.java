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
// Created By : Nikolay Stanchev
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor;

/**
 * a Manager class to handle the certification services
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class CertificationManager {

    /**
     * holds the URL of the last loaded test
     */
    private String lastURL;

    /**
     * getter for the URL of the last loaded test
     * @return the lastURL attribute of the manager
     */
    public String getLastURL(){
        return lastURL;
    }

    /**
     * holds the name of the last loaded test
     */
    private String testName;

    /**
     * a getter for the name of the last loaded test
     * @return the name of the last loaded test
     */
    public String getTestName(){
        return testName;
    }

    /**
     * setter for the info of the last loaded test,
     * this method is to be used only when someone opens a model from the Certification menu
     * @param url the new URL of the last loaded test
     * @param name the name of the loaded test
     */
    public void setInfo(String url, String name){
        this.lastURL = url;
        this.testName = name;
    }

    /**
     * this method is to be used when a new model is loaded or created from somewhere
     * different than the Certification menu, the last URL is set to null
     */
    public void resetURL(){
        this.lastURL = null;
        this.executed = false;
    }

    /**
     * boolean to represent if the test has been executed after it was loaded
     */
    private boolean executed;

    /**
     * a getter for the executed attribute of the certification manager
     * @return true if the test was executed after it has been loaded and false otherwise
     */
    public boolean getExecuted(){
        return executed;
    }

    /**
     * a setter method for the executed attribute of the certification manager
     * @param executed True if the test was executed after it has been loaded and false otherwise
     * @param executedXMLmodel the xml of the last executed test
     */
    public void setExecuted(boolean executed, String executedXMLmodel){
        this.executedXMLmodel = executedXMLmodel;
        this.executed = executed;
    }

    /**
     * the xml model that was last executed
     */
    private String executedXMLmodel;

    /**
     * a getter for the last executed model
     * @return the xml of the last executed test
     */
    public String getExecutedModel(){
        return executedXMLmodel;
    }

    /**
     * a constructor for the certification manager
     */
    public CertificationManager(){
        // empty constructor, nothing to initialise
    }

}
