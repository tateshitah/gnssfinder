package org.braincopy.gnssfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.braincopy.silbala.ARActivity;
import org.braincopy.silbala.ARObjectDialog;
import org.braincopy.silbala.Point;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

/**
 * a Activity class of for camera mode of GNSS Finder.
 * 
 * @author Hiroaki Tateshita
 * @version 0.7.2
 * 
 */
public class CameraActivity extends ARActivity {
	float gapBtwnWindowAndView;

	private GNSSARView gnssArView;
	// private float lat, lon;
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

	/**
	 * This method is to check the object of this class has the latest
	 * information related to the satellites in the internal database or not. If
	 * yes, return true. if no, return false.
	 * 
	 * This method might be used in the future.
	 * 
	 * @return
	 */
	private boolean checkInformation() {
		boolean result = false;

		return result;
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
				loadImages();
				worker.setStatus(SatelliteInfoWorker.IMAGE_LOADED);
				gnssArView.setSatellites(satellites);
				gnssArView.setStatus("getting location...");
				// this.touchedFlags = new boolean[this.satellites.length];
				// for (int i = 0; i < this.touchedFlags.length; i++) {
				// this.touchedFlags[i] = false;
				// }
			} else if (worker.getStatus() == SatelliteInfoWorker.IMAGE_LOADED
					&& this.isUsingGPS()) {
				// in this moment, the satellite images should be already
				// loaded. so just move to complete mode.
				// this.satellites = worker.getSatArray();
				// if (loadImages()) {
				worker.setStatus(SatelliteInfoWorker.COMPLETED);
				gnssArView.setStatus("completed to show satellites");
				// gnssArView.setSatellites(satellites);
				// }
			}
		}
	}

	/**
	 * Load satellite images from satellite database information. If all
	 * information is loaded successfully, this method return true, if not
	 * return false.
	 * 
	 * @return
	 */
	private boolean loadImages() {
		boolean result = false;

		if (checkInformation()) {
			result = loadInformationFromDB();// this method will be used in the
												// future.
		} else {
			result = loadInformationFromNW();
		}
		return result;
	}

	private boolean loadInformationFromNW() {
		boolean result = false;
		Resources resources = this.getResources();
		InputStream is;
		String strFolder = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
				+ "/gnssfinder/";
		File file = new File(strFolder + "satelliteDataBase.txt");

		try {
			// AssetManager assetManager = resources.getAssets();
			// is = assetManager.open("satelliteDataBase.txt");
			is = new FileInputStream(file);
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
						satellites[i].setImage(Satellite.getGNSSImage(gnssStr,
								resources));
						satellites[i].setGnssStr(gnssStr);
						/*
						 * if (gnssStr.equals("qzss")) { satellites[i]
						 * .setImage(BitmapFactory.decodeResource( resources,
						 * R.drawable.qzss)); } else if
						 * (gnssStr.equals("galileo")) {
						 * satellites[i].setImage(BitmapFactory
						 * .decodeResource(resources, R.drawable.galileo)); }
						 * else if (gnssStr.equals("galileofoc")) {
						 * satellites[i].setImage(BitmapFactory
						 * .decodeResource(resources, R.drawable.galileofoc)); }
						 * else if (gnssStr.equals("gpsBlockIIF")) {
						 * satellites[i].setImage(BitmapFactory
						 * .decodeResource(resources, R.drawable.iif)); }
						 */
					} else {
						satellites[i] = null;
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

	/**
	 * 
	 * @return
	 */
	private boolean loadInformationFromDB() {
		boolean result = false;
		Resources resources = this.getResources();
		SQLiteOpenHelper helper = new MySQLiteOpenHelper(
				getApplicationContext());
		SQLiteDatabase sat_db = helper.getWritableDatabase();
		String sql = "select norad_cat_id, image_name, description from satellite";
		Cursor cursor = sat_db.rawQuery(sql, null);
		cursor.moveToFirst();
		String gnssStr = "";
		while (cursor.moveToNext()) {
			gnssStr = cursor.getString(1);
			for (int i = 0; i < satellites.length; i++) {
				if (satellites[i].getCatNo().equals(cursor.getString(0))) {
					satellites[i].setDescription(cursor.getString(2));
					if (gnssStr.equals("qzss")) {
						satellites[i].setImage(BitmapFactory.decodeResource(
								resources, R.drawable.qzss));
					} else if (gnssStr.equals("galileo")) {
						satellites[i].setImage(BitmapFactory.decodeResource(
								resources, R.drawable.galileo));
					} else if (gnssStr.equals("galileofoc")) {
						satellites[i].setImage(BitmapFactory.decodeResource(
								resources, R.drawable.galileofoc));
					} else if (gnssStr.equals("gpsBlockIIF")) {
						satellites[i].setImage(BitmapFactory.decodeResource(
								resources, R.drawable.iif));
					}
					break;
				}
			}
		}

		return result;
	}

	private static class MySQLiteOpenHelper extends SQLiteOpenHelper {
		/**
		 * If DB_NAME is not null, the data will be saved in the device with its
		 * file name.
		 */
		static String DB_NAME = "satellite.database";
		static int DB_VERSION = 1;
		static final String CREATE_TABLE = "create table satellite ( norad_cat_id text primary key, rinex_id text, image_name text not null, description text );";
		static final String DROP_TABLE = "drop table satellite;";

		/**
		 * 
		 * @param c
		 */
		public MySQLiteOpenHelper(Context c) {
			super(c, DB_NAME, null, DB_VERSION);
		}

		/**
		 * if the data base file already exists, this method will not be called.
		 */
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(DROP_TABLE);
			onCreate(db);
		}
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

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x, y;
		if (this.gapBtwnWindowAndView == 0) {
			gapBtwnWindowAndView = getWindow().getDecorView().getHeight()
					- getARView().getHeight();
		}
		x = event.getX();
		y = event.getY() - gapBtwnWindowAndView;
		if (satellites != null) {
			for (int i = 0; i < satellites.length; i++) {
				if (satellites[i] != null) {
					Point point = satellites[i].getPoint();
					if (point != null) {
						if (Math.abs(x - point.x) < TOUCH_AREA_SIZE
								&& Math.abs(y - point.y) < TOUCH_AREA_SIZE
								&& !satellites[i].isTouched()) {
							satellites[i].setTouched(true);

							String message = satellites[i].getDescription();
							ARObjectDialog dialog2 = new SatelliteDialog();
							Bundle args = new Bundle();
							args.putString("message", message);
							args.putInt("index", i + 1);
							args.putString("catNo", satellites[i].getCatNo());
							args.putString("gnssStr",
									satellites[i].getGnssStr());
							dialog2.setArguments(args);
							dialog2.show(getFragmentManager(), "tag?");
						}
					}
				}
			}
		}
		return super.onTouchEvent(event);
	}
}
