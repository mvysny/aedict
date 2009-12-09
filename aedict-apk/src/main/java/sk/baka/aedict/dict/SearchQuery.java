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

import sk.baka.autils.ListBuilder;
import android.content.Intent;

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
	 * Creates an empty search query.
	 */
	public SearchQuery() {
		super();
	}

	/**
	 * Checks if given line matches the query.
	 * 
	 * @param line
	 *            the line from the EDict file
	 * @return true if the line matched, false otherwise.
	 */
	public boolean matches(final String line) {
		if (query == null) {
			return true;
		}
		for (final String q : query) {
			if (matcher.matches(q, line)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a Lucene query which matches this query as close as possible.
	 * 
	 * @param kanjidic
	 *            if true we will search in a kanjidic, if false, we will search
	 *            in edict.
	 * @return the Apache Lucene query
	 */
	public String getLuceneQuery(final boolean kanjidic) {
		if (!kanjidic) {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < query.length; i++) {
				sb.append(query[i].trim());
				if (i < query.length - 1) {
					sb.append(" OR ");
				}
			}
			return sb.toString();
		}
		// query can be null in case we are performing e.g. a pure SKIP
		// lookup
		final ListBuilder qb = new ListBuilder(" AND ");
		if (query != null) {
			if (query.length != 1) {
				throw new IllegalStateException("Kanjidic search requires a single kanji character search");
			}
			qb.add("kanji:" + query[0].trim());
		}
		if (strokeCount != null) {
			final StringBuilder sb = new StringBuilder();
			sb.append('(');
			boolean first = true;
			final int plusMinus = strokesPlusMinus == null ? 0 : strokesPlusMinus;
			if ((plusMinus > 3) || (plusMinus < 0)) {
				throw new IllegalStateException("Invalid value: " + strokesPlusMinus);
			}
			for (int strokes = strokeCount - plusMinus; strokes <= strokeCount + plusMinus; strokes++) {
				if (first) {
					first = false;
				} else {
					sb.append(" OR ");
				}
				sb.append("strokes:").append(strokes);
			}
			sb.append(')');
			qb.add(sb.toString());
		}
		if (skip != null) {
			qb.add("skip:" + skip);
		}
		if (radical != null) {
			qb.add("radical:" + radical);
		}
		return qb.toString();
	}

	/**
	 * Clones given search query.
	 * 
	 * @param other
	 *            query to clone
	 */
	public SearchQuery(SearchQuery other) {
		this();
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
	 * Retrieves values for the query from given intent.
	 * 
	 * @param intent
	 *            the intent to parse
	 * @return query instance.
	 */
	public static SearchQuery fromIntent(final Intent intent) {
		return (SearchQuery) intent.getSerializableExtra(INTENTKEY_SEARCH_QUERY);
	}

	private static final String INTENTKEY_SEARCH_QUERY = "QUERY";

	/**
	 * Puts values from this bean to given intent. The object can be
	 * reconstructed later by using {@link #fromIntent(Intent)}.
	 * 
	 * @param intent
	 *            store the values here.
	 */
	public void putTo(Intent intent) {
		intent.putExtra(INTENTKEY_SEARCH_QUERY, this);
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
		final SearchQuery result = new SearchQuery();
		result.isJapanese = true;
		result.matcher = MatcherEnum.ExactMatchEng;
		result.query = new String[] { String.valueOf(kanji) };
		result.strokeCount = strokes;
		result.strokesPlusMinus = strokesPlusMinus;
		return result;
	}
}
