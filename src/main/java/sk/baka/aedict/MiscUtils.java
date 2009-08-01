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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.util.Log;

/**
 * Contains misc utility methods.
 * 
 * @author Martin Vysny
 */
public final class MiscUtils {
	private MiscUtils() {
		throw new AssertionError();
	}

	/**
	 * Closes given closeable quietly. Any errors are logged as warnings to the
	 * android log.
	 * 
	 * @param in
	 *            the closeable to close. Nothing is done if null.
	 */
	public static void closeQuietly(Closeable in) {
		if (in == null) {
			return;
		}
		try {
			in.close();
		} catch (Exception ex) {
			Log.w(ResultActivity.class.getSimpleName(),
					"Failed to close closeable", ex);
		}
	}

	/**
	 * Opens a class-loader resource. Fails if the resource does not exist.
	 * 
	 * @param resName
	 *            the resource name
	 * @param cl
	 *            class-loader to use
	 * @return an opened stream
	 * @throws IOException
	 *             if the resource does not exist.
	 */
	public static InputStream openResource(String resName, final ClassLoader cl)
			throws IOException {
		final InputStream result = cl.getResourceAsStream(resName);
		if (result == null) {
			throw new IOException("Failed to load resource '" + resName + "'");
		}
		return result;
	}

	/**
	 * Checks if given string is null, empty or whitespace-only.
	 * 
	 * @param str
	 *            the string to check
	 * @return true if given string is null, empty or consists of whitespaces
	 *         only.
	 */
	public static boolean isBlank(final String str) {
		return str == null || str.trim().length() == 0;
	}

	/**
	 * Returns stacktrace of given exception.
	 * 
	 * @param ex
	 *            the exception
	 * @return stacktrace as string.
	 */
	public static String getStacktrace(Exception ex) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace();
		pw.close();
		return sw.toString();
	}
}
