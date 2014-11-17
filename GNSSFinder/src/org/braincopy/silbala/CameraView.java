package org.braincopy.silbala;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * @deprecated
 * @author Hiroaki Tateshita
 * @version 0.2.0
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
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if (camera != null) {
			camera.stopPreview();
			try {
				camera.setPreviewDisplay(arg0);
			} catch (IOException e) {
				Log.d("silbala",
						"Error starting camera preview: " + e.getMessage());
				e.printStackTrace();
			}
			camera.startPreview();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = Camera.open();
			if (camera != null) {
				camera.setPreviewDisplay(holder);
				camera.setDisplayOrientation(90);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		stopPreviewAndFreeCamera();
	}

	public void stopPreviewAndFreeCamera() {
		if (camera != null) {
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}
}
