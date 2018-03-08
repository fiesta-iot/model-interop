/*******************************************************************************
 * Created by Stanislav Lapitsky
 *
 * Reference to original source code http://java-sl.com/xml_editor_kit.html
 ******************************************************************************/

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
// Modified By : Nikolay Stanchev - ns17@it-innovation.soton.ac.uk
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.XMLEditorKit;

import javax.swing.text.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.AbstractGraphElement;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ArchitectureNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.ConstantData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.DataModel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.GraphNode;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.forms.ComponentForm;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.InterfaceData;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.tables.XMLSpecificationPanel;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.DataModelState;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.GUIdentifier;
import uk.ac.soton.itinnovation.xifiinteroperability.modelframework.specification.XMLStateMachine;

public class XMLEditorKit extends StyledEditorKit {
    ViewFactory defaultFactory = new XMLViewFactory();

    /**
     * reference to the xmlPanel
     */
    private final XMLSpecificationPanel xmlPanel;

    /**
     * a boolean representing the state of the editor - enabled or disabled editing
     */
    private boolean editingMode;

    /**
     * a method to check if editing is allowed
     * @return True if editing is allowed, false otherwise
     */
    public final boolean editingAllowed(){
        return editingMode;
    }

    /**
     * a method to toggle between editing modes
     */
    public final void toggleEditingMode(){
        editingMode = !editingMode;
    }

    /**
     * a boolean which represents if the XML data has been changed by the user
     */
    private boolean changed;

    /**
     * a method to check if the XML data is changed
     * @return True if the pattern has been changed
     */
    public final boolean isChanged(){
        return changed;
    }

    /**
     * a method to reset the changed variable
     */
    public final void resetChanged(){
        changed = false;
    }

    /**
     * a boolean to represent if changes were updated
     */
    private boolean saved;

    /**
     * a method to check if the changes were updated
     * @return True if changes were updated and False otherwise
     */
    public final boolean changesSaved(){
        return saved;
    }

    /**
     * setting the saved variable
     */
    public final void resetSaved(){
        this.saved = true;
    }

    /**
     * the first state before starting deletion
     */
    private DataModelState firstState;

    /**
     * the last state before deletion
     */
    private DataModelState lastState;

    /**
     * resets the first data model state
     */
    public void resetFirstState(){
        this.firstState = null;
    }

    public XMLEditorKit(XMLSpecificationPanel xmlPanel, boolean editingMode){
        super();
        this.xmlPanel = xmlPanel;
        this.editingMode = editingMode;
        this.changed = false;
        this.saved = true;
        this.firstState = null;
    }

    @Override
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }

    @Override
    public Document createDefaultDocument() {
        return new XMLDocument();
    }

    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        BufferedReader br=new BufferedReader(in);
        String s=br.readLine();
        StringBuilder buff=new StringBuilder();
        while (s!=null) {
            buff.append(s);
            s=br.readLine();
        }

        int p=getInsertPosition(pos, doc);
        XMLReader.getInstance().read(new ByteArrayInputStream(buff.toString().getBytes()), doc, p);
    }

    @Override
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
        int p=getInsertPosition(pos, doc);
        XMLReader.getInstance().read(in, doc, p);
    }
    @Override
    public void write(OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException {
        int[] sel=new int[2];
        sel[0]=pos;
        sel[1]=pos+len;
        correctSelectionBounds(sel, doc);
        pos=sel[0];
        len=sel[1]-pos;
        super.write(out, doc, pos, len);
    }
    @Override
    public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
        int[] sel=new int[2];
        sel[0]=pos;
        sel[1]=pos+len;
        correctSelectionBounds(sel, doc);
        pos=sel[0];
        len=sel[1]-pos - 1;
        super.write(out, doc, pos, len);
    }

    public static void correctSelectionBounds(int[] selection, Document d) {
        if (d instanceof XMLDocument && d.getLength()>0) {
            XMLDocument doc=(XMLDocument)d;
            int start=selection[0];
            Element root=doc.getDefaultRootElement();
            int i=root.getElementIndex(start);
            while (i>=0 && root.getElement(i).getName().equals(XMLDocument.TAG_ELEMENT)) {
                root=root.getElement(i);
                i=root.getElementIndex(start);
            }

            Element startTag=root;

            int end=selection[0];
            root=doc.getDefaultRootElement();
            i=root.getElementIndex(end);
            while (i>=0 && root.getElement(i).getName().equals(XMLDocument.TAG_ELEMENT)) {
                root=root.getElement(i);
                i=root.getElementIndex(end);
            }

            Element endTag=root;
            Element commonParent=startTag;
            while (commonParent!=null &&
                    !(commonParent.getStartOffset()<=endTag.getStartOffset() &&
                     commonParent.getEndOffset()>=endTag.getEndOffset()) ) {
                commonParent=commonParent.getParentElement();
            }

            if (commonParent!=null) {
                selection[0]=commonParent.getStartOffset();
                selection[1]=commonParent.getEndOffset();
            }
        }
    }

    protected int getInsertPosition(int pos, Document d) {
        if (d instanceof XMLDocument && d.getLength()>0) {
            XMLDocument doc=(XMLDocument)d;
            Element root=doc.getDefaultRootElement();
            int i=root.getElementIndex(pos);
            while (i>=0 && root.getElement(i).getName().equals(XMLDocument.TAG_ELEMENT)) {
                root=root.getElement(i);
                i=root.getElementIndex(pos);
            }

            while (root.getElementCount()<3) {
                root=root.getParentElement();
            }
            return root.getElement(0).getEndOffset();
        }

        return pos;
    }

    // the mouse listener, which handles mouse clicks over elements of the xml tree
    protected MouseListener clickListener=new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            JEditorPane src=(JEditorPane)e.getSource();

            int pos=src.viewToModel(e.getPoint());

            if (editingAllowed()){
                // checking for plain text view click, only if editing is allowed
                PlainTextView deepestPlainTextView = (PlainTextView) getDeepestView(pos, src, PlainTextView.class);
                if (deepestPlainTextView != null){
                    Shape a = getAllocation(deepestPlainTextView, src);
                    if (a != null){
                        Rectangle r=a instanceof Rectangle ? (Rectangle)a : a.getBounds();
                        if (r.contains(e.getPoint())){
                            int start = deepestPlainTextView.getStartOffset();
                            int end = deepestPlainTextView.getEndOffset();
                            String oldValue = deepestPlainTextView.getText(start, end).toString().trim();
                            String newValue = (String) JOptionPane.showInputDialog(xmlPanel,
                                    "Please type a value to replace the chosen one",
                                    "Editting", JOptionPane.PLAIN_MESSAGE,
                                    null, null, oldValue);

                            if (newValue == null)
                                return;

                            if (!newValue.equals(oldValue)){
                                // retreving the state before editing
                                DataModelState state = xmlPanel.getDataModel().getState();
                                if (firstState == null) {
                                    firstState = state;
                                    lastState = state;
                                }
                                else {
                                    xmlPanel.getDataModel().updateState(lastState);
                                }

                                try {
                                    if (validateData(oldValue, newValue, deepestPlainTextView)) {
                                        XMLDocument doc = (XMLDocument) deepestPlainTextView.getDocument();
                                        doc.remove(start, end - start);
                                        if (doc.getText(start - 1, 1).equals("\n")) {
                                            doc.insertString(start, newValue + "\n", XMLDocument.PLAIN_ATTRIBUTES);
                                        } else {
                                            doc.insertString(start, "\n" + newValue + "\n", XMLDocument.PLAIN_ATTRIBUTES);
                                        }
                                        changed = true;
                                        saved = false;
                                    }
                                }
                                catch (BadLocationException ex){}

                                lastState = xmlPanel.getDataModel().getState();
                                // returning to the old data model state after the deletion
                                xmlPanel.getDataModel().updateState(firstState);
                            }

                            return;
                        }
                    }
                }

                 // checking for TagNameView, only if editing is allowed
                TagNameView deepestTagNameView = (TagNameView) getDeepestView(pos, src, TagNameView.class);
                if (deepestTagNameView != null){
                    final String chosenTag;
                    try {
                        String nodeName = deepestTagNameView.getDocument().getText(deepestTagNameView.getStartOffset() - 1, deepestTagNameView.getEndOffset() - deepestTagNameView.getStartOffset() + 1);
                        if (!(nodeName.equals("<state") || nodeName.equals("<component") || nodeName.equals("<data") || nodeName.equals("<patterndata")
                                || nodeName.equals("<interface") || nodeName.equals("<transition")
                                || nodeName.equals("<architecture") || nodeName.equals("<behaviour")
                                || nodeName.equals("<address") || nodeName.equals("<label"))) {
                            return;
                        }
                        else {
                            chosenTag = deepestTagNameView.getDocument().getText(deepestTagNameView.getStartOffset(), deepestTagNameView.getEndOffset()-deepestTagNameView.getStartOffset())
;                        }
                    } catch (BadLocationException ex) {
                        return;
                    }
                    Shape a = getAllocation(deepestTagNameView, src);
                    Rectangle r = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
                    if (r.contains(e.getPoint())) {
                        try {
                            switch (chosenTag) {
                                case "state":
                                    {
                                        int start = deepestTagNameView.getStartOffset() + 13;
                                        int end = start;
                                        while (!deepestTagNameView.getDocument().getText(end, 8).equals("</label>")){
                                            end += 1;
                                        }
                                        String stateLabel = deepestTagNameView.getDocument().getText(start, end-start);
                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to delete state '" + stateLabel + "' ? "
                                                + "All transitions related to it will also be deleted.", "Delete confirmation", JOptionPane.OK_CANCEL_OPTION);
                                        if (check == JOptionPane.OK_OPTION){
                                            // retreving the data model state before deletion
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            xmlPanel.getDataModel().deleteNode(xmlPanel.getDataModel().getNodeByLabel(stateLabel).getUIIdentifier());

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old data model state after the deletion
                                            xmlPanel.getDataModel().updateState(firstState);
                                            changed = true;
                                            saved = false;
                                        }       break;
                                    }

                                case "component":
                                    {
                                        int start = deepestTagNameView.getStartOffset() + 14;
                                        int end = start;
                                        while (!deepestTagNameView.getDocument().getText(end, 5).equals("</id>")){
                                            end += 1;
                                        }
                                        String componentLabel = deepestTagNameView.getDocument().getText(start, end-start);
                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to delete component '" + componentLabel + "' ? "
                                                + "All information related to it will also be deleted.", "Delete confirmation", JOptionPane.OK_CANCEL_OPTION);
                                        if (check == JOptionPane.OK_OPTION){
                                            // retreving the state before deletion
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            xmlPanel.getDataModel().deleteNode(xmlPanel.getDataModel().getComponentByLabel(componentLabel).getUIIdentifier());

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old state after the deletion
                                            xmlPanel.getDataModel().updateState(firstState);
                                            changed = true;
                                            saved = false;
                                        }       break;
                                    }

                                case "data":
                                    {
                                        int start = deepestTagNameView.getStartOffset() + 11;
                                        int end = start;
                                        while(!deepestTagNameView.getDocument().getText(end, 7).equals("</name>")){
                                            end += 1;
                                        }
                                        String patternDataLabel = deepestTagNameView.getDocument().getText(start, end-start);
                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to delete pattern data with name '" + patternDataLabel + "' ? ",
                                                "Delete confirmation", JOptionPane.OK_CANCEL_OPTION);
                                        if (check == JOptionPane.OK_OPTION){
                                            // retreving the state before deletion
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            List<ConstantData> patternData = xmlPanel.getDataModel().getStartNode().getConstantData();
                                            ConstantData toRemove = null;
                                            for(ConstantData data : patternData){
                                                if (data.getFieldName().equals(patternDataLabel)){
                                                    toRemove = data;
                                                    break;
                                                }
                                            }
                                            if (toRemove != null){
                                                patternData.remove(toRemove);
                                                changed = true;
                                                saved = false;
                                            }

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old state after the deletion
                                            xmlPanel.getDataModel().updateState(firstState);
                                        }       break;
                                    }

                                case "interface":
                                    {
                                        int start = deepestTagNameView.getStartOffset() + 14;
                                        int end = start;
                                        while(!deepestTagNameView.getDocument().getText(end, 5).equals("</id>")){
                                            end += 1;
                                        }
                                        String interfaceId = deepestTagNameView.getDocument().getText(start, end - start);

                                        View componentView = deepestTagNameView.getParent().getParent().getParent().getView(1).getView(1);
                                        String componentId = componentView.getDocument().getText(componentView.getStartOffset(), componentView.getEndOffset()-componentView.getStartOffset());

                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to delete interface with id '" + interfaceId + "' from component "
                                                + "with id '" + componentId + "' ? ",
                                                "Delete confirmation", JOptionPane.YES_NO_OPTION);
                                        if (check == JOptionPane.YES_OPTION) {
                                            // retreving the state before deletion
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            ArchitectureNode archNode = (ArchitectureNode) xmlPanel.getDataModel().getComponentByLabel(componentId);
                                            archNode.removeInterfaceData(interfaceId);

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old state after the deletion
                                            xmlPanel.getDataModel().updateState(firstState);
                                            changed = true;
                                            saved = false;
                                        }       break;
                                    }

                                case "transition":
                                    {
                                        int start = deepestTagNameView.getStartOffset() + 15;
                                        int end = start;
                                        while(!deepestTagNameView.getDocument().getText(end, 5).equals("</to>")){
                                            end += 1;
                                        }
                                        String toLabel = deepestTagNameView.getDocument().getText(start, end - start);

                                        View fromView = deepestTagNameView.getParent().getParent().getParent().getView(1).getView(1);
                                        String fromLabel = fromView.getDocument().getText((fromView.getStartOffset()), fromView.getEndOffset()-fromView.getStartOffset());

                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to delete transition from state '" + fromLabel + "' to state '"
                                                + toLabel + "' ? ",
                                                "Delete confirmation", JOptionPane.YES_NO_OPTION);
                                        if (check == JOptionPane.YES_OPTION) {
                                            // retreving the state before deletion
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            GraphNode fromNode = (GraphNode) xmlPanel.getDataModel().getNodeByLabel(fromLabel);
                                            fromNode.deleteTransitionByLabel(toLabel);

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old state after the deletion
                                            xmlPanel.getDataModel().updateState(firstState);
                                            changed = true;
                                            saved = false;
                                        }       break;
                                    }

                                case "patterndata":
                                    {
                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to append new pattern data ?",
                                                "Append confirmation", JOptionPane.YES_NO_OPTION);
                                        if (check == JOptionPane.YES_OPTION){
                                            // retreving the state before appending
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            String id = (String) JOptionPane.showInputDialog(xmlPanel, "Please choose an id for the new pattern data.",
                                                    "Appending new pattern data", JOptionPane.PLAIN_MESSAGE);
                                            if (id != null && !id.equals("")){
                                                GraphNode startNode = xmlPanel.getDataModel().getStartNode();
                                                if (startNode != null){
                                                    List<ConstantData> patternData = startNode.getConstantData();
                                                    boolean idExists = false;
                                                    for (ConstantData data: patternData){
                                                        if (data.getFieldName().equalsIgnoreCase(id)){
                                                            idExists = true;
                                                            break;
                                                        }
                                                    }
                                                    if (idExists){
                                                        JOptionPane.showMessageDialog(xmlPanel, "Pattern data with this id already exists.",
                                                                "Warning", JOptionPane.WARNING_MESSAGE);
                                                    }
                                                    else {
                                                        String value = (String) JOptionPane.showInputDialog(xmlPanel, "Please choose a value for the new pattern data.",
                                                                "Appending new pattern data", JOptionPane.PLAIN_MESSAGE);
                                                        if (value != null && !value.equals("")){
                                                            startNode.addConstantData(id, value);
                                                            changed = true;
                                                            saved = false;
                                                        }
                                                    }
                                                }
                                                else {
                                                    JOptionPane.showMessageDialog(xmlPanel, "There is no start or triggerstart node created in the graph.",
                                                            "Error", JOptionPane.ERROR_MESSAGE);
                                                }
                                            }

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old state after appending
                                            xmlPanel.getDataModel().updateState(firstState);
                                        }       break;
                                    }

                                case "architecture":
                                    {
                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to append a new architecure component ?",
                                                "Append confirmation", JOptionPane.YES_NO_OPTION);
                                        if (check == JOptionPane.YES_OPTION){
                                             // retreving the state before appending
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            String id = (String) JOptionPane.showInputDialog(xmlPanel, "Please choose an id for the new component.",
                                                    "Appending architecure component", JOptionPane.PLAIN_MESSAGE);
                                            if (id != null && !id.equals("")){
                                                id = id.replaceAll("\\s+", "_");
                                                if (xmlPanel.getDataModel().archIdentExist(id)){
                                                    JOptionPane.showMessageDialog(xmlPanel, "Architecture component with this id already exists.",
                                                                "Warning", JOptionPane.WARNING_MESSAGE);
                                                }
                                                else {
                                                    String[] types = {"Interface", "Client"};
                                                    String type = (String) JOptionPane.showInputDialog(xmlPanel,
                                                            "Please choose the type of the new component.", "Appending architecture component",
                                                            JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
                                                    if (type != null){
                                                        if (type.equals("Interface")){
                                                            ArchitectureNode arch = new ArchitectureNode(GUIdentifier.setArchID(id), id, XMLStateMachine.INTERFACE_LABEL, id);
                                                            // adding null interface data so that the component is identified as interface
                                                            arch.addInterfaceData("null", "null", "http");
                                                            xmlPanel.getDataModel().addArchNode(arch);
                                                        }
                                                        else {
                                                            xmlPanel.getDataModel().addNode(id, id, DataModel.CLIENT);
                                                        }
                                                        changed = true;
                                                        saved = false;
                                                    }
                                                }
                                            }

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old state after appending
                                            xmlPanel.getDataModel().updateState(firstState);
                                        }       break;
                                    }

                               case "behaviour":
                                    {
                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to append a new state node ?",
                                                "Append confirmation", JOptionPane.YES_NO_OPTION);
                                        if (check == JOptionPane.YES_OPTION){
                                            // retreving the state before appending
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            String id = (String) JOptionPane.showInputDialog(xmlPanel, "Please choose a label for the new state node.",
                                                    "Appending state node", JOptionPane.PLAIN_MESSAGE);
                                            if (id != null && !id.equals("")){
                                                id = id.replaceAll("\\s+", "_");
                                                if (xmlPanel.getDataModel().graphIdentExist(id)){
                                                    JOptionPane.showMessageDialog(xmlPanel, "State node with this label already exists.",
                                                                "Warning", JOptionPane.WARNING_MESSAGE);
                                                }
                                                else {
                                                    String[] types = {"Start", "Triggerstart", "Trigger", "Loop", "Dataloop", "Normal", "End"};
                                                    String type = (String) JOptionPane.showInputDialog(xmlPanel,
                                                            "Please choose the type of the new state node.", "State node",
                                                            JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
                                                    if (type != null){
                                                        switch(type){
                                                            case "Start":
                                                            case "Triggerstart":
                                                                if (xmlPanel.getDataModel().containsStart()){
                                                                    JOptionPane.showMessageDialog(xmlPanel,
                                                                            "There is already one start node in the graph.", "Error",
                                                                            JOptionPane.ERROR_MESSAGE);
                                                                }
                                                                else {
                                                                    xmlPanel.getDataModel().addNode(id, id, type.toLowerCase());
                                                                    changed = true;
                                                                    saved = false;
                                                                }
                                                                break;
                                                            case "Trigger":
                                                            case "Loop":
                                                            case "DataLoop":
                                                            case "Normal":
                                                            case "End":
                                                                xmlPanel.getDataModel().addNode(id, id, type.toLowerCase());
                                                                changed = true;
                                                                saved = false;
                                                            default:
                                                                break;
                                                        }
                                                    }
                                                }
                                            }

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old state after appending
                                            xmlPanel.getDataModel().updateState(firstState);
                                        }       break;
                                    }

                                case "address":
                                    {
                                        View componentView = deepestTagNameView.getParent().getParent().getParent().getView(1).getView(1);
                                        String componentId = componentView.getDocument().getText(componentView.getStartOffset(), componentView.getEndOffset() - componentView.getStartOffset());

                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to append a new rest interface url to component"
                                                + " with id '" + componentId + "' ?",
                                                "Append confirmation", JOptionPane.YES_NO_OPTION);
                                        if (check == JOptionPane.YES_OPTION){
                                            // retreving the state before appending
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            ArchitectureNode archNode = (ArchitectureNode) xmlPanel.getDataModel().getComponentByLabel(componentId);
                                            String restId = (String) JOptionPane.showInputDialog(xmlPanel,
                                                    "Please choose an id for the new rest url.", "Appending rest url",
                                                    JOptionPane.PLAIN_MESSAGE);
                                            if (restId != null){
                                                boolean idExists = false;
                                                for(InterfaceData data: archNode.getData()){
                                                    if (data.getRestID().equalsIgnoreCase(restId)){
                                                        idExists = true;
                                                        break;
                                                    }
                                                }

                                                if (idExists){
                                                    JOptionPane.showMessageDialog(xmlPanel, "Rest interface url with this id already exists.",
                                                                    "Warning", JOptionPane.WARNING_MESSAGE);
                                                }
                                                else {
                                                    String restUrl = (String) JOptionPane.showInputDialog(xmlPanel,
                                                            "Please choose a url for the new rest interface.", "Appending rest url",
                                                            JOptionPane.PLAIN_MESSAGE);
                                                    if (restUrl != null){
                                                        String[] protocols = {"HTTP", "COAP", "MQTT", "SOAP"};
                                                        String protocol = (String) JOptionPane.showInputDialog(xmlPanel,
                                                                "Please choose the protocol of the new rest interface url.", "Rest interface url",
                                                                JOptionPane.PLAIN_MESSAGE, null, protocols, protocols[0]);
                                                        if (protocol != null){
                                                            archNode.addInterfaceData(restId, restUrl, protocol);
                                                            changed = true;
                                                            saved = false;
                                                        }
                                                    }
                                                }
                                            }

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old state after the deletion
                                            xmlPanel.getDataModel().updateState(firstState);
                                        }       break;
                                    }

                               case "label":
                                    {
                                        View fromView = deepestTagNameView.getParent().getParent().getView(1);
                                        String fromLabel = fromView.getDocument().getText((fromView.getStartOffset()), fromView.getEndOffset()-fromView.getStartOffset());

                                        View typeView = deepestTagNameView.getParent().getParent().getParent().getView(2).getView(1);
                                        String type = typeView.getDocument().getText(typeView.getStartOffset(), typeView.getEndOffset()-typeView.getStartOffset());

                                        int check = JOptionPane.showConfirmDialog(xmlPanel, "Are you sure you want to append a transition from state node"
                                                + " with label '" + fromLabel + "' ?",
                                                "Append confirmation", JOptionPane.YES_NO_OPTION);
                                        if (check == JOptionPane.YES_OPTION){
                                            // retreving the state before appending
                                            DataModelState state = xmlPanel.getDataModel().getState();
                                            if (firstState == null){
                                                firstState = state;
                                                lastState = state;
                                            }
                                            else {
                                                xmlPanel.getDataModel().updateState(lastState);
                                            }

                                            GraphNode fromNode = (GraphNode) xmlPanel.getDataModel().getNodeByLabel(fromLabel);
                                            switch(type.toLowerCase()){
                                                case "start":
                                                case "triggerstart":
                                                case "trigger":
                                                    if (fromNode.getNumberTransitions() > 0){
                                                        JOptionPane.showMessageDialog(xmlPanel,
                                                                "You cannot have more than one transitions for states of type start, trigger and triggerstart.",
                                                                "Transition error", JOptionPane.ERROR_MESSAGE);
                                                        break;
                                                    }

                                                case "normal":
                                                case "loop":
                                                    Object[] arrayNodes = xmlPanel.getDataModel().getGraphElements().toArray();
                                                    GraphNode toNode = (GraphNode) JOptionPane.showInputDialog(xmlPanel,
                                                            "Please choose the label of the target state for the new transition", "Creating transition",
                                                            JOptionPane.PLAIN_MESSAGE, null, arrayNodes, arrayNodes[0]);
                                                    if (toNode == null){
                                                        break;
                                                    }

                                                    if (toNode.getType().equals("start") || toNode.getType().equals("triggerstart")){
                                                        JOptionPane.showMessageDialog(xmlPanel,
                                                                "You cannot have a transition with a start or triggerstart node as a target.",
                                                                "Transition error", JOptionPane.ERROR_MESSAGE);
                                                        break;
                                                    }

                                                    xmlPanel.getDataModel().addConnection(fromNode.getUIIdentifier() + "_" + toNode.getUIIdentifier(), fromNode.getUIIdentifier(), toNode.getUIIdentifier());
                                                    changed = true;
                                                    saved = false;

                                                    break;
                                                case "dataloop":
                                                    Object[] arrayNodes2 = xmlPanel.getDataModel().getGraphElements().toArray();
                                                    GraphNode toNode2 = (GraphNode) JOptionPane.showInputDialog(xmlPanel,
                                                            "Please choose the label of the target state for the new transition", "Creating transition",
                                                            JOptionPane.PLAIN_MESSAGE, null, arrayNodes2, arrayNodes2[0]);
                                                    if (toNode2 == null){
                                                        break;
                                                    }

                                                    if (toNode2.getType().equals("start") || toNode2.getType().equals("triggerstart")){
                                                        JOptionPane.showMessageDialog(xmlPanel,
                                                                "You cannot have a transition with a start or triggerstart node as a target.",
                                                                "Transition error", JOptionPane.ERROR_MESSAGE);
                                                        break;
                                                    }

                                                    xmlPanel.getDataModel().addConnection(fromNode.getUIIdentifier() + "_" + toNode2.getUIIdentifier(), fromNode.getUIIdentifier(), toNode2.getUIIdentifier());
                                                    changed = true;
                                                    saved = false;

                                                    break;
                                                case "end":
                                                    JOptionPane.showMessageDialog(xmlPanel, "You cannot append transitions starting from an end node.",
                                                            "Tansition error", JOptionPane.ERROR_MESSAGE);
                                                    break;
                                                default:
                                                    break;
                                            }

                                            xmlPanel.displayXMLSpecification();
                                            lastState = xmlPanel.getDataModel().getState();
                                            // returning to the old state after the deletion
                                            xmlPanel.getDataModel().updateState(firstState);
                                        }

                                        break;
                                    }

                                default:
                                    break;
                            }

                            return;
                        }
                        catch(BadLocationException ex){
                            return;
                        }
                    }
                }
            }

            // checking for a click over an expanding tag
            TagView deepest = (TagView) getDeepestView(pos, src, TagView.class);
            if (deepest!=null && !deepest.isStartTag()) {
                Shape a=getAllocation(deepest, src);
                if (a!=null) {
                    Rectangle r=a instanceof Rectangle ? (Rectangle)a : a.getBounds();
                    if (!deepest.isSecondTag()){
                        r.x += TagView.AREA_X_SHIFT;
                    }
                    r.y+=TagView.AREA_SHIFT/4;
                    r.width=TagView.AREA_SHIFT;
                    r.height=TagView.AREA_SHIFT;

                    if (r.contains(e.getPoint())) {
                        deepest.setExpanded(!deepest.isExpanded());

                        XMLDocument doc= (XMLDocument)src.getDocument();
                        try {
                            pos++;
                            doc.insertString(pos, "\n", new SimpleAttributeSet());
                            doc.remove(pos,1);
                        } catch (BadLocationException e1) {
                            JOptionPane.showMessageDialog(xmlPanel,
                                    "Something went wrong while processing your XML pattern.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    };

    Cursor oldCursor;
    MouseMotionListener lstMoveCollapse=new MouseMotionAdapter() {
        @Override
        public void mouseMoved(MouseEvent e) {
            JEditorPane src=(JEditorPane)e.getSource();
            if (oldCursor==null) {
                oldCursor=src.getCursor();
            }

            int pos=src.viewToModel(e.getPoint());

            if (editingAllowed()){
                // checking for plain text view click, only if editing is allowed
                PlainTextView deepestPlainTextView = (PlainTextView) getDeepestView(pos, src, PlainTextView.class);
                if (deepestPlainTextView != null){
                    Shape a = getAllocation(deepestPlainTextView, src);
                    if (a != null) {
                        Rectangle r = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
                        if (r.contains(e.getPoint())) {
                            src.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }

                // checking for TagNameView, only if editing is allowed
                TagNameView deepestTagNameView = (TagNameView) getDeepestView(pos, src, TagNameView.class);
                if (deepestTagNameView != null){
                    try {
                        String nodeName = deepestTagNameView.getDocument().getText(deepestTagNameView.getStartOffset() - 1, deepestTagNameView.getEndOffset() - deepestTagNameView.getStartOffset() + 1);
                        if (!(nodeName.equals("<state") || nodeName.equals("<component") || nodeName.equals("<data")
                                || nodeName.equals("<interface") || nodeName.equals("<transition")
                                || nodeName.equals("<patterndata") || nodeName.equals("<architecture")
                                || nodeName.equals("<behaviour") || nodeName.equals("<address")
                                || nodeName.equals("<label"))) {
                            return;
                        }
                    } catch (BadLocationException ex) {
                        return;
                    }
                    Shape a = getAllocation(deepestTagNameView, src);
                    Rectangle r = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
                    if (r.contains(e.getPoint())) {
                        src.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
            }

            TagView deepest = (TagView) getDeepestView(pos, src, TagView.class);
            if (deepest!=null && !deepest.isStartTag()) {
                Shape a=getAllocation(deepest, src);
                if (a!=null) {
                    Rectangle r=a instanceof Rectangle ? (Rectangle)a : a.getBounds();
                    if (!deepest.isSecondTag()){
                        r.x += TagView.AREA_X_SHIFT;
                    }
                    r.y+=TagView.AREA_SHIFT/4;
                    r.width=TagView.AREA_SHIFT;
                    r.height=TagView.AREA_SHIFT;

                    if (r.contains(e.getPoint())) {
                        src.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
            }

            src.setCursor(oldCursor);
        }
    };

    @Override
    public void install(JEditorPane c) {
        super.install(c);
        c.addMouseListener(clickListener);
        c.addMouseMotionListener(lstMoveCollapse);
    }

    @Override
    public void deinstall(JEditorPane c) {
        c.removeMouseListener(clickListener);
        c.removeMouseMotionListener(lstMoveCollapse);
        super.deinstall(c);
    }

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

    /**
     * a method to get the deepest view of a given type on click
     * @param pos the position of the click
     * @param src the source
     * @param c the class of the view we are searching for
     * @return
     */
    public View getDeepestView(int pos, JEditorPane src, Class c){
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
        }
        catch (Exception ex) {
            return null;
        }
    }

    /**
     * a method to validate the new value in the view
     * @param value the value replacing the old data
     * @param changedView the view that is being edited
     * @return True if validation is successful and False otherwise
     * @throws BadLocationException
     */
    private boolean validateData(String oldValue, String value, View changedView) throws BadLocationException{
        if (oldValue.equalsIgnoreCase(value)){
            return true;
        }

        View parentTagView = changedView.getParent().getParent();
        String parentContent = parentTagView.getDocument().getText(parentTagView.getStartOffset(), parentTagView.getEndOffset()-parentTagView.getStartOffset());

        StringBuilder builder = new StringBuilder();
        int index = 1;
        while (parentContent.charAt(index) != ">".charAt(0)){
            builder.append(parentContent.charAt(index));
            index += 1;
        }

        String parentTag = builder.toString();

        switch (parentTag){
            // changing label identifier
            case "label":
                if (xmlPanel.getDataModel().graphIdentExist(value)){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "There already exists a state node with this label.",
                            "Renaming error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;

            // changing the target of a link transition
            case "with":
                if (!xmlPanel.getDataModel().archIdentExist(value)){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "A component node with this label doesn't exist.",
                            "Link transition error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                View componentParentView = parentTagView.getParent().getParent().getView(1).getView(1);
                String from = componentParentView.getDocument().getText(componentParentView.getStartOffset(),
                        componentParentView.getEndOffset()-componentParentView.getStartOffset());
                if (from.equalsIgnoreCase(value)){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "You cannot have a link transition with the same source and target node.",
                            "Link transition error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                break;

            // changing the target of a transition
            case "to":
                if (!xmlPanel.getDataModel().graphIdentExist(value)){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "A state node with this label doesn't exist.",
                            "Transition error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                View stateView = parentTagView.getParent().getParent().getView(1).getView(1);
                String fromLabel = stateView.getDocument().getText(stateView.getStartOffset(), stateView.getEndOffset()-stateView.getStartOffset());
                if (fromLabel.equalsIgnoreCase(value)){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "You cannot have a transition with the same source and target node.",
                            "Transition error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (xmlPanel.getDataModel().getNodeByLabel(value).getType().equalsIgnoreCase("start") ||
                        xmlPanel.getDataModel().getNodeByLabel(value).getType().equalsIgnoreCase("triggerstart")){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "You cannot have a transition leading to a start or a trigger start node.",
                            "Transition error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                break;

            // changing the guard description
            case "param":
                if (value.equalsIgnoreCase("timeout")){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "You are not allowed to create a timeout guard through XML editing.",
                            "Timeout guard error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else if (value.equalsIgnoreCase("index")){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "You are not allowed to create an index guard through XML editing",
                            "Counter guard error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else if (value.equalsIgnoreCase("response-time")){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "You are not allowed to create a response-time guard through XML editing",
                            "Response-time guard error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                break;

            // changing guard value
            case "value":
                View paramView = parentTagView.getParent().getView(1).getView(1);
                String param = paramView.getDocument().getText(paramView.getStartOffset(), paramView.getEndOffset()- paramView.getStartOffset());
                View functionTypeView = parentTagView.getParent().getView(0);
                String functionType = functionTypeView.getDocument().getText(functionTypeView.getStartOffset(), functionTypeView.getEndOffset() - functionTypeView.getStartOffset());
                if (param.equalsIgnoreCase("timeout")){
                    try {
                        Long timeout = Long.parseLong(value);
                        if (timeout <= 0){
                            JOptionPane.showMessageDialog(xmlPanel, "The value for a timeout guard must be a positive integer.",
                                    "Timeout transition error", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                    catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(xmlPanel, "The value for a timeout guard must be an integer representing the time in milliseconds",
                                "Timeout guard error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }

                else if (param.equalsIgnoreCase("index")){
                    /**
                     * Removed check - counter can be evaluated to a prior state value.
                     */
//                    try {
//                        Integer counter  = Integer.parseInt(value);
//                        if (counter <= 0){
//                            JOptionPane.showMessageDialog(xmlPanel, "The value for an index guard must be a positive integer.",
//                                    "Counter transition error", JOptionPane.ERROR_MESSAGE);
//                            return false;
//                        }
//                    }
//                    catch (NumberFormatException ex){
//                        JOptionPane.showMessageDialog(xmlPanel, "The value for an index guard must be an integer representing the number of iterations",
//                                "Counter guard error", JOptionPane.ERROR_MESSAGE);
//                        return false;
//                    }
                }

                else if (param.equalsIgnoreCase("response-time")){
                    try {
                        Long responseTime = Long.parseLong(value);
                        if (responseTime <= 0){
                            JOptionPane.showMessageDialog(xmlPanel, "The value for a response-time guard must be a positive integer.",
                                    "Transition error", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                    catch (NumberFormatException ex){
                        JOptionPane.showMessageDialog(xmlPanel, "The value for a response-time guard must be an integer representing the time in milliseconds.",
                                "Transition error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                else if (functionType.equalsIgnoreCase("<regex>")){
                    try {
                        Pattern.compile(value);
                    }
                    catch (PatternSyntaxException ex){
                        JOptionPane.showMessageDialog(xmlPanel, "The guard value is not a valid regular expression.",
                                "Regex error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }

                break;

            case "id":
                View upperView = parentTagView.getParent().getView(0);
                String label = upperView.getDocument().getText(upperView.getStartOffset(), upperView.getEndOffset() - upperView.getStartOffset());
                if (label.substring(1, label.length()-1).equalsIgnoreCase("component")){
                    // component id check
                    if (xmlPanel.getDataModel().archIdentExist(value)){
                        JOptionPane.showMessageDialog(xmlPanel,
                            "There already exists a component node with this label.",
                            "Renaming error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                else if (label.substring(1, label.length()-1).equalsIgnoreCase("interface")){
                    // interface id check
                    View componentView = parentTagView.getParent().getParent().getView(1).getView(1);
                    String componentLabel = componentView.getDocument().getText(componentView.getStartOffset(), componentView.getEndOffset() - componentView.getStartOffset());
                    AbstractGraphElement archNode = xmlPanel.getDataModel().getComponentByLabel(componentLabel);
                    if (archNode == null) {
                        return false;
                    }

                    ArchitectureNode node = (ArchitectureNode) archNode;
                    if (!node.getData().stream().noneMatch((data) -> (data.getRestID().equalsIgnoreCase(value)))) {
                        JOptionPane.showMessageDialog(xmlPanel,
                            "The component already has an interface with this id.",
                            "Renaming error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                else {
                    // parent of id is either component or interface
                    return false;
                }

                break;


            case "method":
                if (!(value.equalsIgnoreCase("GET") || value.equalsIgnoreCase("POST") || value.equalsIgnoreCase("PUBLISH")
                        || value.equalsIgnoreCase("PUT") || value.equalsIgnoreCase("DELETE"))){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "The method of a message transition can only be one "
                            + "of the following: GET, POST, PUT and DELETE.",
                            "Method error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                break;

            case "success":
                if(!(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))){
                    JOptionPane.showMessageDialog(xmlPanel,
                            "The success attribute of an end node can only be true or false.",
                            "End state error", JOptionPane.ERROR_MESSAGE);

                    return false;
                }

                break;

            case "protocol":
                // changing of protocol through XML editing is not allowed
                JOptionPane.showMessageDialog(xmlPanel,
                            "You are not allowed to change the interface protocol by editing the XML pattern.",
                            "Protocol error", JOptionPane.ERROR_MESSAGE);
                return false;

            case "type":
                View upperTypeView = parentTagView.getParent().getView(0);
                String typeLabel = upperTypeView.getDocument().getText(upperTypeView.getStartOffset(), upperTypeView.getEndOffset()-upperTypeView.getStartOffset());
                if (typeLabel.substring(1, typeLabel.length()-1).equalsIgnoreCase("message")){
                    if (!(value.equalsIgnoreCase("XML") || value.equalsIgnoreCase("JSON")
                            || value.equalsIgnoreCase("OTHER"))) {
                        JOptionPane.showMessageDialog(xmlPanel,
                            "The type of data can only be one of the following: XML, JSON and OTHER.",
                            "Data type error", JOptionPane.ERROR_MESSAGE);

                        return false;
                    }
                }
                else if (typeLabel.substring(1, typeLabel.length()-1).equalsIgnoreCase("state")){
                    // changing state type through XML editing is not allowed
                    JOptionPane.showMessageDialog(xmlPanel,
                            "You are not allowed to change the type of a state node by editing the XML pattern.",
                            "State type error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                else {
                    // parent of type is either message or state
                    return false;
                }

                break;

            case "url":
                View upperMessageView = parentTagView.getParent().getView(0);
                String messageLabel = upperMessageView.getDocument().getText(upperMessageView.getStartOffset(), upperMessageView.getEndOffset()-upperMessageView.getStartOffset());
                if (messageLabel.substring(1, messageLabel.length()-1).equalsIgnoreCase("message")){
                    List<String> componentUrls = xmlPanel.getDataModel().getRestUrls();
                    if(!componentUrls.contains(value)){
                        // message url pointers must be existing ones
                        JOptionPane.showMessageDialog(xmlPanel,
                                "The new url pointer doesn't exist.",
                                "URL pointer error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                else if (messageLabel.substring(1, messageLabel.length()-1).equalsIgnoreCase("interface")){
                    String newUrl = value;
                    boolean hasPortNumber = Pattern.matches(ComponentForm.REGEX, newUrl);
                    if (!hasPortNumber) {
                        JOptionPane.showMessageDialog(xmlPanel,
                                "The url of the interface is not valid. Keep in mind that a port number must be specified. For instance - 'http://127.0.0.1:8080/'",
                                "Invalid URL",
                                JOptionPane.WARNING_MESSAGE);
                        return false;
                    } else {
                        Pattern p = Pattern.compile(ComponentForm.PORT_REGEX);
                        Matcher m = p.matcher(newUrl);
                        m.find();
                        String portStr = newUrl.substring(m.start() + 1, m.end());
                        try {
                            Integer port = Integer.parseInt(portStr);
                            if (port > 65536) {
                                JOptionPane.showMessageDialog(xmlPanel,
                                        "The port number in the url must be between 0 and 65536.",
                                        "Port number exceeding limit", JOptionPane.WARNING_MESSAGE);
                                return false;
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(xmlPanel,
                                    "The specified port number in the url is invalid!",
                                    "Invalid port number", JOptionPane.WARNING_MESSAGE);
                            return false;
                        }
                    }
                }

                break;

            case "name":
                GraphNode node = xmlPanel.getDataModel().getStartNode();
                if (node == null){
                    return false;
                }
                else {
                    for(ConstantData data: node.getConstantData()){
                        if (data.getFieldName().equalsIgnoreCase(value)){
                            JOptionPane.showMessageDialog(xmlPanel,
                                "The new pattern data id is already used.",
                                "Pattern data error", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                }

                break;

            default:
                break;
        }

        return true;
    }
}
