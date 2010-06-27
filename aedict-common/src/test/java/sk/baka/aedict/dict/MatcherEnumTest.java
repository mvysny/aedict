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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *  Tests the {@link MatcherEnum} class.
 * @author Martin Vysny
 */
public class MatcherEnumTest {

    @Test
    public void simpleSubstringMatches() {
        assertFalse(MatcherEnum.Substring.matches("query", "foo"));
        assertTrue(MatcherEnum.Substring.matches("query", "query"));
        assertTrue(MatcherEnum.Substring.matches("query", "QUERY"));
        assertTrue(MatcherEnum.Substring.matches("query", "asd QUERYda dar4"));
        assertTrue(MatcherEnum.Substring.matches("query", "QUERY asd QUERYda dar4"));
        assertTrue(MatcherEnum.Substring.matches("query", "asd QUERYda dar4QUERY"));
    }

    @Test
    public void simpleExactMatches() {
        assertFalse(MatcherEnum.Exact.matches("query", "foo"));
        assertTrue(MatcherEnum.Exact.matches("query", "query"));
        assertTrue(MatcherEnum.Exact.matches("query", "QUERY"));
        assertFalse(MatcherEnum.Exact.matches("query", "asd QUERYda dar4"));
        assertFalse(MatcherEnum.Exact.matches("query", "QUERY asd QUERYda dar4"));
        assertFalse(MatcherEnum.Exact.matches("query", "asd QUERYda dar4QUERY"));
    }

    @Test
    public void simpleStartsWithMatches() {
        assertFalse(MatcherEnum.StartsWith.matches("query", "foo"));
        assertTrue(MatcherEnum.StartsWith.matches("query", "query"));
        assertTrue(MatcherEnum.StartsWith.matches("query", "QUERY"));
        assertFalse(MatcherEnum.StartsWith.matches("query", "asd QUERYda dar4"));
        assertTrue(MatcherEnum.StartsWith.matches("query", "QUERY asd QUERYda dar4"));
        assertFalse(MatcherEnum.StartsWith.matches("query", "asd QUERYda dar4QUERY"));
    }

    @Test
    public void simpleEndsWithMatches() {
        assertFalse(MatcherEnum.EndsWith.matches("query", "foo"));
        assertTrue(MatcherEnum.EndsWith.matches("query", "query"));
        assertTrue(MatcherEnum.EndsWith.matches("query", "QUERY"));
        assertFalse(MatcherEnum.EndsWith.matches("query", "asd QUERYda dar4"));
        assertFalse(MatcherEnum.EndsWith.matches("query", "QUERY asd QUERYda dar4"));
        assertTrue(MatcherEnum.EndsWith.matches("query", "asd QUERYda dar4QUERY"));
    }
}
