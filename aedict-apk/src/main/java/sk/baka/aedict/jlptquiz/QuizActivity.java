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
package sk.baka.aedict.jlptquiz;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.EdictEntryDetailActivity;
import sk.baka.aedict.KanjiDetailActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.KanjidicEntry;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.ListBuilder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

/**
 * Performs the quiz itself.
 * 
 * @author Martin Vysny
 */
public class QuizActivity extends Activity {

	private static final String INTENTKEY_JLPT_SET = "jlptset";
	private static final String INTENTKEY_STATE = "state";

	public static class State implements Serializable {
		private static final long serialVersionUID = 1L;
		public int currentQuestion = 0;
		public boolean showsAnswer = false;
		public int correctQuestions = 0;
		public Boolean isShowingRomaji = null;

		public void correctAnswer() {
			correctQuestions++;
			showsAnswer = false;
			currentQuestion++;
		}

		public void incorrectAnswer() {
			showsAnswer = false;
			currentQuestion++;
		}
	}

	public static void launch(final Activity a, final List<? extends DictEntry> questions) {
		if (questions.isEmpty()) {
			throw new IllegalArgumentException("No questions");
		}
		final Intent i = new Intent(a, QuizActivity.class);
		i.putExtra(INTENTKEY_JLPT_SET, (Serializable) questions);
		a.startActivity(i);
	}

	private List<? extends DictEntry> questions;
	private ShowRomaji showRomaji;
	private State state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jlpt_quiz);
		state = (State) getIntent().getSerializableExtra(INTENTKEY_STATE);
		if (state == null) {
			state = new State();
		}
		showRomaji = new ShowRomaji(this, state.isShowingRomaji) {

			@Override
			protected void show(boolean romaji) {
				updateControls();
			}
		};
		findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				state.correctAnswer();
				nextQuestion();
			}
		});
		findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				state.incorrectAnswer();
				nextQuestion();
			}
		});
		findViewById(R.id.showDetailed).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final DictEntry e = questions.get(state.currentQuestion);
				if (e instanceof KanjidicEntry) {
					KanjiDetailActivity.launch(QuizActivity.this, (KanjidicEntry) e);
				} else if (e instanceof EdictEntry) {
					EdictEntryDetailActivity.launch(QuizActivity.this, (EdictEntry) e);
				}
			}
		});
		findViewById(R.id.main).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (!state.showsAnswer) {
					state.showsAnswer = true;
					updateControls();
				}
			}
		});
		questions = (List<? extends DictEntry>) getIntent().getSerializableExtra(INTENTKEY_JLPT_SET);
		updateControls();
	}

	private void nextQuestion() {
		final boolean isFinished = state.currentQuestion >= questions.size();
		if (isFinished) {
			updateControls();
		} else {
			// we have to launch a new activity to preserve state of the quiz.
			// the state would get erased if the screen orientation is changed.
			final Intent intent = new Intent(this, QuizActivity.class);
			intent.putExtra(INTENTKEY_JLPT_SET, (Serializable) questions);
			state.isShowingRomaji = showRomaji.isShowingRomaji();
			intent.putExtra(INTENTKEY_STATE, state);
			startActivity(intent);
			finish();
		}
	}

// Preserve user-configured show-romaji setting
//	@Override
//	protected void onResume() {
//		super.onResume();
//		showRomaji.onResume();
//	}

	private void updateControls() {
		final boolean isFinished = state.currentQuestion >= questions.size();
		if (!isFinished) {
			final int vis = state.showsAnswer ? View.VISIBLE : View.INVISIBLE;
			final DictEntry e = questions.get(state.currentQuestion);
			if (e instanceof KanjidicEntry) {
				final KanjidicEntry ke = (KanjidicEntry) e;
				((TextView) findViewById(R.id.kanji)).setText(e.kanji);
				final TextView onyomi = (TextView) findViewById(R.id.onyomi);
				onyomi.setText(cs(ke.getOnyomi(), true));
				onyomi.setVisibility(vis);
				final TextView kunyomi = (TextView) findViewById(R.id.kunyomi);
				kunyomi.setText(cs(ke.getKunyomi(), true));
				kunyomi.setVisibility(vis);
				final TextView namae = (TextView) findViewById(R.id.namae);
				namae.setText(cs(ke.getNamae(), true));
				namae.setVisibility(vis);
				final TextView english = (TextView) findViewById(R.id.english);
				english.setText(cs(ke.getEnglish(), false));
				english.setVisibility(vis);
			} else {
				((TextView) findViewById(R.id.kanji)).setText(e.getJapanese());
				final TextView onyomi = (TextView) findViewById(R.id.onyomi);
				onyomi.setText(showRomaji.romanize(e.reading != null ? e.reading : ""));
				onyomi.setVisibility(vis);
				final TextView kunyomi = (TextView) findViewById(R.id.kunyomi);
				kunyomi.setVisibility(View.INVISIBLE);
				final TextView namae = (TextView) findViewById(R.id.namae);
				namae.setVisibility(View.INVISIBLE);
				final TextView english = (TextView) findViewById(R.id.english);
				english.setText(e.english);
				english.setVisibility(vis);
			}
		}
		final int vis = !isFinished && state.showsAnswer ? View.VISIBLE : View.INVISIBLE;
		findViewById(R.id.yes).setVisibility(vis);
		findViewById(R.id.no).setVisibility(vis);
		findViewById(R.id.showDetailed).setVisibility(vis);
		if (isFinished) {
			new DialogUtils(this).showInfoDialog(getString(R.string.results), AedictApp.format(R.string.youScored, state.correctQuestions, questions.size()));
		}
	}

	private String cs(final Collection<String> strings, final boolean isJapanese) {
		final ListBuilder b = new ListBuilder(", ");
		for (final String s : strings) {
			b.add(showRomaji.romanize(s));
		}
		return b.toString();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		showRomaji.register(menu);
		return true;
	}
}
