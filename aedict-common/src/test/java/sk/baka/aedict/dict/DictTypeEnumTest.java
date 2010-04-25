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

import static org.junit.Assert.*;
import java.text.ParseException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.junit.Test;

/**
 * Tests the {@link DictTypeEnum} class.
 * 
 * @author Martin Vysny
 */
public class DictTypeEnumTest {

    @Test
    public void complexExactEdictMatchesEng() {
        assertFalse(matches("query", "QUERYQUERY"));
        assertFalse(matches("query", "QUERY QUERY"));
        assertFalse(matches("query", "queryquery"));
        assertFalse(matches("query", "query query"));
        assertFalse(matches("query", "query-query"));
        assertFalse(matches("query", "query'query"));
        assertFalse(matches("query", "query.query"));
        assertFalse(matches("query", "query,query"));
        assertTrue(matches("query", "query; query"));
        assertTrue(matches("query", "foo-bar-baz [f] (p) query; query"));
        assertTrue(matches("mother", "(n) (hum) mother/(P)"));
    }

    @Test
    public void complexSubstringEdictMatchesEng() {
        assertFalse(matches2("query", "foobarbaz"));
        assertTrue(matches2("query", "QUERYQUERY"));
        assertTrue(matches2("query", "QUERY QUERY"));
        assertTrue(matches2("query", "queryquery"));
        assertTrue(matches2("query", "query query"));
        assertTrue(matches2("query", "query-query"));
        assertTrue(matches2("query", "query'query"));
        assertTrue(matches2("query", "query.query"));
        assertTrue(matches2("query", "query,query"));
        assertTrue(matches2("query", "query; query"));
        assertTrue(matches2("query", "foo-bar-baz [f] (p) query; query"));
        assertTrue(matches2("mother", "(n) (hum) mother/(P)"));
    }

    @Test
    public void complexSubstringEdictMatchesJp() {
        assertTrue(DictTypeEnum.Edict.matches(entryjp("あはは"), true, "はは", MatcherEnum.Substring));
        assertFalse(DictTypeEnum.Edict.matches(entryjp("あはは"), true, "はは", MatcherEnum.Exact));
        assertTrue(DictTypeEnum.Edict.matches(entryjp("はは"), true, "はは", MatcherEnum.Substring));
        assertTrue(DictTypeEnum.Edict.matches(entryjp("はは"), true, "はは", MatcherEnum.Exact));
    }

    private DictEntry entryjp(final String jp) {
        return new DictEntry(null, jp, "English");
    }

    private DictEntry entry(final String eng) {
        return new DictEntry(null, null, eng);
    }

    private boolean matches2(final String query, final String eng) {
        return DictTypeEnum.Edict.matches(entry(eng), false, query, MatcherEnum.Substring);
    }

    private boolean matches(final String query, final String eng) {
        return DictTypeEnum.Edict.matches(entry(eng), false, query, MatcherEnum.Exact);
    }

    @Test
    public void simpleKatakanaParse() throws ParseException {
        final DictEntry e = DictTypeEnum.Edict.getEntry(doc("aaa / bbb"));
        assertEquals("aaa", e.reading);
        assertEquals("bbb", e.english);
    }

    @Test
    public void simpleHiraganaParse() throws ParseException {
        final DictEntry e = DictTypeEnum.Edict.getEntry(doc("aaa [ccc] / bbb"));
        assertEquals("aaa", e.kanji);
        assertEquals("ccc", e.reading);
        assertEquals("bbb", e.english);
    }

    @Test
    public void parserDropsTrailingSlashes() throws ParseException {
        final DictEntry e = DictTypeEnum.Edict.getEntry(doc("aaa [ccc] / bbb//"));
        assertEquals("aaa", e.kanji);
        assertEquals("ccc", e.reading);
        assertEquals("bbb", e.english);
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleUnsuccessfullParse() throws ParseException {
        DictTypeEnum.Edict.getEntry(doc("aaa"));
    }

    private Document doc(final String edictLine) {
        final Document doc = new Document();
        doc.add(new Field("contents", edictLine, Field.Store.YES, Field.Index.ANALYZED));
        return doc;
    }

    @Test
    public void testTanakaQueryCreator() {
        final SearchQuery q = new SearchQuery(DictTypeEnum.Tanaka);
        q.query = new String[]{"foo", "bar"};
        q.isJapanese = true;
        assertArrayEquals(new String[]{"japanese:foo OR jp-deinflected:foo OR japanese:bar OR jp-deinflected:bar"}, DictTypeEnum.Tanaka.getLuceneQuery(q));
        q.isJapanese = false;
        assertArrayEquals(new String[]{"english:foo OR english:bar"}, DictTypeEnum.Tanaka.getLuceneQuery(q));
    }
}
