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

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * a customised tree cell renderer, which renders the nodes with icons depending
 * on the type of the nodes - model or collection
 *
 * @author ns17
 */
public class CustomisedTreeCellRenderer implements TreeCellRenderer {

    /**
     * the default cell renderer used in the JTree
     */
    private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();

    /**
     * the image used to render leaf nodes, that is models
     */
    private final ImageIcon leafIcon;

    /**
     * the image used to render other nodes, that is collections
     */
    private final ImageIcon collectionIcon;

    /**
     * the image used to render the root node
     */
    private final ImageIcon rootIcon;
    
    private final String root;

    /**
     * created the customised renderer
     *
     * @param leafIcon relative path to the leaf icon 
     * @param collectionIcon relative path to the collection icon
     * @param rootIcon relative path to the root icon
     * @param rootValue the string value of the root element, e.g. "All available collections"
     */
    public CustomisedTreeCellRenderer(final String leafIcon, final String collectionIcon, final String rootIcon, final String rootValue) {
        // add images to the tree 
        InputStream stream = getClass().getResourceAsStream(leafIcon);
        ImageIcon icon;
        try {
            icon = new ImageIcon(ImageIO.read(stream));
        } catch (IOException ex) {
            icon = null;
        }
        this.leafIcon = icon;

        stream = getClass().getResourceAsStream(collectionIcon);
        try {
            icon = new ImageIcon(ImageIO.read(stream));
        } catch (IOException ex) {
            icon = null;
        }
        this.collectionIcon = icon;

        stream = getClass().getResourceAsStream(rootIcon);
        try {
            icon = new ImageIcon(ImageIO.read(stream));
        } catch (IOException ex) {
            icon = null;
        }
        this.rootIcon = icon;
        
        this.root = rootValue;
    }

    /**
     * this method handles the setting of the proper icon for a given node,
     * depending on the string value
     * @param tree
     * @param value
     * @param isSelected
     * @param isExpanded
     * @param isLeaf
     * @param row
     * @param isFocused
     * @return 
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded, boolean isLeaf, int row, boolean isFocused) {
        renderer.getTreeCellRendererComponent(tree, value, isSelected, isExpanded, isLeaf, row, isFocused);

        // check if the node is a model or a collection
        if (value.toString().endsWith(".xml")) {
            renderer.setIcon(leafIcon);
        } else if (value.toString().equals(root)) {
            renderer.setIcon(rootIcon);
        } else {
            renderer.setIcon(collectionIcon);
        }
        
        return renderer;
    }
}
