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
package sk.baka.aedict.dict;

import org.junit.Test;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.tools.test.Assert;

/**
 * Tests the {@link SearchQuery} class.
 * @author Martin Vysny
 */
public class SearchQueryTest {

    @Test
    public void tanakaAndParserEn() {
	final SearchQuery q = SearchQuery.searchTanaka(DictTypeEnum.Tanaka, "mother AND father", false, null, null);
	Assert.assertArrayEquals(q.query, new String[]{"mother AND father"});
    }

    @Test
    public void tanakaAndParserJp() {
	final RomanizationEnum r = RomanizationEnum.NihonShiki;
	final SearchQuery q = SearchQuery.searchTanaka(DictTypeEnum.Tanaka, "haha AND chichi", true, r, null);
	Assert.assertArrayEquals(q.query, new String[]{r.toKatakana("haha") + " AND " + r.toKatakana("chichi"), r.toHiragana("haha") + " AND " + r.toHiragana("chichi")});
    }
}
