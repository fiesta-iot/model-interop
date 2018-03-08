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

import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.EditorActions;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.FileActions;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxKeyboardHandler;
import uk.ac.soton.itinnovation.xifiinteroperability.guitool.editor.actions.PopUpMenuActions.HistoryAction;

/**
 * @author Administrator
 *
 */
public class EditorKeyboardHandler extends mxKeyboardHandler {
	/**
	 * Add the keyboard handler to each graph component. Hence, created
         * for both the behaviour and system graphs.
	 * @param graphComponent The graph component reference to add keyboard to.
	 */
	public EditorKeyboardHandler(final mxGraphComponent graphComponent) {
		super(graphComponent);
	}

	/**
         * Get the keyboard input map - actions assigned to keys.
	 * @return JTree's input map.
         * @param condition The input condition
	 */
        @Override
	protected final InputMap getInputMap(final int condition) {
            final InputMap map = super.getInputMap(condition);

            if (condition == JComponent.WHEN_FOCUSED && map != null) {
                map.put(KeyStroke.getKeyStroke("control S"), "save");
                map.put(KeyStroke.getKeyStroke("control shift S"), "saveAs");
                map.put(KeyStroke.getKeyStroke("control N"), "new");
                map.put(KeyStroke.getKeyStroke("control O"), "open");

                map.put(KeyStroke.getKeyStroke("control Z"), "undo");
                map.put(KeyStroke.getKeyStroke("control Y"), "redo");
                map.put(KeyStroke.getKeyStroke("DELETE"), "delete");
                map.put(KeyStroke.getKeyStroke("control X"), "cut");
                map.put(KeyStroke.getKeyStroke("control C"), "copyComponent");
                map.put(KeyStroke.getKeyStroke("control V"), "pasteComponent");
            }

            return map;
	}

	/**
	 * Return the mapping between JTree's input map and JGraph's actions.
         * @return The created action map with tool actions.
	 */
        @Override
	protected final ActionMap createActionMap() {
            final ActionMap map = super.createActionMap();

            map.put("save", new FileActions.SaveAction(null, false));
            map.put("saveAs", new FileActions.SaveAction(null, true));
            map.put("new", new FileActions.NewAction(null));
            map.put("open", new FileActions.OpenAction(null));
            map.put("undo", new HistoryAction(true, null));
            map.put("redo", new HistoryAction(false, null));
            map.put("delete", new EditorActions.Delete(null));
            map.put("cut", new EditorActions.Delete(null));
            map.put("copyComponent", new EditorActions.CopyComponentAction(null));
            map.put("pasteComponent", new EditorActions.PasteComponentAction(null));

            return map;
	}

}
