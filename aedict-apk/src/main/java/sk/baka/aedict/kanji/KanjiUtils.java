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

/**
 * Utility methods for kanji characters
 * 
 * @author Martin Vysny
 */
public final class KanjiUtils {
	private KanjiUtils() {
		throw new AssertionError();
	}

	/**
	 * A very simple check for kanji. Works only on a mixture of kanji, katakana
	 * and hiragana.
	 * 
	 * @param c
	 *            the character to analyze.
	 * @return true if it is a kanji, false otherwise.
	 */
	public static boolean isKanji(char c) {
		return RomanizationEnum.Hepburn.toRomaji(c).charAt(0) == c;
	}

	/**
	 * A very simple check for hiragana characters.
	 * 
	 * @param c
	 *            the character to analyze.
	 * @return true if it is a hiragana character, false otherwise.
	 */
	public static boolean isHiragana(char c) {
		final String romaji = RomanizationEnum.Hepburn.toRomaji(c);
		if (romaji.charAt(0) == c) {
			// kanji
			return false;
		}
		final char c1 = RomanizationEnum.Hepburn.toHiragana(romaji).charAt(0);
		return c1 == c;
	}

	/**
	 * A very simple check for hiragana characters.
	 * 
	 * @param c
	 *            the character to analyze.
	 * @return true if it is a hiragana character, false otherwise.
	 */
	public static boolean isKatakana(char c) {
		final String romaji = RomanizationEnum.Hepburn.toRomaji(c);
		if (romaji.charAt(0) == c) {
			// kanji
			return false;
		}
		final char c1 = RomanizationEnum.Hepburn.toKatakana(romaji).charAt(0);
		return c1 == c;
	}
}
