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

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

/**
 * Allows search for Kanji characters using a Radical lookup.
 * 
 * @author Martin Vysny
 * 
 */
public class KanjiSearchRadicalActivity extends AbstractActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kanjisearch_radical);
		final TableLayout v = (TableLayout) findViewById(R.id.kanjisearchRadicals);
		// there is no stupid flow layout in the great Android. Oh well. Let's
		// emulate that with a table.
		radicalsPerRow = getWindowManager().getDefaultDisplay().getWidth() / (30 + 2 * 3);
		currentColumn = 0;
		row = null;
		int strokeCount = -1;
		// general idea: add two-state pushbutton-like textviews for each
		// radical. We cannot use ToggleButton as it is too large.
		for (final char radical : Radicals.RADICAL_ORDERING.toCharArray()) {
			final int strokes = Radicals.getRadical(radical).strokes;
			if (strokeCount != strokes) {
				strokeCount = strokes;
				addRadicalToggle(v, null, strokes);
			}
			addRadicalToggle(v, radical, strokes);
		}
	}

	private int radicalsPerRow;
	private TableRow row = null;
	private int currentColumn = 0;

	/**
	 * 
	 * @param v
	 * @param radical
	 * @param strokes
	 */
	private void addRadicalToggle(final TableLayout v, final Character radical, final int strokes) {
		if (currentColumn++ >= radicalsPerRow) {
			row = null;
			currentColumn = 0;
		}
		if (row == null) {
			row = new TableRow(this);
			v.addView(row);
		}
		int drawable = radical != null ? Radicals.getRadical(radical).resource : -1;
		final View vv;
		if (drawable != -1) {
			final ImageView iv = new ImageView(this);
			vv = iv;
			iv.setImageResource(drawable);
			iv.setMinimumHeight(36);
			iv.setMinimumWidth(36);
			iv.setScaleType(ScaleType.FIT_CENTER);
		} else {
			final TextView tv = new TextView(this);
			vv = tv;
			tv.setText(radical == null ? String.valueOf(strokes) : radical.toString());
			tv.setGravity(Gravity.CENTER);
			tv.setTextSize(30);
			if (radical == null) {
				tv.setBackgroundColor(0xFF993333);
			}
		}
		vv.setPadding(3, 3, 3, 3);
		if (radical != null) {
			final PushButtonListener pbl = new PushButtonListener(radical);
			vv.setOnClickListener(pbl);
			vv.setTag(pbl);
		}
		row.addView(vv);
	}

	/**
	 * Adds a ToggleButton-like functionality to a view.
	 */
	private final class PushButtonListener implements View.OnClickListener {

		public PushButtonListener(char radical) {
			super();
			this.radical = radical;
		}

		public final char radical;
		private boolean pushed = false;

		public boolean isPushed() {
			return pushed;
		}

		public void onClick(View v) {
			pushed = !pushed;
			v.setBackgroundColor(pushed ? 0xFF449977 : 0x00000000);
			recomputeRadical();
		}
	}

	/**
	 * Updates the activity caption to reflect selected radicals.
	 */
	private void recomputeRadical() {
		final String selectedRadicals = getRadicals();
		this.setTitle(getString(R.string.kanjiRadicalLookup) + ": " + selectedRadicals);
	}

	/**
	 * Computes currently selected radicals.
	 */
	private String getRadicals() {
		final StringBuilder sb = new StringBuilder();
		final TableLayout v = (TableLayout) findViewById(R.id.kanjisearchRadicals);
		for (int i = 0; i < v.getChildCount(); i++) {
			final TableRow tr = (TableRow) v.getChildAt(i);
			for (int j = 0; j < tr.getChildCount(); j++) {
				final PushButtonListener pbl = (PushButtonListener) tr.getChildAt(j).getTag();
				if (pbl != null && pbl.isPushed()) {
					sb.append(pbl.radical);
				}
			}
		}
		return sb.toString();
	}
}
