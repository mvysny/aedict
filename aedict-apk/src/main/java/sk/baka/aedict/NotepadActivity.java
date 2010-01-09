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

import java.util.ArrayList;
import java.util.List;

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DownloadDictTask;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.DialogAsyncTask;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TwoLineListItem;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * A simple notepad activity, a simple kanji persistent storage. Allows for
 * adding/removing of kanji characters and lookup for a kanji combination.
 * 
 * @author Martin Vysny
 */
public class NotepadActivity extends ListActivity {
	/**
	 * The cached model (a list of edict entries as only the japanese text is
	 * persisted).
	 */
	private static List<EdictEntry> modelCache = null;
	/**
	 * true if romaji is shown instead of katakana/hiragana.
	 */
	private boolean isShowingRomaji;

	/**
	 * Expects {@link EdictEntry} as a value. Adds given entry to the model
	 * list.
	 */
	public static final String INTENTKEY_ADD_ENTRY = "addEntry";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notepad);
		final Config cfg = AedictApp.loadConfig();
		isShowingRomaji = cfg.useRomaji;
		getListView().setOnCreateContextMenuListener(AndroidUtils.safe(this, new View.OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenuInfo menuInfo) {
				menu.add(isShowingRomaji ? R.string.show_kana : R.string.show_romaji).setOnMenuItemClickListener(AndroidUtils.safe(NotepadActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						isShowingRomaji = !isShowingRomaji;
						((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
						return true;
					}
				}));
				final int pos = ((AdapterContextMenuInfo) menuInfo).position;
				menu.add(R.string.delete).setOnMenuItemClickListener(AndroidUtils.safe(NotepadActivity.this, new MenuItem.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						modelCache.remove(pos);
						onModelChanged();
						return true;
					}
				}));
				menu.add(R.string.deleteAll).setOnMenuItemClickListener(AndroidUtils.safe(NotepadActivity.this, new MenuItem.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						modelCache.clear();
						onModelChanged();
						return true;
					}
				}));
			}
		}));
		new SearchUtils(this).registerSearch(R.id.notepadExactMatch, R.id.editNotepadSearch, false, R.id.btnNotepadSearch, true);
		processIntent();
	}

	private void processIntent() {
		final Intent intent = getIntent();
		if (intent.hasExtra(INTENTKEY_ADD_ENTRY)) {
			final EdictEntry e = (EdictEntry) intent.getSerializableExtra(INTENTKEY_ADD_ENTRY);
			if (modelCache == null) {
				final Config cfg = AedictApp.loadConfig();
				if (MiscUtils.isBlank(cfg.notepadItems)) {
					cfg.notepadItems = e.getJapanese();
				} else {
					cfg.notepadItems = cfg.notepadItems + "," + e.getJapanese();
				}
				AedictApp.saveConfig(cfg);
			} else {
				modelCache.add(e);
				onModelChanged();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateModel();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final EditText edit = (EditText) findViewById(R.id.editNotepadSearch);
		final EdictEntry entry = modelCache.get(position);
		final String text = edit.getText().toString();
		edit.setText(text + entry.getJapanese());
	}

	/**
	 * Persists the model to the {@link Config configuration}. Invoked after
	 * each change.
	 */
	private void onModelChanged() {
		final ListBuilder b = new ListBuilder(",");
		for (final EdictEntry entry : modelCache) {
			b.add(entry.getJapanese());
		}
		final Config cfg = new Config();
		cfg.notepadItems = b.toString();
		AedictApp.saveConfig(cfg);
		if (getListAdapter() != null) {
			// the adapter may be null if this method is invoked from onCreate() method
			((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
		}
	}

	private void updateModel() {
		if (modelCache == null) {
			new ComputeCacheTask().execute();
		} else {
			setModel();
		}
	}

	/**
	 * Sets the ListView model. Expects valid {@link #modelCache model cache}.
	 */
	private void setModel() {
		final Config cfg = AedictApp.loadConfig();
		setListAdapter(new ArrayAdapter<EdictEntry>(this, android.R.layout.simple_list_item_2, modelCache) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TwoLineListItem view = (TwoLineListItem) convertView;
				if (view == null) {
					view = (TwoLineListItem) getLayoutInflater().inflate(android.R.layout.simple_list_item_2, getListView(), false);
				}
				modelCache.get(position).print(view, isShowingRomaji ? cfg.romanization : null);
				return view;
			}

		});
	}

	private class ComputeCacheTask extends DialogAsyncTask<Void, List<EdictEntry>> {

		protected ComputeCacheTask() {
			super(NotepadActivity.this);
		}

		@Override
		protected void cleanupAfterError() {
			// nothing to do
		}

		@Override
		protected void onTaskSucceeded(List<EdictEntry> result) {
			modelCache = result;
			setModel();
		}

		@Override
		protected List<EdictEntry> protectedDoInBackground(Void... params) throws Exception {
			final Config cfg = AedictApp.loadConfig();
			final String[] items = MiscUtils.isBlank(cfg.notepadItems) ? new String[0] : cfg.notepadItems.split("\\,");
			final List<EdictEntry> result = new ArrayList<EdictEntry>(items.length);
			// always use the EDICT dictionary instead of user-selected dictionary
			final LuceneSearch lsEdict = new LuceneSearch(false, DownloadDictTask.LUCENE_INDEX);
			try {
				for (int i = 0; i < items.length; i++) {
					final String item = items[i].trim();
					onProgressUpdate(new Progress(null, i, items.length));
					if (isCancelled()) {
						return null;
					}
					final SearchQuery q = SearchQuery.searchForJapanese(item, true);
					EdictEntry ee = null;
					final List<String> matches = lsEdict.search(q);
					if (matches.size() > 0) {
						ee = EdictEntry.tryParseEdict(matches.get(0));
					}
					if (ee == null) {
						// no luck. Just add the item
						ee = new EdictEntry(item, "", "");
					}
					result.add(ee);
				}
				return result;
			} finally {
				MiscUtils.closeQuietly(lsEdict);
			}
		}
	}
}
