package org.braincopy.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SampleClient {

	public static void main(String[] args) {
		SampleClient client = new SampleClient();
		client.createSatelliteArray(30, 130, new Date(System.currentTimeMillis()));
	}
	public SampleClient() {
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

}
