package org.braincopy.gnssfinder;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

/**
 * Setting page of this application has following functions:
 * <ul>
 * <li>GNSS</li>
 * <li>Location Information (developing)</li>
 * <li>Camera on off (developing)</li>
 * </ul>
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class SettingFragment extends Fragment {

	/**
	 * a kind of hashtable for saving setting information.
	 */
	private SharedPreferences pref;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_settings,
				container, false);

		/*
		 * initialize setting by loading setting information from shared
		 * preference.
		 */
		pref = getActivity().getSharedPreferences("gnssfinder",
				Activity.MODE_PRIVATE);

		boolean isGpsBlockIIF = pref.getBoolean("gpsBlockIIF", false);
		final Switch gpsBlockIIFSwitch = (Switch) rootView
				.findViewById(R.id.gpsBlockIIFSwitch);
		gpsBlockIIFSwitch.setChecked(isGpsBlockIIF);

		boolean isGalileo = pref.getBoolean("galileo", false);
		final Switch galileoSwitch = (Switch) rootView
				.findViewById(R.id.galileoSwitch);
		galileoSwitch.setChecked(isGalileo);

		boolean isQzss = pref.getBoolean("qzss", false);
		final Switch qzssSwitch = (Switch) rootView
				.findViewById(R.id.qzssSwitch);
		qzssSwitch.setChecked(isQzss);

		/*
		 * OK button
		 */
		Button okButton = (Button) rootView.findViewById(R.id.okButton);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean("gpsBlockIIF", gpsBlockIIFSwitch.isChecked());
				editor.putBoolean("galileo", galileoSwitch.isChecked());
				editor.putBoolean("qzss", qzssSwitch.isChecked());
				editor.commit();

				goBackToHome();
			}
		});

		/*
		 * cancel button
		 */
		Button cancelButton = (Button) rootView.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				goBackToHome();
			}
		});

		return rootView;
	}

	void goBackToHome() {
		MainFragment mainFragment = new MainFragment();
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.addToBackStack(null);
		transaction.replace(R.id.container, mainFragment);
		transaction.commit();
	}

	/**
	 * 
	 * @param activity
	 * @return
	 */
	public static String getGNSSString(Activity activity) {
		/*
		 * initialize setting by loading setting information from shared
		 * preference.
		 */
		SharedPreferences pref = activity.getSharedPreferences("gnssfinder",
				Activity.MODE_PRIVATE);
		boolean isGpsBlockIIF = pref.getBoolean("gpsBlockIIF", true);
		boolean isGalileo = pref.getBoolean("galileo", false);
		boolean isQzss = pref.getBoolean("qzss", false);
		String gnssString = "";
		if (isGpsBlockIIF) {
			gnssString += "G";
		}
		if (isGalileo) {
			gnssString += "E";
		}
		if (isQzss) {
			gnssString += "J";
		}
		return gnssString;
	}

}
