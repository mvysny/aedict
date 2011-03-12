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
import sk.baka.tools.test.Assert;

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
        assertFalse(matchesSubstring("query", "foobarbaz"));
        assertTrue(matchesSubstring("query", "QUERYQUERY"));
        assertTrue(matchesSubstring("query", "QUERY QUERY"));
        assertTrue(matchesSubstring("query", "queryquery"));
        assertTrue(matchesSubstring("query", "query query"));
        assertTrue(matchesSubstring("query", "query-query"));
        assertTrue(matchesSubstring("query", "query'query"));
        assertTrue(matchesSubstring("query", "query.query"));
        assertTrue(matchesSubstring("query", "query,query"));
        assertTrue(matchesSubstring("query", "query; query"));
        assertTrue(matchesSubstring("query", "foo-bar-baz [f] (p) query; query"));
        assertTrue(matchesSubstring("mother", "(n) (hum) mother/(P)"));
    }

    @Test
    public void complexEdictAndMatchesEng() {
        assertFalse(matchesSubstring("query AND foo", "foobarbaz"));
        assertTrue(matchesSubstring("baz AND foo", "foobarbaz"));
        assertFalse(matches("query AND foo", "foobarbaz"));
        assertFalse(matches("baz AND foo", "foobarbaz"));
        assertTrue(matches("baz AND foo", "baz;foo"));
        assertTrue(matches("baz AND foo", "foo;bar;baz"));
    }
    @Test
    public void complexSubstringEdictMatchesJp() {
        assertTrue(DictTypeEnum.Edict.matchesHandlesAnd(entryjp("あはは"), true, "はは", MatcherEnum.Substring));
        assertFalse(DictTypeEnum.Edict.matchesHandlesAnd(entryjp("あはは"), true, "はは", MatcherEnum.Exact));
        assertTrue(DictTypeEnum.Edict.matchesHandlesAnd(entryjp("はは"), true, "はは", MatcherEnum.Substring));
        assertTrue(DictTypeEnum.Edict.matchesHandlesAnd(entryjp("はは"), true, "はは", MatcherEnum.Exact));
    }

    private DictEntry entryjp(final String jp) {
        return new DictEntry(null, jp, "English");
    }

    private DictEntry entry(final String eng) {
        return new DictEntry(null, null, eng);
    }

    private boolean matchesSubstring(final String query, final String eng) {
        return DictTypeEnum.Edict.matchesHandlesAnd(entry(eng), false, query, MatcherEnum.Substring);
    }

    private boolean matches(final String query, final String eng) {
        return DictTypeEnum.Edict.matchesHandlesAnd(entry(eng), false, query, MatcherEnum.Exact);
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
    public void testEdictQueryCreator() {
        final SearchQuery q = new SearchQuery(DictTypeEnum.Edict);
        q.matcher = MatcherEnum.Exact;
        q.query = new String[]{"foo", "bar"};
        q.isJapanese = true;
        q.validate();
        Assert.assertArrayEquals(DictTypeEnum.Edict.getLuceneQuery(q), new String[]{"(jp:\"WfooW\" OR jp:\"WbarW\") AND common:t", "(jp:\"WfooW\" OR jp:\"WbarW\") AND common:f"});
    }

    @Test
    public void testEdictAndQueryCreator() {
        final SearchQuery q = new SearchQuery(DictTypeEnum.Edict);
        q.matcher = MatcherEnum.StartsWith;
        q.query = new String[]{"foo AND goo", "bar"};
        q.isJapanese = true;
        q.validate();
        Assert.assertArrayEquals(DictTypeEnum.Edict.getLuceneQuery(q), new String[]{"((jp:Wfoo AND jp:Wgoo) OR jp:\"Wbar\") AND common:t", "((jp:Wfoo AND jp:Wgoo) OR jp:\"Wbar\") AND common:f"});
    }

    @Test
    public void testTanakaQueryCreator() {
        final SearchQuery q = new SearchQuery(DictTypeEnum.Tanaka);
        q.query = new String[]{"foo", "bar"};
        q.isJapanese = true;
        Assert.assertArrayEquals(DictTypeEnum.Tanaka.getLuceneQuery(q), new String[]{"(japanese:\"foo\") OR (jp-deinflected:\"foo\") OR (japanese:\"bar\") OR (jp-deinflected:\"bar\")"});
        q.isJapanese = false;
        Assert.assertArrayEquals(DictTypeEnum.Tanaka.getLuceneQuery(q), new String[]{"(english:\"foo\") OR (english:\"bar\")"});
    }
    @Test
    public void testTanakaAndQueryCreator() {
        final SearchQuery q = new SearchQuery(DictTypeEnum.Tanaka);
        q.query = new String[]{"foo AND goo", "bar"};
        q.isJapanese = true;
        Assert.assertArrayEquals(DictTypeEnum.Tanaka.getLuceneQuery(q), new String[]{"(japanese:\"foo\" AND japanese:\"goo\") OR (jp-deinflected:\"foo\" AND jp-deinflected:\"goo\") OR (japanese:\"bar\") OR (jp-deinflected:\"bar\")"});
        q.isJapanese = false;
        Assert.assertArrayEquals(DictTypeEnum.Tanaka.getLuceneQuery(q), new String[]{"(english:\"foo\" AND english:\"goo\") OR (english:\"bar\")"});
    }
}
