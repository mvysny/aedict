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

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

/**
 * Contains several useful methods which helps activity testing.
 * 
 * @author Martin Vysny
 */
public class ActivityTestHelper<T extends Activity> extends ActivityUnitTestCase<T> {

	private final Class<T> activityClass;

	public ActivityTestHelper(Class<T> activityClass) {
		super(activityClass);
		this.activityClass = activityClass;
	}

	/**
	 * Starts the activity and asserts that it is really started.
	 */
	protected void startActivity(final Intent intent) {
		final T activity = startActivity(intent, null, null);
		assertSame(activity, getActivity());
		assertTrue(activityClass.isInstance(activity));
	}

	/**
	 * Starts the activity and asserts that it is really started.
	 */
	protected void startActivity() {
		startActivity(new Intent(Intent.ACTION_MAIN));
	}

	/**
	 * Asserts that the current activity requested start of given activity.
	 * 
	 * @param activity
	 *            the new activity
	 */
	protected void assertStartedActivity(final Class<? extends Activity> activity) {
		final Intent i = getStartedActivityIntent();
		if (i == null) {
			throw new AssertionError("The activity did not requested a start of another activity yet");
		}
		assertEquals(activity.getName(), i.getComponent().getClassName());
	}
}
