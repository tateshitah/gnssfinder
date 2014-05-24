package org.braincopy.gnssfinder;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class CameraView extends SurfaceView implements Callback {
	private Camera camera;
	private SurfaceHolder surfaceHolder;

	public CameraView(Context context, Camera cam) {
		super(context);

		camera = cam;

		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		// holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public CameraView(Context context) {
		super(context);

		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		// surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int width,
			int height) {
		if (surfaceHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			camera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			camera.setPreviewDisplay(surfaceHolder);
			camera.startPreview();

		} catch (Exception e) {
			Log.d("hiro", "Error starting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = getCameraInstanse();
			camera.setDisplayOrientation(90);
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
			Log.e("hiro", "io exception of camera " + e);
			e.printStackTrace();
		}
	}

	private Camera getCameraInstanse() {
		// TODO Auto-generated method stub
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		stopPreviewAndFreeCamera();
	}

	public void stopPreviewAndFreeCamera() {
		if (camera != null) {
			// Call stopPreview() to stop updating the preview surface.
			camera.setPreviewCallback(null);
			camera.stopPreview();

			// Important: Call release() to release the camera for use by other
			// applications. Applications should release the camera immediately
			// during onPause() and re-open() it during onResume()).
			camera.release();

			camera = null;
		}
	}

}
