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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Holds rules for verb inflection and also provides examples of inflected
 * verbs.
 * 
 * @author Martin Vysny
 */
public final class VerbInflection {
	private final static class Base1Inflector extends AbstractBaseInflector {
		@Override
		protected String inflectIchidan(final String verb) {
			return getIchidanForm1(verb);
		}

		@Override
		protected String inflectGodan(final String verb) {
			if (verb.endsWith("au") || verb.endsWith("eu") || verb.endsWith("iu") || verb.endsWith("uu") || verb.endsWith("ou")) {
				return stripGodan(verb) + "wa";
			}
			return stripGodan(verb) + "a";
		}

		@Override
		protected String inflectKuru() {
			return "ko";
		}

		@Override
		protected String inflectSuru() {
			return "si";
		}
	}

	private final static class Base2Inflector extends AbstractBaseInflector {
		@Override
		protected String inflectIchidan(final String verb) {
			return getIchidanForm1(verb);
		}

		@Override
		protected String inflectGodan(final String verb) {
			return stripGodan(verb) + "i";
		}

		@Override
		protected String inflectKuru() {
			return "ki";
		}

		@Override
		protected String inflectSuru() {
			return "si";
		}
	}

	private final static class Base3Inflector extends AbstractBaseInflector {
		@Override
		protected String inflectIchidan(final String verb) {
			return verb;
		}

		@Override
		protected String inflectGodan(final String verb) {
			return verb;
		}

		@Override
		protected String inflectKuru() {
			return "kuru";
		}

		@Override
		protected String inflectSuru() {
			return "suru";
		}
	}

	private final static class Base4Inflector extends AbstractBaseInflector {
		@Override
		protected String inflectIchidan(final String verb) {
			return getIchidanForm1(verb) + "re";
		}

		@Override
		protected String inflectGodan(final String verb) {
			return stripGodan(verb) + "e";
		}

		@Override
		protected String inflectKuru() {
			return "kure";
		}

		@Override
		protected String inflectSuru() {
			return "sure";
		}
	}

	private final static class Base5Inflector extends AbstractBaseInflector {
		@Override
		protected String inflectIchidan(final String verb) {
			return getIchidanForm1(verb) + "you";
		}

		@Override
		protected String inflectGodan(final String verb) {
			return stripGodan(verb) + "ou";
		}

		@Override
		protected String inflectKuru() {
			return "koyou";
		}

		@Override
		protected String inflectSuru() {
			return "siyou";
		}
	}

	/**
	 * A simple base inflector.
	 */
	public static abstract class AbstractBaseInflector {
		/**
		 * Inflects an ichidan verb.
		 * 
		 * @param verb
		 *            a verb in the Base3 -ru form, with
		 *            {@link RomanizationEnum#NihonShiki} romanization.
		 * @return inflected verb, in {@link RomanizationEnum#NihonShiki}
		 *         romanization.
		 */
		protected abstract String inflectIchidan(final String verb);

		/**
		 * Inflects a godan verb.
		 * 
		 * @param verb
		 *            a verb in the Base3 form, with
		 *            {@link RomanizationEnum#NihonShiki} romanization.
		 * @return inflected verb, in {@link RomanizationEnum#NihonShiki}
		 *         romanization.
		 */
		protected abstract String inflectGodan(final String verb);

		/**
		 * Returns an inflected "kuru" verb.
		 * 
		 * @return inflected kuru verb in {@link RomanizationEnum#NihonShiki}
		 *         romanization.
		 */
		protected abstract String inflectKuru();

		/**
		 * Returns an inflected "suru" verb.
		 * 
		 * @return inflected suru verb in {@link RomanizationEnum#NihonShiki}
		 *         romanization.
		 */
		protected abstract String inflectSuru();

		/**
		 * Returns given ichidan verb in form 1 (with trailing -ru removed). The
		 * function performs several checks that a proper verb is given.
		 * 
		 * @param verb
		 *            an ichidan verb in {@link RomanizationEnum#NihonShiki}
		 *            romanization.
		 * @return inflected ichidan verb in {@link RomanizationEnum#NihonShiki}
		 *         romanization.
		 */
		protected final String getIchidanForm1(final String verb) {
			// sanity check
			if (!verb.endsWith("eru") && !verb.endsWith("iru")) {
				throw new RuntimeException(verb + " is not ichidan");
			}
			return verb.substring(0, verb.length() - 2);
		}

		/**
		 * Strips trailing -u of given godan verb. The function performs several
		 * checks that a proper verb is given.
		 * 
		 * @param verb
		 *            a godan verb in {@link RomanizationEnum#NihonShiki}
		 *            romanization.
		 * @return godan verb in {@link RomanizationEnum#NihonShiki}
		 *         romanization with trailing -u stripped.
		 */
		protected final String stripGodan(final String verb) {
			// sanity check
			if (!verb.endsWith("u")) {
				throw new RuntimeException(verb + " is not base-3 godan");
			}
			return verb.substring(0, verb.length() - 1);
		}

		/**
		 * Inflects a verb to the appropriate form.
		 * 
		 * @param verb
		 *            a verb, may be in kanji+hiragana, or in the
		 *            {@link RomanizationEnum#NihonShiki} romanization. Must be
		 *            in Base 3 form.
		 * @param ichidan
		 *            true if the verb is ichidan, false if it is godan or
		 *            irregular, like kuru/suru.
		 * @return inflected verb, never null, in the
		 *         {@link RomanizationEnum#NihonShiki} romanization. Kanji
		 *         characters are left as-is.
		 */
		public final String inflect(final String verb, final boolean ichidan) {
			final String romanized = RomanizationEnum.NihonShiki.toRomaji(verb);
			if (romanized.equals("kuru") || romanized.equals("来ru")) {
				return inflectKuru();
			}
			if (romanized.endsWith("suru")) {
				return romanized.substring(0, romanized.length() - 4) + inflectSuru();
			}
			if (ichidan) {
				return inflectIchidan(romanized);
			}
			return inflectGodan(romanized);
		}
	}

	private static abstract class AbstractBaseTeTaInflector extends AbstractBaseInflector {
		private final char ending;

		protected AbstractBaseTeTaInflector(boolean te) {
			ending = te ? 'e' : 'a';
		}

		@Override
		protected final String inflectIchidan(final String verb) {
			return getIchidanForm1(verb) + "t" + ending;
		}

		@Override
		protected final String inflectGodan(final String verb) {
			final String stripped = stripGodan(verb);
			if (stripped.endsWith("a") || stripped.endsWith("e") || stripped.endsWith("i") || stripped.endsWith("o") || stripped.endsWith("u") || stripped.endsWith("t") || stripped.endsWith("r")) {
				return stripped + "tt" + ending;
			}
			if (stripped.endsWith("k")) {
				return stripped + "it" + ending;
			}
			if (stripped.endsWith("g")) {
				return stripped + "id" + ending;
			}
			if (stripped.endsWith("s")) {
				return stripped + "sit" + ending;
			}
			if (stripped.endsWith("n") || stripped.endsWith("b") || stripped.endsWith("m")) {
				return stripped + "nd" + ending;
			}
			throw new RuntimeException("Not a valid japanese base-3 verb: " + verb);
		}

		@Override
		protected final String inflectKuru() {
			return "kit" + ending;
		}

		@Override
		protected final String inflectSuru() {
			return "sit" + ending;
		}
	}

	private static final class BaseTeInflector extends AbstractBaseTeTaInflector {

		public BaseTeInflector() {
			super(true);
		}
	}

	private static final class BaseTaInflector extends AbstractBaseTeTaInflector {

		public BaseTaInflector() {
			super(false);
		}
	}

	/**
	 * Lists all inflectors for all 5 bases + TE/TA base.
	 */
	public static final List<AbstractBaseInflector> INFLECTORS = Collections.unmodifiableList(Arrays.asList(new Base1Inflector(), new Base2Inflector(), new Base3Inflector(), new Base4Inflector(), new Base5Inflector(), new BaseTeInflector(), new BaseTaInflector()));

	/**
	 * The verb's plain form:
	 * http://www.timwerx.net/language/jpverbs/lesson1.htm
	 */
	public static final Form PLAIN_FORM = new Form(new Base3Inflector(), "", true, -1, new int[] {});
	/**
	 * The verb's polite plain form (I do something):
	 * http://www.timwerx.net/language/jpverbs/lesson2.htm
	 */
	public static final Form POLITE_PLAIN_FORM = new Form(new Base2Inflector(), "masu", true, -1, new int[] {});
	/**
	 * The verb's polite negative form (I do not do something):
	 * http://www.timwerx.net/language/jpverbs/lesson4.htm
	 */
	public static final Form POLITE_NEGATIVE_FORM = new Form(new Base2Inflector(), "masen", true, -1, new int[] {});
	/**
	 * The verb's polite past form (I did something):
	 * http://www.timwerx.net/language/jpverbs/lesson5.htm
	 */
	public static final Form POLITE_PAST_FORM = new Form(new Base2Inflector(), "masita", true, -1, new int[] {});
	/**
	 * The verb's polite past negative form (I didn't do something):
	 * http://www.timwerx.net/language/jpverbs/lesson6.htm
	 */
	public static final Form POLITE_PAST_NEGATIVE_FORM = new Form(new Base2Inflector(), "masen desita", true, -1, new int[] {});
	/**
	 * To hell with official names :-) The verb's "want" form:
	 * http://www.timwerx.net/language/jpverbs/lesson8.htm
	 */
	public static final Form WANT_FORM = new Form(new Base2Inflector(), "tai", true, -1, new int[] {});
	/**
	 * The verb's "Let's do something." form:
	 * http://www.timwerx.net/language/jpverbs/lesson9.htm
	 */
	public static final Form LET_S_FORM = new Form(new Base2Inflector(), "masyou", true, -1, new int[] {});
	/**
	 * The verb's "Do something!" form:
	 * http://www.timwerx.net/language/jpverbs/lesson10.htm
	 */
	public static final Form SIMPLE_COMMAND_FORM = new Form(new Base2Inflector(), "nasai", true, -1, new int[] {});
	/**
	 * The verb's "I'm going to do something" form:
	 * http://www.timwerx.net/language/jpverbs/lesson13.htm
	 */
	public static final Form GOING_FORM = new Form(new Base2Inflector(), " ni iku", true, -1, new int[] {});
	/**
	 * The verb's "I'm going to arrive" form:
	 * http://www.timwerx.net/language/jpverbs/lesson13.htm
	 */
	public static final Form ARRIVE_FORM = new Form(new Base2Inflector(), " ni kuru", true, -1, new int[] {});
	/**
	 * The verb's "It is hard to do something" form:
	 * http://www.timwerx.net/language/jpverbs/lesson14.htm
	 */
	public static final Form HARD_TO_DO_FORM = new Form(new Base2Inflector(), "nikui", false, -1, new int[] {});
	/**
	 * The verb's "It is easy to do something." form:
	 * http://www.timwerx.net/language/jpverbs/lesson14.htm
	 */
	public static final Form EASY_TO_DO_FORM = new Form(new Base2Inflector(), "yasui", false, -1, new int[] {});
	/**
	 * The verb's "I went too far doing something." form:
	 * http://www.timwerx.net/language/jpverbs/lesson15.htm
	 */
	public static final Form GO_TOO_FAR_FORM = new Form(new Base2Inflector(), "sugiru", false, -1, new int[] {});
	/**
	 * The verb's "I did X while I was doing Y." form:
	 * http://www.timwerx.net/language/jpverbs/lesson16.htm
	 */
	public static final Form WHILE_DOING_FORM = new Form(new Base2Inflector(), "nagara", false, -1, new int[] {});
	/**
	 * The verb's "I do not do something." form:
	 * http://www.timwerx.net/language/jpverbs/lesson17.htm
	 */
	public static final Form NEGATIVE_FORM = new Form(new Base1Inflector(), "nai", true, -1, new int[] {});
	/**
	 * The verb's "I probably do not do something." form:
	 * http://www.timwerx.net/language/jpverbs/lesson18.htm
	 */
	public static final Form PROBABLE_NEGATIVE_FORM = new Form(new Base1Inflector(), "nai desyou", true, -1, new int[] {});
	/**
	 * The verb's "I didn't do something." form:
	 * http://www.timwerx.net/language/jpverbs/lesson19.htm
	 */
	public static final Form NEGATIVE_PAST_FORM = new Form(new Base1Inflector(), "nakatta", true, -1, new int[] {});
	/**
	 * The verb's "If I do not do something" form:
	 * http://www.timwerx.net/language/jpverbs/lesson20.htm
	 */
	public static final Form NEGATIVE_CONDITIONAL_FORM = new Form(new Base1Inflector(), "nakereba", false, -1, new int[] {});
	/**
	 * The verb's "I have to do something (It won't go otherwise)" form:
	 * http://www.timwerx.net/language/jpverbs/lesson21.htm
	 */
	public static final Form HAS_TO_FORM = new Form(new Base1Inflector(), "nakereba narimasen", true, -1, new int[] {});
	/**
	 * The verb's "I'll let/have/make him to do something" form:
	 * http://www.timwerx.net/language/jpverbs/lesson22.htm
	 */
	public static final Form LET_HIM_FORM = new LetHimForm();
	/**
	 * The verb's "I did X without doing Y" form:
	 * http://www.timwerx.net/language/jpverbs/lesson23.htm
	 */
	public static final Form DID_X_WITHOUT_DOING_Y_FORM = new Form(new Base1Inflector(), "zu ni", false, -1, new int[] {});
	/**
	 * The verb's "I'll probably do something" form:
	 * http://www.timwerx.net/language/jpverbs/lesson24.htm
	 */
	public static final Form PROBABLE_FORM = new Form(new Base3Inflector(), " desyou", true, -1, new int[] {});
	/**
	 * The verb's "I plan to do something" form:
	 * http://www.timwerx.net/language/jpverbs/lesson25.htm
	 */
	public static final Form PLAN_FORM = new Form(new Base3Inflector(), " hazu", true, -1, new int[] {});
	/**
	 * The verb's "I should do something" form:
	 * http://www.timwerx.net/language/jpverbs/lesson26.htm
	 */
	public static final Form SHOULD_FORM = new Form(new Base3Inflector(), " hou ga ii", true, -1, new int[] {});
	/**
	 * The verb's "I don't know whether I do something or not." form:
	 * http://www.timwerx.net/language/jpverbs/lesson27.htm
	 */
	public static final Form WHETHER_OR_NOT_FORM = new Form(new Base3Inflector(), " ka dou ka", false, -1, new int[] {});
	/**
	 * The verb's "Maybe I'll do something." form:
	 * http://www.timwerx.net/language/jpverbs/lesson28.htm
	 */
	public static final Form MAYBE_FORM = new Form(new Base3Inflector(), " kamo siremasen", true, -1, new int[] {});
	/**
	 * A list of all forms, ordered as in the
	 * http://www.timwerx.net/language/jpverbs/index.htm#contents table of
	 * contents.
	 */
	public static final List<Form> ALL_FORMS = Collections.unmodifiableList(Arrays.asList(PLAIN_FORM, POLITE_PLAIN_FORM, POLITE_NEGATIVE_FORM, POLITE_PAST_FORM, POLITE_PAST_NEGATIVE_FORM, WANT_FORM, LET_S_FORM, SIMPLE_COMMAND_FORM, GOING_FORM, ARRIVE_FORM, HARD_TO_DO_FORM, EASY_TO_DO_FORM, GO_TOO_FAR_FORM, WHILE_DOING_FORM, NEGATIVE_FORM, PROBABLE_NEGATIVE_FORM, NEGATIVE_PAST_FORM,
			NEGATIVE_CONDITIONAL_FORM, HAS_TO_FORM, LET_HIM_FORM, DID_X_WITHOUT_DOING_Y_FORM, PROBABLE_FORM, PLAN_FORM, SHOULD_FORM, WHETHER_OR_NOT_FORM, MAYBE_FORM));

	/**
	 * Holds information about a verb inflection form.
	 * 
	 * @author Martin Vysny
	 */
	public static class Form {
		private final AbstractBaseInflector inflector;
		private final String suffix;
		private final String suffixIchidan;
		private final int explanationResId;
		private final int[] examples;
		private final boolean frequent;

		protected Form(final AbstractBaseInflector inflector, final String suffix, final boolean frequent, final int explanationResId, final int[] examples) {
			this(inflector, suffix, null, frequent, explanationResId, examples);
		}

		protected Form(final AbstractBaseInflector inflector, final String suffixGodan, final String suffixIchidan, final boolean frequent, final int explanationResId, final int[] examples) {
			this.inflector = inflector;
			this.suffix = suffixGodan;
			this.suffixIchidan = suffixIchidan;
			this.frequent = frequent;
			this.explanationResId = explanationResId;
			this.examples = examples;
		}

		/**
		 * Inflects a verb to the appropriate form.
		 * 
		 * @param verb
		 *            a verb, may be in kanji+hiragana, or in the
		 *            {@link RomanizationEnum#NihonShiki} romanization. Must be
		 *            in Base 3 form.
		 * @param ichidan
		 *            true if the verb is ichidan, false if it is godan.
		 * @return inflected verb, never null, in the
		 *         {@link RomanizationEnum#NihonShiki} romanization. Kanji
		 *         characters are left as-is.
		 */
		public String inflect(final String verb, final boolean ichidan) {
			return inflector.inflect(verb, ichidan) + (suffixIchidan == null ? suffix : (ichidan ? suffixIchidan : suffix));
		}
	}

	protected static final class LetHimForm extends Form {

		protected LetHimForm() {
			super(new Base1Inflector(), "seru", "saseru", false, -1, new int[] {});
		}

		@Override
		public String inflect(String verb, boolean ichidan) {
			if (verb.endsWith("suru")) {
				return verb.substring(0, verb.length() - 4) + "saseru";
			}
			return super.inflect(verb, ichidan);
		}

	}
}
