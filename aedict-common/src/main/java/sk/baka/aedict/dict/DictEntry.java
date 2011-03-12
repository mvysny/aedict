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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;

/**
 * Represents a parsed dictionary entry. Immutable.
 * 
 * @author Martin Vysny
 */
public class DictEntry implements Comparable<DictEntry>, Serializable {

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
     * The English translation, in raw format (including POS Markings, multiple meanings separated by slash etc).
     */
    public final String english;
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
        this(kanji, reading, english, null);
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
     * @param isCommon
     *            if true then this word is a common one. null if not known.
     */
    public DictEntry(final String kanji, final String reading, final String english, final Boolean isCommon) {
        if (english == null) {
            throw new IllegalArgumentException("english must not be null");
        }
        this.kanji = kanji;
        this.reading = reading;
        this.english = english;
        this.isCommon = isCommon;
    }

    /**
     * Checks if this entry is valid.
     *
     * @return true if it is valid (the kanji or the reading is not blank),
     *         false otherwise.
     */
    public final boolean isValid() {
        return !MiscUtils.isBlank(kanji) || !MiscUtils.isBlank(reading);
    }

    /**
     * Constructs an invalid entry with given error message.
     *
     * @param t
     *            the error cause
     * @return invalid edict entry.
     */
    public static DictEntry newErrorMsg(final Throwable t) {
        return newErrorMsg(t.toString() + "\n" + getStacktrace(t));
    }
    /**
     * Returns stacktrace of given exception.
     *
     * @param ex
     *            the exception
     * @return stacktrace as string.
     */
    private static String getStacktrace(Throwable ex) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.close();
        return sw.toString();
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
     * Returns a formatted japanese contents, in the form of {@link #kanji} -
     * {@link #reading}. The dash separator is omitted if one of {@link #kanji}
     * or {@link #reading} is missing.
     *
     * @param romanize
     *            if non-null then katakana/hiragana will be shown as romaji
     *
     * @return a formatted japanese contents.
     */
    public String formatJapanese(final RomanizationEnum romanize) {
        final ListBuilder t1 = new ListBuilder(" - ");
        if (kanji != null) {
            t1.add(kanji);
        }
        String reading = this.reading;
        if (romanize != null && reading != null) {
            reading = romanize.toRomaji(reading);
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
    public final String getJapanese() {
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
     * <li>next, more {@link KanjiUtils#getCommonality(String) common} word is
     * preferred</li>
     * <li>finally, {@link #getJapanese()} values are compared</li>
     * </ul>
     * @param another compare to this entry.
     * @return see {@link Comparable} for details
     */
    public final int compareTo(DictEntry another) {
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
        result = getCommonality() - another.getCommonality();
        if (result != 0) {
            return result;
        }
        return getJapanese().compareTo(another.getJapanese());
    }

    @Override
    public final int hashCode() {
        return hashCode(getJapanese()) * 1001 + hashCode(english);
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof DictEntry)) {
            return false;
        }
        final DictEntry o = (DictEntry) other;
        return equals(getJapanese(), o.getJapanese()) && equals(english, o.english);
    }

    private final int hashCode(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }

    private final boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    private final Boolean isCommonNotNull() {
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

    /**
     * Creates an external form of the entry.
     *
     * @return the external form, parsable by {@link #fromExternal(String)}.
     */
    public final String toExternal() {
        return toEmpty(kanji) + "####" + toEmpty(reading) + "####" + toEmpty(english);
    }

    private static String toEmpty(final String s) {
        return s == null ? "" : s;
    }

    private static String toNull(final String s) {
        return (s == null) || (s.length() == 0) ? null : s;
    }

    /**
     * Parses the external form and creates back the Entry.
     *
     * @param external
     *            the external form.
     * @return the entry instance.
     */
    public static DictEntry fromExternal(final String external) {
        final String[] contents = external.split("####");
        if (contents.length < 3) {
            throw new IllegalArgumentException("Invalid external format: \"" + external + "\"");
        }
        return new DictEntry(toNull(contents[0]), toNull(contents[1]), toNull(contents[2]));
    }

    /**
     * Converts a list of entries to an external form, parsable by {@link #fromExternalList(java.lang.String)}.
     * @param entries the list of entries, not null
     * @return external form.
     */
    public static String toExternalList(final List<? extends DictEntry> entries) {
        final ListBuilder b = new ListBuilder("@@@@");
        for (final DictEntry entry : entries) {
            b.add(entry.toExternal());
        }
        return b.toString();
    }

    /**
     * Converts an {@link #toExternalList(java.util.List) externalized list of entries} back to a list of entries.
     * @param external the externalized form
     * @return a list of entries.
     */
    public static List<DictEntry> fromExternalList(final String external) {
        final String items[] = external.split("@@@@");
        final List<DictEntry> result = new ArrayList<DictEntry>();
        for (final String item : items) {
            if (!MiscUtils.isBlank(item)) {
                result.add(DictEntry.fromExternal(item));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return kanji + " [" + reading + "]: " + english;
    }
    private int commonality = -1;

    /**
     * Computes commonality of this japanese entry, as per
     * {@link KanjiUtils#getCommonality(String)}.
     *
     * @return the commonality.
     */
    public final int getCommonality() {
        if (commonality == -1) {
            commonality = KanjiUtils.getCommonality(getJapanese());
        }
        return commonality;
    }
}
