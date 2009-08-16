/**
 *     Ambient - A music player for the Android platform
 Copyright (C) 2007 Martin Vysny
 
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
import java.io.OutputStream;
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
	private static final String EDICT_CACHE = BASE_DIR + "/edict";
	private static final String LUCENE_INDEX = BASE_DIR + "/index";
	private static final String LUCENE_INDEX_ZIP = BASE_DIR
			+ "/edict-lucene.zip";

	public static void main(String[] args) throws Exception {
		downloadEdict();
		indexWithLucene();
		zipLuceneIndex();
		System.out.println("Finished.");
	}

	private static void downloadEdict() throws IOException {
		final File target = new File(EDICT_CACHE);
		if (target.exists()) {
			System.out.println(target.getAbsolutePath()
					+ " exists, skipping download");
			return;
		}
		System.out.println("Downloading EDict to " + target.getAbsolutePath());
		final InputStream in = new GZIPInputStream(EDICT_GZ.openStream());
		try {
			final OutputStream out = new FileOutputStream(target);
			try {
				IOUtils.copy(in, out);
			} finally {
				IOUtils.closeQuietly(out);
			}
		} finally {
			IOUtils.closeQuietly(in);
		}
		System.out.println("Finished downloading EDict");
	}

	private static void indexWithLucene() throws IOException {
		System.out.println("Deleting old Lucene index");
		FileUtils.deleteDirectory(new File(LUCENE_INDEX));
		System.out.println("Indexing with Lucene");
		final BufferedReader edict = new BufferedReader(new InputStreamReader(
				new FileInputStream(EDICT_CACHE), "EUC-JP"));
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
		for (String line = edict.readLine(); line != null; line = edict
				.readLine()) {
			final Document doc = new Document();
			doc.add(new Field("contents", line, Field.Store.YES,
					Field.Index.ANALYZED));
			luceneWriter.addDocument(doc);
		}
		luceneWriter.commit();
	}

	private static void zipLuceneIndex() throws IOException {
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
