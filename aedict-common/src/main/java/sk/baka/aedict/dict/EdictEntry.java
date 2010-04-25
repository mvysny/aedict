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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import sk.baka.autils.MiscUtils;

/**
 * An EDICT entry.
 * 
 * @author Martin Vysny
 */
public final class EdictEntry extends DictEntry {

    private static final long serialVersionUID = 1L;

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
     *            if true then this word is a common one.
     */
    public EdictEntry(final String kanji, final String reading, final String english, final boolean isCommon) {
        super(kanji, reading, english, isCommon);
    }

    /**
     * Checks if this entry is a ichidan verb.
     *
     * @return true if this entry is a ichidan verb, false otherwise.
     */
    public boolean isIchidan() {
        return isValid() && english.contains("v1");
    }

    /**
     * Checks if this entry is a godan verb.
     *
     * @return true if this entry is a godan verb, false otherwise.
     */
    public boolean isGodan() {
        return isValid() && english.contains("v5");
    }

    /**
     * Checks if this entry contains a verb.
     *
     * @return true if this entry is a verb, false otherwise.
     */
    public boolean isVerb() {
        return isIchidan() || isGodan() || isSuru() || isKuru();
    }

    /**
     * Checks if this entry is a "suru" entry.
     *
     * @return true if this entry describes the "suru" irregular verb.
     */
    public boolean isSuru() {
        return english.contains("vs-i") || english.contains("vs-s");
    }

    /**
     * Checks if this entry is a "kuru" entry.
     *
     * @return true if this entry describes the "kuru" irregular verb.
     */
    public boolean isKuru() {
        return english.contains("vk");
    }

    /**
     * Returns a list of POS or other markings which this entry is annotated
     * with.
     *
     * @return a list of markings, never null, may be empty.
     */
    public List<String> getMarkings() {
        final List<String> result = new ArrayList<String>();
        if (isCommon) {
            result.add("P");
        }
        final StringTokenizer braces = new StringTokenizer(english, "()", true);
        boolean inBrace = false;
        while (braces.hasMoreTokens()) {
            final String token = braces.nextToken().trim();
            if (token.startsWith("(")) {
                inBrace = true;
                continue;
            }
            if (token.startsWith(")")) {
                inBrace = false;
                continue;
            }
            // found a translated text. no more markings will be found, bail
            // out.
            if (!inBrace && token.length() != 0) {
                break;
            }
            // there may be multiple markings in a single brace, check them all
            final String[] markings = token.split(",");
            for (final String marking : markings) {
                final String m = marking.trim();
                if (!MiscUtils.isBlank(m) && !m.matches("\\d+")) {
                    result.add(m);
                }
            }
        }
        return result;
    }
}
