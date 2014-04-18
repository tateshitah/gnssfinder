package org.braincopy.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.core.MultivaluedMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SampleClient {

	public static void main(String[] args) {
		SampleClient client = new SampleClient();
		client.createSatelliteArray(30, 130,
				new Date(System.currentTimeMillis()));
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
		Document document = resource.queryParams(params).get(Document.class);
		String text = resource.queryParams(params).get(String.class);
		System.out.println(text);
		NodeList nodes = document.getElementsByTagName("SatObservation");
		Satellite[] result = new Satellite[nodes.getLength()];
		for (int i = 0; i < nodes.getLength(); i++) {
			// System.out.println(nodes.item(i).getNodeName());
			result[i] = createSatFromNode(nodes.item(i));
		}
		return null;
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	private Satellite createSatFromNode(Node satNode) {
		NodeList list = satNode.getChildNodes();
		Satellite result = new Satellite();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals("ObType")) {
				NodeList obsType1List = list.item(i).getFirstChild()
						.getChildNodes();
				for (int j = 0; j < obsType1List.getLength(); j++) {
					if (obsType1List.item(j).getNodeName().equals("Elevation")) {
						result.setElevation(Float.parseFloat(obsType1List
								.item(j).getFirstChild().getNodeValue()));
					} else if (obsType1List.item(j).getNodeName()
							.equals("Azimuth")) {
						result.setAzimuth(Float.parseFloat(obsType1List.item(j)
								.getFirstChild().getNodeValue()));
					}
				}
			}
			System.out.println(" " + list.item(i).getNodeName()
					+ result.getAzimuth() + result.getElevation());
		}
		return result;
	}

}
