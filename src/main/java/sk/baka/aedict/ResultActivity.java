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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
			final Set<Integer> idxOffsets = new HashSet<Integer>();
			for (final ScoreDoc sd : result.scoreDocs) {
				final Document doc = searcher.doc(sd.doc);
				final int idxOffset = Integer.parseInt(doc.get("path")) * 4;
				idxOffsets.add(idxOffset);
			}
			for (final Integer offset : getStartingOffset(idxOffsets)) {
				r.addAll(performSearch(query, offset));
			}
		} finally {
			reader.close();
		}
		return r;
	}

	/**
	 * Translates all IDX file offsets into pointers to the EDICT file.
	 * 
	 * @param idxOffsets
	 *            a collection of offsets into the
	 *            {@link DownloadEdictTask#LINE_INDEX} file.
	 * @return translated offsets into the EDICT file.
	 * @throws IOException
	 */
	private List<Integer> getStartingOffset(final Collection<Integer> idxOffsets)
			throws IOException {
		final List<Integer> offsets = new ArrayList<Integer>();
		final RandomAccessFile ra = new RandomAccessFile(
				DownloadEdictTask.LINE_INDEX, "r");
		try {
			for (final Integer idxOffset : idxOffsets) {
				ra.seek(idxOffset);
				offsets.add(ra.readInt());
			}
		} finally {
			MiscUtils.closeQuietly(ra);
		}
		return offsets;
	}

	/**
	 * Performs a quick byte-comparison search on
	 * {@value DownloadEdictTask#LINES_PER_INDEXABLE_ITEM} lines of the edict
	 * file, starting at given file offset.
	 * 
	 * @param query
	 *            the query object
	 * @param seekTo
	 *            start reading the lines from this position
	 * @return list of matched lines from the edict file.
	 * @throws IOException
	 *             on i/o error
	 */
	private List<String> performSearch(final SearchQuery query, final int seekTo)
			throws IOException {
		final List<String> result = new ArrayList<String>();
		final byte[][] queries = new byte[query.query.length][];
		int i = 0;
		for (final String q : query.query) {
			queries[i++] = q.getBytes("EUC-JP");
		}
		final InputStream in = new FileInputStream("/sdcard/aedict/edict");
		try {
			int seekBytes = seekTo;
			while (seekBytes > 0) {
				final long skipped = in.skip(seekBytes);
				if (skipped == 0) {
					throw new IOException("Cannot skip " + seekBytes + " bytes");
				}
				seekBytes -= skipped;
			}
			final LineReadInputStream edict = new LineReadInputStream(in);
			int linesToRead = DownloadEdictTask.LINES_PER_INDEXABLE_ITEM;
			while ((linesToRead-- > 0) && edict.readLine()) {
				for (final byte[] q : queries) {
					if (contains(q, edict)) {
						final String line = new String(edict.buffer,
								edict.lineStart, edict.lineLength, "EUC-JP");
						result.add(line);
					}
				}
			}
		} finally {
			MiscUtils.closeQuietly(in);
		}
		return result;
	}

	/**
	 * Checks if given sub-array is contained in current line of given line
	 * reader.
	 * 
	 * @param subarray
	 *            the sub-array to check.
	 * @param in
	 *            the line reader. Only the current line is checked.
	 * @return true if the sub-array is contained in current line, false
	 *         otherwise.
	 */
	private boolean contains(byte[] subarray, LineReadInputStream in) {
		byte firstChar = subarray[0];
		int matched = 0;
		final byte[] array = in.buffer;
		final int end = in.lineStart + in.lineLength - subarray.length + 1;
		for (int i = in.lineStart; i < end; i++) {
			if (matched == 0) {
				if (array[i] != firstChar) {
					continue;
				}
				matched++;
				continue;
			}
			if (array[i] != subarray[matched]) {
				matched = 0;
				continue;
			}
			matched++;
			if (matched >= subarray.length) {
				return true;
			}
		}
		return false;
	}
}
