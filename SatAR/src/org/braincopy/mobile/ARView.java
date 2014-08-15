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
	private static final float DISTANCE = 1350f;
	Paint paint;
	float lat, lon;
	int width = 0, height = 0;

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
	final float vVeiwAngle = 30.0f;

	/**
	 * horizontal view angle [degree]
	 */
	final float hVeiwAngle = 25.0f;

	private String statusString = "connecting...";

	private Plane screenPlane;
	private Line line;
	private Point point;

	public ARView(Context context) {
		super(context);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
		screenPlane = new Plane(0, 0, 0, 0);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		width = canvas.getWidth();
		height = canvas.getHeight();

		canvas.drawText("Direction: " + direction, 50, 50, paint);
		canvas.drawText("Pitch: " + pitch, 50, 100, paint);
		canvas.drawText("Roll: " + roll, 50, 150, paint);
		canvas.drawText("Lat: " + lat, 50, 200, paint);
		canvas.drawText("Lon: " + lon, 50, 250, paint);

		// draw horizon
		// drawHorizon(canvas, paint);
		drawAzElLines(canvas, paint, 8);

		drawDirection(canvas, paint);
		drawSatellites(canvas, paint);
		drawStatus(canvas, paint);

	}

	private void drawStatus(Canvas canvas, Paint paint2) {
		canvas.drawText(this.statusString, 50, canvas.getHeight() - 50, paint);
	}

	public void drawTest(Canvas canvas, Paint paint, float az, float el) {
		Point point = convertAzElPoint(az, el);
		if (point != null) {
			canvas.drawText("(" + az + "," + el + ")", point.x, point.y, paint);
		}
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

	/**
	 * 
	 * @param canvas
	 * @param paint
	 */
	private void drawHorizon(Canvas canvas, Paint paint) {
		Point startPoint, stopPoint;
		for (int i = 0; i < 8; i++) {
			startPoint = convertAzElPoint(i * 45, 0);
			stopPoint = convertAzElPoint((i + 1) * 45, 0);
			if (startPoint != null && stopPoint != null) {
				canvas.drawLine(startPoint.x, startPoint.y, stopPoint.x,
						stopPoint.y, paint);
			}
		}
	}

	private void drawAzElLines(Canvas canvas, Paint paint, int numOfLines) {

		// create visible points array
		Point[] points = new Point[(numOfLines + 1) * (numOfLines + 1) * 4];
		for (int i = 0; i < numOfLines + 1; i++) {
			for (int j = 0; j < (numOfLines + 1) * 4; j++) {
				points[(numOfLines + 1) * 4 * i + j] = convertAzElPoint(j * 90
						/ (numOfLines + 1), i * 90 / (numOfLines + 1));
			}
		}

		// draw horizontal lines
		for (int i = 0; i < numOfLines + 1; i++) {
			for (int j = 1; j < (numOfLines + 1) * 4; j++) {
				if (points[(numOfLines + 1) * 4 * i + j - 1] != null
						&& points[(numOfLines + 1) * 4 * i + j] != null) {
					canvas.drawLine(points[(numOfLines + 1) * 4 * i + j - 1].x,
							points[(numOfLines + 1) * 4 * i + j - 1].y,
							points[(numOfLines + 1) * 4 * i + j].x,
							points[(numOfLines + 1) * 4 * i + j].y, paint);
				}
			}
			if (points[(numOfLines + 1) * 4 * i] != null
					&& points[(numOfLines + 1) * 4 * (i + 1) - 1] != null) {
				canvas.drawLine(points[(numOfLines + 1) * 4 * i].x,
						points[(numOfLines + 1) * 4 * i].y,
						points[(numOfLines + 1) * 4 * (i + 1) - 1].x,
						points[(numOfLines + 1) * 4 * (i + 1) - 1].y, paint);
			}
		}

		for (int j = 0; j < (numOfLines + 1) * 4; j++) {
			for (int i = 0; i < numOfLines; i++) {
				if (points[(numOfLines + 1) * 4 * i + j] != null
						&& points[(numOfLines + 1) * 4 * (i + 1) + j] != null) {
					canvas.drawLine(points[(numOfLines + 1) * 4 * i + j].x,
							points[(numOfLines + 1) * 4 * i + j].y,
							points[(numOfLines + 1) * 4 * (i + 1) + j].x,
							points[(numOfLines + 1) * 4 * (i + 1) + j].y, paint);

				}
			}
		}
	}

	private void drawDirection(Canvas canvas, Paint paint) {
		Point startPoint, stopPoint;

		// draw west
		startPoint = convertAzElPoint(180, 60);
		stopPoint = convertAzElPoint(180, 90);
		if (startPoint != null && stopPoint != null) {
			canvas.drawLine(startPoint.x, startPoint.y, stopPoint.x,
					stopPoint.y, paint);
			canvas.drawText("WEST", startPoint.x, startPoint.y, paint);
		}

		// draw south
		startPoint = convertAzElPoint(90, 60);
		stopPoint = convertAzElPoint(90, 90);
		if (startPoint != null && stopPoint != null) {
			canvas.drawLine(startPoint.x, startPoint.y, stopPoint.x,
					stopPoint.y, paint);
			canvas.drawText("SOUTH", startPoint.x, startPoint.y, paint);
		}
		// draw east
		startPoint = convertAzElPoint(0, 60);
		stopPoint = convertAzElPoint(0, 90);
		if (startPoint != null && stopPoint != null) {
			canvas.drawLine(startPoint.x, startPoint.y, stopPoint.x,
					stopPoint.y, paint);
			canvas.drawText("EAST", startPoint.x, startPoint.y, paint);
		}
		// draw north
		startPoint = convertAzElPoint(270, 60);
		stopPoint = convertAzElPoint(270, 90);
		if (startPoint != null && stopPoint != null) {
			canvas.drawLine(startPoint.x, startPoint.y, stopPoint.x,
					stopPoint.y, paint);
			canvas.drawText("NORTH", startPoint.x, startPoint.y, paint);
		}
	}

	public void drawScreen(float[] orientation, float lat_, float lon_) {
		direction = ((float) Math.toDegrees(orientation[0]) + 360) % 360;
		pitch = (float) Math.toDegrees(orientation[1]);
		roll = (float) Math.toDegrees(orientation[2]);
		lat = lat_;
		lon = lon_;

		screenPlane.setParam(
				(float) (Math.cos(orientation[1]) * Math.sin(orientation[0])),
				-(float) Math.sin(orientation[1]),
				(float) (Math.cos(orientation[1]) * Math.cos(orientation[0])),
				DISTANCE);

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
		double dx = 0, dy = 0;
		float result = 0;
		dx = 0.5f * width / Math.tan(hVeiwAngle / 180 * Math.PI)
				* Math.tan(plusMinusPI(azimuth - direction));
		dy = -0.5f * height / Math.tan(vVeiwAngle / 180 * Math.PI)
				* Math.tan(plusMinusPI(elevation - pitch))
				/ Math.cos(plusMinusPI(azimuth - direction));
		/*
		 * rotate by using roll.
		 */
		result = (float) (0.5f * width + dx * Math.cos(roll / 180 * Math.PI) + dy
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
		double dx = 0, dy = 0;
		float result = 0;
		dx = 0.5f * width / Math.tan(hVeiwAngle / 180 * Math.PI)
				* Math.tan(plusMinusPI(azimuth - direction));
		dy = -0.5f * height / Math.tan(vVeiwAngle / 180 * Math.PI)
				* Math.tan(plusMinusPI(elevation - pitch))
				/ Math.cos(plusMinusPI(azimuth - direction));
		/*
		 * rotate by using roll.
		 */
		result = (float) (0.5f * height - dx * Math.sin(roll / 180 * Math.PI) + dy
				* Math.cos(roll / 180 * Math.PI));

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
	protected Point convertAzElPoint(float azimuth, float elevation) {
		Point result = null;
		float ce = (float) Math.cos(elevation / 180 * Math.PI);
		float se = (float) Math.sin(elevation / 180 * Math.PI);
		float ca = (float) Math.cos(azimuth / 180 * Math.PI);
		float sa = (float) Math.sin(azimuth / 180 * Math.PI);
		line = new Line(ce * sa, -se, ce * ca);
		point = this.screenPlane.getIntersection(line);
		if (point != null) {
			// the order of rotations is important.
			point.rotateY(direction);
			point.rotateX(pitch);
			point.rotateZ(roll);
			result = new Point(0.5f * width + point.x, 0.5f * height + point.y,
					0);
		}
		return result;
	}

	/**
	 * 
	 * @param in
	 *            [degree]
	 * @return
	 */
	private float plusMinusPI(float in) {
		if (in < -180) {
			in += 360;
		} else if (in > 180) {
			in -= 360;
		}
		return (float) (in * Math.PI / 180);
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

	public void updateScreenPlane() {
		screenPlane.setParam((float) (Math.cos(Math.toRadians(pitch)) * Math
				.sin(Math.toRadians(direction))), -(float) Math.sin(Math
				.toRadians(pitch)),
				(float) (Math.cos(Math.toRadians(pitch)) * Math.cos(Math
						.toRadians(direction))), DISTANCE);
	}

}
