package sk.baka.aedict.skip;

import sk.baka.aedict.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * A first form of the SKIP lookup wizard.
 * 
 * @author Martin Vysny
 */
public class Skip1Activity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.skip1);
	}
}
