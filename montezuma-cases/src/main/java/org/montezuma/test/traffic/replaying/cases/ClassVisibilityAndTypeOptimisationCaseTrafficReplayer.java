package org.montezuma.test.traffic.replaying.cases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.ClassVisibilityAndTypeOptimisationCaseTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import analysethis.privateclassreferencesandtypeoptimisation.ClassVisibilityAndTypeOptimisationCaseMainClass;


public class ClassVisibilityAndTypeOptimisationCaseTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = ClassVisibilityAndTypeOptimisationCaseMainClass.class;
		String recordingsSubDir = ClassVisibilityAndTypeOptimisationCaseTrafficRecorder.CLASS_VISIBILITY_AND_TYPE_OPTIMISATION_CASE_RECORDING_SUBDIR;
		replay(clazz, CasesCommon.getRecordingsDir(recordingsSubDir));
	}

}
