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

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.kanji.RomanizationEnum;
import android.os.Bundle;
import android.view.Gravity;
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
	private void fillTables(boolean hiragana) {
		fillTable(R.id.gojuuonTable, GOJUUON, 5, hiragana);
		fillTable(R.id.youonTable, YOUON, 3, hiragana);
	}

	private void fillTable(int tableId, List<String> table, final int columns, final boolean hiragana) {
		final Config cfg = AedictApp.loadConfig();
		final TableLayout l = (TableLayout) findViewById(tableId);
		l.removeAllViews();
		if (columns == 5) {
			// create a header
			final TableRow row = new TableRow(this);
			l.addView(row);
			add(row, null, "", cfg);
			for (char c : ORDER) {
				add(row, null, String.valueOf(c), cfg);
			}
		}
		// create other rows
		for (int r = 0; r < table.size() / columns; r++) {
			final TableRow row = new TableRow(this);
			l.addView(row);
			final TableRow row2 = new TableRow(this);
			l.addView(row2);
			String firstKana = table.get(r * columns);
			if (firstKana.length() > 1) {
				firstKana = firstKana.substring(0, 1);
			} else {
				firstKana = "";
			}
			add(row, null, firstKana, cfg);
			row2.addView(new TextView(this));
			for (int i = 0; i < columns; i++) {
				String kana = table.get(r * columns + i);
				kana = hiragana ? RomanizationEnum.NihonShiki.toHiragana(kana) : RomanizationEnum.NihonShiki.toKatakana(kana);
				add(row, row2, kana, cfg);
			}
		}
	}

	private KanaTableActivity add(final TableRow row, final TableRow row2, String text, final Config cfg) {
		final TextView tv = new TextView(this);
		tv.setText(text);
		tv.setPadding(9, 3, 9, 3);
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(30);
		row.addView(tv);
		if (row2 != null) {
			final TextView tvReading = new TextView(this);
			tvReading.setText(cfg.romanization.getWriting(text));
			tvReading.setGravity(Gravity.CENTER);
			row2.addView(tvReading);
		}
		return this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kanatable);
		fillTables(true);
		final Spinner s = (Spinner) findViewById(R.id.kanaSelectSpinner);
		s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				fillTables(arg2 == 0);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private static final char[] ORDER = new char[] { 'a', 'i', 'u', 'e', 'o' };

	private static final List<String> GOJUUON = new ArrayList<String>();
	private static final List<String> YOUON = new ArrayList<String>();
	static {
		add("");
		add("k");
		add("s");
		add("t");
		add("n");
		add("h");
		add("m");
		add("ya", "", "yu", "", "yo");
		add("r");
		add("wa", "wi", "", "we", "wo");
		add("", "", "n", "", "");
		add("g");
		add("z");
		add("d");
		add("b");
		add("p");
		add("kya", "kyu", "kyo");
		add("sya", "syu", "syo");
		add("tya", "tyu", "tyo");
		add("nya", "nyu", "nyo");
		add("hya", "hyu", "hyo");
		add("mya", "myu", "myo");
		add("rya", "ryu", "ryo");
		add("gya", "gyu", "gyo");
		add("zya", "zyu", "zyo");
		add("dya", "dyu", "dyo");
		add("bya", "byu", "byo");
		add("pya", "pyu", "pyo");
	}

	private static void add(final String prefix) {
		for (final char suffix : ORDER) {
			GOJUUON.add(prefix + suffix);
		}
	}

	private static void add(String ya, String yu, String yo) {
		YOUON.add(ya);
		YOUON.add(yu);
		YOUON.add(yo);
	}

	private static void add(String a, String i, String u, String e, String o) {
		GOJUUON.add(a);
		GOJUUON.add(i);
		GOJUUON.add(u);
		GOJUUON.add(e);
		GOJUUON.add(o);
	}

}
