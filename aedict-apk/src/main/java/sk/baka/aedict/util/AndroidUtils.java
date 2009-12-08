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
import sk.baka.aedict.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Contains several Android utilities.
 * 
 * @author Martin Vysny
 */
public final class AndroidUtils {
	private final Context ctx;

	/**
	 * Creates new utility class.
	 * 
	 * @param ctx
	 *            the context to use.
	 */
	public AndroidUtils(final Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * Shows a simple yes/no dialog. The dialog does nothing and simply
	 * disappears when No is clicked.
	 * 
	 * @param message
	 *            the message to show
	 * @param yesListener
	 *            invoked when the Yes button is pressed. The listener is automatically {@link AedictApp#safe(Activity, Class, Object) safe-protected}.
	 */
	public void showYesNoDialog(final String message, final DialogInterface.OnClickListener yesListener) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.yes, AedictApp.safe((Activity)ctx, DialogInterface.OnClickListener.class, yesListener));
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * Shows an error dialog.
	 * 
	 * @param messageRes
	 *            the message to show.
	 */
	public void showErrorDialog(final int messageRes) {
		showErrorDialog(ctx.getString(messageRes));
	}
	/**
	 * Shows an error dialog.
	 * 
	 * @param message
	 *            the message to show.
	 */
	public void showErrorDialog(final String message) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(message);
		builder.setTitle(R.string.error);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.create().show();
	}

	/**
	 * Shows a quick info toast.
	 * 
	 * @param messageRes
	 *            the message to show.
	 */
	public void showInfoDialog(final int messageRes) {
		showInfoDialog(ctx.getString(messageRes));
	}

	/**
	 * Shows a quick info toast.
	 * 
	 * @param message
	 *            the message to show.
	 */
	public void showInfoDialog(final String message) {
		final Toast toast = Toast.makeText(ctx, message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();
	}
}
