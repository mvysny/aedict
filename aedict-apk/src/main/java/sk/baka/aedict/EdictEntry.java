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

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.util.Log;
import android.widget.TextView;
import android.widget.TwoLineListItem;

/**
 * Represents a parsed EDict entry. Immutable.
 * 
 * @author Martin Vysny
 */
public final class EdictEntry implements Comparable<EdictEntry>, Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * The kanji expression, may be null if the entry does not contain any kanji
	 */
	public final String kanji;
	/**
	 * The reading, in hiragana or katakana.
	 */
	public final String reading;
	/**
	 * The English translation
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
	 *            the English translation
	 */
	public EdictEntry(final String kanji, final String reading, final String english) {
		this.kanji = kanji;
		this.reading = reading;
		this.english = english;
	}

	/**
	 * Checks if this entry is valid.
	 * 
	 * @return true if it is valid (the kanji or the reading is not blank),
	 *         false otherwise.
	 */
	public boolean isValid() {
		return !MiscUtils.isBlank(kanji) || !MiscUtils.isBlank(reading);
	}

	/**
	 * Constructs an invalid entry with given error message.
	 * 
	 * @param errorMsg
	 *            the error message
	 * @return invalid edict entry.
	 */
	public static EdictEntry newErrorMsg(final String errorMsg) {
		return new EdictEntry(null, null, errorMsg);
	}

	/**
	 * Parses given EDICT entry.
	 * 
	 * @param edictEntry
	 *            the entry to parse.
	 * @return a parsed entry. Never null. The entry may not be valid. In such
	 *         case the English translation contains the error description, all
	 *         other fields are null.
	 */
	public static EdictEntry tryParseEdict(final String edictEntry) {
		try {
			return parseEdict(edictEntry);
		} catch (Exception ex) {
			Log.e(EdictEntry.class.getSimpleName(), "Failed to parse an entry '" + edictEntry + "'", ex);
			return new EdictEntry(null, null, ex.toString());
		}
	}

	/**
	 * Parses a list of EDICT entries.
	 * 
	 * @param edictEntries
	 *            a list of entries.
	 * @return a list of parsed entries, some of them may be invalid if parse
	 *         error occurred.
	 */
	public static List<EdictEntry> tryParseEdict(final Collection<? extends String> edictEntries) {
		final List<EdictEntry> result = new ArrayList<EdictEntry>(edictEntries.size());
		for (final String unparsedEntry : edictEntries) {
			result.add(tryParseEdict(unparsedEntry));
		}
		return result;
	}

	/**
	 * Parses given EDICT entry.
	 * 
	 * @param edictEntry
	 *            the entry to parse.
	 * @return a parsed entry
	 * @throws ParseException
	 *             if the parsing fails
	 */
	public static EdictEntry parseEdict(final String edictEntry) throws ParseException {
		// the entry is in one of the two following formats:
		// KANJI [hiragana] / english meaning
		// katakana / english meaning
		final int firstSlash = edictEntry.indexOf('/');
		if (firstSlash < 0) {
			throw new ParseException("Failed to parse " + edictEntry + ": missing slash", 0);
		}
		String englishPart = edictEntry.substring(firstSlash + 1).trim();
		while (englishPart.endsWith("/")) {
			// drop trailing slashes
			englishPart = englishPart.substring(0, englishPart.length() - 1);
		}
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

	/**
	 * Prints itself to a ListView item.
	 * 
	 * @param item
	 *            the item.
	 */
	public void print(final TwoLineListItem item) {
		print(item.getText1(), item.getText2());
	}

	/**
	 * Prints itself to a ListView item.
	 * 
	 * @param text1
	 *            first, larger textview.
	 * @param text2
	 *            second, smaller textview.
	 */
	public void print(final TextView text1, final TextView text2) {
		final String t1;
		if (kanji == null) {
			if (reading == null) {
				t1 = "";
			} else {
				t1 = reading;
			}
		} else {
			t1 = kanji + "  -  " + reading;
		}
		text1.setText(t1);
		text2.setText(english);
	}

	/**
	 * Returns japanese translation. Returns {@link #kanji} if available,
	 * {@link #reading} otherwise.
	 * 
	 * @return a japanese translation, kanji or hiragana/katakana.
	 */
	public String getJapanese() {
		return kanji != null ? kanji : reading;
	}

	public int compareTo(EdictEntry another) {
		if (!isValid()) {
			if (another.isValid()) {
				return 1;
			}
			return english.compareTo(another.english);
		}
		if (!another.isValid()) {
			return -1;
		}
		return getJapanese().compareTo(another.getJapanese());
	}
}
