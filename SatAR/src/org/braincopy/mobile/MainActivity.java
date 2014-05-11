package org.braincopy.mobile;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * Main Activity of SatAR. The coordinate system of actual_orientation has been
 * adjusted as follows:
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
public class MainActivity extends Activity implements SensorEventListener,
		LocationListener {
	private SensorManager sensorManager;
	private float[] accelerometerValues = new float[3];
	private float[] magneticValues = new float[3];
	List<Sensor> listMag;
	List<Sensor> listAcc;

	private ARView arView;
	private LocationManager locationManager;
	private float lat, lon;
	private GeomagneticField geomagneticField;
	private Satellite[] satellites;
	private SatelliteInfoWorker worker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * getWindow().clearFlags(
		 * WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		 * getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		 * requestWindowFeature(Window.FEATURE_NO_TITLE);
		 */
		arView = new ARView(this);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		listMag = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		listAcc = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

		setContentView(new CameraView(this));
		addContentView(arView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		lat = (float) 35.660994;
		lon = (float) 139.677619;

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// final Handler handler = new Handler();
		if (networkInfo != null && networkInfo.isConnected()) {
			worker = new SatelliteInfoWorker();
			worker.setLatLon(lat, lon);
			worker.setCurrentDate(new Date(System.currentTimeMillis()));
			/*
			 * worker.setMessageListener(new MessageListener() {
			 * 
			 * @Override public void sendMessage(String message) {
			 * handler.post(new Runnable() {
			 * 
			 * @Override public void run() { // worker.createSatelliteArray(lat,
			 * lon); arView.setStatus("connected.");
			 * 
			 * } }); } });
			 */
			worker.start();
			worker.setStatus(SatelliteInfoWorker.CONNECTED);
			arView.setStatus("connected");
		}
		/*
		 * satellites = new Satellite[2]; satellites[0] = new Satellite(this);
		 * satellites[1] = new Satellite(this);
		 * satellites[1].setAzimuth(120.0f); arView.setSatellites(satellites);
		 */
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
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
	protected void onResume() {
		super.onResume();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, this);
		sensorManager.registerListener(this, listMag.get(0),
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, listAcc.get(0),
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	@Override
	public void onStop() {
		super.onStop();
		locationManager.removeUpdates(this);
		sensorManager.unregisterListener(this);

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
			if (worker.getStatus() == SatelliteInfoWorker.RECEIVED_SATINFO) {
				this.satellites = worker.getSatArray();
				if (loadImages()) {
					worker.setStatus(SatelliteInfoWorker.COMPLETED);
					arView.setSatellites(satellites);
				}
			} else if (worker.getStatus() == SatelliteInfoWorker.COMPLETED) {
				arView.setStatus("Satellite information loaded.");
			}
		}
	}

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
					satellites[i].setDescription(getSatInfo(
							satellites[i].getCatNo(), datalist));
					if (gnssStr.equals("qzss")) {
						satellites[i].setImage(BitmapFactory.decodeResource(
								resources, R.drawable.qzss));
					} else if (gnssStr.equals("galileo")) {
						satellites[i].setImage(BitmapFactory.decodeResource(
								resources, R.drawable.galileo));
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
		if (this.satellites != null) {
			// satellites = worker.createSatelliteArray(lat, lon);
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
