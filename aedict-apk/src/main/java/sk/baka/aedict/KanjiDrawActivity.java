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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sk.baka.aedict.util.Constants;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.DialogUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import edu.arizona.cs.javadict.DrawPanel;

/**
 * Allows user to draw a kanji and perform a Kanji lookup.
 * 
 * @author Martin Vysny
 */
public class KanjiDrawActivity extends AbstractActivity {

	private static final class State implements Serializable {
		private static final long serialVersionUID = 1L;
		public final List<String> kanjis = new ArrayList<String>();
		public final List<Character> selected = new ArrayList<Character>();

		public boolean isEmpty() {
			return kanjis.isEmpty();
		}

		public String getSelectedWord() {
			final StringBuilder result = new StringBuilder();
			for (final Character c : selected) {
				result.append(c);
			}
			return result.toString();
		}
	}

	private State state;
	private static final String BUNDLEKEY_STATE = "state";

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		state = (State) savedInstanceState.getSerializable(BUNDLEKEY_STATE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(BUNDLEKEY_STATE, state);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		state = new State();
		setContentView(R.layout.kanjidraw);
		final PainterView view = new PainterView(this, R.id.textStrokes);
		((ViewGroup) findViewById(R.id.kanjidrawRoot)).addView(view);
		findViewById(R.id.btnKanjiClear).setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				view.clear();
			}
		}));
		findViewById(R.id.btnKanjiSearch).setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				try {
					final String kanjis = state.isEmpty() ? view.analyzeKanji() : state.getSelectedWord();
					KanjiAnalyzeActivity.launch(KanjiDrawActivity.this, kanjis, !state.isEmpty());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}));
		findViewById(R.id.undo).setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				view.undoLastStroke();
			}
		}));
		findViewById(R.id.more).setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			public void onClick(View v) {
				try {
					final String kanjis = view.analyzeKanji();
					if (kanjis.length() > 0) {
						state.kanjis.add(kanjis);
						state.selected.add(kanjis.charAt(0));
						view.clear();
						update();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}));
		new DialogUtils(this).showInfoOnce(Constants.INFOONCE_KANJIDRAWWARNING, -1, R.string.kanjiDrawWarning);
	}

	/**
	 * Uses the DrawPanel class to paint and recognize Kanjis.
	 * 
	 * @author Martin Vysny
	 */
	public static class PainterView extends View implements OnTouchListener {
		private final DrawPanel recognizer;
		private final Paint bg = new Paint();
		private final Paint fg1 = new Paint();
		private final Paint fg2 = new Paint();
		private final int textViewStrokes;

		public PainterView(Activity context, final int textViewStrokes) {
			super(context);
			recognizer = new DrawPanel(context.getClassLoader());
			this.textViewStrokes = textViewStrokes;
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
			updateStrokes();
		}

		public String analyzeKanji() throws IOException {
			return recognizer.analyzeKanji();
		}

		public void undoLastStroke() {
			recognizer.undoLastStroke();
			updateStrokes();
			invalidate();
		}

		public void clear() {
			recognizer.clear();
			updateStrokes();
			invalidate();
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

		private void updateStrokes() {
			((TextView) ((Activity) getContext()).findViewById(textViewStrokes)).setText(AedictApp.format(R.string.strokes, recognizer.xstrokes.size()));
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

	@Override
	protected void onResume() {
		super.onResume();
		update();
	}

	private void update() {
		final ViewGroup buttonList = (ViewGroup) findViewById(R.id.kanjiButtonBar);
		buttonList.setVisibility(state.isEmpty() ? View.GONE : View.VISIBLE);
		buttonList.removeAllViews();
		for (int i = 0; i < state.kanjis.size(); i++) {
			final Button b = new Button(this);
			final int index = i;
			final String kanjiList = state.kanjis.get(i);
			b.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					final AlertDialog.Builder builder = new AlertDialog.Builder(KanjiDrawActivity.this);
					final CharSequence[] items = new CharSequence[kanjiList.length() + 1];
					items[0] = getString(R.string.delete);
					for (int i = 0; i < kanjiList.length(); i++) {
						items[i + 1] = Character.toString(kanjiList.charAt(i));
					}
					builder.setItems(items, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								state.kanjis.remove(index);
								state.selected.remove(index);
							} else {
								state.selected.set(index, kanjiList.charAt(which - 1));
							}
							update();
						}
					});
					builder.setTitle(R.string.kanjiSearchMethod);
					final AlertDialog dlg = builder.create();
					dlg.setOwnerActivity(KanjiDrawActivity.this);
					dlg.show();
				}
			});
			b.setText(state.selected.get(i).toString());
			buttonList.addView(b);
		}
	}
}
