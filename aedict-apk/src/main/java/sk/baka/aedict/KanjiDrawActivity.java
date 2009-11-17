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
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;

/**
 * Allows user to draw a kanji and perform a Kanji lookup.
 * 
 * @author Martin Vysny
 */
public class KanjiDrawActivity extends AbstractActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kanjidraw);
		((ViewGroup) findViewById(R.id.kanjidrawRoot)).addView(new PainterView(this));
	}

	private static class PainterView extends View implements OnTouchListener {
		List<Point> points = new ArrayList<Point>();
		Paint paint = new Paint();

		public PainterView(Context context) {
			super(context);
			setFocusable(true);
			setFocusableInTouchMode(true);

			this.setOnTouchListener(this);

			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true);
		}

		@Override
		public void onDraw(Canvas canvas) {
			for (Point point : points) {
				canvas.drawCircle(point.x, point.y, 5, paint);
			}
		}

		public boolean onTouch(View view, MotionEvent event) {
			// if(event.getAction() != MotionEvent.ACTION_DOWN)
			// return super.onTouchEvent(event);
			Point point = new Point();
			point.x = (int) event.getX();
			point.y = (int) event.getY();
			points.add(point);
			invalidate();
			return true;
		}
	}
}
