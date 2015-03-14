package org.braincopy.gnssfinder;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

//import android.support.v4.app.Fragment;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			// getFragmentManager().beginTransaction()
			// .add(R.id.container, new PlaceholderFragment()).commit();
			getFragmentManager().beginTransaction()
					.add(R.id.container, new MainFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_bar_back) {
			MainFragment mainFragment = new MainFragment();
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction.addToBackStack(null);
			transaction.replace(R.id.container, mainFragment);
			transaction.commit();

			return true;
		} else if (id == R.id.action_close) {
			this.moveTaskToBack(true);
			return true;
		} else if (id == R.id.action_info) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
