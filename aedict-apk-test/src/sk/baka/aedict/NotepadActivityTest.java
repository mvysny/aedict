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

import java.util.HashSet;
import java.util.Set;

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DictEntry;
import android.content.Intent;

/**
 * Tests the {@link NotepadActivity} class.
 * 
 * @author Martin Vysny
 */
public class NotepadActivityTest extends AbstractAedictTest<NotepadActivity> {

	public NotepadActivityTest() {
		super(NotepadActivity.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// set the configuration to a known state
		final Config cfg = AedictApp.getConfig();
		cfg.setNotepadItems(new DictEntry("母は留守です。", "はははです", "Mother is.").toExternal());
	}

	public void testSimpleStart() {
		tester.startActivity();
	}

	public void testAnalyze() {
		tester.startActivity();
		tester.contextMenu(getActivity().getListView(), 10000, 0);
		tester.assertRequestedActivity(KanjiAnalyzeActivity.class);
		assertEquals("母は留守です。", getStartedActivityIntent().getStringExtra(KanjiAnalyzeActivity.INTENTKEY_WORD));
	}

	public void testDelete() {
		tester.startActivity();
		tester.contextMenu(getActivity().getListView(), 10002, 0);
		assertEquals(0, getActivity().getListAdapter().getCount());
	}

	public void testDeleteAll() {
		tester.startActivity();
		tester.contextMenu(getActivity().getListView(), 10003, 0);
		assertEquals(0, getActivity().getListAdapter().getCount());
	}

	public void testShowSod() {
		tester.startActivity();
		tester.contextMenu(getActivity().getListView(), 10004, 0);
		tester.assertRequestedActivity(StrokeOrderActivity.class);
		final String q = getStartedActivityIntent().getStringExtra(StrokeOrderActivity.INTENTKEY_KANJILIST);
		assertEquals("母は留守です。", q);
	}

	public void testAddEntry() {
		final Intent intent = new Intent(getInstrumentation().getTargetContext(), NotepadActivity.class);
		intent.putExtra(NotepadActivity.INTENTKEY_ADD_ENTRY, new DictEntry("母", "はは", "mother"));
		tester.startActivity(intent);
		DictEntry e = (DictEntry) getActivity().getListAdapter().getItem(0);
		assertEquals(new DictEntry("母は留守です。", "はははです", "Mother is."), e);
		e = (DictEntry) getActivity().getListAdapter().getItem(1);
		assertEquals(new DictEntry("母", "はは", "mother"), e);
		assertEquals(2, getActivity().getListAdapter().getCount());
		assertEquals(new DictEntry("母は留守です。", "はははです", "Mother is.").toExternal() + "@@@@" + new DictEntry("母", "はは", "mother").toExternal(), AedictApp.getConfig().getNotepadItems());
	}
}
