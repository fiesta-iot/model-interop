/*
Copyright (c) 2001-2014, JGraph Ltd
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the JGraph nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL JGRAPH BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import uk.ac.soton.itinnovation.xifiinteroperability.ServiceLogger;

/**
 * Pop-up dialogue with the about message; i.e. who developed the software,
 * what it does, etc.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class EditorAboutFrame extends JDialog {

    /**
     * Construct the new about frame.
     */
    private static String uriString = "https://gitlab.it-innovation.soton.ac.uk/iot/model-interop/blob/public/docs/attributions.md";

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
          try {
            Desktop.getDesktop().browse(uri);
          } catch (IOException e) { /* TODO: error handling */ }
        } else { /* TODO: error handling */ }
    }

    class OpenUrlAction implements ActionListener {
          @Override public void actionPerformed(ActionEvent e) {
              try {
                  final URI uri = new URI(uriString);
                  open(uri);
              } catch (URISyntaxException ex) {
              }
          }
        }

    /**
     * Construct the new about frame.
     * @param owner The frame hosting the app.
     */
    public EditorAboutFrame(final Frame owner) {
        super(owner);
        setTitle("Model Based Interoperability Testing Tool");
        setLayout(new BorderLayout());

        // Creates the gradient panel
        final JPanel panel = new JPanel(new BorderLayout()) {





            @Override
            public void paintComponent(final Graphics gphs) {
                    super.paintComponent(gphs);

                    // Paint gradient background
                    Graphics2D g2d = (Graphics2D) gphs;
                    g2d.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth(),
                                    0, getBackground()));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
            }

        };

            panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                            .createMatteBorder(0, 0, 1, 0, Color.GRAY), BorderFactory
                            .createEmptyBorder(8, 8, 12, 8)));

            // Adds title
            final JLabel titleLabel = new JLabel("Interoperability Testing");
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            titleLabel.setOpaque(false);
            panel.add(titleLabel, BorderLayout.NORTH);

            getContentPane().add(panel, BorderLayout.NORTH);

            final JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            content.add(new JLabel("This tool supports the development of interoperability"));
            content.add(new JLabel("tests for distributed systems"));
            content.add(new JLabel(" "));

            content.add(new JLabel("Copyright (C) 2017 IT Innovation"));
            content.add(new JLabel("All rights reserved."));
            content.add(new JLabel(" "));

            JButton button = new JButton();
            button.setText("See acknowledgements in a browser");
            button.setBorderPainted(false);
            button.setOpaque(false);
            button.setBackground(Color.WHITE);
            button.setToolTipText(uriString);
            button.addActionListener(new OpenUrlAction());
            content.add(button);
            content.add(new JLabel(" "));

            try {
                    content.add(new JLabel("Operating System Name: "
                                    + System.getProperty("os.name")));
                    content.add(new JLabel("Java Version: "
                                    + System.getProperty("java.version", "undefined")));
                    content.add(new JLabel(" "));

                    content.add(new JLabel("Total Memory: "
                                    + Runtime.getRuntime().totalMemory()));
                    content.add(new JLabel("Free Memory: "
                                    + Runtime.getRuntime().freeMemory()));
            } catch (Exception e) {
                ServiceLogger.LOG.error("Error getting system info, but ignore");
            }

            getContentPane().add(content, BorderLayout.CENTER);

            final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                            .createMatteBorder(1, 0, 0, 0, Color.GRAY), BorderFactory
                            .createEmptyBorder(16, 8, 8, 8)));
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);

            // Adds OK button to close window
            final JButton closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent event) {
                            setVisible(false);
                    }
            });

            buttonPanel.add(closeButton);

            // Sets default button for enter key
            getRootPane().setDefaultButton(closeButton);

            setResizable(false);
            setSize(400, 400);
    }

    /**
     * Overrides {@link JDialog#createRootPane()} to return a root pane that
     * hides the window when the user presses the ESCAPE key.
     * @return The Panel creatd.
     */
    @Override
    protected final JRootPane createRootPane() {
            final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            final JRootPane rootPane = new JRootPane();
            rootPane.registerKeyboardAction(new ActionListener() {
                    public void actionPerformed(final ActionEvent actionEvent) {
                            setVisible(false);
                    }
            }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
            return rootPane;
    }
}

