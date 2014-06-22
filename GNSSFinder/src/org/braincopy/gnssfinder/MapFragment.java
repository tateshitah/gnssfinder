package org.braincopy.gnssfinder;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_map,
				container, false);
		WebView webView = (WebView) rootView.findViewById(R.id.webview);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i("hiro", "hello");
				((WebView) v).loadUrl("javascript:startPlot('J')");
				return false;
			}
		});
		// webView.loadUrl("http://192.168.1.13:8080/gnss_webclient/sample05.html");
		webView.loadUrl("http://braincopy.org/WebContent/sample04.html");
		return rootView;
	}
}
