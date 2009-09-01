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

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the {@link JpUtils} class.
 * @author Martin Vysny
 */
public class JpUtilsTest {

	@BeforeClass
	public static void initialize() throws IOException{
		JpUtils.initialize(Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * Test method for {@link sk.baka.aedict.JpUtils#toHiragana(java.lang.String)}.
	 */
	@Test
	public void testToHiragana() {
		assertEquals("はは", JpUtils.toHiragana("haha"));
		assertEquals("よこはま", JpUtils.toHiragana("yokohama"));
		assertEquals("ずっと", JpUtils.toHiragana("zutto"));
	}

	/**
	 * Test method for {@link sk.baka.aedict.JpUtils#toKatakana(java.lang.String)}.
	 */
	@Test
	public void testToKatakana() {
		assertEquals("ミニコン", JpUtils.toKatakana("minikon"));
		assertEquals("コンピュータ", JpUtils.toKatakana("konpyuuta"));
	}

	/**
	 * Test method for {@link sk.baka.aedict.JpUtils#toRomaji(java.lang.String)}.
	 */
	@Test
	public void testToRomaji() {
		assertEquals("haha", JpUtils.toRomaji("はは"));
		assertEquals("yokohama", JpUtils.toRomaji("よこはま"));
		assertEquals("zutto", JpUtils.toRomaji("ずっと"));
		assertEquals("minikon", JpUtils.toRomaji("ミニコン"));
		assertEquals("konpyuuta", JpUtils.toRomaji("コンピュータ"));
	}
}
