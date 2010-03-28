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

package sk.baka.aedict.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.autils.MiscUtils;
import android.graphics.drawable.BitmapDrawable;

/**
 * Loads the <a href="http://www.kanjicafe.com/license.htm">SOD</a> images from
 * a custom binary format. The format is gzipped, with the following structure:
 * <h4>The header:</h4> 4 byte int - number of kanji entries; for each entry:
 * 2-byte UTF-16 kanji character, 4-byte png offset in the file. The entries are
 * ordered by the "offset" value, ascending. <h4>The contents:</h4> Basically
 * just a streams of PNG images, one after another.
 * <p/>
 * Not thread safe.
 * 
 * @author Martin Vysny
 */
public class SodLoader {
	/**
	 * Creates new loader. The loader is initialized immediately, by reading the
	 * binary format header. The constructor fails if the file is not available.
	 * 
	 * @throws IOException
	 *             if the file fails to load.
	 */
	public SodLoader() throws IOException {
		// parse the header
		final DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(SDCARD_LOCATION)));
		try {
			final int numberOfKanjis = in.readInt();
			Character lastKanji = null;
			for (int i = 0; i < numberOfKanjis; i++) {
				final Character kanji = in.readChar();
				final int offset = in.readInt();
				startOffset.put(kanji, offset);
				if (lastKanji != null) {
					final int length = offset - startOffset.get(lastKanji);
					this.length.put(lastKanji, length);
				}
				lastKanji = kanji;
			}
			length.put(lastKanji, (int) (SDCARD_LOCATION.length() - startOffset.get(lastKanji)));
		} finally {
			MiscUtils.closeQuietly(in);
		}
	}

	/**
	 * Maps a character to the starting offset of the image in the file.
	 */
	private final Map<Character, Integer> startOffset = new HashMap<Character, Integer>();
	/**
	 * Maps a character to the length of the image data in the file.
	 */
	private final Map<Character, Integer> length = new HashMap<Character, Integer>();

	/**
	 * The file can be obtained from this URL.
	 */
	public static final URL DOWNLOAD_URL;
	static {
		try {
			DOWNLOAD_URL = new URL(DictTypeEnum.DICT_BASE_LOCATION_URL + "sod.dat.gz");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * The file name of the file.
	 */
	public static final File SDCARD_LOCATION = new File(DictTypeEnum.BASE_DIR + "/sod", "sod.dat");

	/**
	 * Loads a SOD image for given kanji character. Returns null if there is no
	 * image for given kanji. No caching is performed of the image.
	 * 
	 * @param kanji
	 *            the kanji
	 * @return a bitmap instance or null if there is no such image.
	 * @throws IOException
	 *             if the file is not available.
	 */
	private byte[] load(final Character kanji) throws IOException {
		final Integer offset = startOffset.get(kanji);
		if (offset == null) {
			return null;
		}
		final int length = this.length.get(kanji);
		final RandomAccessFile file = new RandomAccessFile(SDCARD_LOCATION, "r");
		try {
			file.seek(offset);
			final byte[] result = new byte[length];
			file.readFully(result);
			return result;
		} finally {
			MiscUtils.closeQuietly(file);
		}
	}

	/**
	 * The cached images.
	 */
	private final Map<Character, BitmapDrawable> cachedImages = new HashMap<Character, BitmapDrawable>();

	/**
	 * Loads a SOD image for given kanji character. Returns null if there is no
	 * image for given kanji. The image is cached in this instance of the
	 * loader.
	 * 
	 * @param kanji
	 *            the kanji
	 * @return a bitmap instance or null if there is no such image.
	 * @throws IOException
	 *             if the file is not available.
	 */
	public BitmapDrawable loadBitmap(final Character kanji) throws IOException {
		BitmapDrawable result = cachedImages.get(kanji);
		if (result == null) {
			final byte[] img = load(kanji);
			if (img != null) {
				result = new BitmapDrawable(new ByteArrayInputStream(img));
				cachedImages.put(kanji, result);
			}
		}
		return result;
	}
}
