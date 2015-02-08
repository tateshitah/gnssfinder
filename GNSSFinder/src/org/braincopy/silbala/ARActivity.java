package org.braincopy.silbala;

import java.util.Date;
import java.util.List;

import org.braincopy.gnssfinder.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

/**
 * The coordinate system of actual_orientation has been adjusted as follows:
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
 * Call me maybe, Royals, Grace Kelly
 * 
 * @author Hiroaki Tateshita
 * @version 0.4.6
 * 
 */
public class ARActivity extends Activity implements SensorEventListener,
		LocationListener {
	private SensorManager sensorManager;
	private float[] accelerometerValues = new float[3];
	private float[] magneticValues = new float[3];
	List<Sensor> listMag;
	List<Sensor> listAcc;

	private ARView arView;
	private LocationManager locationManager;
	public float lat, lon;
	private GeomagneticField geomagneticField;
	CameraCallbackImpl callbackImple;
	private boolean isUsingGPS = false;

	/**
	 * flag to show the AR Object is touched or not. it should be in ARObject
	 * class?
	 */
	// public boolean[] touchedFlags;

	public final float TOUCH_AREA_SIZE = 150;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ar);

		callbackImple = new CameraCallbackImpl();
		SurfaceView camView = (SurfaceView) findViewById(R.id.cam_view);
		SurfaceHolder holder = camView.getHolder();
		// holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(callbackImple);

		ImageButton shutterButton = (ImageButton) findViewById(R.id.cameraShutter);
		shutterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				callbackImple.takePicture();

			}
		});

		callbackImple.setContentResolver(this.getContentResolver());

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		listMag = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		listAcc = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		lat = (float) 35.660994;
		lon = (float) 139.677619;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
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
		this.isUsingGPS = false;

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

	}

	@Override
	public void onLocationChanged(Location arg0) {
		lat = (float) arg0.getLatitude();
		lon = (float) arg0.getLongitude();

		geomagneticField = new GeomagneticField((float) arg0.getLatitude(),
				(float) arg0.getLongitude(), (float) arg0.getAltitude(),
				new Date().getTime());
		if (!isUsingGPS) {
			isUsingGPS = true;
		}
	}

	public boolean isUsingGPS() {
		return isUsingGPS;
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

	/**
	 * 
	 * @param arview_
	 */
	public void setARView(ARView arview_) {
		this.arView = arview_;
		callbackImple.setOverlayView(arView);
		addContentView(arView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

	}

	/**
	 * 
	 * @return
	 */
	public ARView getARView() {
		return this.arView;
	}

}
