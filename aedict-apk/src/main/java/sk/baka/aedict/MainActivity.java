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

import java.util.List;

import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.DictTypeEnum;
import sk.baka.aedict.dict.DownloaderService;
import sk.baka.aedict.dict.Edict;
import sk.baka.aedict.kanji.RomanizationEnum;
import sk.baka.aedict.util.SearchUtils;
import sk.baka.aedict.util.ShowRomaji;
import sk.baka.autils.DialogUtils;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

/**
 * Provides means to search the edict dictionary file.
 * 
 * @author Martin Vysny
 */
public class MainActivity extends ListActivity {
	private ShowRomaji showRomaji;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		showRomaji = new ShowRomaji(this) {

			@Override
			protected void show(boolean romaji) {
				((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
			}
		};
		final SearchUtils utils = new SearchUtils(this);
		utils.registerSearch(R.id.exactMatch, R.id.jpDeinflectVerbs, null, R.id.searchEdit, false, R.id.jpSearch, true);
		utils.registerSearch(R.id.exactMatch, null, R.id.searchExamples, R.id.searchEdit, false, R.id.englishSearch, false);
		utils.setupAnalysisControls(R.id.btnJpTranslate, R.id.txtJpTranslate, true);
		findViewById(R.id.advanced).setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				final View g = findViewById(R.id.advancedPanel);
				g.setVisibility(g.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
			}
		});
		// check for dictionary file and download it if it is missing.
		bindService(new Intent(this, DownloaderService.class), new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder service) {
				((DownloaderService.LocalBinder) service).getService().checkDic(MainActivity.this, DictTypeEnum.Edict);
			}

			public void onServiceDisconnected(ComponentName className) {
			}
		}, Context.BIND_AUTO_CREATE);
		if (!AedictApp.isInstrumentation) {
			new DialogUtils(this).showInfoOnce(AedictApp.getVersion(), AedictApp.format(R.string.whatsNew, AedictApp.getVersion()), getString(R.string.whatsNewText));
		}
		findViewById(R.id.intro).setVisibility(getModel().isEmpty() ? View.VISIBLE : View.GONE);
		findViewById(R.id.recentlyViewed).setVisibility(getModel().isEmpty() ? View.GONE : View.VISIBLE);
		((TextView) findViewById(R.id.aedict)).setText("Aedict " + AedictApp.getVersion());
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		showRomaji.register(menu);
		AbstractActivity.addMenuItems(this, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		modelCache = null;
		setModel();
		showRomaji.onResume();
	}

	private List<DictEntry> modelCache = null;

	private List<DictEntry> getModel() {
		if (modelCache == null) {
			modelCache = NotepadActivity.deserialize(AedictApp.getConfig().getRecentlyViewed());
		}
		return modelCache;
	}

	public static void recentlyViewed(final DictEntry entry) {
		final List<DictEntry> entries = NotepadActivity.deserialize(AedictApp.getConfig().getRecentlyViewed());
		while (entries.size() > 20) {
			entries.remove(entries.size() - 1);
		}
		entries.remove(entry);
		entries.add(0, entry);
		AedictApp.getConfig().setRecentlyViewed(NotepadActivity.serialize(entries));
	}

	/**
	 * Sets the ListView model.
	 */
	private void setModel() {
		final RomanizationEnum romanization = AedictApp.getConfig().getRomanization();
		setListAdapter(new ArrayAdapter<DictEntry>(this, android.R.layout.simple_list_item_2, getModel()) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TwoLineListItem view = (TwoLineListItem) convertView;
				if (view == null) {
					view = (TwoLineListItem) getLayoutInflater().inflate(android.R.layout.simple_list_item_2, getListView(), false);
				}
				Edict.print(getModel().get(position), view, showRomaji.isShowingRomaji() ? romanization : null);
				return view;
			}

		});
	}
}