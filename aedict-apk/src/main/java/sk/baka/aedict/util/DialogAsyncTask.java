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

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.dict.DownloadDictTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

/**
 * An {@link AsyncTask} which shows a dialog and allows user to cancel the task.
 * 
 * @author Martin Vysny
 * @param <P>
 *            the parameter type
 * @param <R>
 *            the result type
 */
public abstract class DialogAsyncTask<P, R> extends AsyncTask<P, DialogAsyncTask<P, R>.Progress, R> {

	/**
	 * Contains data about a progress.
	 * 
	 * @author Martin Vysny
	 */
	protected final class Progress {
		/**
		 * Creates instance with given message and a progress.
		 * 
		 * @param message
		 *            the message to display
		 * @param progress
		 *            a progress
		 * @param max
		 *            the maximum value of the progress parameter.
		 */
		public Progress(final String message, final int progress, final int max) {
			this.message = message;
			this.progress = progress;
			error = null;
			this.max = max;
		}

		/**
		 * Creates instance with given message and a progress.
		 * 
		 * @param messageRes
		 *            the message to display
		 * @param progress
		 *            a progress
		 * @param max
		 *            the maximum value of the progress parameter.
		 */
		public Progress(final int messageRes, final int progress, final int max) {
			this(context.getString(messageRes), progress, max);
		}

		/**
		 * The message to show.
		 */
		public final String message;
		/**
		 * A progress being made.
		 */
		public final int progress;
		/**
		 * Optional error (if the download failed).
		 */
		public final Throwable error;
		/**
		 * the maximum value of the progress parameter.
		 */
		public final int max;

		/**
		 * Creates the progress object from an error.
		 * 
		 * @param t
		 *            the error, must not be null.
		 */
		private Progress(final Throwable t) {
			progress = 0;
			message = context.getString(sk.baka.aedict.R.string.error) + ": " + t;
			error = t;
			max = 100;
		}
	}

	/**
	 * A context reference.
	 */
	protected final Activity context;

	/**
	 * Creates the task instance.
	 * 
	 * @param context
	 *            a context reference, used to create a dialog.
	 */
	protected DialogAsyncTask(final Activity context) {
		this.context = context;
	}

	private ProgressDialog dlg;

	@Override
	protected final void onPreExecute() {
		dlg = new ProgressDialog(context);
		dlg.setCancelable(true);
		dlg.setOnCancelListener(AedictApp.safe(context, new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				cancel(true);
				dlg.setTitle("Cancelling");
			}
		}));
		dlg.setIndeterminate(false);
		dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// we have to call this method otherwise the title will never be shown
		// (on android 1.5)
		dlg.setTitle("Background operation");
		dlg.show();
	}

	private volatile boolean isError = false;

	/**
	 * Returns true if the task failed with an exception.
	 * 
	 * @return true if the task failed with an exception, false if the task is
	 *         still running or finished successfully.
	 */
	public final boolean isError() {
		return isError;
	}

	@Override
	protected final R doInBackground(P... params) {
		try {
			return protectedDoInBackground(params);
		} catch (Exception ex) {
			if (!isCancelled()) {
				Log.e(DownloadDictTask.class.getSimpleName(), context.getString(sk.baka.aedict.R.string.error), ex);
				isError = true;
				publishProgress(new Progress(ex));
			} else {
				Log.i(DownloadDictTask.class.getSimpleName(), context.getString(sk.baka.aedict.R.string.interrupted), ex);
			}
			cleanupAfterError();
		}
		return null;
	}

	/**
	 * The implementation of the task. The task should periodically invoke
	 * {@link #publishProgress(Object...)} to update the progress. The task
	 * should periodically check for {@link #isCancelled()} - it should
	 * terminate ASAP when canceled, even by throwing an exception.
	 * 
	 * @param params
	 *            the parameters.
	 * @return the result.
	 * @throws Exception
	 */
	protected abstract R protectedDoInBackground(final P... params) throws Exception;

	/**
	 * Performs a cleanup when the task fails (throws an exception).
	 */
	protected abstract void cleanupAfterError();

	@Override
	protected final void onPostExecute(R result) {
		if (!isError) {
			dlg.dismiss();
			onTaskSucceeded(result);
		}
	}

	/**
	 * Invoked when the task finished successfully, from the UI thread.
	 * 
	 * @param result
	 *            the task product.
	 */
	protected abstract void onTaskSucceeded(final R result);

	@Override
	protected void onProgressUpdate(Progress... values) {
		int p = values[0].progress;
		dlg.setProgress(p);
		dlg.setMax(values[0].max);
		String msg = values[0].message;
		final Throwable t = values[0].error;
		if (t != null) {
			dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			String message = msg;
			if (message == null) {
				message = t.toString();
			}
			// This throws NPE on android 1.5???
			// dlg.setMessage(message);
			// the title is too short to display a complex exception. Dismiss the dialog and show a new one.
			dlg.dismiss();
			new AndroidUtils(context).showErrorDialog(t.toString());
		} else {
			if (msg != null) {
				dlg.setTitle(msg);
			}
		}
	}
}
