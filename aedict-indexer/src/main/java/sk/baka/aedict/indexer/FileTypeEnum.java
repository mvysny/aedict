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

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
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

        public void addLine(final String line, final Document doc) {
            doc.add(new Field("contents", line, Field.Store.YES, Field.Index.ANALYZED));
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

        public void addLine(final String line, final Document doc) {
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

        public void addLine(final String line, final Document doc) {
            if (!line.startsWith("A: ")) {
                // skip
                return;
            }
            final ArrayList<Object> parsed = Collections.list(new StringTokenizer(line.substring(3), "\t#"));
            final String japanese = (String) parsed.get(0);
            final String english = (String) parsed.get(1);
            doc.add(new Field("japanese", japanese, Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("english", english, Field.Store.YES, Field.Index.ANALYZED));
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
     * Adds given line from given file type to the document.
     * @param line the file line
     * @param doc the document
     */
    public abstract void addLine(final String line, final Document doc);

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
