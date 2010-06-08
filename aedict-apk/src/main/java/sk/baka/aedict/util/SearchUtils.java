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
import sk.baka.aedict.KanjiAnalyzeActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.ResultActivity;
import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.autils.AndroidUtils;
import android.app.Activity;
import android.content.Context;
import android.text.ClipboardManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;

/**
 * Contains utility methods for searching with Lucene.
 * 
 * @author Martin Vysny
 */
public final class SearchUtils {
	private final Activity activity;

	/**
	 * Creates new utility class instance.
	 * 
	 * @param activity
	 *            owning activity, must not be null.
	 */
	public SearchUtils(final Activity activity) {
		this.activity = activity;
	}

	/**
	 * Performs search for a japanese word or expression.
	 * 
	 * @param romaji
	 *            word spelling. This string is converted to both hiragana and
	 *            katakana, then the EDict search is performed.
	 * @param isExact
	 *            if true then only exact matches are returned.
	 * @param isDeinflect
	 *            if true then a verb deinflection is attempted before the
	 *            search.
	 */
	private void searchForJapan(final String romaji, final boolean isExact, final boolean isDeinflect, final boolean isSearchInExamples) {
		final Config cfg = AedictApp.getConfig();
		final SearchQuery q = SearchQuery.searchForRomaji(romaji, cfg.getRomanization(), isExact, isDeinflect, isSearchInExamples);
		performSearch(q);
	}

	/**
	 * Performs EDICT/TANAKA search for an english word or expression.
	 * 
	 * @param text
	 *            the text to search for.
	 * @param isExact
	 *            if true then only exact matches are returned.
	 * @param inExamples
	 *            if true then the Tanaka dictionary is polled.
	 */
	private void searchForEnglish(final String text, final boolean isExact, final boolean inExamples) {
		final SearchQuery q = new SearchQuery(inExamples ? DictTypeEnum.Tanaka : DictTypeEnum.Edict);
		q.isJapanese = false;
		q.query = new String[] { text };
		q.matcher = isExact ? MatcherEnum.Exact : MatcherEnum.Substring;
		performSearch(q);
	}

	private void performSearch(final SearchQuery query) {
		if (!AedictApp.getDownloader().checkDic(activity, query.dictType)) {
			// the dictionary is not yet available. An activity was popped up,
			// which offers dictionary download. Nothing to do here, just do
			// nothing.
			return;
		}
		ResultActivity.launch(activity, query);
	}

	/**
	 * Registers search functionality to a standardized set of three components:
	 * the "IsExact" check box, the search query edit box and the "Search"
	 * button.
	 * 
	 * @param isExactCheckBox
	 *            the "IsExact" check box resource id. If null then an exact
	 *            search will always be performed.
	 * @param deinflectCheckBox
	 *            the "deinflect" check box reference. If null then no
	 *            deinflection attempt will be made.
	 * @param searchInExamplesCheckBox
	 *            the "Search in examples" check box reference. If null then a
	 *            regular search will be performed.
	 * @param searchEditText
	 *            the search query edit box
	 * @param handleSelections
	 *            if true then only selected portions of text will be used for
	 *            search (if a selection exists).
	 * @param searchButton
	 *            the search button
	 * @param isJapanSearch
	 *            if true then we are searching for japanese text (in romaji).
	 */
	public void registerSearch(final Integer isExactCheckBox, final Integer deinflectCheckBox, final Integer searchInExamplesCheckBox, final int searchEditText, final boolean handleSelections,
			final int searchButton, final boolean isJapanSearch) {
		final EditText searchEdit = (EditText) activity.findViewById(searchEditText);
		final Button searchBtn = (Button) activity.findViewById(searchButton);
		final SearchText handler = new SearchText(isExactCheckBox, deinflectCheckBox, searchInExamplesCheckBox, searchEditText, handleSelections, isJapanSearch);
		searchEdit.setOnEditorActionListener(AndroidUtils.safe(activity, OnEditorActionListener.class, handler));
		searchBtn.setOnClickListener(AndroidUtils.safe(activity, OnClickListener.class, handler));
		if (isExactCheckBox != null && deinflectCheckBox != null) {
			final CheckBox deinflect = (CheckBox) activity.findViewById(deinflectCheckBox);
			deinflect.setOnCheckedChangeListener(AndroidUtils.safe(activity, OnCheckedChangeListener.class, handler));
		}
		if (isExactCheckBox != null && searchInExamplesCheckBox != null) {
			final CheckBox search = (CheckBox) activity.findViewById(searchInExamplesCheckBox);
			search.setOnCheckedChangeListener(AndroidUtils.safe(activity, OnCheckedChangeListener.class, handler));
		}
	}

	/**
	 * Configures specific GUI components for the dictionary search.
	 * 
	 * @author Martin Vysny
	 */
	private class SearchText implements TextView.OnEditorActionListener, View.OnClickListener, OnCheckedChangeListener {
		private final Integer isExactCheckBox;
		private final int searchEditText;
		private final boolean handleSelections;
		private final boolean isJapanSearch;
		private final Integer deinflectCheckBox;
		private final Integer searchInExamplesCheckBox;

		/**
		 * Creates new search instance.
		 * 
		 * @param isExactCheckBox
		 *            the "IsExact" check box resource id. If null then an exact
		 *            search will always be performed.
		 * @param deinflectCheckBox
		 *            the "deinflect" check box reference. If null then no
		 *            deinflection attempt will be made.
		 * @param searchInExamplesCheckBox
		 *            the "Search in examples" check box reference. If null then
		 *            a regular search will be performed.
		 * @param searchEditText
		 *            the search query edit box
		 * @param handleSelections
		 *            if true then only selected portions of text will be used
		 *            for search (if a selection exists).
		 * @param isJapanSearch
		 *            if true then we are searching for japanese text (in
		 *            romaji).
		 */
		public SearchText(final Integer isExactCheckBox, final Integer deinflectCheckBox, final Integer searchInExamplesCheckBox, final int searchEditText, final boolean handleSelections,
				final boolean isJapanSearch) {
			this.isExactCheckBox = isExactCheckBox;
			this.deinflectCheckBox = deinflectCheckBox;
			this.searchInExamplesCheckBox = searchInExamplesCheckBox;
			this.searchEditText = searchEditText;
			this.handleSelections = handleSelections;
			this.isJapanSearch = isJapanSearch;
		}

		public void onClick(View v) {
			performSearch();
		}

		private void performSearch() {
			final EditText searchEdit = (EditText) activity.findViewById(searchEditText);
			final boolean isDeinflect = deinflectCheckBox == null ? false : ((CheckBox) activity.findViewById(deinflectCheckBox)).isChecked();
			final boolean isSearchInExamples = searchInExamplesCheckBox == null ? false : ((CheckBox) activity.findViewById(searchInExamplesCheckBox)).isChecked();
			final boolean isExact = isDeinflect ? true : (isSearchInExamples ? false : (isExactCheckBox == null ? true : ((CheckBox) activity.findViewById(isExactCheckBox)).isChecked()));
			String query = searchEdit.getText().toString();
			if (handleSelections) {
				int start = searchEdit.getSelectionStart();
				int end = searchEdit.getSelectionEnd();
				if ((start >= 0) && (end >= 0)) {
					if (start > end) {
						start = searchEdit.getSelectionEnd();
						end = searchEdit.getSelectionStart();
					}
					String selected = query.substring(start, end).trim();
					if (selected.length() > 0) {
						query = selected;
					}
				}
			}
			if (isJapanSearch) {
				searchForJapan(query, isExact, isDeinflect, isSearchInExamples);
			} else {
				searchForEnglish(query, isExact, isSearchInExamples);
			}
		}

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			performSearch();
			return true;
		}

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				final CheckBox cb = (CheckBox) activity.findViewById(isExactCheckBox);
				if (deinflectCheckBox != null) {
					cb.setChecked(true);
					cb.setEnabled(false);
				} else if (searchInExamplesCheckBox != null) {
					cb.setChecked(false);
					cb.setEnabled(false);
				} else {
					cb.setEnabled(true);
				}
			}
		}
	}

	/**
	 * Configures given button to copy a text from given edit to the global
	 * clipboard.
	 * 
	 * @param copyButton
	 *            copies the text to the clipboard on this button press
	 * @param textView
	 *            copies the text from this {@link TextView}
	 */
	public void setupCopyButton(final int copyButton, final int textView) {
		final Button btn = (Button) activity.findViewById(copyButton);
		final TextView text = (TextView) activity.findViewById(textView);
		btn.setOnClickListener(AndroidUtils.safe(activity, new View.OnClickListener() {

			public void onClick(View v) {
				final ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
				cm.setText(text.getText());
				final Toast toast = Toast.makeText(activity, AedictApp.format(R.string.copied, text.getText()), Toast.LENGTH_SHORT);
				toast.show();
			}
		}));
	}

	/**
	 * Configures given button to perform an analysis of japanese text from
	 * given edit.
	 * 
	 * @param analysisButton
	 *            performs analysis on click on this button
	 * @param textView
	 *            analyzes text from this {@link TextView}
	 * @param startWordAnalysis
	 *            if true then a word analysis will be shown, if false then
	 *            character-based analysis will be shown by default.
	 */
	public void setupAnalysisControls(final int analysisButton, final int textView, final boolean startWordAnalysis) {
		final Button analyze = (Button) activity.findViewById(analysisButton);
		final TextView text = (TextView) activity.findViewById(textView);
		analyze.setOnClickListener(AndroidUtils.safe(activity, new View.OnClickListener() {

			public void onClick(View v) {
				KanjiAnalyzeActivity.launch(activity, text.getText().toString().trim(), startWordAnalysis);
			}
		}));
	}
}
