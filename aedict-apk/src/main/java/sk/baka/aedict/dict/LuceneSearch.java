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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import sk.baka.aedict.util.IOExceptionWithCause;
import sk.baka.autils.MiscUtils;

/**
 * Allows Lucene search for a query.
 * 
 * @author Martin Vysny
 */
public final class LuceneSearch implements Closeable {
	private final IndexReader reader;
	private final Searcher searcher;
	private final QueryParser parser;
	/**
	 * The dictionary type.
	 */
	private final DictTypeEnum dictType;

	/**
	 * Creates the object and opens the index file.
	 * 
	 * @param dictType
	 *            the dictionary we will use for the search.
	 * @param dictionaryPath
	 *            overrides default dictionary location if non-null. An absolute
	 *            os-specific path, e.g. /sdcard/aedict/index.
	 * @throws IOException
	 *             on I/O error.
	 */
	public LuceneSearch(final DictTypeEnum dictType, final String dictionaryPath) throws IOException {
		this.dictType = dictType;
		reader = IndexReader.open(dictionaryPath != null ? dictionaryPath : dictType.getDefaultDictionaryPath());
		searcher = new IndexSearcher(reader);
		parser = new QueryParser("contents", new StandardAnalyzer());
	}

	/**
	 * Performs a search. Returns a maximum of 100 results.
	 * 
	 * @param query
	 *            the query to search for.
	 * @return a result list, never null, may be empty.
	 * @throws IOException
	 *             on I/O error.
	 */
	public List<DictEntry> search(final SearchQuery query) throws IOException {
		return search(query, 100);
	}

	/**
	 * Performs a search.
	 * 
	 * @param query
	 *            the query to search for.
	 * @param maxResults
	 *            the maximum number of results to list
	 * @return a result list, never null, may be empty.
	 * @throws IOException
	 *             on I/O error.
	 */
	private List<DictEntry> searchInternal(final SearchQuery query, final int maxResults) throws IOException {
		final List<DictEntry> r = new ArrayList<DictEntry>();
		final String[] queries = dictType.getLuceneQuery(query);
		// 5000 is just an approximate value.
		// we are searching for an exact match. We cannot simply grab the first
		// "maxResults" results and filter out non-exact results - we can filter
		// out all results this way, and the real, exact matches, may remain
		// unretrieved by Lucene. TODO perhaps a better Lucene query might help.
		final int maxLuceneResults = query.matcher == MatcherEnum.Exact ? 5000 : maxResults;
		int resultsToFind = maxLuceneResults;
		for (final String q : queries) {
			// gradually walk through the queries and fill the result list.
			final Query parsedQuery;
			try {
				parsedQuery = parser.parse(q);
			} catch (ParseException e) {
				// not expected - the SearchQuery object should produce valid
				// query strings... indicates a bug in Aedict code.
				throw new RuntimeException(e);
			}
			final TopDocs result = searcher.search(parsedQuery, null, resultsToFind);
			for (final ScoreDoc sd : result.scoreDocs) {
				final Document doc = searcher.doc(sd.doc);
				final DictEntry entry = dictType.tryGetEntry(doc, query);
				if (entry != null) {
					r.add(entry);
					if (r.size() >= maxResults) {
						break;
					}
				}
			}
			resultsToFind = maxLuceneResults - r.size();
			if (resultsToFind <= 0) {
				break;
			}
		}
		return r;
	}

	/**
	 * Performs a search.
	 * 
	 * @param query
	 *            the query to search for.
	 * @param maxResults
	 *            the maximum number of results to list
	 * @return a result list, never null, may be empty.
	 * @throws IOException
	 *             on I/O error.
	 */
	public List<DictEntry> search(final SearchQuery query, final int maxResults) throws IOException {
		try {
			return searchInternal(query, maxResults);
		} catch (IOException ex) {
			// catch the "read past EOF" IO exception which indicates that the
			// dictionary files are corrupted. See
			// http://code.google.com/p/aedict/issues/detail?id=55 for details
			if ("read past EOF".equals(ex.getMessage())) {
				throw new IOExceptionWithCause(AedictApp.getStr(R.string.dictFilesCorrupted) + ": " + ex.getMessage(), ex);
			}
			throw ex;
		}
	}

	public void close() throws IOException {
		searcher.close();
		reader.close();
	}

	/**
	 * A handy method to perform a quick search. For performing multiple search
	 * queries please use the {@link #search(SearchQuery)} method.
	 * 
	 * @param query
	 *            the query
	 * @param dictionaryPath
	 *            overrides default dictionary location if non-null. An absolute
	 *            os-specific path, e.g. /sdcard/aedict/index.
	 * @return a list of matched lines, never null.
	 * @throws IOException
	 *             on I/O error.
	 */
	public static List<DictEntry> singleSearch(final SearchQuery query, final String dictionaryPath) throws IOException {
		final LuceneSearch s = new LuceneSearch(query.dictType, dictionaryPath);
		try {
			return s.search(query);
		} finally {
			MiscUtils.closeQuietly(s);
		}
	}
}
