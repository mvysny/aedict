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

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final String word = getIntent().getStringExtra(INTENTKEY_WORD);
		setTitle(AedictApp.format(R.string.kanjiAnalysisOf, word));
		List<String> model;
		try {
			model = analyze(word);
		} catch (IOException e) {
			model = new ArrayList<String>();
			model.add("Analysis failed: " + e);
		}
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, model));
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

	private List<String> analyze(final String word) throws IOException {
		final List<String> result = new ArrayList<String>(word.length());
		final LuceneSearch ls = new LuceneSearch();
		try {
			for (char c : word.toCharArray()) {
				final boolean isKanji = isKanji(c);
				if (!isKanji) {
					result.add(String.valueOf(c));
				} else {
					// it is a kanji. search for it in the dictionary.
					final SearchQuery q = new SearchQuery();
					q.isJapanese = true;
					q.matcher = MatcherEnum.ExactMatchEng;
					q.query = new String[] { String.valueOf(c) };
					final List<String> matches = ls.search(q);
					if (matches.size() > 0) {
						result.add(matches.get(0));
					} else {
						// no luck. Just add the kanji
						result.add(String.valueOf(c));
					}
				}
			}
			return result;
		} finally {
			MiscUtils.closeQuietly(ls);
		}
	}
}
