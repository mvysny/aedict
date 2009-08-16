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

package sk.baka.aedict;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Downloads the EDICT dictionary.
 * 
 * @author Martin Vysny
 */
public final class DownloadEdictTask extends
		AsyncTask<Void, DownloadEdictTask.Progress, Void> {

	/**
	 * Contains data about a progress.
	 * 
	 * @author Martin Vysny
	 */
	protected static final class Progress {
		/**
		 * Creates new empty progress instance.
		 */
		public Progress() {
			super();
		}

		/**
		 * Creates instance with given message and a progress.
		 * 
		 * @param message
		 *            the message to display
		 * @param progress
		 *            a progress
		 */
		public Progress(final String message, final int progress) {
			this.message = message;
			this.progress = progress;
		}

		/**
		 * The message to show.
		 */
		public String message;
		/**
		 * A progress being made.
		 */
		public int progress;
		/**
		 * Optional error (if the download failed).
		 */
		public Throwable error;

		/**
		 * Creates the progress object from an error.
		 * 
		 * @param t
		 *            the error, must not be null.
		 * @return new object instance with filled message.
		 */
		public static Progress fromError(final Throwable t) {
			Progress p = new Progress();
			p.progress = -1;
			p.message = "Failed to download EDICT: " + t;
			p.error = t;
			return p;
		}
	}

	private static final URL EDICT_LUCENE_ZIP;
	static {
		try {
			EDICT_LUCENE_ZIP = new URL("http://baka.sk/aedict/edict-lucene.zip");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	private final Context context;

	/**
	 * Creates new EDict downloader.
	 * 
	 * @param context
	 *            parent context.
	 */
	public DownloadEdictTask(Context context) {
		this.context = context;
	}

	private ProgressDialog dlg;

	@Override
	protected void onPreExecute() {
		dlg = new ProgressDialog(context);
		dlg.setCancelable(true);
		dlg.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				cancel(true);
				dlg.setTitle("Cancelling");
			}
		});
		dlg.setIndeterminate(false);
		dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dlg.setTitle("Connecting");
		dlg.show();
	}

	private volatile boolean isError = false;
	/**
	 * The base temporary directory, located on the sdcard, where EDICT and
	 * index files are stored.
	 */
	public static final String BASE_DIR = "/sdcard/aedict";
	/**
	 * Directory where the Apache Lucene index is stored.
	 */
	public static final String LUCENE_INDEX = BASE_DIR + "/index";

	/**
	 * Checks if the edict is downloaded and indexed correctly.
	 * 
	 * @return true if everything is okay, false if not
	 */
	public static boolean isComplete() {
		final File f = new File(LUCENE_INDEX);
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
	protected Void doInBackground(Void... params) {
		try {
			edictDownloadAndUnpack();
		} catch (Exception ex) {
			if (!isCancelled()) {
				Log.e(DownloadEdictTask.class.getSimpleName(), "Error", ex);
				isError = true;
				publishProgress(Progress.fromError(ex));
			} else {
				Log.i(DownloadEdictTask.class.getSimpleName(), "Interrupted",
						ex);
			}
		}
		return null;
	}

	/**
	 * Downloads the edict file (in the .gz format) and unpacks it onto the
	 * sdcard.
	 * 
	 * @throws IOException
	 *             on i/o error.
	 */
	private void edictDownloadAndUnpack() throws IOException {
		if (isComplete()) {
			return;
		}
		publishProgress(new Progress("Connecting", 0));
		final URLConnection conn = EDICT_LUCENE_ZIP.openConnection();
		// this is the unpacked edict file size.
		final int length = conn.getContentLength();
		final File dir = new File(LUCENE_INDEX);
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Failed to create " + LUCENE_INDEX);
		}
		final CountingInputStream in = new CountingInputStream(
				new BufferedInputStream(conn.getInputStream()));
		try {
			final ZipInputStream zip = new ZipInputStream(in);
			copy(in, zip, length);
		} catch (InterruptedIOException ex) {
			MiscUtils.closeQuietly(in);
			FileUtils.deleteDirectory(dir);
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
	 * @param length
	 *            number of bytes in the input stream
	 * @throws IOException
	 *             on i/o error
	 */
	private void copy(final CountingInputStream in, final ZipInputStream zip,
			final int length) throws IOException {
		dlg.setMax(length / 1024);
		publishProgress(new Progress("Downloading EDict", 0));
		for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip
				.getNextEntry()) {
			final OutputStream out = new FileOutputStream(LUCENE_INDEX + "/"
					+ entry.getName());
			try {
				copy(entry, zip, out);
			} finally {
				MiscUtils.closeQuietly(out);
			}
			zip.closeEntry();
		}
	}

	private void copy(final ZipEntry entry, final InputStream in,
			final OutputStream out) throws IOException {
		dlg.setMax((int) (entry.getSize() / 1024));
		int downloaded = 0;
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
				publishProgress(new Progress(null, downloaded / 1024));
				reportCountdown = REPORT_EACH_XTH_BYTE;
			}
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		if (!isError) {
			dlg.dismiss();
		}
	}

	@Override
	protected void onProgressUpdate(Progress... values) {
		int p = values[0].progress;
		dlg.setProgress(p);
		final String msg = values[0].message;
		final Throwable t = values[0].error;
		if (t != null) {
			dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dlg.setMessage(msg);
			dlg.setTitle("Error");
		} else {
			if (msg != null) {
				dlg.setTitle(msg);
			}
		}
	}

	/**
	 * Split the EDICT file into chunks with {@value #LINES_PER_INDEXABLE_ITEM}
	 * lines.
	 */
	public static final int LINES_PER_INDEXABLE_ITEM = 20;
}
