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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Provides additional methods to aid testing of the android activity.
 * 
 * @author Martin Vysny
 * @param <T>
 *            the activity type
 */
public class AndroidTester<T extends Activity> {
	private final ActivityUnitTestCase<T> test;
	private final Class<T> activityClass;

	/**
	 * Creates new tester instance.
	 * 
	 * @param test
	 *            the JUnit test class instance.
	 * @param activityClass
	 *            the activity under test.
	 */
	public AndroidTester(final ActivityUnitTestCase<T> test, final Class<T> activityClass) {
		this.test = test;
		this.activityClass = activityClass;
	}

	/**
	 * Start the activity under test, in the same way as if it was started by
	 * ActivityUnitTestCase.startActivity, providing the arguments it supplied.
	 * When you use this method to start the activity, it will automatically be
	 * stopped by tearDown().
	 * <p/>
	 * This method will call onCreate(), if you wish to further exercise
	 * Activity life cycle methods, set the fullStart parameter to true.
	 * <p/>
	 * Do not call from your setUp() method. You must call this method from each
	 * of your test methods.
	 * 
	 * @param intent
	 *            The Intent as if supplied to startActivity(Intent).
	 * @param savedInstanceState
	 *            The instance state, if you are simulating this part of the
	 *            life cycle. Typically null.
	 * @param lastNonConfigurationInstance
	 *            This Object will be available to the Activity if it calls
	 *            getLastNonConfigurationInstance(). Typically null.
	 * @param fullStart
	 *            if true then the full activity start-up is performed:
	 *            onCreate(), onStart(), onResume(). If false then only the
	 *            onCreate() method is invoked.
	 * @return the activity instance.
	 */
	public T startActivity(final Intent intent, Bundle savedInstanceState, final Object lastNonConfigurationInstance, final boolean fullStart) {
		try {
			final Method m = ActivityUnitTestCase.class.getDeclaredMethod("startActivity", Intent.class, Bundle.class, Object.class);
			m.setAccessible(true);
			final T result = activityClass.cast(m.invoke(test, intent, savedInstanceState, lastNonConfigurationInstance));
			if (fullStart) {
				test.getInstrumentation().callActivityOnStart(test.getActivity());
				test.getInstrumentation().callActivityOnResume(test.getActivity());
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Starts the activity and asserts that it is really started.
	 * 
	 * @param intent
	 *            the intent to use.
	 * @param fullStart
	 *            if true then the full activity start-up is performed:
	 *            onCreate(), onStart(), onResume(). If false then only the
	 *            onCreate() method is invoked.
	 */
	public void startActivity(final Intent intent, final boolean fullStart) {
		final T activity = startActivity(intent, null, null, fullStart);
		assertSame(activity, test.getActivity());
		assertTrue(activityClass.isInstance(activity));
	}

	/**
	 * Fully starts the activity and asserts that it is really started.
	 * 
	 * @param intent
	 *            the intent to use.
	 */
	public void startActivity(final Intent intent) {
		startActivity(intent, true);
	}

	/**
	 * Starts the activity and asserts that it is really started.
	 */
	public void startActivity() {
		startActivity(new Intent(test.getInstrumentation().getContext(), activityClass));
	}

	/**
	 * Asserts that the current activity requested start of given activity,
	 * using one of the {@link Activity#startActivity(Intent)} or
	 * {@link Activity#startActivityForResult(Intent, int)} methods.
	 * 
	 * @param activity
	 *            the new activity
	 */
	public void assertRequestedActivity(final Class<? extends Activity> activity) {
		final Intent i = test.getStartedActivityIntent();
		if (i == null) {
			throw new AssertionError("The activity did not requested a start of another activity yet");
		}
		assertEquals(activity.getName(), i.getComponent().getClassName());
	}

	/**
	 * Sets text of given {@link TextView}.
	 * 
	 * @param textViewId
	 *            the text view ID
	 * @param text
	 *            the text to set
	 */
	public void setText(final int textViewId, final String text) {
		((TextView) test.getActivity().findViewById(textViewId)).setText(text);
	}

	/**
	 * Asserts that the text of given {@link TextView} is as expected.
	 * 
	 * @param textViewId
	 *            the text view ID
	 * @param text
	 *            the text to expect
	 */
	public void assertText(final int textViewId, final String text) {
		assertEquals(text, getText(textViewId));
	}

	/**
	 * Returns text value of given {@link TextView}.
	 * 
	 * @param textViewId
	 *            the text view ID
	 * @return the text.
	 */
	public String getText(final int textViewId) {
		return ((TextView) test.getActivity().findViewById(textViewId)).getText().toString();
	}

	/**
	 * Asserts that the text of given {@link TextView} is as expected.
	 * 
	 * @param textViewId
	 *            the text view ID
	 * @param stringId
	 *            the text to expect. References strings from the application,
	 *            not from the test module.
	 */
	public void assertText(final int textViewId, final int stringId) {
		assertText(textViewId, test.getActivity().getString(stringId));
	}

	/**
	 * Clicks on given {@link Button}.
	 * 
	 * @param buttonId
	 *            the button id
	 */
	public void click(final int buttonId) {
		test.getActivity().findViewById(buttonId).performClick();
	}

	/**
	 * Checks whether given {@link CompoundButton} is checked.
	 * 
	 * @param expected
	 *            the expected check state
	 * @param checkboxId
	 *            the button ID
	 */
	public void assertChecked(final boolean expected, final int checkboxId) {
		assertEquals(expected, ((CompoundButton) test.getActivity().findViewById(checkboxId)).isChecked());
	}

	/**
	 * Sets focus on given view.
	 * 
	 * @param view
	 *            the view to focus.
	 */
	public void focus(final View view) {
		if (view.isFocused()) {
			return;
		}
		if (!view.isFocusable()) {
			throw new AssertionError("The view " + view.getId() + " is not focusable");
		}
		if (!view.requestFocus()) {
			throw new AssertionError("The view " + view.getId() + " did not took the focus");
		}
	}

	/**
	 * Opens and activates context menu item for given view. Fails if the view
	 * does not provide a context menu. For ListView please use the
	 * {@link #contextMenu(AbsListView, int, int)} method.
	 * 
	 * @param view
	 *            the view
	 * @param menuId
	 *            the menu item ID to activate.
	 */
	public void contextMenu(final View view, final int menuId) {
		// view.performLongClick() does not work for ListView as the
		// ContextMenuInfo parameter is null
		if (view instanceof AbsListView) {
			throw new RuntimeException("Use the other contextMenu function for listview");
		}
		focus(view);
		if (!test.getInstrumentation().invokeContextMenuAction(test.getActivity(), menuId, 0)) {
			throw new AssertionError("Activation of menu item " + menuId + " failed for view " + view.getId() + " for unknown reasons. Check that there is menu item with such ID in the menu and it is enabled");
		}
	}

	/**
	 * Opens and activates context menu item for given ListView. Fails if the
	 * view does not provide a context menu.
	 * 
	 * @param view
	 *            the view
	 * @param menuId
	 *            the menu item ID which is to be activated. If the ID was not
	 *            assigned then the automatic ID generation is employed,
	 *            starting at 10000.
	 * @param item
	 *            the ListView item ordinal to click.
	 */
	public void contextMenu(final AbsListView view, final int menuId, final int item) {
		try {
			final Field m = View.class.getDeclaredField("mOnCreateContextMenuListener");
			m.setAccessible(true);
			final OnCreateContextMenuListener listener = (OnCreateContextMenuListener) m.get(view);
			final ContextMenuHandler handler = new ContextMenuHandler();
			final ContextMenu menu = (ContextMenu) Proxy.newProxyInstance(test.getActivity().getClassLoader(), new Class<?>[] { ContextMenu.class }, handler);
			// the trick here is to force the ListView to create context menu on
			// our hacky ContextMenu, which then traces the listeners.
			listener.onCreateContextMenu(menu, null, new AdapterContextMenuInfo(null, item, 0));
			handler.click(menuId);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private class ContextMenuHandler implements InvocationHandler {
		private final Map<Integer, MenuItem.OnMenuItemClickListener> listeners = new HashMap<Integer, MenuItem.OnMenuItemClickListener>();
		private int autogeneratedId = 10000;

		public boolean click(final int id) {
			if (listeners.get(id) == null) {
				throw new RuntimeException("No menu item with ID " + id + ": available: " + new ArrayList<Integer>(listeners.keySet()));
			}
			return listeners.get(id).onMenuItemClick(null);
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("add")) {
				final int id;
				if (method.getParameterTypes().length == 4) {
					id = (Integer) args[1];
				} else {
					id = autogeneratedId++;
				}
				return Proxy.newProxyInstance(test.getActivity().getClassLoader(), new Class<?>[] { MenuItem.class }, new InvocationHandler() {

					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						if (method.getName().equals("setOnMenuItemClickListener")) {
							listeners.put(id, (OnMenuItemClickListener) args[0]);
						}
						return proxy;
					}

				});
			}
			if (ContextMenu.class.isAssignableFrom(method.getReturnType())) {
				return proxy;
			}
			return null;
		}
	}

	/**
	 * Returns intent set via invocation to the
	 * {@link Activity#setResult(int, Intent)} call. Fails if no such intent was
	 * set.
	 * 
	 * @return intent, never null.
	 */
	public Intent getResultIntent() {
		try {
			final Field f = Activity.class.getDeclaredField("mResultData");
			f.setAccessible(true);
			final Intent result = (Intent) f.get(test.getActivity());
			if (result == null) {
				throw new AssertionError("result intent was not set via the setResult() call");
			}
			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Verifies whether given AdapterView contains given object.
	 * {@link Object#equals(Object)} is used to compare objects.
	 * 
	 * @param viewId
	 *            the view id.
	 * @param obj
	 *            the object, must not be null.
	 * @return true if the view contains given object, false otherwise.
	 */
	@SuppressWarnings("unchecked")
	public boolean isAdapterViewContains(final int viewId, final Object obj) {
		final AdapterView<? extends Adapter> view = (AdapterView<? extends Adapter>) test.getActivity().findViewById(viewId);
		return isAdapterContains(view.getAdapter(), obj);
	}

	/**
	 * Asserts that given adapter contains given object.
	 * {@link Object#equals(Object)} is used to compare objects.
	 * 
	 * @param adapter
	 *            the adapter, must not be null.
	 * @param obj
	 *            the object, must not be null.
	 * @return true if the view contains given object, false otherwise.
	 */
	public boolean isAdapterContains(final Adapter adapter, final Object obj) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (obj.equals(adapter.getItem(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Activates i-th option menu item.
	 * 
	 * @param menuId
	 *            the menu item ID which is to be activated. If the ID was not
	 *            assigned then the automatic ID generation is employed,
	 *            starting at 10000.
	 */
	public void optionMenu(int menuId) {
		final ContextMenuHandler handler = new ContextMenuHandler();
		final Menu menu = (Menu) Proxy.newProxyInstance(test.getActivity().getClassLoader(), new Class<?>[] { Menu.class }, handler);
		if (!test.getActivity().onCreateOptionsMenu(menu)) {
			throw new AssertionError("onCreateOptionsMenu returned false -> it requested the menu not to be shown");
		}
		if (!test.getActivity().onPrepareOptionsMenu(menu)) {
			throw new AssertionError("onPrepareOptionsMenu returned false -> it requested the menu not to be shown");
		}
		handler.click(menuId);
	}
}
