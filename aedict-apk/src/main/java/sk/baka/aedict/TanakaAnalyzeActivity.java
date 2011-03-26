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
import java.util.Iterator;
import java.util.List;

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.Dictionary;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.dict.TanakaDictEntry;
import sk.baka.aedict.util.DictEntryListActions;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.AbstractTask;
import sk.baka.autils.MiscUtils;
import sk.baka.autils.Progress;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Perform analysis of the Tanaka sentence.
 * 
 * @author Martin Vysny
 */
public class TanakaAnalyzeActivity extends ListActivity {
	/**
	 * The {@link TanakaDictEntry} to analyze.
	 */
	static final String INTENTKEY_TANAKADICTENTRY = "tanakaDictEntry";
	private static final String INTENTKEY_STATE = "state";

	public static void launch(final Activity activity, final TanakaDictEntry td) {
		if (td == null || td.wordList == null || td.wordList.isEmpty()) {
			throw new IllegalArgumentException("word is null");
		}
		if (!AedictApp.getDownloader().checkDictionary(activity, new Dictionary(DictTypeEnum.Tanaka, null), null, false)) {
			return;
		}
		final Intent i = new Intent(activity, TanakaAnalyzeActivity.class);
		i.putExtra(INTENTKEY_TANAKADICTENTRY, td);
		activity.startActivity(i);
	}

	private ShowRomaji showRomaji;
	private List<DictEntry> model = null;
	private TanakaDictEntry tanaka;

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		model = (List<DictEntry>) savedInstanceState.getSerializable(INTENTKEY_STATE);
		showRomaji.loadState(savedInstanceState);
		setListAdapter(newAdapter());
	}

	private ArrayAdapter<DictEntry> newAdapter() {
		return new ArrayAdapter<DictEntry>(this, R.layout.kanjidic_list_item, model) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					v = getLayoutInflater().inflate(R.layout.kanjidic_list_item, getListView(), false);
				}
				final DictEntry e = model.get(position);
				((TextView) v.findViewById(android.R.id.text1)).setText(showRomaji.romanize(e.reading));
				final StringBuilder sb = new StringBuilder();
				sb.insert(0, e.english);
				((TextView) v.findViewById(android.R.id.text2)).setText(sb.toString());
				final TextView tv = (TextView) v.findViewById(R.id.kanjiBig);
				// if the japanese word is too big the reading and the
				// translation is not shown anymore
				// workaround: add \n character after each third char
				tv.setText(splitToRows(e.getJapanese()));
				return v;
			}

			private String splitToRows(final String str) {
				if (str == null) {
					return "";
				}
				final StringBuilder sb = new StringBuilder(str.length() * 4 / 3);
				for (int i = 0; i < str.length(); i++) {
					if ((i > 0) && (i % 3 == 0)) {
						sb.append('\n');
					}
					sb.append(str.charAt(i));
				}
				return sb.toString();
			}
		};
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		showRomaji.saveState(outState);
		outState.putSerializable(INTENTKEY_STATE, (Serializable) model);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final DictEntry e = model.get(position);
		if (!e.isValid()) {
			return;
		}
		if (e instanceof EdictEntry) {
			EdictEntryDetailActivity.launch(this, (EdictEntry) e);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showRomaji = new ShowRomaji() {

			@Override
			protected void show(boolean romaji) {
				if (getListAdapter() != null) {
					((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
				}
			}
		};
		tanaka = (TanakaDictEntry) getIntent().getSerializableExtra(INTENTKEY_TANAKADICTENTRY);
		new DictEntryListActions(this, true, true, false, true).register(getListView());
	}

	@Override
	protected void onResume() {
		super.onResume();
		showRomaji.onResume();
		if(model==null){
			recomputeModel();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		showRomaji.register(this, menu);
		return true;
	}

	private void recomputeModel() {
		new RecomputeModel().execute(AedictApp.isInstrumentation, this, tanaka);
	}

	private class RecomputeModel extends AbstractTask<TanakaDictEntry, List<DictEntry>> {

		@Override
		protected void cleanupAfterError(Exception ex) {
			// nothing to do
		}

		@Override
		protected void onSucceeded(List<DictEntry> result) {
			model = result;
			model.add(0, new DictEntry("", tanaka.kanji + "\n" + tanaka.reading, tanaka.english));
			setListAdapter(newAdapter());
		}

		@Override
		public List<DictEntry> impl(TanakaDictEntry... params) throws Exception {
			publish(new Progress(AedictApp.getStr(R.string.analyzing), 0, 100));
			final List<DictEntry> result = new ArrayList<DictEntry>();
			final LuceneSearch lsEdict = new LuceneSearch(DictTypeEnum.Edict, AedictApp.getConfig().getDictionaryLoc(), true);
			try {
				final TanakaDictEntry e = params[0];
				for (int i = 0; i < e.wordList.size(); i++) {
					publish(new Progress(null, i, e.wordList.size()));
					if (isCancelled()) {
						return null;
					}
					final String kanji = e.wordList.get(i);
					final SearchQuery q = SearchQuery.searchJpEdict(kanji, MatcherEnum.Exact);
					final List<DictEntry> matches = lsEdict.search(q, 10);
					for (Iterator<DictEntry> it = matches.iterator(); it.hasNext();) {
						final DictEntry ee = it.next();
						if (!ee.getJapanese().equals(kanji)) {
							it.remove();
						}
					}
					DictEntry.removeInvalid(matches);
					final DictEntry ee;
					if (!matches.isEmpty()) {
						ee = matches.get(0);
					} else {
						// no luck. Just add the kanji
						ee = new DictEntry(kanji, "", "");
					}
					result.add(ee);
				}
				return result;
			} finally {
				MiscUtils.closeQuietly(lsEdict);
			}
		}
	}
}
