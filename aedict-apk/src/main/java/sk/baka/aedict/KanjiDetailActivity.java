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
import java.util.List;

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.KanjidicEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.util.Constants;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.aedict.util.SpanStringBuilder;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows a detail of a single Kanji character.
 * 
 * @author Martin Vysny
 */
public class KanjiDetailActivity extends AbstractActivity {
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
		new SearchClickListener(this,entry.kanji, true).registerTo(kanji);
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
			new SearchClickListener(this, query, isJapanese).registerTo(tv);
			tv.setTextSize(textSize);
			p.addView(tv);
		}
	}

	public static class SearchClickListener implements View.OnClickListener, View.OnFocusChangeListener, View.OnCreateContextMenuListener {
		private final boolean isJapanese;
		private final String searchFor;
		private final Activity activity;

		public SearchClickListener(final Activity activity, final String searchFor, final boolean isJapanese) {
			this.activity = activity;
			this.searchFor = searchFor;
			this.isJapanese = isJapanese;
		}

		public SearchClickListener registerTo(final View view) {
			view.setOnClickListener(this);
			view.setFocusable(true);
			view.setOnFocusChangeListener(this);
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

		public void onFocusChange(View v, boolean hasFocus) {
			v.setBackgroundColor(hasFocus ? 0xCFFF8c00 : 0);
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

	public static class TanakaSearchTask extends AsyncTask<String, Void, List<DictEntry>> implements View.OnFocusChangeListener {
		private final ViewGroup vg;
		private final Activity activity;
		private List<DictEntry> exampleSentences = new ArrayList<DictEntry>();
		private final List<ViewGroup> views = new ArrayList<ViewGroup>();
		private final ShowRomaji showRomaji;
		private final String highlightTerm;

		public TanakaSearchTask(final Activity activity, final ViewGroup vg, final ShowRomaji showRomaji, final String highlightTerm) {
			this.activity = activity;
			this.vg = vg;
			this.showRomaji = showRomaji;
			this.highlightTerm = highlightTerm;
		}

		@Override
		protected void onPreExecute() {
			new SearchUtils(activity).checkDic(DictTypeEnum.Tanaka);
			activity.setProgressBarIndeterminate(true);
			activity.setProgressBarIndeterminateVisibility(true);
			final TextView tv = (TextView) activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, vg, false);
			tv.setText(R.string.searching);
			vg.addView(tv);
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
			exampleSentences = result;
			if (exampleSentences.isEmpty()) {
				exampleSentences = Collections.singletonList(DictEntry.newErrorMsg(activity.getString(R.string.no_results)));
			}
			vg.removeAllViews();
			updateModel();
		}

		public void updateModel() {
			final RomanizationEnum romanization = AedictApp.getConfig().getRomanization();
			int i = 0;
			for (final DictEntry de : exampleSentences) {
				ViewGroup view;
				if (views.size() <= i) {
					view = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.tanakaexample_list_item, vg, false);
					views.add(view);
					vg.addView(view);
				} else {
					view = views.get(i);
				}
				i++;
				print(i, de, view, showRomaji.isShowingRomaji() ? romanization : null);
				if (de.isValid()) {
					view.setOnClickListener(AndroidUtils.safe(activity, new View.OnClickListener() {

						public void onClick(View v) {
							KanjiAnalyzeActivity.launch(activity, de.kanji, false);
						}
					}));
				} else {
					view.setOnClickListener(null);
				}
				view.setFocusable(true);
				view.setOnFocusChangeListener(this);
			}
			while (views.size() > i) {
				vg.removeView(views.remove(i));
			}
		}

		private void print(final int num, DictEntry de, ViewGroup view, RomanizationEnum r) {
			final String kanjis = getKanjis(highlightTerm);
			TextView tv = (TextView) view.findViewById(R.id.kanji);
			final SpanStringBuilder sb = new SpanStringBuilder();
			sb.append(sb.newForeground(0xFF777777), "(" + num + ") ");
			final SpannableString str = new SpannableString(de.kanji);
			for (int i = de.kanji.indexOf(kanjis); i >= 0; i = de.kanji.indexOf(kanjis, i + 1)) {
				str.setSpan(sb.newForeground(0xFF7da5e7), i, i + kanjis.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			sb.append(str);
			tv.setText(sb);
			tv = (TextView) view.findViewById(R.id.romaji);
			if (MiscUtils.isBlank(de.reading)) {
				tv.setVisibility(View.GONE);
			} else {
				tv.setVisibility(View.VISIBLE);
				tv.setText(RomanizationEnum.toRomaji(de.reading, r));
			}
			tv = (TextView) view.findViewById(R.id.english);
			tv.setText(de.english);
		}

		private String getKanjis(final String jp) {
			int start = 0;
			for (; start < jp.length() && !KanjiUtils.isKanji(jp.charAt(start)); start++) {
			}
			int end = jp.length() - 1;
			for (; end >= 0 && !KanjiUtils.isKanji(jp.charAt(end)); end--) {
			}
			if (start <= end) {
				return jp.substring(start, end + 1);
			}
			return jp;
		}

		public void onFocusChange(View v, boolean hasFocus) {
			v.setBackgroundColor(hasFocus ? 0xCFFF8c00 : 0);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		showRomaji.register(menu);
		return true;
	}

	private TanakaSearchTask tanakaSearchTask;

	@Override
	protected void onResume() {
		super.onResume();
		showRomaji.onResume();
		if (tanakaSearchTask == null) {
			tanakaSearchTask = new TanakaSearchTask(this, (ViewGroup) findViewById(R.id.tanakaExamples), showRomaji, entry.kanji);
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
