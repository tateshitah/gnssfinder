package org.braincopy.gnssfinder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.braincopy.silbala.ARActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * a Activity class of for camera mode of GNSS Finder.
 * 
 * @author Hiroaki Tateshita
 * @version 0.1.0
 * 
 */
public class CameraActivity extends ARActivity {

	private GNSSARView gnssArView;
	private float lat, lon;
	private Satellite[] satellites;
	private SatelliteInfoWorker worker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gnssArView = new GNSSARView(this);

		this.setARView(gnssArView);

		final String gnssString = SettingFragment.getGNSSString(this);
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			worker = new SatelliteInfoWorker();
			worker.setLatLon(lat, lon);
			worker.setCurrentDate(new Date(System.currentTimeMillis()));
			worker.setGnssString(gnssString);
			worker.start();
			worker.setStatus(SatelliteInfoWorker.CONNECTED);
			gnssArView.setStatus("connected");
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		// cameraView.stopPreviewAndFreeCamera();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		super.onSensorChanged(event);

		if (worker != null) {
			if (worker.getStatus() == SatelliteInfoWorker.INFORMATION_LOADED_WO_LOCATION) {
				this.satellites = worker.getSatArray();
				if (loadImages()) {
					worker.setStatus(SatelliteInfoWorker.IMAGE_LOADED);
					gnssArView.setSatellites(satellites);
					gnssArView.setStatus("getting location...");
				}
			} else if (worker.getStatus() == SatelliteInfoWorker.INFORMATION_LOADED_W_LOCATION) {
				this.satellites = worker.getSatArray();
				if (loadImages()) {
					worker.setStatus(SatelliteInfoWorker.COMPLETED);
					gnssArView.setSatellites(satellites);
				}
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private boolean loadImages() {
		boolean result = false;
		Resources resources = this.getResources();
		InputStream is;
		try {
			AssetManager assetManager = resources.getAssets();
			is = assetManager.open("satelliteDataBase.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			ArrayList<String[]> datalist = new ArrayList<String[]>();
			String buf = null;
			try {
				while ((buf = br.readLine()) != null) {
					datalist.add(buf.split("\t"));
				}
				String gnssStr = "";
				for (int i = 0; i < satellites.length; i++) {
					gnssStr = getGnssStr(satellites[i].getCatNo(), datalist);
					if (gnssStr != null) {
						satellites[i].setDescription(getSatInfo(
								satellites[i].getCatNo(), datalist));
						if (gnssStr.equals("qzss")) {
							satellites[i]
									.setImage(BitmapFactory.decodeResource(
											resources, R.drawable.qzss));
						} else if (gnssStr.equals("galileo")) {
							satellites[i].setImage(BitmapFactory
									.decodeResource(resources,
											R.drawable.galileo));
						} else if (gnssStr.equals("galileofoc")) {
							satellites[i].setImage(BitmapFactory
									.decodeResource(resources,
											R.drawable.galileofoc));
						} else if (gnssStr.equals("gpsBlockIIF")) {
							satellites[i].setImage(BitmapFactory
									.decodeResource(resources, R.drawable.iif));
						}
					}
				}
				result = true;
			} catch (IOException e) {
				Log.e("hiro",
						"failed when to read a line of the satellite database text file."
								+ e);
				e.printStackTrace();
			}
			is.close();
			br.close();
		} catch (FileNotFoundException e) {
			Log.e("hiro", "" + e);
			e.printStackTrace();
		} catch (IOException e1) {
			Log.e("hiro", "" + e1);
			e1.printStackTrace();
		}

		return result;
	}

	private String getSatInfo(String catNo, ArrayList<String[]> datalist) {
		String result = null;
		String[] tmpStrArray = null;
		for (int i = 0; i < datalist.size(); i++) {
			tmpStrArray = datalist.get(i);
			if (catNo.equals(tmpStrArray[0])) {
				result = tmpStrArray[3];
				break;
			}
		}
		return result;
	}

	private String getGnssStr(String catNo, ArrayList<String[]> list) {
		String result = null;
		String[] tmpStrArray = null;
		for (int i = 0; i < list.size(); i++) {
			tmpStrArray = list.get(i);
			if (catNo.equals(tmpStrArray[0])) {
				result = tmpStrArray[2];
				break;
			}
		}
		return result;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		super.onLocationChanged(arg0);

		if (this.satellites != null && worker != null) {
			if (worker.getStatus() == SatelliteInfoWorker.IMAGE_LOADED) {
				worker = new SatelliteInfoWorker();
				worker.setStatus(SatelliteInfoWorker.IMAGE_LOADED);
				worker.setLatLon(lat, lon);
				worker.setCurrentDate(new Date(System.currentTimeMillis()));
				worker.start();
			} else if (worker.getStatus() == SatelliteInfoWorker.COMPLETED) {
				this.gnssArView
						.setStatus("position is updated and information loaded.");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_bar_back) {
			Intent intent = new Intent(this.getApplicationContext(),
					MainActivity.class);
			this.startActivity(intent);
			return true;
		} else if (id == R.id.action_close) {
			this.moveTaskToBack(true);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
