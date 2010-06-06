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
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import sk.baka.aedict.util.SodLoader;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.StatFs;
import android.util.Log;

/**
 * Downloads an EDICT/KANJIDIC dictionary.
 * 
 * @author Martin Vysny
 */
public class DownloaderService extends Service {
	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public DownloaderService getService() {
			return DownloaderService.this;
		}
	}

	private final ExecutorService downloader = Executors.newSingleThreadExecutor();

	@Override
	public void onDestroy() {
		downloader.shutdownNow();
		try {
			downloader.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new LocalBinder();

	public static class State {
		public State(String msg, int completeness, final boolean isError) {
			super();
			this.msg = msg;
			this.completeness = completeness;
			this.isError = isError;
		}

		public final String msg;
		/**
		 * 0..100%
		 */
		public final int completeness;
		/**
		 * If true then this state denotes an error. In such case the error
		 * message is stored in {@link #msg}.
		 */
		public final boolean isError;
	}

	/**
	 * Returns current download state or null if no download is currently
	 * active.
	 * 
	 * @return a state of a download or null.
	 */
	public State getState() {
		return state;
	}

	/**
	 * Checks if given dictionary file exists. If not, user is prompted for a
	 * download and the files are downloaded if requested.
	 * 
	 * @return true if the files are available, false otherwise.
	 */
	private boolean checkDictionaryFile(final Activity activity, final AbstractDownloader downloader) {
		if (!isComplete(downloader.targetDir)) {
			final StatFs stats = new StatFs("/sdcard");
			final long free = ((long) stats.getBlockSize()) * stats.getAvailableBlocks();
			final StringBuilder msg = new StringBuilder(getString(R.string.dictionary_missing_download, downloader.dictName));
			if (free < downloader.expectedSize) {
				msg.append('\n');
				msg.append(AedictApp.format(R.string.warning_less_than_x_mb_free, downloader.expectedSize / 1024, free / 1024));
			}
			new DialogUtils(activity).showYesNoDialog(msg.toString(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					download(downloader);
				}
			});
			return false;
		}
		return true;
	}

	/**
	 * Checks if given dictionary exists. If not, user is prompted for a
	 * download and the files are downloaded if requested.
	 * 
	 * @param dict
	 *            the dictionary type.
	 * @return true if the files are available, false otherwise.
	 */
	public boolean checkDic(final Activity a, final DictTypeEnum dict) {
		return checkDictionaryFile(a, new DictDownloader(dict.getDownloadSite(), dict.getDefaultDictionaryPath(), dict.name(), dict.luceneFileSize()));
	}

	/**
	 * Checks if the SOD images exists. If not, user is prompted for a download
	 * and the files are downloaded if requested.
	 * 
	 * @return true if the files are available, false otherwise.
	 */
	public boolean checkSod(final Activity a) {
		return checkDictionaryFile(a, new SodDownloader(SodLoader.DOWNLOAD_URL, SodLoader.SDCARD_LOCATION.getParent(), SodLoader.SDCARD_LOCATION.getName(), 4584605L));
	}

	private void download(final AbstractDownloader download) {
		if (!new File(download.targetDir).isAbsolute()) {
			throw new IllegalArgumentException("Not absolute: " + download.targetDir);
		}
		queue.put(download.dictName, new Object());
		downloader.submit(download);
	}

	public void downloadDict(final URL source, final String targetDir, final String dictName, final long expectedSize) {
		download(new DictDownloader(source, targetDir, dictName, expectedSize));
	}

	private volatile boolean isDownloading = false;
	private volatile State state = null;
	private final ConcurrentMap<String, Object> queue = new ConcurrentHashMap<String, Object>();

	/**
	 * If true then there is a download active.
	 * 
	 * @return true if the service is downloading a dictionary, false otherwise.
	 */
	public boolean isDownloading() {
		return isDownloading;
	}

	public Set<String> getDownloadQueue() {
		return new HashSet<String>(queue.keySet());
	}

	private abstract class AbstractDownloader implements Runnable {
		protected final URL source;
		protected final String targetDir;
		protected final String dictName;
		protected final long expectedSize;

		public AbstractDownloader(final URL source, final String targetDir, final String dictName, final long expectedSize) {
			this.source = source;
			this.targetDir = targetDir;
			this.dictName = dictName;
			this.expectedSize = expectedSize;
		}

		public void run() {
			queue.remove(dictName);
			if (isComplete(targetDir)) {
				return;
			}
			try {
				isDownloading = true;
				try {
					download();
				} finally {
					state = null;
					isDownloading = false;
				}
			} catch (Throwable t) {
				Log.e(DownloaderService.class.getSimpleName(), "Error downloading a dictionary", t);
				state = new State(t.getMessage(), -1, true);
				deleteDirQuietly(new File(targetDir));
			}
		}

		private void deleteDirQuietly(final File dir) {
			try {
				MiscUtils.deleteDir(dir);
			} catch (IOException e) {
				Log.e(DownloaderService.class.getSimpleName(), "Failed to delete the directory", e);
			}
		}

		private void download() throws Exception {
			final URLConnection conn = source.openConnection();
			// this is the unpacked edict file size.
			final File dir = new File(targetDir);
			if (!dir.exists() && !dir.mkdirs()) {
				throw new IOException("Failed to create directory '" + targetDir + "'. Please make sure that the sdcard is inserted in the phone, mounted and is not write-protected.");
			}
			final InputStream in = new BufferedInputStream(conn.getInputStream());
			try {
				state = new State(AedictApp.format(R.string.downloading_dictionary, dictName), 0, false);
				copy(in);
			} finally {
				MiscUtils.closeQuietly(in);
			}
		}

		/**
		 * Copies all bytes from given input stream to given file, overwriting
		 * the file. Progress is updated periodically.
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
			final int max = (int) (size / 1024L);
			long downloaded = downloadedUntilNow;
			state = new State(AedictApp.format(R.string.downloading_dictionary, dictName), (int) (downloaded / 1024L * 100L / max), false);
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
					state = new State(AedictApp.format(R.string.downloading_dictionary, dictName), (int) (progress * 100L / max), false);
					reportCountdown = REPORT_EACH_XTH_BYTE;
				}
			}
			return downloaded;
		}

		private static final int BUFFER_SIZE = 32768;
		private static final int REPORT_EACH_XTH_BYTE = BUFFER_SIZE * 8;
	}

	private class DictDownloader extends AbstractDownloader {
		/**
		 * Creates new dictionary downloader.
		 * 
		 * @param source
		 *            download the dictionary files from here. A zipped Lucene
		 *            index file is expected.
		 * @param targetDir
		 *            unzip the files here
		 * @param dictName
		 *            the dictionary name.
		 * @param expectedSize
		 *            the expected file size of unpacked dictionary.
		 */
		public DictDownloader(URL source, String targetDir, String dictName, long expectedSize) {
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

	private class SodDownloader extends AbstractDownloader {

		/**
		 * Creates new dictionary downloader.
		 * 
		 * @param source
		 *            download the dictionary files from here. A gzipped SOD
		 *            binary file is expected. Please see {@link SodLoader} for
		 *            details on the file format.
		 * @param targetDir
		 *            unzip the files here
		 * @param dictName
		 *            the dictionary name.
		 * @param expectedSize
		 *            the expected file size of unpacked dictionary.
		 */
		public SodDownloader(URL source, String targetDir, String dictName, long expectedSize) {
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
				if (isNonEdictDirectory(dict)) {
					continue;
				}
				final String dictName = dict.substring("index-".length());
				result.put(dictName, "/sdcard/aedict/" + dict);
			}
		}
		return result;
	}

	private static boolean isNonEdictDirectory(final String name) {
		for (DictTypeEnum e : DictTypeEnum.values()) {
			if (e == DictTypeEnum.Edict) {
				continue;
			}
			if (e.getDefaultDictionaryLoc().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
