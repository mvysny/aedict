package sk.baka.aedict.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Contains ISO 639-3 language codes.
 * @author Martin Vysny
 */
public class Iso6393Codes {

    private static final Map<String, String> CODES = new HashMap<String, String>();

    static {
        final String[] packedCodes = {"acm", "Mesopotamian Arabic", "afr", "Afrikaans", "ara", "Arabic", "arz", "Egyptian Arabic", "bel", "Belarusian", "ben", "Bengali", "bos", "Bosnian", "bre", "Breton", "bul", "Bulgarian", "cat", "Catalan", "ces", "Czech", "cha", "Chamorro", "cmn", "Mandarin Chinese", "dan", "Danish", "deu", "German", "ell", "Modern Greek (1453-)", "eng", "English", "epo", "Esperanto", "est", "Estonian", "eus", "Basque", "fao", "Faroese", "fin", "Finnish", "fra", "French", "fry", "Western Frisian", "gle", "Irish", "glg", "Galician", "heb", "Hebrew", "hin", "Hindi", "hrv", "Croatian", "hun", "Hungarian", "hye", "Armenian", "ina", "Interlingua (International Auxiliary Language Association)", "ind", "Indonesian", "isl", "Icelandic", "ita", "Italian", "jbo", "Lojban", "kat", "Georgian", "kaz", "Kazakh", "kor", "Korean", "lat", "Latin", "lit", "Lithuanian", "lvs", "Standard Latvian", "lzh", "Literary Chinese", "mal", "Malayalam", "mon", "Mongolian", "nan", "Min Nan Chinese", "nds", "Low German", "nld", "Dutch", "nob", "Norwegian Bokmål", "non", "Old Norse", "orv", "Old Russian", "oss", "Ossetian", "pes", "Iranian Persian", "pol", "Polish", "por", "Portuguese", "que", "Quechua", "roh", "Romansh", "ron", "Romanian", "rus", "Russian", "scn", "Sicilian", "slk", "Slovak", "slv", "Slovenian", "spa", "Spanish", "sqi", "Albanian", "srp", "Serbian", "swe", "Swedish", "swh", "Swahili (individual language)", "tat", "Tatar", "tgl", "Tagalog", "tha", "Thai", "tlh", "Klingon", "tur", "Turkish", "uig", "Uighur", "ukr", "Ukrainian", "urd", "Urdu", "uzb", "Uzbek", "vie", "Vietnamese", "vol", "Volapük", "wuu", "Wu Chinese", "yid", "Yiddish", "yue", "Yue Chinese", "zsm", "Standard Malay"};
        for (int i = 0; i < packedCodes.length / 2; i++) {
            CODES.put(packedCodes[i * 2], packedCodes[i * 2 + 1]);
        }
    }

    public static Set<String> getAllCodes() {
        return Collections.unmodifiableSet(CODES.keySet());
    }

    public static String getLanguageName(String code) {
        Check.checkNotNull("code", code);
        final String result = CODES.get(code);
        if (result == null) {
            throw new RuntimeException("Unknown ISO 639-3 language code: " + code);
        }
        return result;
    }

    public static SortedMap<String, String> getSortedLangNames() {
        final SortedMap<String, String> result= new TreeMap<String, String>();
        for(final Map.Entry<String, String> e:CODES.entrySet()){
            result.put(e.getValue(), e.getKey());
        }
        return result;
    }
}
