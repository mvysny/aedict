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

package sk.baka.aedict;

import android.content.Intent;
import android.widget.Adapter;

/**
 * Tests the {@link StrokeOrderActivity} class.
 * 
 * @author Martin Vysny
 */
public class StrokeOrderActivityTest extends AbstractAedictTest<StrokeOrderActivity> {
	public StrokeOrderActivityTest() {
		super(StrokeOrderActivity.class);
	}

	public void testSimpleStart() {
		final Intent i = new Intent(getInstrumentation().getContext(), StrokeOrderActivity.class);
		i.putExtra(StrokeOrderActivity.INTENTKEY_KANJILIST, "母は留守です。");
		tester.startActivity(i);
		final Adapter a = getActivity().getListAdapter();
		assertTrue(tester.isAdapterContains(a, '母'));
		assertTrue(tester.isAdapterContains(a, 'は'));
		assertTrue(tester.isAdapterContains(a, '留'));
		assertTrue(tester.isAdapterContains(a, '守'));
		assertTrue(tester.isAdapterContains(a, 'で'));
		assertTrue(tester.isAdapterContains(a, 'す'));
		assertTrue(tester.isAdapterContains(a, '。'));
		assertEquals(7, getActivity().getListAdapter().getCount());
	}
}
