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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Callable;

import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.Dictionary;
import sk.baka.aedict.dict.DictionaryVersions;
import sk.baka.aedict.dict.DownloaderService.UpdateDictionaries;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.util.DialogActivity;
import sk.baka.aedict.util.Iso6393Codes;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * Configures AEdict.
 * 
 * @author Martin Vysny
 */
public class ConfigActivity extends PreferenceActivity {
	/**
	 * Boolean: Always available? (Adds or removes notification icon).
	 */
	public static final String KEY_ALWAYS_AVAILABLE = "alwaysAvailable";
	/**
	 * String (the name of the {@link RomanizationEnum} enum). Which
	 * romanization system to use.
	 */
	public static final String KEY_ROMANIZATION = "romanization";
	/**
	 * Boolean. If true then Romaji will be used instead of katakana/hiragana
	 * throughout the application.
	 */
	public static final String KEY_USE_ROMAJI = "useRomaji";
	/**
	 * Launches the {@link DownloadDictionaryActivity} activity.
	 */
	public static final String KEY_DOWNLOAD_DICTIONARIES = "downloadDictionaries";
	/**
	 * Which EDICT dictionary to use for search.
	 */
	public static final String KEY_DICTIONARY_NAME = "dictionaryName";
	/**
	 * Performs the SDCard dictionary cleanup.
	 */
	public static final String KEY_SDCARD_CLEANUP = "sdcardCleanup";
	/**
	 * Resets the introduction dialogs - all dialogs will be shown again.
	 */
	public static final String KEY_RESET_INTRODUCTIONS = "resetIntroductions";
	/**
	 * Checks for dictionary updates.
	 */
	public static final String KEY_CHECK_FOR_UPDATES = "checkForUpdates";
	/**
	 * Shows the download dialog
	 */
	public static final String KEY_SHOW_DOWNLOADER = "showDownloader";
	/**
	 * Shows the "About" dialog.
	 */
	public static final String KEY_ABOUT= "about";
	/**
	 * Launches the "Donate" page.
	 */
	public static final String KEY_DONATE= "donate";
	/**
	 * Picks the example dictionary, Tanaka or Tatoeba.
	 */
	public static final String KEY_EXAMPLES_DICT = "examplesDict";
	/**
	 * Picks the Tatoeba language.
	 */
	public static final String KEY_EXAMPLES_DICT_LANG = "examplesDictLang";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.config);
		addPreferencesFromResource(R.xml.preferences);
		final ListPreference dictNames = (ListPreference) findPreference(KEY_EXAMPLES_DICT_LANG);
		final SortedMap<String, String> codes = Iso6393Codes.getSortedLangNames();
		dictNames.setEntries(codes.keySet().toArray(new CharSequence[0]));
		dictNames.setEntryValues(codes.values().toArray(new CharSequence[0]));
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		final String key = preference.getKey();
		if (key.equals(KEY_DOWNLOAD_DICTIONARIES)) {
			final Intent intent = new Intent(this, DownloadDictionaryActivity.class);
			startActivity(intent);
			return true;
		}
		if (key.equals(KEY_SDCARD_CLEANUP)) {
			cleanup();
			return true;
		}
		if (key.equals(KEY_RESET_INTRODUCTIONS)) {
			final DialogUtils utils = new DialogUtils(ConfigActivity.this);
			utils.clearInfoOccurency();
			utils.showToast(R.string.resetIntroductionsSummary);
			return true;
		}
		if (key.equals(KEY_ABOUT)) {
			AboutActivity.launch(this);
			return true;
		}
		if(key.equals(KEY_SHOW_DOWNLOADER)){
			DownloadActivity.launch(this);
			return true;
		}
		if (key.equals(KEY_DONATE)) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/aedict/#Donate")));
			return true;
		}
		if(key.equals(KEY_CHECK_FOR_UPDATES)) {
			checkForNewVersions();
			return true;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// components are now initialized in onResume phase, to refresh
		// dictionary list when a new dictionary is downloaded
		final List<String> dictionaries = new ArrayList<String>();
		for(final Dictionary d:Dictionary.listEdictInstalled()){
			dictionaries.add(d.custom==null?Dictionary.DEFAULT_DICTIONARY_NAME:d.custom);
		}
		Collections.sort(dictionaries);
		final ListPreference dictNames = (ListPreference) findPreference(KEY_DICTIONARY_NAME);
		dictNames.setEntries(dictionaries.toArray(new CharSequence[0]));
		dictNames.setEntryValues(dictionaries.toArray(new CharSequence[0]));
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

	private static class GetVersionNumbers implements Callable<Void> {
		public final Activity a;
		public GetVersionNumbers(Activity a){
			this.a=a;
		}
		public static final String KEY = "getNewDictionaryVersionNumbers";
		public Void call() throws Exception {
			final DictionaryVersions dv = new DictionaryVersions();
			for(Dictionary d: Dictionary.listInstalled()) {
				final String version = d.downloadVersion();
				dv.versions.put(d, version);
			}
			AedictApp.getConfig().setServerDictVersions(dv);
			final DictionaryVersions current = AedictApp.getConfig().getCurrentDictVersions();
			final Set<Dictionary> updatable = current.getOlderThan(dv);
			if(updatable.isEmpty()){
				new DialogActivity.Builder(a).showInfoDialog("No updates found", "No dictionary updates has been found.");
			}else{
				new DialogActivity.Builder(a).setDialogListener(new UpdateDictionaries(updatable)).showYesNoDialog(
						"The following dictionaries may be updated: " + updatable + ". Perform the update now?");
			}
			return null;
		}
	}
	private void checkForNewVersions() {
		final Toast toast = Toast.makeText(this, "Checking for updates", Toast.LENGTH_SHORT);
		toast.show();
		AedictApp.getBackground().schedule(GetVersionNumbers.KEY, new GetVersionNumbers(this));
	}
}
