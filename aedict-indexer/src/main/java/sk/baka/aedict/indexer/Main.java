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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.commons.cli.Options;

/**
 * Downloads the EDict file, indexes it with Lucene then zips it.
 * 
 * @author Martin Vysny
 */
public class Main {

    private static final String EDICT_GZ = "http://ftp.monash.edu.au/pub/nihongo/edict.gz";
    private static final String KANJIDIC_GZ = "http://ftp.monash.edu.au/pub/nihongo/kanjidic.gz";
    private static final String BASE_DIR = "target";
    static final String LUCENE_INDEX = BASE_DIR + "/index";
    static final String LUCENE_INDEX_ZIP = "edict-lucene.zip";
    private static final String LUCENE_INDEX_ZIP_KANJIDIC = "kanjidic-lucene.zip";

    /**
     * Performs EDICT download and indexing tasks.
     * @param args ignored, does not take any parameters.
     * @throws Exception if error occurs
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                printHelp();
                System.exit(255);
            }
            new Main(args).run();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Indexing failed: " + ex.toString());
            System.exit(1);
        }
    }
    private final File localSource;
    private final URL urlSource;
    private final String source;
    private final boolean isGzipped;
    private final boolean isKanjidic;
    private final Charset encoding;
    private final String targetFileName;

    private static Options getOptions() {
        final Options opts = new Options();
        Option opt = new Option("f", "file", true, "load edict file from a filesystem");
        opt.setArgName("file");
        opts.addOption(opt);
        opt = new Option("u", "url", true, "load edict file from a URL");
        opt.setArgName("url");
        opts.addOption(opt);
        opts.addOption("d", "default", false, "load default eng-jp edict file. Equal to -g -u " + EDICT_GZ + ". May be used with the -k switch, to download default kanjidic");
        opts.addOption("g", "gzipped", false, "the edict file is gzipped");
        opt = new Option("e", "encoding", true, "edict file encoding, defaults to EUC_JP");
        opt.setArgName("encoding");
        opts.addOption(opt);
        opts.addOption("?", null, false, "prints help");
        opts.addOption("k", "kanjidic", false, "the file to process is actually a kanjidic");
        return opts;
    }

    Main(final String[] args) throws MalformedURLException, ParseException {
        final CommandLineParser parser = new GnuParser();
        final CommandLine cl = parser.parse(getOptions(), args);
        if (cl.hasOption('?')) {
            printHelp();
            System.exit(255);
        }
        isKanjidic = cl.hasOption('k');
        targetFileName = isKanjidic ? LUCENE_INDEX_ZIP_KANJIDIC : LUCENE_INDEX_ZIP;
        if (cl.hasOption('u')) {
            source = cl.getOptionValue('u');
            urlSource = new URL(source);
            localSource = null;
        } else if (cl.hasOption('d')) {
            source = isKanjidic ? KANJIDIC_GZ : EDICT_GZ;
            urlSource = new URL(source);
            localSource = null;
        } else if (cl.hasOption('f')) {
            source = cl.getOptionValue('f');
            urlSource = null;
            localSource = new File(source);
        } else {
            throw new ParseException("At least one of -u, -d or -f switch must be specified");
        }
        isGzipped = cl.hasOption('g') || cl.hasOption('d');
        final String charset = cl.getOptionValue('e', "EUC_JP");
        if (!Charset.isSupported(charset)) {
            throw new ParseException("Charset " + charset + " is not supported by JVM. Supported charsets: " + new ArrayList<String>(Charset.availableCharsets().keySet()));
        }
        encoding = Charset.forName(charset);
    }

    private static void printHelp() {
        final HelpFormatter f = new HelpFormatter();
        f.printHelp("ai", "Aedict index file generator\nProduces a Lucene-indexed file from given EDict- or kanjidic-formatted dictionary file. To download and index the default english-japan edict file just use the -d switch - the file is downloaded automatically.", getOptions(), null, true);
    }

    void run() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("Indexing ");
        if (isGzipped) {
            sb.append("gzipped ");
        }
        sb.append(isKanjidic ? "kanjidic" : "edict");
        sb.append(" file from ");
        sb.append(urlSource != null ? "URL" : "file");
        sb.append(' ').append(source);
        System.out.println(sb.toString());
        indexWithLucene();
        zipLuceneIndex();
        final String aedictDir = "aedict/index" + (isKanjidic ? "-kanjidic" : "DICTIONARY_NAME") + "/";
        System.out.println("Finished - the index file '" + targetFileName + "' was created.");
        System.out.println("To use the indexed file with Aedict, you'll have to:");
        System.out.println("1. Connect your phone as a mass storage device to your computer");
        System.out.println("2. Browse the SDCard contents and delete the aedict/ directory if it is present");
        System.out.println("3. Create the " + aedictDir + " directory");
        System.out.println("4. Unzip the " + targetFileName + " file to the " + aedictDir + " directory");
        System.out.println("See http://code.google.com/p/aedict/wiki/CustomEdictFile for details");
    }

    private InputStream readEdict() throws IOException {
        InputStream in;
        if (localSource != null) {
            in = new FileInputStream(localSource);
        } else {
            in = urlSource.openStream();
        }
        if (isGzipped) {
            in = new GZIPInputStream(in);
        }
        return in;
    }

    private void indexWithLucene() throws IOException {
        System.out.println("Deleting old Lucene index");
        FileUtils.deleteDirectory(new File(LUCENE_INDEX));
        System.out.println("Indexing with Lucene");
        final BufferedReader edict = new BufferedReader(new InputStreamReader(
                readEdict(), encoding));
        try {
            final IndexWriter luceneWriter = new IndexWriter(LUCENE_INDEX,
                    new StandardAnalyzer(), true,
                    IndexWriter.MaxFieldLength.UNLIMITED);
            try {
                indexWithLucene(edict, luceneWriter, isKanjidic);
                System.out.println("Optimizing Lucene index");
                luceneWriter.optimize();
            } finally {
                luceneWriter.close();
            }
        } finally {
            IOUtils.closeQuietly(edict);
        }
        System.out.println("Finished Lucene indexing");
    }

    private static void indexWithLucene(BufferedReader edict,
            IndexWriter luceneWriter, final boolean isKanjidic) throws IOException {
        for (String line = edict.readLine(); line != null; line = edict.readLine()) {
            if (line.startsWith("#")) {
                // skip comments
                continue;
            }
            final Document doc = new Document();
            if (isKanjidic) {
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
            } else {
                doc.add(new Field("contents", line, Field.Store.YES, Field.Index.ANALYZED));
            }
            luceneWriter.addDocument(doc);
        }
        luceneWriter.commit();
    }

    private static String getFields(final String kanjidicLine, final char firstChar, final boolean firstOnly) {
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

    private static String getKanji(final String kanjidicLine) {
        if (kanjidicLine.charAt(1) != ' ') {
            throw new IllegalArgumentException("Line in incorrect format. A single kanji followed by a space is expected: " + kanjidicLine);
        }
        return kanjidicLine.substring(0, 1);
    }

    private void zipLuceneIndex() throws IOException {
        System.out.println("Zipping the index file");
        final File zip = new File(targetFileName);
        if (zip.exists() && !zip.delete()) {
            throw new IOException("Cannot delete " + zip.getAbsolutePath());
        }
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                zip));
        try {
            out.setLevel(9);
            final File[] luceneIndexFiles = new File(LUCENE_INDEX).listFiles();
            for (final File indexFile : luceneIndexFiles) {
                final ZipEntry entry = new ZipEntry(indexFile.getName());
                entry.setSize(indexFile.length());
                out.putNextEntry(entry);
                final InputStream in = new FileInputStream(indexFile);
                try {
                    IOUtils.copy(in, out);
                } finally {
                    IOUtils.closeQuietly(in);
                }
                out.closeEntry();
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
        System.out.println("Finished index zipping");
    }
}
