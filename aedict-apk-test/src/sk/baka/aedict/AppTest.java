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

/**
 * Tests the application startup.
 * 
 * @author Martin Vysny
 */
import sk.baka.aedict.AedictApp;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class AppTest extends ApplicationTestCase<AedictApp> {

	public AppTest() {
		super(AedictApp.class);
	}

	/**
	 * The name 'test preconditions' is a convention to signal that if this test
	 * doesn't pass, the test case was not set up properly and it might explain
	 * any and all failures in other tests. This is not guaranteed to run before
	 * other tests, as junit uses reflection to find the tests.
	 */
	@SmallTest
	public void testPreconditions() {
	}

	/**
	 * Test basic startup/shutdown of Application
	 */
	@MediumTest
	public void testSimpleCreate() {
		createApplication();
	}
}
