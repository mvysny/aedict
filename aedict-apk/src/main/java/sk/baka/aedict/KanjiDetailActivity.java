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

import java.util.Collections;
import java.util.List;

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.Edict;
import sk.baka.aedict.dict.KanjidicEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.util.Constants;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.DialogUtils;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

/**
 * Shows a detail of a single Kanji character.
 * 
 * @author Martin Vysny
 */
public class KanjiDetailActivity extends ListActivity {
	static final String INTENTKEY_KANJIDIC_ENTRY = "entry";

	public static void launch(final Context activity, final KanjidicEntry entry) {
		final Intent intent = new Intent(activity, KanjiDetailActivity.class);
		intent.putExtra(INTENTKEY_KANJIDIC_ENTRY, entry);
		activity.startActivity(intent);
	}

	private ShowRomaji showRomaji;
	private KanjidicEntry entry;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.kanji_detail);
		showRomaji = new ShowRomaji(this) {

			@Override
			protected void show(boolean romaji) {
				updateContent();
				tanakaSearchTask.updateModel();
			}
		};
		entry = (KanjidicEntry) getIntent().getSerializableExtra(INTENTKEY_KANJIDIC_ENTRY);
		final TextView kanji = (TextView) findViewById(R.id.kanji);
		kanji.setText(entry.kanji);
		final SearchClickListener l = new SearchClickListener(entry.kanji, true);
		kanji.setOnClickListener(l);
		kanji.setFocusable(true);
		kanji.setOnFocusChangeListener(l);
		((TextView) findViewById(R.id.stroke)).setText(Integer.toString(entry.strokes));
		((TextView) findViewById(R.id.grade)).setText(entry.grade == null ? "-" : entry.grade.toString());
		((TextView) findViewById(R.id.skip)).setText(entry.skip);
		final Integer jlpt = entry.getJlpt();
		((TextView) findViewById(R.id.jlpt)).setText(jlpt == null ? "-" : jlpt.toString());
		final SearchUtils utils = new SearchUtils(this);
		utils.setupCopyButton(R.id.copy, R.id.kanji);
		((Button) findViewById(R.id.showStrokeOrder)).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				StrokeOrderActivity.launch(KanjiDetailActivity.this, entry.kanji);
			}
		});
		((Button) findViewById(R.id.addToNotepad)).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				NotepadActivity.addAndLaunch(KanjiDetailActivity.this, entry);
			}
		});
		addTextViews(R.id.english, entry.getEnglish(), false, 15);
		updateContent();
		// display hint
		if (!AedictApp.isInstrumentation) {
			new DialogUtils(this).showInfoOnce(Constants.INFOONCE_CLICKABLE_NOTE, R.string.note, R.string.clickableNote);
		}
	}

	private void updateContent() {
		// compute ONYOMI, KUNYOMI, NAMAE and ENGLISH
		addTextViews(R.id.onyomi, entry.getOnyomi(), true, 20);
		addTextViews(R.id.kunyomi, entry.getKunyomi(), true, 20);
		addTextViews(R.id.namae, entry.getNamae(), true, 20);
	}

	private void addTextViews(final int parent, final List<String> items, final boolean isJapanese, float textSize) {
		final RomanizationEnum romanization = AedictApp.getConfig().getRomanization();
		final ViewGroup p = (ViewGroup) findViewById(parent);
		p.removeAllViews();
		if (items.isEmpty()) {
			p.setVisibility(View.GONE);
			return;
		}
		for (int i = 0; i < items.size(); i++) {
			final String item = items.get(i);
			final String sitem = KanjidicEntry.removeSplits(item);
			final TextView tv = new TextView(p.getContext());
			String text = item + (i == items.size() - 1 ? "" : ", ");
			if (isJapanese && showRomaji.isShowingRomaji()) {
				text = romanization.toRomaji(text);
			}
			tv.setText(text);
			final String query = KanjiUtils.isKatakana(sitem.charAt(0)) ? RomanizationEnum.NihonShiki.toHiragana(RomanizationEnum.NihonShiki.toRomaji(sitem)) : sitem;
			final SearchClickListener l = new SearchClickListener(query, isJapanese);
			tv.setOnClickListener(l);
			tv.setTextSize(textSize);
			tv.setFocusable(true);
			tv.setOnFocusChangeListener(l);
			p.addView(tv);
		}
	}

	public static class SearchClickListener implements View.OnClickListener, View.OnFocusChangeListener {
		private final boolean isJapanese;
		private final String searchFor;

		public SearchClickListener(final String searchFor, final boolean isJapanese) {
			this.searchFor = searchFor;
			this.isJapanese = isJapanese;
		}

		public void onClick(View v) {
			final SearchQuery q = new SearchQuery(DictTypeEnum.Edict);
			q.isJapanese = isJapanese;
			q.query = new String[] { searchFor };
			q.matcher = MatcherEnum.Substring;
			ResultActivity.launch(v.getContext(), q);
		}

		public void onFocusChange(View v, boolean hasFocus) {
			v.setBackgroundColor(hasFocus ? 0xCFFF8c00 : 0);
		}
	}

	public static class TanakaSearchTask extends AsyncTask<String, Void, List<DictEntry>> {
		private final ListView listView;
		private final Activity activity;
		private List<DictEntry> exampleSentences;
		private final ShowRomaji showRomaji;

		public TanakaSearchTask(final Activity activity, final ListView listView, final ShowRomaji showRomaji) {
			this.activity = activity;
			this.listView = listView;
			this.showRomaji = showRomaji;
		}

		@Override
		protected void onPreExecute() {
			activity.setProgressBarIndeterminate(true);
			activity.setProgressBarIndeterminateVisibility(true);
			listView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, new String[] { activity.getString(R.string.searching) }));
		}

		@Override
		protected List<DictEntry> doInBackground(String... params) {
			final SearchQuery query = new SearchQuery(DictTypeEnum.Tanaka);
			query.isJapanese = true;
			query.matcher = MatcherEnum.Substring;
			query.query = new String[] { params[0] };
			try {
				return LuceneSearch.singleSearch(query, null, true);
			} catch (Exception e) {
				Log.e(TanakaSearchTask.class.getSimpleName(), "Failed to search in Tanaka", e);
				return Collections.singletonList(DictEntry.newErrorMsg(e));
			}
		}

		@Override
		protected void onPostExecute(List<DictEntry> result) {
			activity.setProgressBarIndeterminateVisibility(false);
			final RomanizationEnum romanization = AedictApp.getConfig().getRomanization();
			exampleSentences = result;
			if (exampleSentences.isEmpty()) {
				exampleSentences = Collections.singletonList(DictEntry.newErrorMsg(activity.getString(R.string.no_results)));
			}
			listView.setAdapter(new ArrayAdapter<DictEntry>(activity, android.R.layout.simple_list_item_2, exampleSentences) {

				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					TwoLineListItem view = (TwoLineListItem) convertView;
					if (view == null) {
						view = (TwoLineListItem) activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_2, listView, false);
					}
					Edict.print(exampleSentences.get(position), view, showRomaji.isShowingRomaji() ? romanization : null);
					return view;
				}

			});
		}

		public void updateModel() {
			((ArrayAdapter<?>) listView.getAdapter()).notifyDataSetChanged();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		AbstractActivity.addMenuItems(this, menu);
		showRomaji.register(menu);
		return true;
	}

	private TanakaSearchTask tanakaSearchTask;

	@Override
	protected void onResume() {
		super.onResume();
		showRomaji.onResume();
		if (tanakaSearchTask == null) {
			tanakaSearchTask = new TanakaSearchTask(this, getListView(), showRomaji);
			tanakaSearchTask.execute(entry.kanji);
		}
	}

	@Override
	protected void onStop() {
		if (tanakaSearchTask.cancel(true)) {
			tanakaSearchTask = null;
		}
		super.onStop();
	}

}
