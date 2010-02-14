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

import java.net.MalformedURLException;
import java.net.URL;

import sk.baka.autils.ListBuilder;

/**
 * Enumerates possible types of dictionaries.
 * 
 * @author Martin Vysny
 */
public enum DictTypeEnum {
	/**
	 * The EDICT dictionary.
	 */
	Edict {
		@Override
		public String getLuceneQuery(SearchQuery query) {
			final ListBuilder sb = new ListBuilder(" OR ");
			for (final String q : query.query) {
				sb.add(q.trim());
			}
			return sb.toString();
		}

		@Override
		public String getDefaultDictionaryLoc() {
			return "index";
		}

		@Override
		public URL getDownloadSite() {
			try {
				return new URL(DICT_BASE_LOCATION_URL + "edict-lucene.zip");
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	},
	/**
	 * The KanjiDic dictionary.
	 */
	Kanjidic {
		@Override
		public String getLuceneQuery(SearchQuery q) {
			// query can be null in case we are performing e.g. a pure SKIP
			// lookup
			final ListBuilder qb = new ListBuilder(" AND ");
			if (q.query != null) {
				if (q.query.length != 1) {
					throw new IllegalStateException("Kanjidic search requires a single kanji character search");
				}
				qb.add("kanji:" + q.query[0].trim());
			}
			if (q.strokeCount != null) {
				final ListBuilder sb = new ListBuilder(" OR ");
				final int plusMinus = q.strokesPlusMinus == null ? 0 : q.strokesPlusMinus;
				if ((plusMinus > 3) || (plusMinus < 0)) {
					throw new IllegalStateException("Invalid value: " + q.strokesPlusMinus);
				}
				for (int strokes = q.strokeCount - plusMinus; strokes <= q.strokeCount + plusMinus; strokes++) {
					sb.add("strokes:" + strokes);
				}
				qb.add("(" + sb.toString() + ")");
			}
			if (q.skip != null) {
				qb.add("skip:" + q.skip);
			}
			if (q.radical != null) {
				qb.add("radical:" + q.radical);
			}
			return qb.toString();
		}

		@Override
		public String getDefaultDictionaryLoc() {
			return "index-kanjidic";
		}

		@Override
		public URL getDownloadSite() {
			try {
				return new URL(DICT_BASE_LOCATION_URL + "kanjidic-lucene.zip");
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	},
	/**
	 * The Tanaka Corpus containing example sentences.
	 */
	Tanaka {
		@Override
		public String getLuceneQuery(SearchQuery query) {
			if (query.query.length != 1) {
				throw new IllegalStateException("Tanaka search requires a single kanji character search");
			}
			return (query.isJapanese ? "japanese:" + query.query[0] + " OR jp-deinflected:" + query.query[0] : "english:" + query.query[0]);
		}

		@Override
		public String getDefaultDictionaryLoc() {
			return "index-tanaka";
		}

		@Override
		public URL getDownloadSite() {
			try {
				return new URL(DICT_BASE_LOCATION_URL + "tanaka-lucene.zip");
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	};
	/**
	 * A base http:// location of the dictionary files.
	 */
	public static final String DICT_BASE_LOCATION_URL = "http://baka.sk/aedict/";

	/**
	 * Returns a Lucene query which matches given query as close as possible.
	 * 
	 * @param query
	 *            the query.
	 * @return the Apache Lucene query
	 */
	public abstract String getLuceneQuery(final SearchQuery query);

	/**
	 * The default dictionary location. A directory name without the
	 * '/sdcard/aedict/' prefix.
	 * 
	 * @return the default dictionary location.
	 */
	public abstract String getDefaultDictionaryLoc();

	/**
	 * Returns the address where the zipped Lucene index of the dictionary can
	 * be downloaded.
	 * 
	 * @return the URL pointing to a zip file.
	 */
	public abstract URL getDownloadSite();
}
