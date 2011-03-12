/**
 *     Aedict - an EDICT browser for Android
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
package sk.baka.aedict.dict;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;

import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;

import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;

/**
 * Enumerates possible types of dictionaries.
 * 
 * @author Martin Vysny
 */
public enum DictTypeEnum {

    /**
     * The EDICT dictionary.
     */
    Edict {

        private String getJpSearchTerm(String term, MatcherEnum matcher) {
            switch(matcher){
                case EndsWith: return term+"W";
                case Exact: return "W"+term+"W";
                case StartsWith: return "W"+term;
                case Substring: return term;
            }
            throw new RuntimeException("Unknown matcher: "+matcher);
        }

        @Override
        public String[] getLuceneQuery(SearchQuery query) {
            final ListBuilder sb = new ListBuilder(" OR ");
            for (final String q : query.query) {
                if (query.isJapanese) {
                    if (q.contains(" AND ")) {
                        final ListBuilder lb = new ListBuilder(" AND ");
                        for (final String term : q.split(" AND ")) {
                            lb.add(getJpSearchTerm(term.trim(), query.matcher));
                        }
                        sb.add("(" + lb + ")");
                    } else {
                        sb.add("\"" + getJpSearchTerm(q.trim(), query.matcher) + "\"");
                    }
                } else {
                    if (q.contains(" AND ")) {
                        sb.add("(" + q.trim() + ")");
                    } else {
                        sb.add("\"" + q.trim() + "\"");
                    }
                }
            }
            // first the common words are returned, then return all the rest
            // fixes http://code.google.com/p/aedict/issues/detail?id=47
            return new String[]{"(" + sb + ") AND common:t", "(" + sb + ") AND common:f"};
        }

        @Override
        public String getDefaultDictionaryLoc() {
            return "index";
        }

        @Override
        public URL getDownloadSite() {
            try {
                return new URL(DICT_BASE_LOCATION_URL + "edict-lucene.zip");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public long luceneFileSize() {
            return 20L * 1024 * 1024;
        }

        @Override
        public DictEntry getEntry(Document doc) {
            return parseEdictEntry(doc.get("contents"));
        }

        @Override
        public boolean matches(final DictEntry entry, boolean isJapanese, String query, MatcherEnum matcher) {
            if (isJapanese) {
                return matcher.matches(query, entry.reading) || ((entry.kanji != null) && matcher.matches(query, entry.kanji));
            }
            if (matcher == MatcherEnum.Substring) {
                return matcher.matches(query, entry.english);
            }
            // check the special English Edict processing, see MatcherEnum.Exact
            // for details.
            int lastIndex = 0;
            final String _line = entry.english.toLowerCase();
            int indexOfQuery = _line.indexOf(query, lastIndex);
            while (indexOfQuery >= 0) {
                if (!isWordPart(skipWhitespaces(_line, indexOfQuery - 1, -1)) && !isWordPart(skipWhitespaces(_line, indexOfQuery + query.length(), 1))) {
                    return true;
                }
                lastIndex = indexOfQuery + 1;
                indexOfQuery = _line.indexOf(query, lastIndex);
            }
            return false;
        }

        private boolean isWordPart(final char c) {
            return c == '-' || c == '\'' || c == '.' || c == ',' || Character.isLetter(c);
        }

        private char skipWhitespaces(final String line, final int charIndex, final int direction) {
            for (int i = charIndex; i >= 0 && i < line.length(); i += direction) {
                final char c = line.charAt(i);
                if (!Character.isWhitespace(c)) {
                    return c;
                }
            }
            return 0;
        }
    },
    /**
     * The KanjiDic dictionary.
     */
    Kanjidic {

        @Override
        public String[] getLuceneQuery(SearchQuery q) {
            // q.query can be null in case we are performing e.g. a pure SKIP
            // lookup (see the SkipActivity for details)
            final ListBuilder qb = new ListBuilder(" AND ");
            if (q.query != null) {
                if (q.query.length != 1) {
                    throw new IllegalStateException("Kanjidic search requires a single kanji character search");
                }
                qb.add("kanji:\"" + q.query[0].trim() + "\"");
            }
            if (q.strokeCount != null) {
                final int plusMinus = q.strokesPlusMinus == null ? 0 : q.strokesPlusMinus;
                if ((plusMinus > 3) || (plusMinus < 0)) {
                    throw new IllegalStateException("Invalid value: " + q.strokesPlusMinus);
                }
                final String sc;
                if (plusMinus > 0) {
                    sc = "[" + (q.strokeCount - plusMinus) + " TO " + (q.strokeCount + plusMinus) +"]";
                } else {
                    sc = String.valueOf(q.strokeCount);
                }
                qb.add("(strokes:" + sc + ")");
            }
            if (q.skip != null) {
                qb.add("skip:" + q.skip);
            }
            if (q.radical != null) {
                qb.add("radical:" + q.radical);
            }
            return new String[]{qb.toString()};
        }

        @Override
        public String getDefaultDictionaryLoc() {
            return "index-kanjidic";
        }

        @Override
        public URL getDownloadSite() {
            try {
                return new URL(DICT_BASE_LOCATION_URL + "kanjidic-lucene.zip");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public long luceneFileSize() {
            return 1500 * 1024;
        }

        @Override
        public DictEntry getEntry(Document doc) {
            // the entry is described at
            // http://www.csse.monash.edu.au/~jwb/kanjidic.html
            String kanjidicEntry = doc.get("contents");
            if (kanjidicEntry == null) {
                // reading Lucene >=2.9.0 data file...
                try {
                    kanjidicEntry = CompressionTools.decompressString(doc.getBinaryValue("contents"));
                } catch (DataFormatException ex) {
                    throw new RuntimeException(ex);
                }
            }
            final char kanji = kanjidicEntry.charAt(0);
            if (kanjidicEntry.charAt(1) != ' ') {
                throw new IllegalArgumentException("Invalid kanjidic entry: " + kanjidicEntry);
            }
            final ListBuilder reading = new ListBuilder(", ");
            final ListBuilder namesReading = new ListBuilder(", ");
            boolean readingInNames = false;
            final int radicalNumber = Integer.parseInt(doc.get("radical"));
            // the strokes count is a space-separated list of strokes. First
            // number denotes a correct number of strokes, following numbers
            // denote a commonly mismatched number of strokes.
            final int strokeCount = Integer.parseInt(new StringTokenizer(doc.get("strokes")).nextToken());
            Integer grade = null;
            final String skip = doc.get("skip");
            // first pass: ignore English readings as they may contain spaces
            // and
            // this simple algorithm would match them as readings (as the token
            // does
            // not start with '{' )
            for (final String field : kanjidicEntry.substring(2).split("\\ ")) {
                final char firstChar = KanjidicEntry.removeSplits(field).charAt(0);
                if (firstChar == '{') {
                    break;
                } else if (firstChar == 'G') {
                    grade = Integer.parseInt(field.substring(1));
                } else if (KanjiUtils.isHiragana(firstChar) || KanjiUtils.isKatakana(firstChar)) {
                    // a reading
                    (readingInNames ? namesReading : reading).add(field);
                } else if (field.equals("T1")) {
                    readingInNames = true;
                }
            }
            // second pass: English translations
            final ListBuilder english = new ListBuilder(", ");
            List<Object> tokens = Collections.list(new StringTokenizer(kanjidicEntry, "{}"));
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
            return new KanjidicEntry(String.valueOf(kanji), reading.toString(), english.toString(), radicalNumber, strokeCount, skip, grade);
        }

        @Override
        public boolean matches(DictEntry entry, boolean isJapanese, String string, MatcherEnum matcher) {
            // just ignore the substring matching, it should be never used with
            // this
            // dictionary type
            if (isJapanese) {
                return entry.kanji.equals(string);
            }
            return entry.english.toLowerCase().contains(string);
        }
    },
    /**
     * The Tanaka Corpus containing example sentences.
     */
    Tanaka {

        @Override
        public String[] getLuceneQuery(SearchQuery query) {
            final ListBuilder result = new ListBuilder(" OR ");
            for (final String q : query.trim().query) {
		final String[] qs = q.split("\\s+AND\\s+");
                if (query.isJapanese) {
		    add(result, "japanese", qs);
		    add(result, "jp-deinflected", qs);
                } else {
		    add(result, "english", qs);
                }
            }
            return new String[]{result.toString()};
        }

	private void add(final ListBuilder bu, final String prefix, final String[] andTerms) {
	    final ListBuilder b = new ListBuilder(" AND ");
	    for(final String term: andTerms) {
		b.add(prefix+":\""+term.trim()+"\"");
	    }
	    bu.add("("+b.toString()+")");
	}

        @Override
        public String getDefaultDictionaryLoc() {
            return "index-tanaka";
        }

        @Override
        public URL getDownloadSite() {
            try {
                return new URL(DICT_BASE_LOCATION_URL + "tanaka-lucene.zip");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public long luceneFileSize() {
            return 50012213;
        }

        @Override
        public DictEntry getEntry(Document doc) {
            final String japanese = doc.get("japanese");
            final String english = doc.get("english");
            final byte[] b = doc.getBinaryValue("kana");
            try {
                final String reading = b == null ? null : CompressionTools.decompressString(b);
                return new TanakaDictEntry(japanese, reading, english, doc.get("jp-deinflected"));
            } catch (DataFormatException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean matches(final DictEntry entry, final boolean isJapanese, String query, MatcherEnum matcher) {
            final String line = isJapanese ? entry.getJapanese() : entry.english;
            return matcher.matches(query, line);
        }
    };
    /**
     * A base http:// location of the dictionary files.
     */
    public static final String DICT_BASE_LOCATION_URL = "http://baka.sk/aedict/";

    /**
     * Returns a Lucene query which matches given query as close as possible.
     *
     * @param query
     *            the query.
     * @return the Apache Lucene query, or a list of queries. Must not be null
     *         nor empty. If multiple queries are returned they have to be
     *         executed in given order.
     */
    public abstract String[] getLuceneQuery(final SearchQuery query);

    /**
     * The default dictionary location. A directory name without the
     * '/sdcard/aedict/' prefix.
     *
     * @return the default dictionary location.
     */
    public abstract String getDefaultDictionaryLoc();
    /**
     * The base temporary directory, located on the sdcard, where EDICT and
     * index files are stored.
     */
    public static final String BASE_DIR = "/sdcard/aedict";

    /**
     * The default dictionary location. A directory name including the
     * '/sdcard/aedict/' prefix.
     *
     * @return the default dictionary location.
     */
    public final String getDefaultDictionaryPath() {
        return BASE_DIR + "/" + getDefaultDictionaryLoc();
    }

    /**
     * Returns the address where the zipped Lucene index of the dictionary can
     * be downloaded.
     *
     * @return the URL pointing to a zip file.
     */
    public abstract URL getDownloadSite();

    /**
     * Returns the expected size of the Lucene (not zip!) files, zipped in
     * {@link #getDownloadSite()}.
     *
     * @return a size of the zip file in bytes
     */
    public abstract long luceneFileSize();

    /**
     * Returns a dictionary entry from a Lucene document, from a proper
     * dictionary file. May throw a RuntimeException on parse error.
     *
     * @param doc
     *            the lucene document, not null.
     * @return never null entry.
     */
    public abstract DictEntry getEntry(final Document doc);

    /**
     * Returns a dictionary entry from a Lucene document, from a proper
     * dictionary file.
     *
     * @param doc
     *            the lucene document, not null.
     * @return never null entry. May return an error entry on parse error.
     */
    public DictEntry tryGetEntry(final Document doc) {
        try {
            return getEntry(doc);
        } catch (Exception ex) {
            return DictEntry.newErrorMsg(ex);
        }
    }

    /**
     * Returns a entry parsed entry from given document. The entry must match
     * the query.
     *
     * @param doc
     *            the Lucene document.
     * @param query
     *            the query
     * @return an entry instance if matched, null if unmatched, error entry in
     *         case of a parsing error.
     */
    public DictEntry tryGetEntry(final Document doc, final SearchQuery query) {
        final DictEntry entry = tryGetEntry(doc);
        if (!entry.isValid() || MiscUtils.isBlank(query.query)) {
            return entry;
        }
	for (final String q : query.query) {
	    if (matchesHandlesAnd(entry, query.isJapanese, q, query.matcher)) {
		return entry;
	    }
	}
        return null;
    }

    protected final boolean matchesHandlesAnd(final DictEntry entry, final boolean isJapanese, final String query, final MatcherEnum matcher){
	    final String[] qt = query.split("\\s+AND\\s+");
	    for (final String term : qt) {
		if (!matches(entry, isJapanese, term.trim().toLowerCase(), matcher)) {
		    return false;
		}
	    }
	    return true;
    }

    /**
     * Checks if given dictionary entry (in a form of a Lucene document) matches
     * given query.
     *
     * @param entry
     *            the entry to match. Never null, always
     *            {@link DictEntry#isValid() valid}.
     * @param isJapanese
     *            if true we are matching japanese word, if false, a
     *            non-japanese (presumably english) word is being matched.
     * @param query
     *            the string to match, always lower-case
     * @param matcher
     *            the matcher type, never {@link MatcherEnum#Any}.
     * @return true if the query matches, false otherwise.
     */
    protected abstract boolean matches(final DictEntry entry, final boolean isJapanese, final String query, final MatcherEnum matcher);

    public static EdictEntry parseEdictEntry(final String edictEntry) {
        // the entry is in one of the two following formats:
        // KANJI [hiragana] / english meaning
        // katakana / english meaning
        final int firstSlash = edictEntry.indexOf('/');
        if (firstSlash < 0) {
            throw new IllegalArgumentException("Failed to parse " + edictEntry + ": missing slash");
        }
        String englishPart = edictEntry.substring(firstSlash + 1).trim();
        while (englishPart.endsWith("/")) {
            // drop trailing slashes
            englishPart = englishPart.substring(0, englishPart.length() - 1);
        }
        final String jpPart = edictEntry.substring(0, firstSlash).trim();
        final int openSquareBracket = jpPart.indexOf('[');
        final String kanji;
        final String reading;
        if (openSquareBracket < 0) {
            // just a katakana reading, no kanji
            kanji = null;
            reading = jpPart;
        } else {
            kanji = jpPart.substring(0, openSquareBracket).trim();
            final int closingSquareBracket = jpPart.indexOf(']');
            reading = jpPart.substring(openSquareBracket + 1, closingSquareBracket).trim();
        }
        return new EdictEntry(kanji, reading, englishPart);
    }
}
