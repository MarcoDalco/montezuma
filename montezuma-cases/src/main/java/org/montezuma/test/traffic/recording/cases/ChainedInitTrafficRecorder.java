package org.montezuma.test.traffic.recording.cases;

import analysethis.returningpassedobjects.ChainedInit;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspectControl;

public class ChainedInitTrafficRecorder {
	public static final String	CHAINEDINIT_RECORDING_SUBDIR	= "chainedinit";

	public static void main(String[] args) throws ClassNotFoundException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspectControl.getInstance().turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new ChainedInit());
				RecordingAspectControl.getInstance().turnOn();

				String both = new ChainedInit().setFirst("first").setSecond("second").getBoth();

				RecordingAspectControl.getInstance().turnOff();
				System.out.println(both);
			}
		}, CHAINEDINIT_RECORDING_SUBDIR);
	}
}
