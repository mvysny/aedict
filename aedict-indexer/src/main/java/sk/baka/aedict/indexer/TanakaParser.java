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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.autils.MiscUtils;

/**
 * Parses Tanaka dictionary.
 * @author Martin Vysny
 */
public class TanakaParser implements IDictParser {

    /**
     * Maps entry word (kanji+hiragana) to its hiragana reading. Does not contain katakana nor pure hiragana entries as it
     * is only used to get the kana transcription of Tanaka entries.
     */
    private final Map<String, String> edict = new HashMap<String, String>();
    /**
     * Check if the {@link #edict} entry is common or not. Incommon entries may get overwritten.
     */
    private final Map<String, Boolean> entryIsCommon = new HashMap<String, Boolean>();

    public TanakaParser() {
        // quickly parse the EDICT dictionary, we are going to need it when constructing the kana reading of the example sentence.
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("edict.gz")), "EUC_JP"));
            try {
                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    if (line.startsWith("#") || MiscUtils.isBlank(line) || line.startsWith("　？？？")) {
                        // skip
                        continue;
                    }
                    final String[] tokens = line.split("\\[|\\]|\\/");
                    final String kanji = tokens[0].trim();
                    if (!containsKanji(kanji)) {
                        continue;
                    }
                    final String reading = tokens[1].trim();
                    final String previous = edict.get(kanji);
                    if ((previous == null) || (!entryIsCommon.get(kanji))) {
                        edict.put(kanji, reading);
                        entryIsCommon.put(kanji, line.endsWith("(P)"));
                    }
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Tanaka parser requires edict.gz to be available at " + new File("").getAbsolutePath(), ex);
        }
    }

    static boolean containsKanji(final String str) {
        for (int i = 0; i < str.length(); i++) {
            if (KanjiUtils.isKanji(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    private String lastLine = null;

    public boolean addLine(String line, Document doc) {
        if (line.startsWith("A: ")) {
            lastLine = line.substring(3);
            lastLine = lastLine.substring(0, lastLine.indexOf('\t'));
            final ArrayList<Object> parsed = Collections.list(new StringTokenizer(line.substring(3), "\t#"));
            final String japanese = (String) parsed.get(0);
            final String english = (String) parsed.get(1);
            doc.add(new Field("japanese", japanese, Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("english", english, Field.Store.YES, Field.Index.ANALYZED));
            return false;
        }
        if (!line.startsWith("B: ")) {
            throw new IllegalArgumentException("The TanakaCorpus file has unexpected format: line " + line);
        }
        // gather all words in their dictionary form.
        final List<BWord> words = parseWords(line);
        final StringBuilder wordList = new StringBuilder();
        for (final BWord word : words) {
            if (word.isInflected()) {
                // the word is inflected. Retrieve the xxx part and add it
                wordList.append(word.dictionaryForm).append(' ');
            }
        }
        doc.add(new Field("jp-deinflected", wordList.toString(), Field.Store.NO, Field.Index.ANALYZED));
        // prepare the kana form of the sentence.
        final StringBuilder kana = new StringBuilder();
        String l = lastLine;
        for (final BWord word : words) {
            final int wordIndex = l.indexOf(word.getInSentence());
            if (wordIndex < 0) {
                //throw new IllegalArgumentException("Line " + lastLine + " does not contain word " + word);
                // this seems to be quite a common case. Just skip the word
                continue;
            }
            kana.append(l.substring(0, wordIndex));
            try {
                kana.append(word.toKana());
            } catch (Exception ex) {
//                System.out.println(ex.getMessage());
                // untranslatable word. just keep the original one
                kana.append(word.getInSentence());
            }
            l = l.substring(wordIndex + word.getInSentence().length());
        }
        kana.append(l);
        doc.add(new Field("kana", CompressionTools.compressString(kana.toString()), Field.Store.YES));
        return true;
    }

    private List<BWord> parseWords(final String bLine) {
        final List<BWord> result = new ArrayList<BWord>();
        final ArrayList<Object> words = Collections.list(new StringTokenizer(bLine.substring(3)));
        for (final Object w : words) {
            result.add(new BWord((String) w));
        }
        return result;
    }

    public void onFinish() {
        // do nothing
    }

    /**
     * Parses a word stored on the B line of the Tanaka file. See http://www.edrdg.org/wiki/index.php/Tanaka_Corpus for details.
     */
    private class BWord {

        public BWord(final String word) {
            String _hiraganaReading = null;
            Integer _senseNumber = null;
            String _wordInSentence = null;
            boolean _isChecked = false;
            boolean inRoundBraces = false;
            boolean inSquareBraces = false;
            boolean inCurlyBraces = false;
            final StringTokenizer t = new StringTokenizer(word, "{}[]()~", true);
            dictionaryForm = t.nextToken();
            while (t.hasMoreTokens()) {
                final String token = t.nextToken();
                if (token.equals("~")) {
                    _isChecked = true;
                } else if (token.equals("(")) {
                    inRoundBraces = true;
                } else if (token.equals(")")) {
                    inRoundBraces = false;
                } else if (token.equals("{")) {
                    inCurlyBraces = true;
                } else if (token.equals("}")) {
                    inCurlyBraces = false;
                } else if (token.equals("[")) {
                    inSquareBraces = true;
                } else if (token.equals("]")) {
                    inSquareBraces = false;
                } else {
                    if (inRoundBraces) {
                        _hiraganaReading = token;
                    } else if (inSquareBraces) {
                        _senseNumber = Integer.valueOf(token);
                    } else if (inCurlyBraces) {
                        _wordInSentence = token;
                    }
                }
            }
            isChecked = _isChecked;
            hiraganaReading = _hiraganaReading;
            senseNumber = _senseNumber;
            wordInSentence = _wordInSentence;
        }
        /**
         * The dictionary form of the word.
         */
        public final String dictionaryForm;
        /**
         * A reading in hiragana. This is to resolve cases where the word can be read different ways. WWWJDIC uses this to ensure that only the appropriate sentences are linked. The reading is in "round" parentheses. May be null.
         */
        public final String hiraganaReading;
        /**
         * A sense number. This occurs when the word has multiple senses in the EDICT file, and indicates which sense applies in the sentence. WWWJDIC displays these numbers. The sense number is in "square" parentheses. May be null.
         */
        public final Integer senseNumber;
        /**
         * The form in which the word appears in the sentence. This will differ from the indexing word if it has been inflected, for example. This field is in "curly" parentheses. May be null.
         */
        public final String wordInSentence;
        /**
         * a "~" character to indicate that the sentence pair is a good and checked example of the usage of the word. Words are marked to enable appropriate sentences to be selected by dictionary software. Typically only one instance per sense of a word will be marked.The WWWJDIC server displays these sentences below the display of therelated dictionary entry.
         */
        public final boolean isChecked;

        public String getInSentence() {
            return wordInSentence != null ? wordInSentence : dictionaryForm;
        }

        public String toKana() {
            String result = getInSentence();
            if (!containsKanji(result)) {
                return result;
            }
            if (hiraganaReading != null) {
                // warning: this is the reading of the dictionary form. We cannot just use it if the sentence uses inflected form.
                if (wordInSentence == null) {
                    return hiraganaReading;
                }
                if (wordInSentence.startsWith(dictionaryForm)) {
                    return hiraganaReading + wordInSentence.substring(dictionaryForm.length());
                }
            }
            // no luck. We have to search Edict for the dictionary form of the word
            if (wordInSentence != null) {
                result = edict.get(wordInSentence);
                if (result != null) {
                    return result;
                }
            }
            // ow, tough. We need to try to somehow match it with the deinflected form.
            String hiragana = containsKanji(dictionaryForm) ? edict.get(dictionaryForm) : dictionaryForm;
            if (hiragana == null) {
                throw new RuntimeException(dictionaryForm + " is not in EDICT. Nothing to do.");
            }
            if (wordInSentence == null) {
                // the word is present in the sentence in the dictionary form. Just return the hiragana translation.
                return hiragana;
            }
            final String kanjiSubstring = getShortestPrefixWithAllKanjis(dictionaryForm);
            if (!postprocess(wordInSentence).startsWith(kanjiSubstring)) {
                throw new RuntimeException("Cannot deinflect: " + wordInSentence + " does not start with " + kanjiSubstring);
            }
            final String suffix = dictionaryForm.substring(kanjiSubstring.length());
            if (!hiragana.endsWith(suffix)) {
                throw new RuntimeException("Something's weird: is really " + hiragana + " reading of " + dictionaryForm + "?");
            }
            final String kanaSubstring = hiragana.substring(0, hiragana.length() - suffix.length());
            return kanaSubstring + wordInSentence.substring(kanjiSubstring.length());
        }

        private String postprocess(String wordInSentence) {
            return wordInSentence.replaceAll("１か月", "一ヶ月").replaceAll("１カ所", "一か所").replaceAll("10|１０", "十").replaceAll("２４", "二十").replaceAll("１４", "十四").replaceAll("1|１", "一").replaceAll("2|２", "二").replaceAll("3|３", "三").replaceAll("4|４", "四").replace('5', '五');
        }

        private String getShortestPrefixWithAllKanjis(final String jp) {
            for (int i = jp.length() - 1; i >= 0; i--) {
                if (KanjiUtils.isKanji(jp.charAt(i)) || (jp.charAt(i) == '々')) {
                    return jp.substring(0, i + 1);
                }
            }
            throw new RuntimeException(jp + " does not contain any kanji (it appears as " + wordInSentence + " in sentence)");
        }

        public boolean isInflected() {
            return wordInSentence != null;
        }

        @Override
        public String toString() {
            return getInSentence();
        }
    }
}
