/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.categories;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions;

/**
 * a wrapper class for a Tree object, which provides GUI functionality by extending the JPanel class
 * @author ns17
 */
public class TreeGUI extends JPanel {

    /**
     * the distance between two levels of the tree
     */
    private final int deltaY = 90;

    /**
     * the minimum distance between two nodes on the same level, used when recalculating size of panel
     */
    private final int deltaX = 40;

    /**
     * the tree to wrap around
     */
    private final Tree<String> tree;

    /**
     * the map linking leaf category with model names
     */
    private final Map<String, List<String>> modelsMap;
    
    /**
     * the stack stores the expanded path of the categories
     */
    private final ArrayList<Node<String>> expandedPath = new ArrayList<>();
    
    /**
     * a reference to the actual action that initialised the TreeGUI component
     */
    private final FileActions.OpenFromWebAction actionReference;

    /**
     * the actual repository URL
     */
    private final String repoUrl;
    
    /**
     * a reference to the parent JDialog, so that the parent can be closed on click of a category
     */
    private JDialog dialogReference;
    
    /**
     * a private setter for the parent dialog reference
     * @param dialogReference the reference to the parent JDialog object
     */
    private void setDialog(JDialog dialog){
        this.dialogReference = dialog;
    }
    
    /**
     * a constructor for the wrapper object
     * @param tree the tree to wrap around
     * @param reference the reference to the action that initialised this TreeGUI component
     */

    /**
     * a constructor for the wrapper object
     * @param tree the tree to wrap around
     * @param modelsMap a map linking leaf nodes in the tree (category with no sub-categories) to the model names
     * @param repoUrl the URL of the loaded repository from which the categories tree is obtained
     * @param actionReference the reference to the actual action object
     */
    public TreeGUI(Tree<String> tree, Map<String, List<String>> modelsMap, String repoUrl, FileActions.OpenFromWebAction actionReference){
        super();

        this.tree = tree;
        this.actionReference = actionReference;
        this.repoUrl = repoUrl;
        this.modelsMap = modelsMap;
        
        expandedPath.add(this.tree.getRoot());

        addMouseListener(new NodeClickListener(this));
    }

    /**
     * a getter for the tree that this object wraps around
     * @return
     */
    public Tree<String> getTree(){
        return this.tree;
    }

    /**
     * this method is used to adjust the size of the panel, when expanding new nodes, the width of the panel is ensured to fit all the new children nodes
     * @param node the node that is expanded
     */
    private void adjustSize(Node<String> node){
        int width = 0;
        int height = (node.getLevel() + 2) * deltaY;
        width = node.getChildren().stream().map((current) -> NodeGUI.getFullWidth(this.getGraphics(), current) + deltaX).reduce(width, Integer::sum);
        this.setPreferredSize(new Dimension(width, height));

        this.revalidate();
        this.repaint();
    }

    /**
     * providing implementation for the drawing of a GUI tree
     * @param g
     */
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(rh);

        Dimension size = this.getSize();
        Color pathColor = new Color(1, 51, 8);

        // drawing the root node
        Node<String> root = this.expandedPath.get(0);
        int width = NodeGUI.getFullWidth(g2, root);
        int height = NodeGUI.getHeight(g2);
        int x = (int) size.width/2 - width/2;
        int y = 10;
        NodeGUI.paintComponent(g2, this.expandedPath.get(0), x, y, pathColor);
        int parentX = (int) x + width/2;
        int parentY = y + height;

        // drawing the expanded path
        Node<String> child;
        Node<String> current;
        List<Node<String>> path = (List<Node<String>>) this.expandedPath;
        List<Node<String>> children;
        int childrenCount;
        int blockWidth;
        Point parentPoint = new Point(x, y);
        int childX;
        int childY;
        for (int i = 0; i < path.size(); i++) {
            current = path.get(i);
            childrenCount = current.getChildren().size();
            blockWidth = (int) size.width/childrenCount;

            children = (List<Node<String>>) current.getChildren();
            for (int j = 0; j < childrenCount; j++){
                child = children.get(j);
                width = NodeGUI.getFullWidth(g2, child);
                height = NodeGUI.getHeight(g2);

                childX = j*blockWidth + blockWidth/2 - width/2;
                childY = y + child.getLevel()*deltaY;

                g2.setColor(new Color(24, 35, 53));
                g2.drawLine(parentX, parentY, childX+width/2, childY);

                if (i < path.size()-1 && path.get(i+1).getValue().equals(child.getValue())){
                    NodeGUI.paintComponent(g2, child, childX, childY, pathColor);
                    parentPoint = new Point(childX+width/2, childY + height);
                }
                else {
                    NodeGUI.paintComponent(g2, child, childX, childY);
                }
            }

            parentX = (int) parentPoint.getX();
            parentY = (int) parentPoint.getY();
        }
    }

    /**
     * a mouse click listener, which checks if the click is over a node from the tree
     */
    private class NodeClickListener extends MouseAdapter {

        /**
         * a reference to the gui tree component
         */
        private final TreeGUI tree;

        /**
         * constructor for the listener
         * @param tree reference to the gui tree component
         */
        private NodeClickListener(TreeGUI tree){
            this.tree = tree;
        }

        /**
         * implementation for the mouse clicked event
         * @param me the actual mouse event object
         */
        @Override
        public void mouseClicked(MouseEvent me) {
            Dimension size = this.tree.getSize();

            int pointX = me.getX();
            int pointY = me.getY();

            int width;
            int height;
            int x;
            int y = 10;

            Node<String> child;
            int testY;
            Node<String> toExpand = null;
            for (Node<String> current : this.tree.expandedPath) {
                int childrenCount = current.getChildren().size();
                int blockWidth = (int) size.width / childrenCount;

                for (int i = 0; i < childrenCount; i++) {
                    child = ((List<Node<String>>) current.getChildren()).get(i);
                    width = NodeGUI.getFullWidth(this.tree.getGraphics(), child);
                    height = NodeGUI.getHeight(this.tree.getGraphics());
                    x = i * blockWidth + blockWidth / 2 - width / 2;
                    testY = y + child.getLevel() * deltaY;
                    if (pointX >= x && pointX <= x + width && pointY >= testY && pointY <= testY + height) {
                        toExpand = child;
                        break;
                    }
                }
                if (toExpand != null) {
                    break;
                }
            }

            if (toExpand != null && toExpand.getChildren().size() > 0) {
                if (this.tree.expandedPath.contains(toExpand)){
                    int pathSize = this.tree.expandedPath.size();
                    while (!this.tree.expandedPath.get(pathSize - 1).getValue().equals(toExpand.getValue())){
                        this.tree.expandedPath.remove(pathSize - 1);
                        pathSize -= 1;
                    }
                    this.tree.expandedPath.remove(pathSize - 1);
                }
                else {
                    int lastIndex = this.tree.expandedPath.size() - 1;
                    while (this.tree.expandedPath.get(lastIndex).getLevel() >= toExpand.getLevel()) {
                        this.tree.expandedPath.remove(lastIndex);
                        lastIndex -= 1;
                    }
                    this.tree.expandedPath.add(toExpand);
                }
                this.tree.adjustSize(toExpand);
                this.tree.repaint();
            }
            else if (toExpand != null){
                List<String> toInclude = this.tree.modelsMap.get(toExpand.getValue());
                boolean check = true;
                if (toInclude == null || toInclude.isEmpty()){
                    check = JOptionPane.showConfirmDialog(tree, 
                            "The category either contains no models or doesn't exist anymore. Are you sure you want to continue?", 
                            "Empty category", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
                }
                
                if (check) {
                    boolean opened = this.tree.actionReference.initAvailableModels(this.tree.repoUrl, this.tree.modelsMap.get(toExpand.getValue()));
                    if (opened) {
                        if (this.tree.dialogReference != null) {
                            this.tree.dialogReference.dispose();
                        }
                    }
                }
            }
        }
    }

    public static JDialog initDialog(TreeGUI treeGUI){
        JDialog dialog = new JDialog();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setTitle("Categories Chooser");
        dialog.setSize(450,450);
        dialog.setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new JScrollPane(treeGUI), BorderLayout.CENTER);
        dialog.add(mainPanel);
        
        treeGUI.setDialog(dialog);
        
        return dialog;
    }
}
