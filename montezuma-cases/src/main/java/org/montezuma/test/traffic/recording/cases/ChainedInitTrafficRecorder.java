package org.montezuma.test.traffic.recording.cases;

import analysethis.returningpassedobjects.ChainedInit;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

public class ChainedInitTrafficRecorder {
	public static final String	CHAINEDINIT_RECORDING_SUBDIR	= "chainedinit";

	public static void main(String[] args) throws ClassNotFoundException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new ChainedInit());
				RecordingAspect.turnOn();

				String both = new ChainedInit().setFirst("first").setSecond("second").getBoth();

				RecordingAspect.turnOff();
				System.out.println(both);
			}
		}, CHAINEDINIT_RECORDING_SUBDIR);
	}
}
