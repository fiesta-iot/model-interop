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
// Created By : Nikolay Stanchev
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions;

/**
 * a class, which represents a pop up menu used for editing the collections tree
 *
 * @author ns17
 */
abstract class TreeEditingMenu extends JPopupMenu {
    
    /**
     * holds a reference to the last clicked node
     */
    protected String lastClick;
    
    /**
     * a setter for the last clicked attribute
     * @param clicked 
     */
    public void setLastClicked(String clicked) {
        this.lastClick = clicked;
    }
    
    // an action which opens a model from a collection
    protected final AbstractAction openAction;
    
    // an action which runs a model in a collection
    protected final AbstractAction runAction;

    /**
     * constructor for the popup menu, there can be three types of menus, one
     * for editing the root, one for editing a collection, one for editing a
     * model
     *
     * @param editor reference to the editor
     */
    public TreeEditingMenu(BasicGraphEditor editor, CollectionsBrowserForm browser) {
        super(); // called for clarity reasons
        
        // an action which opens a model from a collection
        openAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (browser.getCurrentModel() != null && browser.getOpenedModels().containsKey(browser.getCurrentModel())) {
                    browser.getOpenedModels().get(browser.getCurrentModel()).setXML(editor.getDataModel().getGraphXML());
                    browser.getOpenedModels().get(browser.getCurrentModel()).setModified(editor.isModified());
                }
                
                FileActions.OpenAction.openFromCollection(editor, browser.getOpenedModels().get(lastClick).getXml(), 
                        browser.getOpenedModels().get(lastClick).getPath(), browser.getOpenedModels().get(lastClick).getModified());
                browser.setCurrentModel(lastClick);
            }
        };

        // an action which runs a model in a collection
        runAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!lastClick.equals(browser.getCurrentModel())) {
                    openAction.actionPerformed(ae);
                }

                // check the runMode value of the action, which shows whether to ask the user for execution mode or use execution mode without asking
                final Boolean askUser = (Boolean) getValue("runMode");

                if (askUser == null) {
                    new EditorActions.ExecuteAction(editor).actionPerformed(ae);
                } else {
                    new EditorActions.ExecuteAction(editor, askUser).actionPerformed(ae);
                }
            }
        };
    }
}
