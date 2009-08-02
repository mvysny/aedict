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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * Performs a search and shows search result.
 * 
 * @author Martin Vysny
 */
public class ResultActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		List<String> list;
		final SearchQuery query = SearchQuery.fromIntent(getIntent())
				.toLowerCase();
		if (MiscUtils.isBlank(query.query)) {
			// nothing to search for
			list = Collections.singletonList("Nothing to search for");
		} else {
			try {
				list = performSearch2(query);
			} catch (Exception ex) {
				Log.e(ResultActivity.class.getSimpleName(),
						"Failed to perform search", ex);
				list = Collections.singletonList("Failed to perform search: "
						+ ex);
			}
			if (list.isEmpty()) {
				list = Collections.singletonList("No results");
			}
		}
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list));
	}

	private List<String> performSearch2(final SearchQuery query)
			throws IOException {
		final List<String> result = new ArrayList<String>();
		final byte[][] queries = new byte[query.query.length][];
		int i = 0;
		for (final String q : query.query) {
			queries[i++] = q.getBytes("EUC-JP");
		}
		final InputStream in = new FileInputStream("/sdcard/aedict/edict");
		try {
			final LineReadInputStream edict = new LineReadInputStream(in);
			while (edict.readLine()) {
				for (final byte[] q : queries) {
					if (contains(q, edict)) {
						final String line = new String(edict.buffer,
								edict.lineStart, edict.lineLength, "EUC-JP");
						result.add(line);
					}
				}
			}
		} finally {
			MiscUtils.closeQuietly(in);
		}
		return result;
	}

	private boolean contains(byte[] subarray, LineReadInputStream in) {
		byte firstChar = subarray[0];
		int matched = 0;
		final byte[] array = in.buffer;
		final int end = in.lineStart + in.lineLength - subarray.length + 1;
		for (int i = in.lineStart; i < end; i++) {
			if (matched == 0) {
				if (array[i] != firstChar) {
					continue;
				}
				matched++;
				continue;
			}
			if (array[i] != subarray[matched]) {
				matched = 0;
				continue;
			}
			matched++;
			if (matched >= subarray.length) {
				return true;
			}
		}
		return false;
	}
}
