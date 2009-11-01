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
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

/**
 * Downloads the EDict file, indexes it with Lucene then zips it.
 * 
 * @author Martin Vysny
 */
public class Main {

    private static final URL EDICT_GZ;

    static {
        try {
            EDICT_GZ = new URL("http://ftp.monash.edu.au/pub/nihongo/edict.gz");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    private static final String BASE_DIR = "target";
    private static final String LUCENE_INDEX = BASE_DIR + "/index";
    private static final String LUCENE_INDEX_ZIP = BASE_DIR
            + "/edict-lucene.zip";

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

    private Main(final String[] args) throws MalformedURLException {
        if ("-u".equals(args[0])) {
            source = args[1];
            urlSource = new URL(args[1]);
            localSource = null;
        } else if ("-e".equals(args[0])) {
            source = EDICT_GZ.toString();
            urlSource = EDICT_GZ;
            localSource = null;
        } else {
            urlSource = null;
            localSource = new File(args[0]);
            source = args[0];
        }
        isGzipped = source.endsWith(".gz");
    }

    private static void printHelp() {
        System.out.println("Aedict index file generator");
        System.out.println("Usage: ai [-u] edict-file");
        System.out.println("  or:  ai -e");
        System.out.println();
        System.out.println("Produces a Lucene-indexed file from given EDict-formatted dictionary file (a local filesystem reference or an URL if the -u switch is specified). The file may be gzipped - in such case the filename must end with .gz.");
        System.out.println("To download and index the default english-japan edict file just use the -e switch - the file is downloaded automatically.");
    }

    private void run() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("Indexing ");
        if (isGzipped) {
            sb.append("gzipped ");
        }
        sb.append("Edict file from ");
        sb.append(urlSource != null ? "URL" : "file");
        sb.append(' ').append(source);
        System.out.println(sb.toString());
        indexWithLucene();
        zipLuceneIndex();
        System.out.println("Finished - a " + LUCENE_INDEX_ZIP + " file was created");
        System.out.println("To use the indexed file with Aedict, you'll have to:");
        System.out.println("1. Connect your phone as a mass storage device to your computer");
        System.out.println("2. Browse the SDCard contents and delete the aedict/ directory if it is present");
        System.out.println("3. Create the aedict/index/ directory");
        System.out.println("4. Unzip the " + LUCENE_INDEX_ZIP + " file to the aedict/index/ directory");
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
                readEdict(), "EUC-JP"));
        try {
            final IndexWriter luceneWriter = new IndexWriter(LUCENE_INDEX,
                    new StandardAnalyzer(), true,
                    IndexWriter.MaxFieldLength.UNLIMITED);
            try {
                indexWithLucene(edict, luceneWriter);
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
            IndexWriter luceneWriter) throws IOException {
        for (String line = edict.readLine(); line != null; line = edict.readLine()) {
            final Document doc = new Document();
            doc.add(new Field("contents", line, Field.Store.YES,
                    Field.Index.ANALYZED));
            luceneWriter.addDocument(doc);
        }
        luceneWriter.commit();
    }

    private void zipLuceneIndex() throws IOException {
        System.out.println("Zipping the index file");
        final File zip = new File(LUCENE_INDEX_ZIP);
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
