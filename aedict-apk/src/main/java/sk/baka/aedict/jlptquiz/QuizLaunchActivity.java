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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.R;
import sk.baka.aedict.dict.DictTypeEnum;
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
	private final Map<Integer, Integer> jlptMap = new HashMap<Integer, Integer>();
	{
		jlptMap.put(R.id.jlptLevel1, 1);
		jlptMap.put(R.id.jlptLevel2, 2);
		jlptMap.put(R.id.jlptLevel3, 3);
		jlptMap.put(R.id.jlptLevel4, 4);
		jlptMap.put(R.id.jlptLevel5, 5);
		jlptMap.put(R.id.jlptLevel6, 6);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!AedictApp.getDownloader().checkDic(this, DictTypeEnum.Kanjidic)){
			finish();
			return;
		}
		setContentView(R.layout.jlpt_quiz_launch);
		for (final Map.Entry<Integer, Integer> e : jlptMap.entrySet()) {
			final CheckBox cb = (CheckBox) findViewById(e.getKey());
			cb.setText(AedictApp.format(R.string.jlptLevelX, e.getValue()));
		}
		findViewById(R.id.launch).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final Set<Integer> jlpt = new HashSet<Integer>();
				for (final Map.Entry<Integer, Integer> e : jlptMap.entrySet()) {
					final CheckBox cb = (CheckBox) findViewById(e.getKey());
					if (cb.isChecked()) {
						jlpt.add(e.getValue());
					}
				}
				if (jlpt.isEmpty()) {
					return;
				}
				QuizActivity.launch(QuizLaunchActivity.this, jlpt);
			}
		});
	}
}
