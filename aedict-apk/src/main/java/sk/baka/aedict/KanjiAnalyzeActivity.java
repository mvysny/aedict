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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sk.baka.aedict.dict.DownloadDictTask;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.Radicals;
import sk.baka.aedict.kanji.RomanizationEnum;

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
 * Analyzes each kanji in given word.
 * 
 * @author Martin Vysny
 */
public class KanjiAnalyzeActivity extends ListActivity {
	/**
	 * The string word to analyze.
	 */
	public static final String INTENTKEY_WORD = "word";
	private List<EdictEntry> model = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String word = getIntent().getStringExtra(INTENTKEY_WORD);
		setTitle(AedictApp.format(R.string.kanjiAnalysisOf, word));
		try {
			model = analyze(word);
		} catch (IOException e) {
			model = new ArrayList<EdictEntry>();
			model.add(EdictEntry.newErrorMsg("Analysis failed: " + e));
		}
		setListAdapter(new ArrayAdapter<EdictEntry>(this, R.layout.kanjidetail, model) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					v = getLayoutInflater().inflate(R.layout.kanjidetail, getListView(), false);
				}
				final EdictEntry e = model.get(position);
				((TextView) v.findViewById(android.R.id.text1)).setText(e.reading);
				final StringBuilder sb = new StringBuilder();
				if (e.radical != null) {
					// TODO mvy: show radicals as images when available?
					sb.append(' ').append(Radicals.getRadicals(e.kanji.charAt(0)));
				}
				if (e.strokes != null) {
					sb.append(" Strokes:").append(e.strokes);
				}
				if (e.skip != null) {
					sb.append(" SKIP:").append(e.skip);
				}
				if (e.grade != null) {
					sb.append(" Grade:").append(e.grade);
				}
				if (sb.length() > 0) {
					sb.replace(0, 1, "\n");
				}
				sb.insert(0, e.english);
				((TextView) v.findViewById(android.R.id.text2)).setText(sb.toString());
				final TextView tv = (TextView) v.findViewById(R.id.kanjiBig);
				tv.setText(e.getJapanese());
				return v;
			}

		});
		// check that KANJIDIC exists
		new SearchUtils(this).checkKanjiDic();
	}

	/**
	 * A very simple check for kanji. Works only on a mixture of kanji, katakana
	 * and hiragana.
	 * 
	 * @param c
	 *            the character to analyze.
	 * @return true if it is a kanji, false otherwise.
	 */
	private boolean isKanji(char c) {
		return RomanizationEnum.Hepburn.toRomaji(String.valueOf(c)).charAt(0) == c;
	}

	private List<EdictEntry> analyze(final String word) throws IOException {
		final List<EdictEntry> result = new ArrayList<EdictEntry>(word.length());
		final LuceneSearch lsEdict = new LuceneSearch(false);
		try {
			LuceneSearch lsKanjidic = null;
			if (DownloadDictTask.isComplete(DownloadDictTask.LUCENE_INDEX_KANJIDIC)) {
				lsKanjidic = new LuceneSearch(true);
			}
			try {
				for (char c : word.toCharArray()) {
					final boolean isKanji = isKanji(c);
					if (!isKanji) {
						result.add(new EdictEntry(String.valueOf(c), String.valueOf(c), ""));
					} else {
						// it is a kanji. search for it in the dictionary.
						final SearchQuery q = new SearchQuery();
						q.isJapanese = true;
						q.matcher = MatcherEnum.ExactMatchEng;
						q.query = new String[] { String.valueOf(c) };
						List<String> matches = null;
						EdictEntry ee = null;
						if (lsKanjidic != null) {
							matches = lsKanjidic.search(q);
						}
						if (matches != null && !matches.isEmpty()) {
							ee = EdictEntry.tryParseKanjidic(matches.get(0));
						}
						if (ee == null) {
							matches = lsEdict.search(q);
							if (matches.size() > 0) {
								ee = EdictEntry.tryParseEdict(matches.get(0));
							}
						}
						if (ee == null) {
							// no luck. Just add the kanji
							ee = new EdictEntry(String.valueOf(c), "", "");
						}
						result.add(ee);
					}
				}
				return result;
			} finally {
				MiscUtils.closeQuietly(lsKanjidic);
			}
		} finally {
			MiscUtils.closeQuietly(lsEdict);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final EdictEntry e = model.get(position);
		if (!e.isValid()) {
			return;
		}
		final Intent intent = new Intent(this, EntryDetailActivity.class);
		intent.putExtra(EntryDetailActivity.INTENTKEY_ENTRY, e);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		AbstractActivity.addActivityLauncher(this, menu, R.string.showkanaTable, R.drawable.kanamenuitem, KanaTableActivity.class);
		AbstractActivity.addActivityLauncher(this, menu, R.string.kanjiDrawLookup, R.drawable.ic_menu_compose, KanjiDrawActivity.class);
		AbstractActivity.addActivityLauncher(this, menu, R.string.kanjiRadicalLookup, android.R.drawable.ic_menu_search, KanjiSearchRadicalActivity.class);
		return true;
	}
}
