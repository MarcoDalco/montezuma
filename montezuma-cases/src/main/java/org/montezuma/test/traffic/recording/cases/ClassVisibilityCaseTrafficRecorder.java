package org.montezuma.test.traffic.recording.cases;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspect;

import analysethis.privateclassreferences.ClassVisibilityCaseMainClass;
import dontanalisethis.privateclassreferences.VisibleClass;
import dontanalisethis.privateclassreferences.VisibleInterface;

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

				final VisibleClass result1 = cut1.getNewSubClassForClass();
				final VisibleInterface result2 = cut1.getNewSubClassForInterface();
				final String result3 = cut1.getValueFromNewSubClassForClass();
				final String result4 = cut1.getValueFromNewSubClassForInterface();
				RecordingAspect.turnOff();
				System.out.println(result1);
				System.out.println(result2);
				System.out.println(result3);
				System.out.println(result4);
			}
		}, CLASS_VISIBILITY_CASE_RECORDING_SUBDIR);
	}
}
