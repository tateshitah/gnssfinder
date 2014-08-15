package org.braincopy.gnssfinder;

import org.braincopy.silbala.ARView;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class GNSSARView extends ARView {

	private Satellite[] satellites;

	/**
	 * vertical view angle [degree]
	 */
	final float vVeiwAngle = 60.0f;

	/**
	 * horizontal view angle [degree]
	 */
	final float hVeiwAngle = 50.0f;

	public GNSSARView(Context context) {
		super(context);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
	}

	public GNSSARView(Fragment fragment) {
		super(fragment.getActivity());
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setTextSize(20);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawAzElLines(canvas, paint, 8);
		drawSatellites(canvas, paint);
	}

	/**
	 * 
	 * @param canvas
	 * @param paint
	 */
	private void drawSatellites(Canvas canvas, Paint paint) {
		// float dx = 0;
		// float dy = 0;
		Matrix matrix = new Matrix();
		float scale = 1.0f;
		matrix.postScale(scale, scale);
		if (this.satellites != null) {
			for (int i = 0; i < satellites.length; i++) {
				point = convertAzElPoint(satellites[i].getAzimuth(),
						satellites[i].getElevation());
				if (satellites[i].getImage() != null && point != null) {
					matrix.postTranslate(point.x, point.y);
					canvas.drawBitmap(satellites[i].getImage(), matrix, paint);
					canvas.drawText(satellites[i].getDescription(),
							point.x + 30, point.y, paint);
					matrix.postTranslate(-point.x, -point.y);

				}
			}
		}
	}

	public Satellite[] getSatellites() {
		return satellites;
	}

	public void setSatellites(Satellite[] satellites) {
		this.satellites = satellites;
	}

}
