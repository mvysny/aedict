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

package sk.baka.aedict.skip;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import sk.baka.aedict.AbstractActivity;
import sk.baka.aedict.KanjiAnalyzeActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.autils.AndroidUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * A first form of the SKIP lookup wizard.
 * 
 * @author Martin Vysny
 */
public class SkipActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.skip1);
		configureButtonFor123SkipWizardContinuation(R.id.skip11, 1);
		configureButtonFor123SkipWizardContinuation(R.id.skip12, 2);
		configureButtonFor123SkipWizardContinuation(R.id.skip13, 3);
		AbstractActivity.setButtonActivityLauncher(this, R.id.skip14, Skip4Activity.class);
		// check that the KANJIDIC dictionary file is available
		new SearchUtils(this).checkKanjiDic();
	}

	private void configureButtonFor123SkipWizardContinuation(final int buttonId, final int skipType) {
		final Button btn = (Button) findViewById(buttonId);
		btn.setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				final Intent intent = new Intent(SkipActivity.this, Skip123Activity.class);
				intent.putExtra(Skip123Activity.INTENTKEY_TYPE, skipType);
				startActivity(intent);
			}
		}));
	}

	/**
	 * Creates a Lucene-lookupable SKIP code.
	 * 
	 * @param first
	 *            the first number
	 * @param second
	 *            the second number
	 * @param third
	 *            the third number
	 * @return string SKIP code
	 */
	public static String getSkipCode(final int first, final int second, final int third) {
		if (first < 1 || first > 4) {
			throw new IllegalArgumentException("SKIP type must be 1, 2, 3 or 4: " + first);
		}
		return first + "-" + second + "-" + third;
	}

	/**
	 * Performs search for given SKIP code
	 * 
	 * @param activity
	 *            the activity reference.
	 * @param skip
	 *            the skip code
	 */
	public static void searchForSkip(final Activity activity, final String skip) {
		final SearchQuery query = new SearchQuery();
		query.skip = skip;
		try {
			final List<String> result = LuceneSearch.singleSearch(query, true);
			final List<EdictEntry> parsedResult = EdictEntry.parseKanjidic(result);
			// no need to sort on the number of strokes as all results will have
			// the same amount of strokes
			Collections.sort(parsedResult);
			final Intent intent = new Intent(activity, KanjiAnalyzeActivity.class);
			intent.putExtra(KanjiAnalyzeActivity.INTENTKEY_ENTRYLIST, (Serializable) parsedResult);
			activity.startActivity(intent);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
