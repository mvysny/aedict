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

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.util.MiscUtils;
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
	 * The classification radical number of the kanji (as in Nelson). Non-null
	 * only when {@link #kanji} is a single kanji and this entry was parsed from
	 * KANJIDIC.
	 */
	public final Integer radical;
	/**
	 * The total stroke-count of the kanji. Non-null only when {@link #kanji} is
	 * a single kanji and this entry was parsed from KANJIDIC.
	 */
	public final Integer strokes;
	/**
	 * The "SKIP" coding of the kanji, as used in Halpern. Non-null only when
	 * {@link #kanji} is a single kanji and this entry was parsed from KANJIDIC.
	 */
	public final String skip;
	/**
	 * The "grade" of the kanji, In this case, G2 means it is a Jouyou (general
	 * use) kanji taught in the second year of elementary schooling in Japan.
	 * Non-null only when {@link #kanji} is a single kanji and this entry was
	 * parsed from KANJIDIC.
	 */
	public final Integer grade;
	/**
	 * if true then this word is a common one. null if not known.
	 */
	public final Boolean isCommon;

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
		this(kanji, reading, english, null, null, null, null, null);
	}

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
	 * @param radical
	 *            The classification radical number of the kanji (as in Nelson).
	 *            Non-null only when {@link #kanji} is a single kanji and this
	 *            entry was parsed from KANJIDIC.
	 * @param strokes
	 *            The total stroke-count of the kanji. Non-null only when
	 *            {@link #kanji} is a single kanji and this entry was parsed
	 *            from KANJIDIC.
	 * @param skip
	 *            The "SKIP" coding of the kanji, as used in Halpern. Non-null
	 *            only when {@link #kanji} is a single kanji and this entry was
	 *            parsed from KANJIDIC.
	 * @param grade
	 *            The "grade" of the kanji, In this case, G2 means it is a
	 *            Jouyou (general use) kanji taught in the second year of
	 *            elementary schooling in Japan. Non-null only when
	 *            {@link #kanji} is a single kanji and this entry was parsed
	 *            from KANJIDIC.
	 * @param isCommon if true then this word is a common one. null if not known.
	 */
	public EdictEntry(final String kanji, final String reading, final String english, final Integer radical, final Integer strokes, final String skip, final Integer grade, final Boolean isCommon) {
		this.kanji = kanji;
		this.reading = reading;
		this.english = english;
		this.radical = radical;
		this.strokes = strokes;
		this.skip = skip;
		this.grade = grade;
		this.isCommon = isCommon;
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
		final boolean isCommon = edictEntry.contains("(P)");
		return new EdictEntry(kanji, reading, englishPart, null, null, null, null, isCommon);
	}

	/**
	 * Parses a list of KANJIDIC entries.
	 * 
	 * @param edictEntries
	 *            a list of entries.
	 * @return a list of parsed entries, some of them may be invalid if parse
	 *         error occurred.
	 */
	public static List<EdictEntry> parseKanjidic(final Collection<? extends String> edictEntries) {
		final List<EdictEntry> result = new ArrayList<EdictEntry>(edictEntries.size());
		for (final String unparsedEntry : edictEntries) {
			result.add(tryParseKanjidic(unparsedEntry));
		}
		return result;
	}

	/**
	 * Parses given KANJIDIC entry.
	 * 
	 * @param kanjidicEntry
	 *            the entry to parse.
	 * @return a parsed entry. Never null. The entry may not be valid. In such
	 *         case the English translation contains the error description, all
	 *         other fields are null.
	 */
	public static EdictEntry tryParseKanjidic(final String kanjidicEntry) {
		final char kanji = kanjidicEntry.charAt(0);
		if (kanjidicEntry.charAt(1) != ' ') {
			throw new IllegalArgumentException("Invalid kanjidic entry: " + kanjidicEntry);
		}
		final ListBuilder reading = new ListBuilder(", ");
		final ListBuilder namesReading = new ListBuilder(", ");
		boolean readingInNames = false;
		Integer radicalNumber = null;
		Integer strokeCount = null;
		Integer grade = null;
		String skip = null;
		// first pass: ignore English readings as they may contain spaces and
		// this simple algorithm would match them as readings (as the token does
		// not start with '{' )
		for (final String field : kanjidicEntry.substring(2).split("\\ ")) {
			final char firstChar = field.charAt(0);
			if (firstChar == '{') {
				break;
			} else if (firstChar == 'B') {
				radicalNumber = parse(field.substring(1));
			} else if (firstChar == 'S') {
				// get only the first value and ignore other, commonly mistaken
				// stroke numbers.
				if (strokeCount == null) {
					strokeCount = parse(field.substring(1));
				}
			} else if (firstChar == 'P') {
				skip = field.substring(1);
			} else if (firstChar == 'G') {
				grade = parse(field.substring(1));
			} else if ((firstChar < 'A' || firstChar > 'Z') && (firstChar < '0' || firstChar > '9')) {
				// a reading
				(readingInNames ? namesReading : reading).add(field);
			} else if (field.equals("T1")) {
				readingInNames = true;
			}
		}
		// second pass: English translations
		final ListBuilder english = new ListBuilder(", ");
		List<Object> tokens = Collections.list(new StringTokenizer(kanjidicEntry, "{}"));
		// skip the kanji definition tokens
		tokens = tokens.subList(1, tokens.size());
		for (final Object eng : tokens) {
			final String engStr = eng.toString().trim();
			if (engStr.length() == 0) {
				// skip spaces between } {
				continue;
			}
			english.add(engStr);
		}
		if (!namesReading.isEmpty()) {
			reading.add("[" + namesReading + "]");
		}
		return new EdictEntry(String.valueOf(kanji), reading.toString(), english.toString(), radicalNumber, strokeCount, skip, grade, null);
	}

	private static Integer parse(final String str) {
		try {
			return Integer.valueOf(str);
		} catch (NumberFormatException ex) {
			Log.e(EdictEntry.class.getSimpleName(), "Failed to parse integer " + str, ex);
			return null;
		}
	}

	private static class ListBuilder {
		private final String separator;
		private boolean isFirst = true;
		private final StringBuilder sb = new StringBuilder();

		public ListBuilder(final String separator) {
			this.separator = separator;
		}

		public ListBuilder add(final String string) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(separator);
			}
			sb.append(string);
			return this;
		}

		public boolean isEmpty() {
			return isFirst;
		}

		@Override
		public String toString() {
			return sb.toString();
		}
	}

	/**
	 * Prints itself to a ListView item.
	 * 
	 * @param item
	 *            the item.
	 * @param romanize
	 *            if non-null then katakana/hiragana will be shown as romaji
	 */
	public void print(final TwoLineListItem item, final RomanizationEnum romanize) {
		print(item.getText1(), item.getText2(), romanize);
	}

	/**
	 * Prints itself to a ListView item.
	 * 
	 * @param text1
	 *            first, larger textview.
	 * @param text2
	 *            second, smaller textview.
	 * @param romanize
	 *            if non-null then katakana/hiragana will be shown as romaji
	 */
	public void print(final TextView text1, final TextView text2, final RomanizationEnum romanize) {
		String reading = this.reading;
		if (romanize != null) {
			reading = romanize.toRomaji(reading);
		}
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
