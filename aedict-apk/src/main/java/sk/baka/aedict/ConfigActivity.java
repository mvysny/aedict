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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DownloadDictTask;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
		cfgNotifBar.setOnCheckedChangeListener(AndroidUtils.safe(this, new CompoundButton.OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				final Config cfg = new Config(false);
				cfg.isAlwaysAvailable = isChecked;
				AedictApp.saveConfig(cfg);
			}
		}));
		final CheckBox cfgUseRomaji = (CheckBox) findViewById(R.id.cfgUseRomaji);
		cfgUseRomaji.setChecked(cfg.useRomaji);
		cfgUseRomaji.setOnCheckedChangeListener(AndroidUtils.safe(this, new CompoundButton.OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				final Config cfg = new Config(false);
				cfg.useRomaji = isChecked;
				AedictApp.saveConfig(cfg);
			}
		}));
		final Button cleanup = (Button) findViewById(R.id.cleanupEdictFilesButton);
		cleanup.setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				cleanup();
			}

		}));
		final Spinner s = (Spinner) findViewById(R.id.romanizationSystem);
		s.setSelection(cfg.romanization.ordinal());
		s.setOnItemSelectedListener(AndroidUtils.safe(this, new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final Config cfg = new Config(false);
				cfg.romanization = RomanizationEnum.values()[arg2];
				AedictApp.saveConfig(cfg);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		}));
		final Spinner dictPicker = (Spinner) findViewById(R.id.spinDictionaryPicker);
		final List<String> dictionaries = new ArrayList<String>(listEdictDictionaries().keySet());
		Collections.sort(dictionaries);
		dictPicker.setAdapter(new ArrayAdapter<String>(this, -1, dictionaries));
	}

	/**
	 * Deletes all dictionary files.
	 */
	private void cleanup() {
		final DialogUtils utils = new DialogUtils(this);
		utils.showYesNoDialog(AedictApp.format(R.string.deleteDictionaryFiles, MiscUtils.getLength(new File(DownloadDictTask.BASE_DIR)) / 1024), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					MiscUtils.deleteDir(new File(DownloadDictTask.BASE_DIR));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				utils.showToast(R.string.data_files_removed);
			}

		});
	}

	/**
	 * Lists all available edict dictionaries.
	 * 
	 * @return maps a dictionary name to to an absolute directory name (e.g.
	 *         /sdcard/aedict/index). The list will always contain the default
	 *         dictionary.
	 */
	private Map<String, String> listEdictDictionaries() {
		final Map<String, String> result = new HashMap<String, String>();
		result.put("Default", "/sdcard/aedict/index");
		final File aedict = new File("/sdcard/aedict");
		if (aedict.exists() && aedict.isDirectory()) {
			final String[] dictionaries = aedict.list(new FilenameFilter() {

				public boolean accept(File dir, String filename) {
					return filename.toLowerCase().startsWith("index-");
				}
			});
			for (final String dict : dictionaries) {
				result.put(dict.substring("index-".length()), "/sdcard/aedict/" + dict);
			}
		}
		return result;
	}
}
