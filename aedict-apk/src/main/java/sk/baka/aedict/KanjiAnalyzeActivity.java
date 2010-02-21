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
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.DownloadDictTask;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.Radicals;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.autils.AbstractTask;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.MiscUtils;
import sk.baka.autils.Progress;
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
import android.widget.AdapterView.AdapterContextMenuInfo;

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
	 * A list of {@link DictEntry} with all information filled (radical, stroke
	 * count, etc).
	 */
	public static final String INTENTKEY_ENTRYLIST = "entrylist";
	/**
	 * Boolean value: False if we parsed given word on a per-character basis,
	 * true on a per-word basis.
	 */
	public static final String INTENTKEY_WORD_ANALYSIS = "wordAnalysis";
	private List<DictEntry> model = null;
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
	/**
	 * true if romaji is shown instead of katakana/hiragana.
	 */
	private boolean isShowingRomaji;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isShowingRomaji = AedictApp.loadConfig().useRomaji;
		word = getIntent().getStringExtra(INTENTKEY_WORD);
		model = (List<DictEntry>) getIntent().getSerializableExtra(INTENTKEY_ENTRYLIST);
		isAnalysisPerCharacter = !getIntent().getBooleanExtra(INTENTKEY_WORD_ANALYSIS, false);
		if (word == null && model == null) {
			throw new IllegalArgumentException("Both word and entrylist are null");
		}
		setTitle(AedictApp.format(R.string.kanjiAnalysisOf, word != null ? word : DictEntry.getJapaneseWord(model)));
		if (model == null) {
			recomputeModel();
		} else {
			// if the activity received a list of EdictEntry instead of a word,
			// the model was not set to the activity and the activity shown an
			// empty list
			// fixes http://code.google.com/p/aedict/issues/detail?id=29
			setListAdapter(newAdapter());
		}
		// check that the KANJIDIC dictionary file is available
		new SearchUtils(this).checkDic(DictTypeEnum.Kanjidic);
		getListView().setOnCreateContextMenuListener(AndroidUtils.safe(this, new View.OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				final DictEntry ee = model.get(((AdapterContextMenuInfo) menuInfo).position);
				menu.add(isShowingRomaji ? R.string.show_kana : R.string.show_romaji).setOnMenuItemClickListener(AndroidUtils.safe(KanjiAnalyzeActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						isShowingRomaji = !isShowingRomaji;
						((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
						return true;
					}
				}));
				menu.add(R.string.addToNotepad).setOnMenuItemClickListener(AndroidUtils.safe(KanjiAnalyzeActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						final Intent intent = new Intent(KanjiAnalyzeActivity.this, NotepadActivity.class);
						intent.putExtra(NotepadActivity.INTENTKEY_ADD_ENTRY, ee);
						startActivity(intent);
						return true;
					}
				}));
			}
		}));
	}

	private ArrayAdapter<DictEntry> newAdapter() {
		final Config cfg = AedictApp.loadConfig();
		return new ArrayAdapter<DictEntry>(this, R.layout.kanjidetail, model) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					v = getLayoutInflater().inflate(R.layout.kanjidetail, getListView(), false);
				}
				final DictEntry e = model.get(position);
				((TextView) v.findViewById(android.R.id.text1)).setText(isShowingRomaji ? cfg.romanization.toRomaji(e.reading) : e.reading);
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
		final DictEntry e = model.get(position);
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
		new RecomputeModel().execute(AedictApp.isInstrumentation, this, word);
	}

	private class RecomputeModel extends AbstractTask<String, List<DictEntry>> {

		@Override
		protected void cleanupAfterError(Exception ex) {
			// nothing to do
		}

		@Override
		protected void onSucceeded(List<DictEntry> result) {
			model = result;
			setListAdapter(newAdapter());
		}

		@Override
		public List<DictEntry> impl(String... params) throws Exception {
			publish(new Progress(AedictApp.getStr(R.string.analyzing), 0, 100));
			if (isAnalysisPerCharacter) {
				// remove all non-letter characters
				final String w = word.replaceAll("[^\\p{javaLetter}]+", "");
				return analyzeByCharacters(KanjiUtils.halfwidthToKatakana(w));
			} else {
				return analyzeByWords(KanjiUtils.halfwidthToKatakana(word));
			}
		}

		private List<DictEntry> analyzeByWords(final String sentence) throws IOException {
			final List<DictEntry> result = new ArrayList<DictEntry>();
			final LuceneSearch lsEdict = new LuceneSearch(DictTypeEnum.Edict, AedictApp.getDictionaryLoc());
			try {
				final String[] words = getWords(sentence);
				final int progressMax = getNumberOfCharacters(words);
				int currentProgress = 0;
				for (int i = 0; i < words.length; i++) {
					if (isCancelled()) {
						return null;
					}
					String w = words[i].trim();
					while (w.length() > 0) {
						final DictEntry entry = findLongestWord(w, lsEdict);
						result.add(entry);
						w = w.substring(entry.getJapanese().length());
						currentProgress += entry.getJapanese().length();
						publish(new Progress(null, currentProgress, progressMax));
					}
				}
				return result;
			} finally {
				MiscUtils.closeQuietly(lsEdict);
			}
		}

		private final String[] getWords(final String sentence) {
			// split the sentence by a non-word characters, like space, hyphen,
			// -, etc.
			return sentence.split("[^\\p{javaLetter}]+");
		}

		private int getNumberOfCharacters(final String[] words) {
			int result = 0;
			for (String word : words) {
				result += word.length();
			}
			return result;
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
		private DictEntry findLongestWord(final String word, final LuceneSearch edict) throws IOException {
			String w = word;
			if (w.length() > 10) {
				// optimization to avoid quadratic search complexity
				w = w.substring(0, 10);
			}
			while (w.length() > 0) {
				final List<DictEntry> result = edict.search(SearchQuery.searchForJapanese(w, true));
				DictEntry.removeInvalid(result);
				Collections.sort(result);
				if (!result.isEmpty()) {
					for (final DictEntry e : result) {
						if (e.getJapanese().equals(w)) {
							return e;
						}
					}
					// no luck, continue with the search
				}
				w = w.substring(0, w.length() - 1);
			}
			return new DictEntry(word.substring(0, 1), "", "");
		}

		private List<DictEntry> analyzeByCharacters(final String word) throws IOException {
			final List<DictEntry> result = new ArrayList<DictEntry>(word.length());
			final LuceneSearch lsEdict = new LuceneSearch(DictTypeEnum.Edict, AedictApp.getDictionaryLoc());
			try {
				LuceneSearch lsKanjidic = null;
				if (DownloadDictTask.isComplete(DictTypeEnum.Kanjidic)) {
					lsKanjidic = new LuceneSearch(DictTypeEnum.Kanjidic, null);
				}
				try {
					final String w = MiscUtils.removeWhitespaces(word);
					for (int i = 0; i < w.length(); i++) {
						publish(new Progress(null, i, w.length()));
						if (isCancelled()) {
							return null;
						}
						final char c = w.charAt(i);
						final boolean isKana = KanjiUtils.isKana(c);
						if (isKana) {
							result.add(new DictEntry(String.valueOf(c), String.valueOf(c), ""));
						} else {
							// it is probably a kanji. search for it in the dictionary.
							final SearchQuery q = SearchQuery.searchForJapanese(String.valueOf(c), true);
							List<DictEntry> matches = null;
							DictEntry ee = null;
							if (lsKanjidic != null) {
								matches = lsKanjidic.search(q, 1);
								DictEntry.removeInvalid(matches);
							}
							if (matches != null && !matches.isEmpty()) {
								ee = matches.get(0);
							}
							if (ee == null) {
								matches = lsEdict.search(q, 1);
								DictEntry.removeInvalid(matches);
								if (!matches.isEmpty()) {
									ee = matches.get(0);
								}
							}
							if (ee == null) {
								// no luck. Just add the kanji
								ee = new DictEntry(String.valueOf(c), "", "");
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
