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
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import edu.arizona.cs.javadict.DrawPanel;

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
		private final DrawPanel recognizer = new DrawPanel();
		private final Paint bg = new Paint();
		private final Paint fg1 = new Paint();
		private final Paint fg2 = new Paint();
		private int lastx, lasty;

		public PainterView(Context context) {
			super(context);
			setFocusable(true);
			setFocusableInTouchMode(true);
			this.setOnTouchListener(this);
			bg.setARGB(255, 30, 30, 50);
			fg1.setARGB(255, 235, 255, 235);
			fg1.setAntiAlias(true);
			fg2.setARGB(255, 160, 160, 255);
			fg2.setAntiAlias(true);
		}

		@Override
		public void onDraw(Canvas c) {
			Rect r = new Rect();
			getDrawingRect(r);
			c.drawRect(r, bg);
			Iterator<List<Integer>> xe = recognizer.xstrokes.iterator();
			Iterator<List<Integer>> ye = recognizer.ystrokes.iterator();
			while (xe.hasNext()) {
				List<Integer> xvec, yvec;
				xvec = xe.next();
				yvec = ye.next();
				final Iterator<Integer> xe2 = xvec.iterator();
				final Iterator<Integer> ye2 = yvec.iterator();
				final Paint p;
				if (xvec != recognizer.curxvec)
					p = fg2;
				else
					p = fg1;
				drawVec(c, xe2, ye2, p);
			} // while xe
		}

		public boolean onTouch(View view, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				recognizer.curxvec = new ArrayList<Integer>();
				recognizer.curyvec = new ArrayList<Integer>();
				recognizer.xstrokes.add(recognizer.curxvec);
				recognizer.ystrokes.add(recognizer.curyvec);
				lastx = (int) event.getX();
				lasty = (int) event.getY();
				recognizer.curxvec.add(lastx);
				recognizer.curyvec.add(lasty);
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				int x, y;
				x = (int) event.getX();
				y = (int) event.getY();
				recognizer.curxvec.add(x);
				recognizer.curyvec.add(y);
				lastx = x;
				lasty = y;
			}
			invalidate();
			return true;
		}

		public void drawVec(Canvas g, Iterator<Integer> xe2, Iterator<Integer> ye2, final Paint p) {
			int lastx, lasty;
			lastx = -1;
			lasty = -1;
			while (xe2.hasNext()) {
				int x, y;
				x = xe2.next();
				y = ye2.next();
				if (lastx != -1)
					g.drawLine(lastx, lasty, x, y, p);
				lastx = x;
				lasty = y;
			} // while xe2
		}
	}
}
