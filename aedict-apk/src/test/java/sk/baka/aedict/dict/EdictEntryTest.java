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

import static sk.baka.tools.test.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Tests the {@link EdictEntry} class.
 * 
 * @author Martin Vysny
 */
public class EdictEntryTest {
	@Test
	public void testMarkings() {
		final EdictEntry e = new EdictEntry("喰らう", "くらう", "(v5u,vt) (1) (vulg) to eat; to drink; (2) to receive (e.g. a blow);", false);
		assertArrayEquals(e.getMarkings(), markings("v5u", "vt", "vulg"));
	}

	private static List<EdictEntry.Marking> markings(final String... markings) {
		final List<EdictEntry.Marking> result = new ArrayList<EdictEntry.Marking>();
		for (final String m : markings) {
			result.add(new EdictEntry.Marking(m, 0));
		}
		return result;
	}
}
