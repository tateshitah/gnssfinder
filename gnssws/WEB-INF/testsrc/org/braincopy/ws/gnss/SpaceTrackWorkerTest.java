package org.braincopy.ws.gnss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

public class SpaceTrackWorkerTest {
	protected static DateFormat DATETIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd_HH:mm:ss");

	@Test
	public void testGetCalendarFmYearAndDays() {
		SpaceTrackWorker worker = new SpaceTrackWorker();
		Calendar calendarIn = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		DATETIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			calendarIn.setTime(DATETIME_FORMAT.parse("2012-08-21_12:00:00"));
		} catch (ParseException e) {
			fail(e.getMessage());
		}

		int year = 2001;
		double days = 45.500;
		Calendar calendar = worker.getCalendarFmYearAndDays(year, days);
		assertEquals(1, calendar.get(Calendar.MONTH));
		assertEquals(12, calendar.get(Calendar.HOUR_OF_DAY));
	}
}
