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
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;

/**
 * a class, which represents a pop up menu used for editing the collections tree
 *
 * @author ns17
 */
public class ModelEditingMenu extends TreeEditingMenu {

    /**
     * creates the menu for the models in the tree
     *
     * @param editor the editor reference
     * @param browser the collections browser reference
     */
    public ModelEditingMenu(BasicGraphEditor editor, CollectionsBrowserForm browser) {
        super(editor, browser);

        JMenuItem item;

        // an action to open an xml model
        item = new JMenuItem("Open this model");
        item.addActionListener(openAction);
        item.setIcon(new ImageIcon(getClass().getResource("/images/import-icon.png")));
        add(item);

        // an action to remove a previously opened model
        item = new JMenuItem("Remove this model");
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int check = JOptionPane.showConfirmDialog(browser.getTree(), "Are you sure you want to remove this model ?", "Model removal", JOptionPane.OK_CANCEL_OPTION);
                if (check != JOptionPane.OK_OPTION) {
                    return;
                }

                browser.removeModel(lastClick);
            }
        });
        item.setIcon(new ImageIcon(getClass().getResource("/images/cut16.png")));
        add(item);

        // an action to rename a model
        item = new JMenuItem("Rename this model");
        item.addActionListener(new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ae){
                String newModelName = (String) JOptionPane.showInputDialog(browser.getTree(), "Please specify the new name of the model:", 
                        "Renaming model", JOptionPane.OK_CANCEL_OPTION);
                if (newModelName == null){
                    return;
                }
                
                browser.renameModel(lastClick, newModelName.trim());
            }
        });
        item.setIcon(new ImageIcon(getClass().getResource("/images/copy16.png")));
        add(item);
        
        // an action to move a model to a different collection
        item = new JMenuItem("Move this model");
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String newCollection = (String) JOptionPane.showInputDialog(browser.getTree(), "Please specify into which collection you want to move the selected model",
                        "Moving model", JOptionPane.PLAIN_MESSAGE, null, browser.getCollectionsArray(), CollectionsBrowserForm.DEFAULT_COLLECTION);

                if (newCollection == null || newCollection.isEmpty()) {
                    return;
                }

                if (!browser.collectionExists(newCollection)) {
                    JOptionPane.showMessageDialog(browser.getTree(), "The specified collection doesn't exist.", "Collection error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                browser.moveModel(lastClick, newCollection);
            }
        });
        item.setIcon(new ImageIcon(getClass().getResource("/images/import16.png")));
        add(item);

        item = new JMenuItem("Run test");
        item.addActionListener(runAction);
        item.setIcon(new ImageIcon(getClass().getResource("/images/run16.png")));
        add(item);
    }

}
