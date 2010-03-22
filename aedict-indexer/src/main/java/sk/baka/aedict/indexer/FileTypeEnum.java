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
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Denotes a dictionary file type.
 * @author Martin Vysny
 */
public enum FileTypeEnum {

    /**
     * The EDICT file as downloaded from the Monash web site.
     */
    Edict {

        public String getTargetFileName() {
            return "edict-lucene.zip";
        }

        public String getDefaultDownloadUrl() {
            return "http://ftp.monash.edu.au/pub/nihongo/edict.gz";
        }

        public IDictParser newParser() {
            return new IDictParser() {

                public boolean addLine(String line, Document doc) {
                    doc.add(new Field("contents", line, Field.Store.YES, Field.Index.ANALYZED));
                    return true;
                }

                public void onFinish() {
                    // do nothing
                }
            };
        }

        public String getAndroidSdcardRelativeLoc() {
            return "aedict/index-DICTIONARY_NAME/";
        }
    },
    Kanjidic {

        public String getTargetFileName() {
            return "kanjidic-lucene.zip";
        }

        public String getDefaultDownloadUrl() {
            return "http://ftp.monash.edu.au/pub/nihongo/kanjidic.gz";
        }

        public String getAndroidSdcardRelativeLoc() {
            return "aedict/index-kanjidic/";
        }

        public IDictParser newParser() {
            return new IDictParser() {

                private final char[] commonality = new char[1000];

                public boolean addLine(String line, Document doc) {
                    doc.add(new Field("contents", line, Field.Store.COMPRESS, Field.Index.NO));
                    // the kanji itself
                    doc.add(new Field("kanji", getKanji(line), Field.Store.YES, Field.Index.NOT_ANALYZED));
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
                            commonality[rank - 1] = getKanji(line).charAt(0);
                        }
                    }
                    return true;
                }

                public void onFinish() throws IOException {
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
    },
    Tanaka {

        public IDictParser newParser() {
            return new IDictParser() {

                public boolean addLine(String line, Document doc) {
                    if (line.startsWith("A: ")) {
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
                    // gather all inflected japanese words. Search for tokens in a form of xxx{yyy} and store xxx only.
                    final ArrayList<Object> words = Collections.list(new StringTokenizer(line.substring(3)));
                    final StringBuilder wordList = new StringBuilder();
                    for (final Object w : words) {
                        final String word = (String) w;
                        int curlyBraceIndex = word.indexOf('{');
                        if (curlyBraceIndex < 0) {
                            continue;
                        }
                        // the word is inflected. Retrieve the xxx part and add it
                        final String base = new StringTokenizer(word, "{}[]()~").nextToken();
                        wordList.append(base).append(' ');
                    }
                    doc.add(new Field("jp-deinflected", wordList.toString(), Field.Store.NO, Field.Index.ANALYZED));
                    return true;
                }

                public void onFinish() {
                    // do nothing
                }
            };
        }

        public String getTargetFileName() {
            return "tanaka-lucene.zip";
        }

        public String getDefaultDownloadUrl() {
            return "http://www.csse.monash.edu.au/~jwb/examples.gz";
        }

        public String getAndroidSdcardRelativeLoc() {
            return "aedict/index-tanaka/";
        }
    };

    /**
     * Produces new parser for this dictionary type.
     * @return a new instance of the parser, never null.
     */
    public abstract IDictParser newParser();

    /**
     * The file name of the target zip file, which contains the Lucene index.
     * @return the file name, without a path.
     */
    public abstract String getTargetFileName();

    /**
     * Returns a default download URL of the gzipped file.
     * @return the default URL.
     */
    public abstract String getDefaultDownloadUrl();

    public abstract String getAndroidSdcardRelativeLoc();
}
