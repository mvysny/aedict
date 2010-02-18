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

package sk.baka.aedict.kanji;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Utility methods for kanji characters
 * 
 * @author Martin Vysny
 */
public final class KanjiUtils {
	private KanjiUtils() {
		throw new AssertionError();
	}

	/**
	 * A very simple check for kanji. Works only on a mixture of kanji, katakana
	 * and hiragana.
	 * 
	 * @param c
	 *            the character to analyze.
	 * @return true if it is a kanji, false otherwise.
	 */
	public static boolean isKanji(char c) {
		return RomanizationEnum.Hepburn.toRomaji(c).charAt(0) == c;
	}

	/**
	 * A very simple check for hiragana characters.
	 * 
	 * @param c
	 *            the character to analyze.
	 * @return true if it is a hiragana character, false otherwise.
	 */
	public static boolean isHiragana(char c) {
		final String romaji = RomanizationEnum.Hepburn.toRomaji(c);
		if (romaji.charAt(0) == c) {
			// kanji
			return false;
		}
		final char c1 = RomanizationEnum.Hepburn.toHiragana(romaji).charAt(0);
		return c1 == c;
	}

	/**
	 * A very simple check for hiragana characters.
	 * 
	 * @param c
	 *            the character to analyze.
	 * @return true if it is a hiragana character, false otherwise.
	 */
	public static boolean isKatakana(char c) {
		final String romaji = RomanizationEnum.Hepburn.toRomaji(c);
		if (romaji.charAt(0) == c) {
			// kanji
			return false;
		}
		final char c1 = RomanizationEnum.Hepburn.toKatakana(romaji).charAt(0);
		return c1 == c;
	}

	private static final String HALFWIDTH_KATAKANA_TABLE = "。=｡;「=｢;」=｣;、=､;ー=ｰ;ッ=ｯ;ャ=ｬ;ュ=ｭ;ョ=ｮ;ァ=ｧ;ィ=ｨ;ゥ=ｩ;ェ=ｪ;ォ=ｫ;ア=ｱ;イ=ｲ;ウ=ｳ;エ=ｴ;オ=ｵ;カ=ｶ;キ=ｷ;ク=ｸ;ケ=ｹ;コ=ｺ;サ=ｻ;シ=ｼ;ス=ｽ;セ=ｾ;ソ=ｿ;タ=ﾀ;チ=ﾁ;ツ=ﾂ;テ=ﾃ;ト=ﾄ;ナ=ﾅ;ニ=ﾆ;ヌ=ﾇ;ネ=ﾈ;ノ=ﾉ;ハ=ﾊ;ヒ=ﾋ;フ=ﾌ;ヘ=ﾍ;ホ=ﾎ;マ=ﾏ;ミ=ﾐ;ム=ﾑ;メ=ﾒ;モ=ﾓ;ヤ=ﾔ;ユ=ﾕ;ヨ=ﾖ;ラ=ﾗ;リ=ﾘ;ル=ﾙ;レ=ﾚ;ロ=ﾛ;ワ=ﾜ;ヲ=ｦ;ン=ﾝ;ガ=ｶﾞ;ギ=ｷﾞ;グ=ｸﾞ;ゲ=ｹﾞ;ゴ=ｺﾞ;ザ=ｻﾞ;ジ=ｼﾞ;ズ=ｽﾞ;ゼ=ｾﾞ;ゾ=ｿﾞ;ダ=ﾀﾞ;ヂ=ﾁﾞ;ヅ=ﾂﾞ;デ=ﾃﾞ;ド=ﾄﾞ;バ=ﾊﾞ;ビ=ﾋﾞ;ブ=ﾌﾞ;ベ=ﾍﾞ;ボ=ﾎﾞ;パ=ﾊﾟ;ピ=ﾋﾟ;プ=ﾌﾟ;ペ=ﾍﾟ;ポ=ﾎﾟ";
	private static final Map<String, String> KATAKANA_TO_HALFWIDTH = new HashMap<String, String>();
	private static final Map<String, String> HALFWIDTH_TO_KATAKANA = new HashMap<String, String>();
	static {
		for (final Object entry : Collections.list(new StringTokenizer(HALFWIDTH_KATAKANA_TABLE, ";"))) {
			final String[] mapping = ((String) entry).split("\\=");
			final String kana = mapping[0];
			final String halfwidth = mapping[1];
			if (KATAKANA_TO_HALFWIDTH.put(kana, halfwidth) != null) {
				throw new IllegalArgumentException("Mapping for " + kana + " defined multiple times");
			}
			if (HALFWIDTH_TO_KATAKANA.put(halfwidth, kana) != null) {
				throw new IllegalArgumentException("Mapping for " + halfwidth + " defined multiple times");
			}
		}
	}

	/**
	 * Converts a string containing half-width katakana to full-width katakana.
	 * Non-half-width katakana characters are unchanged.
	 * 
	 * @param halfwidth
	 *            a string containing half-width characters, not null
	 * @return full-width katakana, never null
	 */
	public static String halfwidthToKatakana(final String halfwidth) {
		return translate(halfwidth, HALFWIDTH_TO_KATAKANA, 2);
	}

	/**
	 * Converts a string containing full-width katakana to half-width katakana.
	 * Non-full-width katakana characters are unchanged.
	 * 
	 * @param katakana
	 *            a string containing full-width characters, not null
	 * @return half-width katakana, never null
	 */
	public static String toHalfwidth(final String katakana) {
		return translate(katakana, KATAKANA_TO_HALFWIDTH, 1);
	}

	/**
	 * Checks if given character is a half-width katakana character.
	 * 
	 * @param ch
	 *            the character to check
	 * @return true if it is half-width katakana, false otherwise
	 */
	public static boolean isHalfwidth(final char ch) {
		return HALFWIDTH_TO_KATAKANA.containsKey(String.valueOf(ch));
	}

	private static String translate(final String in, final Map<? extends String, ? extends String> table, final int maxKeyLength) {
		final StringBuilder result = new StringBuilder(in.length());
		for (int i = 0; i < in.length(); i++) {
			String translated = null;
			int prefixLen;
			for (prefixLen = Math.min(maxKeyLength, in.length() - i); prefixLen >= 1; prefixLen--) {
				final String prefix = in.substring(i, i + prefixLen);
				translated = table.get(prefix);
				if (translated != null) {
					break;
				}
			}
			result.append(translated != null ? translated : in.substring(i, i + 1));
			if ((translated != null) && (prefixLen > 1)) {
				i += prefixLen - 1;
			}
		}
		return result.toString();
	}

	/**
	 * Checks whether given character is a kana character:
	 * {@link #isHiragana(char) hiragana},
	 * {@link #isKatakana(char) katakana} or {@link #isHalfwidth(char)
	 * half-width katakana} character.
	 * 
	 * @param ch
	 *            the character to check
	 * @return true if it is japanese character, false otherwise.
	 */
	public static boolean isKana(char ch) {
		return isKatakana(ch) || isHiragana(ch) || isHalfwidth(ch);
	}
}
