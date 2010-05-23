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

import java.util.Collection;
import java.util.List;

import se.fnord.android.layout.FlowLayout;
import sk.baka.aedict.KanjiDetailActivity.TanakaSearchTask;
import sk.baka.aedict.dict.Edict;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.ListBuilder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Shows detailed info about a single EDICT entry.
 * 
 * @author Martin Vysny
 */
public class EdictEntryDetailActivity extends AbstractActivity {

	public static final String INTENTKEY_ENTRY = "entry";

	public static void launch(final Activity a, final EdictEntry entry) {
		final Intent i = new Intent(a, EdictEntryDetailActivity.class);
		i.putExtra(INTENTKEY_ENTRY, entry);
		a.startActivity(i);
	}

	private EdictEntry entry;
	private ShowRomaji showRomaji;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edict_entry_detail);
		entry = (EdictEntry) getIntent().getSerializableExtra(INTENTKEY_ENTRY);
		showRomaji = new ShowRomaji(this) {

			@Override
			protected void show(boolean romaji) {
				displayEntry();
				tanakaSearchTask.updateModel();
			}
		};
		((TextView) findViewById(R.id.kanji)).setText(entry.kanji);
		new KanjiDetailActivity.SearchClickListener(this, entry.kanji, true).registerTo(findViewById(R.id.kanji));
		displayEntry();
		new SearchUtils(this).setupCopyButton(R.id.copy, R.id.kanji);
		findViewById(R.id.analyze).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				KanjiAnalyzeActivity.launch(EdictEntryDetailActivity.this, entry.kanji, false);
			}
		});
		findViewById(R.id.addToNotepad).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				NotepadActivity.addAndLaunch(EdictEntryDetailActivity.this, entry);
			}
		});
	}

	private void displayEntry() {
		((TextView) findViewById(R.id.kana)).setText(showRomaji.romanize(entry.reading));
		new KanjiDetailActivity.SearchClickListener(this, entry.reading, true).registerTo(findViewById(R.id.kana));
		// display the markings
		final List<String> markings = entry.getMarkings();
		final ViewGroup senseGroup = (ViewGroup) findViewById(R.id.entryDetails);
		senseGroup.removeAllViews();
		final TextView marking = new TextView(this);
		marking.setTextColor(0xFFFFFFFF);
		marking.setText(csv(markings));
		marking.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		new KanjiDetailActivity.FocusVisual().registerTo(marking);
		senseGroup.addView(marking);
		senseGroup.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final ListBuilder sb = new ListBuilder("\n");
				for (final Edict.Marking m : Edict.getMarkings(markings)) {
					sb.add(m.mark + '\t' + getString(m.descriptionRes));
				}
				new DialogUtils(EdictEntryDetailActivity.this).showInfoDialog(null, sb.toString());
			}
		});
		// display the senses
		final List<List<String>> senses = entry.getSenses();
		for (int i = 0; i < senses.size(); i++) {
			final FlowLayout layout = new FlowLayout(this);
			layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			TextView tv = new TextView(this);
			tv.setText("(" + (i + 1) + ") ");
			layout.addView(tv);
			for (int j = 0; j < senses.get(i).size(); j++) {
				final String sense = senses.get(i).get(j);
				tv = new TextView(this);
				tv.setTextColor(0xFFFFFFFF);
				new KanjiDetailActivity.SearchClickListener(this, sense, false).registerTo(tv);
				layout.addView(tv);
				tv.setText(sense + (j == senses.get(i).size() - 1 ? "" : ", "));
			}
			senseGroup.addView(layout);
		}
	}

	public static String csv(final Collection<?> objs) {
		final ListBuilder lb = new ListBuilder(", ");
		for (final Object obj : objs) {
			lb.add(obj.toString());
		}
		return lb.toString();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		showRomaji.register(menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		showRomaji.onResume();
		if (tanakaSearchTask == null && entry.isValid()) {
			tanakaSearchTask = new TanakaSearchTask(this, (ViewGroup) findViewById(R.id.tanakaExamples), showRomaji, entry.getJapanese());
			tanakaSearchTask.execute(entry.getJapanese());
		}
	}

	private TanakaSearchTask tanakaSearchTask;

	@Override
	protected void onStop() {
		if (tanakaSearchTask.cancel(true)) {
			tanakaSearchTask = null;
		}
		super.onStop();
	}
}
