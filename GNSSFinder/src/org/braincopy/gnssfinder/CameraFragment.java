package org.braincopy.gnssfinder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * a Fragment of for camera mode of GNSS Finder. The coordinate system of
 * actual_orientation has been adjusted as follows:
 * <ol>
 * <li>right-handed coordinate system</li>
 * <li>when the camera directs east without any lean and incline, azimuth,
 * pitch, and roll will be 0 (zero).</li>
 * <li>x axis is direction of moving,</li>
 * <li>y axis is horizontal right direction,</li>
 * <li>z axis is vertical down direction,</li>
 * <li>angle increases for clockwise for all axis. The coordinate system should
 * be adjusted for each devices.</li>
 * </ol>
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class CameraFragment extends Fragment implements SensorEventListener,
		LocationListener {
	private SensorManager sensorManager;
	private float[] accelerometerValues = new float[3];
	private float[] magneticValues = new float[3];
	List<Sensor> listMag;
	List<Sensor> listAcc;

	private ARView arView;
	private CameraView cameraView;
	private LocationManager locationManager;
	private float lat, lon;
	private GeomagneticField geomagneticField;
	private Satellite[] satellites;
	private SatelliteInfoWorker worker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// super.onCreate(savedInstanceState);
		cameraView = new CameraView(getActivity());
		container.addView(cameraView, new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		final View rootView = inflater.inflate(R.layout.fragment_camera,
				container, false);

		arView = new ARView(this);
		arView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MainFragment mainFragment = new MainFragment();
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				transaction.addToBackStack(null);
				// View view = getActivity().findViewById(R.id.cameraPreview);
				transaction.replace(R.id.container, mainFragment);
				transaction.commit();
				return true;
			}
		});

		container.addView(arView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		getActivity().setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		sensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		listMag = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		listAcc = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

		lat = (float) 35.660994;
		lon = (float) 139.677619;
		final String gnssString = SettingFragment.getGNSSString(getActivity());
		ConnectivityManager cm = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			worker = new SatelliteInfoWorker();
			worker.setLatLon(lat, lon);
			worker.setCurrentDate(new Date(System.currentTimeMillis()));
			worker.setGnssString(gnssString);
			worker.start();
			worker.setStatus(SatelliteInfoWorker.CONNECTED);
			arView.setStatus("connected");
		}
		return rootView;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		sensorManager.registerListener(this, listMag.get(0),
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, listAcc.get(0),
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	@Override
	public void onPause() {
		super.onPause();
		cameraView.stopPreviewAndFreeCamera();
	}

	@Override
	public void onStop() {
		super.onStop();
		locationManager.removeUpdates(this);
		sensorManager.unregisterListener(this);
		cameraView.stopPreviewAndFreeCamera();

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			accelerometerValues = event.values.clone();
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			magneticValues = event.values.clone();
			break;
		}

		if (magneticValues != null && accelerometerValues != null) {
			float[] R = new float[16];
			float[] outR = new float[16];

			float[] I = new float[16];

			SensorManager.getRotationMatrix(R, I, accelerometerValues,
					magneticValues);
			SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_MINUS_X,
					SensorManager.AXIS_Z, outR);
			float[] actual_orientation = new float[3];
			SensorManager.getOrientation(outR, actual_orientation);

			if (geomagneticField != null) {
				actual_orientation[0] = actual_orientation[0]
						+ geomagneticField.getDeclination();
			}

			actual_orientation[0] = (float) (actual_orientation[0] + Math.PI * 0.5);
			actual_orientation[2] = -1 * actual_orientation[2];

			arView.drawScreen(actual_orientation, lat, lon);
		}
		if (worker != null) {
			if (worker.getStatus() == SatelliteInfoWorker.INFORMATION_LOADED_WO_LOCATION) {
				this.satellites = worker.getSatArray();
				if (loadImages()) {
					worker.setStatus(SatelliteInfoWorker.IMAGE_LOADED);
					arView.setSatellites(satellites);
					arView.setStatus("getting location...");
				}
			} else if (worker.getStatus() == SatelliteInfoWorker.INFORMATION_LOADED_W_LOCATION) {
				this.satellites = worker.getSatArray();
				if (loadImages()) {
					worker.setStatus(SatelliteInfoWorker.COMPLETED);
					arView.setSatellites(satellites);
				}
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private boolean loadImages() {
		boolean result = false;
		Resources resources = this.getResources();
		InputStream is;
		try {
			AssetManager assetManager = resources.getAssets();
			is = assetManager.open("satelliteDataBase.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			ArrayList<String[]> datalist = new ArrayList<String[]>();
			String buf = null;
			try {
				while ((buf = br.readLine()) != null) {
					datalist.add(buf.split("\t"));
				}
				String gnssStr = "";
				for (int i = 0; i < satellites.length; i++) {
					gnssStr = getGnssStr(satellites[i].getCatNo(), datalist);
					if (gnssStr != null) {
						satellites[i].setDescription(getSatInfo(
								satellites[i].getCatNo(), datalist));
						if (gnssStr.equals("qzss")) {
							satellites[i]
									.setImage(BitmapFactory.decodeResource(
											resources, R.drawable.qzss));
						} else if (gnssStr.equals("galileo")) {
							satellites[i].setImage(BitmapFactory
									.decodeResource(resources,
											R.drawable.galileo));
						} else if (gnssStr.equals("gpsBlockIIF")) {
							satellites[i].setImage(BitmapFactory
									.decodeResource(resources, R.drawable.iif));
						}
					}
				}
				result = true;
			} catch (IOException e) {
				Log.e("hiro",
						"failed when to read a line of the satellite database text file."
								+ e);
				e.printStackTrace();
			}
			is.close();
			br.close();
		} catch (FileNotFoundException e) {
			Log.e("hiro", "" + e);
			e.printStackTrace();
		} catch (IOException e1) {
			Log.e("hiro", "" + e1);
			e1.printStackTrace();
		}

		return result;
	}

	private String getSatInfo(String catNo, ArrayList<String[]> datalist) {
		String result = null;
		String[] tmpStrArray = null;
		for (int i = 0; i < datalist.size(); i++) {
			tmpStrArray = datalist.get(i);
			if (catNo.equals(tmpStrArray[0])) {
				result = tmpStrArray[3];
				break;
			}
		}
		return result;
	}

	private String getGnssStr(String catNo, ArrayList<String[]> list) {
		String result = null;
		String[] tmpStrArray = null;
		for (int i = 0; i < list.size(); i++) {
			tmpStrArray = list.get(i);
			if (catNo.equals(tmpStrArray[0])) {
				result = tmpStrArray[2];
				break;
			}
		}
		return result;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		lat = (float) arg0.getLatitude();
		lon = (float) arg0.getLongitude();

		geomagneticField = new GeomagneticField((float) arg0.getLatitude(),
				(float) arg0.getLongitude(), (float) arg0.getAltitude(),
				new Date().getTime());
		if (this.satellites != null && worker != null) {
			if (worker.getStatus() == SatelliteInfoWorker.IMAGE_LOADED) {
				worker = new SatelliteInfoWorker();
				worker.setStatus(SatelliteInfoWorker.IMAGE_LOADED);
				worker.setLatLon(lat, lon);
				worker.setCurrentDate(new Date(System.currentTimeMillis()));
				worker.start();
			} else if (worker.getStatus() == SatelliteInfoWorker.COMPLETED) {
				this.arView
						.setStatus("position is updated and information loaded.");
			}
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
}
