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
import sk.baka.aedict.dict.EdictEntry;
import android.app.SearchManager;
import android.content.Intent;

/**
 * Tests the {@link GlobalSearchHandler} class.
 * 
 * @author Martin Vysny
 */
public class GlobalSearchHandlerTest extends AbstractAedictTest<GlobalSearchHandler> {

	public GlobalSearchHandlerTest() {
		super(GlobalSearchHandler.class);
	}

	/**
	 * Tests http://code.google.com/p/aedict/issues/detail?id=59
	 */
	public void testHandlerProvidesEdictEntry() {
		final Intent i = new Intent(getInstrumentation().getTargetContext(), EdictEntryDetailActivity.class);
		i.putExtra(SearchManager.EXTRA_DATA_KEY, new DictEntry("Kanji", "Reading", "English (P)").toExternal());
		tester.startActivity(i);
		tester.assertRequestedActivity(EdictEntryDetailActivity.class);
		final EdictEntry ee = (EdictEntry) getStartedActivityIntent().getSerializableExtra(EdictEntryDetailActivity.INTENTKEY_ENTRY);
		assertEquals("Kanji", ee.kanji);
		assertEquals("Reading", ee.reading);
		assertEquals("English (P)", ee.english);
		assertTrue(ee.isCommon);
	}
}
