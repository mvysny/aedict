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
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.MiscUtils;
import sk.baka.autils.bind.AndroidViewMapper;
import sk.baka.autils.bind.Binder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;

/**
 * Configures AEdict.
 * 
 * @author Martin Vysny
 */
public class ConfigActivity extends PreferenceActivity {
	/**
	 * Boolean: Always available? (Adds or removes notification icon).
	 */
	public static String KEY_ALWAYS_AVAILABLE = "alwaysAvailable";
	/**
	 * String (the name of the {@link RomanizationEnum} enum). Which
	 * romanization system to use.
	 */
	public static String KEY_ROMANIZATION = "romanization";
	/**
	 * Boolean. If true then Romaji will be used instead of katakana/hiragana
	 * throughout the application.
	 */
	public static String KEY_USE_ROMAJI = "useRomaji";
	/**
	 * Launches the {@link DownloadDictionaryActivity} activity.
	 */
	public static String KEY_DOWNLOAD_DICTIONARIES = "downloadDictionaries";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.config);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		final String key = preference.getKey();
		if (key.equals(KEY_DOWNLOAD_DICTIONARIES)) {
			final Intent intent = new Intent(this, DownloadDictionaryActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// components are now initialized in onResume phase, to refresh
		// dictionary list when a new dictionary is downloaded
		// fill in the components
		// final Config cfg = AedictApp.loadConfig();
		// final Spinner dictPicker = (Spinner)
		// findViewById(R.id.spinDictionaryPicker);
		// final List<String> dictionaries = new
		// ArrayList<String>(DownloadDictTask.listEdictDictionaries().keySet());
		// Collections.sort(dictionaries);
		// dictPicker.setAdapter(new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_item, dictionaries));
		// dictPicker.setOnItemSelectedListener(new ModificationHandler());
		// new Binder().bindFromBean(cfg, new AndroidViewMapper(true), this,
		// false);
		// // add modification handlers
		// final CheckBox cfgNotifBar = (CheckBox)
		// findViewById(R.id.cfgNotifBar);
		// cfgNotifBar.setOnCheckedChangeListener(new ModificationHandler());
		// final CheckBox cfgUseRomaji = (CheckBox)
		// findViewById(R.id.cfgUseRomaji);
		// cfgUseRomaji.setOnCheckedChangeListener(new ModificationHandler());
		// final Button cleanup = (Button)
		// findViewById(R.id.cleanupEdictFilesButton);
		// cleanup.setOnClickListener(AndroidUtils.safe(this, new
		// View.OnClickListener() {
		//
		// public void onClick(View v) {
		// cleanup();
		// }
		//
		// }));
		// final Button showInfoDialogs = (Button)
		// findViewById(R.id.showInfoDialogsButton);
		// showInfoDialogs.setOnClickListener(AndroidUtils.safe(this, new
		// View.OnClickListener() {
		//
		// public void onClick(View v) {
		// final DialogUtils utils = new DialogUtils(ConfigActivity.this);
		// utils.clearInfoOccurency();
		// utils.showToast(R.string.showInfoDialogsEnabled);
		// }
		//
		// }));
		// final Spinner s = (Spinner) findViewById(R.id.romanizationSystem);
		// s.setOnItemSelectedListener(new ModificationHandler());
		// AbstractActivity.setButtonActivityLauncher(this,
		// R.id.btnDownloadDictionary, DownloadDictionaryActivity.class);
	}

	/**
	 * Deletes all dictionary files.
	 */
	private void cleanup() {
		final DialogUtils utils = new DialogUtils(this);
		utils.showYesNoDialog(AedictApp.format(R.string.deleteDictionaryFiles, MiscUtils.getLength(new File(DictTypeEnum.BASE_DIR)) / 1024), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					MiscUtils.deleteDir(new File(DictTypeEnum.BASE_DIR));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				utils.showToast(R.string.data_files_removed);
			}

		});
	}
}
