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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * This is a customised JDialog, opened when downloading models to display the available models in a given repository
 * 
 * @author ns17
 */
public class ModelsTableDialog extends JDialog {
    
    /**
     * the panel containing the already added filters
     */
    private final JPanel filtersPanel;
    
    /**
     * the main panel in the dialog window
     */
    private final JPanel mainPanel;
    
    /**
     * the heading label
     */
    private final JLabel heading;
    
    /**
     * a set used to hold the applied filters, filters are added lowercase
     */
    private final Set<String> filters;
    
    /**
     * stores the original data in the table
     */
    private Object[][] data;
    
    /**
     * reference to the JTable object
     */
    private JTable table;
    
    /**
     * a constructor for the ModelsTableDialog, builds the UI layout
     */
    public ModelsTableDialog(){
        this.setTitle("Available models in the repository");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        mainPanel = new JPanel(new BorderLayout());
        
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.PAGE_AXIS));
        northPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        heading = new JLabel("0 applied filters");
        heading.setFont(new Font("Serif", Font.BOLD, 14));
        heading.setForeground(new Color(63, 98, 155));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        northPanel.add(heading);
        northPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        filtersPanel = new JPanel();
        filtersPanel.setLayout(new WrapLayout());
        northPanel.add(filtersPanel);
        northPanel.add(Box.createRigidArea(new Dimension(0, 10)));
       
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
        inputPanel.add(Box.createHorizontalGlue());
        inputPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        JTextField input = new JTextField();
        inputPanel.add(input);
        inputPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        JButton apply = new JButton("Apply filter");
        apply.addActionListener((ActionEvent ae) -> {
            String filter = input.getText();
            if (filter != null && !filter.isEmpty()){
                this.addFilter(filter);
            }
            input.setText("");
        });
        input.addKeyListener((new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    apply.doClick();
                }
            }
        }));
        inputPanel.add(apply);
        inputPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        inputPanel.add(Box.createHorizontalGlue());
        northPanel.add(inputPanel);
        northPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        mainPanel.add(northPanel, BorderLayout.NORTH);
        
        filters = new LinkedHashSet<>();
        
        this.add(mainPanel);
    }
    
    /**
     * sets the table displayed in the dialog, to be used once, before initialising the Dialog, (before calling setVisible)
     * @param table the table of models, that must already be given the data to display
     * @param data the data of the table
     */
    public void setTable(JTable table, Object[][] data){
        this.data = data;
        this.table = table;
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(null);
    }
    
    /**
     * a mutator method to add a new filter to the set of filters (the filter will be set to lower case before adding)
     * @param filter the filter to add
     */
    private void addFilter(String filter){
        if (filter == null || filter.isEmpty()){
            return;
        }
        
        if (filters.add(filter.toLowerCase()))
            adjustFilters();
    }
    
    /**
     * a mutator method to remove a filter from the set of applied filters (filter is set to lower case before trying to remove)
     * @param filter the filter to remove
     */
    public void removeFilter(String filter){
        if (filter == null || filter.isEmpty()){
            return;
        }
        
        if (filters.remove(filter.toLowerCase()))
            adjustFilters();
    }
    
    /**
     * utility method which adjusts the set of filter labels in the filters panel
     */
    private void adjustFilters(){
        filtersPanel.removeAll();
        filters.forEach((filter) -> {
            filtersPanel.add(new FilterLabel(filter, this));
        });
        filtersPanel.revalidate();
        filtersPanel.repaint();
        
        int filtersSize = filters.size();
        String adjustedHeading = filtersSize == 1 ? filtersSize + " applied filter" : filtersSize + " applied filters";
        heading.setText(adjustedHeading);
        
        // clear table
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setRowCount(0);
        
        // add only those rows that are somehow related to the filters, that is - include all filters
        boolean toExclude;
        for (Object[] row: data){
            toExclude = false;
            for(String filter: filters){
                if (!row[0].toString().toLowerCase().contains(filter) && !row[1].toString().toLowerCase().contains(filter)){
                    toExclude = true;
                    break;
                }
            }
            
            if (!toExclude){
                tableModel.addRow(row);
            }
        }
    }
}

class FilterLabel extends JLabel {
    
    /**
     * the default font used to render filter labels
     */
    private final Font font = new Font("Serif", Font.PLAIN, 13);
    
    /**
     * the size of the cross used to represent a remove filter button
     */
    private final int crossSize = 4;
    
    /**
     * constructs a valid FilterLabel
     * @param message the message in the label, in this case - the filter
     */
    public FilterLabel(String message, ModelsTableDialog parent) {
        super(" " + message + " ");  // initialise the JLabel
        
        // customize the FilterLabel
        this.setToolTipText("");
        this.setFont(font);
        this.setOpaque(true);
        this.setBackground(new Color(213, 230, 239));
        this.setForeground(new Color(40, 52, 91));
        this.setPreferredSize(new Dimension(this.getPreferredSize().width + 15, this.getPreferredSize().height + 2));
        
        // adding a mouse listener to remove filters on click over the cross button
        this.addMouseListener(new MouseAdapter(){
            /**
             * remove the filter on click
             * @param e the actual mouse event
             */
            @Override
            public void mouseClicked(MouseEvent e){
                int x = e.getX();
                int y = e.getY();
                Rectangle crossRect = new Rectangle(getWidth() - crossSize - 6, 0, crossSize + 3, crossSize + 3);
                if (crossRect.contains(x, y)){
                    parent.removeFilter(getText().trim().toLowerCase());
                }
            }
        });
    }
    
    /**
     * overriding the paintComponent method - provide custom drawing for the JLabel, which include a cross button for removing the filter
     * @param g the graphics to draw with
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(68, 2, 2).darker());
        Rectangle rect = g2.getClipBounds();
        int startX = (int) rect.getWidth() - this.crossSize - 3;
        int startY = (int) rect.getY() + 3;
        g2.drawLine(startX, startY, startX + crossSize, startY + crossSize);
        g2.drawLine(startX + crossSize, startY, startX, startY + crossSize);
    }
    
    /**
     * Override the getToolTipText method so that a tool tip is returned depending on the position of the mouse
     * @param e the mouse event
     * @return a tool tip if mouse is over the cross button and null otherwise (no tooltip)
     */
    @Override
    public String getToolTipText(MouseEvent e){
        int x = e.getX();
        int y = e.getY();
        Rectangle crossRect = new Rectangle(getWidth() - crossSize - 6, 0, crossSize + 3, crossSize + 3);
        if (crossRect.contains(x, y)){
            return "Click to remove the filter";
        }
        else {
            return null;
        }
    }
}
