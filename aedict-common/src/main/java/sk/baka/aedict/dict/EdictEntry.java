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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import sk.baka.autils.MiscUtils;

/**
 * An EDICT entry. The EDICT entry format is described at http://www.csse.monash.edu.au/~jwb/edict_doc.html
 * <p/>
 * Example: １つ [ひとつ] /(n) (1) one/(2) for one thing (often used in itemized lists)/(3) (after a noun) only/(4) (with a verb in negative form) (not) even/(n-adv) (5) just (i.e. "just try it")/
 * Is a noun with four senses
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
        return isValid() && getMarkings().contains("v1");
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
        return getMarkings().contains("vs-i") || getMarkings().contains("vs-s");
    }

    /**
     * Checks if this entry is a "kuru" entry.
     *
     * @return true if this entry describes the "kuru" irregular verb.
     */
    public boolean isKuru() {
        return getMarkings().contains("vk");
    }

    /**
     * Returns a list of POS or other markings which this entry is annotated
     * with.
     *
     * @return a list of markings, never null, may be empty.
     */
    public List<String> getMarkings() {
        return getMarkings(english);
    }

    /**
     * Returns a list of POS or other markings which this entry is annotated
     * with.
     * @param str find markings in this string.
     * @return a list of markings, never null, may be empty.
     */
    public List<String> getMarkings(final String str) {
        final List<String> result = new ArrayList<String>();
        if (isCommon) {
            result.add("P");
        }
        findMarkings(str, result);
        return result;
    }
    private static final String[] MARKING_LIST = new String[]{"adj-i", "adj-na", "adj-no", "adj-pn", "adj-t",
        "adj-f", "adj", "adv", "adv-n", "adv-to", "aux",
        "aux-v", "aux-adj", "conj", "ctr", "exp", "id",
        "int", "iv", "n", "n-adv", "n-pref", "n-suf", "n-t",
        "num", "pn", "pref", "prt", "suf", "v1", "v5",
        "v5aru", "v5b", "v5g", "v5k", "v5k-s", "v5m",
        "v5n", "v5r", "v5r-i", "v5s", "v5t", "v5u", "v5u-s",
        "v5uru", "v5z", "vz", "vi", "vk", "vn", "vs",
        "vs-i", "vs-s", "vt", "Buddh", "MA", "comp", "food",
        "geom", "gram", "ling", "math", "mil", "physics",
        "X", "abbr", "arch", "ateji", "chn", "col",
        "derog", "eK", "ek", "fam", "fem", "gikun", "hon",
        "hum", "iK", "id", "io", "m-sl", "male", "male-sl",
        "ng", "oK", "obs", "obsc", "ok", "on-mim", "poet",
        "pol", "rare", "sens", "sl", "uK", "uk", "vulg", "P"
    };
    private static final Set<String> MARKINGS = new HashSet<String>(Arrays.asList(MARKING_LIST));

    static int findMarkings(final String str, final List<String> result) {
        final StringTokens braces = new StringTokens(str, "()", true);
        boolean inBrace = false;
        while (braces.hasMoreTokens()) {
            final String t = braces.nextToken();
            final String token = t.trim();
            if (token.startsWith("(")) {
                inBrace = true;
                continue;
            }
            if (token.startsWith(")")) {
                inBrace = false;
                continue;
            }
            if (!inBrace) {
                if (token.length() != 0) {
                    // found a translated text. no more markings will be found, bail
                    // out.
                    return braces.getCurrentPosition() - t.length();
                }
                continue;
            }
            // there may be multiple markings in a single brace, check them all
            final String[] markings = token.split(",");
            for (final String marking : markings) {
                final String m = marking.trim();
                if (MARKINGS.contains(m)) {
                    result.add(m);
                } else {
                    return braces.getCurrentPosition() - t.length() - 1;
                }
            }
        }
        return str.length();
    }

    public List<List<String>> getSenses() {
        final List<List<String>> result = new ArrayList<List<String>>();
        int currentSense = 1;
        // remove the initial markings part
        final String e = english.substring(findMarkings(english, new ArrayList<String>()));
        for (final StringTokenizer senses = new StringTokenizer(e, ";"); senses.hasMoreTokens();) {
            final String sense = senses.nextToken();
            if (MiscUtils.isBlank(sense)) {
                continue;
            }
            int[] newCurrentSense = findSenseNumber(sense);
            if (newCurrentSense != null) {
                currentSense = newCurrentSense[0];
            }
            while (result.size() < currentSense) {
                result.add(new ArrayList<String>());
            }
            result.get(currentSense - 1).add(newCurrentSense != null ? sense.substring(newCurrentSense[1]).trim() : sense.trim());
        }
        return result;
    }

    static int[] findSenseNumber(String str) {
        final StringTokens braces = new StringTokens(str, "()", true);
        boolean inBrace = false;
        while (braces.hasMoreTokens()) {
            final String t = braces.nextToken();
            final String token = t.trim();
            if (token.startsWith("(")) {
                inBrace = true;
                continue;
            }
            if (token.startsWith(")")) {
                inBrace = false;
                continue;
            }
            if (inBrace) {
                try {
                    return new int[]{Integer.parseInt(token), braces.getCurrentPosition() + 1};
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return null;
    }
}
