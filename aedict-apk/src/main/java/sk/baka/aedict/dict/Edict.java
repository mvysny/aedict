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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sk.baka.aedict.R;
import sk.baka.aedict.kanji.RomanizationEnum;
import android.widget.TextView;
import android.widget.TwoLineListItem;

/**
 * Contains various utilities for working with the Edict.
 * 
 * @author Martin Vysny
 */
public final class Edict {
	private Edict() {
		throw new AssertionError();
	}

	private static final Object[] MARKING_LIST = new Object[] { "adj-i", R.string.mark_adj_i, "adj-na", R.string.mark_adj_na, "adj-no", R.string.mark_adj_no, "adj-pn", R.string.mark_adj_pn, "adj-t",
			R.string.mark_adj_t, "adj-f", R.string.mark_adj_f, "adj", R.string.mark_adj, "adv", R.string.mark_adv, "adv-n", R.string.mark_adv_n, "adv-to", R.string.mark_adv_to, "aux",
			R.string.mark_aux, "aux-v", R.string.mark_aux_v, "aux-adj", R.string.mark_aux_adj, "conj", R.string.mark_conj, "ctr", R.string.mark_ctr, "exp", R.string.mark_exp, "id", R.string.mark_id,
			"int", R.string.mark_int, "iv", R.string.mark_iv, "n", R.string.mark_n, "n-adv", R.string.mark_n_adv, "n-pref", R.string.mark_n_pref, "n-suf", R.string.mark_n_suf, "n-t",
			R.string.mark_n_t, "num", R.string.mark_num, "pn", R.string.mark_pn, "pref", R.string.mark_pref, "prt", R.string.mark_prt, "suf", R.string.mark_suf, "v1", R.string.mark_v1, "v5",
			R.string.mark_v5, "v5aru", R.string.mark_v5aru, "v5b", R.string.mark_v5b, "v5g", R.string.mark_v5g, "v5k", R.string.mark_v5k, "v5k-s", R.string.mark_v5k_s, "v5m", R.string.mark_v5m,
			"v5n", R.string.mark_v5n, "v5r", R.string.mark_v5r, "v5r-i", R.string.mark_v5r_i, "v5s", R.string.mark_v5s, "v5t", R.string.mark_v5t, "v5u", R.string.mark_v5u, "v5u-s",
			R.string.mark_v5u_s, "v5uru", R.string.mark_v5uru, "v5z", R.string.mark_v5z, "vz", R.string.mark_vz, "vi", R.string.mark_vi, "vk", R.string.mark_vk, "vn", R.string.mark_vn, "vs",
			R.string.mark_vs, "vs-i", R.string.mark_vs_i, "vs-s", R.string.mark_vs_s, "vt", R.string.mark_vt, "Buddh", R.string.mark_Buddh, "MA", R.string.mark_MA, "comp", R.string.mark_comp, "food",
			R.string.mark_food, "geom", R.string.mark_geom, "gram", R.string.mark_gram, "ling", R.string.mark_ling, "math", R.string.mark_math, "mil", R.string.mark_mil, "physics",
			R.string.mark_physics, "X", R.string.mark_X, "abbr", R.string.mark_abbr, "arch", R.string.mark_arch, "ateji", R.string.mark_ateji, "chn", R.string.mark_chn, "col", R.string.mark_col,
			"derog", R.string.mark_derog, "eK", R.string.mark_eK, "ek", R.string.mark_ek, "fam", R.string.mark_fam, "fem", R.string.mark_fem, "gikun", R.string.mark_gikun, "hon", R.string.mark_hon,
			"hum", R.string.mark_hum, "iK", R.string.mark_iK, "id", R.string.mark_id, "io", R.string.mark_io, "m-sl", R.string.mark_m_sl, "male", R.string.mark_male, "male-sl", R.string.mark_male_sl,
			"ng", R.string.mark_ng, "oK", R.string.mark_oK, "obs", R.string.mark_obs, "obsc", R.string.mark_obsc, "ok", R.string.mark_ok, "on-mim", R.string.mark_on_mim, "poet", R.string.mark_poet,
			"pol", R.string.mark_pol, "rare", R.string.mark_rare, "sens", R.string.mark_sens, "sl", R.string.mark_sl, "uK", R.string.mark_uK, "uk", R.string.mark_uk, "vulg", R.string.mark_vulg, "P",
			R.string.mark_p };
	private static final Map<String, Integer> MARKINGS = new HashMap<String, Integer>();
	static {
		for (int i = 0; i < MARKING_LIST.length / 2; i++) {
			MARKINGS.put((String) MARKING_LIST[i * 2], (Integer) MARKING_LIST[i * 2 + 1]);
		}
	}

	/**
	 * A POS (Part of Speech) / Field of Application / Miscellaneous marking
	 * definition.
	 * 
	 * @author Martin Vysny
	 */
	public static final class Marking {
		/**
		 * Creates new marking object.
		 * 
		 * @param mark
		 *            The marking as defined in the <a
		 *            href="http://www.csse.monash.edu.au/~jwb/edict_doc.html"
		 *            >Edict specification</a>.
		 * @param descriptionRes
		 *            The description of the marking, a reference to
		 *            strings.xml.
		 */
		public Marking(final String mark, final int descriptionRes) {
			this.mark = mark;
			this.descriptionRes = descriptionRes;
		}

		/**
		 * The marking as defined in the <a
		 * href="http://www.csse.monash.edu.au/~jwb/edict_doc.html">Edict
		 * specification</a>.
		 */
		public final String mark;
		/**
		 * The description of the marking, a reference to strings.xml.
		 */
		public final int descriptionRes;

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Marking)) {
				return false;
			}
			return mark.equals(((Marking) o).mark);
		}

		@Override
		public int hashCode() {
			return mark.hashCode();
		}

		@Override
		public String toString() {
			return mark;
		}
	}

	/**
	 * Returns a list of POS or other markings which this entry is annotated
	 * with.
	 * 
	 * @param markings
	 *            a list of string marking identifiers.
	 * @return a list of markings, never null, may be empty.
	 */
	public static List<Marking> getMarkings(final List<String> markings) {
		final List<Marking> result = new ArrayList<Marking>();
		for (final String m : markings) {
			final Integer res = MARKINGS.get(m);
			if (res != null) {
				final Marking mark = new Marking(m, res);
				result.add(mark);
			}
		}
		return result;
	}

	/**
	 * Prints itself to a ListView item.
	 * 
	 * @param item
	 *            the item.
	 * @param romanize
	 *            if non-null then katakana/hiragana will be shown as romaji
	 */
	public static void print(final DictEntry e, final TwoLineListItem item, final RomanizationEnum romanize) {
		print(e, item.getText1(), item.getText2(), romanize);
	}

	/**
	 * Prints itself to a ListView item.
	 * 
	 * @param text1
	 *            first, larger textview.
	 * @param text2
	 *            second, smaller textview.
	 * @param romanize
	 *            if non-null then katakana/hiragana will be shown as romaji
	 */
	public static void print(final DictEntry e, final TextView text1, final TextView text2, final RomanizationEnum romanize) {
		text1.setText(e.formatJapanese(romanize));
		text2.setText(e.english);
	}
}
