package org.braincopy.silbala;

/**
 * This class expresses a plane in the real world coordinate system.
 * 
 * <ol>
 * <li>right-handed coordinate system</li>
 * <li>x axis is south direction,</li>
 * <li>y axis is down direction,</li>
 * <li>z axis is east direction,</li>
 * <li>angle increases for clockwise for all axis.</li>
 * </ol>
 * 
 * a normal vector should be (a, b, c)
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class Plane {
	// ax + by + cz = d
	float a, b, c, d;

	public Plane(float a_, float b_, float c_, float d_) {
		this.a = a_;
		this.b = b_;
		this.c = c_;
		this.d = d_;
	}

	public void setParam(float a_, float b_, float c_, float d_) {
		this.a = a_;
		this.b = b_;
		this.c = c_;
		this.d = d_;
	}

	/**
	 * 
	 * @param line
	 * @return intersection point . return null when no intersection.
	 */
	public Point getIntersection(Line line) {
		Point result = null;

		// if inner-product > 0, this plane and the line should cross.
		if (this.a * line.a + this.b * line.b + this.c * line.c > 0.001) {

			float t = 0;

			t = this.d / (this.a * line.a + this.b * line.b + this.c * line.c);
			result = new Point(line.a * t, line.b * t, line.c * t);
		}

		return result;
	}

}
