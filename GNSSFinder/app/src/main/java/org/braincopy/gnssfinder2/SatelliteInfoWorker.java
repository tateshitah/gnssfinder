package org.braincopy.gnssfinder2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpUriRequest;
//import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * This class will work for you to get any information related satellites from
 * Internet.
 * 
 * @author Hiroaki Tateshita
 * @version 0.8.0
 * 
 */
public class SatelliteInfoWorker extends Thread {
	float lat, lon;
	// private MessageListener messageListener;
	private Date currentDate;
	private Satellite[] satArray;
	private int status = SatelliteInfoWorker.INITIAL_STATUS;
	private String gnssString;
	static final int INITIAL_STATUS = 0;
	static final int CONNECTING = 1;
	static final int CONNECTED = 2;
	static final int COMPLETED = 3;
	static final int IMAGE_LOADED = 4;
	// static final int LOADING_IMAGES = 5;
	static final int INFORMATION_LOADED_WO_LOCATION = 6;

	// static final int LOCATION_UPDATED = 7;
	// static final int INFORMATION_LOADED_W_LOCATION = 8;

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
			result[i].setTouched(false);

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
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("http");
		builder.encodedAuthority("braincopy.org");
		builder.path("/gnssws/az_and_el");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss",
				Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		builder.appendQueryParameter("dateTime", sdf.format(currentDate));
		builder.appendQueryParameter("lat", Float.toString(lat));
		builder.appendQueryParameter("lon", Float.toString(lon));
		builder.appendQueryParameter("gnss", gnssString);

        HttpURLConnection connection = null;
        URL url = null;

//        DefaultHttpClient client = new DefaultHttpClient();
//		HttpUriRequest getRequest = new HttpGet(builder.build().toString());

//		HttpResponse response = null;
		int statusCode;
		try {
            url = new URL(builder.build().toString());
            connection = (HttpURLConnection)url.openConnection();
            connection.connect();

//            response = client.execute(getRequest);
//			statusCode = response.getStatusLine().getStatusCode();
            statusCode = connection.getResponseCode();

			Document doc = null;
			if (statusCode == HttpURLConnection.HTTP_OK){//HttpStatus.SC_OK) {
//				HttpEntity entity = response.getEntity();

				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				try {
					DocumentBuilder docBuilder = factory.newDocumentBuilder();
//					doc = docBuilder.parse(entity.getContent());
                    doc = docBuilder.parse(connection.getInputStream());
					createSatelliteArray(doc);
					// something strange. why image loaded?
					// if (this.getStatus() == SatelliteInfoWorker.IMAGE_LOADED)
					// {
					// setStatus(SatelliteInfoWorker.INFORMATION_LOADED_W_LOCATION);
					// } else {
					setStatus(SatelliteInfoWorker.INFORMATION_LOADED_WO_LOCATION);
					// }
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

		} catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

		}

		// from here, i will try to download satellite database text file
		// temporary.

//		getRequest = new HttpGet(
//				"http://braincopy.org/WebContent/assets/satelliteDataBase.txt");
        final String satelliteDatabaseURLString ="https://braincopy.org/WebContent/assets/satelliteDataBase.txt";
		try {
            url = new URL(satelliteDatabaseURLString);
            connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            //           response = client.execute(getRequest);
//			statusCode = response.getStatusLine().getStatusCode();
            statusCode = connection.getResponseCode();

			if (statusCode == HttpURLConnection.HTTP_OK){//HttpStatus.SC_OK) {

				String strFolder = Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
						+ "/gnssfinder/";
				File folder = new File(strFolder);
				File file = new File(strFolder + "satelliteDataBase.txt");
				if (!folder.exists()) {
					folder.mkdir();
				}
				InputStream is;
				try {
//					is = response.getEntity().getContent();
                    is = connection.getInputStream();
					BufferedInputStream in = new BufferedInputStream(is, 1024);
					BufferedOutputStream out;
					file.createNewFile();
					out = new BufferedOutputStream(new FileOutputStream(file,
							false), 1024);
					byte buf[] = new byte[1024];
					int size = -1;
					while ((size = in.read(buf)) != -1) {
						out.write(buf, 0, size);
					}
					out.flush();
					out.close();
					in.close();
				} catch (IllegalStateException e) {
					e.printStackTrace();
					Log.e("error", "error when trying executing request.: " + e);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Log.e("error", "error when trying executing request.: " + e);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void setLatLon(float _lat, float _lon) {
		this.lat = _lat;
		this.lon = _lon;
	}

	public void setCurrentDate(Date _currentDate) {
		this.currentDate = _currentDate;
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

	public void setGnssString(String gnssString_) {
		this.gnssString = gnssString_;
	}
}
