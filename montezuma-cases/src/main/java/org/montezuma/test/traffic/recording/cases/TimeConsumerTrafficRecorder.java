package org.montezuma.test.traffic.recording.cases;

import analysethis.utils.time.TimeConsumer;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class TimeConsumerTrafficRecorder {
	public static final String	TIME_CONSUMER_RECORDING_SUBDIR	= "timeconsumer";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new TimeConsumer());
				RecordingAspect.turnOn();

				TimeConsumer cut = new TimeConsumer();

				final Date date = cut.getDate();
				final long millis = cut.getTimeMillis();
				final Calendar cal = cut.getCalendar();
				final long nanos = cut.getNanos();
				final java.sql.Date sqlDate = cut.getSQLDate();
				RecordingAspect.turnOff();
				System.out.println(date);
				System.out.println(millis);
				System.out.println(cal);
				System.out.println(nanos);
				System.out.println(sqlDate);
			}
		}, TIME_CONSUMER_RECORDING_SUBDIR);
	}
}
