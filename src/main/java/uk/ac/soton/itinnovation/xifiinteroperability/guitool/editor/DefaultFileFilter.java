/*
 * Copyright (c) 2001-2005, Gaudenz Alder
 *
 * All rights reserved.
 *
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
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

import java.io.File;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

/**
 * Filter for use in a {@link JFileChooser}.
 */
public class DefaultFileFilter extends FileFilter {

	/**
	 * Extension of accepted files.
	 */
	private transient String ext;

	/**
	 * Description of accepted files.
	 */
	private final transient String desc;

	/**
	 * Constructs a new filter for the specified extension and descpription.
	 *
	 * @param extension
	 *            The extension to accept files with.
	 * @param description
	 *            The description of the file format.
	 */
	public DefaultFileFilter(final String extension, final String description) {
            super();
            ext = extension.toLowerCase(Locale.ENGLISH);
            desc = description;
	}

	/**
	 * Returns true if <code>file</code> is a directory or ends with
	 * {@link #ext}.
	 *
	 * @param file
	 *            The file to be checked.
	 * @return Returns true if the file is accepted.
	 */
	public boolean accept(final File file) {
		return file.isDirectory() || file.getName().toLowerCase(Locale.ENGLISH).endsWith(ext);
	}

	/**
	 * Returns the description for accepted files.
	 *
	 * @return Returns the description.
	 */
	public final String getDescription() {
		return desc;
	}

	/**
	 * Returns the extension for accepted files.
	 *
	 * @return Returns the extension.
	 */
	public final String getExtension() {
		return ext;
	}

	/**
	 * Sets the extension for accepted files.
	 *
	 * @param extension
	 *            The extension to set.
	 */
	public final void setExtension(final String extension) {
		this.ext = extension;
	}

	/**
	 * Utility file filter to accept all image formats supported by image io.
	 *
	 * @see ImageIO#getReaderFormatNames()
	 */
	public static class ImageFileFilter extends FileFilter {

		/**
		 * Holds the accepted file format extensions for images.
		 */
		private static final String[] IMAGEFORMATS = ImageIO.getReaderFormatNames();

		/**
		 * Description of the filter.
		 */
		private final transient String desc;

		/**
		 * Constructs a new file filter for all supported image formats using
		 * the specified description.
		 *
		 * @param description
		 *            The description to use for the file filter.
		 */
		public ImageFileFilter(final String description) {
                    super();
                    desc = description;
		}

		/**
		 * Returns true if the file is a directory or ends with a known image
		 * extension.
		 *
		 * @param file
		 *            The file to be checked.
		 * @return Returns true if the file is accepted.
		 */
		public final boolean accept(final File file) {
			if (file.isDirectory()) {
				return true;
			}

			final String filename = file.toString().toLowerCase();

			for (int j = 0; j < IMAGEFORMATS.length; j++) {
				if (filename.endsWith("." + IMAGEFORMATS[j].toLowerCase())) {
					return true;
				}
			}

			return false;
		}

		/**
		 * Returns the description.
		 *
		 * @return Returns the description.
		 */
		public final String getDescription() {
			return desc;
		}

	}

	/**
	 * Utility file filter to accept editor files, namely .xml and .xml.gz
	 * extensions.
	 *
	 * @see ImageIO#getReaderFormatNames()
	 */
	public static class EditorFileFilter extends FileFilter {

		/**
		 * Description of the File format.
		 */
		private final transient String desc;

		/**
		 * Constructs a new editor file filter using the specified description.
		 *
		 * @param description
		 *            The description to use for the filter.
		 */
		public EditorFileFilter(final String description) {
                    super();
                    desc = description;
		}

		/**
		 * Returns true if the file is a directory or has a .xml or .xml.gz
		 * extension.
		 *
                 * @param file The file to accept.
		 * @return Returns true if the file is accepted.
		 */
                @Override
		public final boolean accept(final File file) {
			if (file.isDirectory()) {
				return true;
			}

			final String filename = file.getName().toLowerCase();

			return filename.endsWith(".xml") || filename.endsWith(".xml.gz");
		}

		/**
		 * Returns the description.
		 *
		 * @return Returns the description.
		 */
                @Override
		public final String getDescription() {
			return desc;
		}

	}
}
