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
package sk.baka.aedict.dict;

import org.junit.Test;
import static sk.baka.tools.test.Assert.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link DictEntry} class.
 * 
 * @author Martin Vysny
 */
public class DictEntryTest {
	@Test
	public void testEquals() {
		// we will cheat a bit here: regularly, a subclass cannot be equal to
		// its parent under any circumstances. However, DictEntry is an
		// exception as multiple DictEntry types may be intermixed in a single
		// Collection.
		assertCorrectEquals(new DictEntry("kanji", "reading", "english"), new DictEntry("kanji", "reading2", "english"), new DictEntry("kanji2", "reading", "english"), new EdictEntry("CHEAT",
				"CHEAT", "CHEAT", false));
		assertCorrectEquals(new DictEntry(null, "reading", "english"), new DictEntry(null, "reading", "english"), new DictEntry(null, "reading", "english2"), new EdictEntry("CHEAT",
				"CHEAT", "CHEAT", false));
	}

	@Test
	public void testExternalize() {
		checkExternalize(new DictEntry("kanji", "reading", "english"));
		checkExternalize(new DictEntry(null, "reading", "e"));
		checkExternalize(new DictEntry(null, null, "e"));
		checkExternalize(new DictEntry("kanji", null, "foo"));
		checkExternalize(new DictEntry("foo", null, "bar"));
	}

	private void checkExternalize(final DictEntry entry) {
		assertEquals(entry, DictEntry.fromExternal(entry.toExternal()));
	}
}
