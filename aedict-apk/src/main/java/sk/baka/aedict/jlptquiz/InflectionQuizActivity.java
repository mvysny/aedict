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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.kanji.VerbInflection;
import sk.baka.autils.DialogUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Shows a verb conjugation quiz.
 * 
 * @author Martin Vysny
 */
public class InflectionQuizActivity extends Activity {
	public static final String INTENTKEY_ENTRY = "entry";

	public static void launch(final Activity a, final EdictEntry e) {
		if (!e.isVerb()) {
			throw new IllegalArgumentException(e + " is not verb");
		}
		final Intent i = new Intent(a, InflectionQuizActivity.class);
		i.putExtra(INTENTKEY_ENTRY, e);
		a.startActivity(i);
	}

	private EdictEntry entry;
	private final Random r = new Random();
	private int currentQuestion = 0;
	private int correctAnswer = 0;
	private boolean showingAnswer = false;
	private int correctlyAnsweredCount = 0;
	/**
	 * A list of all possible conjugations. Contains pairs of [conjugated verb
	 * in kanji+kana, english explanation].
	 */
	private List<String[]> model;

	private void recomputeModel() {
		model = new ArrayList<String[]>();
		final boolean isIchidan = entry.isIchidan();
		// now, add all possible inflections
		for (final VerbInflection.Form form : VerbInflection.Form.values()) {
			if (isIchidan && !form.appliesToIchidan()) {
				// filter out forms not applicable to ichidan verbs
				continue;
			}
			final String inflected = RomanizationEnum.NihonShiki.toHiragana(form.inflect(RomanizationEnum.NihonShiki.toRomaji(entry.reading), isIchidan));
			final String explanation = getString(form.explanationResId);
			model.add(new String[] { inflected, explanation });
		}
		Collections.shuffle(model);
		final int questions = Math.min(MAX_NUMBER_OF_QUESTIONS, model.size() / NO_OF_OPTIONS);
		model = model.subList(0, questions * NO_OF_OPTIONS);
		currentQuestion = 0;
		correctAnswer = r.nextInt(NO_OF_OPTIONS);
		showingAnswer = false;
		correctlyAnsweredCount = 0;
	}

	private static final int MAX_NUMBER_OF_QUESTIONS = 10;
	private static final int NO_OF_OPTIONS = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inflection_quiz);
		entry = (EdictEntry) getIntent().getSerializableExtra(INTENTKEY_ENTRY);
		recomputeModel();
		updateGui();
		findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (!showingAnswer) {
					if (getSelected() == correctAnswer) {
						correctlyAnsweredCount++;
					}
				}
				showingAnswer = !showingAnswer;
				if (!showingAnswer) {
					currentQuestion += NO_OF_OPTIONS;
					correctAnswer = r.nextInt(NO_OF_OPTIONS);
				}
				if (currentQuestion >= model.size()) {
					findViewById(R.id.next).setEnabled(false);
					new DialogUtils(InflectionQuizActivity.this).showInfoDialog(getString(R.string.results), AedictApp.format(R.string.youScored, correctlyAnsweredCount, model.size() / NO_OF_OPTIONS));
				} else {
					updateGui();
				}
			}
		});
	}

	private void updateGui() {
		final String[] answers = new String[NO_OF_OPTIONS];
		for (int i = 0; i < NO_OF_OPTIONS; i++) {
			answers[i] = model.get(i + currentQuestion)[1];
		}
		final String tmp = answers[correctAnswer];
		answers[correctAnswer] = answers[0];
		answers[0] = tmp;
		((TextView) findViewById(R.id.question)).setText(model.get(currentQuestion)[0]);
		for (final Map.Entry<Integer, Integer> e : OPTION_TO_ID.entrySet()) {
			final RadioButton btn = (RadioButton) findViewById(e.getValue());
			btn.setEnabled(showingAnswer ? e.getKey() == correctAnswer : true);
			btn.setText(answers[e.getKey()]);
		}
		if (!showingAnswer) {
			final RadioButton btn = (RadioButton) findViewById(R.id.option1);
			btn.setChecked(true);
		}
	}

	private static final Map<Integer, Integer> OPTION_TO_ID = new HashMap<Integer, Integer>();
	static {
		OPTION_TO_ID.put(0, R.id.option1);
		OPTION_TO_ID.put(1, R.id.option2);
		OPTION_TO_ID.put(2, R.id.option3);
	}

	private int getSelected() {
		for (final Map.Entry<Integer, Integer> e : OPTION_TO_ID.entrySet()) {
			final RadioButton btn = (RadioButton) findViewById(e.getValue());
			if (btn.isChecked()) {
				return e.getKey();
			}
		}
		throw new AssertionError();
	}
}
