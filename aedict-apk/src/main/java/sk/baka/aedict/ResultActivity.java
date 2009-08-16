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

package sk.baka.aedict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * Performs a search and shows search result.
 * 
 * @author Martin Vysny
 */
public class ResultActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		List<String> list;
		final SearchQuery query = SearchQuery.fromIntent(getIntent())
				.toLowerCase();
		if (MiscUtils.isBlank(query.query)) {
			// nothing to search for
			list = Collections.singletonList("Nothing to search for");
		} else {
			try {
				list = performLuceneSearch(query);
			} catch (Exception ex) {
				Log.e(ResultActivity.class.getSimpleName(),
						"Failed to perform search", ex);
				list = Collections.singletonList("Failed to perform search: "
						+ ex);
			}
			if (list.isEmpty()) {
				list = Collections.singletonList("No results");
			}
		}
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list));
	}

	private List<String> performLuceneSearch(final SearchQuery query)
			throws IOException, ParseException {
		final List<String> r = new ArrayList<String>();
		final IndexReader reader = IndexReader
				.open(DownloadEdictTask.LUCENE_INDEX);
		try {
			final Searcher searcher = new IndexSearcher(reader);
			final QueryParser parser = new QueryParser("contents",
					new StandardAnalyzer());
			final Query parsedQuery = parser.parse(query.getLuceneQuery());
			final TopDocs result = searcher.search(parsedQuery, null, 100);
			for (final ScoreDoc sd : result.scoreDocs) {
				final Document doc = searcher.doc(sd.doc);
				final String contents = doc.get("contents");
				if (query.matches(contents)) {
					r.add(contents);
				}
			}
		} finally {
			reader.close();
		}
		return r;
	}
}
