package org.montezuma.test.traffic.recording.cases;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspectControl;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import analysethis.untestable.until.timefunctionsareworkedaround.TimeConsumer;

public class TimeConsumerTrafficRecorder {
	public static final String	TIME_CONSUMER_RECORDING_SUBDIR	= "timeconsumer";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspectControl.getInstance().turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new TimeConsumer());
				RecordingAspectControl.getInstance().turnOn();

				TimeConsumer cut = new TimeConsumer();

				final Date date = cut.getDate();
				final long millis = cut.getTimeMillis();
				final Calendar cal = cut.getCalendar();
				final long nanos = cut.getNanos();
				final java.sql.Date sqlDate = cut.getSQLDate();
				RecordingAspectControl.getInstance().turnOff();
				System.out.println(date);
				System.out.println(millis);
				System.out.println(cal);
				System.out.println(nanos);
				System.out.println(sqlDate);
			}
		}, TIME_CONSUMER_RECORDING_SUBDIR);
	}
}
