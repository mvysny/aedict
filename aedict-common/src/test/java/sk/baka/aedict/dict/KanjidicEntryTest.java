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
import static org.junit.Assert.*;

/**
 * Tests the {@link KanjidicEntry} class.
 * @author Martin Vysny
 */
public class KanjidicEntryTest {

    @Test
    public void testConstructor() {
        final KanjidicEntry k = new KanjidicEntry("K", "Reading", "English", 1, 2, "3-4-5", 4);
        assertEquals("K", k.kanji);
        assertEquals('K', k.getKanji());
        assertEquals("Reading", k.reading);
        assertEquals("English", k.english);
        assertEquals(1, k.radical);
        assertEquals(2, k.strokes);
        assertEquals("3-4-5", k.skip);
        assertEquals((Integer) 4, k.grade);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidKanji() {
        final KanjidicEntry k = new KanjidicEntry("KAAAAA", "Reading", "English", 1, 2, "3-4-5", 4);
    }
}
