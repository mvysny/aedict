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

import java.io.File;

import android.widget.Spinner;

import sk.baka.aedict.dict.DictTypeEnum;

/**
 * Tests the {@link ConfigActivity} class.
 * 
 * @author Martin Vysny
 */
public class ConfigActivityTest extends ActivityTestHelper<ConfigActivity> {

	public ConfigActivityTest() {
		super(ConfigActivity.class);
	}

	public void testStartActivity() {
		startActivity();
	}

	/**
	 * Tests that the Tanaka dictionary is not listed in the EDict dictionary
	 * list.
	 */
	public void testTanakaDictionaryIsNotListed() {
		final File tanaka = new File(DictTypeEnum.Tanaka.getDefaultDictionaryPath());
		assertTrue("The tanaka dictionary does not exist, please download it", tanaka.exists());
		startActivity();
		getInstrumentation().callActivityOnResume(getActivity());
		assertFalse("The tanaka directory is listed in the combobox", isAdapterViewContains(R.id.spinDictionaryPicker, "tanaka"));
	}
}
