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

import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.Deinflections.Deinflection;
import sk.baka.aedict.kanji.VerbInflection.Form;

/**
 * Performs a simple verb deinflection.
 * 
 * @author Martin Vysny
 */
public final class VerbDeinflection {

    private static class IrregularDeinflector extends EndsWithDeinflector {

        public IrregularDeinflector(final String inflected, final Form form, final String... base) {
            super(inflected, true, true, form, base);
        }
    }

    private static class EndsWithDeinflector extends AbstractDeinflector {

        private final String endsWith;
        private final String[] replaceBy;
        private final boolean isAllowEntireWordMatch;
        private final boolean isStopIfMatch;
        private final Form form;

        /**
         * Deinflects a verb if it ends with one of the following strings.
         *
         * @param endsWith
         *            a non-empty non-null list of possible endings, lower-case
         *            trimmed romaji.
         *            @param form the form of the verb.
         * @param replaceBy
         *            the ending is replaced by this string.
         */
        public EndsWithDeinflector(final String endsWith, final Form form, final String... replaceBy) {
            this(endsWith, false, false, form, replaceBy);
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
         *            @param form the form of the verb.
         * @param replaceBy
         *            the ending is replaced by this string.
         */
        public EndsWithDeinflector(final String endsWith, final boolean isAllowEntireWordMatch, final boolean isStopIfMatch, final Form form, final String... replaceBy) {
            this.endsWith = endsWith;
            this.replaceBy = replaceBy;
            this.isAllowEntireWordMatch = isAllowEntireWordMatch;
            this.isStopIfMatch = isStopIfMatch;
            this.form = form;
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
            final String ending = endsWith;
                if (isAllowEntireWordMatch && romaji.equals(ending)) {
                    return ending;
                }
                if (!isAllowEntireWordMatch) {
                    if (romaji.endsWith(ending) && !romaji.equals(ending)) {
                        return ending;
                    }
                }
            return null;
        }

        @Override
        public boolean stopIfMatch() {
            return isStopIfMatch;
        }

		@Override
		public Form getForm() {
			return form;
		}
    }

    private static class EruDeinflector extends AbstractDeinflector {
        // this rule is also required, to correctly deinflect e.g.
        // aetai. list as a last rule. Make the rule produce the old verb and
        // also the deinflected one.

        private final AbstractDeinflector eruDeinflector = new EndsWithDeinflector("eru", Form.ABLE_TO_DO2, "eru", "u");

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

		@Override
		public Form getForm() {
			return Form.ABLE_TO_DO2;
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
        
        /**
         * Returns the originating form.
         * @return originating form.
         */
        public abstract Form getForm();
    }

    private static AbstractDeinflector basicSuffix(final String endsWith, final Form form, final String... replaceBy) {
        return new EndsWithDeinflector(endsWith, false, true, form, replaceBy);
    }

    private static List<IrregularDeinflector> irregular(String[] endsWith, final Form form, final String replaceBy) {
    	final List<IrregularDeinflector> result=new ArrayList<VerbDeinflection.IrregularDeinflector>();
    	for(final String ew:endsWith){
    		result.add(new IrregularDeinflector(ew, form, replaceBy));
    	}
    	return result;
    }
    
    private final static List<? extends AbstractDeinflector> DEINFLECTORS;

    static {
        final List<AbstractDeinflector> d = new ArrayList<AbstractDeinflector>();
        d.addAll(irregular(new String[]{"dewaarimasen", "dehaarimasen", "de wa arimasen", "de ha arimasen", "zya arimasen", "zyaarimasen"}, Form.POLITE_NEGATIVE, "desu"));
        d.addAll(irregular(new String[]{"dewaarimasendesita", "dehaarimasendesita", "de wa arimasen desita", "de ha arimasen desita", "zya arimasen desita", "zyaarimasendesita"}, Form.POLITE_PAST_NEGATIVE, "desu"));
        // the -masu deinflector
        d.add(new EndsWithDeinflector("masen", Form.POLITE_NEGATIVE, "masu"));
        d.add(new EndsWithDeinflector("masita", Form.POLITE_PAST, "masu"));
        d.add(new EndsWithDeinflector("masendesita", Form.POLITE_PAST_NEGATIVE, "masu"));
        d.add(new EndsWithDeinflector("masen desita", Form.POLITE_PAST_NEGATIVE, "masu"));
        // the -nakatta deinflector
        d.add(new EndsWithDeinflector("nakatta", Form.NEGATIVE_PAST, "nai"));
        // irregulars deinflector
        d.add(new IrregularDeinflector("sinai", Form.NEGATIVE, "suru"));
        d.add(new IrregularDeinflector("sita", Form.PAST_TENSE, "suru"));
        d.add(new IrregularDeinflector("site", Form.CONTINUATION , "suru"));
        d.add(new IrregularDeinflector("simasu", Form.POLITE, "suru"));
        d.add(new IrregularDeinflector("siyou", Form.LET_S2, "suru"));
        d.add(new IrregularDeinflector("sareru", Form.PLAIN, "sareru"));
        d.add(new IrregularDeinflector("sarenai", Form.NEGATIVE, "sareru"));
        d.add(new IrregularDeinflector("sareta", Form.PAST_TENSE, "sareru"));
        d.add(new IrregularDeinflector("konai", Form.NEGATIVE, "kuru"));
        d.add(new IrregularDeinflector("kita",Form.PAST_TENSE, "kuru"));
        d.add(new IrregularDeinflector("kite", Form.CONTINUATION, "kuru"));
        d.add(new IrregularDeinflector("kimasu", Form.POLITE, "kuru"));
        d.add(new IrregularDeinflector("koyou",Form.LET_S2, "kuru"));
        d.add(new IrregularDeinflector("da", Form.PLAIN, "desu"));
        d.addAll(irregular(new String[]{"dewanai", "zyanai"},Form.NEGATIVE, "desu"));
        d.add(new IrregularDeinflector("datta", Form.PAST_TENSE , "desu"));
        d.add(new IrregularDeinflector("desita", Form.POLITE_PAST, "desu"));
        d.add(new IrregularDeinflector("de", null, "desu"));
        d.addAll(irregular(new String[]{"dehanai", "de ha nai", "dewanai", "de wa nai"}, Form.NEGATIVE, "desu"));
        d.addAll(irregular(new String[]{"dehaaru", "de ha aru", "de wa aru", "dewaaru"}, Form.PLAIN, "desu"));
        d.add(new IrregularDeinflector("itta", Form.PAST_TENSE, "iku"));
        d.add(new IrregularDeinflector("itte", Form.CONTINUATION, "iku"));
        d.add(new IrregularDeinflector("ikimasu", Form.POLITE, "iku"));
        d.add(new IrregularDeinflector("ikareru", Form.PLAIN, "ikareru"));
        d.add(new IrregularDeinflector("ikarenai", Form.NEGATIVE, "ikareru"));
        d.add(new IrregularDeinflector("ikareta",Form.PAST_TENSE, "ikareru"));
        d.add(new IrregularDeinflector("nai", Form.NEGATIVE, "aru"));
        d.add(new IrregularDeinflector("nakatta", Form.NEGATIVE_PAST, "aru"));
        d.add(new IrregularDeinflector("arimasu",Form.POLITE, "aru"));
        d.add(new IrregularDeinflector("atte", Form.CONTINUATION, "aru", "au"));
        d.add(new IrregularDeinflector("atta", Form.PAST_TENSE, "aru", "au"));
        // regular inflections
        d.add(basicSuffix("kereba", Form.NEGATIVE_CONDITIONAL, "i"));
        d.add(basicSuffix("arenai", Form.NEGATIVE, "areru"));
        d.add(basicSuffix("areta", Form.PAST_TENSE, "areru"));
        d.add(basicSuffix("areru", Form.PLAIN, "areru"));
        d.add(basicSuffix("wanai", Form.NEGATIVE, "u"));
        d.add(basicSuffix("anai", Form.NEGATIVE, "u"));
        // further deinflect -eru
        d.add(new EndsWithDeinflector("enai", Form.NEGATIVE, "eru"));
        // e.g. minai -> miru
        d.add(basicSuffix("inai", Form.NEGATIVE, "iru"));
        d.add(basicSuffix("itai", Form.WANT, "u"));
        // further deinflect -eru
        d.add(new EndsWithDeinflector("etai", Form.WANT, "eru"));
        d.add(basicSuffix("eba", Form.IF2, "u"));
        d.add(new EndsWithDeinflector("emasu", Form.ABLE_TO_DO2, "u", "eru"));
        d.add(new EndsWithDeinflector("imasu", Form.POLITE, "u", "iru"));
        d.add(basicSuffix("outosuru", null, "u"));
        d.add(basicSuffix("ou to suru",null, "u"));
        // this is dangerous - it will deinflect all ichidan verbs. however,
        // this rule is also required, to correctly deinflect e.g.
        // aetai. list as a last rule. Make the rule produce the old verb and
        // also the deinflected one.
        d.add(new EruDeinflector());
        // and finally, the -ta and -te deinflectors
        d.add(basicSuffix("sita", Form.PAST_TENSE, "su"));
        d.add(basicSuffix("site",Form.CONTINUATION, "su"));
        // -ite may be a godan -ku but also ichidan -iru verb
        d.add(basicSuffix("ita", Form.PAST_TENSE, "ku", "iru"));
        d.add(basicSuffix("ite",Form.CONTINUATION, "ku", "iru"));
        // this is purely for ichidan -eru verb
        d.add(basicSuffix("eta", Form.PAST_TENSE, "eru"));
        d.add(basicSuffix("ete",Form.CONTINUATION, "eru"));
        d.add(basicSuffix("ida", Form.PAST_TENSE, "gu"));
        d.add(basicSuffix("ide",Form.CONTINUATION, "gu"));
        d.add(basicSuffix("tta", Form.PAST_TENSE, "tu", "u", "ru"));
        d.add(basicSuffix("tte",Form.CONTINUATION, "tu", "u", "ru"));
        d.add(basicSuffix("nda", Form.PAST_TENSE, "nu", "bu", "mu"));
        d.add(basicSuffix("nde",Form.CONTINUATION, "nu", "bu", "mu"));
        DEINFLECTORS = d;
    }

    /**
     * Attempts to deinflect given verb.
     *
     * @param japanese
     *            {@link RomanizationEnum#NihonShiki} romaji or hiragana.
     * @return deinflected verb(s)
     */
    public static Deinflections deinflect(final String japanese) {
        final Deinflections result = new Deinflections();
        result.deinflections=new ArrayList<Deinflection>();
        result.deinflectedVerbs = new HashSet<String>();
        Set<String> finalDeinflect = new HashSet<String>();
        result.deinflectedVerbs.add(RomanizationEnum.NihonShiki.toRomaji(japanese).trim());
        for (final AbstractDeinflector deinflector : DEINFLECTORS) {
            final Set<String> newResult = new HashSet<String>(result.deinflectedVerbs);
            for (final String romaji : result.deinflectedVerbs) {
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
                    result.deinflections.add(new Deinflection(romaji, deinflector.getForm(), deinflected.toArray(new String[0])));
                }
            }
            result.deinflectedVerbs = newResult;
        }
        result.deinflectedVerbs.addAll(finalDeinflect);
        return result;
    }

    private VerbDeinflection() {
        throw new AssertionError();
    }

    /**
     * Creates an EDICT query which searches for a japanese term. Automatically performs a verb deinflection.
     *
     * @param verb
     *            the word to search, in japanese language, may contain romaji.
     *            Full-width katakana conversion is performed automatically. Not
     *            null
     * @param romanization
     *            the romanization system to use, not null.
     * @return search query, never null
     */
    public static Deinflections searchJpDeinflected(final String verb, final RomanizationEnum romanization) {
        final SearchQuery result = new SearchQuery(DictTypeEnum.Edict);
        final String conv = KanjiUtils.halfwidthToKatakana(verb);
        final String romaji = RomanizationEnum.NihonShiki.toRomaji(romanization.toHiragana(conv));
        final Deinflections deinflections = VerbDeinflection.deinflect(romaji);
        result.query = deinflections.deinflectedVerbs.toArray(new String[0]);
        for (int i = 0; i < result.query.length; i++) {
            result.query[i] = RomanizationEnum.NihonShiki.toHiragana(result.query[i]);
        }
        result.isJapanese = true;
        result.matcher = MatcherEnum.Exact;
        deinflections.query = result;
        return deinflections;
    }
}
