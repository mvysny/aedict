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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import sk.baka.autils.DialogAsyncTask;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.util.Log;

/**
 * Downloads an EDICT/KANJIDIC dictionary.
 * 
 * @author Martin Vysny
 */
public class DownloadDictTask extends DialogAsyncTask<Void, Void> {

	/**
	 * A zipped Lucene-indexed EDICT location.
	 */
	public static final URL EDICT_LUCENE_ZIP;
	/**
	 * A zipped Lucene-indexed KANJIDIC location.
	 */
	public static final URL KANJIDIC_LUCENE_ZIP;
	/**
	 * A base http:// location of the dictionary files.
	 */
	public static final String DICT_BASE_LOCATION_URL = "http://baka.sk/aedict/";
	static {
		try {
			EDICT_LUCENE_ZIP = new URL(DICT_BASE_LOCATION_URL + "edict-lucene.zip");
			KANJIDIC_LUCENE_ZIP = new URL(DICT_BASE_LOCATION_URL + "kanjidic-lucene.zip");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	private final URL source;
	private final String targetDir;
	private final String dictName;
	private final long expectedSize;

	/**
	 * Creates new dictionary downloader.
	 * 
	 * @param context
	 *            parent context.
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
	public DownloadDictTask(final Activity context, final URL source, final String targetDir, final String dictName, final long expectedSize) {
		super(context);
		this.source = source;
		this.targetDir = targetDir;
		this.dictName = dictName;
		this.expectedSize = expectedSize;
	}

	/**
	 * The base temporary directory, located on the sdcard, where EDICT and
	 * index files are stored.
	 */
	public static final String BASE_DIR = "/sdcard/aedict";
	/**
	 * Directory where the Apache Lucene for the EDICT file index is stored.
	 */
	public static final String LUCENE_INDEX = BASE_DIR + "/index";
	/**
	 * Directory where the Apache Lucene index for the KANJIDIC file is stored.
	 */
	public static final String LUCENE_INDEX_KANJIDIC = BASE_DIR + "/index-kanjidic";

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
	protected void cleanupAfterError() {
		deleteDirQuietly(new File(targetDir));
	}

	@Override
	protected Void protectedDoInBackground(Void... params) throws Exception {
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
		publishProgress(new Progress(R.string.connecting, 0, 100));
		final URLConnection conn = source.openConnection();
		// this is the unpacked edict file size.
		final File dir = new File(targetDir);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Failed to create directory '" + targetDir + "'. Please make sure that the sdcard is inserted in the phone, mounted and is not write-protected.");
		}
		final InputStream in = new BufferedInputStream(conn.getInputStream());
		try {
			final ZipInputStream zip = new ZipInputStream(in);
			copy(in, zip);
		} catch (InterruptedIOException ex) {
			MiscUtils.closeQuietly(in);
			deleteDirQuietly(new File(targetDir));
			throw ex;
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
	 *            use this stream to count bytes
	 * @param zip
	 *            unzip files from here
	 * @throws IOException
	 *             on i/o error
	 */
	private void copy(final InputStream in, final ZipInputStream zip) throws IOException {
		publishProgress(new Progress(AedictApp.format(R.string.downloading_dictionary, dictName), 0, 100));
		long downloaded = 0;
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
			final OutputStream out = new FileOutputStream(targetDir + "/" + entry.getName());
			try {
				downloaded = copy(downloaded, entry, zip, out);
			} finally {
				MiscUtils.closeQuietly(out);
			}
			zip.closeEntry();
		}
	}

	private long copy(final long downloadedUntilNow, final ZipEntry entry, final InputStream in, final OutputStream out) throws IOException {
		long size = entry.getSize();
		if (size < 0) {
			size = expectedSize;
		}
		final int max = (int) (size / 1024);
		long downloaded = downloadedUntilNow;
		publishProgress(new Progress(null, (int) (downloaded / 1024L), max));
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
				publishProgress(new Progress(null, progress, max));
				reportCountdown = REPORT_EACH_XTH_BYTE;
			}
		}
		return downloaded;
	}

	@Override
	protected void onTaskSucceeded(Void result) {
		// do nothing
	}

	/**
	 * Lists all available edict dictionaries, omitting kanjidic.
	 * 
	 * @return maps a dictionary name to to an absolute directory name (e.g.
	 *         /sdcard/aedict/index). The list will always contain the default
	 *         dictionary.
	 */
	public static Map<String, String> listEdictDictionaries() {
		final Map<String, String> result = new HashMap<String, String>();
		result.put(AedictApp.Config.DEFAULT_DICTIONARY_NAME, "/sdcard/aedict/index");
		final File aedict = new File("/sdcard/aedict");
		if (aedict.exists() && aedict.isDirectory()) {
			final String[] dictionaries = aedict.list(new FilenameFilter() {

				public boolean accept(File dir, String filename) {
					return filename.toLowerCase().startsWith("index-");
				}
			});
			for (final String dict : dictionaries) {
				final String dictName = dict.substring("index-".length());
				if (dictName.equalsIgnoreCase("kanjidic")) {
					continue;
				}
				result.put(dictName, "/sdcard/aedict/" + dict);
			}
		}
		return result;
	}
}
