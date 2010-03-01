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

package sk.baka.aedict.skip;

import java.util.List;

import android.content.Intent;
import sk.baka.aedict.AbstractAedictTest;
import sk.baka.aedict.KanjiAnalyzeActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.DictEntry;

/**
 * Tests the {@link Skip4Activity} class.
 * 
 * @author Martin Vysny
 */
public class Skip4ActivityTest extends AbstractAedictTest<Skip4Activity> {

	public Skip4ActivityTest() {
		super(Skip4Activity.class);
	}

	public void testSearch1() {
		tester.startActivity();
		tester.setText(R.id.editSkipNumberOfStrokes, "2");
		tester.click(R.id.skip41);
		assertSkipSearch("4-2-1");
	}

	public void testSearch2() {
		tester.startActivity();
		tester.setText(R.id.editSkipNumberOfStrokes, "3");
		tester.click(R.id.skip42);
		assertSkipSearch("4-3-2");
	}

	public void testSearch3() {
		tester.startActivity();
		tester.setText(R.id.editSkipNumberOfStrokes, "4");
		tester.click(R.id.skip43);
		assertSkipSearch("4-4-3");
	}

	public void testSearch4() {
		tester.startActivity();
		tester.setText(R.id.editSkipNumberOfStrokes, "5");
		tester.click(R.id.skip44);
		assertSkipSearch("4-5-4");
	}

	private void assertSkipSearch(final String skipNumber) {
		final Intent i = getStartedActivityIntent();
		final List<DictEntry> entries = (List<DictEntry>) i.getSerializableExtra(KanjiAnalyzeActivity.INTENTKEY_ENTRYLIST);
		assertFalse(entries.isEmpty());
		for (DictEntry e : entries) {
			assertEquals(skipNumber, e.skip);
		}
	}
}
