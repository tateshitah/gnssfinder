package org.braincopy.silbala;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

//Put "R package" of YOUR project
import org.braincopy.gnssfinder2.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * This class is supposed to be used an Activity of Android by being extended by
 * developers who use Silbara library.
 * <p>
 * This class will provide following functions:
 * sensors such as GPS, accelerometer, magnetic sensors, and Camera.
 * The Developers should implement create relative position data of the objects
 * which they want to show as AR on the display of Android.
 * <p>
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
 *
 * @author Hiroaki Tateshita
 * @version 0.6.0
 */
public class ARActivity extends Activity implements SensorEventListener, TextureView.SurfaceTextureListener {
    private SensorManager sensorManager;
    private float[] accelerometerValues = new float[3];
    private float[] magneticValues = new float[3];
    List<Sensor> listMag;
    List<Sensor> listAcc;

    private ARView arView;
    private LocationManager locationManager;
    public float lat, lon, alt;
    private GeomagneticField geomagneticField;
    //    CameraCallbackImpl callbackImple;
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

    /**
     * for camera2
     */
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    /**
     * for camera2
     */
    private String mCameraId;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            //When succeeded to connect CameraDevice, set the object as field member of this class.
            mCameraDevice = cameraDevice;
            try {
                createCameraPreviewSession();    //
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };
    private CameraDevice mCameraDevice;
    CameraCaptureSession mCaptureSession = null;
    CaptureRequest mPreviewRequest = null;

    TextureView mTextureView;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private SurfaceView mCamView;

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        CameraManager mCameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        String[] cameraIdList = null;
        try {
            if (mCameraManager != null) {
                cameraIdList = mCameraManager.getCameraIdList();
                for (String cameraId : cameraIdList) {
                    CameraCharacteristics characteristics
                            = mCameraManager.getCameraCharacteristics(cameraId);
                    if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                        mCameraId = cameraId;

                        mCameraManager.openCamera(mCameraId, mStateCallback, null);


                        return;
                    } else {
                        continue;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
//        manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
    }

    private void createCameraPreviewSession() throws CameraAccessException {
        SurfaceTexture texture = mTextureView.getSurfaceTexture();

        // set meaningless value
        texture.setDefaultBufferSize(1080, 1920);

        Surface surface = new Surface(texture);

        // create CaptureRequest
        mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewRequestBuilder.addTarget(surface);

        // create CameraCaptureSession
        mCameraDevice.createCaptureSession(Arrays.asList(surface),
                new CameraCaptureSession.StateCallback() {

                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        //when Session configuration is ready, preview will start
                        mCaptureSession = cameraCaptureSession;
                        try {
                            // Camera view start
                            mPreviewRequest = mPreviewRequestBuilder.build();
                            mCaptureSession.setRepeatingRequest(mPreviewRequest, null, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        //when Session configuration failed
                        Log.e(TAG, "error");
                    }
                }, null);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar2);
        mTextureView = (TextureView) findViewById(R.id.textureView);

        // in the onSurfaceTextureAvailable(), openCamera() is called.
        mTextureView.setSurfaceTextureListener(this);


        /*
        //callbackImple = new CameraCallbackImpl();
        //mCamView = (SurfaceView) findViewById(R.id.cam_view);
        SurfaceHolder holder = mCamView.getHolder();
        // holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(callbackImple);

*/
        ImageButton shutterButton = (ImageButton) findViewById(R.id.cameraShutter);
        shutterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    takePicture();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    //could not save image file
                    e.printStackTrace();
                }
            }
        });
/*
        callbackImple.setContentResolver(this.getContentResolver());
*/
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

    private void takePicture() throws CameraAccessException, IOException {
        //stop preview view
        mCaptureSession.stopRepeating();
        File file = null;
        if (mTextureView.isAvailable()) {

            //get image shown on TextureView as Bitmap
           // Bitmap bmp = Bitmap.createBitmap(mTextureView.getWidth(),
             //       mTextureView.getHeight(), Bitmap.Config.ARGB_8888);
            Bitmap bmp = mTextureView.getBitmap();

            //get Image shown on ARView as Bitmap
            Bitmap arBmp = Bitmap.createBitmap(arView.getWidth(),
                    arView.getHeight(), Bitmap.Config.ARGB_8888);
           // Drawable arDrawable = arView.getBackground();

            //combine images
            Bitmap offBitmap = Bitmap.createBitmap(bmp.getWidth(),
                    bmp.getHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvasForARView = new Canvas(arBmp);
           // if(arDrawable != null){
             //   arDrawable.draw(canvasForARView);
            //}
            arView.draw(canvasForARView);

            Canvas canvasForCombine = new Canvas(bmp);

            canvasForCombine.drawBitmap(bmp, null, new Rect(0, 0,
                    bmp.getWidth(), bmp.getHeight()), null);
            canvasForCombine.drawBitmap(arBmp, null, new Rect(0, 0,
                    bmp.getWidth(), bmp.getHeight()), null);

            // storing the picture data on the android device.
            FileOutputStream fos = null;

            Date today = new Date();
            SimpleDateFormat sdFormat = new SimpleDateFormat(
                    "yyyy_MM_dd_hh_mm_ss_SSS", Locale.JAPAN);
            String fileName = sdFormat.format(today) + ".jpg";

            String strFolder = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + "/silbala/";
            File folder = new File(strFolder);
            file = new File(strFolder + fileName);
            try {

                if (!folder.exists()) {
                    folder.mkdir();
                }
                if (file.createNewFile()) {
                    fos = new FileOutputStream(file);
                    //arBmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    Log.i(TAG, "saved successfully: " + strFolder + fileName);
                    fos.close();
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG,
                        "IOException: " + strFolder + fileName + ", "
                                + e.getMessage());
            }
        }
        // restart camera preview
        mCaptureSession.setRepeatingRequest(mPreviewRequest, null, null);

        if (file != null) {
            //If the image file was saved successfully, notify with toast
            Toast.makeText(this, "Saved: " + file, Toast.LENGTH_SHORT).show();


        // In order to notify the existence of this file to Android OS, this procedure is necessary
            Uri uri = Uri.fromFile(file);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, uri.getLastPathSegment());
            values.put(MediaStore.Images.Media.DISPLAY_NAME,
                    uri.getLastPathSegment());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, uri.getPath());
            values.put(MediaStore.Images.Media.DATE_TAKEN,
                    System.currentTimeMillis());

            this.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            //new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
            Log.d(TAG, "hey, camera was not permitted.");
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
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
     *
     * @return
     */
    protected OnCompleteListener getOnCompleteListener() {
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
//        callbackImple.setOverlayView(arView);
        addContentView(arView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

    }

    /**
     * @return
     */
    public ARView getARView() {
        return this.arView;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openCamera();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
