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

import sk.baka.aedict.dict.DownloadDictTask;
import sk.baka.aedict.util.SearchUtils;
import android.os.Bundle;

/**
 * Provides means to search the edict dictionary file.
 * 
 * @author Martin Vysny
 */
public class MainActivity extends AbstractActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final SearchUtils utils = new SearchUtils(this);
		utils.registerSearch(R.id.jpExactMatch, R.id.jpSearchEdit, false, R.id.jpSearch, true);
		utils.registerSearch(R.id.engExactMatch, R.id.engSearchEdit, false, R.id.engSearch, false);
		setButtonActivityLauncher(R.id.btnAbout, AboutActivity.class);
		// check for dictionary file and download it if it is missing.
		utils.checkDictionaryFile(DownloadDictTask.EDICT_LUCENE_ZIP, DownloadDictTask.LUCENE_INDEX, 20L * 1024 * 1024, "EDict");
	}
}