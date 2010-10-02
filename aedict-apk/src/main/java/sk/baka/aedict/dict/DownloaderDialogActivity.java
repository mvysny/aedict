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
package sk.baka.aedict.dict;

import sk.baka.aedict.AedictApp;
import sk.baka.aedict.DownloadActivity;
import sk.baka.aedict.dict.DownloaderService.AbstractDownloader;
import sk.baka.aedict.util.DialogActivity;

public class DownloaderDialogActivity extends DialogActivity {
	static final String KEY_DOWNLOADER = "downloader";

	@Override
	protected void onPositiveClick() {
		final AbstractDownloader d = (AbstractDownloader) getValues().get(
				KEY_DOWNLOADER);
		AedictApp.getDownloader().download(d);
		DownloadActivity.launch(this);
	}
}
