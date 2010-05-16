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

import java.util.ArrayList;
import java.util.Arrays;
import static sk.baka.tools.test.Assert.*;
import static org.junit.Assert.*;

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
        assertArrayEquals(e.getMarkings(), markings("v5u", "vt"));
        final List<String> markings = new ArrayList<String>();
        final int result = EdictEntry.findMarkings("(v5u,vt) (1) (vulg) to eat; to drink; (2) to receive (e.g. a blow);", markings);
        assertArrayEquals(markings, markings("v5u", "vt"));
        assertEquals(9, result);
    }

    private static List<String> markings(final String... markings) {
        return Arrays.asList(markings);
    }

    @Test
    public void testNotVerb() {
        final EdictEntry e = new EdictEntry(null, "チェコスロバキア", "(n) Czechoslovakia; (P);", false);
        assertFalse(e.isVerb());
        assertFalse(e.isGodan());
        assertFalse(e.isIchidan());
        assertFalse(e.isKuru());
        assertFalse(e.isSuru());
    }

    @Test
    public void testIsIchidan() {
        EdictEntry e = new EdictEntry("食い過ぎる", "くいすぎる", "(v1,vi) to eat too much; to overeat;", false);
        assertTrue(e.isVerb());
        assertFalse(e.isGodan());
        assertTrue(e.isIchidan());
        assertFalse(e.isKuru());
        assertFalse(e.isSuru());
    }

    @Test
    public void testIsGodan() {
        EdictEntry e = new EdictEntry(
                "翫ぶ",
                "もてあそぶ",
                "(v5b,vt) (1) (uk) to play with (a toy, one's hair, etc.); to fiddle with; (2) to toy with (one's emotions, etc.); to trifle with; (3) to do with something as one pleases; (4) to appreciate;",
                false);
        assertTrue(e.isVerb());
        assertTrue(e.isGodan());
        assertFalse(e.isIchidan());
        assertFalse(e.isKuru());
        assertFalse(e.isSuru());
    }

    @Test
    public void testIsKuru() {
        EdictEntry e = new EdictEntry(
                "来る",
                "くる",
                "(vk,vi,aux-v) (1) to come (spatially or temporally); to approach; to arrive; (2) to come back; to do ... and come back; (3) to come to be; to become; to get; to grow; to continue; (vk,vi) (4) to come from; to be caused by; to derive from; (5) to come to (i.e. \"when it comes to spinach ...\"); (P);",
                false);
        assertTrue(e.isVerb());
        assertFalse(e.isGodan());
        assertFalse(e.isIchidan());
        assertTrue(e.isKuru());
        assertFalse(e.isSuru());
    }

    @Test
    public void testIsSuru() {
        EdictEntry e = new EdictEntry(
                "為る",
                "する",
                "(vs-i) (1) (uk) to do; (2) to cause to become; to make (into); to turn (into); (3) to serve as; to act as; to work as; (4) to wear (clothes, a facial expression, etc.); (5) to judge as being; to view as being; to think of as; to treat as; to use as; (6) to decide on; to choose; (vs-i,vi) (7) to be sensed (of a smell, noise, etc.); (8) to be (in a state, condition, etc.); (9) to be worth; to cost; (10) to pass (of time); to elapse; (suf,vs-i) (11) verbalizing suffix (applies to nouns noted in this dictionary with the part of speech \"vs\"); (aux-v,vs-i) (12) creates a humble verb (after a noun prefixed with \"o\" or \"go\"); (13) to be just about to; to be just starting to; to try to; to attempt to; (P);",
                false);
        assertTrue(e.isVerb());
        assertFalse(e.isGodan());
        assertFalse(e.isIchidan());
        assertFalse(e.isKuru());
        assertTrue(e.isSuru());
    }

    @Test
    public void testFindSenseNumber() {
        assertNull(EdictEntry.findSenseNumber("foo"));
        assertEquals(4, EdictEntry.findSenseNumber("foo (4) kraa")[0]);
        assertEquals(7, EdictEntry.findSenseNumber("foo (4) kraa")[1]);
        assertNull(EdictEntry.findSenseNumber("foo (4a) kraa"));
        assertEquals(3, EdictEntry.findSenseNumber("(3)")[0]);
        assertEquals(3, EdictEntry.findSenseNumber("(3)")[1]);
    }

    @Test
    public void getSenses() {
        EdictEntry e = new EdictEntry(
                "翫ぶ",
                "もてあそぶ",
                "(v5b,vt) (1) (uk) to play with (a toy, one's hair, etc.); to fiddle with; (2) to toy with (one's emotions, etc.); to trifle with; (3) to do with something as one pleases; (4) to appreciate;",
                false);
        final List<List<String>> senses = e.getSenses();
        assertEquals("(uk) to play with (a toy, one's hair, etc.)", senses.get(0).get(0));
        assertEquals("to fiddle with", senses.get(0).get(1));
        assertEquals(2, senses.get(0).size());
        assertEquals("to toy with (one's emotions, etc.)", senses.get(1).get(0));
        assertEquals("to trifle with", senses.get(1).get(1));
        assertEquals(2, senses.get(1).size());
        assertEquals("to do with something as one pleases", senses.get(2).get(0));
        assertEquals(1, senses.get(2).size());
        assertEquals("to appreciate", senses.get(3).get(0));
        assertEquals(1, senses.get(3).size());
        assertEquals(4, senses.size());
    }
}
