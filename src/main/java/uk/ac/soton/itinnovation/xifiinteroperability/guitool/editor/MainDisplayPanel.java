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

import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.PreviousReportsPanel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.XMLSpecificationPanel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.TestingOutputPanel;

/**
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class MainDisplayPanel {

    /**
     * Constant to switch to the Graph card.
     */
    public static final String GRAPHPANEL = "graph";

    /**
     * Constant to switch to the xml card.
     */
    public static final String CODEPANEL = "xml";

    /**
     * Constant to switch to the testing output card.
     */
    public static final String REPORTPANEL = "exe";

    /**
     * Constant to switch to previous reports card
     */
    public static final String PREVIOUSREPORTS = "reports";


    /**
     * The XML Panel.
     */
    private final transient XMLSpecificationPanel xmlDocument;

    /**
     * Get the XML panel reference.
     * @return the reference to the jpanel.
     */
    public final XMLSpecificationPanel getXMLPanel() {
        return xmlDocument;
    }


    /**
     * The testing report panel.
     */
    private final transient TestingOutputPanel exDocument;


    /**
     * Get the reference to the text testing output panel.
     * @return the jpanel where testing output is reported.
     */
    public final TestingOutputPanel getTestingPanel() {
        return exDocument;
    }

    /**
     * The privious reports panel.
     */
    private final transient PreviousReportsPanel reportsDocument;

    /**
     * Get the reference to the previous reports panel
     * @return the old reports panel
     */
    public final PreviousReportsPanel getReportsPanel(){
        return reportsDocument;
    }

    /**
     * Create a new instance of the main display panel.
     * @param parent The parent panel in the UI.
     * @param dModel The underlying data model of the pattern.
     * @param editor The overall editor context.
     */
    public MainDisplayPanel(final JPanel parent, final DataModel dModel,
            final BasicGraphEditor editor) {

            xmlDocument = new XMLSpecificationPanel(dModel, editor);
            exDocument = new TestingOutputPanel();
            reportsDocument = new PreviousReportsPanel(editor);

            final JPanel controls = new JPanel();
            controls.setLayout(new GridLayout(1 , 2));

            /**
             * Add the Interoperability Model Label
             */
            final JLabel gLabel = new JLabel("     Test State Model");
            final Font font = gLabel.getFont();
            final Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
            gLabel.setFont(boldFont);
            controls.add(gLabel);

            /**
             * Add the Deployment Model Label
             */
            final JLabel arcLabel = new JLabel("Deployment Model     ", SwingConstants.RIGHT);
            arcLabel.setFont(boldFont);
            controls.add(arcLabel);

            /**
             * Create the two panels for drag and drop of models
             */
            final JSplitPane viewSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor.getBehaviourGraph(), editor.getSystemGraph());
            viewSplit.setDividerLocation(350);
            viewSplit.setResizeWeight(0.7);
            viewSplit.setDividerSize(1);

            final JSplitPane boxSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controls, viewSplit);
            boxSplit.setDividerSize(0);

            //Create the panel that contains the "cards".
            parent.add(boxSplit, GRAPHPANEL);
            parent.add(xmlDocument, CODEPANEL);
            parent.add(exDocument, REPORTPANEL);
            parent.add(reportsDocument, PREVIOUSREPORTS);

    }

}
