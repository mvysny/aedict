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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.DownloaderService;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The main application class.
 * 
 * @author Martin Vysny
 */
public class AedictApp extends Application implements OnSharedPreferenceChangeListener {

	@Override
	public void onCreate() {
		super.onCreate();
		// tests will create multiple instances of this class
		// if (instance != null) {
		// throw new IllegalStateException("Not a singleton");
		// }
		instance = this;
		DialogUtils.resError = R.string.error;
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		apply(new Config(this));
		startService(new Intent(instance, DownloaderService.class));
		bindService(new Intent(instance, DownloaderService.class), new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder service) {
				ds = ((DownloaderService.LocalBinder) service).getService();
			}

			public void onServiceDisconnected(ComponentName className) {
				ds = null;
			}
		}, Context.BIND_AUTO_CREATE);
	}

	private static volatile DownloaderService ds;

	public static DownloaderService getDownloader() {
		if (ds == null) {
			throw new IllegalStateException("Downloader is not yet started");
		}
		return ds;
	}

	private static AedictApp instance;

	/**
	 * Returns the application instance.
	 * 
	 * @return the instance.
	 */
	public static AedictApp getApp() {
		if (instance == null) {
			throw new IllegalStateException("Not yet initialized");
		}
		return instance;
	}

	/**
	 * Formats given string using {@link Formatter} and returns it.
	 * 
	 * @param resId
	 *            the string id
	 * @param args
	 *            the parameters.
	 * @return a formatted string.
	 */
	public static String format(final int resId, Object... args) {
		final String formatStr = getStr(resId);
		return new Formatter().format(formatStr, args).toString();
	}

	/**
	 * Returns string associated with given resource ID.
	 * 
	 * @param resId
	 *            the string id
	 * @return a formatted string.
	 */
	public static String getStr(final int resId) {
		return getApp().getString(resId);
	}

	/**
	 * The Ambient version.
	 */
	private static String version;

	/**
	 * Returns the Ambient version.
	 * 
	 * @return the version string or "unknown" if the version is not available.
	 */
	public static String getVersion() {
		if (version != null) {
			return version;
		}
		final InputStream in = getApp().getClassLoader().getResourceAsStream("version");
		if (in != null) {
			try {
				version = new String(MiscUtils.readFully(in), "UTF-8");
			} catch (Exception ex) {
				Log.e(AedictApp.class.getSimpleName(), "Failed to get version", ex);
				version = "unknown";
			} finally {
				MiscUtils.closeQuietly(in);
			}
		}
		return version;
	}

	/**
	 * The configuration.
	 * 
	 * @author Martin Vysny
	 */
	public static class Config {
		private final SharedPreferences prefs;

		/**
		 * Constructs new config instance.
		 * 
		 * @param context
		 *            load default shared preferences from this context.
		 */
		public Config(final Context context) {
			this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
		}

		/**
		 * The name of the default dictionary.
		 */
		public static final String DEFAULT_DICTIONARY_NAME = "Default";

		/**
		 * Which romanization system to use. Defaults to Hepburn.
		 * 
		 * @return the romanization system to use. Never null.
		 */
		public RomanizationEnum getRomanization() {
			return RomanizationEnum.valueOf(prefs.getString(ConfigActivity.KEY_ROMANIZATION, null));
		}

		/**
		 * If true then a notification icon is registered.
		 * 
		 * @return true if the application is always available.
		 */
		public boolean isAlwaysAvailable() {
			return prefs.getBoolean(ConfigActivity.KEY_ALWAYS_AVAILABLE, false);
		}

		/**
		 * If true then Romaji will be used instead of katakana/hiragana
		 * throughout the application.
		 * 
		 * @return true if Romaji will be displayed.
		 */
		public boolean isUseRomaji() {
			return prefs.getBoolean(ConfigActivity.KEY_USE_ROMAJI, false);
		}

		/**
		 * If true then Romaji will be used instead of katakana/hiragana
		 * throughout the application.
		 * 
		 * @param useRomaji
		 *            true if Romaji will be displayed.
		 */
		public void setUseRomaji(boolean useRomaji) {
			commit(prefs.edit().putBoolean(ConfigActivity.KEY_USE_ROMAJI, useRomaji));
		}

		/**
		 * The dictionary name to use. If null then the default one should be
		 * used. Applies to EDICT dictionaries only.
		 * 
		 * @return the dictionary name, never null. Returns
		 *         {@value Config#DEFAULT_DICTIONARY_NAME} for the default Edict
		 *         file.
		 */
		public String getDictionaryName() {
			return prefs.getString(ConfigActivity.KEY_DICTIONARY_NAME, null);
		}

		/**
		 * The preference key of the "notepad items" configuration item.
		 */
		public static final String KEY_NOTEPAD_ITEMS = "notepadItems2";

		/**
		 * Persisted notepad DictEntries.
		 * 
		 * @param category
		 *            the category, 0 is the default one.
		 * @return the notepad items, never null.
		 */
		public List<DictEntry> getNotepadItems(final int category) {
			try {
				return DictEntry.fromExternalList(prefs.getString(KEY_NOTEPAD_ITEMS + (category == 0 ? "" : "" + category), ""));
			} catch (Exception ex) {
				// this may happen: earlier aedict builds stored the notepad
				// items
				// in a different format
				Log.e(AedictApp.class.getSimpleName(), "Notepad model parsing failed", ex);
				return new ArrayList<DictEntry>();
			}
		}

		/**
		 * Notepad DictEntries.
		 * 
		 * @param category
		 *            the category, 0 is the default one.
		 * @param notepadItems
		 *            the new notepad items, never null.
		 */
		public void setNotepadItems(final int category, final List<? extends DictEntry> notepadItems) {
			commit(prefs.edit().putString(KEY_NOTEPAD_ITEMS + (category == 0 ? "" : "" + category), DictEntry.toExternalList(notepadItems)));
		}

		public static final String KEY_NOTEPAD_CATEGORIES = "notepadCategories";

		public static final int MAX_CATEGORIES = 4;

		/**
		 * Returns a list of notepad categories.
		 * 
		 * @return a list of notepad category names, not null, may be empty.
		 */
		public List<String> getNotepadCategories() {
			final List<String> result = new ArrayList<String>(MAX_CATEGORIES);
			for (final String cat : prefs.getString(KEY_NOTEPAD_CATEGORIES, "").split("@@@@")) {
				if (!MiscUtils.isBlank(cat)) {
					result.add(cat);
				}
			}
			return result;
		}

		/**
		 * A list of notepad categories.
		 * 
		 * @param categories
		 *            a list of notepad category names, not null, may be empty.
		 */
		public void setNotepadCategories(final List<String> categories) {
			final ListBuilder b = new ListBuilder("@@@@");
			for (final String s : categories) {
				b.add(s);
			}
			commit(prefs.edit().putString(KEY_NOTEPAD_CATEGORIES, b.toString()));
		}

		/**
		 * The preference key of the "notepad items" configuration item.
		 */
		public static final String KEY_RECENTLY_VIEWED_ITEMS = "recentlyViewed";

		/**
		 * Recently viewed DictEntries.
		 * 
		 * @return the notepad items, never null.
		 */
		public List<DictEntry> getRecentlyViewed() {
			return DictEntry.fromExternalList(prefs.getString(KEY_RECENTLY_VIEWED_ITEMS, ""));
		}

		/**
		 * Recently viewed DictEntries.
		 * 
		 * @param notepadItems
		 *            the new notepad items, never null.
		 */
		public void setRecentlyViewed(final List<? extends DictEntry> notepadItems) {
			commit(prefs.edit().putString(KEY_RECENTLY_VIEWED_ITEMS, DictEntry.toExternalList(notepadItems)));
		}

		private void commit(final Editor ed) {
			if (!ed.commit()) {
				throw new IllegalStateException("Failed to commit new SharedPreferences value");
			}
		}

		/**
		 * True if the results should be sorted (the default), false otherwise.
		 * See http://code.google.com/p/aedict/issues/detail?id=56 for details.
		 */
		public static final String KEY_SORT = "sort";

		/**
		 * True if the results should be sorted (the default), false otherwise.
		 * See http://code.google.com/p/aedict/issues/detail?id=56 for details.
		 * 
		 * @param sorted
		 *            sort
		 */
		public void setSorted(final boolean sorted) {
			commit(prefs.edit().putBoolean(KEY_SORT, sorted));
		}

		/**
		 * True if the results should be sorted (the default), false otherwise.
		 * See http://code.google.com/p/aedict/issues/detail?id=56 for details.
		 * 
		 * @return sort
		 */
		public boolean isSorted() {
			return prefs.getBoolean(KEY_SORT, true);
		}

		/**
		 * Returns the dictionary location on the SD card of the EDICT
		 * dictionary..
		 * 
		 * @return absolute OS-specific location of the dictionary.
		 */
		public String getDictionaryLoc() {
			final String dictionaryName = getDictionaryName();
			if (dictionaryName == null || dictionaryName.equals(Config.DEFAULT_DICTIONARY_NAME)) {
				return DictTypeEnum.Edict.getDefaultDictionaryPath();
			}
			final String loc = DictTypeEnum.Edict.getDefaultDictionaryPath() + "-" + dictionaryName;
			final File f = new File(loc);
			return f.exists() && f.isDirectory() ? loc : DictTypeEnum.Edict.getDefaultDictionaryPath();
		}
	}

	/**
	 * Loads the configuration from a shared preferences.
	 * 
	 * @return the configuration.
	 */
	public static Config getConfig() {
		return new Config(instance);
	}

	private static final int NOTIFICATION_ID = 1;

	/**
	 * Applies values from given configuration file, e.g. removes or adds the
	 * notification icon etc.
	 * 
	 * @param cfg
	 *            non-null configuration
	 */
	private static void apply(final Config cfg) {
		final NotificationManager nm = (NotificationManager) instance.getSystemService(Context.NOTIFICATION_SERVICE);
		if (!cfg.isAlwaysAvailable()) {
			nm.cancel(NOTIFICATION_ID);
		} else {
			final Notification notification = new Notification(R.drawable.notification, null, 0);
			final PendingIntent contentIntent = PendingIntent.getActivity(instance, 0, new Intent(instance, MainActivity.class), 0);
			notification.setLatestEventInfo(instance, "Aedict", "A japanese dictionary", contentIntent);
			notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			nm.notify(NOTIFICATION_ID, notification);
		}
	}

	/**
	 * If true then the instrumentation (testing) is in progress.
	 */
	public static boolean isInstrumentation = false;

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(ConfigActivity.KEY_ALWAYS_AVAILABLE)) {
			apply(getConfig());
		}
	}
}
