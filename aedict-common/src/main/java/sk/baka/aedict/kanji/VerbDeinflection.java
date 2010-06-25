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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Performs a simple verb deinflection.
 * 
 * @author Martin Vysny
 */
public final class VerbDeinflection {

    private static class IrregularDeinflector extends EndsWithDeinflector {

        public IrregularDeinflector(final String[] inflected, final String... base) {
            super(inflected, true, true, base);
        }
    }

    private static class EndsWithDeinflector extends AbstractDeinflector {

        private final String[] endsWith;
        private final String[] replaceBy;
        private final boolean isAllowEntireWordMatch;
        private final boolean isStopIfMatch;

        /**
         * Deinflects a verb if it ends with one of the following strings.
         *
         * @param endsWith
         *            a non-empty non-null list of possible endings, lower-case
         *            trimmed romaji.
         * @param replaceBy
         *            the ending is replaced by this string.
         */
        public EndsWithDeinflector(final String[] endsWith, final String... replaceBy) {
            this(endsWith, false, false, replaceBy);
        }

        /**
         * Deinflects a verb if it ends with one of the following strings.
         *
         * @param endsWith
         *            a non-empty non-null list of possible endings, lower-case
         *            trimmed romaji.
         * @param isAllowEntireWordMatch
         *            if true then an entire word must match, if false then only
         *            a suffix (not an entire word) must match. This is often
         *            not wanted, e.g. the itai suffix would match the itai
         *            word.
         * @param isStopIfMatch
         *            defines the return value of {@link #stopIfMatch()}.
         * @param replaceBy
         *            the ending is replaced by this string.
         */
        public EndsWithDeinflector(final String[] endsWith, final boolean isAllowEntireWordMatch, final boolean isStopIfMatch, final String... replaceBy) {
            this.endsWith = endsWith;
            this.replaceBy = replaceBy;
            this.isAllowEntireWordMatch = isAllowEntireWordMatch;
            this.isStopIfMatch = isStopIfMatch;
        }

        /**
         * Deinflects a verb if it ends with one of the following strings.
         *
         * @param endsWith
         *            a non-empty non-null list of possible endings, lower-case
         *            trimmed romaji.
         * @param replaceBy
         *            the ending is replaced by this string.
         */
        public EndsWithDeinflector(final String endsWith, final String... replaceBy) {
            this(new String[]{endsWith}, replaceBy);
        }

        @Override
        public Set<String> deinflect(String romaji) {
            final String ending = isMatch(romaji);
            if (ending == null) {
                // nothing matched
                return null;
            }
            final Set<String> result = new HashSet<String>(replaceBy.length);
            final String verbPart = romaji.substring(0, romaji.length() - ending.length());
            for (final String rb : replaceBy) {
                result.add(verbPart + rb);
            }
            return result;
        }

        private String isMatch(final String romaji) {
            for (String ending : endsWith) {
                if (isAllowEntireWordMatch && romaji.equals(ending)) {
                    return ending;
                }
                if (!isAllowEntireWordMatch) {
                    if (romaji.endsWith(ending) && !romaji.equals(ending)) {
                        return ending;
                    }
                }
            }
            return null;
        }

        @Override
        public boolean stopIfMatch() {
            return isStopIfMatch;
        }
    }

    private static class EruDeinflector extends AbstractDeinflector {
        // this rule is also required, to correctly deinflect e.g.
        // aetai. list as a last rule. Make the rule produce the old verb and
        // also the deinflected one.

        private final AbstractDeinflector eruDeinflector = new EndsWithDeinflector("eru", "eru", "u");

        @Override
        public Set<String> deinflect(String romaji) {
            // do not deinflect -rareru
            if (romaji.endsWith("rareru")) {
                return null;
            }
            return eruDeinflector.deinflect(romaji);
        }

        @Override
        public boolean stopIfMatch() {
            // if the -eru is deinflected, there is nothing more to match
            return true;
        }
    }

    private static abstract class AbstractDeinflector {

        /**
         * Tries to deinflect a verb.
         *
         * @param romaji
         *            a verb in lower-case, trimmed romaji.
         * @return deinflected verb, or a multiple verbs if there are multiple
         *         possibilities to deinflect. If this rule cannot be applied to
         *         deinflect the verb, null or an empty array should be
         *         returned.
         */
        public abstract Set<String> deinflect(String romaji);

        /**
         * If true then there is nothing more to deinflect and the process can
         * be safely stopped.
         *
         * @return true if there is nothing more to deinflect, false if the
         *         deinflection should continue.
         */
        public abstract boolean stopIfMatch();
    }

    private static AbstractDeinflector basicSuffix(final String endsWith, final String... replaceBy) {
        return new EndsWithDeinflector(new String[]{endsWith}, false, true, replaceBy);
    }

    private static AbstractDeinflector basicSuffix(final String[] endsWith, final String... replaceBy) {
        return new EndsWithDeinflector(endsWith, false, true, replaceBy);
    }
    private final static List<? extends AbstractDeinflector> DEINFLECTORS;

    static {
        final List<AbstractDeinflector> d = new ArrayList<AbstractDeinflector>();
        d.add(new IrregularDeinflector(new String[]{"dewaarimasen", "dehaarimasen", "de wa arimasen", "de ha arimasen", "zya arimasen", "zyaarimasen"}, "desu"));
        d.add(new IrregularDeinflector(new String[]{"dewaarimasendesita", "dehaarimasendesita", "de wa arimasen desita", "de ha arimasen desita", "zya arimasen desita", "zyaarimasendesita"}, "desu"));
        // the -masu deinflector
        d.add(new EndsWithDeinflector(new String[]{"masen", "masita", "masendesita", "masen desita"}, "masu"));
        // the -nakatta deinflector
        d.add(new EndsWithDeinflector("nakatta", "nai"));
        // irregulars deinflector
        d.add(new IrregularDeinflector(new String[]{"sinai", "sita", "site", "simasu", "siyou"}, "suru"));
        d.add(new IrregularDeinflector(new String[]{"sareru", "sarenai", "sareta"}, "sareru"));
        d.add(new IrregularDeinflector(new String[]{"konai", "kita", "kite", "kimasu", "koyou"}, "kuru"));
        d.add(new IrregularDeinflector(new String[]{"da", "dewanai", "zyanai", "datta", "desita", "de", "dehanai", "de ha nai", "dewanai", "de wa nai", "dehaaru", "de ha aru", "de wa aru", "dewaaru"}, "desu"));
        d.add(new IrregularDeinflector(new String[]{"itta", "itte", "ikimasu"}, "iku"));
        d.add(new IrregularDeinflector(new String[]{"ikareru", "ikarenai", "ikareta"}, "ikareru"));
        d.add(new IrregularDeinflector(new String[]{"nai", "nakatta", "arimasu"}, "aru"));
        d.add(new IrregularDeinflector(new String[]{"atte", "atta"}, "aru", "au"));
        // regular inflections
        d.add(basicSuffix(new String[]{"arenai", "areta", "areru"}, "areru"));
        d.add(basicSuffix("wanai", "u"));
        d.add(basicSuffix("anai", "u"));
        // further deinflect -eru
        d.add(new EndsWithDeinflector("enai", "eru"));
        // e.g. minai -> miru
        d.add(basicSuffix("inai", "iru"));
        d.add(basicSuffix("itai", "u"));
        // further deinflect -eru
        d.add(new EndsWithDeinflector("etai", "eru"));
        d.add(basicSuffix("eba", "u"));
        d.add(new EndsWithDeinflector("emasu", "u", "eru"));
        d.add(new EndsWithDeinflector("imasu", "u", "iru"));
        d.add(basicSuffix(new String[]{"outosuru", "ou to suru"}, "u"));
        // this is dangerous - it will deinflect all ichidan verbs. however,
        // this rule is also required, to correctly deinflect e.g.
        // aetai. list as a last rule. Make the rule produce the old verb and
        // also the deinflected one.
        d.add(new EruDeinflector());
        // and finally, the -ta and -te deinflectors
        d.add(basicSuffix(new String[]{"sita", "site"}, "su"));
        // -ite may be a godan -ku but also ichidan -iru verb
        d.add(basicSuffix(new String[]{"ita", "ite"}, "ku", "iru"));
        // this is purely for ichidan -eru verb
        d.add(basicSuffix(new String[]{"eta", "ete"}, "eru"));
        d.add(basicSuffix(new String[]{"ida", "ide"}, "gu"));
        d.add(basicSuffix(new String[]{"tta", "tte"}, "tu", "u", "ru"));
        d.add(basicSuffix(new String[]{"nda", "nde"}, "nu", "bu", "mu"));
        DEINFLECTORS = d;
    }

    /**
     * Attempts to deinflect given verb.
     *
     * @param japanese
     *            {@link RomanizationEnum#NihonShiki} romaji or hiragana.
     * @return deinflected verb(s) in {@link RomanizationEnum#NihonShiki}
     *         romanization. If the expression cannot be deinflected the
     *         expression is returned.
     */
    public static Set<String> deinflect(final String japanese) {
        Set<String> result = new HashSet<String>();
        Set<String> finalDeinflect = new HashSet<String>();
        result.add(RomanizationEnum.NihonShiki.toRomaji(japanese).trim());
        for (final AbstractDeinflector deinflector : DEINFLECTORS) {
            final Set<String> newResult = new HashSet<String>(result);
            for (final String romaji : result) {
                final Set<String> deinflected = deinflector.deinflect(romaji);
                if (deinflected != null && !deinflected.isEmpty()) {
                    // successfully deinflected. remove the old verb and add the
                    // deinflected one.
                    newResult.remove(romaji);
                    if (deinflector.stopIfMatch()) {
                        finalDeinflect.addAll(deinflected);
                    } else {
                        newResult.addAll(deinflected);
                    }
                }
            }
            result = newResult;
        }
        result.addAll(finalDeinflect);
        return result;
    }

    private VerbDeinflection() {
        throw new AssertionError();
    }
}
