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

import java.io.Serializable;

import android.content.Intent;

/**
 * Contains values for a search.
 * 
 * @author Martin Vysny
 */
public final class SearchQuery implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * A search query.
	 */
	public String query;
	/**
	 * If true we are searching for a japanese query. If false the query is for
	 * an english word.
	 */
	public boolean isJapanese;

	/**
	 * Retrieves values for the query from given intent.
	 * 
	 * @param intent
	 *            the intent to parse
	 * @return query instance.
	 */
	public static SearchQuery fromIntent(final Intent intent) {
		final SearchQuery result = new SearchQuery();
		result.query = intent.getStringExtra(INTENTKEY_SEARCH_QUERY);
		result.isJapanese = intent.getBooleanExtra(INTENTKEY_JAPANESE, false);
		return result;
	}

	private static final String INTENTKEY_SEARCH_QUERY = "QUERY";
	private static final String INTENTKEY_JAPANESE = "JAPANESE";

	/**
	 * Puts values from this bean to given intent. The object can be
	 * reconstructed later by using {@link #fromIntent(Intent)}.
	 * 
	 * @param intent
	 *            store the values here.
	 */
	public void putTo(Intent intent) {
		intent.putExtra(INTENTKEY_SEARCH_QUERY, query);
		intent.putExtra(INTENTKEY_JAPANESE, isJapanese);
	}
}
