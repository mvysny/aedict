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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.KanjidicEntry;
import sk.baka.aedict.indexer.Main.Config;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.autils.ListBuilder;

/**
 * Denotes a dictionary file type.
 * @author Martin Vysny
 */
public enum FileTypeEnum {

    /**
     * The EDICT file as downloaded from the Monash web site.
     */
    Edict {

        public String getTargetFileName(String name) {
            String result = "edict-lucene";
            if (name != null) {
                result += "-"+name;
            }
            return result+".zip";
        }

        public String getDefaultDownloadUrl() {
            return "http://ftp.monash.edu.au/pub/nihongo/edict.gz";
        }

        public IDictParser newParser(Config cfg) {
            return new IDictParser() {

                public void addLine(String line, IndexWriter writer) throws IOException {
                    if (line.startsWith("　？？？")) {
                        return;
                    }
                    final Document doc = new Document();
                    try {
                        final EdictEntry entry = DictTypeEnum.parseEdictEntry(line);
                        doc.add(new Field("contents", line, Field.Store.YES, Field.Index.ANALYZED));
                        doc.add(new Field("common", entry.isCommon ? "t" : "f", Field.Store.NO, Field.Index.NOT_ANALYZED));
                        final ListBuilder jp = new ListBuilder(" ");
                        if (entry.kanji != null) {
                            jp.add("W" + entry.kanji + "W");
                        }
                        jp.add("W" + entry.reading + "W");
                        doc.add(new Field("jp", jp.toString(), Field.Store.NO, Field.Index.ANALYZED));
                        writer.addDocument(doc);
                    } catch (Exception ex) {
                        System.out.println("Failed to parse edict line " + line + ", skipping: " + ex);
                        ex.printStackTrace();
                    }
                }

                public void onFinish(final IndexWriter writer) {
                    // do nothing
                }
            };
        }

        public String getAndroidSdcardRelativeLoc(String custom) {
            String result = "aedict/index";
            if (custom != null) {
                result += "-" + custom;
            }
            return result + '/';
        }

        public String getDefaultEncoding() {
            return "EUC_JP";
        }

        public boolean isDefaultGzipped() {
            return true;
        }
    },
    Kanjidic {

        public String getTargetFileName(String name) {
            return "kanjidic-lucene.zip";
        }

        public String getDefaultDownloadUrl() {
            return "http://ftp.monash.edu.au/pub/nihongo/kanjidic.gz";
        }

        public String getAndroidSdcardRelativeLoc(String custom) {
            return "aedict/index-kanjidic/";
        }

        public IDictParser newParser(final Config cfg) {
            return new IDictParser() {

                private final char[] commonality = new char[1000];
                private int lowestKanjiCodePoint = Integer.MAX_VALUE;
                private int highestKanjiCodePoint = 0;

                public void addLine(String line, IndexWriter writer) throws IOException {
                    final Document doc = new Document();
                    final String kanji = getKanji(line);
                    final int kanjiCodePoint = kanji.codePointAt(0);
                    lowestKanjiCodePoint = Math.min(kanjiCodePoint, lowestKanjiCodePoint);
                    highestKanjiCodePoint = Math.max(kanjiCodePoint, highestKanjiCodePoint);
                    // the kanji itself
                    doc.add(new Field("kanji", kanji, Field.Store.YES, Field.Index.NOT_ANALYZED));
                    // may contain several stroke numbers, separated by spaces. First one is the correct stroke number,
                    // following numbers are common mistakes.
                    doc.add(new Field("strokes", getFields(line, 'S', false), Field.Store.YES, Field.Index.ANALYZED));
                    // the radical number
                    doc.add(new Field("radical", getFields(line, 'B', true), Field.Store.YES, Field.Index.NOT_ANALYZED));
                    // the skip number in the form of x-x-x
                    doc.add(new Field("skip", getFields(line, 'P', true), Field.Store.YES, Field.Index.NOT_ANALYZED));
                    final String unparsedRank = getFields(line, 'F', true);
                    if (unparsedRank.trim().length() > 0) {
                        final int rank = Integer.valueOf(getFields(line, 'F', true));
                        if (rank <= commonality.length) {
                            commonality[rank - 1] = kanji.charAt(0);
                        }
                    }
                    final ListBuilder reading = new ListBuilder(", ");
                    final ListBuilder namesReading = new ListBuilder(", ");
                    boolean readingInNames = false;
                    for (final String field : line.substring(2).split("\\s+")) {
                        final char firstChar = KanjidicEntry.removeSplits(field).charAt(0);
                        if (firstChar == '{') {
                            break;
                        } else if (firstChar == 'G') {
                            final int grade = Integer.parseInt(field.substring(1));
                            doc.add(new Field("grade", String.valueOf(grade), Field.Store.YES, Field.Index.NO));
                        } else if (KanjiUtils.isHiragana(firstChar) || KanjiUtils.isKatakana(firstChar)) {
                            // a reading
                            (readingInNames ? namesReading : reading).add(field);
                        } else if (field.equals("T1")) {
                            readingInNames = true;
                        }
                    }
                    // second pass: English translations
                    final ListBuilder english = new ListBuilder(", ");
                    List<Object> tokens = Collections.list(new StringTokenizer(line, "{}"));
                    // skip the kanji definition tokens
                    tokens = tokens.subList(1, tokens.size());
                    for (final Object eng : tokens) {
                        final String engStr = eng.toString().trim();
                        if (engStr.length() == 0) {
                            // skip spaces between } {
                            continue;
                        }
                        english.add(engStr);
                    }
                    if (!namesReading.isEmpty()) {
                        reading.add("[" + namesReading + "]");
                    }
                    doc.add(new Field("english", CompressionTools.compressString(english.toString()), Field.Store.YES));
                    doc.add(new Field("reading", CompressionTools.compressString(reading.toString()), Field.Store.YES));
                    doc.add(new Field("namereading", CompressionTools.compressString(namesReading.toString()), Field.Store.YES));
                    writer.addDocument(doc);
                }

                public void onFinish(final IndexWriter writer) throws IOException {
                    // check if there are no missing characters
                    for (int i = 0; i < commonality.length; i++) {
                        if (commonality[i] == 0) {
                            throw new RuntimeException("No kanji for commonality " + (i + 1));
                        }
                    }
                    final String commonalityOrder = new String(commonality);
                    final OutputStream out = new FileOutputStream("target/commonality.txt");
                    try {
                        IOUtils.write(commonalityOrder, out, "UTF-8");
                    } finally {
                        IOUtils.closeQuietly(out);
                    }
                    System.out.println("Kanji Unicode codepoints spans over an inclusive range of " + lowestKanjiCodePoint + ".." + highestKanjiCodePoint);
                }
            };
        }

        private String getFields(final String kanjidicLine, final char firstChar, final boolean firstOnly) {
            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (final String field : kanjidicLine.split("\\ ")) {
                if (field.length() <= 1) {
                    continue;
                }
                if (field.charAt(0) != firstChar) {
                    continue;
                }
                if (first) {
                    first = false;
                } else {
                    sb.append(' ');
                }
                sb.append(field.substring(1));
                if (firstOnly) {
                    break;
                }
            }
            return sb.toString();
        }

        private String getKanji(final String kanjidicLine) {
            if (kanjidicLine.charAt(1) != ' ') {
                throw new IllegalArgumentException("Line in incorrect format. A single kanji followed by a space is expected: " + kanjidicLine);
            }
            return kanjidicLine.substring(0, 1);
        }

        public String getDefaultEncoding() {
            return "EUC_JP";
        }

        public boolean isDefaultGzipped() {
            return true;
        }
    },
    Tanaka {

        public IDictParser newParser(Config cfg) {
            return new TanakaParser();
        }

        public String getTargetFileName(String name) {
            return "tanaka-lucene.zip";
        }

        public String getDefaultDownloadUrl() {
            return "http://www.csse.monash.edu.au/~jwb/examples.gz";
        }

        public String getAndroidSdcardRelativeLoc(String custom) {
            return "aedict/index-tanaka/";
        }

        public String getDefaultEncoding() {
            return "EUC_JP";
        }

        public boolean isDefaultGzipped() {
            return true;
        }
    },
    Tatoeba {

        public IDictParser newParser(Config cfg) {
            try {
                return new TatoebaParser(cfg);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public String getTargetFileName(String name) {
            return "tatoeba-lucene.zip";
        }

        public String getDefaultDownloadUrl() {
            return "http://tatoeba.org/files/downloads/sentences.csv";
        }

        public String getAndroidSdcardRelativeLoc(String custom) {
            return "aedict/index-tatoeba/";
        }

        public String getDefaultEncoding() {
            return "UTF-8";
        }

        public boolean isDefaultGzipped() {
            return false;
        }
    };

    /**
     * Produces new parser for this dictionary type.
     * @return a new instance of the parser, never null.
     */
    public abstract IDictParser newParser(Config cfg);

    /**
     * The file name of the target zip file, which contains the Lucene index.
     * @return the file name, without a path.
     */
    public abstract String getTargetFileName(String custom);

    /**
     * Returns a default download URL of the gzipped file.
     * @return the default URL.
     */
    public abstract String getDefaultDownloadUrl();

    /**
     * Indexer uses this method to display a hint to the user, where the dictionary is stored on the SD card.
     * @return a displayable hint.
     */
    public abstract String getAndroidSdcardRelativeLoc(String custom);

    public abstract String getDefaultEncoding();

    public abstract boolean isDefaultGzipped();
}
