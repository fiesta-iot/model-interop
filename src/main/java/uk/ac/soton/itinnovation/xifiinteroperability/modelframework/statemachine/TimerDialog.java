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
package uk.ac.soton.itinnovation.xifiinteroperability.modelframework.statemachine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * A JDialog to show the seconds left until there is a timeout
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class TimerDialog extends JDialog {

    private final JLabel timeoutLabel = new JLabel();

    public TimerDialog() {
        super();
    }

    /**
     * the initGUI method which initialises the GUI components
     *
     * @param time the timeout in milliseconds
     */
    public void initGUI(long time){
        this.setTitle("Timeout count");
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        timeoutLabel.setText("      Time left before timeout transition takes place: " + time + "ms      ");
        timeoutLabel.setFont(new Font("Serif", Font.BOLD, timeoutLabel.getFont().getSize() + 3));
        timeoutLabel.setForeground(new Color(0, 0, 153));
        timeoutLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel timeoutLabelPanel = new JPanel();
        timeoutLabelPanel.setLayout(new BoxLayout(timeoutLabelPanel, BoxLayout.PAGE_AXIS));
        timeoutLabelPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        timeoutLabelPanel.add(timeoutLabel);
        timeoutLabelPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        this.add(timeoutLabelPanel, BorderLayout.CENTER);

        this.startTimer(time);

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void startTimer(long time){

        JDialog reference = this;
        new Timer(500, new ActionListener() {
            int timeCount = -500;

            @Override
            public void actionPerformed(ActionEvent ae) {
                timeCount += 500;
                if (timeCount >= time){
                    reference.dispose();
                }
                else {
                    timeoutLabel.setText("      Time left before timeout transition takes place: " + (time - timeCount) + "ms      ");
                }
            }
        }).start();
    }
}
