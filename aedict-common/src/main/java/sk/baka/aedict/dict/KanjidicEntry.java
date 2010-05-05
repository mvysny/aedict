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

import sk.baka.aedict.kanji.KanjiUtils;

/**
 * A KANJIDIC entry, containing more information for the entry. For details on the KANJIDIC dictionary please see http://www.csse.monash.edu.au/~jwb/kanjidic.html
 * 
 * @author Martin Vysny
 */
public class KanjidicEntry extends DictEntry {

    private static final long serialVersionUID = 1L;
    /**
     * The classification radical number of the kanji (as in Nelson).
     */
    public final int radical;
    /**
     * The total stroke-count of the kanji.
     */
    public final int strokes;
    /**
     * The "SKIP" coding of the kanji, as used in Halpern. Never null.
     */
    public final String skip;
    /**
     * The "grade" of the kanji. For example, 2 means it is a Jouyou (general use) kanji
     * taught in the second year of elementary schooling in Japan. May be null if not known.
     */
    public final Integer grade;

    /**
     * Creates new entry instance.
     *
     * @param kanji
     *            the kanji expression, not null.
     * @param reading
     *            the readings, in hiragana or katakana, not null
     * @param english
     *            the English translation, not null
     * @param radical
     *            The classification radical number of the kanji (as in Nelson).
     *            Not null.
     * @param strokes
     *            The total stroke-count of the kanji. Not null.
     * @param skip
     *            The "SKIP" coding of the kanji, as used in Halpern. Not null.
     * @param grade
     *            The "grade" of the kanji, In this case, G2 means it is a
     *            Jouyou (general use) kanji taught in the second year of
     *            elementary schooling in Japan. Non-null only when
     *            {@link #kanji} is a single kanji and this entry was parsed
     *            from KANJIDIC.
     */
    public KanjidicEntry(final String kanji, final String reading, final String english, final int radical, final int strokes, final String skip, final Integer grade) {
        super(kanji, reading, english);
        if (kanji.length() != 1) {
            throw new IllegalArgumentException("A single kanji expected but got \"" + kanji + "\"");
        }
        this.radical = radical;
        this.strokes = strokes;
        this.skip = skip;
        this.grade = grade;
    }

    /**
     * Returns JLPT level of given kanji.
     * @return JLPT level 1..6, null if the kanji is not present in all JLPT tests.
     */
    public Integer getJlpt() {
        return KanjiUtils.getJlptLevel(getKanji());
    }

    public char getKanji() {
        return kanji.charAt(0);
    }
}
