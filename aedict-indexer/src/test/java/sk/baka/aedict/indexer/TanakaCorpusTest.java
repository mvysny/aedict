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

import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests indexing of the tanaka corpus.
 * @author Martin Vysny
 */
public class TanakaCorpusTest extends AbstractLuceneSearchTest {

    @BeforeClass
    public static void index() throws Exception {
        Utils.index("-t", "examples.gz", FileTypeEnum.Tanaka);
    }

    @Override
    protected String getDefaultFieldName() {
        return "english";
    }

    @Test
    public void testSimpleEnglishSearch() throws Exception {
        List<String> s = search(null, "pretty");
        assertEquals(210, s.size());
        assertEquals("\"That's the new head of the student council?\" \"Cool, isn't he?\" \"Rather, pretty boy?\"", s.get(0));
        s = search("japanese", "pretty");
        assertEquals(210, s.size());
        assertEquals("「これはとてもおもしろそうだね」と博が言います。", s.get(0));
    }
}
