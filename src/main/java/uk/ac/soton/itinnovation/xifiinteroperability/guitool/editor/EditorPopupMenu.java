/*
Copyright (c) 2001-2014, JGraph Ltd
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the JGraph nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL JGRAPH BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor;

import com.mxgraph.util.mxResources;
import javax.swing.JPopupMenu;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions.Delete;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.PopUpMenuActions.CopyURLAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.PopUpMenuActions.HistoryAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.PopUpMenuActions.ZoomInAction;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.PopUpMenuActions.ZoomOutAction;

/**
 * Small pop up menu in the pattern editor GUI. Contains a small number of
 * menu options corresponding to editor actions as found in the EditorActions
 * class.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class EditorPopupMenu extends JPopupMenu {

    /**
     * The parent editor of this pop up menu.
     */
    private final transient BasicGraphEditor editor;

    /**
     * Retrieve the editor reference from this class.
     * @return The parent GUI.
     */
    public final BasicGraphEditor getEditor() {
        return this.editor;
    }

    /**
     * Construct a new instance of the pop up menu.
     * @param newEditor The basic editor context.
     */
    public EditorPopupMenu(final BasicGraphEditor newEditor) {
        super();
            this.editor = newEditor;
            boolean selected = !editor.getSystemGraph().getGraph()
                            .isSelectionEmpty();

            selected = selected || !editor.getBehaviourGraph().getGraph()
                            .isSelectionEmpty();

            add(editor.bind(mxResources.get("undo"), new HistoryAction(true, editor),
                            "/images/undo.gif"));

            addSeparator();

            add(editor.bind(mxResources.get("zoomIn") , new ZoomInAction()));
            add(editor.bind(mxResources.get("zoomOut") , new ZoomOutAction()));

            addSeparator();

            add(
                editor.bind(mxResources.get("delete"), /*mxGraphActions.getDeleteAction()*/ new Delete(editor), "/images/delete.gif")).setEnabled(selected);

            addSeparator();

            add(editor.bind("Copy URL" , new CopyURLAction()));

    }

}
