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
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.DownloadActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.util.DialogActivity;
import sk.baka.aedict.util.IOExceptionWithCause;
import sk.baka.aedict.util.SodLoader;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.StatFs;
import android.util.Log;

/**
 * Downloads an EDICT/KANJIDIC dictionary.
 * 
 * @author Martin Vysny
 */
public class DownloaderService implements Closeable {
	private final ExecutorService downloader = Executors.newSingleThreadExecutor();

	public void close() throws IOException {
		downloader.shutdownNow();
		try {
			downloader.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new IOExceptionWithCause("Interrupted while waiting for thread termination", e);
		}
	}

	public static class State {
		public State(String msg, final String downloadPath, int downloaded, int total, final boolean isError) {
			super();
			this.msg = msg;
			this.downloaded = downloaded;
			this.total = total;
			this.isError = isError;
			this.downloadPath = downloadPath;
		}

		public final String msg;
		public final String downloadPath;
		/**
		 * in KB.
		 */
		public final int downloaded;
		/**
		 * in KB.
		 */
		public final int total;

		public int getCompleteness() {
			return downloaded * 100 / total;
		}

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
	 * @param activity context
	 * @param source
	 *            download the dictionary files from here. A zipped Lucene
	 *            index file is expected.
	 * @param targetDir
	 *            unzip the files here
	 * @param dictName
	 *            the dictionary name.
	 * @param expectedSize
	 *            the expected file size of unpacked dictionary.
	 * @param skipMissingMsg if true then the "dictionary is missing" message is shown only when there is not enough free space.
	 * @return true if the files are available, false otherwise.
	 */
	public boolean checkDictionaryFile(final Activity activity, URL source, String targetDir, String dictName, long expectedSize, final boolean skipMissingMsg) {
		return checkDictionaryFile(activity, new DictDownloader(source, targetDir, dictName, expectedSize), skipMissingMsg);
	}
	/**
	 * Checks if given dictionary file exists. If not, user is prompted for a
	 * download and the files are downloaded if requested.
	 * @param activity context
	 * @param downloader the downloader implementation
	 * @param skipMissingMsg if true then the "dictionary is missing" message is shown only when there is not enough free space.
	 * @return true if the files are available, false otherwise.
	 */
	private boolean checkDictionaryFile(final Activity activity, final AbstractDownloader downloader, final boolean skipMissingMsg) {
		if (!isComplete(downloader.targetDir)) {
			final StatFs stats = new StatFs("/sdcard");
			final long free = ((long) stats.getBlockSize()) * stats.getAvailableBlocks();
			final StringBuilder msg = new StringBuilder(AedictApp.format(R.string.dictionary_missing_download, downloader.dictName));
			if (free < downloader.expectedSize) {
				msg.append('\n');
				msg.append(AedictApp.format(R.string.warning_less_than_x_mb_free, downloader.expectedSize / 1024, free / 1024));
			}
			if (free >= downloader.expectedSize && skipMissingMsg) {
				download(downloader);
				activity.startActivity(new Intent(activity, DownloadActivity.class));
			} else {
				new DialogActivity.Builder(activity).setValue(DownloaderDialogActivity.KEY_DOWNLOADER, downloader).showYesNoDialog(msg.toString(), DownloaderDialogActivity.class);
			}
			return false;
		}
		return true;
	}

	/**
	 * Checks if given dictionary exists. If not, user is prompted for a
	 * download and the files are downloaded if requested.
	 *  @param a context
	 *  @param dict the dictionary type.
	 * @return true if the files are available, false otherwise.
	 */
	public boolean checkDic(final Activity a, final DictTypeEnum dict) {
		return checkDictionaryFile(a, new DictDownloader(dict.getDownloadSite(), dict.getDefaultDictionaryPath(), dict.name(), dict.luceneFileSize()), false);
	}

	/**
	 * Checks if the SOD images exists. If not, user is prompted for a download
	 * and the files are downloaded if requested.
	 * @param a context
	 * @return true if the files are available, false otherwise.
	 */
	public boolean checkSod(final Activity a) {
		return checkDictionaryFile(a, new SodDownloader(), false);
	}

	void download(final AbstractDownloader download) {
		if (!new File(download.targetDir).isAbsolute()) {
			throw new IllegalArgumentException("Not absolute: " + download.targetDir);
		}
		queueDictNames.put(download.dictName, new Object());
		currentDownload = downloader.submit(download);
	}

	private volatile Future<?> currentDownload = null;

	/**
	 * Downloads a dictionary, no questions asked. If the dictionary is already
	 * downloaded it will not be overwritten.
	 * @param dict which dictionary to download.
	 */
	public void downloadDict(final DictTypeEnum dict) {
		download(new DictDownloader(dict.getDownloadSite(), dict.getDefaultDictionaryPath(), dict.name(), dict.luceneFileSize()));
	}
	/**
	 * Downloads the <a href="http://www.kanjicafe.com/using_soder.htm#download">SOD</a> Kanji Draw Order images.
	 */
	public void downloadSod() {
		download(new SodDownloader());
	}

	private volatile boolean isDownloading = false;
	private volatile State state = null;
	private final ConcurrentMap<String, Object> queueDictNames = new ConcurrentHashMap<String, Object>();

	/**
	 * If true then there is a download active.
	 * 
	 * @return true if the service is downloading a dictionary, false otherwise.
	 */
	public boolean isDownloading() {
		return isDownloading;
	}

	public Set<String> getDownloadQueue() {
		return new HashSet<String>(queueDictNames.keySet());
	}

	abstract static class AbstractDownloader implements Runnable, Serializable {
		private static final long serialVersionUID = 1L;
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

		private DownloaderService s() {
			return AedictApp.getDownloader();
		}
		
		public void run() {
			s().queueDictNames.remove(dictName);
			s().state = null;
			if (s().isComplete(targetDir)) {
				return;
			}
			try {
				s().isDownloading = true;
				try {
					download();
				} finally {
					s().state = null;
					s().isDownloading = false;
				}
			} catch (Throwable t) {
				Log.e(DownloaderService.class.getSimpleName(), "Error downloading a dictionary", t);
				s().state = new State(t.getMessage(), null, 0, 1, true);
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
				s().state = new State(AedictApp.format(R.string.downloading_dictionary, dictName), targetDir, 0, 100, false);
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
			s().state = new State(AedictApp.format(R.string.downloading_dictionary, dictName), targetDir, (int) (downloaded / 1024L), max, false);
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
					s().state = new State(AedictApp.format(R.string.downloading_dictionary, dictName), targetDir, progress, max, false);
					reportCountdown = REPORT_EACH_XTH_BYTE;
				}
			}
			return downloaded;
		}

		private static final int BUFFER_SIZE = 32768;
		private static final int REPORT_EACH_XTH_BYTE = BUFFER_SIZE * 8;
	}

	static class DictDownloader extends AbstractDownloader {
		private static final long serialVersionUID = 1L;

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

	static class SodDownloader extends AbstractDownloader {
		private static final long serialVersionUID = 1L;

		/**
		 * Creates new dictionary downloader.
		 */
		public SodDownloader() {
			super(SodLoader.DOWNLOAD_URL, SodLoader.SDCARD_LOCATION.getParent(), SodLoader.SDCARD_LOCATION.getName(), SodLoader.UNPACKED_SIZE);
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
	public boolean isComplete(final DictTypeEnum dict) {
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
	public boolean isComplete(final String indexDir) {
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
		final State s = getState();
		if (s != null && indexDir.equals(s.downloadPath) && !s.isError) {
			// the dictionary is currently being downloaded.
			return false;
		}
		return true;
	}

	public void cancelCurrentDownload() {
		final Future<?> c = currentDownload;
		if (c == null || c.isDone()) {
			return;
		}
		c.cancel(true);
	}
}
