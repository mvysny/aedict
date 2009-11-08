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

import static org.junit.Assert.assertEquals;
import java.text.ParseException;
import org.junit.Test;

/**
 * Tests the {@link EdictEntry} class.
 * 
 * @author Martin Vysny
 */
public class EdictEntryTest {
	@Test
	public void simpleKatakanaParse() throws ParseException {
		final EdictEntry e = EdictEntry.parseEdict("aaa / bbb");
		assertEquals("aaa", e.reading);
		assertEquals("bbb", e.english);
	}

	@Test
	public void simpleHiraganaParse() throws ParseException {
		final EdictEntry e = EdictEntry.parseEdict("aaa [ccc] / bbb");
		assertEquals("aaa", e.kanji);
		assertEquals("ccc", e.reading);
		assertEquals("bbb", e.english);
	}

	@Test
	public void parserDropsTrailingSlashes() throws ParseException {
		final EdictEntry e = EdictEntry.parseEdict("aaa [ccc] / bbb//");
		assertEquals("aaa", e.kanji);
		assertEquals("ccc", e.reading);
		assertEquals("bbb", e.english);
	}

	@Test(expected = ParseException.class)
	public void simpleUnsuccessfullParse() throws ParseException {
		EdictEntry.parseEdict("aaa");
	}
}
