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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.widget.ArrayAdapter;

/**
 * Performs a search and shows search result.
 * 
 * @author Martin Vysny
 */
public class ResultActivity extends ListActivity {
	/**
	 * Creates new activity.
	 */
	public ResultActivity() {
		super();
		List<String> list;
		final SearchQuery query = SearchQuery.fromIntent(getIntent());
		if (MiscUtils.isBlank(query.query)) {
			// nothing to search for
			list = Collections.singletonList("Nothing to search for");
			finish();
		}
		try {
			list = performSearch(query);
		} catch (Exception ex) {
			list = Collections.singletonList(ex.toString() + "\n"
					+ getStacktrace(ex));
		}
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list));
	}

	private String getStacktrace(Exception ex) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace();
		pw.close();
		return sw.toString();
	}

	private List<String> performSearch(final SearchQuery query)
			throws IOException {
		final String expr = query.query.toLowerCase();
		final List<String> result = new ArrayList<String>();
		final BufferedReader in = new BufferedReader(new InputStreamReader(
				MiscUtils.openResource("edict")));
		try {
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {
				if (line.toLowerCase().contains(expr)) {
					result.add(line);
				}
			}
		} finally {
			MiscUtils.closeQuietly(in);
		}
		return result;
	}
}
