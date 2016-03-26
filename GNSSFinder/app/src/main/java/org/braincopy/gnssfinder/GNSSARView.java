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
 * @version 0.7.2
 * 
 */
public class GNSSARView extends ARView {

	// private Satellite[] satellites;

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
		float dx = 0;
		float dy = 0;
		Matrix matrix = new Matrix();
		float scale = 1.0f;
		matrix.postScale(scale, scale);
		// if (this.satellites != null) {
		if (this.arObjs != null) {
			// for (int i = 0; i < satellites.length; i++) {
			for (int i = 0; i < arObjs.length; i++) {
				// if (satellites[i] != null) {
				if (arObjs[i] != null) {
					dx = arObjs[i].getImage().getWidth() / 2 * scale;
					dy = arObjs[i].getImage().getHeight() / 2 * scale;
					// point = convertAzElPoint(satellites[i].getAzimuth(),
					// satellites[i].getElevation());
					point = convertAzElPoint(
							((Satellite) arObjs[i]).getAzimuth(),
							((Satellite) arObjs[i]).getElevation());
					((Satellite) arObjs[i]).setPoint(point);
					if (((Satellite) arObjs[i]).getImage() != null
							&& point != null) {
						matrix.postTranslate(point.x - dx, point.y - dy);
						canvas.drawBitmap(((Satellite) arObjs[i]).getImage(),
								matrix, paint);
						canvas.drawText(
								((Satellite) arObjs[i]).getDescription(),
								point.x + 30, point.y, paint);
						matrix.postTranslate(-point.x + dx, -point.y + dy);
					}
				}
			}
		}
	}

	public Satellite[] getSatellites() {
		return (Satellite[]) arObjs;
	}

	public void setSatellites(Satellite[] satellites) {
		// this.satellites = satellites;
		this.arObjs = satellites;
	}

}
