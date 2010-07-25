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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import sk.baka.aedict.kanji.RomanizationEnum;

/**
 * Tests the {@link RomanizationEnum} class.
 * @author Martin Vysny
 */
public class RomanizationEnumTest {

    /**
     * Test method for {@link sk.baka.aedict.kanji.RomanizationEnum#toHiragana(java.lang.String)}.
     */
    @Test
    public void testHepburnToHiragana() {
        assertEquals("はは", RomanizationEnum.Hepburn.toHiragana("haha"));
        assertEquals("よこはま", RomanizationEnum.Hepburn.toHiragana("yokohama"));
        assertEquals("ずっと", RomanizationEnum.Hepburn.toHiragana("zutto"));
        assertEquals("おちゃ", RomanizationEnum.Hepburn.toHiragana("ocha"));
        assertEquals("そんな", RomanizationEnum.Hepburn.toHiragana("sonna"));
        assertEquals("そんんあ", RomanizationEnum.Hepburn.toHiragana("sonxna"));
        assertEquals("んあ", RomanizationEnum.Hepburn.toHiragana("xna"));
    }

    @Test
    public void testNihonShikiToHiragana() {
        assertEquals("はは", RomanizationEnum.NihonShiki.toHiragana("haha"));
        assertEquals("よこはま", RomanizationEnum.NihonShiki.toHiragana("yokohama"));
        assertEquals("ずっと", RomanizationEnum.NihonShiki.toHiragana("zutto"));
        assertEquals("おちゃ", RomanizationEnum.NihonShiki.toHiragana("otya"));
        assertEquals("そんな", RomanizationEnum.NihonShiki.toHiragana("sonna"));
        assertEquals("そんんあ", RomanizationEnum.NihonShiki.toHiragana("sonxna"));
        assertEquals("んあ", RomanizationEnum.NihonShiki.toHiragana("xna"));
    }

    /**
     * Test method for {@link sk.baka.aedict.kanji.RomanizationEnum#toKatakana(java.lang.String)}.
     */
    @Test
    public void testHepburnToKatakana() {
        assertEquals("ミニコン", RomanizationEnum.Hepburn.toKatakana("minikon"));
        assertEquals("コンピュータ", RomanizationEnum.Hepburn.toKatakana("konpyuuta"));
        assertEquals("ボッブ", RomanizationEnum.Hepburn.toKatakana("bobbu"));
        assertEquals("ソンナ", RomanizationEnum.Hepburn.toKatakana("sonna"));
        assertEquals("ソンンア", RomanizationEnum.Hepburn.toKatakana("sonxna"));
        assertEquals("ンア", RomanizationEnum.Hepburn.toKatakana("xna"));
    }

    @Test
    public void testNihonShikiToKatakana() {
        assertEquals("ミニコン", RomanizationEnum.NihonShiki.toKatakana("minikon"));
        assertEquals("コンピュータ", RomanizationEnum.NihonShiki.toKatakana("konpyuuta"));
        assertEquals("ボッブ", RomanizationEnum.NihonShiki.toKatakana("bobbu"));
        assertEquals("ソンナ", RomanizationEnum.NihonShiki.toKatakana("sonna"));
        assertEquals("ンア", RomanizationEnum.NihonShiki.toKatakana("xna"));
    }

    /**
     * Test method for {@link sk.baka.aedict.kanji.RomanizationEnum#toRomaji(java.lang.String)}.
     */
    @Test
    public void testHepburnToRomaji() {
        assertEquals("haha", RomanizationEnum.Hepburn.toRomaji("はは"));
        assertEquals("yokohama", RomanizationEnum.Hepburn.toRomaji("よこはま"));
        assertEquals("zutto", RomanizationEnum.Hepburn.toRomaji("ずっと"));
        assertEquals("minikon", RomanizationEnum.Hepburn.toRomaji("ミニコン"));
        assertEquals("konpyuuta", RomanizationEnum.Hepburn.toRomaji("コンピュータ"));
        assertEquals("ocha", RomanizationEnum.Hepburn.toRomaji("おちゃ"));
        assertEquals("bobbu", RomanizationEnum.Hepburn.toRomaji("ボッブ"));
        assertEquals("sonna", RomanizationEnum.Hepburn.toRomaji("そんな"));
        assertEquals("sonna", RomanizationEnum.Hepburn.toRomaji("そんんあ"));
        assertEquals("na", RomanizationEnum.Hepburn.toRomaji("んあ"));
    }

    @Test
    public void testNihonShikiToRomaji() {
        assertEquals("haha", RomanizationEnum.NihonShiki.toRomaji("はは"));
        assertEquals("yokohama", RomanizationEnum.NihonShiki.toRomaji("よこはま"));
        assertEquals("zutto", RomanizationEnum.NihonShiki.toRomaji("ずっと"));
        assertEquals("minikon", RomanizationEnum.NihonShiki.toRomaji("ミニコン"));
        assertEquals("konpyuuta", RomanizationEnum.NihonShiki.toRomaji("コンピュータ"));
        assertEquals("otya", RomanizationEnum.NihonShiki.toRomaji("おちゃ"));
        assertEquals("bobbu", RomanizationEnum.NihonShiki.toRomaji("ボッブ"));
        assertEquals("sonna", RomanizationEnum.NihonShiki.toRomaji("そんな"));
        assertEquals("sonna", RomanizationEnum.NihonShiki.toRomaji("そんんあ"));
        assertEquals("na", RomanizationEnum.NihonShiki.toRomaji("んあ"));
    }
}
