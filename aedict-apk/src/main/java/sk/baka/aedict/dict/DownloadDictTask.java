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

package sk.baka.aedict.dict;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sk.baka.autils.MiscUtils;

/**
 * Downloads an EDICT/KANJIDIC dictionary.
 * 
 * @author Martin Vysny
 */
public class DownloadDictTask extends AbstractDownloadTask {
	/**
	 * Creates new dictionary downloader.
	 * 
	 * @param source
	 *            download the dictionary files from here. A zipped Lucene index
	 *            file is expected.
	 * @param targetDir
	 *            unzip the files here
	 * @param dictName
	 *            the dictionary name.
	 * @param expectedSize
	 *            the expected file size of unpacked dictionary.
	 */
	public DownloadDictTask(URL source, String targetDir, String dictName, long expectedSize) {
		super(source, targetDir, dictName, expectedSize);
	}

	@Override
	protected void copy(final InputStream in) throws IOException {
		final ZipInputStream zip = new ZipInputStream(in);
		long downloaded = 0;
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			final OutputStream out = new FileOutputStream(targetDir + "/" + entry.getName());
			try {
				downloaded = copy(downloaded, entry.getSize(), zip, out);
			} finally {
				MiscUtils.closeQuietly(out);
			}
			zip.closeEntry();
		}
	}
}
