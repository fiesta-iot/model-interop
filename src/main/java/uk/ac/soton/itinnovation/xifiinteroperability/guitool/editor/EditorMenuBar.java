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

import com.mxgraph.util.mxResources;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.CertificateActions;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.ExitAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.GraphAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions.OpenAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions.SaveAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.XMLAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions.ImportAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions.NewAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.PopUpMenuActions.HistoryAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.TestGeneratorsActions;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.ToolsActions;

/**
 * Drop down Menu bar at the top of the GUI. Has a set of drop down menus which
 * are described here.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class EditorMenuBar extends JMenuBar {

    /**
     * Create the drop down menus in the editor.
     * @param editor The editor context.
     */
    public EditorMenuBar(final BasicGraphEditor editor) {
        super();
        JMenu menu = add(new JMenu(mxResources.get("file")));

        JMenuItem menuItem = new JMenuItem(mxResources.get("new"), new ImageIcon(BasicGraphEditor.class.getResource("/images/new16.png")));
        menuItem.addActionListener(new NewAction(editor));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        menu.add(menuItem);

        menuItem = new JMenuItem(mxResources.get("openFile"), new ImageIcon(BasicGraphEditor.class.getResource("/images/open16.png")));
        menuItem.addActionListener(new OpenAction(editor));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        menu.add(menuItem);

        menuItem = new JMenuItem(editor.bind("Open Collection", new FileActions.OpenCollectionAction(editor), "/images/open16.png"));
        menu.add(menuItem);
        
        menuItem = new JMenuItem(editor.bind("Create from Template", new FileActions.OpenTemplateAction(editor), "/images/bricks.png"));
        menu.add(menuItem);
        
        // This functionality is hidden from the UI, since the generation from swagger and raml API specs is still in development.
//        menuItem = new JMenuItem(editor.bind("Create from Swagger API", new TestGeneratorsActions.OpenSwaggerAPIaction(editor), "/images/bricks.png"));
//        menu.add(menuItem);
//        
//        menuItem = new JMenuItem(editor.bind("Create from RAML API", new TestGeneratorsActions.OpenRamlAPIaction(editor), "/images/bricks.png"));
//        menu.add(menuItem);

        menuItem = new JMenuItem(editor.bind("Download Model", new FileActions.OpenFromWebAction(editor), "/images/downloadcert.png"));
        menu.add(menuItem);

        menu.addSeparator();


        menuItem = new JMenuItem(mxResources.get("save"), new ImageIcon(BasicGraphEditor.class.getResource("/images/save16.png")));

        menuItem.addActionListener(new SaveAction(editor, false));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        menu.add(menuItem);
        menu.add(editor.bind(mxResources.get("saveAs"), new SaveAction(editor, true), "/images/saveas16.png"));

        menu.addSeparator();

        menu.add(editor.bind(mxResources.get("import"), new ImportAction(editor), "/images/import16.png"));

        menu.addSeparator();

        menu.add(editor.bind(mxResources.get("exit"), new ExitAction(editor)));

        // Creates the edit menu
        menu = add(new JMenu(mxResources.get("edit")));

        menuItem = new JMenuItem(editor.bind("Cut", new DefaultEditorKit.CutAction(), "/images/cut16.png"));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        menu.add(menuItem);

        menuItem = new JMenuItem(editor.bind("Copy", new DefaultEditorKit.CopyAction(), "/images/copy16.png"));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
        menu.add(menuItem);

        menuItem = new JMenuItem(editor.bind("Paste", new DefaultEditorKit.PasteAction(), "/images/paste16.png"));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
        menu.add(menuItem);

        menu.addSeparator();
        menuItem = new JMenuItem(mxResources.get("undo"), new ImageIcon(BasicGraphEditor.class.getResource("/images/undo16.png")));
        menuItem.addActionListener(new HistoryAction(true, editor));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
        menu.add(menuItem);

        menuItem = new JMenuItem(mxResources.get("redo"), new ImageIcon(BasicGraphEditor.class.getResource("/images/redo16.png")));
        menuItem.addActionListener(new HistoryAction(false, editor));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem(mxResources.get("delete"), new ImageIcon(BasicGraphEditor.class.getResource("/images/bin16.png")));
        menuItem.addActionListener(new EditorActions.Delete(editor));
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        menu.add(menuItem);

         // Creates the view menu
        menu = add(new JMenu(mxResources.get("view")));

        menu.add(editor.bind("Model", new GraphAction(editor), "/images/graph16.png"));
        menu.add(editor.bind(mxResources.get("XML"), new XMLAction(editor), "/images/xml16.png"));
//        menuItem = new JMenuItem("Current Test");
//        menuItem.addActionListener(new EditorActions.TestViewAction(editor));
//        menu.add(menuItem);
        menu.add(editor.bind("Current Test", new EditorActions.TestViewAction(editor), "/images/bug.png"));
        menu.addSeparator();
        menu.add(editor.bind("Previous Tests", new EditorActions.ReportsAction(editor), "/images/report16.png"));

        // creates the run menu
        menu = add(new JMenu("Test"));
        menu.add(editor.bind("Check Model", new EditorActions.VerifyAction(editor), "/images/check16.png"));
        menu.add(editor.bind("Run test", new EditorActions.ExecuteAction(editor), "/images/run16.png"));

        JMenuItem stopButton = new JMenuItem("Stop test");
        stopButton.addActionListener((ActionEvent ae) -> {
            EditorToolBar toolBar = (EditorToolBar) ((BorderLayout) editor.getLayout()).getLayoutComponent(BorderLayout.NORTH);
            JButton stop = (JButton) toolBar.getComponentAtIndex(toolBar.getStopButtonIndex());
            stop.doClick();
        });
        stopButton.setIcon(new ImageIcon(BasicGraphEditor.class.getResource("/images/stop16.png")));
        menu.add(stopButton);

        JMenuItem nextButton = new JMenuItem("Next step");
        nextButton.addActionListener((ActionEvent e) -> {
            EditorToolBar toolBar = (EditorToolBar) ((BorderLayout) editor.getLayout()).getLayoutComponent(BorderLayout.NORTH);
            JButton next = (JButton) toolBar.getComponentAtIndex(toolBar.getNextButtonIndex());
            next.doClick();
        });
        nextButton.setIcon(new ImageIcon(BasicGraphEditor.class.getResource("/images/step16.png")));
        menu.add(nextButton);
        
        menu.add(editor.bind("Clear memory maps", new EditorActions.ClearMemoryMapAction(editor), "/images/delete.gif"));

        // creates the certification menu
        menu = add(new JMenu("Certification"));
        JMenuItem openCertificationModelItem = new JMenuItem("Download Model");
        openCertificationModelItem.addActionListener(new CertificateActions.CertificateOpenAction(editor));
        openCertificationModelItem.setIcon(new ImageIcon(BasicGraphEditor.class.getResource("/images/download.png")));
        menu.add(openCertificationModelItem);

        JMenuItem certifyItem = new JMenuItem("Request Certificate");
        certifyItem.addActionListener(new CertificateActions.CertificateRequestAction(editor));
        certifyItem.setIcon(new ImageIcon(BasicGraphEditor.class.getResource("/images/cert16.png")));

        menu.add(certifyItem);

        JMenuItem verifyItem = new JMenuItem("Verify Certificate");
        verifyItem.addActionListener(new CertificateActions.VerifyCertificateAction(editor));
        verifyItem.setIcon(new ImageIcon(BasicGraphEditor.class.getResource("/images/verify.png")));
        menu.add(verifyItem);

        // creates the tools menu
        menu = add(new JMenu("Tools"));

        JMenuItem xPathGeneratorItem = new JMenuItem("XPath Tool");
        verifyItem.setIcon(new ImageIcon(BasicGraphEditor.class.getResource("/images/verify.png")));

        xPathGeneratorItem.addActionListener(new ToolsActions.XPathAction(editor));
        xPathGeneratorItem.setIcon(new ImageIcon(BasicGraphEditor.class.getResource("/images/xpath.png")));

        JMenuItem jsonPathGeneratorItem = new JMenuItem("JSONPath Tool");
        jsonPathGeneratorItem.addActionListener(new ToolsActions.JSONPathAction(editor));
        jsonPathGeneratorItem.setIcon(new ImageIcon(BasicGraphEditor.class.getResource("/images/jsonpath.png")));

        menu.add(xPathGeneratorItem);
        menu.add(jsonPathGeneratorItem);



        // Creates the diagram menu
        menu = add(new JMenu(mxResources.get("layout")));

        menu.add(editor.graphLayout("verticalHierarchical", true));
        menu.add(editor.graphLayout("horizontalHierarchical", true));

        menu.addSeparator();

        menu.add(editor.graphLayout("verticalStack", false));
        menu.add(editor.graphLayout("horizontalStack", false));

        menu.addSeparator();

        menu.add(editor.graphLayout("verticalTree", true));
        menu.add(editor.graphLayout("horizontalTree", true));

        menu.addSeparator();

        menu.add(editor.graphLayout("organicLayout", true));
        menu.add(editor.graphLayout("circleLayout", true));

        // Creates the help menu
        menu = add(new JMenu(mxResources.get("help")));

        final JMenuItem item = menu.add(new JMenuItem(mxResources.get("aboutGraphEditor")));
        item.addActionListener((final ActionEvent event) -> {
            editor.about();
        });
    }
};
