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
import sk.baka.autils.AndroidUtils;
import android.app.Activity;
import android.content.Context;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Contains utility methods for searching with Lucene.
 * 
 * @author Martin Vysny
 */
public final class SearchUtils {
	private final Activity activity;

	/**
	 * Creates new utility class instance.
	 * 
	 * @param activity
	 *            owning activity, must not be null.
	 */
	public SearchUtils(final Activity activity) {
		this.activity = activity;
	}

	/**
	 * Configures given button to copy a text from given edit to the global
	 * clipboard.
	 * 
	 * @param copyButton
	 *            copies the text to the clipboard on this button press
	 * @param textView
	 *            copies the text from this {@link TextView}
	 */
	public void setupCopyButton(final int copyButton, final int textView) {
		final Button btn = (Button) activity.findViewById(copyButton);
		final TextView text = (TextView) activity.findViewById(textView);
		btn.setOnClickListener(AndroidUtils.safe(activity, new View.OnClickListener() {

			public void onClick(View v) {
				final ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
				cm.setText(text.getText());
				final Toast toast = Toast.makeText(activity, AedictApp.format(R.string.copied, text.getText()), Toast.LENGTH_SHORT);
				toast.show();
			}
		}));
	}
}
