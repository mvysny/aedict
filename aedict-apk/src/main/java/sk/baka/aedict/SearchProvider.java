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
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.autils.MiscUtils;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

/**
 * Provides search results for the Android Search functionality.
 * 
 * @author Martin Vysny
 */
public class SearchProvider extends ContentProvider {
	/**
	 * The authority name.
	 */
	public static final String AUTHORITY = "sk.baka.aedict.search";

	private static final String[] COLUMN_NAMES = new String[] { "_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA };
	private static final int SEARCH_SUGGEST = 0;

	private static UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final RomanizationEnum romanize = AedictApp.getConfig().isUseRomaji() ? AedictApp.getConfig().getRomanization() : null;
		final String searchString = uri.getLastPathSegment();
		final MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES);
		final List<DictEntry> entries = new ArrayList<DictEntry>();
		try {
			final LuceneSearch lucene = new LuceneSearch(DictTypeEnum.Edict, AedictApp.getConfig().getDictionaryLoc());
			try {
				entries.addAll(lucene.search(SearchQuery.searchForRomaji(searchString, AedictApp.getConfig().getRomanization(), true, true)));
				entries.addAll(lucene.search(SearchQuery.searchForEnglish(searchString, true)));
			} finally {
				MiscUtils.closeQuietly(lucene);
			}
		} catch (Exception ex) {
			Log.e(SearchProvider.class.getSimpleName(), ex.getMessage(), ex);
			entries.add(DictEntry.newErrorMsg(ex.toString()));
		}
		Collections.sort(entries);
		for (final DictEntry entry : entries) {
			Object[] rowObject = new Object[] { searchString, entry.formatJapanese(romanize), entry.english, entry.toExternal() };
			cursor.addRow(rowObject);
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
}
