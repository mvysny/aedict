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

import java.net.URL;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.KanjiAnalyzeActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.ResultActivity;
import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.DownloadDictTask;
import sk.baka.aedict.dict.MatcherEnum;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.DialogUtils;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.StatFs;
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
	public void searchForJapan(final String romaji, final boolean isExact, final boolean isDeinflect) {
		final Config cfg = AedictApp.loadConfig();
		final SearchQuery q = SearchQuery.searchForRomaji(romaji, cfg.romanization, isExact, isDeinflect);
		performSearch(q);
	}

	/**
	 * Performs search for an english word or expression.
	 * 
	 * @param text
	 *            the text to search for.
	 * @param isExact
	 *            if true then only exact matches are returned.
	 */
	public void searchForEnglish(final String text, final boolean isExact) {
		final SearchQuery q = new SearchQuery();
		q.isJapanese = false;
		q.query = new String[] { text };
		q.matcher = isExact ? MatcherEnum.ExactMatchEng : MatcherEnum.SubstringMatch;
		performSearch(q);
	}

	private void performSearch(final SearchQuery query) {
		final Intent intent = new Intent(activity, ResultActivity.class);
		query.putTo(intent);
		activity.startActivity(intent);
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
	public void registerSearch(final Integer isExactCheckBox, final Integer deinflectCheckBox, final Integer searchInExamplesCheckBox, final int searchEditText, final boolean handleSelections, final int searchButton, final boolean isJapanSearch) {
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
		public SearchText(final Integer isExactCheckBox, final Integer deinflectCheckBox, final Integer searchInExamplesCheckBox, final int searchEditText, final boolean handleSelections, final boolean isJapanSearch) {
			if (deinflectCheckBox != null && searchInExamplesCheckBox != null) {
				throw new IllegalArgumentException("deinflectCheckBox and searchInExamplesCheckBox cannot both be non-null");
			}
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
				searchForJapan(query, isExact, isDeinflect);
			} else {
				searchForEnglish(query, isExact);
			}
		}

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			performSearch();
			return true;
		}

		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				if (deinflectCheckBox != null) {
					((CheckBox) activity.findViewById(isExactCheckBox)).setChecked(true);
				} else if (searchInExamplesCheckBox != null) {
					((CheckBox) activity.findViewById(isExactCheckBox)).setChecked(false);
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
				final Intent intent = new Intent(activity, KanjiAnalyzeActivity.class);
				intent.putExtra(KanjiAnalyzeActivity.INTENTKEY_WORD, text.getText().toString().trim());
				intent.putExtra(KanjiAnalyzeActivity.INTENTKEY_WORD_ANALYSIS, startWordAnalysis);
				activity.startActivity(intent);
			}
		}));
	}

	/**
	 * Checks if given dictionary file exists. If not, user is prompted for a
	 * download and the files are downloaded if requested.
	 * 
	 * @param source
	 *            download the dictionary here. A Lucene zipped index file is
	 *            expected.
	 * @param targetDir
	 *            unpack the files here.
	 * @param expectedSize
	 *            the expected size of the dictionary file.
	 * @param dictName
	 *            the name of the dictionary, EDict or KanjiDic
	 * @return true if the files are available, false otherwise.
	 */
	public boolean checkDictionaryFile(final URL source, final String targetDir, final long expectedSize, final String dictName) {
		if (!DownloadDictTask.isComplete(targetDir)) {
			final StatFs stats = new StatFs("/sdcard");
			final long free = ((long) stats.getBlockSize()) * stats.getAvailableBlocks();
			final StringBuilder msg = new StringBuilder(activity.getString(R.string.dictionary_missing_download, dictName));
			if (free < expectedSize) {
				msg.append('\n');
				msg.append(AedictApp.format(R.string.warning_less_than_x_mb_free, expectedSize / 1024, free / 1024));
			}
			new DialogUtils(activity).showYesNoDialog(msg.toString(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					new DownloadDictTask(source, targetDir, dictName, expectedSize).execute(activity);
				}
			});
			return false;
		}
		return true;
	}

	/**
	 * Checks if KANJIDIC exists. If not, user is prompted for a download and
	 * the files are downloaded if requested.
	 * 
	 * @return true if the files are available, false otherwise.
	 */
	public boolean checkKanjiDic() {
		return checkDictionaryFile(DownloadDictTask.KANJIDIC_LUCENE_ZIP, DownloadDictTask.LUCENE_INDEX_KANJIDIC, 1500 * 1024, "KanjiDic");
	}
}
