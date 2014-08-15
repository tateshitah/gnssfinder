package org.braincopy.silbala;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * 
 * @author Hiroaki Tateshita
 * @version 0.0.6
 * 
 */
public class ARView extends View {
	/**
	 * <P>
	 * DISTANCE expresses the virtual distance between eye and screen plane.
	 * this value should be calculated by using view angle of the camera of
	 * android device. The unit is "pixel", because the unit of screen plane is
	 * pixel on the actual screen of the device. If think about horizontal
	 * direction,
	 * </P>
	 * 
	 * DISTANCE * Math.tan(0.5 * view angle) = 0.5 * width of canvas
	 * 
	 * <P>
	 * It should depends on each device. The easier method for this calibration
	 * should be prepared later.
	 * </P>
	 * 
	 */
	private static final float DISTANCE = 1350f;

	protected float lat, lon;

	protected Paint paint;

	/**
	 * canvas size
	 */
	int width = 0, height = 0;

	/**
	 * [degree]
	 */
	protected float direction;

	/**
	 * [degree]
	 */
	protected float pitch;

	/**
	 * [degree]
	 */
	protected float roll;

	private String statusString = "connecting...";

	/**
	 * 
	 */
	private Plane screenPlane;

	/**
	 * temporary line object
	 */
	protected Line line;

	/**
	 * temporary Point object
	 */
	protected Point point;

	public ARView(Context context) {
		super(context);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
		screenPlane = new Plane(0, 0, 0, 0);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawText("Direction: " + direction, 50, 50, paint);
		canvas.drawText("Pitch: " + pitch, 50, 100, paint);
		canvas.drawText("Roll: " + roll, 50, 150, paint);
		canvas.drawText("Lat: " + lat, 50, 200, paint);
		canvas.drawText("Lon: " + lon, 50, 250, paint);

		drawDirection(canvas, paint);
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

	public void drawAzElLines(Canvas canvas, Paint paint, int numOfLines) {

		// create visible points array
		Point[] points = new Point[(numOfLines + 1) * (numOfLines + 1) * 4];
		Point top = convertAzElPoint(0, 90);
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

		// draw vertical lines
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
			if (points[(numOfLines + 1) * 4 * numOfLines + j] != null
					&& top != null) {
				canvas.drawLine(
						points[(numOfLines + 1) * 4 * numOfLines + j].x,
						points[(numOfLines + 1) * 4 * numOfLines + j].y, top.x,
						top.y, paint);
			}

		}
	}

	private void drawDirection(Canvas canvas, Paint paint) {
		Point textPoint;

		// draw west
		textPoint = convertAzElPoint(180, 0);
		if (textPoint != null) {
			canvas.drawText("WEST", textPoint.x, textPoint.y, paint);
		}

		// draw south
		textPoint = convertAzElPoint(90, 0);
		if (textPoint != null) {
			canvas.drawText("SOUTH", textPoint.x, textPoint.y, paint);
		}

		// draw east
		textPoint = convertAzElPoint(0, 0);
		if (textPoint != null) {
			canvas.drawText("EAST", textPoint.x, textPoint.y, paint);
		}

		// draw north
		textPoint = convertAzElPoint(270, 0);
		if (textPoint != null) {
			canvas.drawText("NORTH", textPoint.x, textPoint.y, paint);
		}
	}

	public void drawScreen(float[] orientation, float lat_, float lon_) {
		direction = ((float) Math.toDegrees(orientation[0]) + 360) % 360;
		pitch = (float) Math.toDegrees(orientation[1]);
		roll = (float) Math.toDegrees(orientation[2]);
		this.lat = lat_;
		this.lon = lon_;

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
	protected Point convertAzElPoint(float azimuth, float elevation) {
		Point result = null;

		/*
		 * create Line object which starts from the original point (0 ,0 ,0) and
		 * whose directional vector is by input azimuth and elevation.
		 */
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
