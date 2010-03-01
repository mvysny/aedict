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

import sk.baka.aedict.AbstractAedictTest;
import sk.baka.aedict.KanjiAnalyzeActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.DictEntry;
import android.content.Intent;

/**
 * Tests the {@link Skip123Activity} class.
 * 
 * @author Martin Vysny
 */
public class Skip123ActivityTest extends AbstractAedictTest<Skip123Activity> {

	public Skip123ActivityTest() {
		super(Skip123Activity.class);
	}

	public void testStartActivity() {
		startActivity(1);
	}

	private void startActivity(int skipType) {
		final Intent i = new Intent(getInstrumentation().getContext(), Skip123Activity.class);
		i.putExtra(Skip123Activity.INTENTKEY_TYPE, skipType);
		tester.startActivity(i);
	}

	public void testPerformSkip1Search() {
		startActivity(1);
		tester.assertText(R.id.skip123, R.string.skip1tutorial);
		tester.assertText(R.id.textSkipFirst, R.string.skip1first);
		tester.assertText(R.id.textSkipSecond, R.string.skip1second);
		tester.setText(R.id.editSkipFirst, "2");
		tester.setText(R.id.editSkipSecond, "3");
		tester.click(R.id.btnSkip123Search);
		assertSkipSearch("1-2-3");
	}

	public void testPerformSkip2Search() {
		startActivity(2);
		tester.assertText(R.id.skip123, R.string.skip2tutorial);
		tester.assertText(R.id.textSkipFirst, R.string.skip2first);
		tester.assertText(R.id.textSkipSecond, R.string.skip2second);
		tester.setText(R.id.editSkipFirst, "3");
		tester.setText(R.id.editSkipSecond, "4");
		tester.click(R.id.btnSkip123Search);
		assertSkipSearch("2-3-4");
	}

	public void testPerformSkip3Search() {
		startActivity(3);
		tester.assertText(R.id.skip123, R.string.skip3tutorial);
		tester.assertText(R.id.textSkipFirst, R.string.skip3first);
		tester.assertText(R.id.textSkipSecond, R.string.skip3second);
		tester.setText(R.id.editSkipFirst, "4");
		tester.setText(R.id.editSkipSecond, "5");
		tester.click(R.id.btnSkip123Search);
		assertSkipSearch("3-4-5");
	}

	private void assertSkipSearch(final String skipNumber) {
		final Intent i = getStartedActivityIntent();
		final List<DictEntry> entries = (List<DictEntry>) i.getSerializableExtra(KanjiAnalyzeActivity.INTENTKEY_ENTRYLIST);
		assertFalse(entries.isEmpty());
		for (DictEntry e : entries) {
			if (!e.isValid()) {
				throw new AssertionError("Invalid entry encountered: " + e.english);
			}
			assertEquals(skipNumber, e.skip);
		}
	}
}
