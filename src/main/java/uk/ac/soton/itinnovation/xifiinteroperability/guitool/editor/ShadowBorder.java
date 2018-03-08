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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.Border;

/**
 * Border with a drop shadow.
 */
public final class ShadowBorder implements Border {

        /**
         * The set of insets.
         */
	private final transient Insets insets;

        /**
         * The shadow border shared by the ui elements.
         */
	public static final transient ShadowBorder SHAREDINSTANCE = new ShadowBorder();

        /**
         * Construct a new shadow border object.
         */
	private ShadowBorder() {
		insets = new Insets(0, 0, 2, 2);
	}

        /**
         * Getter for the component's insets.
         * @param comp The component to get the border insets of.
         * @return The set of insets.
         */
        @Override
	public Insets getBorderInsets(final Component comp) {
		return insets;
	}

        /**
         * Is this an opaque border?
         * @return false as this is a transparent shadow border.
         */
        @Override
	public boolean isBorderOpaque() {
		return false;
	}

        /**
         * Override the paint border implementation with shadow behaviour.
         * @param comp The component.
         * @param graphic The graphics object.
         * @param xPos The position of the border on x-axis
         * @param yPos The position of the border on y-axis.
         * @param width The width.
         * @param height The height.
         */
        @Override
	public void paintBorder(final Component comp, final Graphics graphic, final int xPos,
                final int yPos, final int width, final int height) {
		// choose which colors we want to use
		Color backgroundColour = comp.getBackground();

		if (comp.getParent() != null) {
			backgroundColour = comp.getParent().getBackground();
		}

		if (backgroundColour != null) {
			final Color mid = backgroundColour.darker();
			final Color edge = average(mid, backgroundColour);

			graphic.setColor(backgroundColour);
			graphic.drawLine(0, height - 2, width, height - 2);
			graphic.drawLine(0, height - 1, width, height - 1);
			graphic.drawLine(width - 2, 0, width - 2, height);
			graphic.drawLine(width - 1, 0, width - 1, height);

			// draw the drop-shadow
			graphic.setColor(mid);
			graphic.drawLine(1, height - 2, width - 2, height - 2);
			graphic.drawLine(width - 2, 1, width - 2, height - 2);

			graphic.setColor(edge);
			graphic.drawLine(2, height - 1, width - 2, height - 1);
			graphic.drawLine(width - 1, 2, width - 1, height - 2);
		}
	}

        /**
         * Create an colour based on two colour inputs.
         * @param colour1 The first colour.
         * @param colour2 The second colour.
         * @return  The averaged colour.
         */
	private static Color average(final Color colour1, final Color colour2) {
		final int red = colour1.getRed() + (colour2.getRed() - colour1.getRed()) / 2;
		final int green = colour1.getGreen() + (colour2.getGreen() - colour1.getGreen()) / 2;
		final int blue = colour1.getBlue() + (colour2.getBlue() - colour1.getBlue()) / 2;
		return new Color(red, green, blue);
	}

        /**
         * Getter for the shared instance of the border.
         * @return The shared border instance.
         */
	public static ShadowBorder getSharedInstance() {
		return SHAREDINSTANCE;
	}
}
