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
import android.content.Intent;

/**
 * Tests the {@link EdictEntryDetailActivity} activity.
 * @author Martin Vysny
 */
public class EdictEntryDetailActivityTest extends AbstractAedictTest<EdictEntryDetailActivity> {
	public EdictEntryDetailActivityTest() {
		super(EdictEntryDetailActivity.class);
	}

	public void testStartActivity() {
		final Intent i=new Intent(getInstrumentation().getContext(), EdictEntryDetailActivity.class);
		i.putExtra(EdictEntryDetailActivity.INTENTKEY_ENTRY, new EdictEntry("合う","あう","(v5u,vi) (1) to come together/to merge/to unite/to meet/(2) to fit/to match/to suit/to agree with/to be correct/(3) to be profitable/to be equitable/(suf,v5u) (4) (after the -masu stem of a verb) to do ... to each other/to do ... together/(P)/"));
		tester.startActivity();
	}

	/**
	 * See issue http://code.google.com/p/aedict/issues/detail?id=71 for details.
	 */
	public void testAllSensesAreShown() {
		testStartActivity();
		tester.assertText(R.id.entrySenses, "(1) to come together, to merge, to unite, to meet\n(2) to fit, to match, to suit, to agree with, to be correct\n(3) to be profitable,to be equitable\n(4) (after the -masu stem of a verb) to do ... to each other, to do ... together");
	}
}
