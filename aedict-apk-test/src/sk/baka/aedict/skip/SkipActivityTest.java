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

import android.content.Intent;
import sk.baka.aedict.ActivityTestHelper;
import sk.baka.aedict.R;

public class SkipActivityTest extends ActivityTestHelper<SkipActivity> {

	public SkipActivityTest() {
		super(SkipActivity.class);
	}

	public void testLaunchActivity() {
		startActivity();
	}

	public void testSkip1Click() {
		startActivity();
		click(R.id.skip11);
		assertStartedActivity(Skip123Activity.class);
		final Intent i = getStartedActivityIntent();
		assertEquals(1, i.getIntExtra(Skip123Activity.INTENTKEY_TYPE, -1));
	}

	public void testSkip2Click() {
		startActivity();
		click(R.id.skip12);
		assertStartedActivity(Skip123Activity.class);
		final Intent i = getStartedActivityIntent();
		assertEquals(2, i.getIntExtra(Skip123Activity.INTENTKEY_TYPE, -1));
	}

	public void testSkip3Click() {
		startActivity();
		click(R.id.skip13);
		assertStartedActivity(Skip123Activity.class);
		final Intent i = getStartedActivityIntent();
		assertEquals(3, i.getIntExtra(Skip123Activity.INTENTKEY_TYPE, -1));
	}

	public void testSkip4Click() {
		startActivity();
		click(R.id.skip14);
		assertStartedActivity(Skip4Activity.class);
	}
}
