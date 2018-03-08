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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * a utility class used to generate the PDF certificates
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class PDFGenerator {

    public static String verificationKeyLabel = "========  VERIFICATION KEY  ========";

    /**
     * a static method, which generates the PDF certificates
     * @param file the file object of the PDF
     * @param testTrace the test trace of the tool
     * @param authID the digital signature
     * @param editor reference to the editor
     * @param testName the name of the certificate test
     * @param username the username of the user requesting a certificate
     * @param date the current date and time
     */
    public static void generate(File file, String testTrace, String authID, String date, String testName, String username, BasicGraphEditor editor){
        if (authID == null || authID.equals("")){
            JOptionPane.showMessageDialog(editor,
                    "Something went wrong while generating your certificate. The verification key is not valid.",
                    "Certificate generation error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            writer.setPageEmpty(false);

            PDFGenerator.generateDocument(document, testTrace, authID, Image.getInstance(editor.getClass().getResource("/images/fiesta.png").getFile()), date, testName, username);

            JOptionPane.showMessageDialog(editor,
                    "Successfully saved your certificate in " + file.getPath() + ".",
                    "Saving certificate", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (DocumentException | IOException ex) {
            JOptionPane.showMessageDialog(editor,
                    "Something went wrong while generating your certificate. Make sure the file is not opened by another program.",
                    "Certificate generation error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * this method is used to generate the elements of the pdf document
     * @param document the pdf document object
     * @param testTrace the test trace of the tool
     * @param authID the digital signature
     * @param date the current date and time
     * @param testName the name of the last loaded test
     * @param username the username of the user requesting a certificate
     * @logo the Image object of the fiesta logo
     */
    private static void generateDocument(Document document, String testTrace, String authID, Image logo, String date, String testName, String username) throws DocumentException{
        document.open();

        // generate a heading
        Font font = FontFactory.getFont(FontFactory.COURIER, 19, new BaseColor(7, 34, 76));
        Paragraph heading = new Paragraph("Fiesta Certificate", font);
        heading.setAlignment(Element.ALIGN_CENTER);
        document.add(heading);
        document.add(new Paragraph(" ")); // add an empty line under the heading

        // add the name of the test
        font = FontFactory.getFont(FontFactory.COURIER, 17, new BaseColor(7, 34, 76));
        heading = new Paragraph("Generated for test: " + testName, font);
        heading.setAlignment(Element.ALIGN_CENTER);
        document.add(heading);
        document.add(new Paragraph(" "));

        // add user's username
        font = FontFactory.getFont(FontFactory.COURIER, 15, new BaseColor(7, 34, 76));
        heading = new Paragraph("Certificate owned by: " + username, font);
        heading.setAlignment(Element.ALIGN_CENTER);
        document.add(heading);
        document.add(new Paragraph(" "));

        // generate the test trace in the pdf
        font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, BaseColor.BLACK);
        Paragraph reportParagraph = new Paragraph(testTrace, font);
        document.add(reportParagraph);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));  // add two empty lines under the test trace

        // add the Fiesta logo
        logo.setAlignment(Element.ALIGN_CENTER);
        logo.scalePercent(40f, 40f);
        document.add(logo);

        document.add(new Paragraph(" "));  // add an empty line under the logo

        // generate a timestamp
        font = FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, 14, new BaseColor(13, 46, 99));
        Chunk horizontalGlue = new Chunk(new VerticalPositionMark());
        Paragraph timestamp = new Paragraph(date, font);
        timestamp.add(new Chunk(horizontalGlue));
        timestamp.add("Fiesta IoT");
        document.add(timestamp);
        document.add(new Paragraph(" "));  // add an empty line under the test trace

        document.newPage();

        Paragraph signature = new Paragraph(verificationKeyLabel);
        signature.setAlignment(Element.ALIGN_CENTER);
        document.add(signature);
        signature = new Paragraph(authID);
        signature.setAlignment(Element.ALIGN_CENTER);
        document.add(signature);
        signature = new Paragraph(verificationKeyLabel);
        signature.setAlignment(Element.ALIGN_CENTER);
        document.add(signature);

        document.close();
    }
}