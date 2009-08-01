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

package sk.baka.aedict;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Japan language related stuff.
 * 
 * @author Martin Vysny
 */
public final class JpUtils {
	private JpUtils() {
		throw new AssertionError();
	}

	private static final ConcurrentMap<String, String> katakanaToRomaji = new ConcurrentHashMap<String, String>();
	private static final ConcurrentMap<String, String> hiraganaToRomaji = new ConcurrentHashMap<String, String>();
	private static final ConcurrentMap<String, String> romajiToKatakana = new ConcurrentHashMap<String, String>();
	private static final ConcurrentMap<String, String> romajiToHiragana = new ConcurrentHashMap<String, String>();

	/**
	 * Initializes this class.
	 * 
	 * @param cl
	 *            class-loader to load property files from.
	 * @throws IOException
	 *             if shit happens
	 */
	public static synchronized void initialize(final ClassLoader cl)
			throws IOException {
		parse(MiscUtils.openResource("katakana.properties", cl),
				katakanaToRomaji, romajiToKatakana);
		parse(MiscUtils.openResource("hiragana.properties", cl),
				hiraganaToRomaji, romajiToHiragana);
	}

	private static void parse(InputStream kanaStream,
			ConcurrentMap<String, String> kanaToRomaji,
			ConcurrentMap<String, String> romajiToKana) throws IOException {
		final Properties mapping = MiscUtils.load(kanaStream);
		for (final Map.Entry<Object, Object> e : mapping.entrySet()) {
			final String kana = (String) e.getKey();
			final String romaji = (String) e.getValue();
			if (kanaToRomaji.put(kana, romaji) != null) {
				throw new IllegalArgumentException("Mapping for " + kana
						+ " defined multiple times");
			}
			if (romajiToKana.put(romaji, kana) != null) {
				throw new IllegalArgumentException("Mapping for " + romaji
						+ " defined multiple times");
			}
		}
	}

	/**
	 * Converts given romaji text to hiragana
	 * 
	 * @param romaji
	 *            romaji text
	 * @return text converted to hiragana, with unknown characters untranslated.
	 */
	public static String toHiragana(final String romaji) {
		return toKana(romajiToHiragana, romaji);
	}

	/**
	 * Converts given romaji text to katakana
	 * 
	 * @param romaji
	 *            romaji text
	 * @return text converted to katakana, with unknown characters untranslated.
	 */
	public static String toKatakana(final String romaji) {
		return toKana(romajiToKatakana, romaji);
	}

	private static String toKana(ConcurrentMap<String, String> romajiToKana,
			String romaji) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < romaji.length(); i++) {
			String kana = null;
			for (int matchLen = 1; matchLen <= 4; matchLen++) {
				final String romajiMatch = String.valueOf(romaji.substring(i,
						i + matchLen).toLowerCase());
				if (romajiMatch.equals("n")) {
					// skip "n" for now: prevents conversion of e.g. na to n'a
					continue;
				}
				kana = romajiToKana.get(romajiMatch);
				if (kana != null) {
					i += matchLen - 1;
					break;
				}
			}
			if (kana == null && romaji.substring(i).startsWith("nn")) {
				// check for 'nn'
				kana = romajiToKana.get("nn");
				i += 1;
			}
			if (kana == null) {
				kana = romaji.substring(i, i + 1);
			}
			sb.append(kana);
		}
		return sb.toString();

	}

	/**
	 * Converts a text in hiragana or katakana to romaji. Does not handle kanji.
	 * 
	 * @param hiraganaOrKatakana
	 *            text in hiragana or katakana.
	 * @return romaji text
	 */
	public static String toRomaji(final String hiraganaOrKatakana) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hiraganaOrKatakana.length(); i++) {
			final String kana = String.valueOf(hiraganaOrKatakana.charAt(i));
			String romaji = katakanaToRomaji.get(kana);
			if (romaji == null) {
				romaji = hiraganaToRomaji.get(kana);
			}
			if (romaji != null) {
				// fix xji and nn
				if (romaji.equals("nn")) {
					romaji = "n";
				} else if (romaji.startsWith("x")) {
					romaji = romaji.substring(1);
				}
			} else {
				romaji = kana;
			}
			sb.append(romaji);
		}
		return sb.toString();
	}
}
