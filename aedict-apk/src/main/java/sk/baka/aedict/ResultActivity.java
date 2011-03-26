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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.Edict;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.dict.TanakaDictEntry;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.kanji.Deinflections.Deinflection;
import sk.baka.aedict.util.Constants;
import sk.baka.aedict.util.DictEntryListActions;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.aedict.util.SpanStringBuilder;
import sk.baka.autils.AbstractTask;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

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

	static final String INTENTKEY_SEARCH_QUERY = "QUERY";
	static final String INTENTKEY_DEINFLECTIONS = "DEINFLECTIONS";
	static final String INTENTKEY_RESULT_LIST = "resultList";

	/**
	 * Use this method sparingly, it has many caveats.
	 * @param activity activity reference.
	 * @param query all queries must have same dictionary type.
	 * @param deinflections deinflections to show
	 */
	public static void launch(final Context activity, final List<SearchQuery> query, final List<Deinflection> deinflections) {
		final Intent intent = new Intent(activity, ResultActivity.class);
		intent.putExtra(INTENTKEY_SEARCH_QUERY, (Serializable) query);
		intent.putExtra(INTENTKEY_DEINFLECTIONS, (Serializable) deinflections);
		activity.startActivity(intent);
	}

	public static void launch(final Context activity, final SearchQuery query, final List<Deinflection> deinflections) {
		final List<SearchQuery> queries = new ArrayList<SearchQuery>();
		queries.add(query);
		launch(activity, queries, deinflections);
	}

	/**
	 * The queries.
	 */
	private List<SearchQuery> queries;
	/**
	 * Simeji will send this action when requesting word translation.
	 */
	public static final String SIMEJI_ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
	public static final String EDICT_ACTION_INTERCEPT = "sk.baka.aedict.action.ACTION_SEARCH_EDICT";
	public static final String EDICT_INTENTKEY_KANJIS="kanjis";
	/**
	 * Simeji expects a string stored under this key. This is the replacement
	 * string.
	 */
	public static final String SIMEJI_INTENTKEY_REPLACE = "replace_key";
	/**
	 * boolean - if true then we were launched from Simeji.
	 */
	public static final String INTENTKEY_SIMEJI = "simeji";

	@SuppressWarnings("unchecked")
	private List<SearchQuery> fromIntent() {
		final Intent it = getIntent();
		final List<SearchQuery> result;
		String action = it.getAction();
		if (SIMEJI_ACTION_INTERCEPT.equals(action)) {
			isSimeji = true;
			result = Collections.singletonList(new SearchQuery(DictTypeEnum.Edict));
			result.get(0).matcher = MatcherEnum.Exact;
			String searchFor = it.getStringExtra(SIMEJI_INTENTKEY_REPLACE);
			if (!MiscUtils.isBlank(searchFor)) {
				searchFor = searchFor.trim();
				// If the first character is a japanese character then we are
				// searching for a
				// katakana/hiragana string
				result.get(0).isJapanese = KanjiUtils.isJapaneseChar(searchFor.charAt(0));
				result.get(0).query = new String[] { searchFor };
			}
		}else if(EDICT_ACTION_INTERCEPT.equals(action)){
			result = Collections.singletonList(new SearchQuery(DictTypeEnum.Edict));
			result.get(0).matcher=MatcherEnum.Exact;
			String searchFor = it.getStringExtra(EDICT_INTENTKEY_KANJIS);
			if (!MiscUtils.isBlank(searchFor)) {
				searchFor = searchFor.trim();
				result.get(0).isJapanese = true;
				result.get(0).query = new String[] { searchFor };
			}
		} else {
			result = (List<SearchQuery>) getIntent().getSerializableExtra(INTENTKEY_SEARCH_QUERY);
			isSimeji = it.getBooleanExtra(INTENTKEY_SIMEJI, false);
		}
		for(final SearchQuery sq:result) {
			sq.trim();
		}
		return result;
	}

	ShowRomaji showRomaji=null;
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		showRomaji.loadState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		showRomaji.saveState(outState);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchresult);
		showRomaji = new ShowRomaji() {

			@Override
			protected void show(boolean romaji) {
				((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
			}
		};
		queries = fromIntent();
		if (queries.get(0).dictType == DictTypeEnum.Tanaka && !AedictApp.isInstrumentation) {
			new DialogUtils(this).showInfoOnce(Constants.INFOONCE_TANAKA_MISSING_READING, -1, R.string.tanakaMissingReading);
		}
		setTitle(AedictApp.format(R.string.searchResultsFor, queries.get(0).prettyPrintQuery()));
		if (MiscUtils.isBlank(queries.get(0).query)) {
			// nothing to search for
			model = Collections.singletonList(DictEntry.newErrorMsg(getString(R.string.nothing_to_search_for)));
		} else if (getIntent().getSerializableExtra(INTENTKEY_RESULT_LIST) != null) {
			model = (List<DictEntry>) getIntent().getSerializableExtra(INTENTKEY_RESULT_LIST);
			updateModel(false);
		} else {
			model = Collections.emptyList();
			updateModel(true);
			new SearchTask().execute(AedictApp.isInstrumentation, this, queries.toArray(new SearchQuery[0]));
		}
		updateTopText();
		new DictEntryListActions(this, true, true, false, true) {
			@Override
			protected void addCustomItems(ContextMenu menu, DictEntry entry,
					int itemIndex) {
				if (isSimeji) {
					if (!entry.isValid()) {
						return;
					}
					if (entry.kanji != null) {
						menu.add(Menu.NONE, 2, 2, AedictApp.format(R.string.return_, entry.kanji)).setOnMenuItemClickListener(new SimejiReturn(entry.kanji));
					}
					menu.add(Menu.NONE, 3, 3, AedictApp.format(R.string.return_, entry.reading)).setOnMenuItemClickListener(new SimejiReturn(entry.reading));
					menu.add(Menu.NONE, 4, 4, AedictApp.format(R.string.return_, entry.english)).setOnMenuItemClickListener(new SimejiReturn(entry.english));
				}
			}
		}.register(getListView());
	}

	private void updateModel(final boolean searching) {
		final RomanizationEnum romanization = AedictApp.getConfig().getRomanization();
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
				Edict.print(model.get(position), view, showRomaji.resolveShowRomaji() ? romanization : null);
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
			returnToSimeji(stringToReturn);
			return true;
		}

	}

	private void returnToSimeji(final String stringToReturn) {
		final Intent data = new Intent(SIMEJI_ACTION_INTERCEPT);
		data.addCategory("com.adamrocker.android.simeji.REPLACE");
		data.addCategory("android.intent.category.DEFAULT");
		data.putExtra(SIMEJI_INTENTKEY_REPLACE, stringToReturn);
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final DictEntry e = model.get(position);
		if (!e.isValid()) {
			return;
		}
		if (isSimeji) {
			returnToSimeji(queries.get(0).isJapanese ? e.english : e.getJapanese());
		} else if (e instanceof EdictEntry) {
			EdictEntryDetailActivity.launch(this, (EdictEntry) e);
		} else if (e instanceof TanakaDictEntry) {
			TanakaAnalyzeActivity.launch(this, (TanakaDictEntry)e);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		showRomaji.register(this, menu);
		AbstractActivity.addMenuItems(this, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		showRomaji.onResume();
	}

	private class SearchTask extends AbstractTask<SearchQuery, List<DictEntry>> {

		@Override
		protected void cleanupAfterError(final Exception ex) {
			if (ex == null) {
				// canceled. set an empty model
				model = Collections.emptyList();
			} else {
				model = Collections.singletonList(DictEntry.newErrorMsg(AedictApp.format(R.string.searchFailed, ex.toString())));
			}
			updateModel(false);
		}

		@Override
		public List<DictEntry> impl(SearchQuery... params) throws Exception {
			final List<DictEntry> result = new ArrayList<DictEntry>();
			final LuceneSearch lucene = new LuceneSearch(params[0].dictType, params[0].dictType == DictTypeEnum.Edict ? AedictApp.getConfig().getDictionaryLoc() : null, AedictApp.getConfig().isSorted());
			try {
				for (final SearchQuery query : params) {
					result.addAll(lucene.search(query));
				}
			} finally {
				MiscUtils.closeQuietly(lucene);
			}
			return result;
		}

		@Override
		protected void onSucceeded(List<DictEntry> result) {
			final Intent i = (Intent) ResultActivity.this.getIntent().clone();
			i.putExtra(INTENTKEY_RESULT_LIST, (Serializable) result);
			startActivity(i);
			ResultActivity.this.finish();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void updateTopText() {
		final SpanStringBuilder b=new SpanStringBuilder();
		final Config cfg = AedictApp.getConfig();
		final String dictName = queries.get(0).dictType == DictTypeEnum.Tanaka ? DictTypeEnum.Tanaka.name() : cfg.getDictionaryName();
		b.append(AedictApp.format(R.string.searchingInDictionary, dictName));
		final List<Deinflection> ds=(List<Deinflection>) getIntent().getSerializableExtra(INTENTKEY_DEINFLECTIONS);
		if(ds!=null){
			for(final Deinflection d:ds) {
				final String inflected=RomanizationEnum.NihonShiki.toHiragana(d.inflected);
				b.append('\n');
				b.append(b.newForeground(0xFFFFFFFF), inflected);
				b.append(" -> ");
				final ListBuilder lb=new ListBuilder(", ");
				for(final String s:d.deinflected){
					lb.add(RomanizationEnum.NihonShiki.toHiragana(s));
				}
				b.append(b.newForeground(0xFFFFFFFF), lb.toString());
				if(d.inflectedForm!=null){
					b.append(" (");
					b.append(getString( d.inflectedForm.explanationResId));
					b.append(')');
				}
			}
		}
		((TextView) findViewById(R.id.textSelectedDictionary)).setText(b);
	}
}
