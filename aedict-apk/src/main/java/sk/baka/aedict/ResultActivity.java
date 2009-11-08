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
	 * search failed. In such case {@link #isModelValid} is false.
	 */
	private List<String> model;
	/**
	 * If false then there was some kind of error while performing a search, or
	 * the search returned no results etc. In such case, {@link #model} will
	 * contain a single item containing e.g. throwable message and the list must
	 * not be clickable.
	 */
	private boolean isModelValid = false;
	/**
	 * true if the activity was invoked from the Simeji keyboard application.
	 */
	private boolean isSimeji = false;
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
				final String firstChar = searchFor.substring(0, 1);
				// try to convert the first character to romaji. If the
				// conversion succeeds then we are searching for a
				// katakana/hiragana string
				result.isJapanese = !firstChar.equals(RomanizationEnum.Hepburn.toRomaji(firstChar));
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
			model = Collections.singletonList(getString(R.string.nothing_to_search_for));
		} else {
			try {
				model = LuceneSearch.singleSearch(query);
				if (!model.isEmpty()) {
					isModelValid = true;
					Collections.sort(model);
				}
			} catch (Exception ex) {
				Log.e(ResultActivity.class.getSimpleName(), "Failed to perform search", ex);
				model = Collections.singletonList(AedictApp.format(R.string.searchFailed, ex.toString()));
			}
			if (model.isEmpty()) {
				model = Collections.singletonList(getString(R.string.no_results));
			}
		}
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, model) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TwoLineListItem view = (TwoLineListItem) convertView;
				if (view == null) {
					view = (TwoLineListItem) getLayoutInflater().inflate(android.R.layout.simple_list_item_2, getListView(), false);
				}
				String text1 = "";
				String text2 = model.get(position);
				if (isModelValid) {
					try {
						final EdictEntry ee = EdictEntry.parse(text2);
						if (ee.kanji == null) {
							text1 = ee.reading;
						} else {
							text1 = ee.kanji + "  -  " + ee.reading;
						}
						text2 = ee.english;
					} catch (java.text.ParseException e) {
						Log.e(ResultActivity.class.getSimpleName(), "Failed to parse edict entry", e);
					}
				}
				view.getText1().setText(text1);
				view.getText2().setText(text2);
				return view;
			}

		});
		if (isSimeji) {
			getListView().setOnCreateContextMenuListener(AedictApp.safe(new View.OnCreateContextMenuListener() {

				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					final EdictEntry ee;
					try {
						ee = EdictEntry.parse(model.get(((AdapterContextMenuInfo) menuInfo).position));
					} catch (java.text.ParseException e) {
						throw new RuntimeException(e);
					}
					if (ee.kanji != null) {
						menu.add("Return " + ee.kanji).setOnMenuItemClickListener(new SimejiReturn(ee.kanji));
					}
					menu.add("Return " + ee.reading).setOnMenuItemClickListener(new SimejiReturn(ee.reading));
					menu.add("Return " + ee.english).setOnMenuItemClickListener(new SimejiReturn(ee.english));
				}
			}));
		}
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
		if (!isModelValid) {
			return;
		}
		final String entry = model.get(position);
		if (isSimeji) {
			try {
				final EdictEntry e = EdictEntry.parse(entry);
				final Intent data = new Intent();
				final String result = query.isJapanese ? e.english : e.getJapanese();
				data.putExtra(SIMEJI_INTENTKEY_REPLACE, result);
				setResult(RESULT_OK, data);
				finish();
			} catch (java.text.ParseException e) {
				Log.e(ResultActivity.class.getSimpleName(), "Failed to parse edict entry", e);
			}
		} else {
			final Intent intent = new Intent(this, EntryDetailActivity.class);
			intent.putExtra(EntryDetailActivity.INTENTKEY_ENTRY, entry);
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		AbstractActivity.addActivityLauncher(this, menu, R.string.showkanaTable, R.drawable.kanamenuitem, KanaTableActivity.class);
		AbstractActivity.addActivityLauncher(this, menu, R.string.configuration, android.R.drawable.ic_menu_manage, ConfigActivity.class);
		return true;
	}
}
