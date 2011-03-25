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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.commons.cli.Options;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.autils.MiscUtils;

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
    private static final String REMOTE_DIR = "/home/moto/public_html/aedict/dictionaries";

    private static void exec(SSHClient ssh, String cmd) throws ConnectionException, TransportException, IOException {
        final Session s = ssh.startSession();
        try {
            final Command c = s.exec(cmd);
            if (c.getExitErrorMessage() != null) {
                throw new RuntimeException("Command " + cmd + " failed to execute with status " + c.getExitStatus() + ": " + c.getExitErrorMessage() + ", " + c.getErrorAsString());
            }
        } finally {
            MiscUtils.closeQuietly(s);
        }
    }

    private void upload() throws Exception {
        System.out.println("Uploading");
        final SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        String password = config.password;
        if (password == null) {
            System.out.println("Enter password");
            final Scanner s = new Scanner(System.in);
            password = s.nextLine();
            if (MiscUtils.isBlank(password)) {
                throw new RuntimeException("Invalid password: blank");
            }
        }
        System.out.println("Connecting");
        ssh.connect("rt.sk");
        try {
            System.out.println("Authenticating");
            ssh.authPassword("moto", password);
            System.out.println("Uploading version");
            final String targetFName = REMOTE_DIR + "/" + config.getTargetFileName();
            exec(ssh, "echo `date +%Y%m%d` >" + REMOTE_DIR + "/" + config.getTargetFileName() + ".version");
            exec(ssh, "rm -f " + targetFName);
            System.out.println("Uploading");
            final SCPFileTransfer ft = ssh.newSCPFileTransfer();
            ft.upload(config.getTargetFileName(), targetFName);
        } finally {
            ssh.disconnect();
        }
    }

    public static class Config {

        public File localSource;
        public URL urlSource;
        public String source;
        public boolean isGzipped;
        public FileTypeEnum fileType;
        public Charset encoding;
        public boolean upload;
        public String password;
        public String name;
        public String getTargetFileName() {
            return fileType.getTargetFileName(name);
        }

        public InputStream newInputStream() throws IOException {
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

        public BufferedReader newReader() throws IOException {
            return new BufferedReader(new InputStreamReader(newInputStream(), encoding));
        }
    }
    public final Config config = new Config();

    private static Options getOptions() {
        final Options opts = new Options();
        Option opt = new Option("f", "file", true, "load dictionary file from a filesystem");
        opt.setArgName("file");
        opts.addOption(opt);
        opt = new Option("u", "url", true, "load dictionary file from a URL");
        opt.setArgName("url");
        opts.addOption(opt);
        opts.addOption("d", "default", false, "download the dictionary file from the official download URL. Equal to -g -u " + FileTypeEnum.Edict.getDefaultDownloadUrl() + ". May be used with the -k/-t/-T switches.");
        opts.addOption("g", "gzipped", false, "the dictionary file is gzipped");
        opt = new Option("e", "encoding", true, "dictionary file encoding, defaults to EUC_JP for Jim Breen's dictionaries, UTF-8 for Tatoeba");
        opt.setArgName("encoding");
        opts.addOption(opt);
        opts.addOption("?", null, false, "prints this help");
        opts.addOption("k", "kanjidic", false, "the file to process is actually a kanjidic");
        opts.addOption("t", "tanaka", false, "the file to process is a Tanaka Corpus with example sentences");
        opts.addOption("T", "tatoeba", false, "the file to process is a Tatoeba Project file with example sentences");
        opts.addOption(null, "upload", false, "Uploads the dictionary file to www.baka.sk");
        opts.addOption("p", "password", true, "Upload SSH password");
        opts.addOption("n", "name", true, "(Optional) A custom dictionary name");
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
            config.fileType = FileTypeEnum.Kanjidic;
        } else if (cl.hasOption('t')) {
            config.fileType = FileTypeEnum.Tanaka;
        } else if (cl.hasOption('T')) {
            config.fileType = FileTypeEnum.Tatoeba;
        } else {
            config.fileType = FileTypeEnum.Edict;
        }
        if (cl.hasOption('u')) {
            config.source = cl.getOptionValue('u');
            config.urlSource = new URL(config.source);
            config.localSource = null;
        } else if (cl.hasOption('d')) {
            config.source = config.fileType.getDefaultDownloadUrl();
            config.urlSource = new URL(config.source);
            config.localSource = null;
        } else if (cl.hasOption('f')) {
            config.source = cl.getOptionValue('f');
            config.urlSource = null;
            config.localSource = new File(config.source);
        } else {
            throw new ParseException("At least one of -u, -d or -f switch must be specified");
        }
        config.isGzipped = (cl.hasOption('g') || cl.hasOption('d')) && config.fileType.isDefaultGzipped();
        final String charset = cl.getOptionValue('e', config.fileType.getDefaultEncoding());
        if (!Charset.isSupported(charset)) {
            throw new ParseException("Charset " + charset + " is not supported by JVM. Supported charsets: " + new ArrayList<String>(Charset.availableCharsets().keySet()));
        }
        config.encoding = Charset.forName(charset);
        config.upload = cl.hasOption("upload");
        config.password = cl.getOptionValue('p');
        config.name = cl.getOptionValue('n');
    }

    private static void printHelp() {
        final HelpFormatter f = new HelpFormatter();
        f.printHelp("ai", "Aedict index file generator\nProduces a Lucene-indexed file from given dictionary file (expects Jim Breen's Edict by default). To download and index the default english-japan edict file just use the -d switch - the file is downloaded automatically.", getOptions(), null, true);
    }

    void run() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("Indexing ");
        if (config.isGzipped) {
            sb.append("gzipped ");
        }
        sb.append(config.fileType);
        sb.append(" file from ");
        sb.append(config.urlSource != null ? "URL" : "file");
        sb.append(' ').append(config.source);
        System.out.println(sb.toString());
        indexWithLucene();
        zipLuceneIndex();
        if (config.upload) {
            upload();
        }
        final String aedictDir = config.fileType.getAndroidSdcardRelativeLoc(config.name);
        System.out.println("Finished - the index file '" + config.getTargetFileName() + "' was created.");
        System.out.println("To use the indexed file with Aedict, you'll have to:");
        System.out.println("1. Connect your phone as a mass storage device to your computer");
        System.out.println("2. Browse the SDCard contents and delete the aedict/ directory if it is present");
        System.out.println("3. Create the " + aedictDir + " directory");
        System.out.println("4. Unzip the " + config.getTargetFileName() + " file to the " + aedictDir + " directory");
        System.out.println("See http://code.google.com/p/aedict/wiki/CustomEdictFile for details");
    }

    private void indexWithLucene() throws IOException {
        System.out.println("Deleting old Lucene index");
        FileUtils.deleteDirectory(new File(LUCENE_INDEX));
        System.out.println("Indexing with Lucene");
        final BufferedReader dictionary = config.newReader();
        try {
            final Directory directory = FSDirectory.open(new File(LUCENE_INDEX));
            try {
                final IndexWriter luceneWriter = new IndexWriter(directory,
                        new StandardAnalyzer(LuceneSearch.LUCENE_VERSION), true,
                        IndexWriter.MaxFieldLength.UNLIMITED);
                try {
                    final IDictParser parser = config.fileType.newParser(config);
                    indexWithLucene(dictionary, luceneWriter, parser);
                    System.out.println("Optimizing Lucene index");
                    luceneWriter.optimize();
                } finally {
                    luceneWriter.close();
                }
            } finally {
                closeQuietly(directory);
            }
        } finally {
            IOUtils.closeQuietly(dictionary);
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
        for (String line = edict.readLine(); line != null; line = edict.readLine()) {
            if (line.startsWith("#")) {
                // skip comments
                continue;
            }
            if (line.trim().length() == 0) {
                // skip blank lines
                continue;
            }
            parser.addLine(line, luceneWriter);
        }
        parser.onFinish(luceneWriter);
        luceneWriter.commit();
    }

    private void zipLuceneIndex() throws IOException {
        System.out.println("Zipping the index file");
        final File zip = new File(config.getTargetFileName());
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
