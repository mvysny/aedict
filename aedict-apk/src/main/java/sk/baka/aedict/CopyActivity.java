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

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Provides advanced Copy-Paste functionality.
 * 
 * @author Martin Vysny
 */
public class CopyActivity extends Activity {
	private static final String INTENTKEY_COPY1 = "copy1";
	private static final String INTENTKEY_COPY2 = "copy2";
	private static final String INTENTKEY_COPY3 = "copy3";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.copydialog);
		setup(INTENTKEY_COPY1, R.id.copygroup1);
		setup(INTENTKEY_COPY2, R.id.copygroup2);
		setup(INTENTKEY_COPY3, R.id.copygroup3);
	}

	private void setup(final String intentkey, final int layoutid) {
		final String content = getIntent().getStringExtra(intentkey);
		final View layout = findViewById(layoutid);
		if (MiscUtils.isBlank(content)) {
			layout.setVisibility(View.GONE);
		} else {
			((TextView) layout.findViewById(R.id.edit)).setText(content.trim());
			layout.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					final String text = getSelection((TextView) layout.findViewById(R.id.edit));
					final ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					cm.setText(text);
					final Toast toast = Toast.makeText(CopyActivity.this, AedictApp.format(R.string.copied, text), Toast.LENGTH_SHORT);
					toast.show();
					MainActivity.launch(CopyActivity.this, text);
				}
			});
		}
	}
	
	public static String getSelection(TextView tv) {
		final int e=tv.getSelectionEnd();
		final int s=tv.getSelectionStart();
		if (e < 0 || s < 0 || s == e) {
			return tv.getText().toString();
		}
		return tv.getText().toString().substring(s, e);
	}
	
	public static void launch(Activity activity, String text1, String text2, String text3) {
		final Intent i=new Intent(activity, CopyActivity.class);
		i.putExtra(INTENTKEY_COPY1, text1);
		i.putExtra(INTENTKEY_COPY2, text2);
		i.putExtra(INTENTKEY_COPY3, text3);
		activity.startActivity(i);
	}
	public static void launch(Activity activity, final DictEntry e) {
		launch(activity, e.kanji, ShowRomaji.romanizeIfRequired(e.reading), e.english);
	}
}
