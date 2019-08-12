package org.braincopy.gnssfinder2;

import org.braincopy.silbala.ARObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * This class deals with image, azimuth, and elevation to display on android
 * application.
 * 
 * @author Hiroaki Tateshita
 * @version 0.7.2
 * 
 */
public class Satellite extends ARObject {

	/**
	 * 0-360 [degree] east 0, south 90, west 180, north 270
	 */
	private float azimuth;

	/**
	 * 0 - 90 [degree]
	 */
	private float elevation;

	private String catNo;
	private String description;

	/**
	 * such as "gzss", "galileo", "galileofoc", "gpsBlockIIF"
	 */
	private String gnssStr;

	Satellite(Context context) {
		/*
		 * default
		 */
		Resources res = context.getResources();
		setImage(BitmapFactory.decodeResource(res, R.drawable.qzs_1));
		setAzimuth(270);
		setElevation(60);
	}

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

	public static Satellite[] createSatellite(Context context) {
		Satellite[] result = new Satellite[1];

		result[0] = new Satellite(context);
		return result;
	}

	public String getCatNo() {
		return catNo;
	}

	public void setCatNo(String catNo) {
		this.catNo = catNo;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return this.catNo;
	}

	public String getGnssStr() {
		return gnssStr;
	}

	public void setGnssStr(String gnssStr) {
		this.gnssStr = gnssStr;
	}

	/**
	 * get GNSS images from drawable images by using string described
	 * in the satellite info database.
	 * So if the image files of the GNSS satellites,
	 * this method should be modified.
	 * @param gnssStr
	 * @param resources
	 * @return Bitmap object
	 */
	public static Bitmap getGNSSImage(String gnssStr, Resources resources) {
		Bitmap result = null;
		if (gnssStr.equals("galileo")) {
			result = BitmapFactory
					.decodeResource(resources, R.drawable.galileo);
		} else if (gnssStr.equals("galileofoc")) {
			result = BitmapFactory.decodeResource(resources,
					R.drawable.galileofoc);
		} else if (gnssStr.equals("gpsBlockIIF")) {
			result = BitmapFactory.decodeResource(resources, R.drawable.iif);
		} else if (gnssStr.equals("qzs-1")) {
			result = BitmapFactory.decodeResource(resources, R.drawable.qzs_1);
		} else if (gnssStr.equals("qzs-2_4")) {
			result = BitmapFactory.decodeResource(resources, R.drawable.qzs_2_4);
		} else if (gnssStr.equals("qzs-3")) {
			result = BitmapFactory.decodeResource(resources, R.drawable.qzs_3);
		}else if (gnssStr.equals("gpsBlockIIIA")) {
			result = BitmapFactory.decodeResource(resources, R.drawable.gps_iii_a);
		}
		return result;
	}
}
