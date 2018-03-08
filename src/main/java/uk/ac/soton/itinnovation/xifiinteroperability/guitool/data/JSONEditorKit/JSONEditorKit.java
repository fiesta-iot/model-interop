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

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONEditorKit;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONEditorKit.JSONDocument.GeneratorLeafElement;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.JSONPathGenerator.JSONPathGenerator;

/**
 * a custom JSONEditorKit, which extends the StyledEditorKit
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 *
 * @author Nikolay Stanchev
 */
public class JSONEditorKit extends StyledEditorKit {

    /**
     * custom view factory
     */
    private final ViewFactory defaultViewFactory = new JSONViewFactory();

    /**
     * a getter for the view factory used by the editor kit
     * @return the view factory
     */
    @Override
    public ViewFactory getViewFactory(){
        return defaultViewFactory;
    }

    /**
     * overriding the createDefaultDocument method to create a JSONDocument
     * @return the new JSONDocument
     */
    @Override
    public Document createDefaultDocument() {
        return new JSONDocument();
    }

    /**
     * a getter for the content type of the editor kit
     * @return "text/json" to represent a json document
     */
    @Override
    public String getContentType() {
        return "text/json";
    }

    /**
     * a boolean to represent if the XPath should be directly inserted to a text
     * field
     */
    private final boolean insertPath;

    /**
     * the field to insert the XPath into
     */
    private final JTextField insertField;

    /**
     * the parent dialog window
     */
    private final JDialog parentDialog;

    /**
     * constructor calls parent's constructor
     * @param insertPath boolean to know whether to directly insert the path
     * @param insertField the field to insert the path if required
     * @param parentDialog the parent JDialog
     */
    public JSONEditorKit(boolean insertPath, JTextField insertField, JDialog parentDialog){
        super();
        this.insertPath = insertPath;
        this.insertField = insertField;
        this.parentDialog = parentDialog;
    }

    /**
     * overriding the read method to use the custom JSONReader
     *
     * @param in Reader instance
     * @param doc text document
     * @param pos starting position
     * @throws IOException
     * @throws BadLocationException
     */
    @Override
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        BufferedReader br = new BufferedReader(in);
        String s = br.readLine();
        StringBuilder buff = new StringBuilder();
        while (s != null) {
            buff.append(s);
            s = br.readLine();
        }

        JSONReader.getInstance().read(new ByteArrayInputStream(buff.toString().getBytes()), doc, pos);
    }

    /**
     *  overriding the read method to use the custom JSONReader
     *
     * @param in InputStream instance
     * @param doc text document
     * @param pos starting position
     * @throws IOException
     * @throws BadLocationException
     */
    @Override
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
        JSONReader.getInstance().read(in, doc, pos);
    }

    /**
     * adding the custom mouseListener when installing the other listeners
     * @param c the editor pane
     */
    @Override
    public void install(JEditorPane c) {
        super.install(c);
        c.addMouseListener(mouseListener);
    }

    /**
     * removing the custom mouseListener when deinstalling the other listeners
     * @param c the editor pane
     */
    @Override
    public void deinstall(JEditorPane c) {
        c.removeMouseListener(mouseListener);
        super.deinstall(c);
    }

    /**
     * a custom mouse listener to handle clicks on key - value pairs
     */
    private final MouseListener mouseListener = new MouseAdapter(){
        @Override
        public void mouseClicked(MouseEvent e){
            JEditorPane src = (JEditorPane) e.getSource();

            int pos = src.viewToModel(e.getPoint());
            JSONLabelView deepestLabelView = (JSONLabelView) getDeepestView(pos, src, JSONLabelView.class);
            if (deepestLabelView != null) {
                Shape a = getAllocation(deepestLabelView, src);
                if (a != null) {
                    Rectangle r = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
                    if (r.contains(e.getPoint())) {
                        Element element = deepestLabelView.getElement();
                        String path = JSONPathGenerator.getJSONPath(((GeneratorLeafElement) element).getNode());
                        if (!insertPath){
                            int ans = JOptionPane.showConfirmDialog(null, "<html>JSONPath: <b><i>" + path + "</i></b><br><br>Would you like to copy this JSONPath ?</html>", "JSONPath", JOptionPane.YES_NO_OPTION);
                            if (ans == JOptionPane.YES_OPTION){
                                StringSelection stringSelection = new StringSelection(path);
                                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                                clpbrd.setContents(stringSelection, null);
                            }
                        }
                        else {
                            int check = JOptionPane.showConfirmDialog(null,
                                    "<html>The JSONPath expression - '<b><i>" + path + "</i></b>' will be inserted into the text field and "
                                            + "this dialog will be closed. Do you want to continue?", "User confirmation",
                                            JOptionPane.YES_NO_OPTION);
                            if (check == JOptionPane.YES_OPTION){
                                try{
                                    insertField.getDocument().insertString(insertField.getCaretPosition(), "content[" + path + "]",
                                            null);
                                    parentDialog.dispose();
                                }
                                catch(BadLocationException ex){}
                            }
                        }
                    }
                }
            }
        }
    };

    /**
     * a getter for the deepest view on click
     * @param pos position
     * @param src the editor pane
     * @param c the class of the view we are searching for
     * @return
     */
    public View getDeepestView(int pos, JEditorPane src, Class c) {
        try {
            View rootView = src.getUI().getRootView(src);

            while (rootView != null && !c.isInstance(rootView)) {
                int i = rootView.getViewIndex(pos, Position.Bias.Forward);
                rootView = rootView.getView(i);
            }

            View deepestView = (View) c.cast(rootView);
            while (rootView != null && c.isInstance(rootView)) {
                deepestView = (View) c.cast(rootView);
                int i = rootView.getViewIndex(pos, Position.Bias.Forward);
                rootView = rootView.getView(i);
            }

            return deepestView;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * a getter for the shape of the view so that we can check if a click is in this area
     * @param v the view
     * @param edit the editor pane
     * @return
     */
    protected static Shape getAllocation(View v, JEditorPane edit) {
        Insets ins=edit.getInsets();
        View vParent=v.getParent();
        int x=ins.left;
        int y=ins.top;
        while(vParent!=null) {
            int i=vParent.getViewIndex(v.getStartOffset(), Position.Bias.Forward);
            Shape alloc=vParent.getChildAllocation(i, new Rectangle(0,0, Short.MAX_VALUE, Short.MAX_VALUE));
            x+=alloc.getBounds().x;
            y+=alloc.getBounds().y;

            vParent=vParent.getParent();
        }

        if (v instanceof BoxView) {
            int ind=v.getParent().getViewIndex(v.getStartOffset(), Position.Bias.Forward);
            Rectangle r2=v.getParent().getChildAllocation(ind, new Rectangle(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE)).getBounds();

            return new Rectangle(x,y, r2.width, r2.height);
        }

        return new Rectangle(x,y, (int)v.getPreferredSpan(View.X_AXIS), (int)v.getPreferredSpan(View.Y_AXIS));
    }
}
