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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

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

	private static final class Progress {
		public Progress() {
			super();
		}

		public Progress(final String message, final int progress) {
			this.message = message;
			this.progress = progress;
		}

		public String message;
		public int progress;
		public Throwable error;

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
		dlg = ProgressDialog.show(context, "Downloading EDict", "", false,
				true, new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						cancel(true);
						dlg.setMessage("Cancelling");
					}
				});
		dlg.setMax(10000);
		dlg.setIndeterminate(false);
	}

	private volatile boolean isError = false;

	@Override
	protected Void doInBackground(Void... params) {
		try {
			performDownloadAndUnpack();
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

	private void performDownloadAndUnpack() throws IOException,
			InterruptedException {
		final URLConnection conn = EDICT_GZ.openConnection();
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
	private static final int REPORT_EACH_XTH_BUFFER = 8;

	private void copy(final InputStream in, final File file, final int length)
			throws IOException, InterruptedException {
		publishProgress(new Progress("Downloading", 0));
		OutputStream out = new FileOutputStream(file);
		try {
			int downloaded = 0;
			int reportCountdown = REPORT_EACH_XTH_BUFFER;
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
				if (reportCountdown-- <= 0) {
					publishProgress(new Progress("Downloading: "
							+ (downloaded / 1024) + "K",
							(int) (downloaded * 10000L / length)));
					reportCountdown = REPORT_EACH_XTH_BUFFER;
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
		if (p < 0) {
			p = 0;
		} else if (p > 10000) {
			p = 10000;
		}
		dlg.setProgress(p);
		dlg.setMessage(values[0].message);
	}
}
