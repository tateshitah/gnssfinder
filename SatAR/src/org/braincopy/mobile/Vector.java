package org.braincopy.mobile;

public class Vector {
	private float dx;
	private float dy;

	public Vector(float x, float y) {
		this.dx = x;
		this.dy = y;
	}

	/**
	 * @return the dx
	 */
	public float getDx() {
		return dx;
	}

	/**
	 * @param dx
	 *            the dx to set
	 */
	public void setDx(float dx) {
		this.dx = dx;
	}

	/**
	 * @return the dy
	 */
	public float getDy() {
		return dy;
	}

	/**
	 * @param dy
	 *            the dy to set
	 */
	public void setDy(float dy) {
		this.dy = dy;
	}
}
