package org.braincopy.silbala;

import android.graphics.Bitmap;

/**
 * class for AR Object
 * 
 * @author Hiroaki Tateshita
 * @version 0.4.7
 * 
 */
public class ARObject {
	private Bitmap image;

	private Point point;

	private boolean touched;

	/**
	 * 
	 * @return center point
	 */
	public Point getPoint() {
		return point;
	}

	/**
	 * need to set center point of the AR Object.
	 * 
	 * @param point
	 */
	public void setPoint(Point point) {
		this.point = point;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public boolean isTouched() {
		return touched;
	}

	public void setTouched(boolean touched) {
		this.touched = touched;
	}
}
