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
// Created By : Paul Grace
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


/**
 * General class to dynamically write to a given text area.
 *
 * In the main GUI pane Test can be run to evaluate interoperability. The
 * results of these tests are dynamically displayed in this TextArea. It works using
 * received input from the underlying interoperability framework.
 * 
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class TestingOutputStream extends OutputStream {

    /**
     * The text area where dynamic text is displayed.
     */
    private final transient JTextArea textArea;

    /**
     * Local string builder variable. private field to avoid object
     * creation on each write.
     */
    private final transient StringBuilder strBuild = new StringBuilder();

    /**
    * Create an output stream to the given text area.
    * @param newtextArea The text area to dynamically output to.
    */
    public TestingOutputStream(final JTextArea newtextArea) {
        super();
        this.textArea = newtextArea;
    }

   /**
    * (non-Javadoc).
    */
   @Override
   public final void flush() {
//       strBuild.setLength(0);
//       textArea.setText("");
   }

   /**
    * (non-Javadoc).
    */
   @Override
   public void close() {
       // No implementation necessary
   }

   @Override
   public final void write(final int buffer) throws IOException {

      if (buffer == '\r') {
         return;
      }
      if (buffer == '\n') {
         final String text = strBuild.toString() + "\n";
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               textArea.append(text);
            }
         });
         strBuild.setLength(0);

         return;
      }

      strBuild.append((char) buffer);
   }
}
