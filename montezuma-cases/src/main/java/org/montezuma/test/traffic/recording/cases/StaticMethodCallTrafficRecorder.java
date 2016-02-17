package org.montezuma.test.traffic.recording.cases;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import java.sql.SQLException;

import analysethis.staticmethods.ClassWithStaticMethods;

public class StaticMethodCallTrafficRecorder {
	public static final String	STATIC_METHOD_CALL_RECORDING_SUBDIR	= "staticmethodcall";

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new ClassWithStaticMethods());
				RecordingAspect.turnOn();

				final String result = ClassWithStaticMethods.staticMethod();
				ClassWithStaticMethods cut = new ClassWithStaticMethods();
				final String result2 = cut.nonStaticMethod();

				RecordingAspect.turnOff();
				System.out.println(result);
				System.out.println(result2);
			}
		}, STATIC_METHOD_CALL_RECORDING_SUBDIR);
	}
}
