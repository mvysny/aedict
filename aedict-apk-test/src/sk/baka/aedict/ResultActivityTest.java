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
import sk.baka.aedict.kanji.RomanizationEnum;
import android.app.Activity;
import android.content.Intent;
import android.widget.ListView;

/**
 * Tests the ResultActivity activity.
 * 
 * @author Martin Vysny
 * 
 */
public class ResultActivityTest extends AbstractAedictTest<ResultActivity> {

	public ResultActivityTest() {
		super(ResultActivity.class);
	}

	public void testSimpleEnglishSearch() {
		final SearchQuery q = new SearchQuery(DictTypeEnum.Edict);
		q.isJapanese = false;
		q.matcher = MatcherEnum.Exact;
		q.query = new String[] { "mother" };
		launch(q);
		assertTrue(tester.getText(R.id.textSelectedDictionary).contains("Default"));
		final ListView lv = getActivity().getListView();
		assertEquals(25, lv.getCount());
		final DictEntry entry = (DictEntry) lv.getItemAtPosition(0);
		assertEquals("(n) (hum) mother/(P)", entry.english);
		assertEquals("母", entry.getJapanese());
		assertEquals("はは", entry.reading);
	}

	private void launch(final SearchQuery q) {
		final Intent i = new Intent(getInstrumentation().getContext(), ResultActivity.class);
		i.putExtra(ResultActivity.INTENTKEY_SEARCH_QUERY, q);
		tester.startActivity(i);
	}

	public void testSimpleJapaneseSearch() {
		final SearchQuery q = new SearchQuery(DictTypeEnum.Edict);
		q.isJapanese = true;
		q.matcher = MatcherEnum.Exact;
		q.query = new String[] { RomanizationEnum.Hepburn.toHiragana("haha") };
		launch(q);
		assertTrue(tester.getText(R.id.textSelectedDictionary).contains("Default"));
		final ListView lv = getActivity().getListView();
		assertEquals(1, lv.getCount());
		final DictEntry entry = (DictEntry) lv.getItemAtPosition(0);
		assertEquals("(n) (hum) mother/(P)", entry.english);
		assertEquals("母", entry.getJapanese());
	}

	public void testSubstringJapaneseSearch() {
		final SearchQuery q = new SearchQuery(DictTypeEnum.Edict);
		q.isJapanese = true;
		q.matcher = MatcherEnum.Substring;
		q.query = new String[] { RomanizationEnum.Hepburn.toHiragana("haha"), RomanizationEnum.Hepburn.toKatakana("haha") };
		launch(q);
		assertTrue(tester.getText(R.id.textSelectedDictionary).contains("Default"));
		final ListView lv = getActivity().getListView();
		assertEquals(30, lv.getCount());
		final DictEntry entry = (DictEntry) lv.getItemAtPosition(0);
		assertEquals("(n) (hum) mother/(P)", entry.english);
		assertEquals("母", entry.getJapanese());
	}

	/**
	 * Test for the http://code.google.com/p/aedict/issues/detail?id=30 bug. The
	 * problem was that there are ~2500 matches for kyou however only the first
	 * 100 were retrieved from Lucene and they were further filtered.
	 */
	public void testSearchForKyou() {
		final SearchQuery q = new SearchQuery(DictTypeEnum.Edict);
		q.isJapanese = true;
		q.matcher = MatcherEnum.Exact;
		q.query = new String[] { RomanizationEnum.Hepburn.toHiragana("kyou") };
		launch(q);
		assertTrue(tester.getText(R.id.textSelectedDictionary).contains("Default"));
		final ListView lv = getActivity().getListView();
		assertEquals(18, lv.getCount());
		DictEntry entry = (DictEntry) lv.getItemAtPosition(0);
		assertEquals("(n) (1) imperial capital (esp. Kyoto)/(2) final word of an iroha-uta/(3) 10^16/10,000,000,000,000,000/ten quadrillion (American)/(obs) ten thousand billion (British)/(P)", entry.english);
		assertEquals("京", entry.getJapanese());
		entry = (DictEntry) lv.getItemAtPosition(6);
		assertEquals("(n-t) today/this day/(P)", entry.english);
		assertEquals("今日", entry.getJapanese());
	}

	public void testSwitchToRomaji() {
		testSimpleEnglishSearch();
		tester.optionMenu(10004);
		assertTrue(getActivity().showRomaji.isShowingRomaji());
	}

	public void testShowEntryDetail() {
		testSimpleEnglishSearch();
		final ListView lv = getActivity().getListView();
		lv.performItemClick(null, 0, 0);
		tester.assertRequestedActivity(EntryDetailActivity.class);
		final DictEntry entry = (DictEntry) getStartedActivityIntent().getSerializableExtra(EntryDetailActivity.INTENTKEY_ENTRY);
		assertEquals("(n) (hum) mother/(P)", entry.english);
		assertEquals("母", entry.getJapanese());
		assertEquals("はは", entry.reading);
	}

	public void testAddToNotepad() {
		testSimpleEnglishSearch();
		final ListView lv = getActivity().getListView();
		tester.contextMenu(lv, 1, 0);
		tester.assertRequestedActivity(NotepadActivity.class);
		final DictEntry entry = (DictEntry) getStartedActivityIntent().getSerializableExtra(NotepadActivity.INTENTKEY_ADD_ENTRY);
		assertEquals("(n) (hum) mother/(P)", entry.english);
		assertEquals("母", entry.getJapanese());
		assertEquals("はは", entry.reading);
	}

	private void initSimejiSearchEnv() {
		final Intent i = new Intent(getInstrumentation().getContext(), ResultActivity.class);
		i.setAction(ResultActivity.SIMEJI_ACTION_INTERCEPT);
		i.putExtra(ResultActivity.SIMEJI_INTENTKEY_REPLACE, "mother");
		tester.startActivity(i);
		assertTrue(tester.getText(R.id.textSelectedDictionary).contains("Default"));
		final ListView lv = getActivity().getListView();
		final DictEntry entry = (DictEntry) lv.getItemAtPosition(0);
		assertEquals("(n) (hum) mother/(P)", entry.english);
		assertEquals("母", entry.getJapanese());
		assertEquals("はは", entry.reading);
		assertEquals(25, lv.getCount());
	}

	public void testSimejiSearchKanji() {
		initSimejiSearchEnv();
		final ListView lv = getActivity().getListView();
		tester.contextMenu(lv, 2, 0);
		assertSimejiReturn("母");
	}

	public void testSimejiSearchReading() {
		initSimejiSearchEnv();
		final ListView lv = getActivity().getListView();
		tester.contextMenu(lv, 3, 0);
		assertSimejiReturn("はは");
	}

	public void testSimejiSearchEnglish() {
		initSimejiSearchEnv();
		final ListView lv = getActivity().getListView();
		tester.contextMenu(lv, 4, 0);
		assertSimejiReturn("(n) (hum) mother/(P)");
	}

	private void assertSimejiReturn(final String expected) {
		assertEquals(Activity.RESULT_OK, getFinishedActivityRequest());
		assertEquals(expected, tester.getResultIntent().getStringExtra(ResultActivity.SIMEJI_INTENTKEY_REPLACE));
	}

	public void testSimpleEnglishSearchInTanaka() {
		final SearchQuery q = new SearchQuery(DictTypeEnum.Tanaka);
		q.isJapanese = false;
		q.matcher = MatcherEnum.Substring;
		q.query = new String[] { "mother" };
		launch(q);
		assertTrue(tester.getText(R.id.textSelectedDictionary).contains("Tanaka"));
		final ListView lv = getActivity().getListView();
		final DictEntry entry = (DictEntry) lv.getItemAtPosition(0);
		assertEquals("Mother is away from home.", entry.english);
		assertEquals("母は留守です。", entry.getJapanese());
		assertEquals("はははるすです。", entry.reading);
		assertEquals(100, lv.getCount());
	}

	public void testMultiwordEnglishSearchInTanaka() {
		final SearchQuery q = new SearchQuery(DictTypeEnum.Tanaka);
		q.isJapanese = false;
		q.matcher = MatcherEnum.Substring;
		q.query = new String[] { "mother tongue" };
		launch(q);
		assertTrue(tester.getText(R.id.textSelectedDictionary).contains("Tanaka"));
		final ListView lv = getActivity().getListView();
		final DictEntry entry = (DictEntry) lv.getItemAtPosition(0);
		assertEquals("My mother tongue.", entry.english);
		assertEquals("私の母国語。", entry.getJapanese());
		assertEquals("わたしのぼこくご。", entry.reading);
		assertEquals(15, lv.getCount());
	}

	public void testComplexJapaneseSearchInTanaka() {
		final SearchQuery q = new SearchQuery(DictTypeEnum.Tanaka);
		q.isJapanese = true;
		q.matcher = MatcherEnum.Any;
		q.query = new String[] { "母", "はは" };
		launch(q);
		assertTrue(tester.getText(R.id.textSelectedDictionary).contains("Tanaka"));
		final ListView lv = getActivity().getListView();
		final DictEntry entry = (DictEntry) lv.getItemAtPosition(0);
		assertEquals("My mother tongue.", entry.english);
		assertEquals("私の母国語。", entry.getJapanese());
		assertEquals("わたしのぼこくご。", entry.reading);
		assertEquals(100, lv.getCount());
	}

	public void testJapaneseSearchInTanaka() {
		testSimpleJapaneseSearch();
		tester.contextMenu(getActivity().getListView(), 5, 0);
		tester.assertRequestedActivity(ResultActivity.class);
		final SearchQuery q = (SearchQuery) getStartedActivityIntent().getSerializableExtra(ResultActivity.INTENTKEY_SEARCH_QUERY);
		assertEquals("母", q.query[0]);
		assertEquals("はは", q.query[1]);
		assertEquals(2, q.query.length);
		assertTrue(q.isJapanese);
		assertEquals(DictTypeEnum.Tanaka, q.dictType);
		assertEquals(MatcherEnum.Any, q.matcher);
	}

	public void testSodAnalysis() {
		testSimpleJapaneseSearch();
		tester.contextMenu(getActivity().getListView(), 6, 0);
		tester.assertRequestedActivity(StrokeOrderActivity.class);
		final String q = getStartedActivityIntent().getStringExtra(StrokeOrderActivity.INTENTKEY_KANJILIST);
		assertEquals("母", q);
	}
}
