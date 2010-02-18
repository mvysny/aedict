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

public class KanjiAnalyzeActivityTest extends ActivityTestHelper<KanjiAnalyzeActivity> {

	public KanjiAnalyzeActivityTest() {
		super(KanjiAnalyzeActivity.class);
	}

	public void testStartActivity() {
		startActivity("母");
	}

	private void startActivity(final String word) {
		final Intent i = new Intent(getInstrumentation().getContext(), KanjiAnalyzeActivity.class);
		i.putExtra(KanjiAnalyzeActivity.INTENTKEY_WORD, word);
		startActivity(i);
	}

	public void testAnalyzeAsWords() {
		startActivity("母上");
		assertEquals(2, getActivity().getListAdapter().getCount());
		assertEquals("母", get(0).getJapanese());
		assertEquals("上", get(1).getJapanese());
		activateOptionsMenu(1);
		assertEquals(1, getActivity().getListAdapter().getCount());
		assertEquals("母上", get(0).getJapanese());
	}

	public void testAddToNotepad() {
		startActivity("母上");
		contextMenu(getActivity().getListView(), 10001, 0);
		assertStartedActivity(NotepadActivity.class);
		assertEquals("母", ((DictEntry) getStartedActivityIntent().getExtras().get(NotepadActivity.INTENTKEY_ADD_ENTRY)).getJapanese());
	}

	private DictEntry get(int i) {
		return (DictEntry) getActivity().getListAdapter().getItem(i);
	}
}
