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

package sk.baka.aedict;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Japan language related stuff.
 * 
 * @author Martin Vysny
 */
public enum RomanizationEnum {
	/**
	 * The Hepburn romanization.
	 */
	Hepburn {

		@Override
		public String getHiraganaTable() {
			return "あ=a;い=i;う=u;え=e;お=o;か=ka;き=ki;く=ku;け=ke;こ=ko;きゃ=kya;きゅ=kyu;きょ=kyo;さ=sa;し=shi;す=su;せ=se;そ=so;しゃ=sha;しゅ=shu;しょ=sho;た=ta;ち=chi;つ=tsu;て=te;と=to;ちゃ=cha;ちゅ=chu;ちょ=cho;な=na;に=ni;ぬ=nu;ね=ne;の=no;にゃ=nya;にゅ=nyu;にょ=nyo;は=ha;ひ=hi;ふ=fu;へ=he;ほ=ho;ひゃ=hya;ひゅ=hyu;ひょ=hyo;ま=ma;み=mi;む=mu;め=me;も=mo;みゃ=mya;みゅ=myu;みょ=myo;や=ya;ゆ=yu;よ=yo;ら=ra;り=ri;る=ru;れ=re;ろ=ro;りゃ=rya;りゅ=ryu;りょ=ryo;わ=wa;ゐ=wi;ゑ=we;を=wo;ん=n,nn;が=ga;ぎ=gi;ぐ=gu;げ=ge;ご=go;ぎゃ=gya;ぎゅ=gyu;ぎょ=gyo;ざ=za;じ=ji;ず=zu;ぜ=ze;ぞ=zo;じゃ=ja;じゅ=ju;じょ=jo;だ=da;ぢ=xji;づ=xzu;で=de;ど=do;ぢゃ=xja;ぢゅ=xju;ぢょ=xjo;ば=ba;び=bi;ぶ=bu;べ=be;ぼ=bo;びゃ=bya;びゅ=byu;びょ=byo;ぱ=pa;ぴ=pi;ぷ=pu;ぺ=pe;ぽ=po;ぴゃ=pya;ぴゅ=pyu;ぴょ=pyo;ゔ=vu;くゎ=kwa;ぐゎ=gwa";
		}

		@Override
		public String getKatakanaTable() {
			return "ア=a;イ=i;ウ=u;エ=e;オ=o;カ=ka;キ=ki;ク=ku;ケ=ke;コ=ko;キャ=kya;キュ=kyu;キョ=kyo;サ=sa;シ=shi;ス=su;セ=se;ソ=so;シャ=sha;シュ=shu;ショ=sho;タ=ta;チ=chi;ツ=tsu;テ=te;ト=to;チャ=cha;チュ=chu;チョ=cho;ナ=na;ニ=ni;ヌ=nu;ネ=ne;ノ=no;ニャ=nya;ニュ=nyu;ニョ=nyo;ハ=ha;ヒ=hi;フ=fu;ヘ=he;ホ=ho;ヒャ=hya;ヒュ=hyu;ヒョ=hyo;マ=ma;ミ=mi;ム=mu;メ=me;モ=mo;ミャ=mya;ミュ=myu;ミョ=myo;ヤ=ya;ユ=yu;ヨ=yo;ラ=ra;リ=ri;ル=ru;レ=re;ロ=ro;リャ=rya;リュ=ryu;リョ=ryo;ワ=wa;ヰ=wi;ヱ=we;ヲ=wo;ン=n,nn;ガ=ga;ギ=gi;グ=gu;ゲ=ge;ゴ=go;ギャ=gya;ギュ=gyu;ギョ=gyo;ザ=za;ジ=ji,dži;ズ=zu;ゼ=ze;ゾ=zo;ジャ=ja;ジュ=ju;ジョ=jo;ダ=da;ヂ=xji;ヅ=xzu;デ=de;ド=do;ヂャ=xja;ヂュ=xju;ヂョ=xjo;バ=ba;ビ=bi;ブ=bu;ベ=be;ボ=bo;ビャ=bya;ビュ=byu;ビョ=byo;パ=pa;ピ=pi;プ=pu;ペ=pe;ポ=po;ピャ=pya;ピュ=pyu;ピョ=pyo";
		}

	},
	/**
	 * The Nihon-Shiki romanization.
	 */
	NihonShiki {

		@Override
		public String getHiraganaTable() {
			return "あ=a;い=i;う=u;え=e;お=o;か=ka;き=ki;く=ku;け=ke;こ=ko;きゃ=kya;きゅ=kyu;きょ=kyo;さ=sa;し=si;す=su;せ=se;そ=so;しゃ=sya;しゅ=syu;しょ=syo;た=ta;ち=ti;つ=tu;て=te;と=to;ちゃ=tya;ちゅ=tyu;ちょ=tyo;な=na;に=ni;ぬ=nu;ね=ne;の=no;にゃ=nya;にゅ=nyu;にょ=nyo;は=ha;ひ=hi;ふ=hu;へ=he;ほ=ho;ひゃ=hya;ひゅ=hyu;ひょ=hyo;ま=ma;み=mi;む=mu;め=me;も=mo;みゃ=mya;みゅ=myu;みょ=myo;や=ya;ゆ=yu;よ=yo;ら=ra;り=ri;る=ru;れ=re;ろ=ro;りゃ=rya;りゅ=ryu;りょ=ryo;わ=wa;ゐ=wi;ゑ=we;を=wo;ん=n,nn;が=ga;ぎ=gi;ぐ=gu;げ=ge;ご=go;ぎゃ=gya;ぎゅ=gyu;ぎょ=gyo;ざ=za;じ=zi;ず=zu;ぜ=ze;ぞ=zo;じゃ=zya;じゅ=zyu;じょ=zyo;だ=da;ぢ=di;づ=du;で=de;ど=do;ぢゃ=dya;ぢゅ=dyu;ぢょ=dyo;ば=ba;び=bi;ぶ=bu;べ=be;ぼ=bo;びゃ=bya;びゅ=byu;びょ=byo;ぱ=pa;ぴ=pi;ぷ=pu;ぺ=pe;ぽ=po;ぴゃ=pya;ぴゅ=pyu;ぴょ=pyo;ゔ=vu;くゎ=kwa;ぐゎ=gwa";
		}

		@Override
		public String getKatakanaTable() {
			return "ア=a;イ=i;ウ=u;エ=e;オ=o;カ=ka;キ=ki;ク=ku;ケ=ke;コ=ko;キャ=kya;キュ=kyu;キョ=kyo;サ=sa;シ=si;ス=su;セ=se;ソ=so;シャ=sya;シュ=syu;ショ=syo;タ=ta;チ=ti;ツ=tu;テ=te;ト=to;チャ=tya;チュ=tyu;チョ=tyo;ナ=na;ニ=ni;ヌ=nu;ネ=ne;ノ=no;ニャ=nya;ニュ=nyu;ニョ=nyo;ハ=ha;ヒ=hi;フ=hu;ヘ=he;ホ=ho;ヒャ=hya;ヒュ=hyu;ヒョ=hyo;マ=ma;ミ=mi;ム=mu;メ=me;モ=mo;ミャ=mya;ミュ=myu;ミョ=myo;ヤ=ya;ユ=yu;ヨ=yo;ラ=ra;リ=ri;ル=ru;レ=re;ロ=ro;リャ=rya;リュ=ryu;リョ=ryo;ワ=wa;ヰ=wi;ヱ=we;ヲ=wo;ン=n,nn;ガ=ga;ギ=gi;グ=gu;ゲ=ge;ゴ=go;ギャ=gya;ギュ=gyu;ギョ=gyo;ザ=za;ジ=zi;ズ=zu;ゼ=ze;ゾ=zo;ジャ=zya;ジュ=zyu;ジョ=zyo;ダ=da;ヂ=di;ヅ=du;デ=de;ド=do;ヂャ=dya;ヂュ=dyu;ヂョ=dyo;バ=ba;ビ=bi;ブ=bu;ベ=be;ボ=bo;ビャ=bya;ビュ=byu;ビョ=byo;パ=pa;ピ=pi;プ=pu;ペ=pe;ポ=po;ピャ=pya;ピュ=pyu;ピョ=pyo";
		}

	};
	/**
	 * Returns a mapping of hiragana characters to the appropriate reading in
	 * latin. The format is as follows: KANA=reading;KANA2=reading;...
	 * 
	 * @return a hiragana table
	 */
	protected abstract String getHiraganaTable();

	/**
	 * Returns a mapping of katakana characters to the appropriate reading in
	 * latin. The format is as follows: KANA=reading;KANA2=reading;...
	 * 
	 * @return a hiragana table
	 */
	protected abstract String getKatakanaTable();

	private final ConcurrentMap<String, String> katakanaToRomaji = new ConcurrentHashMap<String, String>();
	private final ConcurrentMap<String, String> hiraganaToRomaji = new ConcurrentHashMap<String, String>();
	private final ConcurrentMap<String, String> romajiToKatakana = new ConcurrentHashMap<String, String>();
	private final ConcurrentMap<String, String> romajiToHiragana = new ConcurrentHashMap<String, String>();

	private RomanizationEnum() {
		parse(new StringTokenizer(getKatakanaTable(), ";"), katakanaToRomaji, romajiToKatakana);
		parse(new StringTokenizer(getHiraganaTable(), ";"), hiraganaToRomaji, romajiToHiragana);
	}

	private static void parse(final StringTokenizer kanaStream, ConcurrentMap<String, String> kanaToRomaji, ConcurrentMap<String, String> romajiToKana) {
		for (final Object entry : Collections.list(kanaStream)) {
			final String[] mapping = ((String) entry).split("\\=");
			final String kana = mapping[0];
			final String[] romajis = mapping[1].split("\\,");
			if (kanaToRomaji.put(kana, romajis[0]) != null) {
				throw new IllegalArgumentException("Mapping for " + kana + " defined multiple times");
			}
			for (final String romaji : romajis) {
				if (romajiToKana.put(romaji, kana) != null) {
					throw new IllegalArgumentException("Mapping for " + romaji + " defined multiple times");
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
	public String toHiragana(final String romaji) {
		return toKana(romajiToHiragana, romaji, false);
	}

	/**
	 * Converts given romaji text to katakana
	 * 
	 * @param romaji
	 *            romaji text
	 * @return text converted to katakana, with unknown characters untranslated.
	 */
	public String toKatakana(final String romaji) {
		return toKana(romajiToKatakana, romaji, true);
	}

	private static String toKana(ConcurrentMap<String, String> romajiToKana, String romaji, final boolean isKatakana) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < romaji.length(); i++) {
			// optimization - only convert ascii letters
			final char c = romaji.charAt(i);
			if (!MiscUtils.isAsciiLetter(c)) {
				sb.append(c);
				continue;
			}
			String kana = null;
			for (int matchLen = Math.min(romaji.length() - i, 4); matchLen >= 1; matchLen--) {
				final String romajiMatch = romaji.substring(i, i + matchLen).toLowerCase();
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
					kana = "ー";
				}
			}
			sb.append(kana);
		}
		return sb.toString();

	}

	private static boolean isVowel(final char c) {
		return c == 'a' || c == 'u' || c == 'e' || c == 'i' || c == 'o' || c == 'A' || c == 'U' || c == 'E' || c == 'I' || c == 'O';
	}

	private final static Set<String> DOUBLED_CONSONANTS = new HashSet<String>(Arrays.asList("rr", "tt", "pp", "ss", "dd", "gg", "hh", "jj", "kk", "zz", "cc", "bb", "mm"));

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
	public String toRomaji(final String hiraganaOrKatakana) {
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
			if (romaji == null && "ー".equals(kana)) {
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

	/**
	 * Returns a hint on how to write given katakana/hiragana character in
	 * romaji so that it may be properly translated back. For example querying
	 * for づ returns xzu.
	 * 
	 * @param kana
	 *            the kana character
	 * @return a writing or null if no such kana is known.
	 */
	public String getWriting(final String kana) {
		String result = katakanaToRomaji.get(kana);
		if (result == null) {
			result = hiraganaToRomaji.get(kana);
		}
		return result;
	}
}
