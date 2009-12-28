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
import java.util.Collections;
import java.util.List;

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DownloadDictTask;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.Radicals;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.autils.DialogAsyncTask;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
	/**
	 * A list of {@link EdictEntry} with all information filled (radical, stroke
	 * count, etc).
	 */
	public static final String INTENTKEY_ENTRYLIST = "entrylist";
	/**
	 * Boolean value: False if we parsed given word on a per-character basis,
	 * true on a per-word basis.
	 */
	public static final String INTENTKEY_WORD_ANALYSIS = "wordAnalysis";
	private List<EdictEntry> model = null;
	/**
	 * The word to analyze. If null then we were simply given a list of
	 * EdictEntry directly.
	 */
	private String word;
	/**
	 * True if we parsed given word on a per-character basis, false on a
	 * per-word basis.
	 */
	private boolean isAnalysisPerCharacter = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		word = getIntent().getStringExtra(INTENTKEY_WORD);
		model = (List<EdictEntry>) getIntent().getSerializableExtra(INTENTKEY_ENTRYLIST);
		isAnalysisPerCharacter = !getIntent().getBooleanExtra(INTENTKEY_WORD_ANALYSIS, false);
		if (word == null && model == null) {
			throw new IllegalArgumentException("Both word and entrylist are null");
		}
		setTitle(AedictApp.format(R.string.kanjiAnalysisOf, word != null ? word : EdictEntry.getJapaneseWord(model)));
		if (model == null) {
			recomputeModel();
		}
		// check that the KANJIDIC dictionary file is available
		new SearchUtils(this).checkKanjiDic();
	}

	private ArrayAdapter<EdictEntry> newAdapter() {
		final Config cfg = AedictApp.loadConfig();
		return new ArrayAdapter<EdictEntry>(this, R.layout.kanjidetail, model) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					v = getLayoutInflater().inflate(R.layout.kanjidetail, getListView(), false);
				}
				final EdictEntry e = model.get(position);
				((TextView) v.findViewById(android.R.id.text1)).setText(cfg.useRomaji ? cfg.romanization.toRomaji(e.reading) : e.reading);
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeGroup(0);
		if (word == null) {
			return false;
		}
		if (!isAnalysisPerCharacter) {
			final MenuItem item = menu.add(0, ANALYZE_CHARACTERS, Menu.NONE, R.string.analyzeCharacters);
			item.setIcon(android.R.drawable.ic_menu_zoom);
		} else {
			final MenuItem item = menu.add(0, ANALYZE_WORDS, Menu.NONE, R.string.analyzeWords);
			item.setIcon(android.R.drawable.ic_menu_search);
		}
		return true;
	}

	private static final int ANALYZE_CHARACTERS = 0;
	private static final int ANALYZE_WORDS = 1;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		isAnalysisPerCharacter = item.getItemId() == ANALYZE_CHARACTERS;
		recomputeModel();
		return true;
	}

	private void recomputeModel() {
		new RecomputeModel(this).execute(word);
	}

	private class RecomputeModel extends DialogAsyncTask<String, List<EdictEntry>> {

		public RecomputeModel(Activity context) {
			super(context);
		}

		@Override
		protected void cleanupAfterError() {
			// nothing to do
		}

		@Override
		protected void onTaskSucceeded(List<EdictEntry> result) {
			model = result;
			setListAdapter(newAdapter());
		}

		@Override
		protected List<EdictEntry> protectedDoInBackground(String... params) throws Exception {
			onProgressUpdate(new Progress(R.string.analyzing, 0, 100));
			if (isAnalysisPerCharacter) {
				// remove all non-letter characters
				final String w = word.replaceAll("[^\\p{javaLetter}]+", "");
				return analyzeByCharacters(w);
			} else {
				return analyzeByWords(word);
			}
		}

		private List<EdictEntry> analyzeByWords(final String sentence) throws IOException {
			final List<EdictEntry> result = new ArrayList<EdictEntry>();
			final LuceneSearch lsEdict = new LuceneSearch(false, AedictApp.getDictionaryLoc());
			try {
				final String[] words = getWords(sentence);
				for (int i = 0; i < words.length; i++) {
					onProgressUpdate(new Progress(null, i, words.length));
					String w = words[i].trim();
					while (w.length() > 0) {
						final EdictEntry entry = findLongestWord(w, lsEdict);
						result.add(entry);
						w = w.substring(entry.getJapanese().length());
					}
				}
				return result;
			} finally {
				MiscUtils.closeQuietly(lsEdict);
			}
		}

		private final String[] getWords(final String sentence) {
			return sentence.split("[^\\p{javaLetter}]+");
		}

		/**
		 * Tries to find longest word which is present in the EDICT dictionary.
		 * The search starts with given word, then cuts the last character off,
		 * etc.
		 * 
		 * @param word
		 *            the word to analyze
		 * @return longest word found or an entry consisting of the first
		 *         character if we were unable to find nothing
		 * @throws IOException
		 */
		private EdictEntry findLongestWord(final String word, final LuceneSearch edict) throws IOException {
			String w = word;
			if (w.length() > 10) {
				// optimization to avoid quadratic search complexity
				w = w.substring(0, 10);
			}
			while (w.length() > 0) {
				final List<EdictEntry> result = EdictEntry.tryParseEdict(edict.search(SearchQuery.searchForJapanese(w, true)));
				EdictEntry.removeInvalid(result);
				Collections.sort(result);
				if (!result.isEmpty()) {
					for (final EdictEntry e : result) {
						if (e.getJapanese().equals(w)) {
							return e;
						}
					}
					// no luck, continue with the search
				}
				w = w.substring(0, w.length() - 1);
			}
			return new EdictEntry(word.substring(0, 1), "", "");
		}

		private List<EdictEntry> analyzeByCharacters(final String word) throws IOException {
			final List<EdictEntry> result = new ArrayList<EdictEntry>(word.length());
			final LuceneSearch lsEdict = new LuceneSearch(false, AedictApp.getDictionaryLoc());
			try {
				LuceneSearch lsKanjidic = null;
				if (DownloadDictTask.isComplete(DownloadDictTask.LUCENE_INDEX_KANJIDIC)) {
					lsKanjidic = new LuceneSearch(true, null);
				}
				try {
					final String w = MiscUtils.removeWhitespaces(word);
					for (int i = 0; i < w.length(); i++) {
						onProgressUpdate(new Progress(null, i, w.length()));
						final char c = w.charAt(i);
						final boolean isKanji = KanjiUtils.isKanji(c);
						if (!isKanji) {
							result.add(new EdictEntry(String.valueOf(c), String.valueOf(c), ""));
						} else {
							// it is a kanji. search for it in the dictionary.
							final SearchQuery q = SearchQuery.searchForJapanese(String.valueOf(c), true);
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
	}
}
