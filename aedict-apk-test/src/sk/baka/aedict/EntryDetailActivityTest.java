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

	private void startActivity(String kanji, String reading, String english) {
		final Intent i = new Intent(getInstrumentation().getTargetContext(), EntryDetailActivity.class);
		i.putExtra(EntryDetailActivity.INTENTKEY_ENTRY, new DictEntry(kanji, reading, english));
		tester.startActivity(i);
	}
}
