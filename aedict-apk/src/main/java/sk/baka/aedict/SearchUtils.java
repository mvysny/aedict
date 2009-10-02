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

import android.app.Activity;
import android.content.Intent;

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
	 */
	public void searchForJapan(final String romaji, final boolean isExact) {
		final SearchQuery q = new SearchQuery();
		q.isJapanese = true;
		q.query = new String[] { JpUtils.toHiragana(romaji),
				JpUtils.toKatakana(romaji) };
		q.matcher = isExact ? MatcherEnum.ExactMatchEng
				: MatcherEnum.SubstringMatch;
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
		q.matcher = isExact ? MatcherEnum.ExactMatchEng
				: MatcherEnum.SubstringMatch;
		performSearch(q);
	}

	private void performSearch(final SearchQuery query) {
		final Intent intent = new Intent(activity, ResultActivity.class);
		query.putTo(intent);
		activity.startActivity(intent);
	}
}
