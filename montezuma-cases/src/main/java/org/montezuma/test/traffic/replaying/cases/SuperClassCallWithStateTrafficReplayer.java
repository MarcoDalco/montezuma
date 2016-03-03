package org.montezuma.test.traffic.replaying.cases;

import analysethis.superclasscall.withstate.SomeClass;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.SuperClassCallWithStateTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class SuperClassCallWithStateTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = SomeClass.class;
		String recordingsSubDir = SuperClassCallWithStateTrafficRecorder.SUPERCLASS_CALL_RECORDING_SUBDIR;
		replay(clazz, CasesCommon.getRecordingsDir(recordingsSubDir));
	}

}
