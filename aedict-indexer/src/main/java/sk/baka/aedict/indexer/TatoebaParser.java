package sk.baka.aedict.indexer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import sk.baka.aedict.indexer.Main.Config;
import sk.baka.aedict.indexer.TanakaParser.BLineParser;
import sk.baka.aedict.indexer.TanakaParser.Edict;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;

/**
 * Parses the Tatoeba dictionary files (see http://tatoeba.org/eng/download_tatoeba_example_sentences for details).
 * @author Martin Vysny
 */
public class TatoebaParser implements IDictParser {

    private final Edict edict;
    /**
     * Maps index of the Japanese sentence to a list of translations.
     */
    private final Map<Integer, Sentences> sentences = new HashMap<Integer, Sentences>();
    private final List<Sentence> nonJpSentences = new ArrayList<Sentence>();
    private final Config cfg;
    private static final String JAPAN_ISO_639_3_CODE = "jpn";

    private static String[] split(String line) {
        int tabcount = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '\t') {
                tabcount++;
            }
        }
        final String[] result = new String[tabcount + 1];
        int i = 0;
        for (StringTokenizer st = new StringTokenizer(line, "\t"); st.hasMoreElements();) {
            result[i++] = st.nextToken();
        }
        return result;
    }

    public void addLine(String line, IndexWriter writer) throws IOException {
        final String[] tokens = split(line);
        final int index = Integer.parseInt(tokens[0]);
        if (tokens[1].equals(JAPAN_ISO_639_3_CODE)) {
            sentences.put(index, new Sentences(tokens[2]));
        } else {
            nonJpSentences.add(new Sentence(index, tokens[1], tokens[2]));
        }
    }

    public void onFinish(IndexWriter writer) throws IOException {
        parseLinks();
        postprocessNonJpSentences();
        parseBLines();
        writeLucene(writer);
    }

    private void writeLucene(IndexWriter writer) throws IOException {
        for (final Entry<Integer, Sentences> e : sentences.entrySet()) {
            if (e.getValue().bLine == null) {
                throw new RuntimeException("Missing B-Line for sentence " + e.getKey());
            }
            final Document doc = new Document();
            doc.add(new Field("japanese", e.getValue().japanese, Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("translations", e.getValue().getSentences(), Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("jp-deinflected", e.getValue().bLine.dictionaryFormWordList, Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("kana", CompressionTools.compressString(e.getValue().bLine.kana), Field.Store.YES));
            writer.addDocument(doc);
        }
    }

    private void parseLinks() throws IOException {
        // maps source link ID to a list of links.
        final Map<Integer, Set<Integer>> links = new HashMap<Integer, Set<Integer>>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("jpn_indices.csv"), cfg.encoding));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final String[] tokens = split(line);
                final int index1 = Integer.parseInt(tokens[0]);
                final int index2 = Integer.parseInt(tokens[1]);
                if (index1 != index2) {
                    Set<Integer> list = links.get(index1);
                    if (list == null) {
                        list = new HashSet<Integer>();
                        links.put(index1, list);
                    }
                    list.add(index2);
                    list = links.get(index2);
                    if (list == null) {
                        list = new HashSet<Integer>();
                        links.put(index2, list);
                    }
                    list.add(index1);
                }
            }
        } finally {
            MiscUtils.closeQuietly(reader);
        }
        // the "links" map is essentially a graph. We need to shrink this graph a bit, for the map to contain only one line per connected graph.
        for (Iterator<Entry<Integer, Set<Integer>>> i = links.entrySet().iterator(); i.hasNext();) {
            final Entry<Integer, Set<Integer>> e = i.next();
            for (final Integer in : e.getValue()) {
                final Set<Integer> other = links.get(in);
                if (other != null) {
                    other.addAll(e.getValue());
                    i.remove();
                    break;
                }
            }
        }
    }

    private static class Sentence {

        public final int index;
        public final String lang;
        public final String sentence;

        public Sentence(int index, String lang, String sentence) {
            this.index = index;
            this.lang = lang;
            this.sentence = sentence;
        }
    }

    private static class Sentences {

        public Sentences(String japanese) {
            this.japanese = japanese;
        }
        /**
         * The japanese original sentence contents.
         */
        public final String japanese;
        public BLineParser bLine;
        /**
         * Maps ISO 639-3 language code to the sentence contents.
         */
        public Map<String, String> sentences = new HashMap<String, String>();

        public String getSentences() {
            final ListBuilder lb = new ListBuilder("\n");
            for (final Entry<String, String> e : sentences.entrySet()) {
                lb.add(e.getKey() + ": " + e.getValue());
            }
            return lb.toString();
        }
    }

    public TatoebaParser(Config cfg) throws IOException {
        edict = Edict.loadFromDefaultLocation(null);
        this.cfg = cfg;
    }

    private void parseBLines() throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("jpn_indices.csv"), cfg.encoding));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                final String[] tokens = split(line);
                final int jpSentenceId = Integer.parseInt(tokens[0]);
                final Sentences sentence = sentences.get(jpSentenceId);
                final BLineParser bLine = new BLineParser(edict, sentence.japanese, tokens[2]);
                if (sentence.bLine != null) {
                    throw new RuntimeException("Duplicite bLine for " + jpSentenceId);
                }
                sentence.bLine = bLine;
            }
        } finally {
            MiscUtils.closeQuietly(reader);
        }
    }
}
