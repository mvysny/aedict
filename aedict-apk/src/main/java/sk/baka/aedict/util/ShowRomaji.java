/**
 * 
 */
package sk.baka.aedict.util;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public abstract class ShowRomaji {
	private boolean isShowingRomaji;

	public ShowRomaji() {
		isShowingRomaji = AedictApp.getConfig().isUseRomaji();
	}

	public void register(final Menu menu) {
		final MenuItem item = menu.add(isShowingRomaji ? R.string.show_kana : R.string.show_romaji);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				isShowingRomaji = !isShowingRomaji;
				show(isShowingRomaji);
				return true;
			}
		});
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
		if (isShowingRomaji != AedictApp.getConfig().isUseRomaji()) {
			isShowingRomaji = !isShowingRomaji;
			show(isShowingRomaji);
		}
	}
}