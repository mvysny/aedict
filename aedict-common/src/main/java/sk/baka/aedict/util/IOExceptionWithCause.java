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
package sk.baka.aedict.util;

import java.io.IOException;

/**
 * An IOException which allows the Throwable cause to be set.
 * 
 * @author Martin Vysny
 */
public class IOExceptionWithCause extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates new exception
     *
     * @param detailMessage
     *            the message
     * @param cause
     *            the cause
     */
    public IOExceptionWithCause(String detailMessage, final Throwable cause) {
        super(detailMessage);
        initCause(cause);
    }
}
