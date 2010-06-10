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

import android.view.View;

/**
 * Highlights a view when focused (by setting its background to an orange
 * color).
 * 
 * @author Martin Vysny
 */
public class FocusVisual implements View.OnFocusChangeListener {
	/**
	 * Registers this listener to a view.
	 * 
	 * @param view
	 *            the view, not null.
	 * @return this
	 */
	public FocusVisual registerTo(final View view) {
		view.setFocusable(true);
		view.setOnFocusChangeListener(this);
		return this;
	}

	public void onFocusChange(View v, boolean hasFocus) {
		v.setBackgroundColor(hasFocus ? 0xCFFF8c00 : 0);
	}
}