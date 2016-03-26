package org.braincopy.gnssfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
			DialogFragment newFragment = new InformationDialogFragment();
			newFragment.show(getFragmentManager(), "test");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class InformationDialogFragment extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			LayoutInflater inflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View content = inflater.inflate(R.layout.dialog_info, null);
			InputStream is = null;
			BufferedReader br = null;
			String text = "";

			try {
				is = getActivity().getAssets().open("Info.txt");
				br = new BufferedReader(new InputStreamReader(is));

				String str;
				while ((str = br.readLine()) != null) {
					text += str + "\n";
				}
				if (is != null)
					is.close();
				if (br != null)
					br.close();
			} catch (IOException e) {
				System.err.println("exception when trying to open Info.txt"
						+ e.getMessage());
				e.printStackTrace();
			}
			TextView infoTextView = (TextView) content
					.findViewById(R.id.infoTextView);
			infoTextView.setText(text);

			builder.setView(content);

			builder.setMessage("About GNSSFinder").setNegativeButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User cancelled the dialog
						}
					});
			// Create the AlertDialog object and return it
			return builder.create();
		}
	}
}
