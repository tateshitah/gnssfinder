package org.braincopy.mobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

/**
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class ARView extends View {
	Paint paint;
	float lat, lon;

	/**
	 * [degree]
	 */
	float direction;

	/**
	 * [degree]
	 */
	float pitch;

	/**
	 * [degree]
	 */
	float roll;

	private Satellite[] satellites;

	/**
	 * vertical view angle [degree]
	 */
	final float vVeiwAngle = 60.0f;

	/**
	 * horizontal view angle [degree]
	 */
	final float hVeiwAngle = 50.0f;

	private String statusString = "connecting...";

	public ARView(Context context) {
		super(context);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawText("Direction: " + direction, 50, 50, paint);
		canvas.drawText("Pitch: " + pitch, 50, 100, paint);
		canvas.drawText("Roll: " + roll, 50, 150, paint);
		canvas.drawText("Lat: " + lat, 50, 200, paint);
		canvas.drawText("Lon: " + lon, 50, 250, paint);

		float height, startX, stopX, startY, stopY;

		// draw horizon
		height = (float) (canvas.getHeight() * (0.5 + pitch
				/ (vVeiwAngle * 0.5) * 0.5));
		startX = 0;
		stopX = canvas.getWidth();
		startY = height;
		stopY = height;
		canvas.drawLine(startX, startY, stopX, stopY, paint);

		drawDirection(canvas, paint, height);
		drawSatellites(canvas, paint);
		drawStatus(canvas, paint);

	}

	private void drawStatus(Canvas canvas, Paint paint2) {
		canvas.drawText(this.statusString, 50, canvas.getHeight() - 50, paint);
	}

	private void drawSatellites(Canvas canvas, Paint paint) {
		float dx = 0;
		float dy = 0;
		Matrix matrix = new Matrix();
		float scale = 1.0f;
		matrix.postScale(scale, scale);
		if (this.satellites != null) {
			for (int i = 0; i < satellites.length; i++) {
				if (direction - satellites[i].getAzimuth() < -270) {
					dx = (float) (canvas.getWidth() * (0.5 - (direction
							- satellites[i].getAzimuth() + 360)
							/ (hVeiwAngle * 0.5) * 0.5));
					dy = (float) (canvas.getHeight() * (0.5 + (pitch - satellites[i]
							.getElevation()) / (vVeiwAngle * 0.5) * 0.5));
				} else if (direction - satellites[i].getAzimuth() > 270) {
					dx = (float) (canvas.getWidth() * (0.5 - (direction
							- satellites[i].getAzimuth() - 360)
							/ (hVeiwAngle * 0.5) * 0.5));
					dy = (float) (canvas.getHeight() * (0.5 + (pitch - satellites[i]
							.getElevation()) / (vVeiwAngle * 0.5) * 0.5));
				} else {
					dx = (float) (canvas.getWidth() * (0.5 - (direction - satellites[i]
							.getAzimuth()) / (hVeiwAngle * 0.5) * 0.5));
					dy = (float) (canvas.getHeight() * (0.5 + (pitch - satellites[i]
							.getElevation()) / (vVeiwAngle * 0.5) * 0.5));
				}
				matrix.postTranslate(dx, dy);
				/*
				 * Log.e("test", "i, dir, az, el: " + i + ", " + direction +
				 * ", " + satellites[i].getAzimuth() + ", " +
				 * satellites[i].getElevation());
				 */
				canvas.drawBitmap(satellites[i].getImage(), matrix, paint);
				canvas.drawText(satellites[i].getDescription(), dx + 30, dy,
						paint);
				matrix.postTranslate(-dx, -dy);
			}
		}
	}

	private void drawDirection(Canvas canvas, Paint paint, float height) {
		float startX, stopX, startY, stopY;

		// draw west
		startX = (float) (canvas.getWidth() * (0.5 - (direction - 180)
				/ (hVeiwAngle * 0.5) * 0.5));
		stopX = (float) (canvas.getWidth() * (0.5 - (direction - 180)
				/ (hVeiwAngle * 0.5) * 0.5));
		startY = 100;
		stopY = height;
		canvas.drawLine(startX, startY, stopX, stopY, paint);
		canvas.drawText("W", startX, startY, paint);

		// draw south
		startX = (float) (canvas.getWidth() * (0.5 - (direction - 90)
				/ (hVeiwAngle * 0.5) * 0.5));
		stopX = (float) (canvas.getWidth() * (0.5 - (direction - 90)
				/ (hVeiwAngle * 0.5) * 0.5));
		startY = 100;
		stopY = height;
		canvas.drawLine(startX, startY, stopX, stopY, paint);
		canvas.drawText("S", startX, startY, paint);

		// draw east
		if (direction > 270) {
			startX = (float) (canvas.getWidth() * (0.5 - (direction - 360)
					/ (hVeiwAngle * 0.5) * 0.5));
			stopX = (float) (canvas.getWidth() * (0.5 - (direction - 360)
					/ (hVeiwAngle * 0.5) * 0.5));
		} else if (direction < 90) {
			startX = (float) (canvas.getWidth() * (0.5 - direction
					/ (hVeiwAngle * 0.5) * 0.5));
			stopX = (float) (canvas.getWidth() * (0.5 - direction
					/ (hVeiwAngle * 0.5) * 0.5));
		}
		startY = 100;
		stopY = height;
		canvas.drawLine(startX, startY, stopX, stopY, paint);
		canvas.drawText("E", startX, startY, paint);

		// draw north
		startX = (float) (canvas.getWidth() * (0.5 - (direction - 270)
				/ (hVeiwAngle * 0.5) * 0.5));
		stopX = (float) (canvas.getWidth() * (0.5 - (direction - 270)
				/ (hVeiwAngle * 0.5) * 0.5));
		startY = 100;
		stopY = height;
		canvas.drawLine(startX, startY, stopX, stopY, paint);
		canvas.drawText("N", startX, startY, paint);

	}

	public void drawScreen(float[] orientation, float lat_, float lon_) {
		direction = ((float) Math.toDegrees(orientation[0]) + 360) % 360;
		pitch = (float) Math.toDegrees(orientation[1]);
		roll = (float) Math.toDegrees(orientation[2]);
		lat = lat_;
		lon = lon_;

		invalidate();
	}

	/**
	 * 
	 * @param azimuth
	 *            [degree]
	 * @param elevation
	 *            [degree]
	 * @return
	 */
	protected float convertAzElX(float azimuth, float elevation) {
		float result = (float) (0.5f * hVeiwAngle + 0.5f * hVeiwAngle
				* (azimuth - direction) * Math.cos(roll / 180 * Math.PI) - 0.5f
				* vVeiwAngle * (elevation - pitch)
				* Math.sin(roll / 180 * Math.PI));
		return result;
	}

	/**
	 * 
	 * @param azimuth
	 *            [degree]
	 * @param elevation
	 *            [degree]
	 * @return
	 */
	protected float convertAzElY(float azimuth, float elevation) {
		float result = (float) (0.5f * vVeiwAngle + 0.5f * hVeiwAngle
				* (azimuth - direction) * Math.sin(roll / 180 * Math.PI) + 0.5f
				* vVeiwAngle * (elevation - pitch)
				* Math.cos(roll / 180 * Math.PI));
		return result;
	}

	public Satellite[] getSatellites() {
		return satellites;
	}

	public void setSatellites(Satellite[] satellites) {
		this.satellites = satellites;
	}

	public void setStatus(String string) {
		this.statusString = string;

	}

}
