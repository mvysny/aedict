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

import org.junit.Test;

/**
 * Tests the {@link RomanizationEnum} class.
 * @author Martin Vysny
 */
public class JpUtilsTest {
	
	/**
	 * Test method for {@link sk.baka.aedict.RomanizationEnum#toHiragana(java.lang.String)}.
	 */
	@Test
	public void testHepburnToHiragana() {
		assertEquals("はは", RomanizationEnum.Hepburn.toHiragana("haha"));
		assertEquals("よこはま", RomanizationEnum.Hepburn.toHiragana("yokohama"));
		assertEquals("ずっと", RomanizationEnum.Hepburn.toHiragana("zutto"));
	}

	/**
	 * Test method for {@link sk.baka.aedict.RomanizationEnum#toKatakana(java.lang.String)}.
	 */
	@Test
	public void testHepburnToKatakana() {
		assertEquals("ミニコン", RomanizationEnum.Hepburn.toKatakana("minikon"));
		assertEquals("コンピュータ", RomanizationEnum.Hepburn.toKatakana("konpyuuta"));
	}

	/**
	 * Test method for {@link sk.baka.aedict.RomanizationEnum#toRomaji(java.lang.String)}.
	 */
	@Test
	public void testHepburnToRomaji() {
		assertEquals("haha", RomanizationEnum.Hepburn.toRomaji("はは"));
		assertEquals("yokohama", RomanizationEnum.Hepburn.toRomaji("よこはま"));
		assertEquals("zutto", RomanizationEnum.Hepburn.toRomaji("ずっと"));
		assertEquals("minikon", RomanizationEnum.Hepburn.toRomaji("ミニコン"));
		assertEquals("konpyuuta", RomanizationEnum.Hepburn.toRomaji("コンピュータ"));
	}
}
