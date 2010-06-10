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
package sk.baka.aedict.kanji;

import org.junit.Test;

import sk.baka.aedict.kanji.VerbInflection.AbstractBaseInflector;

import static org.junit.Assert.*;

/**
 * Tests the {@link VerbInflection} class.
 * 
 * @author Martin Vysny
 */
public class VerbInflectionTest {
	@Test
	public void testBase1Inflection() {
		final AbstractBaseInflector i = new VerbInflection.Base1Inflector();
		assertEquals("ika", i.inflect("iku", false));
		assertEquals("ara", i.inflect("aru", false));
		assertEquals("ko", i.inflect("kuru", false));
		assertEquals("si", i.inflect("suru", false));
		assertEquals("kawa", i.inflect("kau", false));
		assertEquals("aruka", i.inflect("aruku", false));
		assertEquals("isoga", i.inflect("isogu", false));
		assertEquals("kasa", i.inflect("kasu", false));
		assertEquals("mata", i.inflect("matu", false));
		assertEquals("shina", i.inflect("shinu", false));
		assertEquals("asoba", i.inflect("asobu", false));
		assertEquals("yoma", i.inflect("yomu", false));
		assertEquals("kaera", i.inflect("kaeru", false));
		assertEquals("tabe", i.inflect("taberu", true));
	}

	@Test
	public void testBase2Inflection() {
		final AbstractBaseInflector i = new VerbInflection.Base2Inflector();
		assertEquals("iki", i.inflect("iku", false));
		assertEquals("ari", i.inflect("aru", false));
		assertEquals("ki", i.inflect("kuru", false));
		assertEquals("si", i.inflect("suru", false));
		assertEquals("kai", i.inflect("kau", false));
		assertEquals("aruki", i.inflect("aruku", false));
		assertEquals("isogi", i.inflect("isogu", false));
		assertEquals("kasi", i.inflect("kasu", false));
		assertEquals("mati", i.inflect("matu", false));
		assertEquals("shini", i.inflect("shinu", false));
		assertEquals("asobi", i.inflect("asobu", false));
		assertEquals("yomi", i.inflect("yomu", false));
		assertEquals("kaeri", i.inflect("kaeru", false));
		assertEquals("tabe", i.inflect("taberu", true));
	}

	@Test
	public void testBase3Inflection() {
		final AbstractBaseInflector i = new VerbInflection.Base3Inflector();
		assertEquals("iku", i.inflect("iku", false));
		assertEquals("aru", i.inflect("aru", false));
		assertEquals("kuru", i.inflect("kuru", false));
		assertEquals("suru", i.inflect("suru", false));
		assertEquals("kau", i.inflect("kau", false));
		assertEquals("aruku", i.inflect("aruku", false));
		assertEquals("isogu", i.inflect("isogu", false));
		assertEquals("kasu", i.inflect("kasu", false));
		assertEquals("matu", i.inflect("matu", false));
		assertEquals("shinu", i.inflect("shinu", false));
		assertEquals("asobu", i.inflect("asobu", false));
		assertEquals("yomu", i.inflect("yomu", false));
		assertEquals("kaeru", i.inflect("kaeru", false));
		assertEquals("taberu", i.inflect("taberu", true));
	}

	@Test
	public void testBase4Inflection() {
		final AbstractBaseInflector i = new VerbInflection.Base4Inflector();
		assertEquals("ike", i.inflect("iku", false));
		assertEquals("are", i.inflect("aru", false));
		assertEquals("kure", i.inflect("kuru", false));
		assertEquals("sure", i.inflect("suru", false));
		assertEquals("kae", i.inflect("kau", false));
		assertEquals("aruke", i.inflect("aruku", false));
		assertEquals("isoge", i.inflect("isogu", false));
		assertEquals("kase", i.inflect("kasu", false));
		assertEquals("mate", i.inflect("matu", false));
		assertEquals("shine", i.inflect("shinu", false));
		assertEquals("asobe", i.inflect("asobu", false));
		assertEquals("yome", i.inflect("yomu", false));
		assertEquals("kaere", i.inflect("kaeru", false));
		assertEquals("tabere", i.inflect("taberu", true));
	}

	@Test
	public void testBase5Inflection() {
		final AbstractBaseInflector i = new VerbInflection.Base5Inflector();
		assertEquals("ikou", i.inflect("iku", false));
		assertEquals("arou", i.inflect("aru", false));
		assertEquals("koyou", i.inflect("kuru", false));
		assertEquals("siyou", i.inflect("suru", false));
		assertEquals("kaou", i.inflect("kau", false));
		assertEquals("arukou", i.inflect("aruku", false));
		assertEquals("isogou", i.inflect("isogu", false));
		assertEquals("kasou", i.inflect("kasu", false));
		assertEquals("matou", i.inflect("matu", false));
		assertEquals("shinou", i.inflect("shinu", false));
		assertEquals("asobou", i.inflect("asobu", false));
		assertEquals("yomou", i.inflect("yomu", false));
		assertEquals("kaerou", i.inflect("kaeru", false));
		assertEquals("tabeyou", i.inflect("taberu", true));
	}

	@Test
	public void testBaseTeInflection() {
		final AbstractBaseInflector i = new VerbInflection.BaseTeInflector();
		assertEquals("itte", i.inflect("iku", false));
		assertEquals("atte", i.inflect("aru", false));
		assertEquals("kite", i.inflect("kuru", false));
		assertEquals("site", i.inflect("suru", false));
		assertEquals("katte", i.inflect("kau", false));
		assertEquals("aruite", i.inflect("aruku", false));
		assertEquals("isoide", i.inflect("isogu", false));
		assertEquals("kasite", i.inflect("kasu", false));
		assertEquals("matte", i.inflect("matu", false));
		assertEquals("shinde", i.inflect("shinu", false));
		assertEquals("asonde", i.inflect("asobu", false));
		assertEquals("yonde", i.inflect("yomu", false));
		assertEquals("kaette", i.inflect("kaeru", false));
		assertEquals("tabete", i.inflect("taberu", true));
	}

	@Test
	public void testBaseTaInflection() {
		final AbstractBaseInflector i = new VerbInflection.BaseTaInflector();
		assertEquals("itta", i.inflect("iku", false));
		assertEquals("atta", i.inflect("aru", false));
		assertEquals("kita", i.inflect("kuru", false));
		assertEquals("sita", i.inflect("suru", false));
		assertEquals("katta", i.inflect("kau", false));
		assertEquals("aruita", i.inflect("aruku", false));
		assertEquals("isoida", i.inflect("isogu", false));
		assertEquals("kasita", i.inflect("kasu", false));
		assertEquals("matta", i.inflect("matu", false));
		assertEquals("shinda", i.inflect("shinu", false));
		assertEquals("asonda", i.inflect("asobu", false));
		assertEquals("yonda", i.inflect("yomu", false));
		assertEquals("kaetta", i.inflect("kaeru", false));
		assertEquals("tabeta", i.inflect("taberu", true));
	}
}
