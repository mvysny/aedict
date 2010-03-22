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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

	private static final Set<Character> HIRAGANA_SPECIALS = new HashSet<Character>(Arrays.asList('っ', 'ゃ', 'ゅ', 'ょ'));

	/**
	 * A very simple check for hiragana characters.
	 * 
	 * @param c
	 *            the character to analyze.
	 * @return true if it is a hiragana character, false otherwise.
	 */
	public static boolean isHiragana(char c) {
		// first check for special characters, like small ya, yu, yo. These
		// characters are generally untranslateable with the toRomaji founction
		// and it will fail.
		if (HIRAGANA_SPECIALS.contains(c)) {
			return true;
		}
		final String romaji = RomanizationEnum.Hepburn.toRomaji(c);
		if (romaji.length() == 0 || romaji.charAt(0) == c) {
			// kanji
			return false;
		}
		final char c1 = RomanizationEnum.Hepburn.toHiragana(romaji).charAt(0);
		return c1 == c;
	}

	private static final Set<Character> KATAKANA_SPECIALS = new HashSet<Character>(Arrays.asList('ー', 'ャ', 'ュ', 'ョ'));

	/**
	 * A very simple check for hiragana characters.
	 * 
	 * @param c
	 *            the character to analyze.
	 * @return true if it is a hiragana character, false otherwise.
	 */
	public static boolean isKatakana(char c) {
		// first check for special characters, like small ya, yu, yo. These
		// characters are generally untranslateable with the toRomaji founction
		// and it will fail.
		if (KATAKANA_SPECIALS.contains(c)) {
			return true;
		}
		final String romaji = RomanizationEnum.Hepburn.toRomaji(c);
		if (romaji.length() == 0 || romaji.charAt(0) == c) {
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
	 * {@link #isHiragana(char) hiragana}, {@link #isKatakana(char) katakana} or
	 * {@link #isHalfwidth(char) half-width katakana} character.
	 * 
	 * @param ch
	 *            the character to check
	 * @return true if it is japanese character, false otherwise.
	 */
	public static boolean isKana(char ch) {
		return isKatakana(ch) || isHiragana(ch) || isHalfwidth(ch);
	}

	/**
	 * A kanji list, ordered by its commonality (most common one to least common one). Only first 1000 most common kanji characters are stored here.
	 */
	private static final String COMMONALITY = "日一国会人年大十二本中長出三同時政事自行社見月分議後前民生連五発間対上部東者党地合市業内相方四定今回新場金員九入選立開手米力学問高代明実円関決子動京全目表戦経通外最言氏現理調体化田当八六約主題下首意法不来作性的要用制治度務強気小七成期公持野協取都和統以機平総加山思家話世受区領多県続進正安設保改数記院女初北午指権心界支第産結百派点教報済書府活原先共得解名交資予川向際査勝面委告軍文反元重近千考判認画海参売利組知案道信策集在件団別物側任引使求所次水半品昨論計死官増係感特情投示変打男基私各始島直両朝革価式確村提運終挙果西勢減台広容必応演電歳住争談能無再位置企真流格有疑口過局少放税検藤町常校料沢裁状工建語球営空職証土与急止送援供可役構木割聞身費付施切由説転食比難防補車優夫研収断井何南石足違消境神番規術護展態導鮮備宅害配副算視条幹独警宮究育席輸訪楽起万着乗店述残想線率病農州武声質念待試族象銀域助労例衛然早張映限親額監環験追審商葉義伝働形景落欧担好退準賞訴辺造英被株頭技低毎医復仕去姿味負閣韓渡失移差衆個門写評課末守若脳極種美岡影命含福蔵量望松非撃佐核観察整段横融型白深字答夜製票況音申様財港識注呼渉達良響阪帰針専推谷古候史天階程満敗管値歌買突兵接請器士光討路悪科攻崎督授催細効図週積丸他及湾録処省旧室憲太橋歩離岸客風紙激否周師摘材登系批郎母易健黒火戸速存花春飛殺央券赤号単盟座青破編捜竹除完降超責並療従右修捕隊危採織森競拡故館振給屋介読弁根色友苦就迎走販園具左異歴辞将秋因献厳馬愛幅休維富浜父遺彼般未塁貿講邦舞林装諸夏素亡劇河遣航抗冷模雄適婦鉄寄益込顔緊類児余禁印逆王返標換久短油妻暴輪占宣背昭廃植熱宿薬伊江清習険頼僚覚吉盛船倍均億途圧芸許皇臨踏駅署抜壊債便伸留罪停興爆陸玉源儀波創障継筋狙帯延羽努固闘精則葬乱避普散司康測豊洋静善逮婚厚喜齢囲卒迫略承浮惑崩順紀聴脱旅絶級幸岩練押軽倒了庁博城患締等救執層版老令角絡損房募曲撤裏払削密庭徒措仏績築貨志混載昇池陣我勤為血遅抑幕居染温雑招奈季困星傷永択秀著徴誌庫弾償刊像功拠香欠更秘拒刑坂刻底賛塚致抱繰服犯尾描布恐寺鈴盤息宇項喪伴遠養懸戻街巨震願絵希越契掲躍棄欲痛触邸依籍汚縮還枚属笑互複慮郵束仲栄札枠似夕恵板列露沖探逃借緩節需骨射傾届曜遊迷夢巻購揮君燃充雨閉緒跡包駐貢鹿弱却端賃折紹獲郡併草徹飲貴埼衝焦奪雇災浦暮替析預焼簡譲称肉納樹挑章臓律誘紛貸至宗促慎控";
	private static final Map<Character, Integer> COMMONALITY_MAP = new HashMap<Character, Integer>(COMMONALITY.length());
	static {
		int commonality = 2;
		for (final char ch : COMMONALITY.toCharArray()) {
			COMMONALITY_MAP.put(ch, commonality++);
		}
	}

	/**
	 * Returns commonality of given japanese character. Katakana and hiragana
	 * characters receive commonality of 1, kanji characters get commonality
	 * from the F field of the KANJIDIC + 1. Unknown kanji receive commonality
	 * of 1002.
	 * 
	 * @param ch
	 *            the character.
	 * @return the commonality.
	 */
	public static int getCommonality(final char ch) {
		if (isKana(ch)) {
			return 1;
		}
		final Integer commonality = COMMONALITY_MAP.get(ch);
		if (commonality != null) {
			return commonality;
		}
		return COMMONALITY_MAP.size() + 2;
	}

	/**
	 * Computes a commonality of a string. Only japanese characters are
	 * expected. A commonality of a string is defined as a sum of
	 * {@link #getCommonality(char) commonalities} of all its characters.
	 * 
	 * @param str
	 *            the string
	 * @return the commonality.
	 */
	public static int getCommonality(final String str) {
		int result = 0;
		for (final char ch : str.toCharArray()) {
			if (Character.isWhitespace(ch)) {
				continue;
			}
			result += getCommonality(ch);
		}
		return result;
	}
}
