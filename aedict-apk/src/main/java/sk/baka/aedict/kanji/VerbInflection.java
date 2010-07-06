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
import java.util.StringTokenizer;

import sk.baka.aedict.R;
import android.content.Context;

/**
 * Holds rules for verb inflection and also provides examples of inflected
 * verbs.
 * 
 * @author Martin Vysny
 */
public final class VerbInflection {
	final static class Base1Inflector extends AbstractBaseInflector {
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

		@Override
		public String getName() {
			return "Base 1";
		}
	}

	final static class Base2Inflector extends AbstractBaseInflector {
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

		@Override
		public String getName() {
			return "Base 2";
		}
	}

	final static class Base3Inflector extends AbstractBaseInflector {
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

		@Override
		public String getName() {
			return "Base 3";
		}
	}

	final static class Base4Inflector extends AbstractBaseInflector {
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

		@Override
		public String getName() {
			return "Base 4";
		}
	}

	final static class Base5Inflector extends AbstractBaseInflector {
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

		@Override
		public String getName() {
			return "Base 5";
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
		 * Returns an inflected "iku" verb.
		 * 
		 * @return inflected iku verb in {@link RomanizationEnum#NihonShiki}
		 *         romanization.
		 */
		protected String inflectIku() {
			return inflectGodan("iku");
		}

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
		 *            in Base 3 form. Note that "desu" cannot be inflected.
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
			if (romanized.equals("iku")) {
				return inflectIku();
			}
			if (ichidan) {
				return inflectIchidan(romanized);
			}
			return inflectGodan(romanized);
		}

		/**
		 * Returns a displayable name of this inflection.
		 * 
		 * @return the displayable name, never null.
		 */
		public abstract String getName();
	}

	static abstract class AbstractBaseTeTaInflector extends AbstractBaseInflector {
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
			final String base = stripped.substring(0, stripped.length() - 1);
			if (stripped.endsWith("a") || stripped.endsWith("e") || stripped.endsWith("i") || stripped.endsWith("o") || stripped.endsWith("u")) {
				return stripped + "tt" + ending;
			}
			if (stripped.endsWith("t") || stripped.endsWith("r")) {
				return base + "tt" + ending;
			}
			if (stripped.endsWith("k")) {
				return base + "it" + ending;
			}
			if (stripped.endsWith("g")) {
				return base + "id" + ending;
			}
			if (stripped.endsWith("s")) {
				return base + "sit" + ending;
			}
			if (stripped.endsWith("n") || stripped.endsWith("b") || stripped.endsWith("m")) {
				return base + "nd" + ending;
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

		@Override
		public final String getName() {
			return "Base T" + ending;
		}

		@Override
		protected String inflectIku() {
			return "itt" + ending;
		}

	}

	static final class BaseTeInflector extends AbstractBaseTeTaInflector {

		public BaseTeInflector() {
			super(true);
		}
	}

	static final class BaseTaInflector extends AbstractBaseTeTaInflector {

		public BaseTaInflector() {
			super(false);
		}
	}

	/**
	 * Lists all inflectors for all 5 bases + TE/TA base.
	 */
	public static final List<AbstractBaseInflector> INFLECTORS = Collections.unmodifiableList(Arrays.asList(new Base1Inflector(), new Base2Inflector(), new Base3Inflector(), new Base4Inflector(),
			new Base5Inflector(), new BaseTeInflector(), new BaseTaInflector()));

	/**
	 * Holds information about a verb inflection form.
	 * 
	 * @author Martin Vysny
	 */
	public static enum Form {
		/**
		 * The verb's plain form - I do something:
		 * http://www.timwerx.net/language/jpverbs/lesson1.htm
		 */
		PLAIN(new Base3Inflector(), "", true, R.string.iDoSomething, R.string.plainFormExamples),
		/**
		 * The verb's polite plain form (I do something):
		 * http://www.timwerx.net/language/jpverbs/lesson2.htm
		 */
		POLITE(new Base2Inflector(), "masu", true, R.string.iDoSomethingPolitely, R.string.politeFormExamples),
		/**
		 * The verb's polite negative form (I do not do something):
		 * http://www.timwerx.net/language/jpverbs/lesson4.htm
		 */
		POLITE_NEGATIVE(new Base2Inflector(), "masen", true, R.string.iDoNotDoSomethingPolitely, R.string.politeNegativeFormExamples),
		/**
		 * The verb's polite past form (I did something):
		 * http://www.timwerx.net/language/jpverbs/lesson5.htm
		 */
		POLITE_PAST(new Base2Inflector(), "masita", true, R.string.iDidSomethingPolitely, R.string.politePastFormExamples),
		/**
		 * The verb's polite past negative form (I didn't do something):
		 * http://www.timwerx.net/language/jpverbs/lesson6.htm
		 */
		POLITE_PAST_NEGATIVE(new Base2Inflector(), "masen desita", true, R.string.iDidNotDoSomethingPolitely, R.string.politePastNegativeFormExamples),
		/**
		 * To hell with official names :-) The verb's "want" form:
		 * http://www.timwerx.net/language/jpverbs/lesson8.htm
		 */
		WANT(new Base2Inflector(), "tai", true, R.string.iWantToDoSomething, R.string.wantFormExamples),
		/**
		 * The verb's "Let's do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson9.htm
		 */
		LET_S(new Base2Inflector(), "masyou", true, R.string.letsDoSomethingPolitely, R.string.letsFormExamples),
		/**
		 * The verb's "Do something!" form:
		 * http://www.timwerx.net/language/jpverbs/lesson10.htm
		 */
		SIMPLE_COMMAND(new Base2Inflector(), "nasai", false, R.string.doSomething, R.string.simpleCommandExamples),
		/**
		 * The verb's "I'm going to do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson13.htm
		 */
		GOING(new Base2Inflector(), " ni iku", true, R.string.imGoingToDoSomething, R.string.goingFormExamples),
		/**
		 * The verb's "I'm going to arrive" form:
		 * http://www.timwerx.net/language/jpverbs/lesson13.htm
		 */
		ARRIVE(new Base2Inflector(), " ni kuru", true, R.string.imGoingToArrive, R.string.arriveFormExamples),
		/**
		 * The verb's "It is hard to do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson14.htm
		 */
		HARD_TO_DO(new Base2Inflector(), "nikui", false, R.string.itIsHardToDoSomething, R.string.hardToDoFormExamples),
		/**
		 * The verb's "It is easy to do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson14.htm
		 */
		EASY_TO_DO(new Base2Inflector(), "yasui", false, R.string.itIsEasyToDoSomething, R.string.easyToDoFormExamples),
		/**
		 * The verb's "I went too far doing something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson15.htm
		 */
		GO_TOO_FAR(new Base2Inflector(), "sugiru", false, R.string.iWentTooFarDoingSomething, R.string.goTooFarFormExamples),
		/**
		 * The verb's "I did X while I was doing Y." form:
		 * http://www.timwerx.net/language/jpverbs/lesson16.htm
		 */
		WHILE_DOING(new Base2Inflector(), "nagara", false, R.string.iDidXWhileIWasDoingY, R.string.whileDoingFormExamples),
		/**
		 * The verb's "I do not do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson17.htm
		 */
		NEGATIVE(new Base1Inflector(), "nai", true, R.string.iDoNotDoSomething, R.string.negativeFormExamples) {

			@Override
			public String inflect(String verb, boolean ichidan) {
				if (verb.equals("aru")) {
					return "nai";
				}
				return super.inflect(verb, ichidan);
			}
		},
		/**
		 * The verb's "I probably do not do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson18.htm
		 */
		PROBABLE_NEGATIVE(new Base1Inflector(), "nai desyou", false, R.string.iProbablyDoNotDoSomething, R.string.probableNegativeFormExamples),
		/**
		 * The verb's "I didn't do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson19.htm
		 */
		NEGATIVE_PAST(new Base1Inflector(), "nakatta", true, R.string.iDidNotDoSomething, R.string.negativePastFormExamples),
		/**
		 * The verb's "If I do not do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson20.htm
		 */
		NEGATIVE_CONDITIONAL(new Base1Inflector(), "nakereba", false, R.string.ifIDoNotDoSomething, R.string.negativeConditionalFormExamples),
		/**
		 * The verb's "I have to do something (It won't go otherwise)" form:
		 * http://www.timwerx.net/language/jpverbs/lesson21.htm
		 */
		HAS_TO(new Base1Inflector(), "nakereba narimasen", true, R.string.iHaveToDoSomething, R.string.hasToFormExamples),
		/**
		 * The verb's "I'll let/have/make him do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson22.htm
		 */
		LET_HIM(new Base1Inflector(), "seru", "saseru", false, R.string.iLLLetHimDoSomething, R.string.letHimFormExamples) {

			@Override
			public String inflect(String verb, boolean ichidan) {
				if (verb.endsWith("suru")) {
					return verb.substring(0, verb.length() - 4) + "saseru";
				}
				return super.inflect(verb, ichidan);
			}

		},
		/**
		 * The verb's "I did X without doing Y" form:
		 * http://www.timwerx.net/language/jpverbs/lesson23.htm
		 */
		DID_X_WITHOUT_DOING_Y(new Base1Inflector(), "zu ni", false, R.string.iDidXWithoutDoingY, R.string.didXWithoutDoingYFormExamples),
		/**
		 * The verb's "I'll probably do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson24.htm
		 */
		PROBABLE(new Base3Inflector(), " desyou", false, R.string.iLLProbablyDoSomething, R.string.probableFormExamples),
		/**
		 * The verb's "I plan to do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson25.htm
		 */
		PLAN(new Base3Inflector(), " hazu", false, R.string.iPlanToDoSomething, R.string.planFormExamples),
		/**
		 * The verb's "I should do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson26.htm
		 */
		SHOULD(new Base3Inflector(), " hou ga ii", true, R.string.iShouldDoSomething, R.string.shouldFormExamples),
		/**
		 * The verb's "I don't know whether I do something or not." form:
		 * http://www.timwerx.net/language/jpverbs/lesson27.htm
		 */
		WHETHER_OR_NOT(new Base3Inflector(), " ka dou ka", false, R.string.iDontKnowWhetherIDoSomethingOrNot, R.string.whetherOrNotFormExamples),
		/**
		 * The verb's "Maybe I'll do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson28.htm
		 */
		MAYBE(new Base3Inflector(), " kamo siremasen", true, R.string.maybeILLDoSomething, R.string.maybeFormExamples),
		/**
		 * The verb's "Because of X..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson29.htm
		 */
		BECAUSE_OF(new Base3Inflector(), " kara", false, R.string.becauseOfX, R.string.becauseOfFormExamples),
		/**
		 * The verb's "He does X, but..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson30.htm
		 */
		BUT(new Base3Inflector(), " keredomo / kedo", false, R.string.heDoesXBut, R.string.butFormExamples),
		/**
		 * The verb's "I'm able to do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson31.htm
		 */
		ABLE_TO_DO(new Base3Inflector(), " koto ga dekiru", true, R.string.imAbleToDoSomething, R.string.ableToDoFormExamples) {
			@Override
			public String inflect(String verb, boolean ichidan) {
				if (verb.endsWith("suru")) {
					return verb.substring(0, verb.length() - 4) + " dekiru";
				}
				return super.inflect(verb, ichidan);
			}

		},
		/**
		 * The verb's "I decided to do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson32.htm
		 */
		DECIDED_TO_DO(new Base3Inflector(), " koto ni suru", false, R.string.iDecidedToDoSomething, R.string.decidedToDoFormExamples),
		/**
		 * The verb's "... until X." form:
		 * http://www.timwerx.net/language/jpverbs/lesson33.htm
		 */
		UNTIL(new Base3Inflector(), " made", false, R.string.untilX, R.string.untilFormExamples),
		/**
		 * The verb's "Don't do X!" form:
		 * http://www.timwerx.net/language/jpverbs/lesson34.htm
		 */
		NEGATIVE_COMMAND(new Base3Inflector(), " na!", false, R.string.dontDoX, R.string.negativeCommandFormExamples),

		/**
		 * The verb's "If X, then..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson35.htm
		 */
		IF(new Base3Inflector(), " nara", true, R.string.ifXThen, R.string.ifFormExamples),
		/**
		 * The verb's "X which/where/who Y" form:
		 * http://www.timwerx.net/language/jpverbs/lesson36.htm
		 */
		WHICH_WHERE_WHO(new Base3Inflector(), "", true, R.string.whichWhereWho, R.string.whichWhereWhoFormExamples),
		/**
		 * The verb's "In order to do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson38.htm
		 */
		IN_ORDER_TO(new Base3Inflector(), " no ni", false, R.string.inOrderToDoSomething, R.string.inOrderToFormExamples),
		/**
		 * The verb's "No wa" form:
		 * http://www.timwerx.net/language/jpverbs/lesson39.htm
		 */
		NO_WA(new Base3Inflector(), " no ha", false, R.string.noWa, R.string.noWaExamples),
		/**
		 * The verb's "Because of X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson40.htm
		 */
		BECAUSE_OF2(new Base3Inflector(), " node", false, R.string.becauseOfX, R.string.becauseOf2FormExamples),
		/**
		 * The verb's "In spite of X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson41.htm
		 */
		IN_SPITE_OF(new Base3Inflector(), " noni", false, R.string.inSpiteOfX, R.string.inSpiteOfFormExamples),
		/**
		 * The verb's "I was just about to do X." form:
		 * http://www.timwerx.net/language/jpverbs/lesson82.htm
		 */
		NEARLY(new Base3Inflector(), " tokoro datta", false, R.string.iWasJustAboutToDoX, R.string.nearlyFormExamples),
		/**
		 * The verb's "I heard that X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson42.htm
		 */
		I_HEARD(new Base3Inflector(), " sou desu", false, R.string.iHeardThatX, R.string.iHeardFormExamples),
		/**
		 * The verb's "For the purpose of X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson43.htm
		 */
		FOR_THE_PURPOSE_OF(new Base3Inflector(), " tame ni", true, R.string.forThePurposeOf, R.string.forThePurposeOfFormExamples),
		/**
		 * The verb's "When/If" form:
		 * http://www.timwerx.net/language/jpverbs/lesson44.htm
		 */
		WHEN_IF(new Base3Inflector(), " ni", false, R.string.whenIf, R.string.whenIfFormExamples),
		/**
		 * The verb's "I think that X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson45.htm
		 */
		I_THINK_THAT(new Base3Inflector(), " to omou", true, R.string.iThinkThat, R.string.iThinkThatFormExamples),
		/**
		 * The verb's "I intent to X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson46.htm
		 */
		I_INTENT(new Base3Inflector(), " tumori", false, R.string.iIntentToX, R.string.iIntentFormExamples),
		/**
		 * The verb's "It seems to X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson47.htm
		 */
		IT_SEEMS_TO(new Base3Inflector(), " you desu", false, R.string.itSeemsToX, R.string.itSeemsToFormExamples),
		/**
		 * The verb's "If X, then..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson48.htm
		 */
		IF2(new Base4Inflector(), "ba", true, R.string.ifXThen, R.string.if2FormExamples),
		/**
		 * The verb's "It would be good if X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson49.htm
		 */
		IT_WOULD_BE_GOOD_IF(new Base4Inflector(), "ba ii", true, R.string.itWouldBeGoodIfX, R.string.itWouldBeGoodIfFormExamples),
		/**
		 * The verb's "I regret X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson49.htm
		 */
		I_REGRET(new Base4Inflector(), "ba yokatta", false, R.string.iRegretX, R.string.iRegretFormExamples),
		/**
		 * The verb's "Do something!" form:
		 * http://www.timwerx.net/language/jpverbs/lesson50.htm
		 */
		PLAIN_COMMAND(new Base4Inflector(), "", false, R.string.doSomething, R.string.plainCommandFormExamples) {
			@Override
			public boolean appliesToIchidan() {
				return false;
			}
		},
		/**
		 * The verb's "Able to do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson51.htm
		 */
		ABLE_TO_DO2(new Base4Inflector(), "ru", true, R.string.imAbleToDoSomething, R.string.ableToDo2FormExamples),
		/**
		 * The verb's "Let's do something" form:
		 * http://www.timwerx.net/language/jpverbs/lesson54.htm
		 */
		LET_S2(new Base5Inflector(), "", true, R.string.letsDoSomething, R.string.letS2FormExamples),
		/**
		 * The verb's "I wonder if I should..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson54.htm
		 */
		I_WONDER_IF_I_SHOULD(new Base5Inflector(), " ka na", true, R.string.iWonderIfIShould, R.string.iWonderIfIShouldFormExamples),
		/**
		 * The verb's "Try to do X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson54.htm
		 */
		TRY_TO(new Base5Inflector(), " to suru", false, R.string.tryToDoX, R.string.tryToFormExamples),
		/**
		 * The verb's "Do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson55.htm
		 */
		MILD_COMMAND(new BaseTeInflector(), "", false, R.string.doSomethingMild, R.string.mildCommandFormExamples),
		/**
		 * The verb's "Please do something." form:
		 * http://www.timwerx.net/language/jpverbs/lesson55.htm
		 */
		POLITE_COMMAND(new BaseTeInflector(), " kudasai", true, R.string.pleaseDoSomething, R.string.politeCommandFormExamples),
		/**
		 * The verb's "Let me do something for you." form:
		 * http://www.timwerx.net/language/jpverbs/lesson56.htm
		 */
		LET_ME_DO_SOMETHING_FOR_YOU(new BaseTeInflector(), " ageru", true, R.string.letMeDoSomethingForYou, R.string.politeCommand2FormExamples),
		/**
		 * The verb's "honorably take a look." form:
		 * http://www.timwerx.net/language/jpverbs/lesson57.htm
		 */
		HONORABLY_TAKE_A_LOOK(new BaseTeInflector(), " goran", false, R.string.honorablyTakeALook, R.string.honorablyTakeALookFormExamples),
		/**
		 * The verb's "I am doing X." form:
		 * http://www.timwerx.net/language/jpverbs/lesson58.htm
		 */
		PROGRESSIVE_TENSE(new BaseTeInflector(), " iru", true, R.string.iAmDoingX, R.string.progressiveTenseFormExamples),
		/**
		 * The verb's "I humbly partake" form:
		 * http://www.timwerx.net/language/jpverbs/lesson61.htm
		 */
		VERY_POLITE_COMMAND(new BaseTeInflector(), " itadaku", true, R.string.iHumblyPartake, R.string.veryPoliteCommandFormExamples),
		/**
		 * The verb's "I humbly receive" form:
		 * http://www.timwerx.net/language/jpverbs/lesson61.htm
		 */
		I_HUMBLY_RECEIVE_COMMAND(new BaseTeInflector(), " morau", true, R.string.iHumblyReceive, R.string.iHumblyReceiveFormExamples),
		/**
		 * The verb's "After doing X..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson62.htm
		 */
		AFTER(new BaseTeInflector(), " kara", true, R.string.afterDoingX, R.string.afterFormExamples),
		/**
		 * The verb's "Please do X..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson63.htm
		 */
		REGULAR_PLEASE(new BaseTeInflector(), " kureru", true, R.string.pleaseDoSomething, R.string.regularPleaseFormExamples),
		/**
		 * The verb's "X finished" form:
		 * http://www.timwerx.net/language/jpverbs/lesson64.htm
		 */
		TO_FINISH(new BaseTeInflector(), " kuru", true, R.string.xFinished, R.string.toFinishFormExamples),
		/**
		 * The verb's "X will start" form:
		 * http://www.timwerx.net/language/jpverbs/lesson64.htm
		 */
		TO_START(new BaseTeInflector(), " iku", true, R.string.xWillStart, R.string.toStartFormExamples),
		/**
		 * The verb's "I'll try doing X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson65.htm
		 */
		TO_TRY(new BaseTeInflector(), " miru", false, R.string.iLLTryDoingX, R.string.toTryFormExamples),
		/**
		 * The verb's "even IF (something were to happen), you must remember that (something else)..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson80.htm
		 */
		EVEN_IF2(new BaseTeInflector(), " mo", false, R.string.evenIfXYouMustRememberThatY, R.string.evenIf2FormExamples),
		/**
		 * The verb's "Can I do X? (Do I have a permission?)" form:
		 * http://www.timwerx.net/language/jpverbs/lesson66.htm
		 */
		CAN(new BaseTeInflector(), " mo ii", true, R.string.canIDoX, R.string.canFormExamples),
		/**
		 * The verb's "All he does is X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson66.htm
		 */
		ALL_HE_DOES(new BaseTeInflector(), " bakari", false, R.string.allHeDoesIsX, R.string.allHeDoesFormExamples),
		/**
		 * The verb's "I will certainly do X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson67.htm
		 */
		I_WILL_CERTAINLY_DO(new BaseTeInflector(), " oku", false, R.string.iWillCertainlyDoX, R.string.iWillCertainlyDoFormExamples),
		/**
		 * The verb's "To complete doing X / To do something unexpected / Something negative may happen" form:
		 * http://www.timwerx.net/language/jpverbs/lesson68.htm
		 */
		COMPLETE(new BaseTeInflector(), " simau", false, R.string.toCompleteDoingX, R.string.completeFormExamples),
		/**
		 * The verb's "How about doing X?" form:
		 * http://www.timwerx.net/language/jpverbs/lesson69.htm
		 */
		HOW_ABOUT(new BaseTeInflector(), " ha ikaga / dou desu ka", false, R.string.howAboutDoingX, R.string.howAboutFormExamples),
		/**
		 * The verb's "You must not do X!" form:
		 * http://www.timwerx.net/language/jpverbs/lesson70.htm
		 */
		FORBIDDEN(new BaseTeInflector(), " ha ikemasen", true, R.string.youMustNotDoX, R.string.forbiddenFormExamples),
		/**
		 * The verb's "I did X, then Y, then Z..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson71.htm
		 */
		CONTINUATION(new BaseTeInflector(), "", true, R.string.iDidXThenYThenZ, R.string.continuationFormExamples),
		/**
		 * The verb's "I did X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson72.htm
		 */
		PAST_TENSE(new BaseTaInflector(), "", true, R.string.iDidX, R.string.pastTenseFormExamples),
		/**
		 * The verb's "I did X recently" form:
		 * http://www.timwerx.net/language/jpverbs/lesson74.htm
		 */
		I_DID_RECENTLY(new BaseTaInflector(), " bakari", false, R.string.iDidXRecently, R.string.iDidRecentlyFormExamples),
		/**
		 * The verb's "I experienced X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson75.htm
		 */
		EXPERIENCED(new BaseTaInflector(), " koto ga aru", true, R.string.iExperiencedX, R.string.experiencedFormExamples),
		/**
		 * The verb's "If X, then" form:
		 * http://www.timwerx.net/language/jpverbs/lesson76.htm
		 */
		IF3(new BaseTaInflector(), "ra", false, R.string.ifXThen, R.string.if3FormExamples),
		/**
		 * The verb's "If X, then" form:
		 * http://www.timwerx.net/language/jpverbs/lesson77.htm
		 */
		IT_SEEMS_TO2(new BaseTaInflector(), " rasii", false, R.string.itSeemsToX, R.string.itSeemsTo2FormExamples),
		/**
		 * The verb's "I roughly did X" form:
		 * http://www.timwerx.net/language/jpverbs/lesson78.htm
		 */
		ROUGHLY(new BaseTaInflector(), "ri", false, R.string.iRoughlyDidX, R.string.roughlyFormExamples),
		/**
		 * The verb's "If I were to do X, ..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson79.htm
		 */
		SUPPOSITION(new BaseTaInflector(), " to sitara", false, R.string.ifIWereToDoX, R.string.suppositionFormExamples),
		/**
		 * The verb's "even IF X, you must remember that Y." form:
		 * http://www.timwerx.net/language/jpverbs/lesson80.htm
		 */
		EVEN_IF(new BaseTaInflector(), " to site mo", false, R.string.evenIfXYouMustRememberThatY, R.string.evenIfFormExamples),
		/**
		 * The verb's "When I did X..." form:
		 * http://www.timwerx.net/language/jpverbs/lesson81.htm
		 */
		WHEN(new BaseTaInflector(), " toki", false, R.string.whenIDidX, R.string.whenFormExamples),
		/**
		 * The verb's "I did X just now." form:
		 * http://www.timwerx.net/language/jpverbs/lesson82.htm
		 */
		I_DID_JUST_NOW(new BaseTaInflector(), " tokoro", false, R.string.iDidXJustNow, R.string.iDidJustNowFormExamples);

		private final AbstractBaseInflector inflector;
		private final String suffix;
		private final String suffixIchidan;
		/**
		 * Explanation of the form (e.g. I don't do something).
		 */
		public final int explanationResId;
		/**
		 * A new-line-separated list of example sentences, first in Japanese (a
		 * {@link RomanizationEnum#Hepburn}-romanized), then in English. The
		 * Japanese are directly taken from Tim Matheson's site, with very minor
		 * alternations. Therefore, you'll have to:
		 * <ul>
		 * <li>Convert the jp string to lower case</li>
		 * <li>Convert a lone-standing wa into ha</li>
		 * <li>Convert a lone o to wo</li>
		 * <li>Convert _text_ to katakana</li>
		 * </ul>
		 */
		public final int examples;

		/**
		 * Returns the examples as a list of string pairs.
		 * 
		 * @param context
		 *            used to resolve the string
		 * @param romanization
		 *            use optionally this romanization for Japanese sentences.
		 *            May be null.
		 * @return a list of pairs: first pair item is the Japanese sentence,
		 *         second pair item is the English translation. Never null.
		 */
		public final String[][] getExamples(final Context context, final RomanizationEnum romanization) {
			final String[] e = context.getString(examples).split("\n");
			final String[][] result = new String[e.length / 2][];
			for (int i = 0; i < e.length / 2; i++) {
				final String english = e[i * 2 + 1];
				String japanese = e[i * 2].toLowerCase();
				// fix wa and o
				japanese = japanese.replaceAll("\\s+wa\\s+", " ha ").replaceAll("\\s+o\\s+", " wo ");
				// convert words marked with _ to katakana
				boolean inUnderscore = false;
				final StringBuilder jp = new StringBuilder(japanese.length());
				for (final StringTokenizer t = new StringTokenizer(japanese, "_", true); t.hasMoreElements();) {
					final String token = t.nextToken();
					if (token.equals("_")) {
						inUnderscore = !inUnderscore;
					} else {
						jp.append(inUnderscore ? RomanizationEnum.Hepburn.toKatakana(token) : token);
					}
				}
				japanese = RomanizationEnum.Hepburn.toHiragana(jp.toString());
				if (romanization != null) {
					japanese = romanization.toRomaji(japanese);
				}
				result[i] = new String[] { japanese, english };
			}
			return result;
		}

		/**
		 * If true this expression is widely used.
		 */
		public final boolean basic;

		/**
		 * Creates a new form object instance.
		 * 
		 * @param inflector
		 *            the verb inflector, denotes the required base of the verb.
		 * @param suffix
		 *            additional suffix to add to the inflected verb
		 * @param basic
		 *            if true this expression is a basic one
		 * @param explanationResId
		 *            explanation of the form (e.g. I don't do something).
		 * @param examples
		 *            a new-line-separated list of example sentences, first in
		 *            Japanese (a {@link RomanizationEnum#Hepburn}-romanized),
		 *            then in English.
		 */
		private Form(final AbstractBaseInflector inflector, final String suffix, final boolean basic, final int explanationResId, final int examples) {
			this(inflector, suffix, null, basic, explanationResId, examples);
		}

		/**
		 * Creates a new form object instance.
		 * 
		 * @param inflector
		 *            the verb inflector, denotes the required base of the verb.
		 * @param suffixGodan
		 *            additional suffix to add to the inflected verb, only
		 *            applicable to Godan verbs.
		 * @param suffixIchidan
		 *            additional suffix to add to the inflected verb, only
		 *            applicable to Ichidan verbs.
		 * @param basic
		 *            if true this form is a basic one.
		 * @param explanationResId
		 *            explanation of the form (e.g. I don't do something).
		 * @param examples
		 *            a new-line-separated list of example sentences, first in
		 *            Japanese (a {@link RomanizationEnum#Hepburn}-romanized),
		 *            then in English.
		 */
		private Form(final AbstractBaseInflector inflector, final String suffixGodan, final String suffixIchidan, final boolean basic, final int explanationResId, final int examples) {
			this.inflector = inflector;
			this.suffix = suffixGodan;
			this.suffixIchidan = suffixIchidan;
			this.basic = basic;
			this.explanationResId = explanationResId;
			this.examples = examples;
		}

		/**
		 * Inflects a verb to the appropriate form.
		 * 
		 * @param verb
		 *            a verb, must be in {@link RomanizationEnum#NihonShiki}
		 *            romanization. Must be in Base 3 form. Note that "desu"
		 *            cannot be inflected.
		 * @param ichidan
		 *            true if the verb is ichidan, false if it is godan.
		 * @return inflected verb, never null, in the
		 *         {@link RomanizationEnum#NihonShiki} romanization. Kanji
		 *         characters are left as-is.
		 */
		public String inflect(final String verb, final boolean ichidan) {
			return inflector.inflect(verb, ichidan) + (suffixIchidan == null ? suffix : (ichidan ? suffixIchidan : suffix));
		}

		@Override
		public String toString() {
			return inflector.getName() + " + " + suffix;
		}

		/**
		 * Checks if this form is applicable to ichidan verbs.
		 * 
		 * @return true if it is applicable, false otherwise.
		 */
		public boolean appliesToIchidan() {
			return true;
		}
	}

}
