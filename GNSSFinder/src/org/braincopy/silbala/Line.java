package org.braincopy.silbala;

/**
 * This class expresses a line in the real world coordinate system. This line
 * starts from the original point.
 * 
 * <ol>
 * <li>right-handed coordinate system</li>
 * <li>x axis is south direction,</li>
 * <li>y axis is down direction,</li>
 * <li>z axis is east direction,</li>
 * <li>angle increases for clockwise for all axis.</li>
 * </ol>
 * 
 * directional vector should be (a, b, c)
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class Line {
	// x/a = y/b = z/c
	float a, b, c;

	public Line(float a_, float b_, float c_) {
		this.a = a_;
		this.b = b_;
		this.c = c_;
	}

}
