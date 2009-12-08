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

package sk.baka.aedict.skip;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Continues the SKIP wizard for 1, 2 and 3 SKIP type code.
 * 
 * @author Martin Vysny
 */
public class Skip123Activity extends Activity {
	/**
	 * Expects an integer in the invoking intent to be of number 1, 2 or 3.
	 */
	public static final String INTENTKEY_TYPE = "skipType";

	private int skipType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.skip123);
		skipType = getIntent().getIntExtra(INTENTKEY_TYPE, 1);
		final TextView hint = (TextView) findViewById(R.id.skip123);
		hint.setText(skipType == 1 ? R.string.skip1tutorial : skipType == 2 ? R.string.skip2tutorial : R.string.skip3tutorial);
		final TextView first = (TextView) findViewById(R.id.textSkipFirst);
		first.setText(skipType == 1 ? R.string.skip1first : skipType == 2 ? R.string.skip2first : R.string.skip3first);
		final TextView second = (TextView) findViewById(R.id.textSkipSecond);
		second.setText(skipType == 1 ? R.string.skip1second : skipType == 2 ? R.string.skip2second : R.string.skip3second);
		findViewById(R.id.btnSkip123Search).setOnClickListener(AedictApp.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				performSearch();
			}
		}));
	}

	private void performSearch() {
		final int first = skipType;
		final int second = Integer.parseInt(((TextView) findViewById(R.id.textSkipFirst)).getText().toString());
		final int third = Integer.parseInt(((TextView) findViewById(R.id.textSkipSecond)).getText().toString());
		final String skip = SkipActivity.getSkipCode(first, second, third);
		SkipActivity.searchForSkip(this, skip);
	}
}
