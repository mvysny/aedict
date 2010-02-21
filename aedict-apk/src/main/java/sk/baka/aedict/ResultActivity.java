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
import java.util.Collections;
import java.util.List;

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.autils.AbstractTask;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.MiscUtils;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
	private List<DictEntry> model;
	/**
	 * true if the activity was invoked from the Simeji keyboard application.
	 */
	private boolean isSimeji = false;
	/**
	 * true if romaji is shown instead of katakana/hiragana.
	 */
	private boolean isShowingRomaji;

	/**
	 * True if the activity shows entries in romaji.
	 * 
	 * @return true if romaji is shown instead of katakana/hiragana.
	 */
	boolean isShowingRomaji() {
		return isShowingRomaji;
	}

	/**
	 * The query.
	 */
	private SearchQuery query;
	/**
	 * Simeji will send this action when requesting word translation.
	 */
	public static final String SIMEJI_ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
	/**
	 * Simeji expects a string stored under this key. This is the replacement
	 * string.
	 */
	public static final String SIMEJI_INTENTKEY_REPLACE = "replace_key";
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
			result = new SearchQuery(DictTypeEnum.Edict);
			result.matcher = MatcherEnum.Exact;
			String searchFor = it.getStringExtra(SIMEJI_INTENTKEY_REPLACE);
			if (!MiscUtils.isBlank(searchFor)) {
				searchFor = searchFor.trim();
				// If the first character is a japanese character then we are
				// searching for a
				// katakana/hiragana string
				// a simple, stupid test, but mostly works :)
				result.isJapanese = searchFor.charAt(0) >= 256;
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
		setContentView(R.layout.searchresult);
		query = fromIntent().trim();
		setTitle(AedictApp.format(R.string.searchResultsFor, query.prettyPrintQuery()));
		if (MiscUtils.isBlank(query.query)) {
			// nothing to search for
			model = Collections.singletonList(DictEntry.newErrorMsg(getString(R.string.nothing_to_search_for)));
		} else {
			model = Collections.emptyList();
			updateModel(true);
			new SearchTask().execute(AedictApp.isInstrumentation, this, query);
		}
		final Config cfg = AedictApp.loadConfig();
		final String dictName = query.dictType == DictTypeEnum.Tanaka ? DictTypeEnum.Tanaka.name() : cfg.dictionaryName;
		((TextView) findViewById(R.id.textSelectedDictionary)).setText(AedictApp.format(R.string.searchingInDictionary, dictName));
		isShowingRomaji = cfg.useRomaji;
		getListView().setOnCreateContextMenuListener(AndroidUtils.safe(this, new View.OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				final int position = ((AdapterContextMenuInfo) menuInfo).position;
				final DictEntry ee = model.get(position);
				final MenuItem miShowRomaji = menu.add(Menu.NONE, 0, 0, isShowingRomaji ? R.string.show_kana : R.string.show_romaji);
				miShowRomaji.setOnMenuItemClickListener(AndroidUtils.safe(ResultActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						isShowingRomaji = !isShowingRomaji;
						((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
						return true;
					}

				}));
				final MenuItem miAddToNotepad = menu.add(Menu.NONE, 1, 1, R.string.addToNotepad);
				miAddToNotepad.setOnMenuItemClickListener(AndroidUtils.safe(ResultActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						final Intent intent = new Intent(ResultActivity.this, NotepadActivity.class);
						intent.putExtra(NotepadActivity.INTENTKEY_ADD_ENTRY, ee);
						startActivity(intent);
						return true;
					}
				}));
				if (isSimeji) {
					if (!ee.isValid()) {
						return;
					}
					if (ee.kanji != null) {
						menu.add(Menu.NONE, 2, 2, AedictApp.format(R.string.return_, ee.kanji)).setOnMenuItemClickListener(new SimejiReturn(ee.kanji));
					}
					menu.add(Menu.NONE, 3, 3, AedictApp.format(R.string.return_, ee.reading)).setOnMenuItemClickListener(new SimejiReturn(ee.reading));
					menu.add(Menu.NONE, 4, 4, AedictApp.format(R.string.return_, ee.english)).setOnMenuItemClickListener(new SimejiReturn(ee.english));
				}
				final MenuItem miSearchInExamples = menu.add(Menu.NONE, 5, 5, R.string.searchInExamples);
				miSearchInExamples.setOnMenuItemClickListener(AndroidUtils.safe(ResultActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						if (!new SearchUtils(ResultActivity.this).checkDic(query.dictType)) {
							// the dictionary is not yet available. An activity
							// was popped up,
							// which offers dictionary download. Nothing to do
							// here, just do
							// nothing.
							return true;
						}
						final SearchQuery q = new SearchQuery(DictTypeEnum.Tanaka);
						q.isJapanese = true;
						final List<String> query = new ArrayList<String>();
						if (ee.kanji != null) {
							query.add(ee.kanji);
						}
						if (ee.reading != null) {
							query.add(ee.reading);
						}
						q.query = query.toArray(new String[0]);
						// we must not use the Substring search - the Japanese
						// word may be inflected and the entry would get
						// filtered out.
						q.matcher = MatcherEnum.Any;
						final Intent intent = new Intent(ResultActivity.this, ResultActivity.class);
						q.putTo(intent);
						startActivity(intent);
						return true;
					}
				}));
			}
		}));
	}

	private void updateModel(final boolean searching) {
		final Config cfg = AedictApp.loadConfig();
		if (model.isEmpty()) {
			model = Collections.singletonList(DictEntry.newErrorMsg(getString(searching ? R.string.searching : R.string.no_results)));
		}
		setListAdapter(new ArrayAdapter<DictEntry>(this, android.R.layout.simple_list_item_2, model) {

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
		final DictEntry e = model.get(position);
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

	private class SearchTask extends AbstractTask<SearchQuery, List<DictEntry>> {

		@Override
		protected void cleanupAfterError(final Exception ex) {
			if (ex == null) {
				// cancelled. set an empty model
				model = Collections.emptyList();
			} else {
				model = Collections.singletonList(DictEntry.newErrorMsg(AedictApp.format(R.string.searchFailed, ex.toString())));
			}
			updateModel(false);
		}

		@Override
		public List<DictEntry> impl(SearchQuery... params) throws Exception {
			final List<DictEntry> result = LuceneSearch.singleSearch(query, query.dictType == DictTypeEnum.Edict ? AedictApp.getDictionaryLoc() : null);
			Collections.sort(result);
			return result;
		}

		@Override
		protected void onSucceeded(List<DictEntry> result) {
			model = result;
			updateModel(false);
		}
	}
}
