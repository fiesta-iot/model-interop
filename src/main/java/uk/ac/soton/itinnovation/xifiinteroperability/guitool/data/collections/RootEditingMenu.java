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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import static uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections.CollectionsBrowserForm.DEFAULT_COLLECTION;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.BasicGraphEditor;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions;

/**
 * a class, which represents a pop up menu used for editing the collections tree
 *
 * @author ns17
 */
public class RootEditingMenu extends TreeEditingMenu {

    /**
     * constructs the editing menu for the root node
     * @param editor the editor reference
     * @param browser the collections browser reference
     */
    public RootEditingMenu(BasicGraphEditor editor, CollectionsBrowserForm browser) {
        super(editor, browser);
        
        JMenuItem item;

        // an action to add a new collection in the tree
        item = new JMenuItem("Add new collection");
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String newCollection = (String) JOptionPane.showInputDialog(browser.getTree(), "Please enter the name of the new collection",
                        "New collection", JOptionPane.PLAIN_MESSAGE);
                if (newCollection == null || newCollection.isEmpty()) {
                    return;
                }

                if (newCollection.equalsIgnoreCase(DEFAULT_COLLECTION)) {
                    JOptionPane.showMessageDialog(browser.getTree(), "You cannot add a new default collection.", "Default collection adding", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                browser.addCollection(newCollection);
            }
        });
        item.setIcon(new ImageIcon(getClass().getResource("/images/new16.png")));
        add(item);

        // an action to run all tests in all colections
        item = new JMenuItem("Run all tests");
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                final int check = JOptionPane.showConfirmDialog(browser.getTree(),
                        "<html><p>Do you want all tests to run in execution mode without stopping for confirmation to continue ?</p>"
                        + "<ul><li>Click Yes to execute all tests with no interventions.</li>"
                        + "<li>Click No to execute the tests one by one with debuging capability.</li>"
                        + "<li>Click Cancel to stop the execution.</li><ul></html>",
                        "Test mode", JOptionPane.YES_NO_CANCEL_OPTION);
                if (check == JOptionPane.CANCEL_OPTION) {
                    return;
                }
                final boolean askUser = check == JOptionPane.NO_OPTION;

                final StringBuilder summaryReportTrace = new StringBuilder();
                summaryReportTrace.append("Interoperability tests - Summary report\n\n\n");

                // start a thread which goes through all collections and run all tests in each collection
                final Thread runThread = new Thread() {
                    @Override
                    public void run() {
                        TreeNode collection;
                        String toExecute;
                        int continueExecuting;
                        int counter = 0;
                        int failures = 0;
                        for (int i = 0; i < browser.getRoot().getChildCount(); i++) {
                            collection = browser.getRoot().getChildAt(i);
                            for (int j = 0; j < collection.getChildCount(); j++) {
                                toExecute = collection.getChildAt(j).toString();
                                setLastClicked(toExecute);
                                runAction.putValue("runMode", askUser);
                                runAction.actionPerformed(ae);

                                while (editor.isRunning()) {
                                    // wait until the test is finished before proceeding with the next one
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException ex) {
                                        // continue looping untill test has finished running
                                    }
                                }

                                counter += 1;
                                summaryReportTrace.append("Test number ").append(counter).append("\n");
                                summaryReportTrace.append("\tTest identifier - ").append(toExecute).append("\n");
                                summaryReportTrace.append("\tTest execution passed - ").append(editor.getStateMachine().getReport().getSuccess()).append("\n\n");
                                failures = editor.getStateMachine().getReport().getSuccess().equalsIgnoreCase("true") ? failures : failures + 1;

                                if (askUser && j != collection.getChildCount() - 1 && i != browser.getRoot().getChildCount() - 1) {
                                    continueExecuting = JOptionPane.showConfirmDialog(editor, "Do you want to continue executing the rest of the tests?",
                                            "Continue executing", JOptionPane.YES_NO_OPTION);
                                    if (continueExecuting == JOptionPane.NO_OPTION) {
                                        break;
                                    }
                                } else if (j == collection.getChildCount() - 1 && i == browser.getRoot().getChildCount() - 1) {
                                    if (failures == 0) {
                                        summaryReportTrace.append("Successful test execution\n");
                                        summaryReportTrace.append(counter).append(" tests were executed with no failures detected\n");
                                    } else {
                                        summaryReportTrace.append("Unsuccessful test execution\n");
                                        final String failureString = failures == 1 ? " failure" : " failures"; 
                                        summaryReportTrace.append(counter).append(" tests were executed with ").append(failures).append(failureString).append(" detected\n");
                                    }
                                    
                                    // don't reset the test data maps, we want to keep them in memory (e.g. similar to cache)
                                    // browser.getMultiTestsManager().resetMaps();

                                    final int check = JOptionPane.showConfirmDialog(editor, "The execution of the tests has been completed. Do you want to view the summary test report?",
                                            "Completed execution", JOptionPane.YES_NO_OPTION);
                                    editor.getCodePanel().getReportsPanel().addTabReport(summaryReportTrace.toString());
                                    if (check == JOptionPane.YES_OPTION) {
                                        final CardLayout sideLayout = (CardLayout) editor.getAttributePanel().getLayout();
                                        sideLayout.show(editor.getAttributePanel(), "empty");
                                        new EditorActions.ReportsAction(editor).actionPerformed(null);
                                    }
                                }
                            }
                        }
                    }
                };
                runThread.start();
            }
        });
        item.setIcon(new ImageIcon(getClass().getResource("/images/run16.png")));
        add(item);
    }

}
