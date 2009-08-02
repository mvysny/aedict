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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads given input stream and is able to return "lines" - byte arrays
 * terminated with 0x0A (excluding the 0x0A character).
 * 
 * @author Martin Vysny
 */
public final class LineReadInputStream extends BufferedInputStream {

	public LineReadInputStream(InputStream in) {
		super(in);
	}

	public static final int MAX_LINE_LEN = 65536;
	public final byte[] line = new byte[MAX_LINE_LEN];

	public int readLine() throws IOException {
		int i = 0;
		while (true) {
			final int b = read();
			if (b < 0) {
				// end of file
				if (i > 0) {
					return i;
				}
				return -1;
			}
			if (b == 0x0a) {
				return i;
			}
			line[i++] = (byte) b;
			if (i >= MAX_LINE_LEN) {
				throw new IOException("Exceeded maximum line length");
			}
		}
	}
}
