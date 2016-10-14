package org.montezuma.test.traffic.recording.cases;

import analysethis.utils.math.CurrencyUtils;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspectControl;

import java.math.BigDecimal;
import java.sql.SQLException;

public class CurrencyUtilsTrafficRecorder {

	public static final String	CURRENCY_UTILS_RECORDING_SUBDIR	= "currencyutils";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspectControl.getInstance().turnOff();
				System.out.println("Instantiating CUT class: " + new CurrencyUtils());
				RecordingAspectControl.getInstance().turnOn();

				CurrencyUtils cut = new CurrencyUtils();
				final String result = cut.formatForDefaultCurrency(BigDecimal.TEN);
				System.out.println(result);

				RecordingAspectControl.getInstance().turnOff();
				System.out.println(result);
			}
		}, CURRENCY_UTILS_RECORDING_SUBDIR);
	}
}
