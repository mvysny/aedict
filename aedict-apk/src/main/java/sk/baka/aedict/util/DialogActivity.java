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

import java.io.Serializable;

import sk.baka.aedict.R;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Used to replace Android dialogs in cases where an activity wants to display a
 * dialog and finish. In this case, Android will crash with
 * android.view.WindowLeaked exception, which is not very user-friendly. This
 * "dialog" class is a full-blown activity instead of a simple dialog.
 * 
 * @author Martin Vysny
 */
public class DialogActivity extends Activity {
	private final static String INTENTKEY_BUILDER = "builder";

	public static class Builder implements Serializable {
		public Builder(Activity context) {
			this.context = context;
		}

		public final transient Activity context;
		private static final long serialVersionUID = 1L;
		public Integer iconId;
		public String title;
		public String message;
		public Integer positiveTextId;
		public Integer negativeTextId;
		public IDialogListener dlgListener;
		public Builder setDialogListener(IDialogListener dlgListener) {
			this.dlgListener = dlgListener;
			return this;
		}
		public void show() {
			final Intent i = new Intent(context, DialogActivity.class);
			i.putExtra(INTENTKEY_BUILDER, this);
			context.startActivity(i);
		}

		/**
		 * Shows an error dialog.
		 * 
		 * @param messageRes
		 *            the message to show.
		 */
		public void showErrorDialog(final int messageRes) {
			showErrorDialog(context.getString(messageRes));
		}

		/**
		 * Shows an error dialog.
		 * 
		 * @param message
		 *            the message to show.
		 */
		public void showErrorDialog(final String message) {
			setMessage(message);
			setTitle(new DialogUtils(context).getErrorMsg());
			setIcon(android.R.drawable.ic_dialog_alert);
			show();
		}

		/**
		 * Shows an information dialog.
		 * 
		 * @param title
		 *            an optional title. If -1 then no title will be shown.
		 * @param message
		 *            the dialog message.
		 */
		public void showInfoDialog(final int title, final int message) {
			showInfoDialog(title == -1 ? null : context.getString(title),
					context.getString(message));
		}

		/**
		 * Shows an information dialog.
		 * 
		 * @param title
		 *            an optional title. If null then no title will be shown.
		 * @param message
		 *            the dialog message.
		 */
		public void showInfoDialog(final String title, final String message) {
			setMessage(message);
			setTitle(title);
			setIcon(android.R.drawable.ic_dialog_info);
			show();
		}

		/**
		 * Shows a simple yes/no dialog. The dialog does nothing and simply
		 * disappears when No is clicked.
		 * 
		 * @param message
		 *            the message to show
		 */
		public void showYesNoDialog(final String message) {
			showYesNoDialog(null, message);
		}

		/**
		 * Shows a simple yes/no dialog. The dialog does nothing and simply
		 * disappears when No is clicked.
		 * 
		 * @param title
		 *            an optional title of the dialog. When null or blank the
		 *            title will not be shown.
		 * @param message
		 *            the message to show
		 */
		public void showYesNoDialog(final String title, final String message) {
			setTitle(title);
			setMessage(message);
			positiveTextId = R.string.yes;
			negativeTextId = R.string.no;
			show();
		}

		public Builder setTitle(String title) {
			if (!MiscUtils.isBlank(title)) {
				this.title = title;
			}
			return this;
		}

		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		public Builder setIcon(int iconId) {
			this.iconId = iconId;
			return this;
		}
	}

	@Override
	protected final void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.dialog);
		final Builder b = (Builder) getIntent().getSerializableExtra(
				INTENTKEY_BUILDER);
		final ImageView icon = (ImageView) findViewById(R.id.icon);
		if (b.iconId == null) {
			icon.setVisibility(View.GONE);
		} else {
			icon.setImageResource(b.iconId);
		}
		final TextView title = (TextView) findViewById(R.id.title);
		if (b.title == null) {
			title.setVisibility(View.GONE);
		} else {
			title.setText(b.title);
		}
		((TextView) findViewById(R.id.message)).setText(b.message);
		final Button yes = (Button) findViewById(R.id.yes);
		final Button no = (Button) findViewById(R.id.no);
		no.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});
		if (b.negativeTextId != null && b.positiveTextId != null) {
			yes.setText(b.positiveTextId);
			no.setText(b.negativeTextId);
			yes.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					if (b.dlgListener != null) {
						b.dlgListener.onPositiveClick(DialogActivity.this);
					}
					finish();
				}
			});
		} else {
			no.setText("Ok");
			yes.setVisibility(View.GONE);
		}
	}

	public static interface IDialogListener extends Serializable {
		/**
		 * Invoked when the positive button is clicked. Override to implement
		 * custom functionality. The default implementation does nothing. The
		 * activity automatically terminates after this method is invoked.
		 * @param activity the activity reference, never null.
		 */
		void onPositiveClick(final DialogActivity activity);
	}
}
