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
	private Boolean isShowingRomaji;

	public ShowRomaji() {
		this(null);
	}
	public ShowRomaji(final Boolean isShowingRomaji) {
		this.isShowingRomaji = isShowingRomaji;
	}

	public boolean resolveShowRomaji() {
		return isShowingRomaji == null ? AedictApp.getConfig().isUseRomaji() : isShowingRomaji;
	}
	
	public void showRomaji(final Boolean isShowingRomaji) {
		this.isShowingRomaji = isShowingRomaji;
		show(resolveShowRomaji());
	}
	 
	public void register(final Activity a, final Menu menu) {
		final MenuItem item = menu.add(resolveShowRomaji() ? R.string.show_kana : R.string.show_romaji);
		item.setOnMenuItemClickListener(AndroidUtils.safe(a, new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				isShowingRomaji = !resolveShowRomaji();
				show(isShowingRomaji);
				return true;
			}
		}));
		item.setIcon(resolveShowRomaji() ? R.drawable.showkana : R.drawable.showromaji);
	}

	public Boolean isShowingRomaji() {
		return isShowingRomaji;
	}

	protected abstract void show(final boolean romaji);

	/**
	 * Should be invoked from {@link Activity#onResume()}: checks if user
	 * changed the SHOW_ROMAJI flag and reacts accordingly.
	 */
	public void onResume() {
		show(resolveShowRomaji());
	}

	private RomanizationEnum getRomanization() {
		RomanizationEnum result = AedictApp.getConfig().getRomanization();
		if(result == null){
			return RomanizationEnum.Hepburn;
		}
		return result;
	}
	
	public String romanize(final String kana) {
		if (resolveShowRomaji()) {
			return getRomanization().toRomaji(kana);
		}
		return kana;
	}

	public static String romanizeIfRequired(final String kana) {
		final boolean isShowingRomaji = AedictApp.getConfig().isUseRomaji();
		final RomanizationEnum romanization = AedictApp.getConfig().getRomanization();
		return isShowingRomaji ? romanization.toRomaji(kana) : kana;
	}
	
	public String getJapanese(final DictEntry e) {
		Check.checkTrue("entry not valid", e.isValid());
		if (MiscUtils.isBlank(e.kanji)) {
			return romanize(e.reading);
		}
		return e.kanji;
	}
}