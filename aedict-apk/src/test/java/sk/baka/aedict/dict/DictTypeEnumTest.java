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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
	public void complexExactEdictMatches() {
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

	private EdictEntry entry(final String eng) {
		return new EdictEntry(null, null, eng);
	}

	private boolean matches(final String query, final String eng) {
		return DictTypeEnum.Edict.matches(entry(eng), false, query, MatcherEnum.Exact);
	}

	@Test
	public void simpleKatakanaParse() throws ParseException {
		final EdictEntry e = DictTypeEnum.Edict.getEntry(doc("aaa / bbb"));
		assertEquals("aaa", e.reading);
		assertEquals("bbb", e.english);
	}

	@Test
	public void simpleHiraganaParse() throws ParseException {
		final EdictEntry e = DictTypeEnum.Edict.getEntry(doc("aaa [ccc] / bbb"));
		assertEquals("aaa", e.kanji);
		assertEquals("ccc", e.reading);
		assertEquals("bbb", e.english);
	}

	@Test
	public void parserDropsTrailingSlashes() throws ParseException {
		final EdictEntry e = DictTypeEnum.Edict.getEntry(doc("aaa [ccc] / bbb//"));
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
		doc.add(new Field("contents", edictLine, Field.Store.COMPRESS, Field.Index.NO));
		return doc;
	}
}
