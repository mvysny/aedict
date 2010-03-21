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
import java.util.Arrays;
import java.util.List;

import sk.baka.aedict.dict.DictTypeEnum;
import android.preference.ListPreference;

/**
 * Tests the {@link ConfigActivity} class.
 * 
 * @author Martin Vysny
 */
public class ConfigActivityTest extends AbstractAedictTest<ConfigActivity> {

	public ConfigActivityTest() {
		super(ConfigActivity.class);
	}

	public void testStartActivity() {
		tester.startActivity();
	}

	/**
	 * Tests that the Tanaka dictionary is not listed in the EDict dictionary
	 * list.
	 */
	public void testTanakaDictionaryIsNotListed() {
		final File tanaka = new File(DictTypeEnum.Tanaka.getDefaultDictionaryPath());
		assertTrue("The tanaka dictionary does not exist, please download it", tanaka.exists());
		tester.startActivity();
		final List<CharSequence> dictionaries = Arrays.asList(((ListPreference) getActivity().findPreference(ConfigActivity.KEY_DICTIONARY_NAME)).getEntryValues());
		assertFalse("The tanaka directory is listed in the combobox", dictionaries.contains("tanaka"));
	}
}
