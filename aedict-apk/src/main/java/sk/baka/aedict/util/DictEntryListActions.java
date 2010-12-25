package sk.baka.aedict.util;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.CopyActivity;
import sk.baka.aedict.KanjiAnalyzeActivity;
import sk.baka.aedict.MainActivity;
import sk.baka.aedict.NotepadActivity;
import sk.baka.aedict.R;
import sk.baka.aedict.StrokeOrderActivity;
import sk.baka.aedict.TanakaAnalyzeActivity;
import sk.baka.aedict.VerbInflectionActivity;
import sk.baka.aedict.dict.DictEntry;
import sk.baka.aedict.dict.EdictEntry;
import sk.baka.aedict.dict.TanakaDictEntry;
import sk.baka.aedict.jlptquiz.InflectionQuizActivity;
import sk.baka.autils.AndroidUtils;
import android.app.Activity;
import android.content.Context;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Adds default actions to a list of {@link DictEntry}s.
 * @author Martin Vysny
 */
public class DictEntryListActions {
	public final Activity activity;
	public final boolean canAddToNotepad;
	public final boolean canDeleteItems;
	public final boolean canSearchFurther;
	public final boolean canAnalyze;
	
	public DictEntryListActions(Activity activity, final boolean canAnalyze, final boolean canAddToNotepad, final boolean canDeleteItems, final boolean canSearchFurther) {
		this.activity = activity;
		this.canAnalyze = canAnalyze;
		this.canAddToNotepad = canAddToNotepad;
		this.canDeleteItems = canDeleteItems;
		this.canSearchFurther = canSearchFurther;
	}
	
	public DictEntryListActions register(final ListView lv) {
		lv.setOnCreateContextMenuListener(AndroidUtils.safe(activity, new View.OnCreateContextMenuListener() {

			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				final int position = ((AdapterContextMenuInfo) menuInfo).position;
				final DictEntry ee = (DictEntry) lv.getAdapter().getItem(position);
				register(menu, ee, position);
			}
		}));
		return this;
	}
	
	public DictEntryListActions register(final ContextMenu menu, final DictEntry entry, final int itemIndex) {
		if (canAnalyze) {
			menu.add(0, 0, 0, R.string.analyze).setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

				public boolean onMenuItemClick(MenuItem item) {
					if (entry instanceof TanakaDictEntry) {
						final TanakaDictEntry e = (TanakaDictEntry) entry;
						if (e.wordList != null && !e.wordList.isEmpty()) {
							TanakaAnalyzeActivity.launch(activity, e);
							return true;
						}
					}
					KanjiAnalyzeActivity.launch(activity, entry.getJapanese(), false);
					return true;
				}
			}));
		}
		if (canAddToNotepad) {
			final MenuItem miAddToNotepad = menu.add(Menu.NONE, 1, 1, R.string.addToNotepad);
			miAddToNotepad.setOnMenuItemClickListener(AndroidUtils.safe(
					activity, new MenuItem.OnMenuItemClickListener() {

						public boolean onMenuItemClick(MenuItem item) {
							NotepadActivity.addAndLaunch(activity, entry);
							return true;
						}
					}));
		}
		final MenuItem miShowSOD = menu.add(Menu.NONE, 6, 6, R.string.showSod);
		miShowSOD.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				StrokeOrderActivity.launch(activity, entry.getJapanese());
				return true;
			}
		}));
		if (EdictEntry.fromEntry(entry).isVerb()) {
			final MenuItem miShowConjugations = menu.add(Menu.NONE, 7, 7, R.string.showConjugations);
			miShowConjugations.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

				public boolean onMenuItemClick(MenuItem item) {
					VerbInflectionActivity.launch(activity, EdictEntry.fromEntry(entry));
					return true;
				}
			}));
			final MenuItem miConjugationQuiz = menu.add(Menu.NONE, 8, 8, R.string.conjugationQuiz);
			miConjugationQuiz.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

				public boolean onMenuItemClick(MenuItem item) {
					InflectionQuizActivity.launch(activity, EdictEntry.fromEntry(entry));
					return true;
				}
			}));
		}
		if (canDeleteItems) {
			menu.add(Menu.NONE, 9, 9, R.string.delete).setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {
	
				public boolean onMenuItemClick(MenuItem item) {
					onDelete(itemIndex);
					return true;
				}
			}));
			menu.add(Menu.NONE, 10, 10, R.string.deleteAll).setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {
	
				public boolean onMenuItemClick(MenuItem item) {
					onDeleteAll();
					return true;
				}
			}));
		}
		final MenuItem miCopyToClipboard = menu.add(Menu.NONE, 11, 11, R.string.copyToClipboard);
		miCopyToClipboard.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				final ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
				cm.setText(entry.getJapanese());
				final Toast toast = Toast.makeText(activity, AedictApp.format(R.string.copied, entry.getJapanese()), Toast.LENGTH_SHORT);
				toast.show();
				return true;
			}
		}));
		if (canSearchFurther) {
			final MenuItem miSearchFurther = menu.add(Menu.NONE, 12, 12, R.string.searchFurther);
			miSearchFurther.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {
	
				public boolean onMenuItemClick(MenuItem item) {
					MainActivity.launch(activity, entry.getJapanese());
					return true;
				}
			}));
		}
		final MenuItem miAdvancedCopy = menu.add(Menu.NONE, 13, 13, R.string.advancedCopy);
		miAdvancedCopy.setOnMenuItemClickListener(AndroidUtils.safe(activity, new MenuItem.OnMenuItemClickListener() {

			public boolean onMenuItemClick(MenuItem item) {
				CopyActivity.launch(activity, entry);
				return true;
			}
		}));
		addCustomItems(menu, entry, itemIndex);
		return this;
	}
	
	protected void addCustomItems(ContextMenu menu, DictEntry entry, int itemIndex) {}
	protected void onDelete(final int itemIndex){}
	protected void onDeleteAll(){}
}
