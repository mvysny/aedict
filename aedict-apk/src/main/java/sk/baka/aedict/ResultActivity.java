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

import java.util.Collections;
import java.util.List;

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.MiscUtils;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TwoLineListItem;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Performs a search and shows search result.
 * 
 * @author Martin Vysny
 */
public class ResultActivity extends ListActivity {
	/**
	 * Shows a list of matched entries. May contain an error message if the
	 * search failed.
	 */
	private List<EdictEntry> model;
	/**
	 * true if the activity was invoked from the Simeji keyboard application.
	 */
	private boolean isSimeji = false;
	/**
	 * true if romaji is shown instead of katakana/hiragana.
	 */
	private boolean isShowingRomaji;
	/**
	 * The query.
	 */
	private SearchQuery query;
	private static final String SIMEJI_ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
	/**
	 * Simeji expects a string stored under this key. This is the replacement
	 * string.
	 */
	private static final String SIMEJI_INTENTKEY_REPLACE = "replace_key";
	/**
	 * boolean - if true then we were launched from Simeji.
	 */
	public static final String INTENTKEY_SIMEJI = "simeji";

	private SearchQuery fromIntent() {
		final Intent it = getIntent();
		final SearchQuery result;
		String action = it.getAction();
		if (SIMEJI_ACTION_INTERCEPT.equals(action)) {
			isSimeji = true;
			result = new SearchQuery();
			result.matcher = MatcherEnum.ExactMatchEng;
			String searchFor = it.getStringExtra(SIMEJI_INTENTKEY_REPLACE);
			if (!MiscUtils.isBlank(searchFor)) {
				searchFor = searchFor.trim();
				// If the first character is a japanese character then we are
				// searching for a
				// katakana/hiragana string
				result.isJapanese = KanjiUtils.isJapanese(searchFor.charAt(0));
				result.query = new String[] { searchFor };
			}
		} else {
			result = SearchQuery.fromIntent(getIntent());
			isSimeji = it.getBooleanExtra(INTENTKEY_SIMEJI, false);
		}
		return result.toLowerCase();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		query = fromIntent().trim();
		setTitle(AedictApp.format(R.string.searchResultsFor, query.prettyPrintQuery()));
		if (MiscUtils.isBlank(query.query)) {
			// nothing to search for
			model = Collections.singletonList(EdictEntry.newErrorMsg(getString(R.string.nothing_to_search_for)));
		} else {
			try {
				model = EdictEntry.tryParseEdict(LuceneSearch.singleSearch(query, false, AedictApp.getDictionaryLoc()));
				Collections.sort(model);
			} catch (Exception ex) {
				Log.e(ResultActivity.class.getSimpleName(), "Failed to perform search", ex);
				model = Collections.singletonList(EdictEntry.newErrorMsg(AedictApp.format(R.string.searchFailed, ex.toString())));
			}
			if (model.isEmpty()) {
				model = Collections.singletonList(EdictEntry.newErrorMsg(getString(R.string.no_results)));
			}
		}
		final Config cfg = AedictApp.loadConfig();
		isShowingRomaji = cfg.useRomaji;
		setListAdapter(new ArrayAdapter<EdictEntry>(this, android.R.layout.simple_list_item_2, model) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TwoLineListItem view = (TwoLineListItem) convertView;
				if (view == null) {
					view = (TwoLineListItem) getLayoutInflater().inflate(android.R.layout.simple_list_item_2, getListView(), false);
				}
				model.get(position).print(view, isShowingRomaji ? cfg.romanization : null);
				return view;
			}

		});
		getListView().setOnCreateContextMenuListener(AndroidUtils.safe(this, new View.OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				final EdictEntry ee = model.get(((AdapterContextMenuInfo) menuInfo).position);
				menu.add(isShowingRomaji ? "Show kana" : "Show romaji").setOnMenuItemClickListener(AndroidUtils.safe(ResultActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						isShowingRomaji = !isShowingRomaji;
						((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
						return true;
					}

				}));
				if (isSimeji) {
					if (!ee.isValid()) {
						return;
					}
					if (ee.kanji != null) {
						menu.add("Return " + ee.kanji).setOnMenuItemClickListener(new SimejiReturn(ee.kanji));
					}
					menu.add("Return " + ee.reading).setOnMenuItemClickListener(new SimejiReturn(ee.reading));
					menu.add("Return " + ee.english).setOnMenuItemClickListener(new SimejiReturn(ee.english));
				}
			}
		}));
	}

	/**
	 * Forces the activity to close and return given string as a result to
	 * Simeji.
	 * 
	 * @author Martin Vysny
	 */
	private class SimejiReturn implements MenuItem.OnMenuItemClickListener {
		private final String stringToReturn;

		/**
		 * Creates the instance.
		 * 
		 * @param stringToReturn
		 *            return this string to Simeji.
		 */
		public SimejiReturn(final String stringToReturn) {
			this.stringToReturn = stringToReturn;
		}

		public boolean onMenuItemClick(MenuItem item) {
			final Intent data = new Intent();
			data.putExtra(SIMEJI_INTENTKEY_REPLACE, stringToReturn);
			setResult(RESULT_OK, data);
			finish();
			return true;
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final EdictEntry e = model.get(position);
		if (!e.isValid()) {
			return;
		}
		if (isSimeji) {
			final Intent data = new Intent();
			final String result = query.isJapanese ? e.english : e.getJapanese();
			data.putExtra(SIMEJI_INTENTKEY_REPLACE, result);
			setResult(RESULT_OK, data);
			finish();
		} else {
			final Intent intent = new Intent(this, EntryDetailActivity.class);
			intent.putExtra(EntryDetailActivity.INTENTKEY_ENTRY, e);
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		AbstractActivity.addMenuItems(this, menu);
		return true;
	}
}
