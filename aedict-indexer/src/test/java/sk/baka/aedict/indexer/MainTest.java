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
import java.util.List;
import org.apache.commons.cli.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the indexer.
 * @author Martin Vysny
 */
public class MainTest extends AbstractLuceneSearchTest {

    @BeforeClass
    public static void index() throws Exception {
        Utils.index(null, "edict.gz", FileTypeEnum.Edict);
    }

    @Test(expected = ParseException.class)
    public void failOnMissingSwitch() throws Exception {
        new Main(new String[0]).run();
    }

    @Test
    public void findMother() throws IOException, org.apache.lucene.queryParser.ParseException {
        final List<String> result = search(null, "mother");
        assertEquals("お母 [おふくろ] /(iK) (n) (col) one's mother/", result.get(0));
        assertEquals(129, result.size());
    }

    @Test
    public void findHaha() throws IOException, org.apache.lucene.queryParser.ParseException {
        final List<String> result = search(null, "はは");
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
        final List<String> result = search(null, "きょう");
        assertTrue(result.contains("今日 [きょう] /(n-t) today/this day/(P)/"));
        assertEquals("いい度胸 [いいどきょう] /(n,vs) some nerve (as in 'you must have some nerve to ...')/", result.get(0));
        assertEquals(2444, result.size());
    }

    @Override
    protected String getDefaultFieldName() {
        return "contents";
    }
}
