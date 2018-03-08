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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor;

import java.awt.Dimension;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.EmptyAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.ExecuteAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.GraphAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.ReportsAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions.NewAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions.OpenAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions.SaveAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.VerifyAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.XMLAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions;

/**
 * The editor toolbar at the top of the GUI. Simply a set of buttons
 * with a corresponding action in the EditorActions class. Eg. Open, Save,
 * Undo, etc.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class EditorToolBar extends JToolBar {

    /**
     * the index of the stop button icon in the toolbar
     */
    private final int stopButtonIndex;

    /**
     * a getter method for the stop button index
     *
     * @return the index of the stop button
     */
    public int getStopButtonIndex() {
        return stopButtonIndex;
    }

    /**
     * the index of the next button icon in the tool bar
     */
    private final int nextButtonIndex;

    /**
     * a getter method for the next button index
     *
     * @return the index of the next button
     */
    public int getNextButtonIndex() {
        return nextButtonIndex;
    }

    /**
     * Code to create a selectable button that is added to the toolbar
     * @return
     */
    private JButton createToolbarButton(String actionCommand, String toolTip,
            Action actionCmd, final BasicGraphEditor editor, String iconURL, String selectedIconURL) {

        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTip);
        button.addActionListener(actionCmd);
        button.setFocusPainted(false);
        button.setSelectedIcon(new ImageIcon(BasicGraphEditor.class.getResource(selectedIconURL)));
        button.setIcon(new ImageIcon(BasicGraphEditor.class.getResource(iconURL), actionCommand));
        return button;
    }

    /**
     * Construct a new instance of the editor toolbar.
     * @param editor The basic graph editor that the toolbar is added to.
     * @param orientation The orientation of the toolbar (horizontal)
     */
    public EditorToolBar(final BasicGraphEditor editor, final int orientation) {
		super(orientation);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(3, 3, 3, 3), getBorder()));
		setFloatable(false);

                add(createToolbarButton("New", "New File", new NewAction(editor), editor, "/images/new.png", "/images/newselect.png"));

                add(createToolbarButton("Open", "Open File", new OpenAction(editor), editor, "/images/open.png", "/images/openselect.png"));

                add(createToolbarButton("Save", "Save File", new SaveAction(editor, false), editor, "/images/save.png", "/images/saveselect.png"));

                add(createToolbarButton("Import", "Import Model", new FileActions.ImportAction(editor), editor, "/images/import.png", "/images/importselect.png"));

                addSeparator(new Dimension(15,3));

                add(createToolbarButton("XML", "View as XML", new XMLAction(editor), editor, "/images/xml.png", "/images/xmlselect.png"));

                add(createToolbarButton("Model", "View as Model", new GraphAction(editor), editor, "/images/model.png", "/images/modelselect.png"));

                addSeparator(new Dimension(15,3));

                add(createToolbarButton("Check", "Check Model", new VerifyAction(editor), editor, "/images/check.png", "/images/checkselect.png"));

                add(createToolbarButton("Run", "Run Test", new ExecuteAction(), editor, "/images/run.png", "/images/runselect.png"));

                stopButtonIndex = getComponentIndex(add(createToolbarButton("Stop", "Stop Test", new EmptyAction(), editor, "/images/stop.png", "/images/stopselect.png")));

                nextButtonIndex = getComponentIndex(add(createToolbarButton("Step", "Next step", new EmptyAction(), editor, "/images/step.png", "/images/stepselect.png")));

                addSeparator(new Dimension(15,3));

                add(createToolbarButton("Reports", "View Previous Test Reports", new ReportsAction(editor), editor, "/images/tests.png", "/images/testsselect.png"));
	}
}