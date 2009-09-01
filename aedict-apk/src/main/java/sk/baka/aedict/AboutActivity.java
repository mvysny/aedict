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

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Shows the About dialog.
 * 
 * @author Martin Vysny
 */
public final class AboutActivity extends Activity {
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.about);
		final TextView appNameText = (TextView) findViewById(R.id.aboutAppName);
		final String appName = getAppName();
		appNameText.setText(appName);
	}

	private String getAppName() {
		final StringBuilder b = new StringBuilder();
		b.append(getString(R.string.app_name));
		b.append(" v");
		b.append(AedictApp.getVersion());
		return b.toString();
	}
}
