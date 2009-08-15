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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

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

	private static final URL EDICT_GZ;
	static {
		try {
			EDICT_GZ = new URL("http://ftp.monash.edu.au/pub/nihongo/edict.gz");
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
	 * The name of the EDICT file.
	 */
	private static final String EDICT = BASE_DIR + "/edict";
	/**
	 * Directory where the Apache Lucene index is stored.
	 */
	public static final String LUCENE_INDEX = BASE_DIR + "/index";
	/**
	 * Index file which translates Edict file line numbers into appropriate byte
	 * positions.
	 */
	public static final String LINE_INDEX = BASE_DIR + "/idx";

	/**
	 * Checks if the edict is downloaded and indexed correctly.
	 * 
	 * @return true if everything is okay, false if not
	 */
	public static boolean isComplete() {
		return exists(BASE_DIR) && exists(EDICT) && exists(LUCENE_INDEX)
				&& exists(LINE_INDEX);
	}

	private static boolean exists(final String fname) {
		return new File(fname).exists();
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			edictDownloadAndUnpack();
			edictIndex();
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
	 * @throws InterruptedException
	 *             if canceled
	 */
	private void edictDownloadAndUnpack() throws IOException,
			InterruptedException {
		if (exists(EDICT)) {
			return;
		}
		publishProgress(new Progress("Connecting", 0));
		final URLConnection conn = EDICT_GZ.openConnection();
		// this is the unpacked edict file size.
		final int length = 10304902;
		final File dir = new File("/sdcard/aedict");
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Failed to create /sdcard/aedict");
		}
		final InputStream in = new GZIPInputStream(conn.getInputStream());
		try {
			copy(in, new File("/sdcard/aedict/edict"), length);
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
	 *            read bytes from here
	 * @param file
	 *            write bytes here
	 * @param length
	 *            number of bytes in the input stream
	 * @throws IOException
	 *             on i/o error
	 * @throws InterruptedException
	 *             if canceled
	 */
	private void copy(final InputStream in, final File file, final int length)
			throws IOException, InterruptedException {
		dlg.setMax(length / 1024);
		publishProgress(new Progress("Downloading EDict", 0));
		OutputStream out = new FileOutputStream(file);
		try {
			int downloaded = 0;
			int reportCountdown = REPORT_EACH_XTH_BYTE;
			final byte[] buf = new byte[BUFFER_SIZE];
			int bufLen;
			while ((bufLen = in.read(buf)) >= 0) {
				out.write(buf, 0, bufLen);
				downloaded += bufLen;
				if (Thread.currentThread().isInterrupted()) {
					// delete incomplete download
					MiscUtils.closeQuietly(out);
					out = null;
					file.delete();
					throw new InterruptedException();
				}
				reportCountdown -= bufLen;
				if (reportCountdown <= 0) {
					publishProgress(new Progress(null, downloaded / 1024));
					reportCountdown = REPORT_EACH_XTH_BYTE;
				}
			}
		} finally {
			MiscUtils.closeQuietly(out);
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
	private static final int REPORT_EACH_XTH_LINE = 1000;
	private static final int FLUSH_LUCENE_EACH_XTH_LINE = 100000;

	/**
	 * Creates Lucene index for the edict file if the index does not exist yet.
	 * Does nothing if the index already exists.
	 * 
	 * @throws IOException
	 *             on i/o error
	 * @throws InterruptedException
	 *             if canceled
	 */
	private void edictIndex() throws IOException, InterruptedException {
		if (exists(LUCENE_INDEX) && exists(LINE_INDEX)) {
			return;
		}
		// number of lines of the edict file
		dlg.setMax(172280);
		final InputStream edict = new FileInputStream(EDICT);
		try {
			final LineReadInputStream lines = new LineReadInputStream(edict);
			final DataOutputStream idx = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(LINE_INDEX)));
			try {
				final IndexWriter luceneWriter = new IndexWriter(LUCENE_INDEX,
						new StandardAnalyzer(), true,
						IndexWriter.MaxFieldLength.LIMITED);
				try {
					edictIndexImpl(lines, idx, luceneWriter);
					luceneWriter.optimize();
				} finally {
					luceneWriter.close();
				}
			} finally {
				MiscUtils.closeQuietly(idx);
			}
		} finally {
			MiscUtils.closeQuietly(edict);
		}
	}

	private void edictIndexImpl(final LineReadInputStream lines,
			final DataOutputStream idx, final IndexWriter luceneWriter)
			throws IOException, InterruptedException {
		publishProgress(new Progress("Indexing", 0));
		int linesRead = 0;
		int fileName = 0;
		idx.writeInt(0);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while (lines.readLine()) {
			bout.write(lines.buffer, lines.lineStart, lines.lineLength);
			bout.write('\n');
			linesRead++;
			if (linesRead >= LINES_PER_INDEXABLE_ITEM) {
				linesRead = 0;
				final String contents = bout.toString("EUC-JP");
				final Document doc = new Document();
//				doc.add(new Field("path", Integer.toString(fileName++),
//						Field.Store.YES, Field.Index.NOT_ANALYZED));
				doc.add(new Field("contents", contents, Field.Store.YES,
						Field.Index.ANALYZED));
				luceneWriter.addDocument(doc);
				idx.writeInt(lines.lineFilePos);
				bout.reset();
			}
			if (lines.lineNumber % REPORT_EACH_XTH_LINE == 0) {
				publishProgress(new Progress(null, lines.lineNumber));
			}
			if (lines.lineNumber % FLUSH_LUCENE_EACH_XTH_LINE == 0) {
				// this prevents OutOfMemoryErrors
				luceneWriter.commit();
				luceneWriter.optimize();
			}
			if (Thread.currentThread().isInterrupted()) {
				MiscUtils.closeQuietly(idx);
				luceneWriter.close();
				new File(LINE_INDEX).delete();
				MiscUtils.deleteDir(new File(LUCENE_INDEX));
				throw new InterruptedException();
			}
		}
	}
}
