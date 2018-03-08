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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator.JSONPathGeneratorEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XPathGenerator.XPathGeneratorEditor;

/**
 * This is the content pop up menu displayed when right clicking on captured events' content
 * 
 * @author ns17
 */
public class ContentPopUpMenu extends JPopupMenu {
    
    private final JEditorPane parent;
    
    /**
     * constructs a ContentPopUpMenu
     * 
     * @param parent the editor pane that has this pop up menu 
     */
    public ContentPopUpMenu(JEditorPane parent) {
        super();
        
        this.parent = parent;
        JMenuItem item = new JMenuItem("Copy selection");
        item.addActionListener(new CopyAction());
        add(item);
        
        item = new JMenuItem("Open XPath/JSONPath generator");
        item.addActionListener(new OpenGeneratorAction());
        add(item);
    }
    
    /**
     * Inner class - Providing a copy action for the event's content
     */
    class CopyAction extends AbstractAction {

        /**
         * overriding the performed action
         * 
         * @param ae the actual action event
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            String selected = parent.getSelectedText();
            if (selected != null && !selected.isEmpty()){
                StringSelection selection = new StringSelection(selected);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        }
        
    }
    
    /**
     * Inner class - An action which opens the appropriate generator depending on the content type 
     */
    class OpenGeneratorAction extends AbstractAction {

        /**
         * overriding the performed action
         * 
         * @param ae the actual action event
         */
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (parent.getEditorKit().getContentType().contains("xml")){
                new XPathGeneratorEditor().initGUI(parent.getText());
            }
            else if (parent.getEditorKit().getContentType().contains("json")){
                new JSONPathGeneratorEditor().initGUI(parent.getText());
            }
        }
        
    }
    
}
