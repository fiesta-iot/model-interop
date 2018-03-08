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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import uk.ac.soton.itinnovation.xifiinteroperability.ConfigurationException;
import uk.ac.soton.itinnovation.xifiinteroperability.SystemProperties;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.PDFGenerator;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions.OpenFromWebAction;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.InteroperabilityReport;

/**
 * This class holds all the actions related to generation and verification of certificates
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class CertificateActions {

    /**
     *
     */
    private static String ServerURL = null;

    /**
     * Utility class, therefore use a private constructor.
     */
    private CertificateActions() {

    }

    /**
     * an action to open a certification model
     */
    public static class CertificateOpenAction extends OpenFromWebAction {

        public CertificateOpenAction(BasicGraphEditor editor){
            super(editor, true);
        }

    }

    /**
     * an action to request a certificate
     */
    public static class CertificateRequestAction extends AbstractAction {

        /**
         * reference to the editor
         */
        private final BasicGraphEditor editor;

        /**
         * constructor for this action, sets the editor reference
         *
         * @param editor the editor reference
         */
        public CertificateRequestAction(BasicGraphEditor editor){
            this.editor = editor;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {

            try {
                ServerURL = SystemProperties.readProperty("certification");
            } catch (ConfigurationException ex) {
                System.err.println("Failed to read certification system property: " + ex.getMessage());
            }

            if (editor.getCertificationManager().getLastURL() == null){
                JOptionPane.showMessageDialog(editor,
                        "In order to request a certificate open a repository model from the certification menu.",
                        "Certification error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!editor.getCertificationManager().getExecuted()){
                JOptionPane.showMessageDialog(editor,
                        "In order to request a certificate you must execute the loaded test first. Click on the 'Run' icon in the menu toolbar.",
                        "Certification error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // create the login form
            JPanel loginForm = new JPanel();
            loginForm.setLayout(new BoxLayout(loginForm, BoxLayout.PAGE_AXIS));

            JPanel usernamePanel = new JPanel();
            usernamePanel.setLayout(new BoxLayout(usernamePanel, BoxLayout.LINE_AXIS));
            usernamePanel.add(new JLabel("Username    "));
            JTextField usernameField = new JTextField("", 15);
            usernamePanel.add(usernameField);
            loginForm.add(usernamePanel);

            JPanel passwordPanel = new JPanel();
            passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.LINE_AXIS));
            passwordPanel.add(new JLabel("Password    "));
            JPasswordField passwordField = new JPasswordField("", 15);
            passwordPanel.add(passwordField);
            loginForm.add(passwordPanel);

            int check = JOptionPane.showConfirmDialog(editor, loginForm, "Certificate request authentication", JOptionPane.OK_CANCEL_OPTION);

            if (check != JOptionPane.OK_OPTION){
                return;
            }

            // authenticate user
            Client client = Client.create();
            WebResource webResourceOpenAM = client.resource("https://platform.fiesta-iot.eu/openam/json/authenticate");
            ClientResponse responseAuth = webResourceOpenAM.type("application/json")
                    .header("X-OpenAM-Username", usernameField.getText())
                    .header("X-OpenAM-Password", new String(passwordField.getPassword())).post(ClientResponse.class, "{}");
            if (responseAuth.getStatus() != 200){
                JOptionPane.showMessageDialog(editor, "Invalid credentials. You cannot request a certificate!",
                        "Authentication error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Map<String, Object> jsonResponse = new ObjectMapper().readValue(responseAuth.getEntity(String.class), HashMap.class);
                if (!jsonResponse.containsKey("tokenId") || jsonResponse.get("tokenId") == null || jsonResponse.get("tokenId") == ""){
                    JOptionPane.showMessageDialog(editor, "Invalid credentials. You cannot request a certificate!",
                            "Authentication error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            catch (IOException ioe){
                JOptionPane.showMessageDialog(editor, "Invalid credentials. You cannot request a certificate!",
                            "Authentication error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                InteroperabilityReport report = editor.getCodePanel().getTestingPanel().getInteroperabilityReport();
                Map<String, String> testReport = new HashMap<>();
                testReport.put("success", report.getSuccess());
                testReport.put("report", report.getTextTrace());
                testReport.put("model", editor.getCertificationManager().getExecutedModel());
                testReport.put("modelUrl", editor.getCertificationManager().getLastURL());
                String testName = editor.getCertificationManager().getTestName();
                testReport.put("testName", testName);
                testReport.put("username", usernameField.getText());
                String jsonTestReport = new ObjectMapper().writeValueAsString(testReport);

                System.out.println("test report = \n" + jsonTestReport);

                byte[] testReportBytes = jsonTestReport.getBytes(StandardCharsets.UTF_8);
                int testReportLength = testReportBytes.length;

                String urlLink = editor.getCertificationManager().getLastURL();
                int index = urlLink.length();
                String id = "";
                while (!urlLink.substring(index-1, index).equals("/")){
                    id = urlLink.substring(index-1, index) + id;
                    index -= 1;
                }

                 URL url = new URL(urlLink + "/certify"); // this implementation is to be used when the API is updated on the actual server
//                URL url = new URL(ServerURL +"/" + id + "/certify"); // localhost url is currently used for testing purposes
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(testReportLength));

                try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                    dos.write(testReportBytes);
                }
                catch(Exception e) {
                    JOptionPane.showMessageDialog(editor, "There is an error with the server of the repository.", "Internal server error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                StringBuilder responseBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
                br.close();
                
                Map<String, Object> jsonResponseMap = new ObjectMapper().readValue(responseBuilder.toString(), HashMap.class);

                String authID = (String) jsonResponseMap.get("authenticationID");
                String date = (String) jsonResponseMap.get("date");
                String response = (String) jsonResponseMap.get("certificate");

                if (response.equalsIgnoreCase("Altered")) {
                    JOptionPane.showMessageDialog(editor,
                            "The originally loaded model for certification has been altered.\n"
                            + "Either reload the certification model or remove your changes.",
                            "Altered model", JOptionPane.ERROR_MESSAGE);
                }
                else if (response.equalsIgnoreCase("Error")) {
                    JOptionPane.showMessageDialog(editor,
                            "Couldn't generate a certificate, because an unexpected error occured.",
                            "Error while generating certificate", JOptionPane.ERROR_MESSAGE);
                }
                else if (response.equalsIgnoreCase("Failure")) {
                    JOptionPane.showMessageDialog(editor,
                            "The test's last state is not considered to be a successful end state. A certificate cannot be generated.",
                            "Requesting a certificate", JOptionPane.PLAIN_MESSAGE);
                }
                else if (response.equalsIgnoreCase("Success")) {
                    int choice = JOptionPane.showConfirmDialog(editor, "Successfully generated a certificate. Do you want to save it?",
                            "Saving certificate", JOptionPane.YES_NO_OPTION);
                    if (choice != JOptionPane.YES_OPTION){
                        return;
                    }
                    JFileChooser fileChooser = new JFileChooser(System.getProperty("dir"));
                    fileChooser.setDialogTitle("Choose a directory to save the certificate");
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int fileChoice = fileChooser.showOpenDialog(null);

                    if (fileChoice != JFileChooser.APPROVE_OPTION) {
                        return;
                    }

                    File file = fileChooser.getSelectedFile();

                    File certificateFile = new File(Paths.get(file.getPath(), "certificate.pdf").toString());

                    if (certificateFile.exists()) {
                        int confirmation = JOptionPane.showConfirmDialog(editor, "There is already a file named 'certificate.pdf' in this directory."
                                + "Are you sure you want to continue?", "Overriding existing file", JOptionPane.YES_NO_OPTION);
                        if (confirmation != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }

                    PDFGenerator.generate(certificateFile, report.getTextTrace(), authID, date, testName, usernameField.getText(), editor);
                }
            }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(editor, "There is an error with the server of the repository.", "Internal server error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    /**
     * action to verify that a pdf certificate was generated by the Fiesta server
     */
    public static class VerifyCertificateAction extends AbstractAction {

        /**
         * reference to the editor
         */
        private final BasicGraphEditor editor;

        /**
         * constructor for this action, sets the editor reference
         *
         * @param editor the editor reference
         */
        public VerifyCertificateAction(BasicGraphEditor editor){
            this.editor = editor;
        }

        /**
         * this method is used for testing purposes only
         * @param file the pdf certificate to verify
         * @return True if the certificate is a valid one
         * @throws IOException
         */
        public static final boolean verifyCertificate(File file) throws IOException {
            // read the content of the pdf certificate
            StringBuilder fullContent = new StringBuilder();
            PdfReader reader = new PdfReader(file.getAbsolutePath());
            for (int i = 1; i < reader.getNumberOfPages(); i++) {
                fullContent.append(PdfTextExtractor.getTextFromPage(reader, i));
            }
            for (int i = 1; i < reader.getNumberOfPages(); i++) {
                fullContent.append(Base64.getEncoder().encodeToString(reader.getPageContent(i)));
            }

            String certificateContent = fullContent.toString();

            String verificationKey = PdfTextExtractor.getTextFromPage(reader, reader.getNumberOfPages());
            int index = verificationKey.indexOf(PDFGenerator.verificationKeyLabel);
            if (index < 0) {
                reader.close();
                return false;
            }
            if (reader.getPageResources(reader.getNumberOfPages()).size() > 1
                    || Pattern.matches(PDFGenerator.verificationKeyLabel + ".+" + PDFGenerator.verificationKeyLabel, verificationKey.replaceAll("\\s+", ""))) {
                reader.close();
                return false;
            }

            verificationKey = verificationKey.substring(index).replaceAll(PDFGenerator.verificationKeyLabel, "").trim();

            reader.close();

            Map<String, String> dataToVerify = new HashMap<>();
            dataToVerify.put("verificationKey", verificationKey);
            dataToVerify.put("certificateContent", certificateContent);
            String jsonDataToVerify = new ObjectMapper().writeValueAsString(dataToVerify);
            byte[] dataToVerifyBytes = jsonDataToVerify.getBytes();

            // send a request to server to verify the certificate content and the verification key
            URL url = new URL(ServerURL + "/certificates/verify");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(dataToVerifyBytes.length));

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.write(dataToVerifyBytes);
            }

            StringBuilder responseBuilder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
            br.close();

            boolean verified = Boolean.parseBoolean(responseBuilder.toString());

            return verified;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                ServerURL = SystemProperties.readProperty("certification");
            } catch (ConfigurationException ex) {
                System.err.println("Failed to read certification system property: " + ex.getMessage());
            }

            JFileChooser fileChooser = new JFileChooser(System.getProperty("dir"));
            fileChooser.setDialogTitle("Choose the PDF file of the certificate to verify");
            fileChooser.setAcceptAllFileFilterUsed(false);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("*.pdf", "pdf");
            fileChooser.setFileFilter(filter);

            int fileChoice = fileChooser.showOpenDialog(null);

            if (fileChoice != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = fileChooser.getSelectedFile();
            try {
                // read the content of the pdf certificate
                StringBuilder fullContent = new StringBuilder();
                PdfReader reader = new PdfReader(file.getAbsolutePath());
                for(int i=1; i < reader.getNumberOfPages(); i++){
                    fullContent.append(PdfTextExtractor.getTextFromPage(reader, i));
                }
                for(int i=1; i < reader.getNumberOfPages(); i++){
                    fullContent.append(Base64.getEncoder().encodeToString(reader.getPageContent(i)));
                }

                String certificateContent = fullContent.toString();

                String verificationKey = PdfTextExtractor.getTextFromPage(reader, reader.getNumberOfPages());
                int index = verificationKey.indexOf(PDFGenerator.verificationKeyLabel);
                if (index < 0){
                    JOptionPane.showMessageDialog(editor, "No verification key found in this certificate. "
                            + "Therefore, certificate is not a valid Fiesta certificate.",
                        "Reading error", JOptionPane.ERROR_MESSAGE);
                    reader.close();
                    return;
                }
                if (reader.getPageResources(reader.getNumberOfPages()).size() > 1
                        || Pattern.matches(PDFGenerator.verificationKeyLabel + ".+" + PDFGenerator.verificationKeyLabel, verificationKey.replaceAll("\\s+", ""))){
                    JOptionPane.showMessageDialog(editor, "The certificate has been altered. Invalid certificate!",
                            "Invalid certificate", JOptionPane.ERROR_MESSAGE);
                    reader.close();
                    return;
                }

                verificationKey = verificationKey.substring(index).replaceAll(PDFGenerator.verificationKeyLabel, "").trim();

                reader.close();

                Map<String, String> dataToVerify = new HashMap<>();
                dataToVerify.put("verificationKey", verificationKey);
                dataToVerify.put("certificateContent", certificateContent);
                String jsonDataToVerify = new ObjectMapper().writeValueAsString(dataToVerify);
                byte[] dataToVerifyBytes = jsonDataToVerify.getBytes();

                // send a request to server to verify the certificate content and the verification key
                URL url = new URL(ServerURL + "/certificates/verify");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(dataToVerifyBytes.length));

                try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                    dos.write(dataToVerifyBytes);
                }

                StringBuilder responseBuilder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
                br.close();

                boolean verified = Boolean.parseBoolean(responseBuilder.toString());

                if (verified){
                    JOptionPane.showMessageDialog(editor, "The PDF certificate is authenticated as a valid certificate generated by Fiesta.",
                            "Succussful certificate verification", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(editor, "The PDF certificate is authenticated as an invalid certificate NOT generated by Fiesta.",
                            "Unsuccussful certificate verification", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch(IOException ioe){
                JOptionPane.showMessageDialog(editor, "Something went wrong while reading your PDF certificate.",
                        "Reading error", JOptionPane.ERROR_MESSAGE);
            }
        }

    }
}
