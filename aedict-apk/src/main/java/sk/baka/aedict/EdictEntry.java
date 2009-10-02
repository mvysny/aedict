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

import java.text.ParseException;

/**
 * Represents a parsed EDict entry. Immutable.
 * 
 * @author Martin Vysny
 */
public final class EdictEntry {
	/**
	 * The kanji expression, may be null if the entry does not contain any kanji
	 */
	public final String kanji;
	/**
	 * The reading, in hiragana or katakana.
	 */
	public final String reading;
	/**
	 * The english translation
	 */
	public final String english;

	/**
	 * Creates new entry instance.
	 * 
	 * @param kanji
	 *            the kanji expression, may be null if the entry does not
	 *            contain any kanji
	 * @param reading
	 *            the reading, in hiragana or katakana.
	 * @param english
	 *            the english translation
	 */
	public EdictEntry(final String kanji, final String reading, final String english) {
		this.kanji = kanji;
		this.reading = reading;
		this.english = english;
	}

	/**
	 * Parses given entry.
	 * 
	 * @param edictEntry
	 *            the entry to parse.
	 * @return a parsed entry
	 * @throws ParseException
	 *             if the parsing fails
	 */
	public static EdictEntry parse(final String edictEntry) throws ParseException {
		// the entry is in one of the two following formats:
		// KANJI [hiragana] / english meaning
		// katakana / english meaning
		final int firstSlash = edictEntry.indexOf('/');
		if (firstSlash < 0) {
			throw new ParseException("Failed to parse " + edictEntry + ": missing slash", 0);
		}
		final String englishPart = edictEntry.substring(firstSlash + 1).trim();
		final String jpPart = edictEntry.substring(0, firstSlash).trim();
		final int openSquareBracket = jpPart.indexOf('[');
		final String kanji;
		final String reading;
		if (openSquareBracket < 0) {
			// just a katakana reading, no kanji
			kanji = null;
			reading = jpPart;
		} else {
			kanji = jpPart.substring(0, openSquareBracket).trim();
			final int closingSquareBracket = jpPart.indexOf(']');
			reading = jpPart.substring(openSquareBracket + 1, closingSquareBracket).trim();
		}
		return new EdictEntry(kanji, reading, englishPart);
	}
}
