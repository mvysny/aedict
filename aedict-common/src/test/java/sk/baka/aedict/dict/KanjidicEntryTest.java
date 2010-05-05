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

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import static sk.baka.tools.test.Assert.*;

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
        new KanjidicEntry("KAAAAA", "Reading", "English", 1, 2, "3-4-5", 4);
    }

    private KanjidicEntry test1() {
        return new KanjidicEntry("愛", "アイ, いと.しい, かな.しい, め.でる, お.しむ, まな, [あ, あし, え, かな, なる, めぐ, めぐみ, よし, ちか]", "love, affection, favourite", 1, 2, "3-4-5", 6);
    }

    private KanjidicEntry test2() {
        return new KanjidicEntry("方", "ホウ, かた, -かた, -がた, [から, な, なた, ふさ, まさ, みち, も, わ]", "direction, person, alternative", 1, 2, "3-4-5", 6);
    }

    @Test
    public void testEnglish() {
        assertArrayEquals(test1().getEnglish(), Arrays.asList("love", "affection", "favourite"));
        assertArrayEquals(test2().getEnglish(), Arrays.asList("direction", "person", "alternative"));
    }

    @Test
    public void testOnyomi() {
        assertArrayEquals(test1().getOnyomi(), Arrays.asList("アイ"));
        assertArrayEquals(test2().getOnyomi(), Arrays.asList("ホウ"));
    }

    @Test
    public void testKunyomi() {
        assertArrayEquals(test1().getKunyomi(), Arrays.asList("いと.しい", "かな.しい", "め.でる", "お.しむ", "まな"));
        assertArrayEquals(test2().getKunyomi(), Arrays.asList("かた", "-かた", "-がた"));
    }

    @Test
    public void testNamae() {
        assertArrayEquals(test1().getNamae(), Arrays.asList("あ", "あし", "え", "かな", "なる", "めぐ", "めぐみ", "よし", "ちか"));
        assertArrayEquals(test2().getNamae(), Arrays.asList("から", "な", "なた", "ふさ", "まさ", "みち", "も", "わ"));
    }

    @Test
    public void testRemoveSplits() {
        assertEquals("いとしい", KanjidicEntry.removeSplits("いと.しい"));
        assertEquals("かた", KanjidicEntry.removeSplits("-かた"));
    }
}
