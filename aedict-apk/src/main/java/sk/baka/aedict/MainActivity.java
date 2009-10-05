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

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StatFs;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

/**
 * Provides means to search the edict dictionary file.
 * 
 * @author Martin Vysny
 */
public class MainActivity extends AbstractActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final SearchUtils utils = new SearchUtils(this);
		utils.registerSearch(R.id.jpExactMatch, R.id.jpSearchEdit, false, R.id.jpSearch, true);
		utils.registerSearch(R.id.engExactMatch, R.id.engSearchEdit, false, R.id.engSearch, false);
		// check for dictionary file
		if (!DownloadEdictTask.isComplete()) {
			final StatFs stats = new StatFs("/sdcard");
			final long free = ((long) stats.getBlockSize()) * stats.getAvailableBlocks();
			final StringBuilder msg = new StringBuilder(getString(R.string.edict_missing_download));
			if (free < 20 * 1000 * 1000) {
				msg.append('\n');
				msg.append(AedictApp.format(R.string.warning_less_than_20mb_free, free / 1024));
			}
			showYesNoDialog(msg.toString(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					new DownloadEdictTask(MainActivity.this).execute();
				}
			});
		}
		final CheckBox cfgNotifBar = (CheckBox) findViewById(R.id.cfgNotifBar);
		cfgNotifBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				showHideNotification();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuItem item = menu.add(R.string.cleanup);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				cleanup();
				return true;
			}
		});
		addActivityLauncher(menu, R.string.aboutCaption, android.R.drawable.ic_menu_info_details, AboutActivity.class);
		addActivityLauncher(menu, R.string.showkanaTable, R.drawable.kanamenuitem, KanaTableActivity.class);
		return true;
	}

	private void showYesNoDialog(final String message, final DialogInterface.OnClickListener yesListener) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.yes, yesListener);
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void showErrorDialog(final String message) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(R.string.error);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.create().show();
	}

	private void showInfoDialog(final int messageRes) {
		showInfoDialog(getString(messageRes));
	}

	private void showInfoDialog(final String message) {
		final Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM, 0, 0);
		toast.show();
	}

	private void cleanup() {
		showYesNoDialog(AedictApp.format(R.string.deleteEdictFiles, MiscUtils.getLength(new File(DownloadEdictTask.BASE_DIR)) / 1024), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				try {
					MiscUtils.deleteDir(new File(DownloadEdictTask.BASE_DIR));
					showInfoDialog(R.string.data_files_removed);
				} catch (Exception ex) {
					Log.e(MainActivity.class.getSimpleName(), ex.toString(), ex);
					showErrorDialog(getString(R.string.failed_to_clean_files) + ex);
				}
			}

		});
	}

	private static final int NOTIFICATION_ID = 1;

	private void showHideNotification() {
		final CheckBox cb = (CheckBox) findViewById(R.id.cfgNotifBar);
		final boolean isNotification = cb.isChecked();
		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (!isNotification) {
			nm.cancel(NOTIFICATION_ID);
			return;
		}
		final Notification notification = new Notification(R.drawable.notification, null, 0);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		notification.setLatestEventInfo(this, "Aedict", "A japanese dictionary", contentIntent);
		notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		nm.notify(NOTIFICATION_ID, notification);
	}
}