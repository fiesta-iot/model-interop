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

package uk.ac.soton.itinnovation.xifiinteroperability.modelframework;

import java.io.PrintStream;

/**
 * The Interoperability report is a generated report of the trace through
 * the interoperability state machine. It is a final result that is used
 * for a simple request response (e.g. POST) operation to monitor a system.
 * Alternatively the dynamic output stream can be used to monitor in progress
 * execution
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class InteroperabilityReport {

    private String Success;

    public void setSuccess(String val){
        this.Success = val;
    }

    public String getSuccess() {
        return this.Success;
    }

    /**
     * This is the editable content of the report (i.e. the body). We initialise
     * with a title.
     */
    private transient String textTrace = "Beginning test: \n";

    /**
     * a getter for the text trace of the report
     * @return the text trace of the report
     */
    public String getTextTrace(){
        return this.textTrace;
    }

    /**
     * Interoperability Summary in Json format.
     * {"owner":"fiesta",
     *  "extention":"jsonld",
     *  "validated":true,
     *  "global_duration":"271 ms",
     *  "semantic_duration":"5 ms",
     *  "start":"2017/03/10 19:06:36",
     * "results":[{"type":"Namespace and URI validation","value":""},
     * {"type":"Literal","value":""},{"type":"Predicate and Class validation","value":""},
     * {"type":"Semantic Error","value":""},{"type":"Complete","value":""}],"syntactic_duration":"266 ms"}
     *
     */
    private transient String intReport = "[";

    /**
     * The stream output of the text on the local host.
     */
    private transient PrintStream output;

    /**
     * Boolean indicator if the report is carried out during the execution i.e.
     * step by step (true); or as a single batch report at the end (false).
     */
    private final transient boolean realtime;

    /**
     * Method to add a new line to the report. Simple formatting method.
     */
    private void newline() {
        this.textTrace += "\n";
    }

    /**
     * The interoperability report outputs in realtime to the output stream.
     * @param printOut The output stream to report to in realtime.
     */
    public InteroperabilityReport(final PrintStream printOut) {
        this.output = printOut;
        this.realtime = true;
        println(textTrace);
    }

    /**
     * Empty constructor. Simple interoperability report written in batch
     * mode. Output to be displayed as text.
     */
    public InteroperabilityReport() {
        this.realtime = false;
    }

    /**
     * Add a string to a new line.
     * @param newval String to report.
     */
    public final void println(final String newval) {
        newline();
        this.textTrace += newval;
        newline();

        if (realtime) {
            output.println(newval);
        }
    }

    /**
     * A report is a statement of an evaluation of an event, whether
     * and individual test or a full model test. This is added to the JSON
     * interoperability report - as a JSON array element.
     * @param report The JSON String to add to the report.
     */
    public final void addReport(final String report) {
        // If this is the first input, we construct the first element of the json array
        if(this.intReport.equalsIgnoreCase("[")) {
            this.intReport +=report ;
        } else {
            // Otherwise we add the json to the array
            this.intReport += "," + report ;
        }
    }

    /**
     * A report is a statement of an evaluation of an event, whether
     * and individual test or a full model test. This is added to the JSON
     * interoperability report - as a JSON array element.
     */
    public final String getReport() {
        if(this.intReport.lastIndexOf(']')!= (this.intReport.length()-1)){
            this.intReport += "]";
        }
        return this.intReport;
    }

    /**
    * Add a tabbed string new line.
    * @param newval The text to add as a tabbed line.
    */
    public final void printtabline(final String newval) {
        this.textTrace += "\t" + newval;
        newline();

        if (realtime) {
            output.println("\t" + newval);
        }
    }

    /**
     * Clear the text trace of the report
     */
    public final void clear(){
        this.textTrace = "";
    }

    /**
     * Produce a text version of the output report. Typically displayed to
     * a text field or the console.
     * @return The interoperability report as a single string.
     */
    public final String outputTrace() {
        return this.textTrace;
    }

}
