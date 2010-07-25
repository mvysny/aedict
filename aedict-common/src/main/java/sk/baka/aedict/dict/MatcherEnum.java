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

/**
 * Enumerates search modes.
 * 
 * @author Martin Vysny
 */
public enum MatcherEnum {

    /**
     * Matches when the query is a substring of given line.
     */
    Substring {

        @Override
        public boolean matches(String query, String line) {
            return line.toLowerCase().contains(query.toLowerCase());
        }
    },
    /**
     * Matches when the query matches an entire expression text for given line.
     * A special processing should be used in case of an english Edict matching: for example, foo should
     * match "foo", "foo;bar" but not "foo bar"
     */
    Exact {

        @Override
        public boolean matches(String query, String line) {
            return query.equalsIgnoreCase(line);
        }
    },
    /**
     * Matches when the line starts with the query string. Usable only for japanese search.
     */
    StartsWith {

        @Override
        public boolean matches(String query, String line) {
            if (line.length() < query.length()) {
                return false;
            }
            return line.substring(0, query.length()).equalsIgnoreCase(query);
        }
    },
    /**
     * Matches when the line ends with the query string. Usable only for japanese search.
     */
    EndsWith {

        @Override
        public boolean matches(String query, String line) {
            if (line.length() < query.length()) {
                return false;
            }
            return line.substring(line.length() - query.length()).equalsIgnoreCase(query);
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
