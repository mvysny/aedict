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

import sk.baka.aedict.dict.KanjidicEntry;
import android.content.Intent;

/**
 * Tests the {@link KanjiDetailActivity} class
 * 
 * @author Martin Vysny
 * 
 */
public class KanjiDetailActivityTest extends AbstractAedictTest<KanjiDetailActivity> {

	public KanjiDetailActivityTest() {
		super(KanjiDetailActivity.class);
	}

	private void launch(final KanjidicEntry entry) {
		final Intent intent = new Intent(getInstrumentation().getContext(), KanjiDetailActivity.class);
		intent.putExtra(KanjiDetailActivity.INTENTKEY_KANJIDIC_ENTRY, entry);
		tester.startActivity(intent);
	}

	public void testSimpleActivityStart() {
		final KanjidicEntry e = new KanjidicEntry("K", "Reading", "English", 1, 2, "3-4-5", 6);
		launch(e);
		tester.assertText(R.id.kanji, "K");
		tester.assertText(R.id.stroke, "2");
		tester.assertText(R.id.grade, "6");
		tester.assertText(R.id.skip, "3-4-5");
		tester.assertText(R.id.jlpt, "-");
	}
	
	public void testOnyomiKunyomiNamae() {
		final KanjidicEntry e = new KanjidicEntry("愛", "アイ, いと.しい, かな.しい, め.でる, お.しむ, まな, [あ, あし, え, かな, なる, めぐ, めぐみ, よし, ちか]", "love, affection, favourite", 1, 2, "3-4-5", 6);
		launch(e);
		tester.assertText(R.id.onyomi, "Onyomi: アイ");
		tester.assertText(R.id.kunyomi, "Kunyomi: いと.しい, かな.しい, め.でる, お.しむ, まな");
		tester.assertText(R.id.namae, "Namae: あ, あし, え, かな, なる, めぐ, めぐみ, よし, ちか");
	}

	public void testAddToNotepad() {
		testSimpleActivityStart();
		tester.click(R.id.addToNotepad);
		tester.assertRequestedActivity(NotepadActivity.class);
		assertEquals(new KanjidicEntry("K", "Reading", "English", 1, 2, "3-4-5", 6), getStartedActivityIntent().getSerializableExtra(NotepadActivity.INTENTKEY_ADD_ENTRY));
	}

	public void testClickOnKanjiStartsSearch() {
		testSimpleActivityStart();
		tester.click(R.id.kanji);
		tester.assertRequestedActivity(MainActivity.class);
		assertEquals("K", getStartedActivityIntent().getStringExtra(MainActivity.INTENTKEY_PREFILL_SEARCH_FIELD));
	}
}
