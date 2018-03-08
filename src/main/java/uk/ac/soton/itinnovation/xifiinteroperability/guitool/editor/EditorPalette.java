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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.util.mxRectangle;

/**
 * The pallette component into which the shapes e.g. the state machine
 * circles are added, so that they can be dragged and dropped onto the
 * graph editing area.
 *
 * Project acknowledgements - developed in FIESTA (http://www.fiesta-iot.eu)
 * & XIFI (http://www.fi-xifi.eu)
 *
 * @author Paul Grace
 */
public class EditorPalette extends JPanel {

    /**
     * Label for the current selected entry in the pallete. Highlights
     * after select and drag.
     */
    private transient JLabel selectedEntry = null;

    /**
     * The single source to send events from this pallete.
     */
    private final transient mxEventSource eventSource = new mxEventSource(this);

    /**
     * Construct the pallette instance.
     */
    public EditorPalette() {
        super();
        setBackground(Color.WHITE);
        setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

        // Clears the current selection when the background is clicked
        addMouseListener(new MouseListener() {

            @Override
            public void mousePressed(final MouseEvent event) {
                    clearSelection();
            }

            @Override
            public void mouseClicked(final MouseEvent event) {
                // No implementation necessary
            }

            @Override
            public void mouseEntered(final MouseEvent event) {
                // No implementation necessary
            }

            @Override
            public void mouseExited(final MouseEvent event) {
                // No implementation necessary
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                // No implementation necessary
            }
        });

        // Shows a nice icon for drag and drop but doesn't import anything
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(final JComponent comp, final DataFlavor[] flavors) {
                    return true;
            }
        });
    }

    /**
     * Paint the pallete component.
     * @param grph Swing graphics element.
     */
    @Override
    public final void paintComponent(final Graphics grph) {
        super.paintComponent(grph);
    }

    /**
     * Clear the selected entry - typically on drop.
     */
    public final void clearSelection() {
            setSelectionEntry(null, null);
    }

    /**
     * Change the icon in pallete to illustrate it has been selected.
     * @param entry The selected label
     * @param trans The draggable entity.
     */
    public final void setSelectionEntry(final JLabel entry, final mxGraphTransferable trans) {
        final JLabel previous = selectedEntry;
        selectedEntry = entry;

        if (previous != null) {
                previous.setBorder(null);
                previous.setOpaque(false);
        }

        if (selectedEntry != null) {
                selectedEntry.setBorder(ShadowBorder.getSharedInstance());
                selectedEntry.setOpaque(true);
        }

        eventSource.fireEvent(new mxEventObject(mxEvent.SELECT, "entry",
                        selectedEntry, "transferable", trans, "previous", previous));
    }

    /**
     * Set the width of the palette.
     * @param width Requested width.
     */
    public final void setPreferredWidth(final int width) {
        final int cols = Math.max(1, width / 55);
        setPreferredSize(new Dimension(width,
                        (getComponentCount() * 55 / cols) + 30));
        revalidate();
    }

    /**
     * Add a new icon (template) to the pallete.
     * @param name The label of the icon type e.g. interface, client,
     * @param icon The icon image to use in the pallette.
     * @param style The string description of the style e.g. image
     * @param width The width of the icon in the pallete.
     * @param height The height of the icon in the pallete.
     * @param value The initial label value.
     */
    public final void addTemplate(final String name, final ImageIcon icon, final String style,
                    final int width, final int height, final Object value) {
            final mxCell cell = new mxCell(value, new mxGeometry(0, 0, width, height),
                            style);
            cell.setVertex(true);

            addTemplate(name, icon, cell);
    }

    /**
     * Add a new icon (template) to the pallete.
     * @param name The name of the template.
     * @param iconPattern The image icon.
     * @param cell The created graph cell.
     */
    private void addTemplate(final String name, final ImageIcon iconPattern, final mxCell cell) {
            final mxRectangle bounds = (mxGeometry) cell.getGeometry().clone();
            final mxGraphTransferable trans = new mxGraphTransferable(
                            new Object[] {cell}, bounds);

            ImageIcon icon = iconPattern;
            // Scales the image if it's too large for the library
            if (icon == null) {
                return;
            }
            if (icon.getIconWidth() > 32 || icon.getIconHeight() > 32) {
                icon = new ImageIcon(icon.getImage().getScaledInstance(32, 32,
                                0));
            }

            final JLabel entry = new JLabel(icon);
            entry.setPreferredSize(new Dimension(50, 50));
            entry.setBackground(EditorPalette.this.getBackground().brighter());
            entry.setFont(new Font(entry.getFont().getFamily(), 0, 10));

            entry.setVerticalTextPosition(JLabel.BOTTOM);
            entry.setHorizontalTextPosition(JLabel.CENTER);
            entry.setIconTextGap(0);

            entry.setToolTipText(name);
            entry.setText(name);

            entry.addMouseListener(new MouseListener() {

                @Override
                public void mousePressed(final MouseEvent event) {
                        setSelectionEntry(entry, trans);
                }

                @Override
                public void mouseClicked(final MouseEvent event) {
                    // No implementation necessary
                }

                @Override
                public void mouseEntered(final MouseEvent event) {
                    // No implementation necessary
                }

                @Override
                public void mouseExited(final MouseEvent event) {
                    // No implementation necessary
                }

                @Override
                public void mouseReleased(final MouseEvent event) {
                    // No implementation necessary
                    }

            });

            // Install the handler for dragging nodes into a graph
            final DragGestureListener dragGListener = new DragGestureListener() {
                    @Override
                    public void dragGestureRecognized(final DragGestureEvent event) {
                            event.startDrag(null, mxSwingConstants.EMPTY_IMAGE, new Point(),
                                                            trans, null);

                    }

            };

            final DragSource dragSource = new DragSource();
            dragSource.createDefaultDragGestureRecognizer(entry,
                            DnDConstants.ACTION_COPY, dragGListener);
            add(entry);
    }
}
