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
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.Edict;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
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
	private List<DictEntry> modelCache = null;
	private ShowRomaji showRomaji;

	/**
	 * Expects {@link DictEntry} as a value. Adds given entry to the model list.
	 */
	static final String INTENTKEY_ADD_ENTRY = "addEntry";

	public static void addAndLaunch(final Context activity, DictEntry entry) {
		final Intent intent = new Intent(activity, NotepadActivity.class);
		intent.putExtra(INTENTKEY_ADD_ENTRY, entry);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notepad);
		showRomaji=new ShowRomaji(this) {
			
			@Override
			protected void show(boolean romaji) {
				((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
			}
		};
		getListView().setOnCreateContextMenuListener(AndroidUtils.safe(this, new View.OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenuInfo menuInfo) {
				final int pos = ((AdapterContextMenuInfo) menuInfo).position;
				menu.add(R.string.analyze).setOnMenuItemClickListener(AndroidUtils.safe(NotepadActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						KanjiAnalyzeActivity.launch(NotepadActivity.this, getModel().get(pos).getJapanese(), false);
						return true;
					}
				}));
				menu.add(R.string.delete).setOnMenuItemClickListener(AndroidUtils.safe(NotepadActivity.this, new MenuItem.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						getModel().remove(pos);
						onModelChanged();
						return true;
					}
				}));
				menu.add(R.string.showSod).setOnMenuItemClickListener(AndroidUtils.safe(NotepadActivity.this, new MenuItem.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						StrokeOrderActivity.launch(NotepadActivity.this, getModel().get(pos).getJapanese());
						return true;
					}
				}));
			}
		}));
		new SearchUtils(this).registerSearch(R.id.notepadExactMatch, R.id.notepadDeinflect, null, R.id.editNotepadSearch, false, R.id.btnNotepadSearch, true);
		processIntent();
	}

	private void processIntent() {
		final Intent intent = getIntent();
		if (intent.hasExtra(INTENTKEY_ADD_ENTRY)) {
			final DictEntry e = (DictEntry) intent.getSerializableExtra(INTENTKEY_ADD_ENTRY);
			getModel().add(e);
			onModelChanged();
		}
	}

	private List<DictEntry> getModel() {
		if (modelCache == null) {
			final Config cfg = AedictApp.getConfig();
			final String notepadItems = cfg.getNotepadItems();
			final String items[] = notepadItems.split("@@@@");
			modelCache = new ArrayList<DictEntry>();
			try {
				for (final String item : items) {
					if (!MiscUtils.isBlank(item)) {
						modelCache.add(DictEntry.fromExternal(item));
					}
				}
			} catch (Exception ex) {
				Log.e(NotepadActivity.class.getSimpleName(), "Notepad model parsing failed", ex);
			}
		}
		return modelCache;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setModel();
		showRomaji.onResume();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final EditText edit = (EditText) findViewById(R.id.editNotepadSearch);
		final DictEntry entry = getModel().get(position);
		final String text = edit.getText().toString();
		edit.setText(text + entry.getJapanese());
	}

	/**
	 * Persists the model to the {@link Config configuration}. Invoked after
	 * each change.
	 */
	private void onModelChanged() {
		final ListBuilder b = new ListBuilder("@@@@");
		for (final DictEntry entry : getModel()) {
			b.add(entry.toExternal());
		}
		final Config cfg = AedictApp.getConfig();
		cfg.setNotepadItems(b.toString());
		if (getListAdapter() != null) {
			// the adapter may be null if this method is invoked from onCreate()
			// method
			((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
		}
	}

	/**
	 * Sets the ListView model.
	 */
	private void setModel() {
		final RomanizationEnum romanization = AedictApp.getConfig().getRomanization();
		setListAdapter(new ArrayAdapter<DictEntry>(this, android.R.layout.simple_list_item_2, getModel()) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TwoLineListItem view = (TwoLineListItem) convertView;
				if (view == null) {
					view = (TwoLineListItem) getLayoutInflater().inflate(android.R.layout.simple_list_item_2, getListView(), false);
				}
				Edict.print(getModel().get(position), view, showRomaji.isShowingRomaji() ? romanization : null);
				return view;
			}

		});
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		final MenuItem item=menu.add(R.string.deleteAll);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item.setOnMenuItemClickListener(AndroidUtils.safe(this, new MenuItem.OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem item) {
				getModel().clear();
				onModelChanged();
				return true;
			}
		}));
		showRomaji.register(menu);
		return true;
	}
}
