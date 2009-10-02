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

import java.text.ParseException;

import android.os.Bundle;
import android.widget.EditText;

/**
 * Shows an EDict entry detail window which allows copy-paste and further
 * searches.
 * 
 * @author Martin Vysny
 */
public class EntryDetailActivity extends AbstractActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entrydetail);
		final String unparsedEntry = getIntent().getStringExtra(INTENTKEY_ENTRY);
		final EdictEntry entry;
		try {
			entry = EdictEntry.parse(unparsedEntry);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		final EditText kanjiSearchEdit = (EditText) findViewById(R.id.kanjiSearchEdit);
		kanjiSearchEdit.setText(entry.kanji);
		final EditText readingSearchEdit = (EditText) findViewById(R.id.readingSearchEdit);
		readingSearchEdit.setText(JpUtils.toRomaji(entry.reading));
		final EditText englishSearchEdit = (EditText) findViewById(R.id.englishSearchEdit);
		englishSearchEdit.setText(entry.english);
	}

	/**
	 * The activity expects this entry in the intent table.
	 */
	public static final String INTENTKEY_ENTRY = "entry";

}
