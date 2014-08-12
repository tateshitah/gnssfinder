package org.braincopy.mobile;

import android.test.ActivityInstrumentationTestCase2;

public class TestARView extends ActivityInstrumentationTestCase2<MainActivity> {
	private MainActivity mActivity;

	public TestARView() {
		super(MainActivity.class);
		// TODO Auto-generated constructor stub
	}

	// public TestCase(){}

	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
	}

	public void testARView() {
		ARView ar = new ARView(mActivity);
		ar.width = 1080;
		ar.height = 1557;
		System.out.println("direction: " + ar.direction);
		System.out.println("pitch: " + ar.pitch);
		System.out.println("roll: " + ar.roll);
		float[] orientations = { 0f, 0f, 0f };
		ar.drawScreen(orientations, 45f, 135f);
		float x = ar.convertAzElX(0, 0);
		assertEquals((float) ar.width / 2.0f, x);
		float y = ar.convertAzElY(0, 0);
		assertEquals((float) ar.height / 2.0f, y);
		x = ar.convertAzElX(30, 0);
		y = ar.convertAzElY(30, 0);
		assertEquals(
				(float) ar.width
						* 0.5
						* (1.0 + Math.tan(Math.PI / 6.0)
								/ Math.tan(25.0 / 180.0 * Math.PI)), x, 0.1);
		assertEquals((float) ar.height / 2.0f, y);
		Point vec = ar.convertAzElPoint(0, 30);
		System.out
				.println("[0, 0, 0](0, 30) => (" + vec.x + ", " + vec.y + ")");
		assertEquals(ar.width * 0.5, vec.x, 0.1);
		assertTrue(vec.y < 0);

		vec = ar.convertAzElPoint(30, 30);
		System.out.println("[0, 0, 0](30, 30) => (" + vec.x + ", " + vec.y
				+ ")");
		assertEquals(
				ar.width
						* 0.5
						* (1.0 + Math.tan(Math.PI / 6.0)
								/ Math.tan(20.0 / 180.0 * Math.PI)), vec.x, 100);
		assertTrue(vec.y < 0);
		vec = ar.convertAzElPoint(0, 0);
		System.out.println("[0, 0, 0](0, 0) => (" + vec.x + ", " + vec.y + ")");

		ar.direction = 60;
		ar.updateScreenPlane();
		vec = ar.convertAzElPoint(90, 0);
		System.out.println("[60, 0, 0](90, 0) => (" + vec.x + ", " + vec.y
				+ ")");
		assertEquals(
				ar.width
						* 0.5
						* (1.0 + Math.tan(Math.PI / 6.0)
								/ Math.tan(20.0 / 180.0 * Math.PI)), vec.x, 100);
		assertEquals(ar.height * 0.5f, vec.y, 0.1);

		ar.direction = 0;
		ar.pitch = 30;
		ar.updateScreenPlane();
		vec = ar.convertAzElPoint(0, 30);
		assertEquals(ar.width * 0.5, vec.x, 0.1);
		assertEquals(ar.height * 0.5, vec.y, 0.1);

		vec = ar.convertAzElPoint(30, 30);
		System.out.println("[0, 30, 0](30, 30) => (" + vec.x + ", " + vec.y
				+ ")");
		assertTrue(vec.x < ar.width
				* 0.5
				* (1.0 + Math.tan(Math.PI / 6.0)
						/ Math.tan(20.0 / 180.0 * Math.PI)));
		assertTrue(vec.y < ar.height * 0.5);

		ar.direction = 0;
		ar.pitch = 60;
		ar.updateScreenPlane();
		vec = ar.convertAzElPoint(0, 60);
		assertEquals(ar.width * 0.5, vec.x, 0.1);
		assertEquals(ar.height * 0.5, vec.y, 0.1);
		vec = ar.convertAzElPoint(0, 90);
		assertEquals(ar.width * 0.5, vec.x, 0.1);
		System.out.println("[0, 60, 0](0, 90) => (" + vec.x + ", " + vec.y
				+ ")");
		// assertEquals(0, vec.y, 0.1);

		vec = ar.convertAzElPoint(30, 90);
		System.out.println("[0, 60, 0](30, 90) => (" + vec.x + ", " + vec.y
				+ ")");
		// assertEquals(320, vec.x, 0.1);
		// assertEquals(0, vec.y, 0.1);
		ar.direction = 180;
		ar.pitch = 0;
		ar.updateScreenPlane();
		vec = ar.convertAzElPoint(180, 0);
		System.out.println("[180, 0, 0](180, 0) => (" + vec.x + ", " + vec.y
				+ ")");
		assertEquals(ar.width * 0.5, vec.x, 0.1);
		assertEquals(ar.height * 0.5, vec.y, 0.1);
		ar.pitch = 30;
		ar.updateScreenPlane();
		vec = ar.convertAzElPoint(180, 0);
		System.out.println("[180, 30, 0](180, 0) => (" + vec.x + ", " + vec.y
				+ ")");
		assertEquals(ar.width * 0.5, vec.x, 0.1);
		assertTrue(ar.height * 0.5 < vec.y);

	}
}
