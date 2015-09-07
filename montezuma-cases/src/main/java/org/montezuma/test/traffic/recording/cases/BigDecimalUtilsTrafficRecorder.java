package org.montezuma.test.traffic.recording.cases;

import analysethis.utils.math.BigDecimalUtils;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import java.math.BigDecimal;
import java.sql.SQLException;

public class BigDecimalUtilsTrafficRecorder {
	public static final String	BIGDECIMAL_UTILS_RECORDING_SUBDIR	= "bigdecimalutils";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new BigDecimalUtils());
				RecordingAspect.turnOn();

				BigDecimalUtils cut2 = new BigDecimalUtils();
				BigDecimalUtils cut3 = new BigDecimalUtils();

				final Double value = new Double(5.0);
				final BigDecimal result1 = cut2.toBigDecimal(value);
				final BigDecimal result2 = cut2.toBigDecimal("8.01");
				final BigDecimal result3 = cut3.toBigDecimal("", BigDecimal.TEN);
				RecordingAspect.turnOff();
				System.out.println(result1);
				System.out.println(result2);
				System.out.println(result3);
			}
		}, BIGDECIMAL_UTILS_RECORDING_SUBDIR);
	}
}
