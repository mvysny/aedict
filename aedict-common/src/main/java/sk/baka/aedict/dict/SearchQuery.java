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
import java.util.Set;

import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.kanji.VerbDeinflection;

/**
 * Contains values for a search.
 * 
 * @author Martin Vysny
 */
public final class SearchQuery implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * A search query. If multiple queries are specified then all strings which
     * matches at least one query will be shown.
     */
    public String[] query;
    /**
     * If true we are searching for a japanese query. If false the query is for
     * an english word.
     */
    public boolean isJapanese;
    /**
     * A matcher to use when matching query strings to a line.
     */
    public MatcherEnum matcher;
    /**
     * If non-null, defines the stroke count.
     */
    public Integer strokeCount;
    /**
     * If non-null, defines the desired SKIP code.
     */
    public String skip;
    /**
     * If non-null, contains a radical number.
     */
    public Integer radical;
    /**
     * Optional: a positive integer, how much we can deviate from given stroke
     * count.
     */
    public Integer strokesPlusMinus;
    /**
     * The dictionary to use for the search.
     */
    public final DictTypeEnum dictType;

    /**
     * Creates an empty search query.
     *
     * @param dictType
     *            the dictionary type, must not be null.
     */
    public SearchQuery(final DictTypeEnum dictType) {
        super();
        this.dictType = dictType;
    }

    /**
     * Clones given search query.
     *
     * @param other
     *            query to clone
     */
    public SearchQuery(SearchQuery other) {
        this(other.dictType);
        query = other.query.clone();
        isJapanese = other.isJapanese;
        matcher = other.matcher;
        strokeCount = other.strokeCount;
        skip = other.skip;
        radical = other.radical;
    }

    /**
     * When searching for a particular stroke count, SKIP code or radical, the
     * KANJIDIC dictionary is required to be available.
     *
     * @return true if this search query requires a KANJIDIC, false otherwise.
     */
    public boolean requiresKanjidic() {
        return strokeCount != null || skip != null || radical != null;
    }

    /**
     * All query strings are converted to a lower case.
     *
     * @return this
     */
    public SearchQuery toLowerCase() {
        if (query != null) {
            for (int i = 0; i < query.length; i++) {
                query[i] = query[i].toLowerCase();
            }
        }
        return this;
    }

    /**
     * All query strings are trimmed.
     *
     * @return this
     */
    public SearchQuery trim() {
        if (query != null) {
            for (int i = 0; i < query.length; i++) {
                query[i] = query[i].trim();
            }
        }
        return this;
    }

    /**
     * Pretty-prints {@link #query} as query1/query2/...
     *
     * @return pretty-printed query.
     */
    public String prettyPrintQuery() {
        if (query == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (final String q : query) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append('/');
            }
            sb.append(q);
        }
        return sb.toString();
    }

    /**
     * Search for given kanji. Useful with KANJIDIC.
     *
     * @param kanji
     *            the kanji to search for.
     * @param strokes
     *            optional number of strokes
     * @param strokesPlusMinus
     *            optional: a positive integer, how much we can deviate from
     *            given stroke count.
     * @return a search query instance, never null.
     */
    public static SearchQuery kanjiSearch(final char kanji, final Integer strokes, final Integer strokesPlusMinus) {
        final SearchQuery result = new SearchQuery(DictTypeEnum.Kanjidic);
        result.isJapanese = true;
        result.matcher = MatcherEnum.Exact;
        result.query = new String[]{String.valueOf(kanji)};
        result.strokeCount = strokes;
        result.strokesPlusMinus = strokesPlusMinus;
        return result;
    }

    /**
     * Creates an EDICT query which searches for a japanese term.
     *
     * @param word
     *            the word to search, in japanese language. Full-width katakana
     *            conversion is not performed automatically.
     * @param exact
     *            if true then performs exact search, if false then performs a
     *            substring search.
     * @return search query
     */
    public static SearchQuery searchForJapanese(final String word, final boolean exact) {
        final SearchQuery result = new SearchQuery(DictTypeEnum.Edict);
        result.query = new String[]{word};
        result.isJapanese = true;
        result.matcher = exact ? MatcherEnum.Exact : MatcherEnum.Substring;
        return result;
    }

    /**
     * Creates an EDICT query which searches for an English term.
     *
     * @param word
     *            the word to search, in English language.
     * @param exact
     *            if true then performs exact search, if false then performs a
     *            substring search.
     * @return search query
     */
    public static SearchQuery searchForEnglish(final String word, final boolean exact) {
        final SearchQuery result = new SearchQuery(DictTypeEnum.Edict);
        result.query = new String[]{word};
        result.isJapanese = false;
        result.matcher = exact ? MatcherEnum.Exact : MatcherEnum.Substring;
        return result;
    }

    /**
     * Creates an EDICT query which searches for a japanese term.
     *
     * @param word
     *            the word to search, in japanese language, may contain romaji.
     *            Full-width katakana conversion is performed automatically. Not
     *            null
     * @param romanization
     *            the romanization system to use, not null.
     * @param exact
     *            if true then performs exact search, if false then performs a
     *            substring search.
     * @param isDeinflect
     *            if true then the word is expected to be a verb which is
     *            deinflected.
     * @param isSearchInExamples if true then the search will be performed in Tanaka examples.
     * @return search query, never null
     */
    public static SearchQuery searchForRomaji(final String word, final RomanizationEnum romanization, final boolean exact, final boolean isDeinflect, final boolean isSearchInExamples) {
        final SearchQuery result = new SearchQuery(isSearchInExamples ? DictTypeEnum.Tanaka : DictTypeEnum.Edict);
        String conv = KanjiUtils.halfwidthToKatakana(word);
        if (isDeinflect) {
            final String romaji = RomanizationEnum.NihonShiki.toRomaji(romanization.toHiragana(word));
            final Set<String> deinflections = VerbDeinflection.deinflect(romaji);
            result.query = deinflections.toArray(new String[0]);
            for (int i = 0; i < result.query.length; i++) {
                result.query[i] = RomanizationEnum.NihonShiki.toHiragana(result.query[i]);
            }
        } else {
            result.query = new String[]{romanization.toKatakana(conv), romanization.toHiragana(conv)};
        }
        result.isJapanese = true;
        result.matcher = exact || isDeinflect ? MatcherEnum.Exact : MatcherEnum.Substring;
        return result;
    }
}
