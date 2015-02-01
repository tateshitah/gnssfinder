package org.braincopy.silbala;

import android.graphics.Bitmap;

/**
 * class for AR Object
 * 
 * @author Hiroaki Tateshita
 * @version 0.4.0
 * 
 */
public class ARObject {
	private Bitmap image;

	private Point point;

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}
}
