/**
Aedict - an EDICT browser for Android
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
package sk.baka.aedict.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.junit.After;
import org.junit.Before;

/**
 * Contains support for Lucene search.
 * @author Martin Vysny
 */
public abstract class AbstractLuceneSearchTest {

    private IndexReader reader;
    private Searcher searcher;
    private QueryParser parser;

    protected abstract String getDefaultFieldName();

    @Before
    public void initializeLucene() throws IOException {
        reader = IndexReader.open(Main.LUCENE_INDEX);
        searcher = new IndexSearcher(reader);
        parser = new QueryParser(getDefaultFieldName(), new StandardAnalyzer());
    }

    @After
    public void closeLucene() throws IOException {
        searcher.close();
        reader.close();
    }

    protected final List<String> search(final String fieldName, final String query) throws IOException, org.apache.lucene.queryParser.ParseException {
        final TopDocs docs = searcher.search(parser.parse(query), null, 10000);
        final List<String> result = new ArrayList<String>();
        for (final ScoreDoc sd : docs.scoreDocs) {
            final Document doc = searcher.doc(sd.doc);
            final String contents = doc.get(fieldName == null ? getDefaultFieldName() : "contents");
            result.add(contents);
        }
        Collections.sort(result);
        return result;
    }
}
