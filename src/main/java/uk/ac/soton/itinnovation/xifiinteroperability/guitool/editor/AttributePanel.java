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
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.ComponentForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.EmptyForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.GuardForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.MessageForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.NodeForm;
import java.awt.Color;
import java.awt.CardLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections.CollectionsBrowserForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.DataLoopForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.EndForm;

/**
 * The Attribute panel where different panels are switched between to input
 * and view attributes attached to different elements of the pattern.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class AttributePanel extends JPanel{

    /**
     * There are five types of panels:
     * 1) A guard table - for a guard transition
     * 2) A message form - for creating message content
     * 3) A node table - for data constants associated with a node
     * 4) A Component table - proxy property generation
     * 5) A client table - data about a client in the application.
     */
    private final transient MessageForm mForm;

    /**
     * Get the message component element. This is the form in the left
     * hand panel for entering message details.
     * @return The reference to the UI component.
     */
    public final MessageForm getMessageComponent() {
        return mForm;
    }

    /**
     * Reference to the Component Form UI component.
     */
    private final transient ComponentForm cForm;

    /**
     * Get the Component component element. This is the form in the left
     * hand panel for entering proxy component details.
     * @return The reference to the UI component.
     */
    public final ComponentForm getComponentForm() {
        return cForm;
    }

    /**
     * Reference to the Guard Form UI component.
     */
    private final transient GuardForm gForm;

    /**
     * Get the guard component element. This is the form in the left
     * hand panel for entering guard details.
     * @return The reference to the UI component.
     */
    public final GuardForm getGuardForm() {
        return gForm;
    }

    /**
     * Reference to the Node Form UI component.
     */
    private final transient NodeForm nForm;

    /**
     * Get the Node component element. This is the form in the left
     * hand panel for entering node details.
     * @return The reference to the UI component.
     */
    public final NodeForm getNodeForm() {
        return nForm;
    }

    /**
     * Reference to the  End Node Form UI component.
     */
    private final transient DataLoopForm dlForm;

    /**
     * Reference to the  End Node Form UI component.
     */
    private final transient EndForm eForm;

    /**
     * Get the End Node component element. This is the form in the left
     * hand panel for entering node details.
     * @return The reference to the UI component.
     */
    public final EndForm getEndForm() {
        return eForm;
    }
    
    /**
     * Get the Data Loop Node component element. This is the form in the left
     * hand panel for entering node details.
     * @return The reference to the UI component.
     */
    public final DataLoopForm getDataLoopForm() {
        return dlForm;
    }
    
    /**
     * reference to the Collections Browser form
     */
    private final transient CollectionsBrowserForm bForm;

    /**
     * a getter method for the browser form
     * @return the collections browser form
     */
    public final CollectionsBrowserForm getBrowserForm(){
        return bForm;
    }
    
    /**
     * Constant panel refs.
     */

    /**
     * the attribute panel for link transitions, between 2 components
     */
    private static final String LINKPANEL = "link";

    /**
     * The attribute panel for guards.
     */
    private static final String GUARDPANEL = "guard";

    /**
     * The attribute panel for start nodes.
     */
    private static final String NODEPANEL = "start";

    /**
     * The attribute panel for Rest interface components.
     */
    private static final String COMPONENTPANEL = "component";

    /**
     * The attribute panel for HTTP message input.
     */
    private static final String MESSAGEPANEL = "message";

    /**
     * The attribute panel for end nodes.
     */
    private static final String ENDPANEL = "end";

    /**
     * The attribute panel for normal nodes.
     */
    private static final String NORMALPANEL = "normal";

    /**
     * The attribute panel for loop nodes
     */
    private static final String LOOPPANEL = "loop";

    /**
     * The attribute panel for loop nodes
     */
    private static final String DATALOOPPANEL = "dataloop";
    /**
     * The attribute panel for trigger nodes.
     */
    private static final String TRIGGERPANEL = "trigger";

    /**
     * The panel opened on test execution
     */
    public static final String EXECUTION = "execution";

    private final JPanel attributePanel;

    public JPanel getAttribuePanel() {
        return this.attributePanel;
    }

    /**
     * Change the look and feel.
     * @param input The table to change the feel of.
     */
    public static void setTableConsistentLookAndFeel(final JTable input) {
         // Configure some of JTable's paramters
            input.setShowHorizontalLines(true);

            // Change the selection colour
            input.setSelectionForeground(Color.white);
            input.setSelectionBackground(Color.LIGHT_GRAY);
    }

    /**
     * Create a new instance of the attribute panel within the editor context.
     * @param parent The parent panel this is hosted in.
     * @param editor The editor this is hosted in.
     * @param libraryPane
     */
    public AttributePanel(final JPanel parent, final BasicGraphEditor editor, final JTabbedPane libraryPane) {
        super(new BorderLayout());

        // HTTP Method
        final String[] httpMethods = {"GET", "PUT", "POST", "DELETE"};
        final String[] dataTypes = {"XML", "JSON", "OTHER"};

        // Creates the left split pane that contains the library with the
        // palettes and the card attributes
        attributePanel = new JPanel(new CardLayout());

        final JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                        libraryPane, attributePanel);
        inner.setDividerLocation(100);
        inner.setResizeWeight(0.3);
        inner.setOneTouchExpandable(false);
        inner.setContinuousLayout(false);
        inner.setDividerSize(2);
        inner.setBorder(null);


        bForm = new CollectionsBrowserForm(editor);
        mForm = new MessageForm(httpMethods, dataTypes, editor);
        cForm = new ComponentForm(editor);
        gForm = new GuardForm(editor);
        nForm = new NodeForm(editor);
        eForm = new EndForm();
        dlForm = new DataLoopForm();

        //Create the panel that contains the "cards".
        attributePanel.add(bForm, "EmptyPanel");
        attributePanel.add(new EmptyForm(), LINKPANEL);
        attributePanel.add(gForm, GUARDPANEL);
        attributePanel.add(nForm, NODEPANEL);
        attributePanel.add(cForm, COMPONENTPANEL);
        attributePanel.add(mForm, MESSAGEPANEL);
        attributePanel.add(eForm, ENDPANEL);
        attributePanel.add(new EmptyForm(), NORMALPANEL);
        attributePanel.add(new EmptyForm(), LOOPPANEL);
        attributePanel.add(dlForm, DATALOOPPANEL);
        attributePanel.add(new EmptyForm(), TRIGGERPANEL);

        parent.add(inner);
    }

}
