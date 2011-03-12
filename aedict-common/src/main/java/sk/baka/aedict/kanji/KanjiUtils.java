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
     * @return true if it is japanese kana character (not {@link #isKanji(char)
     *         kanji}), false otherwise.
     */
    public static boolean isKana(char ch) {
        return isKatakana(ch) || isHiragana(ch) || isHalfwidth(ch);
    }

    /**
     * Checks whether given character is a kanji character.
     *
     * @param ch
     *            the character to check
     * @return true if it is japanese kanji character (but not {@link #isKana(char)}
     *         kana), false otherwise.
     */
    public static boolean isKanji(char ch) {
        return (ch >= 19968) && (ch <= 40864);
    }

    /**
     * Checks whether given character is a Japanese character ({@link #isKanji(char) kanji} or {@link #isKana(char) kana}.
     * @param ch
     *            the character to check
     * @return true if it is japanese kanji character, false otherwise.
     */
    public static boolean isJapaneseChar(final char ch) {
        return isKanji(ch)||isKana(ch);
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
    private static final Map<Integer, String> JLPT_TABLE = new HashMap<Integer, String>();

    static {
        JLPT_TABLE.put(2, "腕湾論恋齢零涙領療涼了粒留略率律裏卵乱絡頼翌欲踊溶容幼預与余郵優輸戻綿迷娘夢務眠枚埋磨防貿棒暴忙忘帽坊亡豊訪抱宝報暮募補捕保編片壁閉並仏沸払複腹幅復封舞武膚符浮普怖布富婦貧評匹備非被疲比批彼否晩販般犯版判抜髪肌爆薄泊背杯拝破脳能濃悩燃猫認任乳難軟鈍曇届突独銅導逃到筒盗塔凍党倒怒途渡塗殿展適滴泥程痛賃珍沈頂超張庁著駐宙畜築遅恥値段暖断団探担濯宅退袋替損尊存測則造贈蔵臓憎増像装総窓燥操掃捜層双祖善泉専占絶設接責績税製精政性姓勢制吹震針辛寝伸触職蒸畳状条情常城紹昇招承床将召除諸署緒処純準述術柔舟修収授若捨湿識示似資誌詞脂枝支志師刺伺賛雑冊咲財罪在際済歳採妻再座砂査混婚困込骨腰刻香降鉱郊講荒肯耕紅硬構更厚効誤御互雇枯故呼個限現減険軒賢肩権検券件劇迎警経敬恵傾群靴掘隅偶禁均勤胸狭況挟恐境叫供許巨居旧吸久逆詰喫疑技規祈机寄基危含簡看甘環汗換慣干巻刊乾割額革較確格拡皆灰快解介過菓河可価仮欧押応奥汚煙演延越液鋭永営宇因域違移異易偉依圧");
        JLPT_TABLE.put(3, "丁両丸予争交他付令仲伝位例係信倉倍候停健側億兆児全公共兵具典内冷刀列初利刷副功加助努労勇勝包化卒協単博印原参反取受史号司各向君告周命和唱商喜器囲固園坂型塩士変夫央失委季孫守完官定実客宮害宿察寺対局岩岸島州巣差希席帯帳平幸底府庫庭康式弓当形役径徒得必念息悲想愛感成戦戸才打投折拾指挙改放救敗散数整旗昔星昨昭景晴曲最望期未末札材束松板果柱栄根案梅械植極様標横橋機欠次歯歴残殺毒毛氏氷求決汽油治法波泣泳活流浅浴消深清温港湖湯満漁灯炭点無然焼照熱牧玉王球由申畑番登的皮皿直相省矢石礼祝神票祭福科秒種積章童競竹笑笛第筆等算管箱節米粉糸紀約級細組結給絵続緑線練置羊美羽老育胃脈腸臣航船良芸芽苦草荷落葉虫血街衣表要覚観角訓記詩課調談議谷豆象貝負貨貯費賞路身軍輪辞農辺返追速連遊達選郡部配酒里量鉄録鏡関陸陽隊階雪雲静面順願類飛養馬鳴麦黄鼻");
        JLPT_TABLE.put(4, "不世主乗事京仕代以低住体作使便借働元兄光写冬切別力勉動区医去台合同味品員問回図地堂場声売夏夕夜太好妹姉始字室家寒屋工市帰広度建引弟弱強待心思急悪意所持教文料方旅族早明映春昼暑暗曜有服朝村林森業楽歌止正歩死民池注洋洗海漢牛物特犬理産用田町画界病発県真着知短研私秋究答紙終習考者肉自色英茶菜薬親計試説貸質赤走起転軽近送通進運遠都重野銀門開院集青音頭題顔風飯館首験鳥黒");
        JLPT_TABLE.put(5, "一七万三上下中九二五人今休会何先入八六円出分前北十千午半南友口古右名四国土外多大天女子学安小少山川左年店後手新日時書月木本来東校母毎気水火父生男白百目社空立耳聞花行西見言話語読買足車週道金長間雨電食飲駅高魚");
    }
    private static final Map<Character, Integer> JLPT_LEVEL = new HashMap<Character, Integer>();

    static {
        for (int i = 2; i <= 5; i++) {
            final String jlpt = JLPT_TABLE.get(i);
            for (int j = 0; j < jlpt.length(); j++) {
                final Character kanji = jlpt.charAt(j);
                final Integer prev = JLPT_LEVEL.put(kanji, i);
                if (prev != null) {
                    throw new RuntimeException("Kanji " + kanji + " present in levels " + prev + " and " + i);
                }
            }
        }
    }

    /**
     * Returns JLPT level of given kanji.
     * @param kanji the kanji
     * @return JLPT level N2..5, null if the kanji is not present in any of the JLPT test. See http://www.jlptstudy.com/N5/index.html for details.
     */
    public static Integer getJlptLevel(final Character kanji) {
        return JLPT_LEVEL.get(kanji);
    }

    public static String getJlptKanjis(final int level) {
        if (level < 2 || level > 5) {
            throw new IllegalArgumentException("JLPT level must be 2..5: " + level);
        }
        return JLPT_TABLE.get(level);
    }
}
