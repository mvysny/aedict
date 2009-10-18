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

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Shows a list of katakana/hiragana characters.
 * 
 * @author Martin Vysny
 */
public class KanaTableActivity extends AbstractActivity {
	private void fillTable(boolean hiragana) {
		final TableLayout l = (TableLayout) findViewById(R.id.kanaTable);
		l.removeAllViews();
		// create a header
		TableRow row = new TableRow(this);
		l.addView(row);
		add(row, "");
		for (char c : ORDER) {
			add(row, String.valueOf(c));
		}
		// create other rows
		for (int r = 0; r < KANA_ORDER.size() / 5; r++) {
			row = new TableRow(this);
			l.addView(row);
			String firstKana = KANA_ORDER.get(r * 5);
			if (firstKana.length() > 1) {
				firstKana = firstKana.substring(0, 1);
			} else {
				firstKana = "";
			}
			add(row, firstKana);
			for (int i = 0; i < ORDER.length; i++) {
				String kana = KANA_ORDER.get(r * 5 + i);
				kana = hiragana ? RomanizationEnum.Hepburn.toHiragana(kana) : RomanizationEnum.Hepburn.toKatakana(kana);
				add(row, kana);
			}
		}
	}

	private KanaTableActivity add(final TableRow row, String text) {
		TextView tv = new TextView(this);
		tv.setText(text);
		tv.setPadding(9, 3, 9, 3);
		tv.setTextSize(30);
		row.addView(tv);
		return this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kanatable);
		fillTable(true);
		final Spinner s = (Spinner) findViewById(R.id.kanaSelectSpinner);
		s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				fillTable(arg2 == 0);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private static final char[] ORDER = new char[] { 'a', 'i', 'u', 'e', 'o' };

	private static final List<String> KANA_ORDER = new ArrayList<String>();
	static {
		add("");
		add("k");
		add("sa", "shi", "su", "se", "so");
		add("ta", "chi", "tsu", "te", "to");
		add("n");
		add("ha", "hi", "fu", "he", "ho");
		add("m");
		add("ya", "", "yu", "", "yo");
		add("r");
		add("wa", "wi", "", "we", "wo");
		add("", "", "n", "", "");
	}

	private static void add(final String prefix) {
		for (final char suffix : ORDER) {
			KANA_ORDER.add(prefix + suffix);
		}
	}

	private static void add(String a, String i, String u, String e, String o) {
		KANA_ORDER.add(a);
		KANA_ORDER.add(i);
		KANA_ORDER.add(u);
		KANA_ORDER.add(e);
		KANA_ORDER.add(o);
	}

}
