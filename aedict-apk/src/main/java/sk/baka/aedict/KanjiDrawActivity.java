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
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import edu.arizona.cs.javadict.DrawPanel;

/**
 * Allows user to draw a kanji and perform a Kanji lookup.
 * 
 * @author Martin Vysny
 */
public class KanjiDrawActivity extends AbstractActivity {
	private DrawPanel recognizer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kanjidraw);
		recognizer = new DrawPanel(getClassLoader());
		updateStrokes();
		final PainterView view = new PainterView(this, recognizer);
		((ViewGroup) findViewById(R.id.kanjidrawRoot)).addView(view);
		findViewById(R.id.btnKanjiClear).setOnClickListener(AedictApp.safe(new View.OnClickListener() {

			public void onClick(View v) {
				recognizer.clear();
				updateStrokes();
				view.invalidate();
			}
		}));
		findViewById(R.id.btnKanjiSearch).setOnClickListener(AedictApp.safe(new View.OnClickListener() {

			public void onClick(View v) {
				try {
					final String kanjis = recognizer.analyzeKanji();
					final Intent i = new Intent(KanjiDrawActivity.this, KanjiAnalyzeActivity.class);
					i.putExtra(KanjiAnalyzeActivity.INTENTKEY_WORD, kanjis);
					startActivity(i);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}));
	}

	private void updateStrokes() {
		((TextView) findViewById(R.id.textStrokes)).setText(AedictApp.format(R.string.strokes, recognizer.xstrokes.size()));
	}

	/**
	 * Uses the DrawPanel class to paint and recognize Kanjis.
	 * 
	 * @author Martin Vysny
	 */
	private class PainterView extends View implements OnTouchListener {
		private final DrawPanel recognizer;
		private final Paint bg = new Paint();
		private final Paint fg1 = new Paint();
		private final Paint fg2 = new Paint();

		public PainterView(Context context, final DrawPanel recognizer) {
			super(context);
			setFocusable(true);
			setFocusableInTouchMode(true);
			this.setOnTouchListener(this);
			bg.setARGB(255, 0, 0, 0);
			fg1.setARGB(255, 235, 255, 235);
			fg1.setAntiAlias(true);
			fg1.setStrokeWidth(8f);
			fg2.setARGB(255, 160, 160, 255);
			fg2.setAntiAlias(true);
			fg2.setStrokeWidth(8f);
			this.recognizer = recognizer;
		}

		@Override
		protected void onDraw(Canvas c) {
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
			final int x, y;
			x = (int) event.getX();
			y = (int) event.getY();
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				recognizer.curxvec = new ArrayList<Integer>();
				recognizer.curyvec = new ArrayList<Integer>();
				recognizer.xstrokes.add(recognizer.curxvec);
				recognizer.ystrokes.add(recognizer.curyvec);
				recognizer.curxvec.add(x);
				recognizer.curyvec.add(y);
				updateStrokes();
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				recognizer.curxvec.add(x);
				recognizer.curyvec.add(y);
			}
			invalidate();
			return true;
		}

		private void drawVec(Canvas g, Iterator<Integer> xe2, Iterator<Integer> ye2, final Paint p) {
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
