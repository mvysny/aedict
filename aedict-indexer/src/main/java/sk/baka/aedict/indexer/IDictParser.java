/**
Aedict - an EDICT browser for Android
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
package sk.baka.aedict.indexer;

import java.io.IOException;
import org.apache.lucene.document.Document;

/**
 * Serves for dictionary parsing. Parses lines from the dictionary and stores them in the Lucene document.
 * @author Martin Vysny
 */
public interface IDictParser {

    /**
     * Adds given line from given file type to the document.
     * @param line the file line
     * @param doc the document
     * @return true if the document should be added, false if next line will also be stored to the same document.
     */
    boolean addLine(final String line, final Document doc);

    /**
     * Invoked after the parsing finishes.
     * @throws IOException on i/o error
     */
    void onFinish() throws IOException;
}
