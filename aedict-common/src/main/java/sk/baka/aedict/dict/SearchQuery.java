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

import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;

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
     * Creates an EDICT query which searches for an English term.
     *
     * @param word
     *            the word to search, in English language.
     * @param exact
     *            if true then performs exact search, if false then performs a
     *            substring search.
     * @return search query
     */
    public static SearchQuery searchEnEdict(final String word, final boolean exact) {
        final SearchQuery result = new SearchQuery(DictTypeEnum.Edict);
        result.query = new String[]{word};
        result.isJapanese = false;
        result.matcher = exact ? MatcherEnum.Exact : MatcherEnum.Substring;
        return result;
    }

    private static String[] parseQuery(final String query, final boolean isJapanese, final RomanizationEnum r) {
	if (isJapanese) {
	    final String conv = KanjiUtils.halfwidthToKatakana(query);
	    final ListBuilder k = new ListBuilder(" AND ");
	    final ListBuilder h = new ListBuilder(" AND ");
	    for (final String token : conv.split("\\s+AND\\s+")) {
		k.add(r.toKatakana(token));
		h.add(r.toHiragana(token));
	    }
	    return new String[]{k.toString(), h.toString()};
	} else {
	    return new String[]{query};
	}
    }

    /**
     * Creates an EDICT query which searches for a japanese term.
     *
     * @param verb
     *            the word to search, in japanese language. Must contain only katakana, hiragana and kanji. Not null.
     * @param matcher
     *              the matcher to use
     * @return search query, never null
     */
    public static SearchQuery searchJpEdict(final String text, final MatcherEnum matcher) {
        final SearchQuery result = new SearchQuery(DictTypeEnum.Edict);
        final String conv = KanjiUtils.halfwidthToKatakana(text);
        result.query = new String[]{conv};
        result.isJapanese = true;
        result.matcher = matcher;
        return result;
    }

    /**
     * Creates an EDICT query which searches for a japanese or english term in the Tanaka example dictionary.
     *
     * @param word
     *            the word to search, in japanese language, may contain romaji.
     *            Full-width katakana conversion is performed automatically. Not
     *            null
     * @param romanization
     *            the romanization system to use, not null.
     * @return search query, never null
     */
    public static SearchQuery searchTanaka(final String word, final boolean isJapanese, final RomanizationEnum romanization) {
        final SearchQuery result = new SearchQuery(DictTypeEnum.Tanaka);
	result.query = parseQuery(word, isJapanese, romanization);
        result.isJapanese = isJapanese;
        result.matcher = MatcherEnum.Substring;
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
     * @param isSearchInExamples if true then the search will be performed in Tanaka examples.
     * @return search query, never null
     */
    public static SearchQuery searchJpRomaji(final String word, final RomanizationEnum romanization, final MatcherEnum matcher) {
        final SearchQuery result = new SearchQuery(DictTypeEnum.Edict);
        result.query = parseQuery(word, true, romanization);
        result.isJapanese = true;
        result.matcher = matcher;
        return result;
    }

    public void validate() {
        // query may be blank when searching for kanjis based on SKIP number (see SkipActivity for details).
        if (dictType != DictTypeEnum.Kanjidic && MiscUtils.isBlank(query)) {
            throw new IllegalStateException("No query specified");
        }
        if (matcher == null) {
            throw new IllegalStateException("Matcher must not be null");
        }
        switch (dictType) {
            case Edict: {
                if (!isJapanese && matcher != MatcherEnum.Exact && matcher != MatcherEnum.Substring) {
                    throw new IllegalStateException("Edict eng search: invalid matcher type " + matcher);
                }
            }
            break;
            case Tanaka: {
                if (matcher != MatcherEnum.Substring) {
                    throw new IllegalStateException("Tanaka search: matcher must be Substring but is " + matcher);
                }
            }
            break;
            case Kanjidic: {
                if (matcher != MatcherEnum.Exact) {
                    throw new IllegalStateException("Tanaka search: matcher must be Substring but is " + matcher);
                }
            }
            break;
        }
    }
}
