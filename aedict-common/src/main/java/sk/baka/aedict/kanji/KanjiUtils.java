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
import java.util.EnumMap;
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
    public static enum KanjiQuiz {
        JlptN1(true, false),
        JlptN2(true, false),
        JlptN3(true, false),
        JlptN4(true, false),
        JlptN5(true, false),
        MostFrequentKanjisInNewspaper(false, false),
        JoyoGrade1(false, true),
        JoyoGrade2(false, true),
        JoyoGrade3(false, true),
        JoyoGrade4(false, true),
        JoyoGrade5(false, true),
        JoyoGrade6(false, true),
        JoyoJuniorHighSchool(false, true);
        public final boolean isJlpt;
        public final boolean isJoyo;
        private KanjiQuiz(boolean isJlpt, boolean isJoyo) {
            this.isJlpt = isJlpt;
            this.isJoyo = isJoyo;
        }
    }
    public static final Map<KanjiQuiz, String> QUIZ_TABLE = new EnumMap<KanjiQuiz, String>(KanjiQuiz.class);

    static {
        QUIZ_TABLE.put(KanjiQuiz.JlptN1, "氏統保第結派案策基価提挙応企検藤沢裁証援施井護展態鮮視条幹独宮率衛張監環審義訴株姿閣衆評影松撃佐核整融製票渉響推請器士討攻崎督授催及憲離激摘系批郎健盟従修隊織拡故振弁就異献厳維浜遺塁邦素遣抗模雄益緊標宣昭廃伊江僚吉盛皇臨踏壊債興源儀創障継筋闘葬避司康善逮迫惑崩紀聴脱級博締救執房撤削密措志載陣我為抑幕染奈傷択秀徴弾償功拠秘拒刑塚致繰尾描鈴盤項喪伴養懸街契掲躍棄邸縮還属慮枠恵露沖緩節需射購揮充貢鹿却端賃獲郡併徹貴衝焦奪災浦析譲称納樹挑誘紛至宗促慎控智握宙俊銭渋銃操携診託撮誕侵括謝駆透津壁稲仮裂敏是排裕堅訳芝綱典賀扱顧弘看訟戒祉誉歓奏勧騒閥甲縄郷揺免既薦隣華範隠徳哲杉釈己妥威豪熊滞微隆症暫忠倉彦肝喚沿妙唱阿索誠襲懇俳柄驚麻李浩剤瀬趣陥斎貫仙慰序旬兼聖旨即柳舎偽較覇詳抵脅茂犠旗距雅飾網竜詩繁翼潟敵魅嫌斉敷擁圏酸罰滅礎腐脚潮梅尽僕桜滑孤炎賠句鋼頑鎖彩摩励縦輝蓄軸巡稼瞬砲噴誇祥牲秩帝宏唆阻泰賄撲堀菊絞縁唯膨矢耐塾漏慶猛芳懲剣彰棋丁恒揚冒之倫陳憶潜梨仁克岳概拘墓黙須偏雰遇諮狭卓亀糧簿炉牧殊殖艦輩穴奇慢鶴謀暖昌拍朗寛覆胞泣隔浄没暇肺貞靖鑑飼陰銘随烈尋稿丹啓也丘棟壌漫玄粘悟舗妊熟旭恩騰往豆遂狂岐陛緯培衰艇屈径淡抽披廷錦准暑磯奨浸剰胆繊駒虚霊帳悔諭惨虐翻墜沼据肥徐糖搭盾脈滝軌俵妨擦鯨荘諾雷漂懐勘栽拐駄添冠斜鏡聡浪亜覧詐壇勲魔酬紫曙紋卸奮欄逸涯拓眼獄尚彫穏顕巧矛垣欺釣萩粛栗愚嘉遭架鬼庶稚滋幻煮姫誓把践呈疎仰剛疾征砕謡嫁謙后嘆菌鎌巣頻琴班棚潔酷宰廊寂辰霞伏碁俗漠邪晶墨鎮洞履劣那殴娠奉憂朴亭淳怪鳩酔惜穫佳潤悼乏該赴桑桂髄虎盆晋穂壮堤飢傍疫累痴搬晃癒桐寸郭尿凶吐宴鷹賓虜陶鐘憾猪紘磁弥昆粗訂芽庄傘敦騎寧循忍怠如寮祐鵬鉛珠凝苗獣哀跳匠垂蛇澄縫僧眺亘呉凡憩媛溝恭刈睡錯伯笹穀陵霧魂弊妃舶餓窮掌麗綾臭悦刃縛暦宜盲粋辱毅轄猿弦稔窒炊洪摂飽冗桃狩朱渦紳枢碑鍛刀鼓裸猶塊旋弓幣膜扇腸槽慈楊伐駿漬糾亮墳坪紺娯椿舌羅峡俸厘峰圭醸蓮弔乙汁尼遍衡薫猟羊款閲偵喝敢胎酵憤豚遮扉硫赦窃泡瑞又慨紡恨肪扶戯伍忌濁奔斗蘭迅肖鉢朽殻享秦茅藩沙輔媒鶏禅嘱胴迭挿嵐椎絹陪剖譜郁悠淑帆暁傑楠笛玲奴錠拳翔遷拙侍尺峠篤肇渇叔雌亨堪叙酢吟逓嶺甚喬崇漆岬癖愉寅礁乃洲屯樺槙姻巌擬塀唇睦閑胡幽峻曹詠卑侮鋳抹尉槻隷禍蝶酪茎帥逝汽琢匿襟蛍蕉寡琉痢庸朋坑藍賊搾畔遼唄孔橘漱呂拷嬢苑巽杜渓翁廉謹瞳湧欣窯褒醜升殉煩巴禎劾堕租稜桟倭婿慕斐罷矯某囚魁虹鴻泌於赳漸蚊葵厄藻禄孟嫡尭嚇巳凸暢韻霜硝勅芹杏棺儒鳳馨慧愁楼彬匡眉欽薪褐賜嵯綜繕栓翠鮎榛凹艶惣蔦錬隼渚衷逐斥稀芙詔皐雛惟佑耀黛渥憧宵妄惇脩甫酌蚕嬉蒼暉頒只肢檀凱彗謄梓丑嗣叶汐絢朔伽畝抄爽黎惰蛮冴旺萌偲壱瑠允侯蒔鯉弧遥舜瑛附彪卯但綺芋茜凌皓洸毬婆緋鯛怜邑倣碧啄穣酉悌倹柚繭亦詢采紗賦眸玖弐錘諄倖痘笙侃裟洵爾耗昴銑莞伶碩宥滉晏伎朕迪綸且竣晨吏燦麿頌箇楓琳梧哉澪匁晟衿凪梢丙颯茄勺恕蕗瑚遵瞭燎虞柊侑謁斤嵩捺蓉茉袈燿誼冶栞墾勁菖旦椋叡紬胤凜亥爵脹麟莉汰瑶瑳耶椰絃丞璃奎塑昂柾熙菫諒鞠崚濫捷");
        QUIZ_TABLE.put(KanjiQuiz.JlptN2, "党協総区領県設改府査委軍団各島革村勢減再税営比防補境導副算輸述線農州武象域額欧担準賞辺造被技低復移個門課脳極含蔵量型況針専谷史階管兵接細効丸湾録省旧橋岸周材戸央券編捜竹超並療採森競介根販歴将幅般貿講林装諸劇河航鉄児禁印逆換久短油暴輪占植清倍均億圧芸署伸停爆陸玉波帯延羽固則乱普測豊厚齢囲卒略承順岩練軽了庁城患層版令角絡損募裏仏績築貨混昇池血温季星永著誌庫刊像香坂底布寺宇巨震希触依籍汚枚複郵仲栄札板骨傾届巻燃跡包駐弱紹雇替預焼簡章臓律贈照薄群秒奥詰双刺純翌快片敬悩泉皮漁荒貯硬埋柱祭袋筆訓浴童宝封胸砂塩賢腕兆床毛緑尊祝柔殿濃液衣肩零幼荷泊黄甘臣浅掃雲掘捨軟沈凍乳恋紅郊腰炭踊冊勇械菜珍卵湖喫干虫刷湯溶鉱涙匹孫鋭枝塗軒毒叫拝氷乾棒祈拾粉糸綿汗銅湿瓶咲召缶隻脂蒸肌耕鈍泥隅灯辛磨麦姓筒鼻粒詞胃畳机膚濯塔沸灰菓帽枯涼舟貝符憎皿肯燥畜挟曇滴伺");
        QUIZ_TABLE.put(KanjiQuiz.JlptN3, "政議民連対部合市内相定回選米実関決全表戦経最現調化当約首法性要制治務成期取都和機平加受続進数記初指権支産点報済活原共得解交資予向際勝面告反判認参利組信在件側任引求所次昨論官増係感情投示変打直両式確果容必演歳争談能位置流格疑過局放常状球職与供役構割費付由説難優夫収断石違消神番規術備宅害配警育席訪乗残想声念助労例然限追商葉伝働形景落好退頭負渡失差末守若種美命福望非観察段横深申様財港識呼達良候程満敗値突光路科積他処太客否師登易速存飛殺号単座破除完降責捕危給苦迎園具辞因馬愛富彼未舞亡冷適婦寄込顔類余王返妻背熱宿薬険頼覚船途許抜便留罪努精散静婚喜浮絶幸押倒等老曲払庭徒勤遅居雑招困欠更刻賛抱犯恐息遠戻願絵越欲痛笑互束似列探逃遊迷夢君閉緒折草暮酒悲晴掛到寝暗盗吸陽御歯忘雪吹娘誤洗慣礼窓昔貧怒泳祖杯疲皆鳴腹煙眠怖耳頂箱晩寒髪忙才靴恥偶偉猫幾");
        QUIZ_TABLE.put(KanjiQuiz.JlptN4, "会同事自社発者地業方新場員立開手力問代明動京目通言理体田主題意不作用度強公持野以思家世多正安院心界教文元重近考画海売知道集別物使品計死特私始朝運終台広住真有口少町料工建空急止送切転研足究楽起着店病質待試族銀早映親験英医仕去味写字答夜音注帰古歌買悪図週室歩風紙黒花春赤青館屋色走秋夏習駅洋旅服夕借曜飲肉貸堂鳥飯勉冬昼茶弟牛魚兄犬妹姉漢");
        QUIZ_TABLE.put(KanjiQuiz.JlptN5, "日一国人年大十二本中長出三時行見月後前生五間上東四今金九入学高円子外八六下来気小七山話女北午百書先名川千水半男西電校語土木聞食車何南万毎白天母火右読友左休父雨");
        QUIZ_TABLE.put(KanjiQuiz.MostFrequentKanjisInNewspaper, "安案以位委意移違医井域育一員引院基間関韓含企観株官幹感環監改海界開外害各格核確閣革学楽額割活運営影映英衛円援演応横欧岡音下化何価加可家果課過画会解回五午現言限個呼経計警撃決結月件建検権研県見験元原減形景近金銀九区空軍係型教況業局極強境挙京供共協去気規記起技疑義議宮急求球究期機自式識七失質実示視試資事字持時次治思指支施止死氏私四始姿子市三参山産算残仕使察策裁際在財作昨差査再最歳済行高合告国今佐校構港考公口向好工広後語護交水数常情条状職食信審心新深申真神親身進人場初所書助女勝商小少松消渉証象賞上乗準出術述衆集住十重首受収州終手種守取主若社者車写段男談知地置着中断宅沢達担団代台大第題退待態体対想早争相総送増蔵造側足族続村多打組訴席石切設説先千川戦線選鮮前然全税政整正生声製西成世制勢性南難二日入任認年念能脳農派配頭働動同導道得特独内土党島投東当答統藤度渡都張朝町調長直追通低定提的展店転点伝田電注毎末万補報放方法訪望防北本保文聞平米別変辺負武部副復福物分不付夫府八発判半反番比被費非備美必百票表評病品白売連労六論和話量領力例要来落利理率立流両料葉容様用与融予有由輸優約木目問門夜野役名命明面味民務無愛悪圧伊依囲易為異維遺印因飲喜器館丸岸岩顔願危還管簡緩刊巻完患換絵階街拡獲覚角壊右宇羽雨浦栄永益駅越園延遠汚央押王沖億屋温夏科歌河火花貨我介雇互古固庫戸故系継軽芸迎劇激欠血健券憲懸献遣険厳源契恵掲筋緊苦具繰君郡傾刑橋興響曲玉勤均禁恐巨拒拠許競居旧給季紀貴儀吉却客逆久休及救級帰寄希揮棄辞鹿執室誌似児寺紙至史士師志散賛司札殺雑細載材罪坂阪崎埼削左座債催妻採災講貢購降項香号刻黒骨込困婚根混更江皇航攻功効厚幸康抗控候光図推譲植織色触伸慎振森針震陣城順処緒署諸除傷償将承招昇昭焼焦省称章笑紹衝障縮春習週充従宿授樹需周宗就修秀秋弱射借値池致遅築竹仲択奪脱谷単探短端弾逮隊貸帯替層捜草葬装走像臓促則息束測速属卒存損他太素創喪析積籍績責赤跡接折節絶占宣専染船善措狙星清盛精請青静肉熱燃納波破馬廃敗背倍闘督読突届奈逃討踏倒等登途努徒庁徴挑聴超賃痛塚停底庭程締邸摘適徹撤鉄天駐著枚幕歩募暮母包崩抱豊邦亡房暴貿便弁捕返紛併兵並閉編舞風幅服複払仏浜婦富布普浮父抜伴板版犯般販盤否彼批秘避飛尾標描博迫爆買齢歴列練路露老郎録惑枠湾良林臨輪塁類令冷鈴養抑欲頼絡乱裏離陸律略留慮旅了僚療預曜洋誘遊郵雄夕余友油薬躍戻盟迷模満未密夢阿握旭扱暗威慰緯衣磯稲茨陰陥頑鑑肝艦貫滑乾寒勘勧喚寛干慣歓漢甘看戒拐械皆概較隔岳掛笠梶潟括快悔懐隠雲泳鋭液沿炎煙縁塩奥往黄憶恩仮暇稼荷華賀雅湖誇顧孤己鯨穴兼剣圏堅嫌犬肩賢軒玄径慶携敬句駆駒遇屈掘熊訓群兄啓狂狭胸脅郷驚錦虚距漁魚叫牛祈軌輝亀偽犠菊喫詰脚虐丘吸泣奇岐旗既棋耳軸詩諮飼歯孜旨枝祉糸姉惨酸暫刺刷撮擦索桜冊菜剤唆砂鎖彩才栽祭斎郊鉱鋼豪克腰懇浩甲硬稿紅絞綱荒孝宏弘恒拘御悟誤吹衰遂随浄飾殖侵寝浸臣診仁尋尽須剰壌純巡暑序徐唱奨床彰昌沼照症祥訟詳丈盾准旬祝塾熟俊瞬襲柔渋縦銃趣酒寿拾殊捨謝釈芝舎柱智蓄秩茶宙忠抽昼暖卓託諾丹淡炭胆誕滝袋泰滞耐駄奏掃操窓荘騒贈即孫尊妥阻双倉昔雪仙泉浅洗潜繊薦銭曽礎祖斉晴牲聖誠据杉瀬是軟乳妊粘之悩濃覇俳拝排杯肺輩培梅透騰堂童徳毒栃縄豆怒冬凍搭棟盗湯糖到兆帳懲潮頂鳥沈珍陳津墜鶴貞帝廷弟抵艇敵哲典殿塗丁虫貯慢墓簿宝砲胞芳妨忘忙棒冒膨謀僕撲牧没堀幌翻摩麻埋妹勉舗雰柄陛壁偏片封腹覆噴粉貧敏怖敷腐箱畑髪罰閥繁範飯晩悲披疲皮肥微匹彦菱筆俵氷漂秒拍泊薄賠渕煕盧零霊烈裂恋炉朗漏賄腕糧緑倫隣涙励礼踊陽浴翌翼雷卵李梨里隆竜誉幼揚揺擁溶裕諭唯勇柳訳靖黙也矢毛猛網娘銘鳴滅免綿茂漫魅脈妙眠亜哀芦綾闇偉胃郁逸姻閑巌眼缶葛轄樺蒲釜鎌鴨茅刈瓦冠堪憾敢款汗灰芥貝慨涯該垣柿殻穫郭喝渇怪塊渦詠疫悦閲榎堰宴猿鉛殴荻乙俺卸穏佳嘉嫁架菓霞芽餓枯胡虎鼓伍呉桂鶏傑潔拳絹謙顕幻弦憩菌吟倶愚偶隅釧靴窪栗桑勲薫圭挟鏡仰凝暁桐琴恭喬峡亨享凶畿飢騎鬼宜戯擬欺弓朽窮糾毅幾忌机湿漆疾詞雌侍慈滋磁獅紫脂皿傘薩錯笹堺咲沙詐挫宰砕衡酵剛麹穀酷獄恨昆痕紺魂晃洪溝紘耕肯后巧慌吾娯碁酢垂炊睡粋酔瑞髄崇畳蒸醸錠嘱辱尻唇娠晋秦紳辛刃甚腎迅諏曙庶叙匠召哨尚庄掌晶湘礁粧肖鐘冗循淳潤淑粛峻駿臭舟酬汁獣叔洲朱狩珠寂赦斜煮遮蛇邪尺篠柴恥痴稚畜筑窒拓濯濁辰棚樽誰嘆鍛壇鷹怠胎挿曹巣槽燥聡遭憎捉俗袖其蘇僧壮惜拙摂窃舌扇旋践遷禅疎粗隻誓枢菅雀澄寸姓征楠尼如尿忍寧猫乃把琶媒陶洞胴銅峠篤寅屯敦豚曇鈍那灘鍋奴刀唐塔嶋悼桃灯筒斗賭弔彫眺腸跳鎮椎漬辻椿坪釣亭偵呈堤訂逓鄭泥笛迭添吐猪槙膜俣又輔穂俸呆奉峰泡縫飽鵬乏傍剖坊帽紡肪墨朴睦奔凡盆磨魔遍塀幣弊癖赴阜伏淵沸墳憤奮賓頻瓶冨扶符膚譜縛麦函肇幡肌畠鉢伐鳩帆搬班藩磐妃扉碑樋琵鼻姫媛苗柏粕舶漠陪萩伯翔麗暦劣聯蓮廊浪脇鷲亘姜嶌崔陵厘累嶺玲謡淀羅裸嵐欄蘭覧履劉硫粒龍虜亮寮涼猟羊妖楊祐悠憂猶癒幽愉紋弥盲岬稔矛霧趙挨逢葵茜渥葦梓絢鮎或粟庵鞍杏夷尉惟椅畏壱溢芋允蔭舘玩癌翫贋雁竿諌褐叶兜噛萱粥苅棺魁蟹凱劾蓋馨嚇樫橿韻烏卯鵜丑碓臼嘘唄瓜噂荏餌瑛奄怨燕艶苑於凹旺翁鴎桶牡伽寡珂禍迦蚊峨牙狐袴股姑弧渓稽茎蛍頚倹喧硯鍵慧芹襟謹矩寓串櫛隈矯饗尭欣欽侠僑匡卿汽稀祇橘杵仇嬉蒔汐宍雫叱嫉賜肢嗣皐鮫桟蚕讃斬伺鮭冴榊鷺搾朔嵯坐犀砦鴻壕拷轟鵠惚狛此頃杭喉坑孔巷乞鯉侯厨逗帥翠杖穣埴拭榛芯薪壬嬢渚升娼宵抄樟硝蒋蕉詔醤鍾殉舜蹴醜儒呪囚愁腫酌錫偲縞弛逐嫡檀啄琢凧只叩但巽竪狸湛蛋黛鯛醍戴舵楕堆爽宋惣槍漕綜蒼藻霜賊揃堕惰叢斥脊尖栓煎舛漸繕膳租逝雛椙裾畝凄匂虹韮濡撚巴播婆罵芭牌憧瞳匿凸苫酉惇呑謎楢謄宕淘燈杜喋帖暢蝶諜銚勅槌槻佃蔦綴壷爪吊剃悌挺梯禎諦蹄釘滴轍貼衷鋳枕鱒桝抹繭甫慕菩倣峯捧朋萌蜂褒鳳某鉾頬碧蔑篇附侮撫糞瀕芙箸噺塙隼斑汎畔煩頒蕃蛮卑斐泌緋罷眉膝桧彪廟蛭彬剥恣惧抒搜摯旛暉杞檜檄毬汪洸洒浙溥漱澤瀋炒焉煥甕甦瘤皓眞瞑礒笏笘箏篆簑簗絆綺緻胚脩膠舩廉錬呂魯楼牢狼篭麓禄倭歪詫藁蕨丼偕冤厦曼咸哺喘嗅嗜囃姚尹屏峙嶽已廣彗彭徘遼鱗瑠怜隷遥洛酪藍痢溜琉凌梁稜窯耀庸邑柚湧猷佑薮儲餅貰厄蒙冥牝麺妄孟巳箕蜜湊牟婿襄訃謳贅赳鄒銕鍼闊魏鴈黎茗萬蔡蓼薔藏藝蜷袁");
        QUIZ_TABLE.put(KanjiQuiz.JoyoGrade1, "一貝学右雨円王音下火花五月犬見金九空玉気休耳七字糸四子三山左校口水森人女小上出十手車男竹中大早草足村石赤先千川正生青二日入年土町天田虫本文八百白六力林立夕木目名");
        QUIZ_TABLE.put(KanjiQuiz.JoyoGrade2, "引丸岩顔間海絵外角楽活羽雲園遠黄何夏家科歌画会回午言古戸計元原形近兄教強魚京牛汽記弓帰自室寺時思止紙姉市算細作才行高合国黒今考工広後語交光公図数色食心新親場書少春週首秋弱社知地池茶昼谷台体走多太組切雪線船前星晴声西南肉馬頭同道読内冬刀東当答朝長鳥直通弟店点電毎万歩母方北妹聞米風分父麦半番買売話来理里曜用友門夜野矢毛明鳴");
        QUIZ_TABLE.put(KanjiQuiz.JoyoGrade3, "悪安暗委意医育員飲院館岸寒感漢界開階運泳駅央横屋温化荷湖庫軽決血研県銀区苦具君係橋業局曲去起客宮急球究級期式実詩歯事持次指死始皿仕使坂祭号根港向幸植深申真神身進所暑助勝商昭消章乗集住重宿酒受州拾終習取守主者写柱談着炭短代第題待対想相送息速族他打昔全整世農波配倍動童豆島投湯等登都度帳調追定庭笛鉄転丁注放勉返平部服福物負箱畑発反板悲皮美鼻筆氷表病秒品列練路和緑礼陽落流旅両羊葉様洋遊予有由油薬問役命面味");
        QUIZ_TABLE.put(KanjiQuiz.JoyoGrade4, "愛案以位囲胃衣印器関願喜管観完官改械害街各覚栄英塩億加果課貨芽固芸欠結健建験径景訓軍郡型鏡極挙漁競共協季紀議救求泣給希旗機辞失試児治氏史士参散産残司察札殺刷菜材昨差最告航功好康候信臣順初唱松焼照省笑象賞祝周種借置仲達単隊帯巣争側束続卒孫倉席積折節説戦浅選然清静成熱念敗梅働堂得特毒灯努徒兆腸低停底的典伝貯末包法望牧便兵別変辺副粉不付夫府飯費飛必標票博歴連労老録良量輪類令例冷要養浴利陸料勇約満未脈民無");
        QUIZ_TABLE.put(KanjiQuiz.JoyoGrade5, "圧易移因基眼刊幹慣格確額快営永衛液益演往応恩仮価可河過賀解限個故経潔件券検険減現句群興均禁境許居旧規技義逆久寄識質資飼似示支枝師志賛酸雑桜際在罪財査再妻採災講鉱混構耕効厚護常情条状織職序承招証準術述授修謝舎築断団退貸態総像増造則測属損素績責接設絶舌銭祖税政精製制勢性任燃能破導銅徳独統張提程敵適墓報豊暴貿防弁保編武復複仏貧婦富布判版犯比肥非備俵評領率略留預容余輸迷綿務夢");
        QUIZ_TABLE.put(KanjiQuiz.JoyoGrade6, "異遺域危簡株巻干看灰拡閣革割宇映延沿我呼己系警劇激穴憲権絹厳源敬筋胸郷勤供貴疑吸揮机視詞誌磁私至姿蚕冊裁策砂座済鋼降刻穀骨困皇紅后孝誤垂推蒸針仁城純処署諸除傷将障縮熟衆従縦樹収宗就射捨尺若段値宙忠暖宅担探誕層操窓装臓蔵存尊創奏宣専泉洗染善盛聖誠寸難乳認納脳派俳拝背肺届党糖討庁潮頂賃痛展著枚幕補暮宝訪亡忘棒並閉陛片腹奮班晩否批秘朗論臨欲翌乱卵覧裏律幼郵優訳盟模密");
        QUIZ_TABLE.put(KanjiQuiz.JoyoJuniorHighSchool, "亜哀握扱依偉威尉慰為維緯違井壱逸稲芋姻陰隠閑陥含頑企鑑緩缶肝艦貫還滑褐轄且刈乾冠勘勧喚堪寛患憾換敢棺款歓汗環甘監戒拐皆劾慨概涯該垣嚇核殻獲穫較郭隔岳掛潟喝括渇怪悔懐壊塊韻渦浦影詠鋭疫悦謁越閲宴援炎煙猿縁鉛汚凹奥押欧殴翁沖憶乙卸穏佳嫁寡暇架禍稼箇華菓蚊雅餓介誇雇顧鼓互呉孤弧枯渓継茎蛍鶏迎鯨撃傑倹兼剣圏堅嫌懸献肩謙賢軒遣顕幻弦玄契恵慶憩掲携緊菌襟謹吟駆愚虞偶遇隅屈掘靴繰桑勲薫傾刑啓況狂狭矯脅響驚仰凝暁斤琴挟恭恐峡拒拠虚距享凶叫巨祈軌輝飢騎鬼偽儀宜戯擬欺犠菊吉喫詰却脚虐丘及朽窮糾奇岐幾忌既棋棄軸執湿漆疾諮賜雌侍慈滋璽施旨祉紫肢脂嗣傘惨桟暫伺刺撮擦索錯載剤咲崎削搾詐鎖債催宰彩栽歳砕斎衡貢購郊酵項香剛拷豪克酷獄腰込墾婚恨懇昆紺魂佐唆更江洪溝甲硬稿絞綱肯荒坑孔巧恒慌抗拘控攻娯御悟碁侯酢吹帥炊睡粋衰遂酔錘随髄崇浄畳譲醸錠嘱飾殖触辱伸侵唇娠寝審慎振浸紳薪診辛震刃尋甚尽迅陣剰壌嬢巡遵庶緒叙徐償匠升召奨宵尚床彰抄掌昇晶沼渉焦症硝礁祥称粧紹肖衝訟詔詳鐘丈冗盾准循旬殉潤淑粛塾俊瞬臭舟襲酬醜充柔汁渋獣銃叔儒寿需囚愁秀朱殊狩珠趣寂赦斜煮遮蛇邪勺爵酌釈芝恥痴稚致遅畜蓄逐秩窒嫡抽択拓沢濯託濁諾但奪脱棚丹嘆淡端胆鍛壇弾逮滝卓怠替泰滞胎袋耐駄捜掃挿曹槽燥荘葬藻遭霜騒憎贈促即俗賊堕妥惰訴阻僧双喪壮惜斥析籍跡拙摂窃仙占扇栓潜旋繊薦践遷銑鮮漸禅繕塑措疎礎租粗隻斉牲誓請逝枢据杉澄瀬畝是姓征軟尼弐如尿妊忍寧猫粘悩濃把覇婆廃排杯輩培媒透陶騰闘洞胴峠匿督篤凸突屯豚曇鈍縄謄踏逃奴怒倒凍唐塔悼搭桃棟盗痘筒到斗渡途弔彫徴懲挑眺聴脹超跳勅朕沈珍鎮陳津墜塚漬坪釣亭偵貞呈堤帝廷抵締艇訂逓邸泥摘滴哲徹撤迭添殿吐塗衷鋳駐膜又抹繭慢穂募慕簿倣俸奉峰崩抱泡砲縫胞芳褒邦飽乏傍剖坊妨帽忙房某冒紡肪膨謀僕墨撲朴没堀奔翻凡盆摩磨魔麻埋遍舗捕雰丙併塀幣弊柄壁癖偏賦赴附侮舞封伏幅覆払沸噴墳憤紛浜賓頻敏瓶怖扶敷普浮符腐膚譜縛肌鉢髪伐罰抜閥伴帆搬畔繁般藩販範煩頒盤蛮卑妃彼扉披泌疲碑罷被避尾微匹姫漂描苗拍泊舶薄迫漠爆賠陪伯霊麗齢暦劣烈裂廉恋錬炉露廊楼浪漏郎賄惑枠湾腕糧陵倫厘隣塁涙累励鈴隷零謡踊抑翼羅裸頼雷絡酪欄濫吏履痢離硫粒隆竜慮虜了僚寮涼猟療窯誉庸揚揺擁溶与雄融裕誘悠憂猶諭唯幽柳愉癒躍黙戻紋匁厄猛盲網耗銘滅免茂妄漫魅岬妙眠矛霧婿娘");
    }
    private static final Map<Character, Integer> JLPT_LEVEL = new HashMap<Character, Integer>();

    static {
        for (int i = 1; i <= 5; i++) {
            final String jlpt = getJlptKanjis(i);
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
     * @return JLPT level N1..5, null if the kanji is not present in any of the JLPT test. See http://www.tanos.co.uk/jlpt/jlpt1/kanji/ for details.
     */
    public static Integer getJlptLevel(final Character kanji) {
        return JLPT_LEVEL.get(kanji);
    }

    public static String getJlptKanjis(final int level) {
        if (level < 1 || level > 5) {
            throw new IllegalArgumentException("JLPT level must be 1..5: " + level);
        }
        final KanjiQuiz quiz = KanjiQuiz.values()[level - 1];
        if(!quiz.isJlpt) {
            throw new AssertionError("Expected jlpt but got "+quiz);
        }
        return QUIZ_TABLE.get(quiz);
    }
}
