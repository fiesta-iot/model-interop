/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
// Modified By : Nikolay Stanchev - ns17@it-innovation.soton.ac.uk
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;
 
/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and 
 * a JButton to close the tab it belongs to 
 */
public class ButtonTabComponent extends JPanel {
    private final JTabbedPane pane;

    /**
     * Modified By Nikolay Stanchev
     * modification - add a reference to the previous reports panel
     */
    private final transient PreviousReportsPanel previousReportsPanel;
    
    /* Modified by Nikolay Stanchev - constructor takes the previous reports panel as a parameter*/
    public ButtonTabComponent(final JTabbedPane pane, PreviousReportsPanel previousReportsPanel) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        /* Modified by Nikolay Stanchev - setting the reference to the previous reports panel */
        this.previousReportsPanel = previousReportsPanel;
        setOpaque(false);
         
        //make JLabel read titles from JTabbedPane
        JLabel label = new JLabel() {
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
         
        add(label);
        //add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        
        // saveButton
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/save.gif"));
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(14, 14, Image.SCALE_SMOOTH);
        icon = new ImageIcon(newImg);
        JButton saveButton = new SaveButton(icon, 18);
        add(saveButton);
        
        //tab button
        JButton button = new TabButton();
        add(button);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    }
 
    private class TabButton extends JButton implements ActionListener {
        public TabButton() {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            //Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            //Make it transparent
            setContentAreaFilled(false);
            //No need to be focusable
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            //Making nice rollover effect
            //we use the same listener for all buttons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            //Close the proper tab by clicking the button
            addActionListener(this);
        }
 
        @Override
        public void actionPerformed(ActionEvent e) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                /* Modifed by Nikolay Stanchev - let the user choose if he wants to save the report */
                int reply = JOptionPane.showConfirmDialog(previousReportsPanel, 
                        "Do you want to save this test report into a text file?",
                        "Save test report", JOptionPane.YES_NO_CANCEL_OPTION, 
                        JOptionPane.PLAIN_MESSAGE);
                if (reply == JOptionPane.YES_OPTION) {
                    String wDir;
                    boolean userChosenFile = false;
                    if (previousReportsPanel.getEditor().getCurrentFile() != null){
                        wDir = previousReportsPanel.getEditor().getCurrentFile().getParent();
                    }
                    else {
                        final JFileChooser fChooser = new JFileChooser(System.getProperty("user.dir"));
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files (.txt)", "txt", "text");
                        fChooser.setFileFilter(filter);
                        fChooser.setAcceptAllFileFilterUsed(false);
                        final int check = fChooser.showDialog(previousReportsPanel, "Save test report");
                        
                        if (check != JFileChooser.APPROVE_OPTION) {
                            return;
                        }
                        else {
                            wDir = fChooser.getSelectedFile().getAbsolutePath();
                            userChosenFile = true;
                        }
                    }
                    
                    if (wDir != null){
                        java.io.FileWriter fWrite = null;
                        try {
                            File file;
                            if (userChosenFile){
                                if (wDir.contains(".txt")){
                                    file = new File(wDir);
                                }
                                else {
                                    file = new File(wDir + ".txt");
                                }
                            }
                            else {
                                String fileName = pane.getTitleAt(i) + ".txt";
                                fileName = fileName.replaceAll("\\s+", "");
                                fileName = fileName.replaceFirst(":", "h");
                                fileName = fileName.replaceFirst(":", "m");
                                fileName = fileName.replaceFirst("\\.", "s.");

                                file = new File(new File(wDir), fileName);
                            }
                            
                            if (!file.exists() || 
                                    (file.exists() && JOptionPane.showConfirmDialog(previousReportsPanel,
                                    "File " + file.getAbsolutePath() + " will be overwritten. "
                                            + "Are you sure you want to continue?",
                                    "Overriding file", JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)) {
                                
                                fWrite = new java.io.FileWriter(file.getAbsolutePath());
                                try {
                                    fWrite.write(previousReportsPanel.getPreviousReports().get(pane.getTitleAt(i)));
                                    JOptionPane.showMessageDialog(previousReportsPanel, 
                                            "Successfully saved report to " + file.getAbsolutePath(), "Report saving", 
                                            JOptionPane.INFORMATION_MESSAGE);
                                    
                                    /* Modified by Nikolay Stanchev - remove the report from the pevious 
                                       reports and set the new panel title */
                                    previousReportsPanel.getPreviousReports().remove(pane.getTitleAt(i));
                                    pane.remove(i);
                                    previousReportsPanel.setPanelTitle();
                                }
                                finally {
                                    fWrite.close();
                                }
                            }
                        } 
                        catch (IOException ex) {
                            ServiceLogger.LOG.error("Error saving file", ex);
                            JOptionPane.showMessageDialog(previousReportsPanel,
                                    "An error occured while saving your test report. Please try again!",
                                    "Error saving file", JOptionPane.ERROR_MESSAGE);
                        }
                        finally {
                            try {
                                if (fWrite != null) {
                                    fWrite.close();
                                }
                            } 
                            catch (IOException ex) {
                                ServiceLogger.LOG.error("Error saving file", ex);
                                JOptionPane.showMessageDialog(previousReportsPanel,
                                    "An error occured while saving your test report. Please try again!",
                                    "Error saving file", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }                                        
                }
                else if (reply == JOptionPane.NO_OPTION) {
                    /* Modified by Nikolay Stanchev - remove the report from the pevious 
                       reports and set the new panel title */
                    previousReportsPanel.getPreviousReports().remove(pane.getTitleAt(i));
                    pane.remove(i);
                    previousReportsPanel.setPanelTitle();
                }
                
            }
        }
 
        //we don't want to update UI for this button
        public void updateUI() {
        }
 
        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.MAGENTA);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
    
    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }
 
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
    
    /**
     * created by Nikolay Stanchev - SaveButton
     */
    private class SaveButton extends JButton implements ActionListener {
        
        public SaveButton(Icon icon, int size) {
            super(icon);
            
            setPreferredSize(new Dimension(size, size));
            setToolTipText("save this tab");
            setUI(new BasicButtonUI());
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            setRolloverEnabled(true);
            addMouseListener(buttonMouseListener);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            
            String wDir;
            boolean userChosenFile = false;
            if (previousReportsPanel.getEditor().getCurrentFile() != null) {
                wDir = previousReportsPanel.getEditor().getCurrentFile().getParent();
            } 
            else {
                final JFileChooser fChooser = new JFileChooser(System.getProperty("user.dir"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files (.txt)", "txt", "text");
                fChooser.setFileFilter(filter);
                fChooser.setAcceptAllFileFilterUsed(false);
                final int check = fChooser.showDialog(previousReportsPanel, "Save test report");

                if (check != JFileChooser.APPROVE_OPTION) {
                    return;
                } else {
                    wDir = fChooser.getSelectedFile().getAbsolutePath();
                    userChosenFile = true;
                }
            }

            if (wDir != null) {
                java.io.FileWriter fWrite = null;
                try {
                    File file;
                    if (userChosenFile) {
                        if (wDir.contains(".txt")) {
                            file = new File(wDir);
                        } else {
                            file = new File(wDir + ".txt");
                        }
                    } 
                    else {
                        String fileName = pane.getTitleAt(i) + ".txt";
                        fileName = fileName.replaceAll("\\s+", "");
                        fileName = fileName.replaceFirst(":", "h");
                        fileName = fileName.replaceFirst(":", "m");
                        fileName = fileName.replaceFirst("\\.", "s.");

                        file = new File(new File(wDir), fileName);
                    }

                    if (!file.exists()
                            || (file.exists() && JOptionPane.showConfirmDialog(previousReportsPanel,
                            "File " + file.getAbsolutePath() + " will be overwritten. "
                            + "Are you sure you want to continue?",
                            "Overriding file", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)) {

                        fWrite = new java.io.FileWriter(file.getAbsolutePath());
                        try {
                            fWrite.write(previousReportsPanel.getPreviousReports().get(pane.getTitleAt(i)));
                            JOptionPane.showMessageDialog(previousReportsPanel,
                                    "Successfully saved report to " + file.getAbsolutePath(), "Report saving",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } finally {
                            fWrite.close();
                        }
                    }
                } catch (IOException ex) {
                    ServiceLogger.LOG.error("Error saving file", ex);
                    JOptionPane.showMessageDialog(previousReportsPanel,
                            "An error occured while saving your test report. Please try again!",
                            "Error saving file", JOptionPane.ERROR_MESSAGE);
                } finally {
                    try {
                        if (fWrite != null) {
                            fWrite.close();
                        }
                    } catch (IOException ex) {
                        ServiceLogger.LOG.error("Error saving file", ex);
                        JOptionPane.showMessageDialog(previousReportsPanel,
                                "An error occured while saving your test report. Please try again!",
                                "Error saving file", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}
