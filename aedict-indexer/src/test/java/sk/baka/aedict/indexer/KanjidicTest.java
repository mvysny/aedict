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
 * Tests the Kanjidic support.
 * @author Martin Vysny
 */
public class KanjidicTest extends AbstractLuceneSearchTest {

    @Override
    protected String getDefaultFieldName() {
        return "kanji";
    }

    @BeforeClass
    public static void index() throws Exception {
        Utils.index("-k", "kanjidic.gz", FileTypeEnum.Kanjidic);
    }

    @Test
    public void testSimpleSearchForYomuKanji() throws Exception {
        List<String> s = search(null, "読");
        assertEquals(1, s.size());
        assertEquals("読", s.get(0));
        s = search("skip", "kanji:読");
        assertEquals(1, s.size());
        assertEquals("1-7-7", s.get(0));
    }
}
