/**
 *     Aedict - an EDICT browser for Android
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
		if (INITIALIZED) {
			return;
		}
		parse(MiscUtils.openResource("katakana.properties", cl),
				katakanaToRomaji, romajiToKatakana);
		parse(MiscUtils.openResource("hiragana.properties", cl),
				hiraganaToRomaji, romajiToHiragana);
		INITIALIZED = true;
	}

	private static boolean INITIALIZED = false;

	private static void parse(InputStream kanaStream,
			ConcurrentMap<String, String> kanaToRomaji,
			ConcurrentMap<String, String> romajiToKana) throws IOException {
		final Properties mapping = MiscUtils.load(kanaStream);
		for (final Map.Entry<Object, Object> e : mapping.entrySet()) {
			final String kana = (String) e.getKey();
			final String[] romajis = ((String) e.getValue()).split("\\,");
			if (kanaToRomaji.put(kana, romajis[0]) != null) {
				throw new IllegalArgumentException("Mapping for " + kana
						+ " defined multiple times");
			}
			for (final String romaji : romajis) {
				if (romajiToKana.put(romaji, kana) != null) {
					throw new IllegalArgumentException("Mapping for " + romaji
							+ " defined multiple times");
				}
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
		return toKana(romajiToHiragana, romaji, false);
	}

	/**
	 * Converts given romaji text to katakana
	 * 
	 * @param romaji
	 *            romaji text
	 * @return text converted to katakana, with unknown characters untranslated.
	 */
	public static String toKatakana(final String romaji) {
		return toKana(romajiToKatakana, romaji, true);
	}

	private static String toKana(ConcurrentMap<String, String> romajiToKana,
			String romaji, final boolean isKatakana) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < romaji.length(); i++) {
			// optimization - only convert ascii letters
			final char c = romaji.charAt(i);
			if (!isAsciiLetter(c)) {
				sb.append(c);
				continue;
			}
			String kana = null;
			for (int matchLen = Math.min(romaji.length() - i, 4); matchLen >= 1; matchLen--) {
				final String romajiMatch = romaji.substring(i, i + matchLen)
						.toLowerCase();
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
			if (kana == null && romaji.substring(i).startsWith("n")) {
				// a stand-alone n.
				kana = romajiToKana.get("n");
			}
			if (kana == null && i < romaji.length() - 1) {
				// check for double consonant: for example "tta" must be
				// transformed to った
				String romajiMatch = romaji.substring(i, i + 2);
				if (isDoubledConsonant(romajiMatch)) {
					kana = isKatakana ? "ッ" : "っ";
				}
			}
			if (kana == null) {
				// give up
				kana = String.valueOf(romaji.charAt(i));
			}
			if (isKatakana) {
				// check for double vowel: in katakana, aa must be replaced by
				// アー instead of アア
				if (isVowel(c) && i > 0 && romaji.charAt(i - 1) == c) {
					kana = "−";
				}
			}
			sb.append(kana);
		}
		return sb.toString();

	}

	private static boolean isAsciiLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	private static boolean isVowel(final char c) {
		return c == 'a' || c == 'u' || c == 'e' || c == 'i' || c == 'o'
				|| c == 'A' || c == 'U' || c == 'E' || c == 'I' || c == 'O';
	}

	private final static Set<String> DOUBLED_CONSONANTS = new HashSet<String>(
			Arrays.asList("rr", "tt", "pp", "ss", "dd", "gg", "hh", "jj", "kk",
					"zz", "cc", "bb", "mm"));

	private static boolean isDoubledConsonant(final String str) {
		if (str.length() != 2) {
			throw new AssertionError();
		}
		return DOUBLED_CONSONANTS.contains(str.toLowerCase());
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
		// last kana character was the small "tsu". this means that we have to
		// double next character.
		boolean wasXtsu = false;
		for (int i = 0; i < hiraganaOrKatakana.length(); i++) {
			// check two consecutive kana characters first - to support stuff
			// like "pyu" etc
			String romaji = null;
			String kana = null;
			if (i < hiraganaOrKatakana.length() - 1) {
				kana = String.valueOf(hiraganaOrKatakana.substring(i, i + 2));
				romaji = katakanaToRomaji.get(kana);
				if (romaji == null) {
					romaji = hiraganaToRomaji.get(kana);
				}
				if (romaji != null) {
					// success! skip next kana
					i++;
				}
			}
			if (romaji == null) {
				// nope. convert just a single kana character
				kana = String.valueOf(hiraganaOrKatakana.charAt(i));
				romaji = katakanaToRomaji.get(kana);
			}
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
			}
			// check for katakana "-"
			if (romaji == null && "−".equals(kana)) {
				// just repeat last letter if there is one
				if (sb.length() > 0) {
					romaji = String.valueOf(sb.charAt(sb.length() - 1));
				}
			}
			// check for small "tsu"
			if (romaji == null && "っ".equals(kana)) {
				wasXtsu = true;
				continue;
			}
			if (romaji == null) {
				romaji = kana;
			}
			if (wasXtsu) {
				sb.append(romaji.charAt(0));
				wasXtsu = false;
			}
			sb.append(romaji);
		}
		return sb.toString();
	}
}
