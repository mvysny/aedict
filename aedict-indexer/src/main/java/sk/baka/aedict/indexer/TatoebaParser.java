package sk.baka.aedict.indexer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
        boolean prevTokenWasSeparator = false;
        for (StringTokenizer st = new StringTokenizer(line, "\t", true); st.hasMoreElements();) {
            final String token = st.nextToken();
            if (token.equals("\t")) {
                if (prevTokenWasSeparator) {
                    result[i++] = "";
                }
                prevTokenWasSeparator = true;
            } else {
                result[i++] = token;
                prevTokenWasSeparator = false;
            }
        }
        if (prevTokenWasSeparator) {
            result[i++] = "";
        }
        if (i < result.length) {
            throw new RuntimeException("Line " + line + ": Expected " + result.length + " tokens, found only " + i);
        }
        return result;
    }

    public void addLine(String line, IndexWriter writer) throws IOException {
        final String[] tokens = split(line);
        final int index = Integer.parseInt(tokens[0]);
        if (MiscUtils.isBlank(tokens[1])) {
            System.out.println("Sentence #" + index + " language is missing - ignoring");
            return;
        }
        if (tokens[1].equals(JAPAN_ISO_639_3_CODE)) {
            sentences.put(index, new Sentences(tokens[2]));
        } else {
            nonJpSentences.add(new Sentence(index, tokens[1], tokens[2]));
        }
    }

    public void onFinish(IndexWriter writer) throws IOException {
        postprocessNonJpSentences(parseLinks());
        parseBLines();
        writeLucene(writer);
    }

    private void writeLucene(IndexWriter writer) throws IOException {
        int sc = 0;
        for (final Entry<Integer, Sentences> e : sentences.entrySet()) {
            if (e.getValue().bLine == null) {
                System.out.println("Missing B-Line for sentence " + e.getKey() + ", skipping");
            } else {
                final Document doc = new Document();
                doc.add(new Field("japanese", e.getValue().japanese, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("translations", e.getValue().getSentences(), Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("jp-deinflected", e.getValue().bLine.dictionaryFormWordList, Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("kana", CompressionTools.compressString(e.getValue().bLine.kana), Field.Store.YES));
                writer.addDocument(doc);
                sc++;
            }
        }
        System.out.println("Lucene indexed " + sc + " example sentences");
    }

    private Map<Integer, Integer> parseLinks() throws IOException {
        System.out.println("Parsing Sentence Links file");
        // maps source link ID to a list of links.
        final Map<Integer, Set<Integer>> links = new HashMap<Integer, Set<Integer>>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("links.csv"), cfg.encoding));
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                final String[] tokens = split(line);
                final int index1 = Integer.parseInt(tokens[0]);
                if (index1 < 0) {
                    throw new RuntimeException("Error parsing " + tokens[0]);
                }
                final int index2 = Integer.parseInt(tokens[1]);
                if (index2 < 0) {
                    throw new RuntimeException("Error parsing " + tokens[1]);
                }
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
        System.out.println("Constructing sentence sets, please wait");
        final List<Set<Integer>> graphs = new ArrayList<Set<Integer>>(200000);
        while (!links.isEmpty()) {
            final Set<Integer> graph = findCompleteGraph(links);
            if (graph.isEmpty()) {
                throw new RuntimeException("Empty graph");
            }
            graphs.add(graph);
            for (Integer i : graph) {
                links.remove(i);
            }
            if (graphs.size() % 1000 == 0) {
                System.out.println("Got " + graphs.size() + " sentences");
            }
        }
        System.out.println(graphs.size() + " example sentences found");
        int skipped = 0;
        // construct a map which maps non-JP-lang-sentence ID to a JP-lang-sentence_id
        final Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (Set<Integer> graph : graphs) {
            final Integer jpSentenceId = findJpSentenceId(graph);
            if (jpSentenceId == null) {
                System.out.println("No JP ID in graph " + graph);
                skipped++;
            } else {
                for (Integer i : graph) {
                    if (i.intValue() != jpSentenceId.intValue()) {
                        final Integer prev = result.put(i, jpSentenceId);
                        if (prev != null) {
                            throw new RuntimeException(i + "=>" + jpSentenceId + " defined multiple times, prev: " + i + "=>" + prev);
                        }
                    }
                }
            }
        }
        System.out.println("Link analysis complete; found " + skipped + " sentence graphs without Japanese examples");
        return result;
    }

    private Integer findJpSentenceId(Collection<Integer> ids) {
        for (Integer i : ids) {
            if (sentences.containsKey(i)) {
                return i;
            }
        }
        return null;
    }

    private Set<Integer> findCompleteGraph(Map<Integer, Set<Integer>> graph) {
        final Set<Integer> result = new HashSet<Integer>();
        final Integer index = graph.keySet().iterator().next();
        findCompleteGraph(graph, result, index);
        return result;
    }

    private void findCompleteGraph(Map<Integer, Set<Integer>> graph, Set<Integer> result, Integer index) {
        if (result.contains(index)) {
            return;
        }
        result.add(index);
        for (final Integer i : graph.get(index)) {
            findCompleteGraph(graph, result, i);
        }
    }

    private void postprocessNonJpSentences(Map<Integer, Integer> links) {
        System.out.println("Post-processing non-JP sentences");
        for (Sentence s : nonJpSentences) {
            final Integer jpId = links.get(s.index);
            if (jpId == null) {
                continue;
            }
            final Sentences se = sentences.get(jpId);
            if (se == null) {
                throw new RuntimeException("No Sentences object for #" + jpId);
            }
            se.sentences.put(s.lang, s.sentence);
        }
        nonJpSentences.clear();
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

        @Override
        public String toString() {
            return "Sentence{" + "#" + index + " " + lang + " " + sentence + '}';
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
        System.out.println("Parsing B-Lines");
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
