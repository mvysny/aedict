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

package sk.baka.aedict.util;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Helps with the romanization process.
 * 
 * @author Martin Vysny
 */
public abstract class ShowRomaji {
	private boolean isShowingRomaji;
	private RomanizationEnum romanization;
	private final Activity activity;

	public ShowRomaji(final Activity activity) {
		this.activity = activity;
		isShowingRomaji = AedictApp.getConfig().isUseRomaji();
		romanization = AedictApp.getConfig().getRomanization();
	}

	public void register(final Menu menu) {
		final MenuItem item = menu.add(isShowingRomaji ? R.string.show_kana : R.string.show_romaji);
		item.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				isShowingRomaji = !isShowingRomaji;
				show(isShowingRomaji);
				return true;
			}
		}));
		item.setIcon(isShowingRomaji ? R.drawable.showkana : R.drawable.showromaji);
	}

	public boolean isShowingRomaji() {
		return isShowingRomaji;
	}

	protected abstract void show(final boolean romaji);

	/**
	 * Should be invoked from {@link Activity#onResume()}: checks if user
	 * changed the SHOW_ROMAJI flag and reacts accordingly.
	 */
	public void onResume() {
		final RomanizationEnum newR = AedictApp.getConfig().getRomanization();
		final boolean newS = AedictApp.getConfig().isUseRomaji();
		if (isShowingRomaji != newS || newR != romanization) {
			isShowingRomaji = newS;
			romanization = newR;
			show(isShowingRomaji);
		}
	}

	public String romanize(final String kana) {
		if (isShowingRomaji) {
			return (romanization == null ? RomanizationEnum.Hepburn : romanization).toRomaji(kana);
		}
		return kana;
	}

	public String getJapanese(final DictEntry e) {
		Check.checkTrue("entry not valid", e.isValid());
		if (MiscUtils.isBlank(e.kanji)) {
			return romanize(e.reading);
		}
		return e.kanji;
	}
}