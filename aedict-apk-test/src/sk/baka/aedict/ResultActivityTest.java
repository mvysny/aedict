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

import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.RomanizationEnum;
import android.content.Intent;
import android.widget.ListView;

/**
 * Tests the ResultActivity activity.
 * 
 * @author Martin Vysny
 * 
 */
public class ResultActivityTest extends ActivityTestHelper<ResultActivity> {

	public ResultActivityTest() {
		super(ResultActivity.class);
	}

	public void testSimpleEnglishSearch() {
		final SearchQuery q = new SearchQuery();
		q.isJapanese = false;
		q.matcher = MatcherEnum.ExactMatchEng;
		q.query = new String[] { "mother" };
		final Intent i = new Intent(getInstrumentation().getContext(), ResultActivity.class);
		q.putTo(i);
		startActivity(i);
		final ListView lv = getActivity().getListView();
		assertEquals(21, lv.getCount());
		final EdictEntry entry = (EdictEntry) lv.getItemAtPosition(0);
		assertEquals("(n) (hum) mother/(P)", entry.english);
		assertEquals("母", entry.getJapanese());
	}

	public void testSimpleJapaneseSearch() {
		final SearchQuery q = new SearchQuery();
		q.isJapanese = true;
		q.matcher = MatcherEnum.ExactMatchEng;
		q.query = new String[] { RomanizationEnum.Hepburn.toHiragana("haha") };
		final Intent i = new Intent(getInstrumentation().getContext(), ResultActivity.class);
		q.putTo(i);
		startActivity(i);
		final ListView lv = getActivity().getListView();
		assertEquals(1, lv.getCount());
		final EdictEntry entry = (EdictEntry) lv.getItemAtPosition(0);
		assertEquals("(n) (hum) mother/(P)", entry.english);
		assertEquals("母", entry.getJapanese());
	}
}
