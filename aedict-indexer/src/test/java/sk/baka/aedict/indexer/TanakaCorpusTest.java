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
        assertEquals("\"How are you feeling this morning?\" \"Pretty good, thanks.\"", s.get(0));
        s = search("japanese", "english:pretty");
        assertEquals(210, s.size());
        assertEquals("「あれが生徒会長？」「かっこよくない？」「つーか、美形？」", s.get(0));
    }

    @Test
    public void testHiraganaConversion() throws Exception{
        List<String> s = search("kana", "japanese:あれが生徒会長");
        assertEquals(1, s.size());
        assertEquals("「あれ が せいとかい ちょう？」「かっこよくない？」「つーか、びけい？」", s.get(0));
    }

    @Test
    public void testSimpleJapaneseSearch() throws Exception {
        List<String> s = search(null, "japanese:きれい");
        assertEquals(211, s.size());
    }

    @Test
    public void testSimpleJapaneseSearch2() throws Exception {
        List<String> s = search(null, "japanese:きれい");
        assertEquals("\"How pretty she is!\" said Ben to himself.", s.get(0));
    }

    @Test
    public void testSimpleJapaneseSearch3() throws Exception {
        List<String> s = search("japanese", "japanese:きれい");
        assertEquals(211, s.size());
    }

    @Test
    public void testSimpleJapaneseSearch4() throws Exception {
        List<String> s = search("japanese", "japanese:きれい");
        assertEquals("「きれいな宝石ですね」、適当な話題かどうかわからないが、とりあえずそう水を向けてみた。", s.get(0));
    }

    @Test
    public void testSimpleJapaneseSearch5() throws Exception {
        List<String> s = search("japanese", "jp-deinflected:可也");
        assertEquals("「今朝は調子かいかがですか」「かなりいいですよ、ありがとう」", s.get(0));
        assertEquals(255, s.size());
    }

    @Test
    public void testJapaneseSearchForHaha() throws Exception {
        List<String> s = search("japanese", "japanese:母 OR jp-deinflected:母 OR japanese:はは OR jp-deinflected:はは");
        assertEquals("※基本的な禁止事項（誹謗・中傷の禁止等）は「はじめにお読み下さい」に記載してあります。必ずお読みください。", s.get(0));
        assertEquals(1653, s.size());
    }
}
