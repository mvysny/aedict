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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import sk.baka.aedict.util.Check;
import sk.baka.autils.MiscUtils;

/**
 * Represents a dictionary installed locally on the SD Card.
 *
 * @author Martin Vysny
 */
public class Dictionary implements Serializable {
	private static final long serialVersionUID = 1L;

    /**
     * The name of the default dictionary.
     */
    public static final String DEFAULT_DICTIONARY_NAME = "Default";
	/**
	 * Creates new dictionary object.
	 *
	 * @param dte
	 *            the dictionary type, must not be null.
	 * @param custom
	 *            (only for EDICT) - if not null, this denotes a custom edict
	 *            dictionary (e.g. German EDICT dictionary).
	 */
	public Dictionary(DictTypeEnum dte, String custom) {
		Check.checkNotNull("dte", dte);
		if (DEFAULT_DICTIONARY_NAME.equals(custom)) {
			custom = null;
		}
		if (custom != null && MiscUtils.isBlank(custom)) {
			throw new IllegalArgumentException("The dictionary name is blank");
		}
		if (custom != null) {
			if (dte != DictTypeEnum.Edict) {
				throw new IllegalArgumentException(
						"Custom dictionaries are not supported for " + dte);
			}
		}
		this.dte = dte;
		this.custom = custom;
	}

	public final DictTypeEnum dte;
	public final String custom;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((custom == null) ? 0 : custom.hashCode());
		result = prime * result + ((dte == null) ? 0 : dte.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dictionary other = (Dictionary) obj;
		if (custom == null) {
			if (other.custom != null)
				return false;
		} else if (!custom.equals(other.custom))
			return false;
		if (dte != other.dte)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Dictionary[" + dte + (custom != null ? ", " + custom : "")
				+ "]";
	}

	/**
	 * Returns the dictionary location on the SD card of this dictionary.
	 *
	 * @return absolute OS-specific location of the dictionary.
	 */
	public File getDictionaryLocation() {
		String path = dte.getDefaultDictionaryPath();
		if (custom != null) {
			path = path + "-" + custom;
		}
		return new File(path);
	}

	/**
	 * Checks if this dictionary has all necessary files present on the SD Card.
	 *
	 * @return true if the dictionary files are present, false otherwise.
	 */
	public boolean exists() {
		final File dir = getDictionaryLocation();
		return dir.exists() && dir.isDirectory() && dir.list().length != 0;
	}

	/**
	 * Deletes this dictionary from the SD Card.
	 */
	public void delete() throws IOException {
		MiscUtils.deleteDir(getDictionaryLocation());
	}

	/**
	 * Lists all dictionaries currently installed on the SD Card.
	 *
	 * @return list of all installed dictionaries, never null, may be empty.
	 */
	public static Set<Dictionary> listInstalled() {
		final Set<Dictionary> result = new HashSet<Dictionary>();
		for (DictTypeEnum dte : DictTypeEnum.values()) {
			final Dictionary d = new Dictionary(dte, null);
			if (d.exists()) {
				result.add(d);
			}
		}
		result.addAll(listEdictInstalled());
		return result;
	}
	/**
	 * Lists all EDICT dictionaries currently installed on the SD Card.
	 *
	 * @return list of all Edict installed dictionaries, never null, may be empty.
	 */
	public static Set<Dictionary> listEdictInstalled() {
		final Set<Dictionary> result = new HashSet<Dictionary>();
		final File aedict = new File("/sdcard/aedict");
		if (aedict.exists() && aedict.isDirectory()) {
			final String[] dictionaries = aedict.list(new FilenameFilter() {

				public boolean accept(File dir, String filename) {
					return filename.toLowerCase().startsWith("index");
				}
			});
			for (final String dict : dictionaries) {
				if (isNonEdictDirectory(dict)) {
					continue;
				}
				if (dict.equals("index")) {
					result.add(new Dictionary(DictTypeEnum.Edict, null));
				} else {
					final String dictName = dict.substring("index-".length());
					result.add(new Dictionary(DictTypeEnum.Edict, dictName));
				}
			}
		}
		return result;
	}

	private static boolean isNonEdictDirectory(final String name) {
		for (DictTypeEnum e : DictTypeEnum.values()) {
			if (e == DictTypeEnum.Edict) {
				continue;
			}
			if (e.getDefaultDictionaryLoc().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public String getVersionFileURL() {
		String downloadURL = dte.getDownloadSite().toString();
		if (custom != null) {
                    // strip the ending .zip
                    downloadURL = downloadURL.substring(0, downloadURL.length() - 4);
			downloadURL += "-" + custom + ".zip";
		}
		return downloadURL + ".version";
	}

	public String downloadVersion() throws IOException {
		return new String(MiscUtils.readFully(new URL(getVersionFileURL()).openStream()), "UTF-8");
	}

	public String toExternal() {
		String result = dte.name();
		if (custom != null) {
			result += "@@@@" + custom;
		}
		return result;
	}

	public static Dictionary fromExternal(String external) {
		try {
			final String[] parsed = external.split("@@@@");
			final DictTypeEnum dte = DictTypeEnum.valueOf(parsed[0]);
			return new Dictionary(dte, parsed.length == 1 ? null : parsed[1]);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to parse '" + external + "'", ex);
		}
	}

	public String getName() {
		String result = dte.name();
		if (custom != null) {
			result = result + "-" + custom;
		}
		return result;
	}

	public URL getDownloadSite() {
		if(custom==null){
			return dte.getDownloadSite();
		}
		try {
			return new URL(DictTypeEnum.DICT_BASE_LOCATION_URL+custom+".zip");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
}
