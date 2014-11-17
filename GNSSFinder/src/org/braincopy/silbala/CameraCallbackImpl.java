package org.braincopy.silbala;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * This class is callback implementations for camera. Please use SurfaceView
 * class for camera and add this callback implementation object to Surface
 * Holder of the view class.
 * 
 * @author Hiroaki Tateshita
 * @version 0.2.1
 * 
 */
public class CameraCallbackImpl implements SurfaceHolder.Callback,
		ShutterCallback, PictureCallback {
	private static final String TAG = "Silbala";

	private Camera camera;
	private List<Size> supportedPreviewSize;
	private List<Size> supportedPictureSize;
	private Size optimalPreviewSize;
	private Size optimalPictureSize;

	private ARView overlayARView;

	private ContentResolver contentResolver;

	/**
	 * when calculate optimal size of taking picture, this value will be used as
	 * maximum wide length of the picture. If you caught OUT OF MEMORY error in
	 * your device, you should adjust this value.
	 */
	static final private int MAX_WIDTH_SIZE = 2000;

	public CameraCallbackImpl() {
		// In order to get optimal sizes related to camera,
		// camera object will be opened.
		try {
			camera = Camera.open();
			camera.setDisplayOrientation(90);
		} catch (Exception e) {
			Log.d(TAG, "Error: failed to open Camera > " + e.getMessage());
		}
		Camera.Parameters params = camera.getParameters();
		supportedPreviewSize = params.getSupportedPreviewSizes();
		supportedPictureSize = params.getSupportedPictureSizes();
		optimalPreviewSize = getOptimalPreviewSize(supportedPreviewSize);
		optimalPictureSize = getOptimalPictureSize(supportedPictureSize);

		// then released.
		camera.release();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera = Camera.open();
			if (camera != null) {
				camera.setDisplayOrientation(90);
				camera.setPreviewDisplay(holder);
				camera.startPreview();
			}
		} catch (IOException e) {
			Log.d(TAG, "Error: setting Camera preview > " + e.getMessage());
		}

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		if (holder.getSurface() == null) {
			return;
		}

		try {
			camera.stopPreview();
		} catch (Exception e) {
			Log.d(TAG, "Error: failed to stop preview > " + e.getMessage());
		}

		try {
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(optimalPreviewSize.width,
					optimalPreviewSize.height);
			parameters.setPictureSize(optimalPictureSize.width,
					optimalPictureSize.height);

			camera.setParameters(parameters);
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (Exception e) {
			Log.i(TAG,
					"Error: failed to start camera preview > " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
	}

	/**
	 * width should be bigger than height
	 * 
	 * @param sizes
	 * @return
	 */
	private Size getOptimalPreviewSize(List<Size> sizes) {
		Size optimalSize = null;
		int diff = MAX_WIDTH_SIZE;
		for (Size size : sizes) {
			if (size.width < MAX_WIDTH_SIZE) {
				if (diff > MAX_WIDTH_SIZE - size.width) {
					diff = MAX_WIDTH_SIZE - size.width;
					optimalSize = size;
				}
			}
		}
		return optimalSize;

	}

	/**
	 * width should be bigger than height
	 * 
	 * @param sizes
	 * @return
	 */
	private Size getOptimalPictureSize(List<Size> sizes) {
		Size optimalSize = null;
		int diff = MAX_WIDTH_SIZE;
		for (Size size : sizes) {
			if (size.width < MAX_WIDTH_SIZE) {
				if (diff > MAX_WIDTH_SIZE - size.width) {
					diff = MAX_WIDTH_SIZE - size.width;
					optimalSize = size;
				}
			}
		}
		return optimalSize;
	}

	public void takePicture() {
		camera.takePicture(this, null, this);

	}

	@Override
	public void onShutter() {

	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		if (data != null) {

			Bitmap cameraMap = BitmapFactory.decodeByteArray(data, 0,
					data.length, null);

			/*
			 * rotate the picture acquired from camera.
			 */
			Matrix matrix = new Matrix();
			matrix.setRotate(90);
			cameraMap = Bitmap.createBitmap(cameraMap, 0, 0,
					cameraMap.getWidth(), cameraMap.getHeight(), matrix, true);

			/*
			 * capturing the picture of ARView.
			 */
			overlayARView.destroyDrawingCache();
			Bitmap overlayMap = overlayARView.getDrawingCache();
			Bitmap offBitmap = Bitmap.createBitmap(cameraMap.getWidth(),
					cameraMap.getHeight(), Bitmap.Config.ARGB_8888);
			Log.i(TAG,
					"data.length: " + data.length + ", cameraMap h:"
							+ cameraMap.getHeight() + ", w:"
							+ cameraMap.getWidth() + ", overLayMap h:"
							+ overlayMap.getHeight() + ", w:"
							+ overlayMap.getWidth() + ", offBitmap h:"
							+ offBitmap.getHeight() + ", w:"
							+ offBitmap.getWidth());

			/*
			 * Combining the picture of camera and the captured ARView picture
			 */
			Canvas canvasForCombine = new Canvas(offBitmap);
			canvasForCombine.drawBitmap(cameraMap, null, new Rect(0, 0,
					cameraMap.getWidth(), cameraMap.getHeight()), null);
			canvasForCombine.drawBitmap(overlayMap, null, new Rect(0, 0,
					cameraMap.getWidth(), cameraMap.getHeight()), null);

			// storing the picture data on the android device.
			FileOutputStream fos = null;

			Date today = new Date();
			SimpleDateFormat sdFormat = new SimpleDateFormat(
					"yyyy_MM_dd_hh_mm_ss_SSS", Locale.JAPAN);
			String fileName = sdFormat.format(today) + ".jpg";

			String strFolder = Environment.getExternalStorageDirectory()
					+ "/DCIM/Camera/silbala/";
			File file = new File(strFolder + fileName);
			try {
				if (file.createNewFile()) {
					fos = new FileOutputStream(file);
					offBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
					fos.close();
				}
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}

			Uri uri = Uri.fromFile(file);
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.TITLE, uri.getLastPathSegment());
			values.put(MediaStore.Images.Media.DISPLAY_NAME,
					uri.getLastPathSegment());
			values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
			values.put(MediaStore.Images.Media.DATA, uri.getPath());
			values.put(MediaStore.Images.Media.DATE_TAKEN,
					System.currentTimeMillis());

			this.contentResolver.insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

			// then restart camera preview
			camera.startPreview();

		}

	}

	public void setOverlayView(ARView arView) {
		this.overlayARView = arView;

	}

	public void setContentResolver(ContentResolver contentResolver_) {
		this.contentResolver = contentResolver_;

	}
}
