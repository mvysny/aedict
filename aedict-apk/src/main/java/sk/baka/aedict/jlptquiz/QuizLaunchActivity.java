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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.Dictionary;
import sk.baka.aedict.dict.KanjidicEntry;
import sk.baka.aedict.dict.LuceneSearch;
import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.KanjiUtils;
import sk.baka.aedict.kanji.KanjiUtils.KanjiQuiz;
import sk.baka.autils.AbstractTask;
import sk.baka.autils.AndroidUtils;
import sk.baka.autils.MiscUtils;
import sk.baka.autils.Progress;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

/**
 * Allows user to select JLPT levels to test and start the quiz.
 * 
 * @author Martin Vysny
 */
public class QuizLaunchActivity extends Activity {
	/**
	 * Maps checkbox component ID to JLPT level.
	 */
	private final Map<Integer, KanjiQuiz> jlptMap = new HashMap<Integer, KanjiQuiz>();
	{
		jlptMap.put(R.id.jlptLevel1, KanjiQuiz.JlptN1);
		jlptMap.put(R.id.jlptLevel2, KanjiQuiz.JlptN2);
		jlptMap.put(R.id.jlptLevel3, KanjiQuiz.JlptN3);
		jlptMap.put(R.id.jlptLevel4, KanjiQuiz.JlptN4);
		jlptMap.put(R.id.jlptLevel5, KanjiQuiz.JlptN5);
		jlptMap.put(R.id.quizCommonNewspaper, KanjiQuiz.MostFrequentKanjisInNewspaper);
		jlptMap.put(R.id.joyo1, KanjiQuiz.JoyoGrade1);
		jlptMap.put(R.id.joyo2, KanjiQuiz.JoyoGrade2);
		jlptMap.put(R.id.joyo3, KanjiQuiz.JoyoGrade3);
		jlptMap.put(R.id.joyo4, KanjiQuiz.JoyoGrade4);
		jlptMap.put(R.id.joyo5, KanjiQuiz.JoyoGrade5);
		jlptMap.put(R.id.joyo6, KanjiQuiz.JoyoGrade6);
		jlptMap.put(R.id.joyoJuniorHighSchool, KanjiQuiz.JoyoJuniorHighSchool);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!AedictApp.getDownloader().checkDictionary(this, new Dictionary(DictTypeEnum.Kanjidic, null), null, false)) {
			finish();
			return;
		}
		setContentView(R.layout.jlpt_quiz_launch);
		findViewById(R.id.launch).setOnClickListener(AndroidUtils.safe(this, new View.OnClickListener() {

			@SuppressWarnings("unchecked")
			public void onClick(View v) {
				final Set<KanjiQuiz> jlpt = new HashSet<KanjiQuiz>();
				for (final Map.Entry<Integer, KanjiQuiz> e : jlptMap.entrySet()) {
					final CheckBox cb = (CheckBox) findViewById(e.getKey());
					if (cb.isChecked()) {
						jlpt.add(e.getValue());
					}
				}
				if (jlpt.isEmpty()) {
					return;
				}
				new QuestionGenerator().execute(QuizLaunchActivity.this, jlpt);
			}
		}));
	}

	private class QuestionGenerator extends AbstractTask<Set<KanjiQuiz>, List<KanjidicEntry>>{
		@Override
		public List<KanjidicEntry> impl(Set<KanjiQuiz>... params) throws Exception {
			publish(new Progress("Generating flashcards",0,QUIZ_QUESTION_COUNT));
			final StringBuilder kanjiPool = new StringBuilder();
			for (final KanjiQuiz level : params[0]) {
				kanjiPool.append(KanjiUtils.QUIZ_TABLE.get(level));
			}
			final List<KanjidicEntry> questions = new ArrayList<KanjidicEntry>();
			final Random r = new Random();
			final LuceneSearch search = new LuceneSearch(DictTypeEnum.Kanjidic, null, true);
			try {
				for (int i = 0; i < QUIZ_QUESTION_COUNT; i++) {
					publish(new Progress(null,i,QUIZ_QUESTION_COUNT));
					if (kanjiPool.length() == 0) {
						break;
					}
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
			return questions;
		}

		@Override
		protected void cleanupAfterError(Exception ex) {
			// nothing to do
		}

		@Override
		protected void onSucceeded(List<KanjidicEntry> result) {
			QuizActivity.launch(QuizLaunchActivity.this, result);
		}
	}
	
	private static final int QUIZ_QUESTION_COUNT = 20;
}
