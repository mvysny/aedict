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
import java.io.IOException;

import sk.baka.aedict.AedictApp.Config;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

/**
 * Configures AEdict.
 * 
 * @author Martin Vysny
 */
public class ConfigActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		final Config cfg = AedictApp.loadConfig();
		final CheckBox cfgNotifBar = (CheckBox) findViewById(R.id.cfgNotifBar);
		cfgNotifBar.setChecked(cfg.isAlwaysAvailable);
		cfgNotifBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				final Config cfg = new Config(false);
				cfg.isAlwaysAvailable = isChecked;
				AedictApp.saveConfig(cfg);
			}
		});
		final Button cleanup = (Button) findViewById(R.id.cleanupEdictFilesButton);
		cleanup.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				cleanup();
			}

		});
		final Spinner s = (Spinner) findViewById(R.id.romanizationSystem);
		s.setSelection(cfg.romanization.ordinal());
		s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final Config cfg = new Config(false);
				cfg.romanization = RomanizationEnum.values()[arg2];
				AedictApp.saveConfig(cfg);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private void cleanup() {
		final AndroidUtils utils = new AndroidUtils(this);
		utils.showYesNoDialog(AedictApp.format(R.string.deleteEdictFiles, MiscUtils.getLength(new File(DownloadEdictTask.BASE_DIR)) / 1024), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					MiscUtils.deleteDir(new File(DownloadEdictTask.BASE_DIR));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				utils.showInfoDialog(R.string.data_files_removed);
			}

		});
	}
}
