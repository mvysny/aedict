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

import java.util.List;

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.Edict;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.Deinflections;
import sk.baka.aedict.kanji.Deinflections.Deinflection;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.kanji.VerbDeinflection;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.DialogUtils;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TwoLineListItem;

/**
 * Provides means to search the edict dictionary file.
 * 
 * @author Martin Vysny
 */
public class MainActivity extends ListActivity {
	private ShowRomaji showRomaji;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		showRomaji = new ShowRomaji(this) {

			@Override
			protected void show(boolean romaji) {
				((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
			}
		};
		findViewById(R.id.advanced).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final View g = findViewById(R.id.advancedPanel);
				g.setVisibility(g.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
			}
		});
		// check for dictionary file and download it if it is missing.
		AedictApp.getDownloader().checkDic(MainActivity.this, DictTypeEnum.Edict);
		if (!AedictApp.isInstrumentation) {
			new DialogUtils(this).showInfoOnce(AedictApp.getVersion(), AedictApp.format(R.string.whatsNew, AedictApp.getVersion()), getString(R.string.whatsNewText));
		}
		((TextView) findViewById(R.id.aedict)).setText("Aedict " + AedictApp.getVersion());
		getListView().setOnCreateContextMenuListener(AndroidUtils.safe(this, new View.OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				final int position = ((AdapterContextMenuInfo) menuInfo).position;
				final DictEntry ee = getModel().get(position);
				final MenuItem miAddToNotepad = menu.add(Menu.NONE, 1, 1, R.string.addToNotepad);
				miAddToNotepad.setOnMenuItemClickListener(AndroidUtils.safe(MainActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						NotepadActivity.addAndLaunch(MainActivity.this, ee);
						return true;
					}
				}));
				final MenuItem miShowSOD = menu.add(Menu.NONE, 6, 6, R.string.showSod);
				miShowSOD.setOnMenuItemClickListener(AndroidUtils.safe(MainActivity.this, new MenuItem.OnMenuItemClickListener() {

					public boolean onMenuItemClick(MenuItem item) {
						StrokeOrderActivity.launch(MainActivity.this, ee.getJapanese());
						return true;
					}
				}));
				if (EdictEntry.fromEntry(ee).isVerb()) {
					final MenuItem miShowConjugations = menu.add(Menu.NONE, 7, 7, R.string.showConjugations);
					miShowConjugations.setOnMenuItemClickListener(AndroidUtils.safe(MainActivity.this, new MenuItem.OnMenuItemClickListener() {

						public boolean onMenuItemClick(MenuItem item) {
							VerbInflectionActivity.launch(MainActivity.this, EdictEntry.fromEntry(ee));
							return true;
						}
					}));
				}
			}
		}));
		final String prefillTerm = getIntent().getStringExtra(INTENTKEY_PREFILL_SEARCH_FIELD);
		if (prefillTerm != null) {
			((TextView) findViewById(R.id.searchEdit)).setText(prefillTerm);
		}
		// setup search controls
		setupSearchControls();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		showRomaji.register(menu);
		AbstractActivity.addMenuItems(this, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		modelCache = null;
		setModel();
		showRomaji.onResume();
		findViewById(R.id.intro).setVisibility(getModel().isEmpty() ? View.VISIBLE : View.GONE);
		findViewById(R.id.recentlyViewed).setVisibility(getModel().isEmpty() ? View.GONE : View.VISIBLE);
	}

	private List<DictEntry> modelCache = null;

	private List<DictEntry> getModel() {
		if (modelCache == null) {
			modelCache = AedictApp.getConfig().getRecentlyViewed();
		}
		return modelCache;
	}

	public static void recentlyViewed(final DictEntry entry) {
		final List<DictEntry> entries = AedictApp.getConfig().getRecentlyViewed();
		while (entries.size() > 15) {
			entries.remove(entries.size() - 1);
		}
		entries.remove(entry);
		entries.add(0, entry);
		AedictApp.getConfig().setRecentlyViewed(entries);
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final DictEntry e = getModel().get(position);
		if (!e.isValid()) {
			return;
		}
		EdictEntryDetailActivity.launch(this, EdictEntry.fromEntry(e));
	}

	static final String INTENTKEY_PREFILL_SEARCH_FIELD = "prefillSearchField";

	public static void launch(Activity activity, String term) {
		final Intent i = new Intent(activity, MainActivity.class);
		if (term != null) {
			i.putExtra(INTENTKEY_PREFILL_SEARCH_FIELD, term);
		}
		activity.startActivity(i);
	}

	private void setupSearchControls() {
		findViewById(R.id.englishSearch).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				search(false);
			}
		});
		findViewById(R.id.jpSearch).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				search(true);
			}
		});
		final CheckBox deinflect = (CheckBox) findViewById(R.id.jpDeinflectVerbs);
		deinflect.setOnCheckedChangeListener(new ComponentUpdater());
		final CheckBox tanaka = (CheckBox) findViewById(R.id.searchExamples);
		tanaka.setOnCheckedChangeListener(new ComponentUpdater());
		final CheckBox translate = (CheckBox) findViewById(R.id.translate);
		translate.setOnCheckedChangeListener(new ComponentUpdater());
	}

	private void search(final boolean isJapanese) {
		final boolean isAdvanced = findViewById(R.id.advancedPanel).getVisibility() != View.GONE;
		final boolean isTranslate = ((CheckBox) findViewById(R.id.translate)).isChecked();
		final String text = ((TextView) findViewById(R.id.searchEdit)).getText().toString().trim();
		if (text.length() == 0) {
			return;
		}
		if (isAdvanced && isTranslate && isJapanese) {
			KanjiAnalyzeActivity.launch(this, text.trim(), true);
			return;
		}
		final boolean isDeinflect = ((CheckBox) findViewById(R.id.jpDeinflectVerbs)).isChecked();
		final RomanizationEnum r = AedictApp.getConfig().getRomanization();
		if (isAdvanced && isDeinflect && isJapanese) {
			final Deinflections q = VerbDeinflection.searchJpDeinflected(text, r);
			performSearch(q.query, q.deinflections);
			return;
		}
		final boolean isTanaka = ((CheckBox) findViewById(R.id.searchExamples)).isChecked();
		if (isAdvanced && isTanaka) {
			final SearchQuery q = new SearchQuery(DictTypeEnum.Tanaka);
			q.isJapanese = isJapanese;
			if (isJapanese) {
				final String conv = KanjiUtils.halfwidthToKatakana(text);
				q.query = new String[] { r.toKatakana(conv), r.toHiragana(conv) };
			} else {
				q.query = new String[] { text };
			}
			q.matcher = MatcherEnum.Substring;
			performSearch(q, null);
			return;
		}
		final MatcherEnum matcher = isAdvanced ? MatcherEnum.values()[((Spinner) findViewById(R.id.matcher)).getSelectedItemPosition()] : MatcherEnum.Substring;
		final SearchQuery q = isJapanese ? SearchQuery.searchJpRomaji(text, r, matcher) : SearchQuery.searchForEnglish(text, matcher == MatcherEnum.Exact);
		performSearch(q, null);
	}

	private class ComponentUpdater implements OnCheckedChangeListener {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			final Activity activity = MainActivity.this;
			final Spinner matcher = (Spinner) activity.findViewById(R.id.matcher);
			final CheckBox deinflect = (CheckBox) activity.findViewById(R.id.jpDeinflectVerbs);
			final CheckBox tanaka = (CheckBox) activity.findViewById(R.id.searchExamples);
			final CheckBox translate = (CheckBox) activity.findViewById(R.id.translate);
			if (buttonView.getId() == R.id.jpDeinflectVerbs && isChecked) {
				matcher.setSelection(MatcherEnum.Exact.ordinal());
				tanaka.setChecked(false);
				translate.setChecked(false);
			} else if (buttonView.getId() == R.id.searchExamples && isChecked) {
				matcher.setSelection(MatcherEnum.Substring.ordinal());
				deinflect.setChecked(false);
				translate.setChecked(false);
			} else if (buttonView.getId() == R.id.translate && isChecked) {
				deinflect.setChecked(false);
				tanaka.setChecked(false);
			}
			matcher.setEnabled(!deinflect.isChecked() && !tanaka.isChecked() && !translate.isChecked());
			findViewById(R.id.englishSearch).setEnabled(!translate.isChecked());
		}
	}

	private void performSearch(final SearchQuery query, final List<Deinflection> deinflections) {
		if (!AedictApp.getDownloader().checkDic(this, query.dictType)) {
			// the dictionary is not yet available. An activity was popped up,
			// which offers dictionary download. Nothing to do here, just do
			// nothing.
			return;
		}
		ResultActivity.launch(this, query, deinflections);
	}
}