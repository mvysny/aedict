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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sk.baka.aedict.util.SodLoader;
import sk.baka.autils.AndroidUtils;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Shows a stroke order for given kanji list.
 * 
 * @author Martin Vysny
 */
public class StrokeOrderActivity extends ListActivity {
	/**
	 * An intent string value: the list of kanji characters.
	 */
	static final String INTENTKEY_KANJILIST = "kanjiList";

	public static void launch(final Context activity, final String kanjiList) {
		final Intent intent = new Intent(activity, StrokeOrderActivity.class);
		intent.putExtra(INTENTKEY_KANJILIST, kanjiList);
		activity.startActivity(intent);
	}

	/**
	 * Shows a list of matched entries. May contain an error message if the
	 * search failed.
	 */
	private List<Character> model = new ArrayList<Character>();
	/**
	 * Loads SOD images.
	 */
	private SodLoader sodLoader = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (AedictApp.getDownloader().checkSod(this)) {
			final String kanjis = getIntent().getStringExtra(INTENTKEY_KANJILIST);
			for (final char c : kanjis.toCharArray()) {
				model.add(c);
			}
			try {
				sodLoader = new SodLoader();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		setListAdapter(new ArrayAdapter<Character>(this, R.layout.soddetail, model) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView;
				if (view == null) {
					view = getLayoutInflater().inflate(R.layout.soddetail, getListView(), false);
				}
				final TextView kanjiText = (TextView) view.findViewById(R.id.kanjiBig);
				final Character kanji = model.get(position);
				kanjiText.setText(kanji.toString());
				final ImageView image = (ImageView) view.findViewById(R.id.sodImageView);
				BitmapDrawable bd = null;
				try {
					bd = sodLoader.loadBitmap(kanji);
				} catch (IOException e) {
					AndroidUtils.handleError(e, StrokeOrderActivity.this, getClass(), null);
				}
				if (bd != null) {
					image.setImageDrawable(bd);
				} else {
					image.setImageDrawable(new ColorDrawable(0));
				}
				return view;
			}

		});
	}
}
