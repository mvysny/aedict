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
import sk.baka.aedict.ResultActivity;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import android.app.Activity;
import android.content.Context;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;

/**
 * Updates a view so that it is able to get focus, and a search for given string
 * is performed when the view is clicked. To update the view use the
 * {@link #registerTo(View)} method.
 * 
 * @author Martin Vysny
 */
public class SearchClickListener implements View.OnClickListener, View.OnCreateContextMenuListener {
	private final boolean isJapanese;
	private final String searchFor;
	private final Activity activity;

	/**
	 * Creates the searcher.
	 * 
	 * @param activity
	 *            owning activity, must not be null.
	 * @param searchFor
	 *            search for
	 * @param isJapanese
	 *            true if <code>searchFor</code> is a japanese term, false if it
	 *            is an English term.
	 */
	public SearchClickListener(final Activity activity, final String searchFor, final boolean isJapanese) {
		Check.checkNotNull("activity", activity);
		Check.checkNotNull("searchFor", searchFor);
		this.activity = activity;
		this.searchFor = searchFor;
		this.isJapanese = isJapanese;
	}

	/**
	 * Registers the object to given view.
	 * 
	 * @param view
	 *            the view
	 * @return this
	 */
	public SearchClickListener registerTo(final View view) {
		view.setOnClickListener(this);
		new FocusVisual().registerTo(view);
		view.setOnCreateContextMenuListener(this);
		return this;
	}

	public void onClick(View v) {
		final SearchQuery q = new SearchQuery(DictTypeEnum.Edict);
		q.isJapanese = isJapanese;
		q.query = new String[] { searchFor };
		q.matcher = MatcherEnum.Substring;
		ResultActivity.launch(v.getContext(), q);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final MenuItem miCopyToClipboard = menu.add(Menu.NONE, 1, 1, R.string.copyToClipboard);
		miCopyToClipboard.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				final ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
				cm.setText(searchFor);
				final Toast toast = Toast.makeText(activity, AedictApp.format(R.string.copied, searchFor), Toast.LENGTH_SHORT);
				toast.show();
				return true;
			}
		});

	}
}