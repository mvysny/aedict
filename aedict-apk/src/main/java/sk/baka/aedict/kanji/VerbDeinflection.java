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

import java.util.ArrayList;
import java.util.List;

/**
 * Performs a simple verb deinflection.
 * 
 * @author Martin Vysny
 */
public class VerbDeinflection {
	private static class EndsWithDeinflector extends AbstractDeinflector {
		private final String[] endsWith;
		private final String[] replaceBy;

		/**
		 * Deinflects a verb if it ends with one of the following strings.
		 * 
		 * @param endsWith
		 *            a non-empty non-null list of possible endings, lower-case
		 *            trimmed romaji.
		 * @param replaceBy
		 *            the ending is replaced by this string.
		 */
		public EndsWithDeinflector(final String[] endsWith,
				final String... replaceBy) {
			this.endsWith = endsWith;
			this.replaceBy = replaceBy;
		}

		/**
		 * Deinflects a verb if it ends with one of the following strings.
		 * 
		 * @param endsWith
		 *            a non-empty non-null list of possible endings, lower-case
		 *            trimmed romaji.
		 * @param replaceBy
		 *            the ending is replaced by this string.
		 */
		public EndsWithDeinflector(final String endsWith,
				final String... replaceBy) {
			this(new String[] { endsWith }, replaceBy);
		}

		@Override
		public String[] deinflect(String romaji) {
			for (String ending : endsWith) {
				if (romaji.endsWith(ending) && !romaji.equals(ending)) {
					final String[] result = new String[replaceBy.length];
					final String verbPart = romaji.substring(0, romaji.length()
							- ending.length());
					int i = 0;
					for (final String rb : replaceBy) {
						result[i++] = verbPart + rb;
					}
					return result;
				}
			}
			// nothing matched, return null
			return null;
		}
	}

	private static abstract class AbstractDeinflector {
		/**
		 * Tries to deinflect a verb.
		 * 
		 * @param romaji
		 *            a verb in lower-case, trimmed romaji.
		 * @return deinflected verb, or a multiple verbs if there are multiple
		 *         possibilities to deinflect. If this rule cannot be applied to
		 *         deinflect the verb, null or an empty array should be
		 *         returned.
		 */
		public abstract String[] deinflect(String romaji);
	}

	private final static List<? extends AbstractDeinflector> DEINFLECTORS;
	static {
		final List<AbstractDeinflector> d = new ArrayList<AbstractDeinflector>();
		// the -masu deinflector
		d.add(new EndsWithDeinflector(new String[] { "masen", "mashita",
				"masendeshita", "masen deshita" }, "masu"));
		// the -nakatta deinflector
		d.add(new EndsWithDeinflector("nakatta", "nai"));
		d.add(new EndsWithDeinflector("anai", "u"));
		d.add(new EndsWithDeinflector("itai", "u"));
		d.add(new EndsWithDeinflector("etai", "eru"));
		d.add(new EndsWithDeinflector("eba", "u"));
		d.add(new EndsWithDeinflector("emasu", "u"));
		d.add(new EndsWithDeinflector(new String[]{"outosuru","ou to suru"}, "u"));
		// this is dangerous - it will deinflect all ichidan verbs. however, this rule is also required, to correctly deinflect e.g.
		// aetai. list as a last rule. Make the rule produce the old verb and also the deinflected one.
		d.add(new EndsWithDeinflector("eru", "eru","u"));
		DEINFLECTORS = d;
	}
}
