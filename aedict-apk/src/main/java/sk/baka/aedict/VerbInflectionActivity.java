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

import java.util.ArrayList;
import java.util.List;

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.kanji.VerbInflection;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TwoLineListItem;

/**
 * Shows possible verb inflections, with examples.
 * 
 * @author Martin Vysny
 */
public class VerbInflectionActivity extends ListActivity {
	/**
	 * Expects {@link DictEntry} to be present in the Intent.
	 */
	public static final String INTENTKEY_ENTRY = "entry";
	private DictEntry entry;
	private List<String> model;
	/**
	 * true if romaji is shown instead of katakana/hiragana.
	 */
	private boolean isShowingRomaji;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isShowingRomaji = AedictApp.getConfig().isUseRomaji();
		entry = (DictEntry) getIntent().getSerializableExtra(INTENTKEY_ENTRY);
		final boolean isIchidan = entry.isIchidan();
		model = new ArrayList<String>();
		for (final VerbInflection.AbstractBaseInflector inflector : VerbInflection.INFLECTORS) {
			model.add(convert(inflector.inflect(entry.reading, isIchidan)));
		}
		for (final VerbInflection.Form form : VerbInflection.ALL_FORMS) {
			model.add(convert(form.inflect(entry.reading, isIchidan)));
		}
		getListView().setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, model) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TwoLineListItem view = (TwoLineListItem) convertView;
				if (view == null) {
					view = (TwoLineListItem) getLayoutInflater().inflate(android.R.layout.simple_list_item_2, getListView(), false);
				}
				view.getText1().setText(model.get(position));
				return view;
			}

		});
	}

	private String convert(final String romaji) {
		final String kana = RomanizationEnum.NihonShiki.toHiragana(romaji);
		return isShowingRomaji ? AedictApp.getConfig().getRomanization().toRomaji(kana) : kana;
	}
}
