package org.montezuma.test.traffic.recording.cases;

import analysethis.utils.math.MonitoredClass;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import dontanalysethis.DummyThirdParty;

import java.sql.SQLException;

public class PassThroughClassTrafficRecorder {
	public static final String	PASSTHROUGH_CLASS_RECORDING_SUBDIR	= "passthrough";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new DummyThirdParty(""));
				System.out.println("Loading CUT class to avoid static init processing: " + new MonitoredClass(""));
				RecordingAspect.turnOn();

				DummyThirdParty dummyThirdParty = new DummyThirdParty("-Tail");
				MonitoredClass cut4 = new MonitoredClass("PRE");

				final String messageEnhancedBy = cut4.getMessageEnhancedBy(dummyThirdParty);
				final String message = cut4.getMessage();

				RecordingAspect.turnOff();

				System.out.println(messageEnhancedBy);
				System.out.println(message);
			}
		}, PASSTHROUGH_CLASS_RECORDING_SUBDIR);
	}
}
