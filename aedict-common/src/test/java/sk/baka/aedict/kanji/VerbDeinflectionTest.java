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

import static sk.baka.tools.test.Assert.assertArrayEqualsNoOrder;
import static sk.baka.tools.test.Assert.assertUtilityClass;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Tests the {@link VerbDeinflection} class.
 * 
 * @author Martin Vysny
 * 
 */
public class VerbDeinflectionTest {

    @Test
    public void testUtilityClass() {
        assertUtilityClass(VerbDeinflection.class);
    }

    @Test
    public void testNonVerb() {
        assertArrayEqualsNoOrder(VerbDeinflection.deinflect("kirei"), Arrays.asList("kirei"));
    }

    @Test
    public void testDeinflectDesu() {
        assertDeinflected("desu", "だ", "でわない", "じゃない", "じゃない", "だった", "ではなかった", "じゃなかった", "で", "です", "ではありません", "じゃありません", "でした", "ではありませんでした", "じゃありませんでした");
    }

    @Test
    public void testDeinflectKuru() {
        assertDeinflected("kuru", "kuru", "konai", "kita", "konakatta", "kite", "kimasu", "kimasen", "kimashita", "kimasen deshita");
        assertDeinflected("korareru", "korareru", "korarenai", "korarete");
    }

    @Test
    public void testDeinflectSuru() {
        assertDeinflected("suru", "する", "しない", "した", "しなかった", "して", "します", "しません", "しました", "しませんでした");
        assertDeinflected("sareru", "される", "されない", "された");
    }

    @Test
    public void testDeinflectIku() {
        assertDeinflected("iku", "iku", "ikanai", "itta", "ikanakatta", "itte", "ikimasu", "ikimasen", "ikimashita", "ikimasen deshita");
        assertDeinflected("ikareru", "ikareru", "ikarenai", "ikareta");
    }

    @Test
    public void testDeinflectAru() {
        assertDeinflected("aru", "ある", "ない", "なかった", "あります", "ありません", "ありました", "ありませんでした");
        assertDeinflected(Arrays.asList("aru", "au"), "あった", "あって");
    }

    @Test
    public void testDeinflectTaberu() {
        // a bit weird to deinflect taberu to tabu, however we have to be able
        // to also deinflect aeru to au
        assertDeinflected(Arrays.asList("taberu", "tabu"), "taberu", "tabenai", "tabenakatta", "tabemasu", "tabemasen", "tabemashita", "tabemasen deshita");
        assertDeinflected("taberu", "tabeta", "tabete");
        assertDeinflected("taberareru", "taberareru", "taberarenai", "taberareta");
    }

    @Test
    public void testDeinflectKaku() {
        assertDeinflected(Arrays.asList("kaku", "kakiru"), "かきます", "かきません", "かきました", "かきませんでした");
        assertDeinflected(Arrays.asList("kaku", "kairu"), "かいた", "かいて");
        assertDeinflected("kaku", "かく", "かかない", "かかなかった");
        assertDeinflected("kakareru", "かかれる", "かかれない", "かかれた");
    }

    @Test
    public void testDeinflectIsogu() {
        assertDeinflected("isogu", "isoida", "isoide", "isogu", "isoganai", "isoganakatta");
        assertDeinflected(Arrays.asList("isogu", "isogiru"), "isogimasu", "isogimasen", "isogimashita", "isogimasen deshita");
    }

    @Test
    public void testDeinflectKasu() {
        assertDeinflected("kasu", "kashita", "kashite", "kasu", "kasanai", "kasanakatta");
        assertDeinflected(Arrays.asList("kasu", "kasiru"), "kashimasu", "kashimasen", "kashimashita", "kashimasen deshita");
    }

    @Test
    public void testDeinflectMatsu() {
        assertDeinflected(Arrays.asList("matu", "maru", "mau"), "matta", "matte");
        assertDeinflected("matu", "matsu", "matanai", "matanakatta");
        assertDeinflected(Arrays.asList("matu", "matiru"), "machimasu", "machimasen", "machimashita", "machimasen deshita");
    }

    @Test
    public void testDeinflectShinu() {
        assertDeinflected(Arrays.asList("sinu", "simu", "sibu"), "しんだ", "しんで");
        assertDeinflected("sinu", "しぬ", "しなない", "しななかった");
        assertDeinflected(Arrays.asList("sinu", "siniru"), "しにます", "しにません", "しにました", "しにませんでした");
    }

    @Test
    public void testDeinflectMiru() {
        // a bit weird to deinflect taberu to tabu, however we have to be able
        // to also deinflect aeru to au
        assertDeinflected("miru", "miru", "minai", "minakatta");
        assertDeinflected(Arrays.asList("miru", "mku"), "mita", "mite");
        assertDeinflected(Arrays.asList("miru", "mu"), "mimasu", "mimasen", "mimashita", "mimasen deshita");
    }

    @Test
    public void testDeinflectKau() {
        assertDeinflected("kau", "かう", "かわない", "かわなかった");
        assertDeinflected(Arrays.asList("kau", "kairu"), "かいます", "かいません", "かいました", "かいませんでした");
        assertDeinflected(Arrays.asList("kau", "katu", "karu"), "かった", "かって");
        assertDeinflected("kawareru", "かわれる", "かわれない", "かわれた");
    }

    @Test
    public void testDeinflectAu() {
        assertDeinflected("au", "au", "awanai");
        assertDeinflected(Arrays.asList("au", "aru"), "atta");
        // a bit weird, however we have to be able to also "deinflect" taberu to
        // taberu, along with tabu
        assertDeinflected(Arrays.asList("au", "aeru"), "aenai");
    }

    @Test
    public void testDeinflectAsobu() {
        assertDeinflected(Arrays.asList("asobu", "asobiru"), "あそびます", "あそびませんでした");
        assertDeinflected(Arrays.asList("asobu", "asonu", "asomu"), "あそんだ", "あそんで");
        assertDeinflected("asobu", "あそぶ", "あそばない", "あそばなかった");
    }

    @Test
    public void testDeinflectYomu() {
        assertDeinflected(Arrays.asList("yomu", "yomiru"), "yomimasen deshita");
        assertDeinflected("yomu", "yomu", "yomanai", "yomanakatta");
        assertDeinflected(Arrays.asList("yomu", "yonu", "yobu"), "yonda", "yonde");
    }

    @Test
    public void testDeinflectKaeru() {
        assertDeinflected(Arrays.asList("kaeru", "kaeriru", "kau"), "kaerimasen deshita");
        assertDeinflected(Arrays.asList("kaeru", "kau"), "kaeru");
        assertDeinflected(Arrays.asList("kaeru"), "kaeranai", "kaeranakatta");
        assertDeinflected(Arrays.asList("kaeru", "kaeu", "kaetu"), "kaetta", "kaette");
    }

    private void assertDeinflected(final String expected, final String... deinflects) {
        assertDeinflected(Arrays.asList(expected), deinflects);
    }

    private void assertDeinflected(final List<String> expected, final String... deinflects) {
        for (final String deinflect : deinflects) {
            String deinflectNihonShiki = deinflect;
            if (!KanjiUtils.isHiragana(deinflectNihonShiki.charAt(0))) {
                // convert to nihon-shiki
                deinflectNihonShiki = RomanizationEnum.Hepburn.toHiragana(deinflectNihonShiki);
                deinflectNihonShiki = RomanizationEnum.NihonShiki.toRomaji(deinflectNihonShiki);
            }
            assertArrayEqualsNoOrder(VerbDeinflection.deinflect(deinflectNihonShiki), expected, "Deinflecting " + deinflectNihonShiki);
        }
    }
}
