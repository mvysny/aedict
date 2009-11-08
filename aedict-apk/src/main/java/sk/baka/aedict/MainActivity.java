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

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StatFs;
import android.view.Menu;

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
		// check for dictionary file and download it if it is missing.
		if (!DownloadEdictTask.isComplete(DownloadEdictTask.LUCENE_INDEX)) {
			final StatFs stats = new StatFs("/sdcard");
			final long free = ((long) stats.getBlockSize()) * stats.getAvailableBlocks();
			final StringBuilder msg = new StringBuilder(getString(R.string.edict_missing_download));
			if (free < 20 * 1000 * 1000) {
				msg.append('\n');
				msg.append(AedictApp.format(R.string.warning_less_than_20mb_free, free / 1024));
			}
			new AndroidUtils(this).showYesNoDialog(msg.toString(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					new DownloadEdictTask(MainActivity.this, DownloadEdictTask.EDICT_LUCENE_ZIP, DownloadEdictTask.LUCENE_INDEX).execute();
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		addActivityLauncher(menu, R.string.aboutCaption, android.R.drawable.ic_menu_info_details, AboutActivity.class);
		addActivityLauncher(menu, R.string.showkanaTable, R.drawable.kanamenuitem, KanaTableActivity.class);
		addActivityLauncher(menu, R.string.configuration, android.R.drawable.ic_menu_manage, ConfigActivity.class);
		return true;
	}

}