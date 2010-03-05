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

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import android.content.Intent;

/**
 * Tests the {@link EntryDetailActivity} class.
 * 
 * @author Martin Vysny
 */
public class EntryDetailActivityTest extends AbstractAedictTest<EntryDetailActivity> {

	public EntryDetailActivityTest() {
		super(EntryDetailActivity.class);
	}

	public void testStartActivity() {
		startActivity("母", "はは", "mother");
	}

	public void testCorrectContentsOfViews() {
		testStartActivity();
		tester.assertText(R.id.kanjiSearchEdit, "母");
		tester.assertText(R.id.readingSearchEdit, "はは");
		tester.assertText(R.id.englishSearchEdit, "mother");
	}

	public void testEnglishSearch() {
		testCorrectContentsOfViews();
		tester.click(R.id.englishExactMatch);
		tester.click(R.id.englishSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = SearchQuery.fromIntent(getStartedActivityIntent());
		assertEquals("mother", q.query[0]);
		assertEquals(1, q.query.length);
		assertFalse(q.isJapanese);
		assertEquals(DictTypeEnum.Edict, q.dictType);
		assertEquals(MatcherEnum.Exact, q.matcher);
	}

	public void testKanjiSearch() {
		testCorrectContentsOfViews();
		tester.click(R.id.kanjiExactMatch);
		tester.click(R.id.kanjiSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = SearchQuery.fromIntent(getStartedActivityIntent());
		// the jp search is intended primarily for searching for romaji.
		// therefore the default functionality always tries to produce two
		// search terms, both in hiragana and katakana
		assertEquals("母", q.query[0]);
		assertEquals("母", q.query[1]);
		assertEquals(2, q.query.length);
		assertTrue(q.isJapanese);
		assertEquals(DictTypeEnum.Edict, q.dictType);
		assertEquals(MatcherEnum.Exact, q.matcher);
	}

	public void testReadingSearch() {
		testCorrectContentsOfViews();
		tester.click(R.id.readingExactMatch);
		tester.click(R.id.readingSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = SearchQuery.fromIntent(getStartedActivityIntent());
		// the jp search is intended primarily for searching for romaji.
		// therefore the default functionality always tries to produce two
		// search terms, both in hiragana and katakana
		assertEquals("はは", q.query[0]);
		assertEquals("はは", q.query[1]);
		assertEquals(2, q.query.length);
		assertTrue(q.isJapanese);
		assertEquals(DictTypeEnum.Edict, q.dictType);
		assertEquals(MatcherEnum.Exact, q.matcher);
	}

	private void startActivity(String kanji, String reading, String english) {
		final Intent i = new Intent(getInstrumentation().getTargetContext(), EntryDetailActivity.class);
		i.putExtra(EntryDetailActivity.INTENTKEY_ENTRY, new DictEntry(kanji, reading, english));
		tester.startActivity(i);
	}

}
