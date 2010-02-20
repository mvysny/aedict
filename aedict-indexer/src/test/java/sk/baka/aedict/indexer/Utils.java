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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.*;

/**
 * Utilities
 * @author Martin Vysny
 */
public class Utils {

    public static URL getResource(final String res) {
        final URL edictGz = Thread.currentThread().getContextClassLoader().getResource(res);
        if (edictGz == null) {
            throw new AssertionError("The " + res + " resource is missing");
        }
        return edictGz;
    }

    public static void index(final String sw, final String res, final FileTypeEnum fileType) throws Exception {
        final URL dictGz = Utils.getResource(res);
        final List<String> params = new ArrayList<String>(Arrays.asList("-u", dictGz.toString(), "-g"));
        if (sw != null) {
            params.add(sw);
        }
        new Main(params.toArray(new String[0])).run();
        // check that the target file exists
        assertTrue(new File(fileType.getTargetFileName()).exists());
        final File targetFile = new File("target/" + fileType.getTargetFileName());
        targetFile.delete();
        FileUtils.moveFile(new File(fileType.getTargetFileName()), targetFile);
    }
}
