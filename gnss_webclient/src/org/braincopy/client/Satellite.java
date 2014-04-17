package org.braincopy.client;



/**
 * This class deals with image, azimuth, and elevation to display on android
 * application.
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class Satellite {

	/**
	 * 0-360 [degree] east 0, south 90, west 180, north 270
	 */
	private float azimuth;

	/**
	 * 0 - 90 [degree]
	 */
	private float elevation;


	Satellite() {

	}



	public float getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}

	public float getElevation() {
		return elevation;
	}

	public void setElevation(float elevation) {
		this.elevation = elevation;
	}



}
