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
import android.test.ActivityUnitTestCase;

/**
 * Contains several useful methods which helps activity testing.
 * 
 * @author Martin Vysny
 */
public abstract class AbstractAedictTest<T extends Activity> extends ActivityUnitTestCase<T> {

	/**
	 * The Android tester utility class.
	 */
	protected final AndroidTester<T> tester;

	public AbstractAedictTest(Class<T> activityClass) {
		super(activityClass);
		tester = new AndroidTester<T>(this, activityClass);
		AedictApp.isInstrumentation = true;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// reset the config
		final AedictApp.Config cfg = new AedictApp.Config();
		cfg.setDefaults();
		AedictApp.saveConfig(cfg);
	}
}
