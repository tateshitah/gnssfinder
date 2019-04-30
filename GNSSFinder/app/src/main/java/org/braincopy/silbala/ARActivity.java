package org.braincopy.silbala;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.braincopy.gnssfinder2.MainActivity;
import org.braincopy.gnssfinder2.R;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;

import static android.content.ContentValues.TAG;
//import com.google.android.gms.location.LocationServices;


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
 * @version 0.4.7
 */
public class ARActivity extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private float[] accelerometerValues = new float[3];
    private float[] magneticValues = new float[3];
    List<Sensor> listMag;
    List<Sensor> listAcc;

    private ARView arView;
    private LocationManager locationManager;
    public float lat, lon, alt;
    private GeomagneticField geomagneticField;
    CameraCallbackImpl callbackImple;
    private boolean isUsingGPS = false;

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    private static final String TAG = ARActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;


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

        lat = (float) 35.000000;
        lon = (float) 135.000000;
        alt = (float) 0.0f;

		/*
		added in 2019.4.22 by Hiro for implement of new location service.
		 */
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


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

    /**
     * Note: this method should be called after location permission has been granted.
     */
    @Override
    @SuppressWarnings("MissingPermission")
    protected void onResume() {
        super.onResume();
        if (isUsingGPS) {
		    /*
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);
            */
            if (!checkPermissions()) {
                requestPermissions();
            } else {
                    }
        }
        sensorManager.registerListener(this, listMag.get(0),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, listAcc.get(0),
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (isUsingGPS) {
            //locationManager.removeUpdates(this);
        }
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

    /**
     * location listener should not be working always.
     * @param arg0

     @Override public void onLocationChanged(Location arg0) {
     lat = (float) arg0.getLatitude();
     lon = (float) arg0.getLongitude();

     geomagneticField = new GeomagneticField((float) arg0.getLatitude(),
     (float) arg0.getLongitude(), (float) arg0.getAltitude(),
     new Date().getTime());
     if (!isUsingGPS) {
     isUsingGPS = true;
     }
     }*/

    /**
     * Note: this method should be called after location permission has been granted.
     * <p>
     * start working
     * get location
     *
     * @return get location successfully or not
     */
    @SuppressWarnings("MissingPermission")
    public void getLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, getOnCompleteListener());
        /*
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            lat = (float) mLastLocation.getLatitude();
                            lon = (float) mLastLocation.getLongitude();
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });*/
    }

    /**
     * should be overrided.
     * @return
     */
    protected OnCompleteListener getOnCompleteListener(){
        return new OnCompleteListener<Location>() {
            @Override
            public void onComplete(Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    mLastLocation = task.getResult();
                    lat = (float) mLastLocation.getLatitude();
                    lon = (float) mLastLocation.getLongitude();
                } else {
                    Log.w(TAG, "getLastLocation:exception", task.getException());
                    showSnackbar(getString(R.string.no_location_detected));
                }
            }
        };
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(ARActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    /**
     * I suppose this method is created for getting user permission for the location.
     */
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }


    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.cam_view);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    public boolean isUsingGPS() {
        return isUsingGPS;
    }

    public void setUsingGPS(boolean usingGPS_) {
        this.isUsingGPS = usingGPS_;
    }

	/*
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

	}*/

    /**
     * @param arview_
     */
    public void setARView(ARView arview_) {
        this.arView = arview_;
        callbackImple.setOverlayView(arView);
        addContentView(arView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

    }

    /**
     * @return
     */
    public ARView getARView() {
        return this.arView;
    }

}
