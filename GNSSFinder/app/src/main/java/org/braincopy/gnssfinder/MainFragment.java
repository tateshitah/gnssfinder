package org.braincopy.gnssfinder;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class MainFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_main,
				container, false);

		/*
		 * Setting for camera button.
		 */
		ImageButton cameraButton = (ImageButton) rootView
				.findViewById(R.id.imageCameraButton);
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// CameraFragment cameraFragment = new CameraFragment();
				// FragmentTransaction transaction = getFragmentManager()
				// .beginTransaction();
				// transaction.addToBackStack(null);
				// transaction.replace(R.id.container, cameraFragment);
				// transaction.commit();
				Intent intent = new Intent(getActivity()
						.getApplicationContext(), CameraActivity.class);
				startActivity(intent);

			}
		});
		/*
		 * Setting for map button.
		 */
		ImageButton mapButton = (ImageButton) rootView
				.findViewById(R.id.imageMapButton);
		mapButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MapFragment mapFragment = new MapFragment();
				// View view = getActivity().findViewById(R.id.container);
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				transaction.addToBackStack(null);
				transaction.replace(R.id.container, mapFragment);
				transaction.commit();
			}
		});
		/*
		 * Setting for setting button.
		 */
		ImageButton settingButton = (ImageButton) rootView
				.findViewById(R.id.settingImageButton1);
		settingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SettingFragment settingFragment = new SettingFragment();
				// View view = getActivity().findViewById(R.id.container);
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				transaction.addToBackStack(null);
				transaction.replace(R.id.container, settingFragment);
				transaction.commit();
			}
		});
		return rootView;
	}
}
