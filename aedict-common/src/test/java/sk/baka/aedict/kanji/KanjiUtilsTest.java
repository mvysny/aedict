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
package sk.baka.aedict.kanji;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the {@link KanjiUtils} class.
 * 
 * @author Martin Vysny
 */
public class KanjiUtilsTest {

    @Test
    public void testKatakana() {
        assertFalse(KanjiUtils.isKatakana('艦'));
        assertFalse(KanjiUtils.isKatakana('か'));
        assertTrue(KanjiUtils.isKatakana('キ'));
        assertTrue(KanjiUtils.isKatakana('ー'));
        assertFalse(KanjiUtils.isKatakana('っ'));
        assertFalse(KanjiUtils.isKatakana('ゃ'));
        assertFalse(KanjiUtils.isKatakana('ゅ'));
        assertFalse(KanjiUtils.isKatakana('ょ'));
        assertTrue(KanjiUtils.isKatakana('ャ'));
        assertTrue(KanjiUtils.isKatakana('ュ'));
        assertTrue(KanjiUtils.isKatakana('ョ'));
    }

    @Test
    public void testKanji() {
        assertTrue(KanjiUtils.isKanji('艦'));
        assertFalse(KanjiUtils.isKanji('か'));
        assertFalse(KanjiUtils.isKanji('キ'));
        assertFalse(KanjiUtils.isKanji('ー'));
        assertFalse(KanjiUtils.isKanji('っ'));
        assertFalse(KanjiUtils.isKanji('ゃ'));
        assertFalse(KanjiUtils.isKanji('ゅ'));
        assertFalse(KanjiUtils.isKanji('ょ'));
        assertFalse(KanjiUtils.isKanji('ャ'));
        assertFalse(KanjiUtils.isKanji('ュ'));
        assertFalse(KanjiUtils.isKanji('ョ'));
    }

    @Test
    public void testHiragana() {
        assertFalse(KanjiUtils.isHiragana('艦'));
        assertTrue(KanjiUtils.isHiragana('か'));
        assertFalse(KanjiUtils.isHiragana('キ'));
        assertFalse(KanjiUtils.isHiragana('ー'));
        assertTrue(KanjiUtils.isHiragana('っ'));
        assertTrue(KanjiUtils.isHiragana('ゃ'));
        assertTrue(KanjiUtils.isHiragana('ゅ'));
        assertTrue(KanjiUtils.isHiragana('ょ'));
        assertFalse(KanjiUtils.isHiragana('ャ'));
        assertFalse(KanjiUtils.isHiragana('ュ'));
        assertFalse(KanjiUtils.isHiragana('ョ'));
    }

    @Test
    public void testHalfwidthKatakana() {
        assertFalse(KanjiUtils.isHalfwidth('艦'));
        assertFalse(KanjiUtils.isHalfwidth('か'));
        assertFalse(KanjiUtils.isHalfwidth('キ'));
        assertTrue(KanjiUtils.isHalfwidth('ｶ'));
    }

    @Test
    public void testToHalfwidthKatakana() {
        assertEquals("ﾊﾟﾊﾟ", KanjiUtils.toHalfwidth("パパ"));
        assertEquals("ｺﾝﾋﾟｭｰﾀｰ", KanjiUtils.toHalfwidth("コンピューター"));
        assertEquals("JOZOﾊﾟﾊﾟFOO", KanjiUtils.toHalfwidth("JOZOパパFOO"));
        assertEquals("FOOBARBAZ", KanjiUtils.toHalfwidth("FOOBARBAZ"));
    }

    @Test
    public void testToFullwidthKatakana() {
        assertEquals("パパ", KanjiUtils.halfwidthToKatakana("ﾊﾟﾊﾟ"));
        assertEquals("コンピューター", KanjiUtils.halfwidthToKatakana("ｺﾝﾋﾟｭｰﾀｰ"));
        assertEquals("JOZOパパFOO", KanjiUtils.halfwidthToKatakana("JOZOﾊﾟﾊﾟFOO"));
        assertEquals("FOOBARBAZ", KanjiUtils.halfwidthToKatakana("FOOBARBAZ"));
    }

    @Test
    public void testJlpt() {
        assertEquals((Integer) 5, KanjiUtils.getJlptLevel('山'));
        assertEquals((Integer) 5, KanjiUtils.getJlptLevel('週'));
        assertEquals((Integer) 3, KanjiUtils.getJlptLevel('商'));
        assertEquals((Integer) 3, KanjiUtils.getJlptLevel('司'));
        assertEquals((Integer) 2, KanjiUtils.getJlptLevel('可'));
        assertEquals((Integer) 2, KanjiUtils.getJlptLevel('庁'));
    }
}
