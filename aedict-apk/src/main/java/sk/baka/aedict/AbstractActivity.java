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

import sk.baka.aedict.jlptquiz.QuizLaunchActivity;
import sk.baka.aedict.skip.SkipActivity;
import sk.baka.autils.AndroidUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
	public static void addActivityLauncher(final Activity context, final Menu menu, final int caption, final int icon, final Class<? extends Activity> activity) {
		final MenuItem item2 = menu.add(caption);
		item2.setIcon(icon);
		item2.setOnMenuItemClickListener(AndroidUtils.safe(context, new MenuItem.OnMenuItemClickListener() {

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
	 * @param activityToLaunch
	 *            the activity class
	 */
	public void setButtonActivityLauncher(final int buttonId, final Class<? extends Activity> activityToLaunch) {
		setButtonActivityLauncher(this, buttonId, activityToLaunch);
	}

	/**
	 * Sets given button to be an activity launcher.
	 * 
	 * @param activity
	 *            current activity
	 * @param buttonId
	 *            the button ID
	 * @param activityToLaunch
	 *            the activity class to launch
	 */
	public static void setButtonActivityLauncher(final Activity activity, final int buttonId, final Class<? extends Activity> activityToLaunch) {
		setButtonActivityLauncher(activity, (Button) activity.findViewById(buttonId), activityToLaunch);
	}

	/**
	 * Sets given button to be an activity launcher.
	 * 
	 * @param button
	 *            the button
	 * @param activityToLaunch
	 *            the activity class to launch
	 */
	public void setButtonActivityLauncher(final Button button, final Class<? extends Activity> activityToLaunch) {
		setButtonActivityLauncher(this, button, activityToLaunch);
	}

	/**
	 * Sets given button to be an activity launcher.
	 * 
	 * @param activity
	 *            current activity
	 * @param button
	 *            the button
	 * @param activityToLaunch
	 *            the activity class to launch
	 */
	public static void setButtonActivityLauncher(final Activity activity, final Button button, final Class<? extends Activity> activityToLaunch) {
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final Intent intent = new Intent(activity, activityToLaunch);
				activity.startActivity(intent);
			}
		});
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
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
		MenuItem item = menu.add(R.string.kanjiSearch);
		item.setIcon(android.R.drawable.ic_menu_search);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setItems(R.array.kanjiSearchMethod, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						final Class<? extends Activity> launch = which == 0 ? KanjiSearchRadicalActivity.class : which == 1 ? KanjiDrawActivity.class : SkipActivity.class;
						final Intent i = new Intent(activity, launch);
						activity.startActivity(i);
					}
				});
				builder.setTitle(R.string.kanjiSearchMethod);
				builder.create().show();
				return true;
			}
		});
		addActivityLauncher(activity, menu, R.string.notepad, android.R.drawable.ic_menu_agenda, NotepadActivity.class);
		addActivityLauncher(activity, menu, R.string.jlptQuiz, R.drawable.ic_menu_compose, QuizLaunchActivity.class);
		addActivityLauncher(activity, menu, R.string.configuration, android.R.drawable.ic_menu_preferences, ConfigActivity.class);
	}
}
