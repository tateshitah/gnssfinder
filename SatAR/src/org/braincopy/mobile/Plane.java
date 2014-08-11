package org.braincopy.mobile;

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

	public Point getIntersection(Line line) {
		float t = 0;

		t = this.d / (this.a * line.a + this.b * line.b + this.c * line.c);
		Point result = new Point(line.a * t, line.b * t, line.c * t);

		return result;
	}

}
