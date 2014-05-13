package org.braincopy.mobile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.util.Log;

/**
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class SatelliteInfoWorker extends Thread {
	float lat, lon;
	// private MessageListener messageListener;
	private Date currentDate;
	private Satellite[] satArray;
	private int status = SatelliteInfoWorker.INITIAL_STATUS;
	static final int INITIAL_STATUS = 0;
	static final int CONNECTING = 1;
	static final int CONNECTED = 2;
	static final int COMPLETED = 3;
	static final int IMAGE_LOADED = 4;
	static final int LOADING_IMAGES = 5;
	static final int INFORMATION_LOADED_WO_LOCATION = 6;
	static final int LOCATION_UPDATED = 7;

	public SatelliteInfoWorker() {
	}

	public void createSatelliteArray(Document doc) {
		NodeList satObsList = doc.getElementsByTagName("SatObservation");
		NodeList satObs = null;
		NodeList obType1Nodelist = null;
		Satellite[] result = new Satellite[satObsList.getLength()];
		for (int i = 0; i < satObsList.getLength(); i++) {
			satObs = satObsList.item(i).getChildNodes();
			result[i] = new Satellite();

			for (int k = 0; k < satObs.getLength(); k++) {
				if (satObs.item(k).getNodeName().equals("ObType")) {
					obType1Nodelist = satObs.item(k).getFirstChild()
							.getChildNodes();
					for (int j = 0; j < obType1Nodelist.getLength(); j++) {
						if (obType1Nodelist.item(j).getNodeName()
								.equals("Azimuth")) {
							result[i].setAzimuth((float) (((Float
									.parseFloat(obType1Nodelist.item(j)
											.getFirstChild().getNodeValue()))
									* -1 + 360.0) % 360.0));
						} else if (obType1Nodelist.item(j).getNodeName()
								.equals("Elevation")) {
							result[i].setElevation(Float
									.parseFloat(obType1Nodelist.item(j)
											.getFirstChild().getNodeValue()));
						}
					}
				} else if (satObs.item(k).getNodeName()
						.equals("SatelliteNumber")) {
					result[i].setCatNo(satObs.item(k).getFirstChild()
							.getNodeValue());
				}
			}
		}
		satArray = result;
	}

	@Override
	public void run() {
		DefaultHttpClient client = new DefaultHttpClient();
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("http");
		builder.encodedAuthority("braincopy.org");
		builder.path("/gnssws/az_and_el");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss",
				Locale.ENGLISH);
		builder.appendQueryParameter("dateTime", sdf.format(currentDate));
		builder.appendQueryParameter("lat", Float.toString(lat));
		builder.appendQueryParameter("lon", Float.toString(lon));
		builder.appendQueryParameter("gnss", "EJ");

		HttpUriRequest getRequest = new HttpGet(builder.build().toString());

		HttpResponse response = null;
		try {
			response = client.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			// Log.i("hiro", "statusCode=" + statusCode);

			Document doc = null;
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();

				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				try {
					DocumentBuilder docBuilder = factory.newDocumentBuilder();
					doc = docBuilder.parse(entity.getContent());
					createSatelliteArray(doc);
					if (this.getStatus() == SatelliteInfoWorker.IMAGE_LOADED) {
						setStatus(SatelliteInfoWorker.COMPLETED);
					} else {
						setStatus(SatelliteInfoWorker.INFORMATION_LOADED_WO_LOCATION);
					}
				} catch (ParserConfigurationException e) {
					Log.e("hiro", "context might be not expecting xml. " + e);
					e.printStackTrace();
				} catch (IllegalStateException e) {
					Log.e("hiro", "context might be not expecting xml. " + e);
					e.printStackTrace();
				} catch (SAXException e) {
					Log.e("hiro", "context might be not expecting xml. " + e);
					e.printStackTrace();
				} catch (IOException e) {
					Log.e("hiro", "context might be not expecting xml. " + e);
					e.printStackTrace();
				}
			}

		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
			Log.e("error", "error when trying executing request.: " + e1);
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.e("error", "error when trying executing request.: " + e1);
		} finally {
		}
	}

	public void setLatLon(float _lat, float _lon) {
		this.lat = _lat;
		this.lon = _lon;
	}

	public void setCurrentDate(Date _currentDate) {
		this.currentDate = _currentDate;
	}

	public void setMessageListener(MessageListener _messageListener) {
		// this.messageListener = _messageListener;
	}

	public Satellite[] getSatArray() {
		return this.satArray;
	}

	public int getStatus() {
		return this.status;
	}

	public void setStatus(int _status) {
		this.status = _status;
	}
}
