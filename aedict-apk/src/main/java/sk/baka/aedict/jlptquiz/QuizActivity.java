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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.KanjiDetailActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.KanjidicEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;
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
	private static final int QUIZ_QUESTION_COUNT = 20;

	public static void launch(final Activity a, final Set<Integer> jlpt) {
		final Intent i = new Intent(a, QuizActivity.class);
		i.putExtra(INTENTKEY_JLPT_SET, (Serializable) jlpt);
		a.startActivity(i);
	}

	private Set<Integer> jlpt;
	private List<KanjidicEntry> questions;
	private int currentQuestion = 0;
	private boolean showsAnswer = false;
	private int correctQuestions = 0;
	private ShowRomaji showRomaji;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.jlpt_quiz);
		showRomaji = new ShowRomaji(this) {

			@Override
			protected void show(boolean romaji) {
				updateControls();
			}
		};
		findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				correctQuestions++;
				showsAnswer = false;
				currentQuestion++;
				updateControls();
			}
		});
		findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showsAnswer = false;
				currentQuestion++;
				updateControls();
			}
		});
		findViewById(R.id.showDetailed).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				KanjiDetailActivity.launch(QuizActivity.this, questions.get(currentQuestion));
			}
		});
		findViewById(R.id.main).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (!showsAnswer) {
					showsAnswer = true;
					updateControls();
				}
			}
		});
		try {
			jlpt = (Set<Integer>) getIntent().getSerializableExtra(INTENTKEY_JLPT_SET);
			final StringBuilder kanjiPool = new StringBuilder();
			for (final Integer level : jlpt) {
				kanjiPool.append(KanjiUtils.getJlptKanjis(level));
			}
			questions = new ArrayList<KanjidicEntry>();
			final Random r = new Random();
			final LuceneSearch search = new LuceneSearch(DictTypeEnum.Kanjidic, null, true);
			try {
				for (int i = 0; i < QUIZ_QUESTION_COUNT; i++) {
					final int index = r.nextInt(kanjiPool.length());
					final char kanji = kanjiPool.charAt(index);
					kanjiPool.deleteCharAt(index);
					final List<DictEntry> result = search.search(SearchQuery.kanjiSearch(kanji, null, null));
					for (final Iterator<? extends DictEntry> it = result.iterator(); it.hasNext();) {
						final DictEntry e = it.next();
						if (!e.isValid()) {
							throw new RuntimeException(e.english);
						}
					}
					questions.add((KanjidicEntry) result.get(0));
				}
			} finally {
				MiscUtils.closeQuietly(search);
			}
			currentQuestion = 0;
			correctQuestions = 0;
			showsAnswer = false;
			updateControls();
		} catch (Exception ex) {
			AndroidUtils.handleError(ex, this, QuizActivity.class, null);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		showRomaji.onResume();
	}

	private void updateControls() {
		final boolean isFinished = currentQuestion >= questions.size();
		if (!isFinished) {
			final int vis = showsAnswer ? View.VISIBLE : View.INVISIBLE;
			final KanjidicEntry ke = questions.get(currentQuestion);
			((TextView) findViewById(R.id.kanji)).setText(ke.kanji);
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
		}
		final int vis = !isFinished && showsAnswer ? View.VISIBLE : View.INVISIBLE;
		findViewById(R.id.yes).setVisibility(vis);
		findViewById(R.id.no).setVisibility(vis);
		findViewById(R.id.showDetailed).setVisibility(vis);
		if (isFinished) {
			new DialogUtils(this).showInfoDialog(getString(R.string.results), AedictApp.format(R.string.youScored, correctQuestions, QUIZ_QUESTION_COUNT));
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
