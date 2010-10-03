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

package sk.baka.aedict.dict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.CopyActivity;
import sk.baka.aedict.KanjiAnalyzeActivity;
import sk.baka.aedict.MainActivity;
import sk.baka.aedict.NotepadActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.ResultActivity;
import sk.baka.aedict.StrokeOrderActivity;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.util.Check;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.aedict.util.SpanStringBuilder;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.ClipboardManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Performs background search in Tanaka example dictionary and automatically
 * pushes results as Android View components.
 * 
 * @author Martin Vysny
 */
public class TanakaSearchTask extends AsyncTask<String, Void, List<DictEntry>> implements View.OnFocusChangeListener {
	private final ViewGroup vg;
	private final Activity activity;
	private List<DictEntry> exampleSentences = new ArrayList<DictEntry>();
	private final List<ViewGroup> views = new ArrayList<ViewGroup>();
	private final ShowRomaji showRomaji;
	private final String highlightTerm;

	/**
	 * Creates new searcher.
	 * 
	 * @param activity
	 *            owning activity, not null.
	 * @param vg
	 *            views with results will be placed here. not null.
	 * @param showRomaji
	 *            controls display of kana or romaji, not null.
	 * @param highlightTerm
	 *            highlight this term with blueish color in the result. not
	 *            null.
	 */
	public TanakaSearchTask(final Activity activity, final ViewGroup vg, final ShowRomaji showRomaji, final String highlightTerm) {
		Check.checkNotNull("activity", activity);
		Check.checkNotNull("vg", vg);
		Check.checkNotNull("showRomaji", showRomaji);
		Check.checkNotNull("highlightTerm", highlightTerm);
		this.activity = activity;
		this.vg = vg;
		this.showRomaji = showRomaji;
		this.highlightTerm = highlightTerm;
	}

	@Override
	protected void onPreExecute() {
		AedictApp.getDownloader().checkDic(activity, DictTypeEnum.Tanaka);
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

	/**
	 * Invoke this to update the Views values. Invoke after the romaji display
	 * has been set or cleared.
	 */
	public void updateModel() {
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
			print(i, de, view);
			if (de.isValid()) {
				view.setOnClickListener(AndroidUtils.safe(activity, new View.OnClickListener() {

					public void onClick(View v) {
						KanjiAnalyzeActivity.launch(activity, de.kanji, false);
					}
				}));
			} else {
				view.setOnClickListener(null);
			}
			view.setFocusable(de.isValid());
			view.setOnFocusChangeListener(de.isValid() ? this : null);
			if (de.isValid()) {
				view.setOnCreateContextMenuListener(AndroidUtils.safe(activity, new View.OnCreateContextMenuListener() {

					public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
						final MenuItem miAddToNotepad = menu.add(Menu.NONE, 1, 1, R.string.addToNotepad);
						miAddToNotepad.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

							public boolean onMenuItemClick(MenuItem item) {
								NotepadActivity.addAndLaunch(activity, de);
								return true;
							}
						}));
						final MenuItem miShowSOD = menu.add(Menu.NONE, 6, 6, R.string.showSod);
						miShowSOD.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

							public boolean onMenuItemClick(MenuItem item) {
								StrokeOrderActivity.launch(activity, de.getJapanese());
								return true;
							}
						}));
						final MenuItem miCopyToClipboard = menu.add(Menu.NONE, 7, 7, R.string.copyToClipboard);
						miCopyToClipboard.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

							public boolean onMenuItemClick(MenuItem item) {
								final ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
								cm.setText(de.getJapanese());
								final Toast toast = Toast.makeText(activity, AedictApp.format(R.string.copied, de.getJapanese()), Toast.LENGTH_SHORT);
								toast.show();
								return true;
							}
						}));
						final MenuItem miSearchFurther = menu.add(Menu.NONE, 8, 8, R.string.searchFurther);
						miSearchFurther.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

							public boolean onMenuItemClick(MenuItem item) {
								MainActivity.launch(activity, de.getJapanese());
								return true;
							}
						}));
						final MenuItem miAdvancedCopy = menu.add(Menu.NONE, 9, 9, R.string.advancedCopy);
						miAdvancedCopy.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

							public boolean onMenuItemClick(MenuItem item) {
								CopyActivity.launch(activity, de);
								return true;
							}
						}));
					}
				}));
			} else {
				view.setOnCreateContextMenuListener(null);
			}
		}
		while (views.size() > i) {
			vg.removeView(views.remove(i));
		}
	}

	private void print(final int num, DictEntry de, ViewGroup view) {
		if (de.isValid()) {
			final String kanjis = getKanjis(highlightTerm);
			TextView tv = (TextView) view.findViewById(R.id.kanji);
			final SpanStringBuilder sb = new SpanStringBuilder();
			sb.append(sb.newForeground(0xFF777777), "(" + num + ") ");
			final SpannableString str = new SpannableString(de.getJapanese());
			for (int i = de.getJapanese().indexOf(kanjis); i >= 0; i = de.getJapanese().indexOf(kanjis, i + 1)) {
				str.setSpan(sb.newForeground(0xFF7da5e7), i, i + kanjis.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			sb.append(str);
			tv.setText(sb);
			tv = (TextView) view.findViewById(R.id.romaji);
			if (MiscUtils.isBlank(de.reading) || MiscUtils.isBlank(de.kanji)) {
				tv.setVisibility(View.GONE);
			} else {
				tv.setVisibility(View.VISIBLE);
				tv.setText(showRomaji.romanize(de.reading));
			}
		}
		TextView tv = (TextView) view.findViewById(R.id.english);
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