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
package sk.baka.aedict.dict;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.dict.Dictionary.DictionaryVersions;
import sk.baka.autils.MiscUtils;

/**
 * Handles dictionary updates.
 * @author Martin Vysny
 */
public class Updater {
	private static class GetVersionNumbers implements Callable<Void> {
		public static final String KEY = "getNewDictionaryVersionNumbers";
		@Override
		public Void call() throws Exception {
			final DictionaryVersions dv = new DictionaryVersions();
			for(Dictionary d: Dictionary.listInstalled()) {
				final String version = getVersion(d.getVersionFileURL());
				dv.versions.put(d, version);
			}
			AedictApp.getConfig().setServerDictVersions(dv);
			return null;
		}
		private String getVersion(String dictionaryURL) throws IOException {
			return new String(MiscUtils.readFully(new URL(dictionaryURL+".version").openStream()), "UTF-8");
		}
	}
}
