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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

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
	 * Checks if given string array is null, empty or contains whitespace-only
	 * strings only.
	 * 
	 * @param str
	 *            the string to check
	 * @return true if given string is null, empty or consists of whitespaces
	 *         only.
	 */
	public static boolean isBlank(final String[] str) {
		if (str == null || str.length == 0) {
			return true;
		}
		for (final String s : str) {
			if (!isBlank(s)) {
				return false;
			}
		}
		return true;
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

	/**
	 * Loads property file from given input stream. The stream is always closed.
	 * 
	 * @param in
	 *            the stream to read from
	 * @return properties instance
	 * @throws IOException
	 *             if i/o error occurs
	 */
	public static Properties load(final InputStream in) throws IOException {
		try {
			final Properties result = new Properties();
			result.load(in);
			return result;
		} finally {
			closeQuietly(in);
		}
	}

	/**
	 * Deletes given directory, including subdirectories.
	 * 
	 * @param dir
	 *            the directory to delete. For safety reasons it must start with
	 *            /sdcard/aedict
	 * @throws IOException
	 */
	public static void deleteDir(final File dir) throws IOException {
		if (!dir.getPath().startsWith("/sdcard/aedict")) {
			throw new IllegalArgumentException(dir
					+ " does not start with /sdcard/aedict");
		}
		if (!dir.exists()) {
			return;
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir + " is not a directory");
		}
		for (final File f : dir.listFiles()) {
			if (f.isDirectory()) {
				deleteDir(f);
			} else {
				if (!f.delete()) {
					throw new IOException("Failed to delete "
							+ f.getAbsolutePath());
				}
			}
		}
		if (!dir.delete()) {
			throw new IOException("Failed to delete " + dir.getAbsolutePath());
		}
	}

	/**
	 * Returns length in bytes of given file or directory.
	 * 
	 * @param dir
	 *            the directory to list. A directory length is set to be 4kb +
	 *            lengths of all its children.
	 * @return the directory length, 0 if the directory/file does not exist.
	 */
	public static long getLength(final File dir) {
		if (!dir.exists()) {
			return 0;
		}
		if (dir.isFile()) {
			return dir.length();
		} else if (dir.isDirectory()) {
			long result = 4096;
			for (final File file : dir.listFiles()) {
				result += getLength(file);
			}
			return result;
		} else {
			return 0;
		}
	}

	/**
	 * Checks if given character is an ascii letter (a-z, A-Z).
	 * 
	 * @param c
	 *            the character to check
	 * @return true if the character is a letter, false otherwise.
	 */
	public static boolean isAsciiLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	private static final int BUFSIZE = 8192;

	/**
	 * Reads given file fully and returns its contents. The stream is always
	 * closed.
	 * 
	 * @param in
	 *            read this stream. Always closed.
	 * @return the byte contents of given stream.
	 * @throws IOException
	 *             on i/o error
	 */
	public static byte[] readFully(final InputStream in) throws IOException {
		try {
			final byte[] buf = new byte[BUFSIZE];
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			while (true) {
				final int bytesRead = in.read(buf);
				if (bytesRead < 0) {
					break;
				}
				if (bytesRead == 0) {
					continue;
				}
				bout.write(buf, 0, bytesRead);
			}
			return bout.toByteArray();
		} finally {
			closeQuietly(in);
		}
	}
}
