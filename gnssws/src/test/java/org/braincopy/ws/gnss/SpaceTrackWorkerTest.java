package org.braincopy.ws.gnss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;

import org.junit.Test;

import sgp4v.ObjectDecayed;
import sgp4v.SatElsetException;
import sgp4v.Sgp4Data;
import sgp4v.Sgp4Unit;

public class SpaceTrackWorkerTest {
	protected static DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

	@Test
	public void testGetTLEList() {
		SpaceTrackWorker worker = new SpaceTrackWorker();
		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.set(2017, 2, 31, 00, 00, 00);
		try {
			ArrayList<TLEString> tleList = worker.getTLEList(calendar, "G");

			System.out.println("size is " + tleList.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadConnection() {
		SpaceTrackWorker worker = new SpaceTrackWorker();
		GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		calendar.set(2017, 2, 31, 00, 00, 00);
		try {
			worker.loadPropertiesFiles();
		} catch (IOException e1) {
			fail("properties files are strange.");
			e1.printStackTrace();
		}
		try {
			worker.loadConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testGetCalendarFmYearAndDays() {
		SpaceTrackWorker worker = new SpaceTrackWorker();
		Calendar calendarIn = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		DATETIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			calendarIn.setTime(DATETIME_FORMAT.parse("2014-01-01_00:00:00"));
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(1, calendarIn.get(Calendar.DAY_OF_YEAR));

		int year = 2001;
		double days = 45.500;
		Calendar calendar = worker.getCalendarFmYearAndDays(year, days);
		assertEquals(1, calendar.get(Calendar.MONTH));
		assertEquals(12, calendar.get(Calendar.HOUR_OF_DAY));

		year = 2014;
		days = 1.5;
		calendar = worker.getCalendarFmYearAndDays(year, days);
		assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 12, 0.1);

		year = 2014;
		days = 0;
		calendar = worker.getCalendarFmYearAndDays(year, days);
		assertEquals(calendar.get(Calendar.YEAR), 2014);
	}

	@Test
	public void testGetDoubleDay() {
		SpaceTrackWorker worker = new SpaceTrackWorker();
		Calendar calendarIn = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		DATETIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			calendarIn.setTime(DATETIME_FORMAT.parse("2014-01-01_00:00:00"));
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(0, worker.getDoubleDay(calendarIn), 0.1);
		try {
			calendarIn.setTime(DATETIME_FORMAT.parse("2014-01-31_12:00:00"));
		} catch (ParseException e) {
			fail(e.getMessage());
		}
		assertEquals(30.5, worker.getDoubleDay(calendarIn), 0.1);

		Sgp4Unit sgp4 = new Sgp4Unit();
		String tle1 = "1 37158U 10045A   13365.63098170 -.00000068  00000-0  00000+0 0  7033";
		String tle2 = "2 37158 040.6030 177.7422 0747984 270.0326 017.2736 01.00281169 12106";
		int startYear = 2014, stopYear = 2014;
		double startDay = 0, stopDay = 0.5, step = 720;
		try {
			Vector<Sgp4Data> results = sgp4.runSgp4(tle1, tle2, startYear, startDay, stopYear, stopDay, step);
			assertEquals(2, results.size());
		} catch (ObjectDecayed e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (SatElsetException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

	}
}
