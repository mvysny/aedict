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

import sk.baka.aedict.util.MiscUtils;

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
	 * If true then we are using Kanjidic for search. If false, we are using
	 * edict.
	 */
	private final boolean kanjidic;

	/**
	 * Creates the object and opens the index file.
	 * 
	 * @param kanjidic
	 *            If true then we are using Kanjidic for search. If false, we
	 *            are using edict.
	 * @throws IOException
	 *             on I/O error.
	 */
	public LuceneSearch(final boolean kanjidic) throws IOException {
		this.kanjidic = kanjidic;
		reader = IndexReader.open(kanjidic ? DownloadDictTask.LUCENE_INDEX_KANJIDIC : DownloadDictTask.LUCENE_INDEX);
		searcher = new IndexSearcher(reader);
		parser = new QueryParser("contents", new StandardAnalyzer());
	}

	/**
	 * Performs a search.
	 * 
	 * @param query
	 *            the query to search for.
	 * @return a result list, never null, may be empty.
	 * @throws IOException
	 *             on I/O error.
	 */
	public List<String> search(final SearchQuery query) throws IOException {
		final List<String> r = new ArrayList<String>();
		final Query parsedQuery;
		try {
			parsedQuery = parser.parse(query.getLuceneQuery(kanjidic));
		} catch (ParseException e) {
			// not expected - the SearchQuery object should produce valid query
			// strings... indicates a bug in Aedict code.
			throw new RuntimeException(e);
		}
		final TopDocs result = searcher.search(parsedQuery, null, 100);
		for (final ScoreDoc sd : result.scoreDocs) {
			final Document doc = searcher.doc(sd.doc);
			final String contents = doc.get("contents");
			if (query.matches(contents)) {
				r.add(contents);
			}
		}
		return r;
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
	 * @param kanjidic
	 *            If true then we are using Kanjidic for search. If false, we
	 *            are using edict.
	 * @return a list of matched lines, never null.
	 * @throws IOException
	 *             on I/O error.
	 */
	public static List<String> singleSearch(final SearchQuery query, final boolean kanjidic) throws IOException {
		final LuceneSearch s = new LuceneSearch(kanjidic);
		try {
			return s.search(query);
		} finally {
			MiscUtils.closeQuietly(s);
		}
	}
}
