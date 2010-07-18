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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.KanjidicEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.Radicals;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.AbstractTask;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.MiscUtils;
import sk.baka.autils.Progress;
import android.app.Activity;
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
	 * A list of {@link KanjidicEntry}.
	 */
	public static final String INTENTKEY_ENTRYLIST = "entrylist";
	/**
	 * Boolean value: False if we parsed given word on a per-character basis,
	 * true on a per-word basis.
	 */
	public static final String INTENTKEY_WORD_ANALYSIS = "wordAnalysis";

	/**
	 * Longest kana word from the dictionary has 33 characters: ニューモノウルトラマイクロスコーピックシリコヴォルケーノコニオシス
	 */
	private static final int MAX_KANA_WORD_LENGTH=10;
	/**
	 * Longest kanji word from the dictionary has 37 characters: プログラム制御式及びキーボード制御式のアドレス指定可能な記憶域をもつ計算器
	 * However this would slow the word analysis to a crawl. Let's expect that the longest word may take at most 10 characters.
	 */
	private static final int MAX_KANJI_WORD_LENGTH=10;
	
	public static void launch(final Activity activity, final String word, final boolean isWordAnalysis) {
		if (word == null) {
			throw new IllegalArgumentException("word is null");
		}
		if (!AedictApp.getDownloader().checkDic(activity, DictTypeEnum.Kanjidic)) {
			return;
		}
		final Intent i = new Intent(activity, KanjiAnalyzeActivity.class);
		i.putExtra(INTENTKEY_WORD, word);
		i.putExtra(INTENTKEY_WORD_ANALYSIS, isWordAnalysis);
		activity.startActivity(i);
	}

	public static void launch(final Activity activity, final List<? extends DictEntry> entries, final boolean isWordAnalysis) {
		if (entries == null) {
			throw new IllegalArgumentException("entries is null");
		}
		if (!AedictApp.getDownloader().checkDic(activity, DictTypeEnum.Kanjidic)) {
			return;
		}
		final Intent i = new Intent(activity, KanjiAnalyzeActivity.class);
		i.putExtra(INTENTKEY_ENTRYLIST, (Serializable) entries);
		i.putExtra(INTENTKEY_WORD_ANALYSIS, isWordAnalysis);
		activity.startActivity(i);
	}

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
	private ShowRomaji showRomaji;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showRomaji = new ShowRomaji(this) {

			@Override
			protected void show(boolean romaji) {
				((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
			}
		};
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
		getListView().setOnCreateContextMenuListener(AndroidUtils.safe(this, new View.OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				final DictEntry ee = model.get(((AdapterContextMenuInfo) menuInfo).position);
				menu.add(R.string.addToNotepad).setOnMenuItemClickListener(AndroidUtils.safe(KanjiAnalyzeActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						NotepadActivity.addAndLaunch(KanjiAnalyzeActivity.this, ee);
						return true;
					}
				}));
				menu.add(R.string.showSod).setOnMenuItemClickListener(AndroidUtils.safe(KanjiAnalyzeActivity.this, new MenuItem.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						StrokeOrderActivity.launch(KanjiAnalyzeActivity.this, ee.getJapanese());
						return true;
					}
				}));
			}
		}));
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
				if (e instanceof KanjidicEntry) {
					final KanjidicEntry ee = (KanjidicEntry) e;
					sb.append(' ').append(Radicals.getRadicals(ee.kanji.charAt(0)));
					sb.append(" Strokes:").append(ee.strokes);
					sb.append(" SKIP:").append(ee.skip);
					if (ee.grade != null) {
						sb.append(" Grade:").append(ee.grade);
					}
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
		if (e instanceof KanjidicEntry) {
			KanjiDetailActivity.launch(this, (KanjidicEntry) e);
		} else if (e instanceof EdictEntry){
			EdictEntryDetailActivity.launch(this, (EdictEntry)e);
		}else{
			// this only happens when the word analysis is turned off and the entry shows a single kana character.
			// just do nothing.
			// fixes http://code.google.com/p/aedict/issues/detail?id=69
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (word == null) {
			return false;
		}
		final MenuItem item;
		if (!isAnalysisPerCharacter) {
			item = menu.add(R.string.analyzeCharacters);
			item.setIcon(android.R.drawable.ic_menu_zoom);
		} else {
			item = menu.add(R.string.analyzeWords);
			item.setIcon(android.R.drawable.ic_menu_search);
		}
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				isAnalysisPerCharacter = !isAnalysisPerCharacter;
				recomputeModel();
				return true;
			}
		});
		showRomaji.register(menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		showRomaji.onResume();
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
			final LuceneSearch lsEdict = new LuceneSearch(DictTypeEnum.Edict, AedictApp.getConfig().getDictionaryLoc(), AedictApp.getConfig().isSorted());
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
						final MatchedWord match = findLongestWord(w, lsEdict);
						result.add(match.entry);
						w = w.substring(match.wordLength);
						currentProgress += match.wordLength;
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
		 *            the word to analyze. Must not contain romaji.
		 * @return longest word found or an entry consisting of the first
		 *         character if we were unable to find nothing
		 * @throws IOException
		 *             on i/o error
		 */
		private MatchedWord findLongestWord(final String word, final LuceneSearch edict) throws IOException {
			String w = word;
			final int maxLength = KanjiUtils.isKanji(word.charAt(0)) ? MAX_KANJI_WORD_LENGTH : MAX_KANA_WORD_LENGTH;
			if (w.length() > maxLength) {
				// optimization to avoid quadratic search complexity
				w = w.substring(0, maxLength);
			}
			while (w.length() > 0) {
				final List<DictEntry> result = edict.search(SearchQuery.searchJpEdict(w, MatcherEnum.Exact), 1);
				DictEntry.removeInvalid(result);
				if (!result.isEmpty()) {
					for (final DictEntry e : result) {
						return new MatchedWord(e, w.length());
					}
					// no luck, continue with the search
				}
				w = w.substring(0, w.length() - 1);
			}
			return new MatchedWord(new DictEntry(word.substring(0, 1), "", ""), 1);
		}

		private List<DictEntry> analyzeByCharacters(final String word) throws IOException {
			final List<DictEntry> result = new ArrayList<DictEntry>(word.length());
			final LuceneSearch lsEdict = new LuceneSearch(DictTypeEnum.Edict, AedictApp.getConfig().getDictionaryLoc(), AedictApp.getConfig().isSorted());
			try {
				LuceneSearch lsKanjidic = null;
				if (AedictApp.getDownloader().isComplete(DictTypeEnum.Kanjidic)) {
					lsKanjidic = new LuceneSearch(DictTypeEnum.Kanjidic, null, AedictApp.getConfig().isSorted());
				}
				try {
					final String w = MiscUtils.removeWhitespaces(word);
					for (int i = 0; i < w.length(); i++) {
						publish(new Progress(null, i, w.length()));
						if (isCancelled()) {
							return null;
						}
						final char c = w.charAt(i);
						final boolean isKanji = KanjiUtils.isKanji(c);
						if (!isKanji) {
							result.add(new DictEntry(String.valueOf(c), String.valueOf(c), ""));
						} else {
							// it is a kanji. search for it in the
							// dictionary.
							final SearchQuery q = SearchQuery.searchJpEdict(String.valueOf(c), MatcherEnum.Exact);
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

	private static class MatchedWord {
		public final DictEntry entry;
		public final int wordLength;

		public MatchedWord(DictEntry entry, int wordLength) {
			this.entry = entry;
			this.wordLength = wordLength;
		}
	}
}
