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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import sk.baka.autils.AbstractTask;
import sk.baka.autils.MiscUtils;
import sk.baka.autils.Progress;
import android.util.Log;

/**
 * Downloads a file in the background.
 * 
 * @author Martin Vysny
 */
public abstract class AbstractDownloadTask extends AbstractTask<Void, Void> {
	private final URL source;
	/**
	 * unzip the files here.
	 */
	protected final String targetDir;
	/**
	 * the dictionary name.
	 */
	protected final String dictName;
	/**
	 * the expected file size of unpacked dictionary.
	 */
	protected final long expectedSize;

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
	public AbstractDownloadTask(final URL source, final String targetDir, final String dictName, final long expectedSize) {
		this.source = source;
		this.targetDir = targetDir;
		this.dictName = dictName;
		this.expectedSize = expectedSize;
	}

	/**
	 * Checks if the edict is downloaded and indexed correctly.
	 * 
	 * @param dict
	 *            the dictionary type. The default path will be checked.
	 * @return true if everything is okay, false if not
	 */
	public static boolean isComplete(final DictTypeEnum dict) {
		return isComplete(dict.getDefaultDictionaryPath());
	}

	/**
	 * Checks if the edict is downloaded and indexed correctly.
	 * 
	 * @param indexDir
	 *            the directory where the index files are expected to be
	 *            located.
	 * @return true if everything is okay, false if not
	 */
	public static boolean isComplete(final String indexDir) {
		final File f = new File(indexDir);
		if (!f.exists()) {
			return false;
		}
		if (!f.isDirectory()) {
			f.delete();
			return false;
		}
		if (f.listFiles().length == 0) {
			return false;
		}
		return true;
	}

	@Override
	protected void cleanupAfterError(Exception ex) {
		deleteDirQuietly(new File(targetDir));
	}

	@Override
	public Void impl(Void... params) throws Exception {
		edictDownloadAndUnpack();
		return null;
	}

	private void deleteDirQuietly(final File dir) {
		try {
			MiscUtils.deleteDir(dir);
		} catch (IOException e) {
			Log.e(DownloadDictTask.class.getSimpleName(), "Failed to delete the directory", e);
		}
	}

	/**
	 * Downloads the edict file (in the .gz format) and unpacks it onto the
	 * sdcard.
	 * 
	 * @throws IOException
	 *             on i/o error.
	 */
	private void edictDownloadAndUnpack() throws IOException {
		if (isComplete(targetDir)) {
			return;
		}
		publish(new Progress(AedictApp.getStr(R.string.connecting), 0, 100));
		final URLConnection conn = source.openConnection();
		// this is the unpacked edict file size.
		final File dir = new File(targetDir);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Failed to create directory '" + targetDir + "'. Please make sure that the sdcard is inserted in the phone, mounted and is not write-protected.");
		}
		final InputStream in = new BufferedInputStream(conn.getInputStream());
		try {
			publish(new Progress(AedictApp.format(R.string.downloading_dictionary, dictName), 0, 100));
			copy(in);
		} finally {
			MiscUtils.closeQuietly(in);
		}
	}

	private static final int BUFFER_SIZE = 32768;
	private static final int REPORT_EACH_XTH_BYTE = BUFFER_SIZE * 8;

	/**
	 * Copies all bytes from given input stream to given file, overwriting the
	 * file. Progress is updated periodically.
	 * 
	 * @param in
	 *            the source stream, already buffered.
	 * @throws IOException
	 *             on i/o error
	 */
	protected abstract void copy(final InputStream in) throws IOException;

	/**
	 * Copies streams. Provides automatic notification of the progress.
	 * 
	 * @param downloadedUntilNow
	 *            how many bytes we downloaded until now.
	 * @param expectedSize
	 *            the expected size in bytes of the input stream, -1 if not
	 *            known.
	 * @param in
	 *            the input stream itself, must not be null.
	 * @param out
	 *            the output stream, must not be null.
	 * @return bytes actually copied
	 * @throws IOException
	 *             on I/O problem
	 */
	protected final long copy(final long downloadedUntilNow, long expectedSize, final InputStream in, final OutputStream out) throws IOException {
		long size = expectedSize;
		if (size < 0) {
			size = this.expectedSize;
		}
		final int max = (int) (size / 1024);
		long downloaded = downloadedUntilNow;
		publish(new Progress(null, (int) (downloaded / 1024L), max));
		int reportCountdown = REPORT_EACH_XTH_BYTE;
		final byte[] buf = new byte[BUFFER_SIZE];
		int bufLen;
		while ((bufLen = in.read(buf)) >= 0) {
			out.write(buf, 0, bufLen);
			downloaded += bufLen;
			if (Thread.currentThread().isInterrupted()) {
				throw new InterruptedIOException();
			}
			reportCountdown -= bufLen;
			if (reportCountdown <= 0) {
				final int progress = (int) (downloaded / 1024L);
				publish(new Progress(null, progress, max));
				reportCountdown = REPORT_EACH_XTH_BYTE;
			}
		}
		return downloaded;
	}

	@Override
	public void onSucceeded(Void result) {
		// do nothing
	}
}
