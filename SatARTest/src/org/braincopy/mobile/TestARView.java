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
		Vector vec = ar.convertAzElVector(0, 30);
		System.out.println("[0, 0, 0](0, 30) => (" + vec.getDx() + ", "
				+ vec.getDy() + ")");
		assertEquals(ar.width * 0.5, vec.getDx(), 0.1);
		assertTrue(vec.getDy() < 0);

		vec = ar.convertAzElVector(30, 30);
		System.out.println("[0, 0, 0](30, 30) => (" + vec.getDx() + ", "
				+ vec.getDy() + ")");
		assertEquals(
				ar.width
						* 0.5
						* (1.0 + Math.tan(Math.PI / 6.0)
								/ Math.tan(20.0 / 180.0 * Math.PI)),
				vec.getDx(), 100);
		assertTrue(vec.getDy() < 0);
		ar.direction = 60;
		vec = ar.convertAzElVector(90, 0);
		System.out.println("[60, 0, 0](90, 0) => (" + vec.getDx() + ", "
				+ vec.getDy() + ")");
		assertEquals(
				ar.width
						* 0.5
						* (1.0 - Math.tan(Math.PI / 6.0)
								/ Math.tan(20.0 / 180.0 * Math.PI)),
				vec.getDx(), 100);
		assertEquals(ar.height * 0.5f, vec.getDy(), 0.1);

		ar.direction = 0;
		ar.pitch = 30;
		vec = ar.convertAzElVector(0, 30);
		assertEquals(320, vec.getDx(), 0.1);
		assertEquals(640, vec.getDy(), 0.1);

		vec = ar.convertAzElVector(30, 30);
		System.out.println("[0, 30, 0](30, 30) => (" + vec.getDx() + ", "
				+ vec.getDy() + ")");
		assertTrue(vec.getDx() < 716.2);
		// assertTrue(vec.getDy() > 640);

		ar.direction = 0;
		ar.pitch = 60;
		vec = ar.convertAzElVector(0, 60);
		assertEquals(320, vec.getDx(), 0.1);
		assertEquals(640, vec.getDy(), 0.1);
		vec = ar.convertAzElVector(0, 90);
		assertEquals(320, vec.getDx(), 0.1);
		assertEquals(0, vec.getDy(), 0.1);

		vec = ar.convertAzElVector(30, 90);
		// assertEquals(320, vec.getDx(), 0.1);
		// assertEquals(0, vec.getDy(), 0.1);

	}
}
