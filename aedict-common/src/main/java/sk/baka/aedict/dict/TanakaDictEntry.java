package sk.baka.aedict.dict;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Tanaka entry.
 * @author Martin Vysny
 */
public class TanakaDictEntry extends DictEntry {

    private static final long serialVersionUID = 1L;
    /**
     * A list of words in the sentence. The words are deinflected and in the dictionary form. May be null with older Tanaka dictionary builds which did not had this information.
     */
    public final List<String> wordList;

    public TanakaDictEntry(String kanji, String reading, String english, final String words) {
        super(kanji, reading, english);
        if (words == null) {
            wordList = null;
        } else {
            wordList = new ArrayList<String>();
            for (final String word : words.split("\\s")) {
                final String w = word.trim();
                if (w.length() > 0) {
                    wordList.add(w);
                }
            }
        }
    }
}
