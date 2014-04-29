package org.braincopy.mobile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * 
 * @author Hiroaki Tateshita
 * 
 */
public class SatelliteInfoWorker implements Runnable {
	float lat, lon;

	public SatelliteInfoWorker() {
		// TODO Auto-generated constructor stub
	}

	public Satellite[] createSatelliteArray(float lat, float lon,
			Date currentDate) {
		System.out.println("here");
		DefaultClientConfig clientConfig = new DefaultClientConfig();
		Client client = Client.create(clientConfig);

		WebResource resource = client
				.resource("http://braincopy.org/gnssws/az_and_el");
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss",
				Locale.ENGLISH);
		params.add("dateTime", sdf.format(currentDate));
		params.add("lat", Float.toString(lat));
		params.add("lon", Float.toString(lon));
		params.add("gnss", "EJ");
		String text = resource.queryParams(params).get(String.class);

		System.out.println(text);
		return null;
	}

	@Override
	public void run() {
		createSatelliteArray(lat, lon, new Date(System.currentTimeMillis()));

	}

	public void setLatLon(float _lat, float _lon) {
		this.lat = _lat;
		this.lon = _lon;

	}

}
