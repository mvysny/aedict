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

import sk.baka.aedict.skip.Skip1Activity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * Properly initializes the application.
 * 
 * @author Martin Vysny
 */
public abstract class AbstractActivity extends Activity {
	/**
	 * Adds a menu item to given menu which launches given activity.
	 * 
	 * @param menu
	 *            the menu
	 * @param caption
	 *            the menu item caption
	 * @param icon
	 *            the menu item icon
	 * @param activity
	 *            the activity to launch.
	 */
	protected final void addActivityLauncher(final Menu menu, final int caption, final int icon, final Class<? extends Activity> activity) {
		addActivityLauncher(this, menu, caption, icon, activity);
	}

	/**
	 * Adds a menu item to given menu which launches given activity.
	 * 
	 * @param context
	 *            the owning context
	 * @param menu
	 *            the menu
	 * @param caption
	 *            the menu item caption
	 * @param icon
	 *            the menu item icon
	 * @param activity
	 *            the activity to launch.
	 */
	public static void addActivityLauncher(final Context context, final Menu menu, final int caption, final int icon, final Class<? extends Activity> activity) {
		final MenuItem item2 = menu.add(caption);
		item2.setIcon(icon);
		item2.setOnMenuItemClickListener(AedictApp.safe(new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				final Intent intent = new Intent(context, activity);
				context.startActivity(intent);
				return true;
			}
		}));
	}

	/**
	 * Sets given button to be an activity launcher.
	 * 
	 * @param buttonId
	 *            the button ID
	 * @param activity
	 *            the activity class
	 */
	public void setButtonActivityLauncher(final int buttonId, final Class<? extends Activity> activity) {
		setButtonActivityLauncher((Button) findViewById(buttonId), activity);
	}

	/**
	 * Sets given button to be an activity launcher.
	 * 
	 * @param button
	 *            the button
	 * @param activity
	 *            the activity class
	 */
	public void setButtonActivityLauncher(final Button button, final Class<? extends Activity> activity) {
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final Intent intent = new Intent(AbstractActivity.this, activity);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		addMenuItems(this, menu);
		return true;
	}

	/**
	 * Adds default menu items.
	 * 
	 * @param activity
	 *            the activity
	 * @param menu
	 *            the menu
	 */
	public static void addMenuItems(final Activity activity, final Menu menu) {
		addActivityLauncher(activity, menu, R.string.showkanaTable, R.drawable.kanamenuitem, KanaTableActivity.class);
		addActivityLauncher(activity, menu, R.string.kanjiDrawLookup, R.drawable.ic_menu_compose, KanjiDrawActivity.class);
		addActivityLauncher(activity, menu, R.string.kanjiRadicalLookup, android.R.drawable.ic_menu_search, KanjiSearchRadicalActivity.class);
		addActivityLauncher(activity, menu, R.string.skipLookup, R.drawable.skipmenuitem, Skip1Activity.class);
	}
}
