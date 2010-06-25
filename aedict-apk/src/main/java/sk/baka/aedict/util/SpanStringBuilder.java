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

import android.app.Activity;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Provides additional utility methods.
 * 
 * @author Martin Vysny
 */
public class SpanStringBuilder extends SpannableStringBuilder {
	/**
	 * Adds text with given span.
	 * 
	 * @param span
	 *            the span
	 * @param what
	 *            the text to add.
	 */
	public void append(Object span, CharSequence what) {
		int oldLen = length();
		append(what);
		setSpan(span, oldLen, length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	public StyleSpan newStyle(final boolean bold, final boolean italic) {
		return new StyleSpan((bold ? Typeface.BOLD : 0) | (italic ? Typeface.ITALIC : 0));
	}

	/**
	 * Sets the foreground color.
	 * 
	 * @param color
	 *            0xAARRGGBB. Don't forget to set alpha to FF if you do not want
	 *            transparency.
	 * @return span
	 */
	public ForegroundColorSpan newForeground(int color) {
		return new ForegroundColorSpan(color);
	}

	/**
	 * Note that you have to call
	 * <code>TextView.setMovementMethod(new LinkMovementMethod())</code> to
	 * enable the clickables. The TextView should automatically become
	 * focusable.
	 * 
	 * @param handler
	 *            handles click events
	 * @return new span
	 */
	public ClickableSpan newClickable(final View.OnClickListener handler) {
		return new ClickableSpan() {

			@Override
			public void onClick(View widget) {
				handler.onClick(widget);
			}
		};
	}

	public AbsoluteSizeSpan newSize(final Activity a, final int sizeDip) {
		final DisplayMetrics dm = new DisplayMetrics();
		a.getWindowManager().getDefaultDisplay().getMetrics(dm);
		// cannot use dm.densityDpi - the field is not present in Android 1.5
		// fixes http://code.google.com/p/aedict/issues/detail?id=76 
		return new AbsoluteSizeSpan((int)(sizeDip * dm.density));
	}
}
