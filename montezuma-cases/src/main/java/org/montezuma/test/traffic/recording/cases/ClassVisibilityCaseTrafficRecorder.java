package org.montezuma.test.traffic.recording.cases;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import analysethis.untestable.until.privateclassreferencesareworkedaround.ClassVisibilityCaseMainClass;
import dontanalisethis.untestable.until.privateclassreferencesareworkedaround.VisibleClass;
import dontanalisethis.untestable.until.privateclassreferencesareworkedaround.VisibleInterface;

public class ClassVisibilityCaseTrafficRecorder {
	public static final String	CLASS_VISIBILITY_CASE_RECORDING_SUBDIR	= "classvisibilitycase";

	public static void main(String[] args) throws ClassNotFoundException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspect.turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new ClassVisibilityCaseTrafficRecorder());
				RecordingAspect.turnOn();

				ClassVisibilityCaseMainClass cut1 = new ClassVisibilityCaseMainClass();
				ClassVisibilityCaseMainClass cut2 = new ClassVisibilityCaseMainClass();
				ClassVisibilityCaseMainClass cut3 = new ClassVisibilityCaseMainClass();
				ClassVisibilityCaseMainClass cut4 = new ClassVisibilityCaseMainClass();

				final VisibleClass result1 = cut1.getNewSubClassForClass();
				final VisibleInterface result2 = cut2.getNewSubClassForInterface();
				final String result3 = cut3.getValueFromNewSubClassForClass();
				final String result4 = cut4.getValueFromNewSubClassForInterface();
				RecordingAspect.turnOff();
				System.out.println(result1);
				System.out.println(result2);
				System.out.println(result3);
				System.out.println(result4);
			}
		}, CLASS_VISIBILITY_CASE_RECORDING_SUBDIR);
	}
}
