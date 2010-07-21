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

import java.util.Arrays;

import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.RomanizationEnum;
import android.content.Intent;

/**
 * Tests the main activity.
 * 
 * @author Martin Vysny
 */
public class MainActivityTest extends AbstractAedictTest<MainActivity> {

	public MainActivityTest() {
		super(MainActivity.class);
	}

	/**
	 * Tests that a search request is sent when japanese search is requested.
	 */
	public void testSimpleJapaneseSearch() {
		tester.startActivity();
		tester.setText(R.id.searchEdit, "haha");
		tester.click(R.id.jpSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = (SearchQuery) getStartedActivityIntent().getSerializableExtra(ResultActivity.INTENTKEY_SEARCH_QUERY);
		assertEquals(RomanizationEnum.Hepburn.toHiragana("haha"), q.query[1]);
		assertEquals(RomanizationEnum.Hepburn.toKatakana("haha"), q.query[0]);
		assertEquals(2, q.query.length);
		assertTrue(q.isJapanese);
		assertEquals(DictTypeEnum.Edict, q.dictType);
		assertEquals(MatcherEnum.Substring, q.matcher);
	}

	/**
	 * Tests that a search request is sent when japanese search is requested.
	 */
	public void testComplexJapaneseSearch() {
		tester.startActivity();
		tester.setText(R.id.searchEdit, "haha AND chichi");
		tester.click(R.id.jpSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = (SearchQuery) getStartedActivityIntent().getSerializableExtra(ResultActivity.INTENTKEY_SEARCH_QUERY);
		assertEquals(RomanizationEnum.Hepburn.toHiragana("haha") + " AND " 	+ RomanizationEnum.Hepburn.toHiragana("chichi"), q.query[1]);
		assertEquals(RomanizationEnum.Hepburn.toKatakana("haha") + " AND " + RomanizationEnum.Hepburn.toKatakana("chichi"), q.query[0]);
		assertEquals(2, q.query.length);
		assertTrue(q.isJapanese);
		assertEquals(DictTypeEnum.Edict, q.dictType);
		assertEquals(MatcherEnum.Substring, q.matcher);
	}

	/**
	 * Tests that a search request is sent when english search is requested.
	 */
	public void testSimpleEnglishSearch() {
		tester.startActivity();
		tester.setText(R.id.searchEdit, "mother");
		tester.setItem(R.id.matcher, MatcherEnum.Exact.ordinal());
		tester.click(R.id.advanced);
		tester.click(R.id.englishSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = (SearchQuery) getStartedActivityIntent().getSerializableExtra(ResultActivity.INTENTKEY_SEARCH_QUERY);
		assertEquals("mother", q.query[0]);
		assertEquals(1, q.query.length);
		assertFalse(q.isJapanese);
		assertEquals(DictTypeEnum.Edict, q.dictType);
		assertEquals(MatcherEnum.Exact, q.matcher);
	}

	/**
	 * Tests that a search request is sent when english search is requested.
	 */
	public void testComplexAndEnglishSearch() {
		tester.startActivity();
		tester.setText(R.id.searchEdit, "mother AND father");
		tester.setItem(R.id.matcher, MatcherEnum.Exact.ordinal());
		tester.click(R.id.advanced);
		tester.click(R.id.englishSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = (SearchQuery) getStartedActivityIntent().getSerializableExtra(ResultActivity.INTENTKEY_SEARCH_QUERY);
		assertEquals("mother AND father", q.query[0]);
		assertEquals(1, q.query.length);
		assertFalse(q.isJapanese);
		assertEquals(DictTypeEnum.Edict, q.dictType);
		assertEquals(MatcherEnum.Exact, q.matcher);
	}

	/**
	 * Tests that a search request is sent when english search is requested.
	 */
	public void testNonExactEnglishSearch() {
		tester.startActivity();
		tester.setText(R.id.searchEdit, "mother");
		tester.click(R.id.englishSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = (SearchQuery) getStartedActivityIntent().getSerializableExtra(ResultActivity.INTENTKEY_SEARCH_QUERY);
		assertEquals(MatcherEnum.Substring, q.matcher);
		assertEquals("mother", q.query[0]);
		assertEquals(1, q.query.length);
		assertFalse(q.isJapanese);
		assertEquals(DictTypeEnum.Edict, q.dictType);
	}

	/**
	 * Tests that a search request is sent when english search is requested.
	 */
	public void testSearchInExamples() {
		tester.startActivity();
		tester.setText(R.id.searchEdit, "mother");
		tester.click(R.id.advanced);
		tester.setItem(R.id.matcher, MatcherEnum.Exact.ordinal());
		tester.click(R.id.searchExamples);
		tester.assertItem(R.id.matcher, MatcherEnum.Substring.ordinal());
		tester.click(R.id.englishSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = (SearchQuery) getStartedActivityIntent().getSerializableExtra(ResultActivity.INTENTKEY_SEARCH_QUERY);
		assertEquals("mother", q.query[0]);
		assertEquals(1, q.query.length);
		assertFalse(q.isJapanese);
		assertEquals(DictTypeEnum.Tanaka, q.dictType);
		assertEquals(MatcherEnum.Substring, q.matcher);
	}

	public void testJpSearchDeinflectVerbs() {
		tester.startActivity();
		tester.setText(R.id.searchEdit, "aenai");
		tester.click(R.id.advanced);
		tester.setItem(R.id.matcher, MatcherEnum.Substring.ordinal());
		tester.click(R.id.jpDeinflectVerbs);
		tester.assertItem(R.id.matcher, MatcherEnum.Exact.ordinal());
		tester.click(R.id.jpSearch);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = (SearchQuery) getStartedActivityIntent().getSerializableExtra(ResultActivity.INTENTKEY_SEARCH_QUERY);
		Arrays.sort(q.query);
		assertEquals("あう", q.query[0]);
		assertEquals("あえる", q.query[1]);
		assertEquals(2, q.query.length);
		assertTrue(q.isJapanese);
		assertEquals(DictTypeEnum.Edict, q.dictType);
		assertEquals(MatcherEnum.Exact, q.matcher);
	}

	public void testTranslateSentence() {
		tester.startActivity();
		tester.setText(R.id.searchEdit, "今週のおすすめﾊﾞｰｹﾞﾝTVｹﾞｰﾑ");
		tester.click(R.id.advanced);
		tester.click(R.id.translate);
		tester.click(R.id.jpSearch);
		tester.assertRequestedActivity(KanjiAnalyzeActivity.class);
		final Intent i = getStartedActivityIntent();
		assertEquals("今週のおすすめﾊﾞｰｹﾞﾝTVｹﾞｰﾑ", i.getStringExtra(KanjiAnalyzeActivity.INTENTKEY_WORD));
		assertTrue(i.getBooleanExtra(KanjiAnalyzeActivity.INTENTKEY_WORD_ANALYSIS, false));
	}
}
