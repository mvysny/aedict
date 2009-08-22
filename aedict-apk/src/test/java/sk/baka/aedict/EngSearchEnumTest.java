/**
 *     Ambient - A music player for the Android platform
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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  Tests the {@link EngSearchEnum} class.
 * @author Martin Vysny
 */
public class EngSearchEnumTest {
	@Test
	public void simpleSubstringMatches(){
		assertFalse(EngSearchEnum.SubstringMatch.matches("query", "foo"));
		assertTrue(EngSearchEnum.SubstringMatch.matches("query", "query"));
		assertTrue(EngSearchEnum.SubstringMatch.matches("query", "QUERY"));
		assertTrue(EngSearchEnum.SubstringMatch.matches("query", "asd QUERYda dar4"));
	}
	
	@Test
	public void simpleExactMatches() {
		assertFalse(EngSearchEnum.ExactMatch.matches("query", "foo"));
		assertTrue(EngSearchEnum.ExactMatch.matches("query", "query"));
		assertTrue(EngSearchEnum.ExactMatch.matches("query", "QUERY"));
	}
	@Test
	public void complexExactMatches() {
		assertFalse(EngSearchEnum.ExactMatch.matches("query", "QUERYQUERY"));
		assertFalse(EngSearchEnum.ExactMatch.matches("query", "QUERY QUERY"));
		assertFalse(EngSearchEnum.ExactMatch.matches("query", "queryquery"));
		assertFalse(EngSearchEnum.ExactMatch.matches("query", "query query"));
		assertTrue(EngSearchEnum.ExactMatch.matches("query", "query; query"));
		assertTrue(EngSearchEnum.ExactMatch.matches("query", "foo-bar-baz [f] (p) query; query"));
	}
}
