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
import java.util.Collection;
import java.util.Iterator;

import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;
import android.widget.TextView;
import android.widget.TwoLineListItem;

/**
 * Represents a parsed dictionary entry. Immutable.
 * 
 * @author Martin Vysny
 */
public final class DictEntry implements Comparable<DictEntry>, Serializable {
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
	public DictEntry(final String kanji, final String reading, final String english) {
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
	 * @param isCommon
	 *            if true then this word is a common one. null if not known.
	 */
	public DictEntry(final String kanji, final String reading, final String english, final Integer radical, final Integer strokes, final String skip, final Integer grade, final Boolean isCommon) {
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
	public static DictEntry newErrorMsg(final String errorMsg) {
		return new DictEntry(null, null, errorMsg);
	}

	/**
	 * Removes invalid entries from given collection.
	 * 
	 * @param edictEntries
	 *            a list of entries.
	 * @return edictEntries
	 */
	public static Collection<? extends DictEntry> removeInvalid(final Collection<? extends DictEntry> edictEntries) {
		for (final Iterator<? extends DictEntry> i = edictEntries.iterator(); i.hasNext();) {
			if (!i.next().isValid()) {
				i.remove();
			}
		}
		return edictEntries;
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
		if (romanize != null && reading != null) {
			reading = romanize.toRomaji(reading);
		}
		text1.setText(formatJapanese());
		text2.setText(english);
	}

	/**
	 * Returns a formatted japanese contents, in the form of {@link #kanji} -
	 * {@link #reading}. The dash separator is omitted if one of {@link #kanji}
	 * or {@link #reading} is missing.
	 * 
	 * @return a formatted japanese contents.
	 */
	public String formatJapanese() {
		final ListBuilder t1 = new ListBuilder(" - ");
		if (kanji != null) {
			t1.add(kanji);
		}
		if (reading != null) {
			t1.add(reading);
		}
		return t1.toString();
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

	/**
	 * A comparator which imposes order upon an edict entry, according to the
	 * following rules:
	 * <ul>
	 * <li>Invalid entries are placed last,</li>
	 * <li>
	 * {@link DictEntry#isCommon common} words are preferred;</li>
	 * <li>next, shortest {@link DictEntry#getJapanese() japanese} words are
	 * preferred (as they usually are the best matches)</li>
	 * <li>finally, {@link #getJapanese()} values are compared</li>
	 * </ul>
	 */
	public int compareTo(DictEntry another) {
		if (!isValid()) {
			if (another.isValid()) {
				return 1;
			}
			return english.compareTo(another.english);
		}
		if (!another.isValid()) {
			return -1;
		}
		// common words first
		int result = -isCommonNotNull().compareTo(another.isCommonNotNull());
		if (result != 0) {
			return result;
		}
		result = getJapanese().length() - another.getJapanese().length();
		if (result != 0) {
			return result;
		}
		return getJapanese().compareTo(another.getJapanese());
	}

	@Override
	public int hashCode() {
		return hashCode(getJapanese()) * 1001 + hashCode(english);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DictEntry)) {
			return false;
		}
		final DictEntry o = (DictEntry) other;
		return equals(getJapanese(), o.getJapanese()) && equals(english, o.english);
	}

	private int hashCode(Object obj) {
		return obj == null ? 0 : obj.hashCode();
	}

	private boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	private Boolean isCommonNotNull() {
		return isCommon == null ? false : isCommon;
	}

	/**
	 * Returns a japanese word, formed as a concatenation of
	 * {@link DictEntry#getJapanese()} from all entries.
	 * 
	 * @param entries
	 *            the list of entries
	 * @return the japanese word.
	 */
	public static String getJapaneseWord(Collection<? extends DictEntry> entries) {
		final StringBuilder sb = new StringBuilder(entries.size());
		for (final DictEntry e : entries) {
			sb.append(e.getJapanese());
		}
		return sb.toString();
	}
}
