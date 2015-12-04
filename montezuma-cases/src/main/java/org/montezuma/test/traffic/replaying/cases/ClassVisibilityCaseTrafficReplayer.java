package org.montezuma.test.traffic.replaying.cases;

import org.montezuma.test.traffic.recording.cases.ClassVisibilityCaseTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.untestable.until.privateclassreferencesareworkedaround.ClassVisibilityCaseMainClass;

public class ClassVisibilityCaseTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = ClassVisibilityCaseMainClass.class;
		String recordingSuDdir = ClassVisibilityCaseTrafficRecorder.CLASS_VISIBILITY_CASE_RECORDING_SUBDIR;
		replay(clazz, recordingSuDdir);
	}

}
