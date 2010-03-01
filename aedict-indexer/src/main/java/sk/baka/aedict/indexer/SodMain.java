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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;

/**
 * Indexes the SOD images. Expects a directory with unpacked SOD archive contents - essentially a bunch of png files named
 * [KANJI].png. This directory is packed to a huge file, packed with gzip, with the following contents:
 * <h4>The header:</h4>
 * 4 byte int - number of kanji entries; for each entry: 2-byte UTF-16 kanji character, 4-byte png offset in the file.
 * The entries are ordered by the "offset" value, ascending.
 *<h4>The contents:</h4>
 * Basically just a streams of PNG images, one after another.
 * @author Martin Vysny
 */
public class SodMain {

    /**
     * Runs the indexer.
     * @param args requires a single parameter: the unpacked sod-utf8.tar.gz location.
     */
    public static void main(String[] args) throws Exception {
        new SodMain(args).run();
    }
    private final File sodUtf8Location;

    private SodMain(String[] args) throws IOException {
        sodUtf8Location = new File(args[0]);
        if (!sodUtf8Location.exists()) {
            throw new IOException(sodUtf8Location + " does not exist");
        }
    }
    private final Map<Character, File> pngLengths = new HashMap<Character, File>();

    private void run() throws IOException {
        computePngLengths();
        createPackedFile();
        System.out.println("Done");
    }

    private void computePngLengths() {
        // list all png files
        final File[] pngs = sodUtf8Location.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".png");
            }
        });
        for (final File png : pngs) {
            final String kanji = png.getName().substring(0, png.getName().length() - 4);
            if (kanji.length() != 1) {
                throw new RuntimeException("Found a png file " + png + " with an invalid name (the name must be [KANJI].png");
            }
            pngLengths.put(kanji.charAt(0), png);
        }
    }

    private void createPackedFile() throws IOException {
        System.out.println("Packaging " + pngLengths.size() + " pngs");
        final DataOutputStream out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream("target/sod.dat.gz")));
        try {
            // write index table
            int offset = pngLengths.size() * 6 + 4;
            out.writeInt(pngLengths.size());
            final List<Character> kanjis = new ArrayList<Character>(pngLengths.keySet());
            for (final Character kanji : kanjis) {
                out.writeChar(kanji);
                out.writeInt(offset);
                offset += (int) pngLengths.get(kanji).length();
            }
            // write png contents
            for (final Character kanji : kanjis) {
                final InputStream in = new FileInputStream(pngLengths.get(kanji));
                try {
                    IOUtils.copy(in, out);
                } finally {
                    IOUtils.closeQuietly(in);
                }
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
