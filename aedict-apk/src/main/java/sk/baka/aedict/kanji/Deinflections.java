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

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import sk.baka.aedict.dict.SearchQuery;
import sk.baka.aedict.kanji.VerbInflection.Form;

/**
 * The product of the deinflection.
 * @author Martin Vysny
 */
public class Deinflections implements Serializable {
	/**
     * Deinflected verb(s) in {@link RomanizationEnum#NihonShiki}
     *         romanization. If the expression cannot be deinflected the
     *         expression is returned.
	 */
	public Set<String> deinflectedVerbs;
	/**
	 * List of rules used in the process of deinflection.
	 */
	public List<Deinflection> deinflections;
	public SearchQuery query;
	public static class Deinflection implements Serializable {
		public Deinflection(final String inflected, final Form inflectedForm, final String... deinflected){
			this.inflected=inflected;
			this.inflectedForm=inflectedForm;
			this.deinflected=deinflected;
		}
		/**
		 * {@link RomanizationEnum#NihonShiki} romanized inflected verb.
		 */
		public String inflected;
		public Form inflectedForm;
		/**
		 * {@link RomanizationEnum#NihonShiki} romanized possible deinflections.
		 */
		public String[] deinflected;
	}
}
