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

import sk.baka.aedict.AedictApp.Config;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.util.SearchUtils;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Shows an EDict entry detail window which allows copy-paste and further
 * searches.
 * 
 * @author Martin Vysny
 */
public class EntryDetailActivity extends AbstractActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entrydetail);
		final EdictEntry entry = (EdictEntry) getIntent().getSerializableExtra(INTENTKEY_ENTRY);
		final EditText kanjiSearchEdit = (EditText) findViewById(R.id.kanjiSearchEdit);
		kanjiSearchEdit.setText(entry.getJapanese());
		final EditText readingSearchEdit = (EditText) findViewById(R.id.readingSearchEdit);
		final Config cfg = AedictApp.loadConfig();
		readingSearchEdit.setText(cfg.useRomaji ? cfg.romanization.toRomaji(entry.reading) : entry.reading);
		final EditText englishSearchEdit = (EditText) findViewById(R.id.englishSearchEdit);
		englishSearchEdit.setText(entry.english);
		final SearchUtils utils = new SearchUtils(this);
		utils.registerSearch(R.id.kanjiExactMatch, null, R.id.kanjiSearchEdit, true, R.id.kanjiSearch, false);
		utils.registerSearch(R.id.readingExactMatch, null, R.id.readingSearchEdit, true, R.id.readingSearch, true);
		utils.registerSearch(R.id.englishExactMatch, null, R.id.englishSearchEdit, true, R.id.englishSearch, false);
		utils.setupCopyButton(R.id.kanjiCopy, R.id.kanjiSearchEdit);
		utils.setupCopyButton(R.id.readingCopy, R.id.readingSearchEdit);
		utils.setupCopyButton(R.id.englishCopy, R.id.englishSearchEdit);
		utils.setupAnalysisControls(R.id.kanjiAnalyze, R.id.kanjiSearchEdit, false);
	}

	/**
	 * The activity expects {@link EdictEntry} in the intent keys.
	 */
	public static final String INTENTKEY_ENTRY = "entry";
}
