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
import sk.baka.aedict.dict.EdictEntry;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

/**
 * Handles the search requests from the Android Search functionality and
 * redirects it to the {@link EdictEntryDetailActivity} activity. Note that the
 * {@link EdictEntryDetailActivity} activity cannot be used directly as EntryDetail's
 * label must not be displayed in the Quick Search Box.
 * 
 * @author Martin Vysny
 */
public class GlobalSearchHandler extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final DictEntry entry = DictEntry.fromExternal(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
		EdictEntryDetailActivity.launch(this, new EdictEntry(entry.kanji, entry.reading, entry.english, entry.english.contains("(P)")));
		finish();
	}
}
