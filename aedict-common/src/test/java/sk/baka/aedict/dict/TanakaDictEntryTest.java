package sk.baka.aedict.dict;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the {@link TanakaDictEntry} class.
 * @author Martin Vysny
 */
public class TanakaDictEntryTest {

    @Test
    public void testNullWordParse() {
        assertNull(new TanakaDictEntry(null, null, "foo", null).wordList);
    }

    @Test
    public void testWhitespaceWordParse() {
        List<String> words = new TanakaDictEntry(null, null, "foo", "                ").wordList;
        assertEquals(0, words.size());
        words = new TanakaDictEntry(null, null, "foo", "     fooo    bar         ").wordList;
        assertEquals("fooo", words.get(0));
        assertEquals("bar", words.get(1));
        assertEquals(2, words.size());
    }

    @Test
    public void testWordParse() {
        List<String> words = new TanakaDictEntry(null, null, "foo", "I can haz kanji.").wordList;
        assertEquals(4, words.size());
        assertEquals("I", words.get(0));
        assertEquals("can", words.get(1));
        assertEquals("haz", words.get(2));
        assertEquals("kanji.", words.get(3));
    }
}
