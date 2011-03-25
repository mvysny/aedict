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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import sk.baka.autils.ListBuilder;

/**
 * Holds a list of dictionaries and their versions.
 * @author Martin Vysny
 */
public class DictionaryVersions {

    public final Map<Dictionary, String> versions = new HashMap<Dictionary, String>();

    public String toExternal() {
        final ListBuilder sb = new ListBuilder("####");
        for (Entry<Dictionary, String> e : versions.entrySet()) {
            sb.add(e.getKey().toExternal() + "##" + e.getValue());
        }
        return sb.toString();
    }

    public static DictionaryVersions fromExternal(String external) {
        try {
            final DictionaryVersions dv = new DictionaryVersions();
            if (external.trim().length() == 0) {
                return dv;
            }
            for (String entry : external.split("####")) {
                final String[] e = entry.split("##");
                dv.versions.put(Dictionary.fromExternal(e[0]), e[1]);
            }
            return dv;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse '" + external + "'", ex);
        }
    }

    /**
     * Lists dictionaries which are older in this object than in the other object.
     * @param other the other versions object
     * @return a set of older dictionaries, not null, may be empty.
     */
    public Set<Dictionary> getOlderThan(DictionaryVersions other) {
        final Set<Dictionary> dict = new HashSet<Dictionary>();
        final Map<Dictionary, String> versions = new HashMap<Dictionary, String>(this.versions);
        for (Dictionary d : Dictionary.listInstalled()) {
            if (!versions.containsKey(d)) {
                versions.put(d, "20000101");
            }
        }
        for (Dictionary d : versions.keySet()) {
            if (!other.versions.containsKey(d)) {
                continue;
            }
            if (versions.get(d).compareTo(other.versions.get(d)) < 0) {
                dict.add(d);
            }
        }
        return dict;
    }
}
