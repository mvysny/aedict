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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
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
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the indexer.
 * @author Martin Vysny
 */
public class MainTest {

    @BeforeClass
    public static void index() throws Exception {

        final URL edictGz = Thread.currentThread().getContextClassLoader().getResource("edict.gz");
        if (edictGz == null) {
            throw new AssertionError("The edict.gz resource is missing");
        }
        new Main(new String[]{"-u", edictGz.toString(), "-g"}).run();
        // check that the target file exists
        assertTrue(new File(FileTypeEnum.Edict.getTargetFileName()).exists());
        final File targetFile = new File("target/" + FileTypeEnum.Edict.getTargetFileName());
        targetFile.delete();
        FileUtils.moveFile(new File(FileTypeEnum.Edict.getTargetFileName()), targetFile);
    }

    @Test(expected = ParseException.class)
    public void failOnMissingSwitch() throws Exception {
        new Main(new String[0]).run();
    }
    private IndexReader reader;
    private Searcher searcher;
    private QueryParser parser;

    @Before
    public void initializeLucene() throws IOException {
        reader = IndexReader.open(Main.LUCENE_INDEX);
        searcher = new IndexSearcher(reader);
        parser = new QueryParser("contents", new StandardAnalyzer());
    }

    @After
    public void closeLucene() throws IOException {
        searcher.close();
        reader.close();
    }

    @Test
    public void findMother() throws IOException, org.apache.lucene.queryParser.ParseException {
        final List<String> result = search("mother");
        assertEquals("お母 [おふくろ] /(iK) (n) (col) one's mother/", result.get(0));
        assertEquals(129, result.size());
    }

    @Test
    public void findHaha() throws IOException, org.apache.lucene.queryParser.ParseException {
        final List<String> result = search("はは");
        assertEquals("あはは /(int) a-ha-ha (laughing loudly)/", result.get(0));
        assertEquals(33, result.size());
    }

    /**
     * Tests for the Issue 30: http://code.google.com/p/aedict/issues/detail?id=30
     * @throws IOException
     * @throws org.apache.lucene.queryParser.ParseException
     */
    @Test
    public void findKyou() throws IOException, org.apache.lucene.queryParser.ParseException {
        final List<String> result = search("きょう");
        assertTrue(result.contains("今日 [きょう] /(n-t) today/this day/(P)/"));
        assertEquals("いい度胸 [いいどきょう] /(n,vs) some nerve (as in 'you must have some nerve to ...')/", result.get(0));
        assertEquals(2444, result.size());
    }

    private List<String> search(final String query) throws IOException, org.apache.lucene.queryParser.ParseException {
        final TopDocs docs = searcher.search(parser.parse(query), null, 10000);
        final List<String> result = new ArrayList<String>();
        for (final ScoreDoc sd : docs.scoreDocs) {
            final Document doc = searcher.doc(sd.doc);
            final String contents = doc.get("contents");
            result.add(contents);
        }
        Collections.sort(result);
        return result;
    }
}
