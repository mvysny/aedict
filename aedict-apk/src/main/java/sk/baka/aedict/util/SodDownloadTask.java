/**
 *     Aedict - an EDICT browser for Android
 Copyright (C) 2009 Martin Vysny
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sk.baka.aedict.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import sk.baka.aedict.dict.AbstractDownloadTask;
import sk.baka.autils.MiscUtils;

/**
 * Downloads SOD images.
 * 
 * @author Martin Vysny
 */
public class SodDownloadTask extends AbstractDownloadTask {

	/**
	 * Creates new dictionary downloader.
	 * 
	 * @param source
	 *            download the dictionary files from here. A gzipped SOD binary
	 *            file is expected. Please see {@link SodLoader} for details on
	 *            the file format.
	 * @param targetDir
	 *            unzip the files here
	 * @param dictName
	 *            the dictionary name.
	 * @param expectedSize
	 *            the expected file size of unpacked dictionary.
	 */
	public SodDownloadTask(URL source, String targetDir, String dictName, long expectedSize) {
		super(source, targetDir, dictName, expectedSize);
	}

	@Override
	protected void copy(InputStream in) throws IOException {
		// we have to ungzip the input stream
		final InputStream gzipped = new GZIPInputStream(in);
		final OutputStream out = new FileOutputStream(SodLoader.SDCARD_LOCATION);
		try {
			copy(0L, -1, gzipped, out);
		} finally {
			MiscUtils.closeQuietly(out);
		}
	}

}
