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
public class KanjiAnalyzeActivityTest extends ActivityTestHelper<KanjiAnalyzeActivity> {

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
		startActivity(i);
	}

	public void testAnalyzeAsWords() {
		startActivity("母上");
		assertEquals(2, getActivity().getListAdapter().getCount());
		assertEquals("母", get(0).getJapanese());
		assertEquals("mama, mother", get(0).english);
		assertEquals("上", get(1).getJapanese());
		assertEquals("above, up", get(1).english);
		activateOptionsMenu(1);
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
		contextMenu(getActivity().getListView(), 10001, 0);
		assertStartedActivity(NotepadActivity.class);
		assertEquals("母", ((DictEntry) getStartedActivityIntent().getExtras().get(NotepadActivity.INTENTKEY_ADD_ENTRY)).getJapanese());
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

	private DictEntry get(int i) {
		return (DictEntry) getActivity().getListAdapter().getItem(i);
	}
}
