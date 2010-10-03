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
import sk.baka.aedict.jlptquiz.InflectionQuizActivity;
import sk.baka.aedict.kanji.Deinflections.Deinflection;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.util.Constants;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.aedict.util.SpanStringBuilder;
import sk.baka.autils.AbstractTask;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
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

	public static void launch(final Context activity, final SearchQuery query, final List<Deinflection> deinflections) {
		final Intent intent = new Intent(activity, ResultActivity.class);
		intent.putExtra(INTENTKEY_SEARCH_QUERY, query);
		intent.putExtra(INTENTKEY_DEINFLECTIONS, (Serializable) deinflections);
		activity.startActivity(intent);
	}

	/**
	 * The query.
	 */
	private SearchQuery query;
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
				result.isJapanese = KanjiUtils.isJapaneseChar(searchFor.charAt(0));
				result.query = new String[] { searchFor };
			}
		}else if(EDICT_ACTION_INTERCEPT.equals(action)){
			result = new SearchQuery(DictTypeEnum.Edict);
			result.matcher=MatcherEnum.Exact;
			String searchFor = it.getStringExtra(EDICT_INTENTKEY_KANJIS);
			if (!MiscUtils.isBlank(searchFor)) {
				searchFor = searchFor.trim();
				result.isJapanese = true;
				result.query = new String[] { searchFor };
			}
		} else {
			result = (SearchQuery) getIntent().getSerializableExtra(INTENTKEY_SEARCH_QUERY);
			isSimeji = it.getBooleanExtra(INTENTKEY_SIMEJI, false);
		}
		return result;
	}

	ShowRomaji showRomaji=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchresult);
		showRomaji = new ShowRomaji(this) {

			@Override
			protected void show(boolean romaji) {
				((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
			}
		};
		query = fromIntent().trim();
		if (query.dictType == DictTypeEnum.Tanaka && !AedictApp.isInstrumentation) {
			new DialogUtils(this).showInfoOnce(Constants.INFOONCE_TANAKA_MISSING_READING, -1, R.string.tanakaMissingReading);
		}
		setTitle(AedictApp.format(R.string.searchResultsFor, query.prettyPrintQuery()));
		if (MiscUtils.isBlank(query.query)) {
			// nothing to search for
			model = Collections.singletonList(DictEntry.newErrorMsg(getString(R.string.nothing_to_search_for)));
		} else {
			model = Collections.emptyList();
			updateModel(true);
			new SearchTask().execute(AedictApp.isInstrumentation, this, query);
		}
		updateTopText();
		getListView().setOnCreateContextMenuListener(AndroidUtils.safe(this, new View.OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				final int position = ((AdapterContextMenuInfo) menuInfo).position;
				final DictEntry ee = model.get(position);
				final MenuItem miAddToNotepad = menu.add(Menu.NONE, 1, 1, R.string.addToNotepad);
				miAddToNotepad.setOnMenuItemClickListener(AndroidUtils.safe(ResultActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						NotepadActivity.addAndLaunch(ResultActivity.this, ee);
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
				final MenuItem miShowSOD = menu.add(Menu.NONE, 6, 6, R.string.showSod);
				miShowSOD.setOnMenuItemClickListener(AndroidUtils.safe(ResultActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						StrokeOrderActivity.launch(ResultActivity.this, ee.getJapanese());
						return true;
					}
				}));
				if ((ee instanceof EdictEntry) && ((EdictEntry) ee).isVerb()) {
					final MenuItem miShowConjugations = menu.add(Menu.NONE, 7, 7, R.string.showConjugations);
					miShowConjugations.setOnMenuItemClickListener(AndroidUtils.safe(ResultActivity.this, new MenuItem.OnMenuItemClickListener() {

						public boolean onMenuItemClick(MenuItem item) {
							VerbInflectionActivity.launch(ResultActivity.this, (EdictEntry)ee);
							return true;
						}
					}));
					final MenuItem miConjugationQuiz = menu.add(Menu.NONE, 8, 8, R.string.conjugationQuiz);
					miConjugationQuiz.setOnMenuItemClickListener(AndroidUtils.safe(ResultActivity.this, new MenuItem.OnMenuItemClickListener() {

						public boolean onMenuItemClick(MenuItem item) {
							InflectionQuizActivity.launch(ResultActivity.this, (EdictEntry)ee);
							return true;
						}
					}));
				}
				final MenuItem miAdvancedCopy = menu.add(Menu.NONE, 9, 9, R.string.advancedCopy);
				miAdvancedCopy.setOnMenuItemClickListener(AndroidUtils.safe(ResultActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						CopyActivity.launch(ResultActivity.this, ee);
						return true;
					}
				}));
			}
		}));
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
				Edict.print(model.get(position), view, showRomaji.isShowingRomaji() ? romanization : null);
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
			returnToSimeji(query.isJapanese ? e.english : e.getJapanese());
		} else if (e instanceof EdictEntry) {
			EdictEntryDetailActivity.launch(this, (EdictEntry) e);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		showRomaji.register(menu);
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
			final List<DictEntry> result = LuceneSearch.singleSearch(query, query.dictType == DictTypeEnum.Edict ? AedictApp.getConfig().getDictionaryLoc() : null, AedictApp.getConfig().isSorted());
			return result;
		}

		@Override
		protected void onSucceeded(List<DictEntry> result) {
			model = result;
			updateModel(false);
		}
	}
	
	private void updateTopText() {
		final SpanStringBuilder b=new SpanStringBuilder();
		final Config cfg = AedictApp.getConfig();
		final String dictName = query.dictType == DictTypeEnum.Tanaka ? DictTypeEnum.Tanaka.name() : cfg.getDictionaryName();
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
