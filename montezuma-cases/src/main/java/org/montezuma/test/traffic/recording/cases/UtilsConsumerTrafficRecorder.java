package org.montezuma.test.traffic.recording.cases;

import analysethis.utils.consumer.UtilsConsumer;
import analysethis.utils.math.BigDecimalUtils;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import java.math.BigDecimal;
import java.sql.SQLException;

public class UtilsConsumerTrafficRecorder {
	public static final String	UTILS_CONSUMER_RECORDING_SUBDIR	= "utilsconsumer";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new UtilsConsumer());
				System.out.println("Loading CUT class to avoid static init processing: " + new BigDecimalUtils());
				RecordingAspect.turnOn();

				UtilsConsumer cut = new UtilsConsumer();

				final BigDecimal result1 = cut.doSomething("12");
				final BigDecimal result2 = cut.doSomethingReturningNull();
				RecordingAspect.turnOff();
				System.out.println(result1);
				System.out.println(result2);
			}
		}, UTILS_CONSUMER_RECORDING_SUBDIR);
	}
}
