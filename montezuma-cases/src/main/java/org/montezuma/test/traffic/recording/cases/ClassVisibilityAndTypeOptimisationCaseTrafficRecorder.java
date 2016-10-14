package org.montezuma.test.traffic.recording.cases;

import org.montezuma.test.traffic.recording.TrafficRecorder;
import org.montezuma.test.traffic.recording.aop.aspects.RecordingAspectControl;

import analysethis.privateclassreferencesandtypeoptimisation.ClassVisibilityAndTypeOptimisationCaseMainClass;
import dontanalysethis.privateclassreferencesandtypeoptimisation.ClassA;
import dontanalysethis.privateclassreferencesandtypeoptimisation.ClassC;
import dontanalysethis.privateclassreferencesandtypeoptimisation.InterfaceB;
import dontanalysethis.privateclassreferencesandtypeoptimisation.InterfaceD;

public class ClassVisibilityAndTypeOptimisationCaseTrafficRecorder {
	public static final String	CLASS_VISIBILITY_AND_TYPE_OPTIMISATION_CASE_RECORDING_SUBDIR	= "classvisibilityandtypeoptimisationcase";

	public static void main(String[] args) throws ClassNotFoundException {
		new TrafficRecorder().runRecording(new Runnable() {

			@Override
			public void run() {
				RecordingAspectControl.getInstance().turnOff();
				System.out.println("Loading CUT class to avoid static init processing: " + new ClassVisibilityAndTypeOptimisationCaseTrafficRecorder());
				RecordingAspectControl.getInstance().turnOn();

				ClassVisibilityAndTypeOptimisationCaseMainClass cut = new ClassVisibilityAndTypeOptimisationCaseMainClass();

				final ClassA result1 = cut.getNewSubClassForClassA();
				final InterfaceB result2 = cut.getNewSubClassForInterfaceB();
				final InterfaceD result3 = cut.getNewSubClassForInterfaceD();
				final String result4 = cut.getValueFromNewSubClassForClass();
				final String result5 = cut.getValueFromNewSubClassForInterfaceB();
				final String result6 = cut.getValueFromNewSubClassForInterfaceD();


				ClassVisibilityAndTypeOptimisationCaseMainClass cut2 = new ClassVisibilityAndTypeOptimisationCaseMainClass();

				final ClassA result11 = cut2.getNewSubClassForClassA();
				final InterfaceB result12 = cut2.getNewSubClassForInterfaceB();
				final InterfaceD result13 = cut2.getNewSubClassForInterfaceD();
				final String result14 = cut2.getClassNameOf((ClassC)result11);
				final String result15 = cut2.getClassNameOf((ClassC)result12);
				final String result16 = cut2.getClassNameOf((ClassC)result13);

				RecordingAspectControl.getInstance().turnOff();
				System.out.println(result1);
				System.out.println(result2);
				System.out.println(result3);
				System.out.println(result4);
				System.out.println(result5);
				System.out.println(result6);
				System.out.println(result11);
				System.out.println(result12);
				System.out.println(result13);
				System.out.println(result14);
				System.out.println(result15);
				System.out.println(result16);
			}
		}, CLASS_VISIBILITY_AND_TYPE_OPTIMISATION_CASE_RECORDING_SUBDIR);
	}
}
