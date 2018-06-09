package org.braincopy.gnssfinder2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * 
 * @author Hiroaki Tateshita
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class MapFragment extends Fragment {
	/**
	 * a kind of hashtable for saving setting information.
	 */
	private SharedPreferences pref;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_map,
				container, false);

		/*
		 * initialize setting by loading setting information from shared
		 * preference.
		 */
		pref = getActivity().getSharedPreferences("gnssfinder",
				Activity.MODE_PRIVATE);
		boolean isGpsBlockIIF = pref.getBoolean("gpsBlockIIF", true);
		boolean isGalileo = pref.getBoolean("galileo", false);
		boolean isQzss = pref.getBoolean("qzss", false);
		String gnssString = "";
		if (isGpsBlockIIF) {
			gnssString += "G";
		}
		if (isGalileo) {
			gnssString += "E";
		}
		if (isQzss) {
			gnssString += "J";
		}
		final String finalGnssString = gnssString;
		WebView webView = (WebView) rootView.findViewById(R.id.webview);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		webView.clearCache(true);
		// webView.loadUrl("http://192.168.1.13:8080/gnss_webclient/sample05.html?gnss="
		// + finalGnssString);
		webView.loadUrl("http://braincopy.org/WebContent/sample05.html?gnss="
				+ finalGnssString);
		// webView.loadUrl("file:///android_asset/jstest.html");
		return rootView;
	}
}
