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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.apache.lucene.index.IndexWriter;
import org.apache.commons.cli.Options;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import sk.baka.aedict.dict.LuceneSearch;

/**
 * Downloads the EDict file, indexes it with Lucene then zips it.
 * 
 * @author Martin Vysny
 */
public class Main {

    private static final String BASE_DIR = "target";
    static final String LUCENE_INDEX = BASE_DIR + "/index";

    /**
     * Performs EDICT download and indexing tasks.
     * @param args ignored, does not take any parameters.
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
    private final FileTypeEnum fileType;
    private final Charset encoding;

    private static Options getOptions() {
        final Options opts = new Options();
        Option opt = new Option("f", "file", true, "load edict file from a filesystem");
        opt.setArgName("file");
        opts.addOption(opt);
        opt = new Option("u", "url", true, "load edict file from a URL");
        opt.setArgName("url");
        opts.addOption(opt);
        opts.addOption("d", "default", false, "load default eng-jp edict file. Equal to -g -u " + FileTypeEnum.Edict.getDefaultDownloadUrl() + ". May be used with the -k switch to download default kanjidic or -t to download default tanaka corpus");
        opts.addOption("g", "gzipped", false, "the edict file is gzipped");
        opt = new Option("e", "encoding", true, "edict file encoding, defaults to EUC_JP");
        opt.setArgName("encoding");
        opts.addOption(opt);
        opts.addOption("?", null, false, "prints help");
        opts.addOption("k", "kanjidic", false, "the file to process is actually a kanjidic");
        opts.addOption("t", "tanaka", false, "the file to process is a Tanaka Corpus with example sentences");
        return opts;
    }

    Main(final String[] args) throws MalformedURLException, ParseException {
        final CommandLineParser parser = new GnuParser();
        final CommandLine cl = parser.parse(getOptions(), args);
        if (cl.hasOption('?')) {
            printHelp();
            System.exit(255);
        }
        if (cl.hasOption('k')) {
            fileType = FileTypeEnum.Kanjidic;
        } else if (cl.hasOption('t')) {
            fileType = FileTypeEnum.Tanaka;
        } else {
            fileType = FileTypeEnum.Edict;
        }
        if (cl.hasOption('u')) {
            source = cl.getOptionValue('u');
            urlSource = new URL(source);
            localSource = null;
        } else if (cl.hasOption('d')) {
            source = fileType.getDefaultDownloadUrl();
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
        sb.append(fileType);
        sb.append(" file from ");
        sb.append(urlSource != null ? "URL" : "file");
        sb.append(' ').append(source);
        System.out.println(sb.toString());
        indexWithLucene();
        zipLuceneIndex();
        final String aedictDir = fileType.getAndroidSdcardRelativeLoc();
        System.out.println("Finished - the index file '" + fileType.getTargetFileName() + "' was created.");
        System.out.println("To use the indexed file with Aedict, you'll have to:");
        System.out.println("1. Connect your phone as a mass storage device to your computer");
        System.out.println("2. Browse the SDCard contents and delete the aedict/ directory if it is present");
        System.out.println("3. Create the " + aedictDir + " directory");
        System.out.println("4. Unzip the " + fileType.getTargetFileName() + " file to the " + aedictDir + " directory");
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
            final Directory directory = FSDirectory.open(new File(LUCENE_INDEX));
            try {
                final IndexWriter luceneWriter = new IndexWriter(directory,
                        new StandardAnalyzer(LuceneSearch.LUCENE_VERSION), true,
                        IndexWriter.MaxFieldLength.UNLIMITED);
                try {
                    final IDictParser parser = fileType.newParser();
                    indexWithLucene(edict, luceneWriter, parser);
                    parser.onFinish();
                    System.out.println("Optimizing Lucene index");
                    luceneWriter.optimize();
                } finally {
                    luceneWriter.close();
                }
            } finally {
                closeQuietly(directory);
            }
        } finally {
            IOUtils.closeQuietly(edict);
        }
        System.out.println("Finished Lucene indexing");
    }
    private static final Logger log = Logger.getLogger(Main.class.getName());

    private static void closeQuietly(final Directory d) {
        try {
            d.close();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to close a Directory object", ex);
        }
    }

    private static void indexWithLucene(BufferedReader edict,
            IndexWriter luceneWriter, final IDictParser parser) throws IOException {
        Document doc = new Document();
        for (String line = edict.readLine(); line != null; line = edict.readLine()) {
            if (line.startsWith("#")) {
                // skip comments
                continue;
            }
            if (line.trim().length() == 0) {
                // skip blank lines
                continue;
            }
            if (parser.addLine(line, doc)) {
                luceneWriter.addDocument(doc);
                doc = new Document();
            }
        }
        luceneWriter.commit();
    }

    private void zipLuceneIndex() throws IOException {
        System.out.println("Zipping the index file");
        final File zip = new File(fileType.getTargetFileName());
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
