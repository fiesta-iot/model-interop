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
// Created By : Nikolay Stanchev - ns17@it-innovation.soton.ac.uk
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.categories;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * a wrapper class for a Node object, which provides GUI functionality by extending the JComponent class
 * 
 * @author ns17
 */
public class NodeGUI {
    
    /**
     * the constant font used for rendering the node value
     */
    private final static Font FONT = new Font("serif", Font.BOLD, 14);
    
    /**
     * default border color
     */
    private final static Color BORDER_COLOR = new Color(0, 20, 51);
    
    /**
     * default color for node value
     */
    private final static Color DEFAULT_VALUE_COLOR = new Color(171, 171, 178);
    
    /**
     * the color of the expansion sign (plus or minus)
     */
    private final static Color SIGN_COLOR = new Color(84, 0, 11);
    
    /**
     * stores the min width/height of a node block
     */
    private final static int MIN_SIZE = 12;
    
    /**
     * a method to return the width of the node block
     * @param g the graphics used to render the node
     * @param node the node to get the width for
     * @return the width of the node text
     */
    public static int getWidth(Graphics g, Node<String> node){
        // get metrics for the text font
        FontMetrics metrics = g.getFontMetrics(FONT);
        return metrics.stringWidth(node.getValue()) + MIN_SIZE;
    }
    
    /**
     * a method to get the full width of the node block, including the expansion sign
     * @param g the graphics used to render the node
     * @param node the node to get the width for
     * @return the full width of the node block
     */
    public static int getFullWidth(Graphics g, Node<String> node){
        return getWidth(g, node) + getHeight(g);
    }
    
    /**
     * a method to return the height of the node block
     * @param g the graphics used to render the node
     * @return the width of the node text
     */
    public static int getHeight(Graphics g){
        // get metrics for the text font
        FontMetrics metrics = g.getFontMetrics(FONT);
        return metrics.getHeight() + MIN_SIZE;
    }
    
    /**
     * providing implementation for the drawing of a GUI node
     * @param g the graphics to use
     * @param node the node to draw
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public static void paintComponent(Graphics g, Node<String> node, int x, int y){
        NodeGUI.paintComponent(g, node, x, y, null);
    }
    
     
    /**
     * providing implementation for the drawing of a GUI node
     * @param g the graphics to use
     * @param node the node to draw
     * @param x the x coordinate
     * @param y the y coordinate
     * @param c the color to use for the value of expanded nodes
     */
    public static void paintComponent(Graphics g, Node<String> node, int x, int y, Color c){
        // get metrics for the text font
        int height = getHeight(g);
        int width = getWidth(g, node);
        
        g.setColor(BORDER_COLOR);        
        g.drawRect(x, y, width, height);
        g.drawRect(x + width, y, height, height);
        g.setFont(FONT);
        if (c != null){
            g.setColor(c);
        }
        else {
            g.setColor(DEFAULT_VALUE_COLOR);
        }
        g.drawString(node.getValue(), x+MIN_SIZE/2, y+height-(2*MIN_SIZE/3));
        
        g.setColor(SIGN_COLOR);
//        Graphics2D g2 = (Graphics2D) g;
//        g2.setStroke(new BasicStroke(4));
        int delta = height / 3;
        g.drawLine(x + width + delta, y + height/2 + 1, x + width + height - delta, y + height/2 + 1);
        if (!node.getChildren().isEmpty()){
            g.drawLine(x + width + height/2 + 1, y + delta, x + width + height/2 + 1, y + height - delta);
        }
    }
}
