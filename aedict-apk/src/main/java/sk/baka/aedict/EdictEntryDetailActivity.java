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

import sk.baka.aedict.dict.Edict;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.TanakaSearchTask;
import sk.baka.aedict.util.Check;
import sk.baka.aedict.util.FocusVisual;
import sk.baka.aedict.util.SearchClickListener;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.aedict.util.SpanStringBuilder;
import sk.baka.autils.DialogUtils;
import sk.baka.autils.ListBuilder;
import sk.baka.autils.MiscUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
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

	static final String INTENTKEY_ENTRY = "entry";

	/**
	 * Launches this activity.
	 * 
	 * @param a
	 *            callee activity, not null.
	 * @param entry
	 *            show this entry, not null.
	 */
	public static void launch(final Activity a, final EdictEntry entry) {
		Check.checkNotNull("a", a);
		Check.checkNotNull("entry", entry);
		Check.checkTrue("entry is not valid", entry.isValid());
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
		MainActivity.recentlyViewed(entry);
		showRomaji = new ShowRomaji(this) {

			@Override
			protected void show(boolean romaji) {
				displayEntry();
				tanakaSearchTask.updateModel();
			}
		};
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
		final TextView kanji = ((TextView) findViewById(R.id.kanji));
		kanji.setText(showRomaji.getJapanese(entry));
		new SearchClickListener(this, entry.getJapanese(), true).registerTo(kanji);
		final TextView kana = ((TextView) findViewById(R.id.kana));
		if (MiscUtils.isBlank(entry.kanji)) {
			kana.setVisibility(View.GONE);
		} else {
			kana.setText(showRomaji.romanize(entry.reading));
			new SearchClickListener(this, entry.reading, true).registerTo(kana);
		}
		// display the markings
		final List<String> markings = entry.getMarkings();
		final TextView marking = (TextView) findViewById(R.id.entryMarkings);
		marking.setText(csv(markings));
		new FocusVisual().registerTo(marking);
		marking.setOnClickListener(new View.OnClickListener() {

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
			final SpanStringBuilder sb = new SpanStringBuilder();
			sb.append(sb.newForeground(0xFF777777), "(" + (i + 1) + ") ");
			for (int j = 0; j < senses.get(i).size(); j++) {
				final String sense = senses.get(i).get(j);
				final Object span = sb.newClickable(new SearchClickListener(this, sense, false));
				sb.append(span, sense);
				if (j < senses.get(i).size() - 1) {
					sb.append(", ");
				}
			}
			final TextView s = (TextView) findViewById(R.id.entrySenses);
			s.setMovementMethod(new LinkMovementMethod());
			s.setText(sb);
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
		showRomaji.register(menu);
		AbstractActivity.addMenuItems(this, menu);
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
