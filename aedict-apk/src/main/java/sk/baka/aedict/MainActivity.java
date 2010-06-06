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

import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.DownloaderService;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.autils.DialogUtils;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

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
		utils.registerSearch(R.id.exactMatch, R.id.jpDeinflectVerbs, null, R.id.searchEdit, false, R.id.jpSearch, true);
		utils.registerSearch(R.id.exactMatch, null, R.id.searchExamples, R.id.searchEdit, false, R.id.englishSearch, false);
		utils.setupAnalysisControls(R.id.btnJpTranslate, R.id.txtJpTranslate, true);
		findViewById(R.id.advanced).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final View g = findViewById(R.id.advancedPanel);
				g.setVisibility(g.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
			}
		});
		// check for dictionary file and download it if it is missing.
		bindService(new Intent(this, DownloaderService.class), new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder service) {
				((DownloaderService.LocalBinder) service).getService().checkDic(MainActivity.this, DictTypeEnum.Edict);
			}

			public void onServiceDisconnected(ComponentName className) {
			}
		}, Context.BIND_AUTO_CREATE);
		if (!AedictApp.isInstrumentation) {
			new DialogUtils(this).showInfoOnce(AedictApp.getVersion(), AedictApp.format(R.string.whatsNew, AedictApp.getVersion()), getString(R.string.whatsNewText));
		}
		findViewById(R.id.intro).setVisibility(true ? View.VISIBLE : View.GONE);
		((TextView) findViewById(R.id.aedict)).setText("Aedict " + AedictApp.getVersion());
	}
}