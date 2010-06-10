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
import android.content.Intent;
/**
 * Tests the {@link KanjiAnalyzeActivity} class.
 * @author Martin Vysny
 */
public class KanjiAnalyzeActivityTest extends AbstractAedictTest<KanjiAnalyzeActivity> {

	public KanjiAnalyzeActivityTest() {
		super(KanjiAnalyzeActivity.class);
	}

	public void testStartActivity() {
		startActivity("母");
	}

	private void startActivity(final String word) {
		startActivity(word, false);
	}

	private void startActivity(final String word, final boolean isAnalysisPerWord) {
		final Intent i = new Intent(getInstrumentation().getContext(), KanjiAnalyzeActivity.class);
		i.putExtra(KanjiAnalyzeActivity.INTENTKEY_WORD, word);
		i.putExtra(KanjiAnalyzeActivity.INTENTKEY_WORD_ANALYSIS, isAnalysisPerWord);
		tester.startActivity(i);
	}

	public void testAnalyzeAsWords() {
		startActivity("母上");
		assertEquals(2, getActivity().getListAdapter().getCount());
		assertEquals("母", get(0).getJapanese());
		assertEquals("mama, mother", get(0).english);
		assertEquals("上", get(1).getJapanese());
		assertEquals("above, up", get(1).english);
		tester.optionMenu(10000);
		assertEquals(1, getActivity().getListAdapter().getCount());
		assertEquals("母上", get(0).getJapanese());
	}

	/**
	 * If this test fails then probably the kanjidic dictionary is not
	 * downloaded.
	 */
	public void testAnalyzeYomu() {
		startActivity("読");
		assertEquals(1, getActivity().getListAdapter().getCount());
		assertEquals("読", get(0).getJapanese());
		assertEquals("read", get(0).english);
	}

	public void testIndexOutOfBoundsThrownWhenASmallTsuIsAnalyzed() {
		startActivity("っ");
		assertEquals(1, getActivity().getListAdapter().getCount());
		assertEquals("っ", get(0).getJapanese());
		assertEquals("", get(0).english);
	}

	public void testAddToNotepad() {
		startActivity("母上");
		tester.contextMenu(getActivity().getListView(), 10000, 0);
		tester.assertRequestedActivity(NotepadActivity.class);
		assertEquals("母", ((DictEntry)getStartedActivityIntent().getSerializableExtra(NotepadActivity.INTENTKEY_ADD_ENTRY)).getJapanese());
	}

	/**
	 * Tests for http://code.google.com/p/aedict/issues/detail?id=35
	 */
	public void testWordAnalysis() {
		startActivity("今週のおすすめﾊﾞｰｹﾞﾝTVｹﾞｰﾑ", true);
		assertEquals("今週", get(0).getJapanese());
		assertEquals("の", get(1).reading);
		assertEquals("おすすめ", get(2).reading);
		assertEquals("バーゲン", get(3).getJapanese());
		assertEquals("T", get(4).getJapanese());
		assertEquals("V", get(5).getJapanese());
		assertEquals("ゲーム", get(6).getJapanese());
		assertEquals(7, getActivity().getListAdapter().getCount());
	}

	public void testWordAnalysisLaunchesEdictDetailView() {
		startActivity("今週のおすすめﾊﾞｰｹﾞﾝTVｹﾞｰﾑ", true);
		assertEquals("おすすめ", get(2).reading);
		tester.click(android.R.id.list, 2);
		tester.assertRequestedActivity(EdictEntryDetailActivity.class);
	}

	/**
	 * Tests for http://code.google.com/p/aedict/issues/detail?id=69
	 */
	public void testClickOnHiraganaCharacterDoesNothing() {
		startActivity("今週のおすすめﾊﾞｰｹﾞﾝTVｹﾞｰﾑ", false);
		assertEquals("の", get(2).reading);
		tester.click(android.R.id.list, 2);
		tester.assertNoRequestedActivity();
	}
	
	private DictEntry get(int i) {
		return (DictEntry) getActivity().getListAdapter().getItem(i);
	}

	/**
	 * Tests for the SOD functionality.
	 */
	public void testShowSod() {
		startActivity("今週のおすすめﾊﾞｰｹﾞﾝTVｹﾞｰﾑ", false);
		tester.contextMenu(getActivity().getListView(), 10001, 0);
		tester.assertRequestedActivity(StrokeOrderActivity.class);
		final String q = getStartedActivityIntent().getStringExtra(StrokeOrderActivity.INTENTKEY_KANJILIST);
		assertEquals("今", q);
	}
}
