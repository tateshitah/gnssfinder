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
import android.widget.CheckBox;
import android.widget.Switch;

/**
 * Setting page of this application has following functions:
 * <ul>
 * <li>GNSS</li>
 * <li>Location Information</li>
 * <li>Camera on off</li>
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
		final CheckBox gpsBlockIIFCheckBox = (CheckBox) rootView
				.findViewById(R.id.gpsBlockIIFcheckBox);
		gpsBlockIIFCheckBox.setChecked(isGpsBlockIIF);
		final Switch gpsBlockIIFSwitch = (Switch) rootView
				.findViewById(R.id.gpsBlockIIFSwitch);
		gpsBlockIIFSwitch.setChecked(isGpsBlockIIF);

		boolean isGalileo = pref.getBoolean("galileo", false);
		final CheckBox galileoCheckBox = (CheckBox) rootView
				.findViewById(R.id.galileoCheckBox);
		galileoCheckBox.setChecked(isGalileo);
		final Switch galileoSwitch = (Switch) rootView
				.findViewById(R.id.galileoSwitch);
		galileoSwitch.setChecked(isGalileo);

		boolean isQzss = pref.getBoolean("qzss", false);
		final CheckBox qzssCheckBox = (CheckBox) rootView
				.findViewById(R.id.qzssCheckBox);
		qzssCheckBox.setChecked(isQzss);
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
				editor.putBoolean("gpsBlockIIF",
						gpsBlockIIFCheckBox.isChecked());
				editor.putBoolean("galileo", galileoCheckBox.isChecked());
				editor.putBoolean("qzss", qzssCheckBox.isChecked());
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
}
