package org.montezuma.test.traffic.replaying.cases;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.StaticMethodCallTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.staticmethods.ClassWithStaticMethods;

public class StaticMethodCallTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = ClassWithStaticMethods.class;
		String recordingsSubDir = StaticMethodCallTrafficRecorder.STATIC_METHOD_CALL_RECORDING_SUBDIR;
		replay(clazz, CasesCommon.getRecordingsDir(recordingsSubDir));
	}

}
