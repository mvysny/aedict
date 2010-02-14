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
import java.util.Formatter;

import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.MiscUtils;
import sk.baka.autils.bind.BindToView;
import sk.baka.autils.bind.Binder;
import sk.baka.autils.bind.SharedPref;
import sk.baka.autils.bind.SharedPrefsMapper;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * The main application class.
 * 
 * @author Martin Vysny
 */
public class AedictApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
//		tests will create multiple instances of this class
//		if (instance != null) {
//			throw new IllegalStateException("Not a singleton");
//		}
		instance = this;
		DialogUtils.resError = R.string.error;
		apply(loadConfig());
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
		/**
		 * Loads default values to null fields.
		 */
		public void setDefaults() {
			if (romanization == null) {
				romanization = RomanizationEnum.Hepburn;
			}
			if (isAlwaysAvailable == null) {
				isAlwaysAvailable = false;
			}
			if (useRomaji == null) {
				useRomaji = false;
			}
			if (dictionaryName == null) {
				dictionaryName = DEFAULT_DICTIONARY_NAME;
			}
			if (notepadItems == null) {
				notepadItems = "";
			}
		}

		/**
		 * The name of the default dictionary.
		 */
		public static final String DEFAULT_DICTIONARY_NAME = "Default";
		/**
		 * Which romanization system to use. Defaults to Hepburn.
		 */
		@SharedPref(key = "romanization", removeOnNull = false)
		@BindToView(R.id.romanizationSystem)
		public RomanizationEnum romanization;
		/**
		 * If true then a notification icon is registered.
		 */
		@SharedPref(key = "isAlwaysAvailable", removeOnNull = false)
		@BindToView(R.id.cfgNotifBar)
		public Boolean isAlwaysAvailable;
		/**
		 * If true then Romaji will be used instead of katakana/hiragana
		 * throughout the application.
		 */
		@SharedPref(key = "useRomaji", removeOnNull = false)
		@BindToView(R.id.cfgUseRomaji)
		public Boolean useRomaji;
		/**
		 * The dictionary name to use. If null then the default one should be
		 * used.
		 */
		@SharedPref(key = "dictionaryName", removeOnNull = false)
		@BindToView(R.id.spinDictionaryPicker)
		public String dictionaryName;
		/**
		 * Persisted notepad items. A mixture of Japanese kanjis, hiragana and
		 * katakana, comma-separated.
		 */
		@SharedPref(key = "notepadItems", removeOnNull = false)
		public String notepadItems;
	}

	/**
	 * Loads the configuration from a shared preferences.
	 * 
	 * @return the configuration.
	 */
	public static Config loadConfig() {
		final SharedPreferences prefs = instance.getSharedPreferences("cfg", Context.MODE_PRIVATE);
		final Config result = new Config();
		new Binder().bindToBean(result, new SharedPrefsMapper(), prefs, false);
		result.setDefaults();
		return result;
	}

	/**
	 * Stores new configuration. null values are left unchanged.
	 * 
	 * @param cfg
	 *            the configuration, must not be null.
	 */
	public static void saveConfig(final Config cfg) {
		final SharedPreferences prefs = instance.getSharedPreferences("cfg", Context.MODE_PRIVATE);
		new Binder().bindFromBean(cfg, new SharedPrefsMapper(), prefs, false);
		apply(loadConfig());
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
		if (!cfg.isAlwaysAvailable) {
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
	 * Returns the dictionary location on the SD card of the EDICT dictionary..
	 * 
	 * @return absolute OS-specific location of the dictionary.
	 */
	public static String getDictionaryLoc() {
		final String dictionaryName = loadConfig().dictionaryName;
		if (dictionaryName == null || dictionaryName.equals(Config.DEFAULT_DICTIONARY_NAME)) {
			return DictTypeEnum.Edict.getDefaultDictionaryPath();
		}
		final String loc = DictTypeEnum.Edict.getDefaultDictionaryPath() + "-" + dictionaryName;
		final File f = new File(loc);
		return f.exists() && f.isDirectory() ? loc : DictTypeEnum.Edict.getDefaultDictionaryPath();
	}
}
