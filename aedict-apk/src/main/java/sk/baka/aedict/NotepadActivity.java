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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.Edict;
import sk.baka.aedict.jlptquiz.QuizActivity;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.util.DictEntryListActions;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.TwoLineListItem;

/**
 * A simple notepad activity, a simple kanji persistent storage. Allows for
 * adding/removing of kanji characters and lookup for a kanji combination.
 * 
 * @author Martin Vysny
 */
public class NotepadActivity extends Activity implements TabContentFactory {
	/**
	 * The cached model (a list of edict entries as only the japanese text is
	 * persisted).
	 */
	private final Map<Integer, List<DictEntry>> modelCache = new HashMap<Integer, List<DictEntry>>();
	private ShowRomaji showRomaji;

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		showRomaji.loadState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		showRomaji.saveState(outState);
	}

	/**
	 * Expects {@link DictEntry} as a value. Adds given entry to the model list.
	 */
	static final String INTENTKEY_ADD_ENTRY = "addEntry";
	static final String INTENTKEY_CATEGORY = "category";

	public static void addAndLaunch(final Context activity, final DictEntry entry, final int category) {
		final Intent intent = new Intent(activity, NotepadActivity.class);
		intent.putExtra(INTENTKEY_ADD_ENTRY, entry);
		intent.putExtra(INTENTKEY_CATEGORY, category);
		activity.startActivity(intent);
	}

	public static void addAndLaunch(final Activity activity, final DictEntry entry) {
		final List<String> notepadCategories = AedictApp.getConfig().getNotepadCategories();
		if (notepadCategories.size() <= 1) {
			addAndLaunch(activity, entry, 0);
			return;
		}
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setItems(notepadCategories.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				addAndLaunch(activity, entry, which);
			}
		});
		builder.setTitle(R.string.selectCategory);
		builder.create().show();
	}

	private TabHost getTabHost() {
		return (TabHost) findViewById(R.id.tabs);
	}

	private int getCategoryCount() {
		final int result = AedictApp.getConfig().getNotepadCategories().size();
		if (result > 1) {
			return result;
		}
		return 1;
	}

	/**
	 * Returns currently selected category tab.
	 * @return currently selected tab, 0 if no tabs are displayed.
	 */
	private int getCurrentCategory() {
		if (AedictApp.getConfig().getNotepadCategories().size() > 0) {
			return getTabHost().getCurrentTab();
		}
		return 0;
	}

	private final Map<Integer, ListView> tabContents = new HashMap<Integer, ListView>();

	public ListView getListView(final int category) {
		if (AedictApp.getConfig().getNotepadCategories().size() > 0) {
			return tabContents.get(category);
		}
		return (ListView) findViewById(android.R.id.list);
	}

	private void initializeListView(final ListView lv, final int category) {
		final RomanizationEnum romanization = AedictApp.getConfig().getRomanization();
		lv.setAdapter(new ArrayAdapter<DictEntry>(this, android.R.layout.simple_list_item_2, getModel(category)) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TwoLineListItem view = (TwoLineListItem) convertView;
				if (view == null) {
					view = (TwoLineListItem) getLayoutInflater().inflate(android.R.layout.simple_list_item_2, getListView(category), false);
				}
				Edict.print(getModel(category).get(position), view, showRomaji.resolveShowRomaji() ? romanization : null);
				return view;
			}

		});
		new DictEntryListActions(this, false, true, true) {

			@Override
			protected void addCustomItems(ContextMenu menu, DictEntry entry,
					final int itemIndex) {
				if (AedictApp.getConfig().getNotepadCategories().size() > 1) {
					menu.add(0, 20, 20, R.string.moveToCategory).setOnMenuItemClickListener(AndroidUtils.safe(NotepadActivity.this, new MenuItem.OnMenuItemClickListener() {
						public boolean onMenuItemClick(MenuItem item) {
							final List<String> notepadCategories = AedictApp.getConfig().getNotepadCategories();
							notepadCategories.remove(category);
							final AlertDialog.Builder builder = new AlertDialog.Builder(NotepadActivity.this);
							builder.setItems(notepadCategories.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									final int target = which < category ? which : which + 1;
									final DictEntry e = getModel(category).remove(itemIndex);
									getModel(target).add(0, e);
									onModelChanged(category);
									onModelChanged(target);
								}
							});
							builder.setTitle(R.string.selectCategory);
							builder.create().show();
							return true;
						}
					}));
				}
			}

			@Override
			protected void onDelete(int itemIndex) {
				getModel(category).remove(itemIndex);
				onModelChanged(category);
			}

			@Override
			protected void onDeleteAll() {
				final int category = getCurrentCategory();
				getModel(category).clear();
				onModelChanged(category);
			}
			
		}.register(lv);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				final EditText edit = (EditText) findViewById(R.id.editNotepadSearch);
				final DictEntry entry = getModel(category).get(position);
				final String text = edit.getText().toString();
				edit.setText(text + entry.getJapanese());
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notepad);
		showRomaji = new ShowRomaji() {

			@Override
			protected void show(boolean romaji) {
				for (int i = 0; i < getCategoryCount(); i++) {
					final ListView lv = getListView(i);
					if (lv != null) {
						final ArrayAdapter<?> adapter = (ArrayAdapter<?>) lv.getAdapter();
						adapter.notifyDataSetChanged();
					}
				}
			}
		};
		initializeListView((ListView) findViewById(android.R.id.list), 0);
		findViewById(R.id.btnNotepadSearch).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				MainActivity.launch(NotepadActivity.this, ((TextView)findViewById(R.id.editNotepadSearch)).getText().toString().trim());
			}
		});
		final TabHost tabs = getTabHost();
		tabs.setup();
		updateTabs();
		processIntent();
	}

	private void processIntent() {
		final Intent intent = getIntent();
		if (intent.hasExtra(INTENTKEY_ADD_ENTRY)) {
			final DictEntry e = (DictEntry) intent.getSerializableExtra(INTENTKEY_ADD_ENTRY);
			final int category = intent.getIntExtra(INTENTKEY_CATEGORY, 0);
			getModel(category).add(e);
			final Config cfg = AedictApp.getConfig();
			cfg.setNotepadItems(category, getModel(category));
		}
	}

	/**
	 * Returns the contents of the category, loading it as required.
	 * @param category the category number.
	 * @return category items, never null, may be empty.
	 */
	private List<DictEntry> getModel(final int category) {
		List<DictEntry> model = modelCache.get(category);
		if (model == null) {
			model = AedictApp.getConfig().getNotepadItems(category);
			modelCache.put(category, model);
		}
		return model;
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateTabs();
		showRomaji.onResume();
	}

	/**
	 * Persists the model to the {@link Config configuration}. Invoked after
	 * each change.
	 */
	private void onModelChanged(final int category) {
		final Config cfg = AedictApp.getConfig();
		cfg.setNotepadItems(category, getModel(category));
		final ArrayAdapter<?> adapter = (ArrayAdapter<?>) getListView(category).getAdapter();
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		showRomaji.register(this, menu);
		final MenuItem item = menu.add(0, 1, 1, R.string.deleteAll);
		item.setIcon(android.R.drawable.ic_menu_delete);
		item.setOnMenuItemClickListener(AndroidUtils.safe(this, new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				final int category = getCurrentCategory();
				getModel(category).clear();
				onModelChanged(category);
				return true;
			}
		}));
		if (AedictApp.getConfig().getNotepadCategories().size() < Config.MAX_CATEGORIES) {
			final MenuItem addCategory = menu.add(0, 2, 2, R.string.addCategory);
			addCategory.setIcon(android.R.drawable.ic_menu_add);
			addCategory.setOnMenuItemClickListener(AndroidUtils.safe(this, new MenuItem.OnMenuItemClickListener() {

				public boolean onMenuItemClick(MenuItem item) {
					final List<String> categories = AedictApp.getConfig().getNotepadCategories();
					categories.add("new");
					AedictApp.getConfig().setNotepadCategories(categories);
					final int category = categories.size() - 1;
					if (category != 0) {
						getModel(category).clear();
					}
					updateTabs();
					return true;
				}

			}));
		}
		if (!AedictApp.getConfig().getNotepadCategories().isEmpty()) {
			final MenuItem deleteCategory = menu.add(0, 3, 3, R.string.deleteCategory);
			deleteCategory.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			deleteCategory.setOnMenuItemClickListener(AndroidUtils.safe(this, new MenuItem.OnMenuItemClickListener() {

				public boolean onMenuItemClick(MenuItem item) {
					final int category = getCurrentCategory();
					// Fucking TabHost will throw NullPointerException if we
					// dare to remove his sweet fucking current tab, thank you
					// very much. NEVER REMOVE ALL TABS FROM THE STUPID TABHOST
					getTabHost().setCurrentTab(0);
					final List<String> categories = AedictApp.getConfig().getNotepadCategories();
					if (categories.size() == 1) {
						// a special case - delete the category but not its
						// items
						categories.clear();
						AedictApp.getConfig().setNotepadCategories(categories);
					} else {
						categories.remove(category);
						AedictApp.getConfig().setNotepadCategories(categories);
						AedictApp.getConfig().setNotepadItems(category, new ArrayList<DictEntry>());
						for (int i = category; i < categories.size(); i++) {
							AedictApp.getConfig().setNotepadItems(i, AedictApp.getConfig().getNotepadItems(i + 1));
						}
						modelCache.clear();
					}
					updateTabs();
					return true;
				}
			}));
			final MenuItem renameCategory = menu.add(0, 4, 4, R.string.renameCategory);
			renameCategory.setIcon(android.R.drawable.ic_menu_edit);
			renameCategory.setOnMenuItemClickListener(AndroidUtils.safe(this, new MenuItem.OnMenuItemClickListener() {

				public boolean onMenuItemClick(MenuItem item) {
					final AlertDialog.Builder builder = new AlertDialog.Builder(NotepadActivity.this);
					final EditText tv = new EditText(NotepadActivity.this);
					builder.setView(tv);
					builder.setTitle(R.string.renameCategory);
					builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							final String newName = tv.getText().toString();
							if (MiscUtils.isBlank(newName)) {
								return;
							}
							final int current = getCurrentCategory();
							getTabHost().setCurrentTab(0);
							final List<String> categories = AedictApp.getConfig().getNotepadCategories();
							categories.set(current, newName);
							AedictApp.getConfig().setNotepadCategories(categories);
							updateTabs();
						}
					});
					builder.create().show();
					return true;
				}
			}));
		}
		if (!getAllEntries().isEmpty()) {
			final MenuItem quiz = menu.add(0, 5, 5, R.string.quiz);
			quiz.setIcon(R.drawable.ic_menu_compose);
			quiz.setOnMenuItemClickListener(AndroidUtils.safe(this, new MenuItem.OnMenuItemClickListener() {

				public boolean onMenuItemClick(MenuItem item) {
					List<DictEntry> all = getAllEntries();
					Collections.shuffle(all);
					all = new ArrayList<DictEntry>(all.subList(0, Math.min(20, all.size())));
					QuizActivity.launch(NotepadActivity.this, all);
					return true;
				}
			}));
		}
		return true;
	}

	/**
	 * Returns entries from all categories.
	 * @return a list of entries, never null, may be empty.
	 */
	private List<DictEntry> getAllEntries() {
		final List<DictEntry> result = new ArrayList<DictEntry>();
		for (int i = 0; i < getCategoryCount(); i++) {
			result.addAll(AedictApp.getConfig().getNotepadItems(i));
		}
		return result;
	}

	public View createTabContent(String tag) {
		final int category = Integer.parseInt(tag);
		if (category < 0 || category >= Config.MAX_CATEGORIES) {
			throw new IllegalArgumentException("Invalid category value: "
					+ category);
		}
		final ListView lv = new ListView(this);
		lv.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		initializeListView(lv, category);
		tabContents.put(category, lv);
		return lv;
	}

	/**
	 * Updates all tabs by removing them and re-creating them anew.
	 */
	private void updateTabs() {
		getTabHost().setCurrentTab(0);
		final TabHost tabs = getTabHost();
		tabs.clearAllTabs();
		final List<String> categories = AedictApp.getConfig().getNotepadCategories();
		findViewById(android.R.id.list).setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
		tabs.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);
		if (categories.isEmpty()) {
			// add a single tab to the TabHost otherwise it will throw
			// NullPointerException later on. It is amazing how much TabHost
			// fucking sucks.
			getTabHost().addTab(getTabHost().newTabSpec("0").setIndicator("0").setContent(this));
			initializeListView((ListView) findViewById(android.R.id.list), 0);
		}
		tabContents.clear();
		int i = 0;
		for (final String cat : categories) {
			final TabHost.TabSpec newTab = getTabHost().newTabSpec(Integer.toString(i++)).setIndicator(cat).setContent(this);
			getTabHost().addTab(newTab);
		}
	}
}
