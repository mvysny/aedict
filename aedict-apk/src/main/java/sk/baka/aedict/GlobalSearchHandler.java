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

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.Deinflections;
import sk.baka.aedict.kanji.VerbDeinflection;
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
		final String serializedDictEntry = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
		if (serializedDictEntry != null) {
			final DictEntry entry = DictEntry.fromExternal(serializedDictEntry);
			EdictEntryDetailActivity.launch(this, EdictEntry.fromEntry(entry));
		}
		final String query = intent.getStringExtra(SearchManager.QUERY);
		if (query != null) {
			final Deinflections deinflection = VerbDeinflection.searchJpDeinflected(query, AedictApp.getConfig().getRomanization());
			final List<SearchQuery> queries = new ArrayList<SearchQuery>();
			queries.add(deinflection.query);
			queries.add(SearchQuery.searchEnEdict(query, true));
			ResultActivity.launch(this, queries, deinflection.deinflections);
		}
		finish();
	}
}
