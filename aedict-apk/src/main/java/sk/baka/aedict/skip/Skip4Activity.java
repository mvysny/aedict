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

import sk.baka.aedict.R;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.bind.AndroidViewMapper;
import sk.baka.autils.bind.BindToView;
import sk.baka.autils.bind.Binder;
import sk.baka.autils.bind.validator.Range;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Handles the SKIP type 4 codes.
 * 
 * @author Martin Vysny
 */
public class Skip4Activity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.skip4);
		final View.OnClickListener search = AndroidUtils.safe(this, new View.OnClickListener() {
			public void onClick(View v) {
				final int type;
				switch (v.getId()) {
				case R.id.skip41:
					type = 1;
					break;
				case R.id.skip42:
					type = 2;
					break;
				case R.id.skip43:
					type = 3;
					break;
				default:
					type = 4;
					break;
				}
				performSearch(type);
			}
		});
		findViewById(R.id.skip41).setOnClickListener(search);
		findViewById(R.id.skip42).setOnClickListener(search);
		findViewById(R.id.skip43).setOnClickListener(search);
		findViewById(R.id.skip44).setOnClickListener(search);
	}

	private static class Strokes {
		@BindToView(R.id.editSkipNumberOfStrokes)
		@Range(min = 1, max = 30)
		public int strokes;
	}

	private void performSearch(final int type) {
		final Strokes bean = new Strokes();
		new Binder().bindFromComponent(bean, new AndroidViewMapper(), this, true);
		SkipActivity.searchForSkip(this, SkipActivity.getSkipCode(4, bean.strokes, type));
	}
}
