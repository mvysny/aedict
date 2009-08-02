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

import java.io.IOException;
import java.io.InputStream;

/**
 * Reads given input stream and is able to return "lines" - byte arrays
 * terminated with 0x0A (excluding the 0x0A character). Performs buffering.
 * Thread-unsafe.
 * 
 * @author Martin Vysny
 */
public final class LineReadInputStream {
	private final InputStream in;

	/**
	 * Creates a new line reader instance.
	 * 
	 * @param in
	 *            underlying input stream.
	 * @throws IOException
	 */
	public LineReadInputStream(final InputStream in) throws IOException {
		this.in = in;
		preloadBuffer(0);
	}

	/**
	 * Maximum line length.
	 */
	public static final int BUFFER_LEN = 32768;
	/**
	 * Currently loaded line. Available after {@link #readLine()} invocation.
	 */
	public final byte[] buffer = new byte[BUFFER_LEN];
	/**
	 * Marks beginning of the line in the {@link #buffer}. Valid after
	 * {@link #readLine()} is invoked.
	 */
	public int lineStart = 0;
	/**
	 * Marks length of the line in the {@link #buffer}. Valid after
	 * {@link #readLine()} is invoked.
	 */
	public int lineLength = 0;
	private boolean isNoMoreBytes = false;

	private void preloadBuffer(final int from) throws IOException {
		if (isNoMoreBytes) {
			return;
		}
		int readPosition = from;
		while (readPosition < BUFFER_LEN) {
			int bytesRead = in.read(buffer, readPosition, BUFFER_LEN
					- readPosition);
			if (bytesRead < 0) {
				isNoMoreBytes = true;
				currentBufferLength = readPosition;
				return;
			}
			readPosition += bytesRead;
		}
		currentBufferLength = BUFFER_LEN;
	}

	private int currentBufferPosition = 0;
	private int currentBufferLength;

	private int nextIndexOfSeparator() {
		for (int i = currentBufferPosition; i < currentBufferLength; i++) {
			if (buffer[i] == 0x0a) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Fills the {@link #buffer} array with next line.
	 * 
	 * @return true if everything went okay, false if there are no more lines.
	 * @throws IOException
	 *             on i/o error
	 */
	public boolean readLine() throws IOException {
		int nextSeparator = nextIndexOfSeparator();
		if (nextSeparator < 0) {
			if (isNoMoreBytes) {
				return false;
			}
			System.arraycopy(buffer, currentBufferPosition, buffer, 0,
					currentBufferLength - currentBufferPosition);
			preloadBuffer(currentBufferLength - currentBufferPosition);
			currentBufferPosition = 0;
			nextSeparator = nextIndexOfSeparator();
			if (nextSeparator < 0) {
				throw new IOException("Too long line");
			}
		}
		lineStart = currentBufferPosition;
		lineLength = nextSeparator - currentBufferPosition;
		currentBufferPosition = nextSeparator + 1;
		return true;
	}
}
