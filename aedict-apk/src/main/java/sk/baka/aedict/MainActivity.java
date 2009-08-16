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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Provides means to search the edict dictionary file.
 * 
 * @author Martin Vysny
 */
public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			JpUtils.initialize(getClassLoader());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		setContentView(R.layout.main);
		final Button jpSearch = (Button) findViewById(R.id.jpSearch);
		final EditText jpSearchEdit = (EditText) findViewById(R.id.jpSearchEdit);
		jpSearch.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final SearchQuery q = new SearchQuery();
				q.isJapanese = true;
				final String romaji = jpSearchEdit.getText().toString();
				q.query = new String[] { JpUtils.toHiragana(romaji),
						JpUtils.toKatakana(romaji) };
				performSearch(q);
			}

		});
		final Button engSearch = (Button) findViewById(R.id.engSearch);
		final EditText engSearchEdit = (EditText) findViewById(R.id.engSearchEdit);
		engSearch.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final SearchQuery q = new SearchQuery();
				q.isJapanese = false;
				q.query = new String[] { engSearchEdit.getText().toString() };
				performSearch(q);
			}

		});
		// check for dictionary file
		if (!DownloadEdictTask.isComplete()) {
			showYesNoDialog(
					"The EDict dictionary is missing. Do you wish to download it now?",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							new DownloadEdictTask(MainActivity.this).execute();
						}

					});
		}
	}

	private void performSearch(final SearchQuery query) {
		final Intent intent = new Intent(this, ResultActivity.class);
		query.putTo(intent);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuItem item = menu.add("Cleanup");
		item.setIcon(android.R.drawable.ic_menu_delete);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				cleanup();
				return true;
			}
		});
		return true;
	}

	private void showYesNoDialog(final String message,
			final DialogInterface.OnClickListener yesListener) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("Yes", yesListener);
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void showErrorDialog(final String message) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle("Error");
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.create().show();
	}

	private void showInfoDialog(final String message) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle("Info");
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.create().show();
	}

	private void cleanup() {
		showYesNoDialog(
				"EDict data files are currently taking up to "
						+ (MiscUtils.getLength(new File(
								DownloadEdictTask.BASE_DIR)) / 1024)
						+ "kb. Do you wish to clean the files?",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							MiscUtils.deleteDir(new File(
									DownloadEdictTask.BASE_DIR));
							showInfoDialog("Data files were removed");
						} catch (Exception ex) {
							Log.e(MainActivity.class.getSimpleName(), ex
									.toString(), ex);
							showErrorDialog("Failed to clean the files: " + ex);
						}
					}

				});
	}
}