/**
 *     Aedict - an EDICT browser for Android
 Copyright (C) 2007 Martin Vysny
 
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

/**
 * Enumerates search modes.
 * 
 * @author Martin Vysny
 */
public enum MatcherEnum {
	/**
	 * Matches when the query is a substring of given line.
	 */
	SubstringMatch {
		@Override
		public boolean matches(String query, String line) {
			return line.toLowerCase().contains(query.toLowerCase());
		}
	},
	/**
	 * Matches when the query matches an entire expression text for given line.
	 * E.g. foo matches "foo", "foo;bar" but not "foo bar"
	 */
	ExactMatchEng {
		@Override
		public boolean matches(String query, String line) {
			int lastIndex = 0;
			final String _line = line.toLowerCase();
			final String _query = query.toLowerCase();
			int indexOfQuery = _line.indexOf(_query, lastIndex);
			while (indexOfQuery >= 0) {
				if (!isWordPart(skipWhitespaces(_line, indexOfQuery - 1, -1))
						&& !isWordPart(skipWhitespaces(_line, indexOfQuery
								+ _query.length(), 1))) {
					return true;
				}
				lastIndex = indexOfQuery + 1;
				indexOfQuery = _line.indexOf(_query, lastIndex);
			}
			return false;
		}

		private boolean isWordPart(final char c) {
			return c == '-' || c == '\'' || MiscUtils.isAsciiLetter(c);
		}

		private char skipWhitespaces(final String line, final int charIndex,
				final int direction) {
			for (int i = charIndex; i >= 0 && i < line.length(); i += direction) {
				final char c = line.charAt(i);
				if (!Character.isWhitespace(c)) {
					return c;
				}
			}
			return 0;
		}
	};
	/**
	 * Checks if given query matches given line.
	 * 
	 * @param query
	 *            the word being searched for
	 * @param line
	 *            the line
	 * @return true if the line matches the word, false otherwise.
	 */
	public abstract boolean matches(final String query, final String line);
}
