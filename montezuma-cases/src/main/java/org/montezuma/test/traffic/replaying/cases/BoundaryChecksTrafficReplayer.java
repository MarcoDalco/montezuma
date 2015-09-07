package org.montezuma.test.traffic.replaying.cases;

import analysethis.captureboundarychecks.EntryClassToAnalyse;

import org.montezuma.test.traffic.recording.cases.BoundaryChecksTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class BoundaryChecksTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = EntryClassToAnalyse.class;
		String recordingSuDdir = BoundaryChecksTrafficRecorder.BOUNDARY_CHECKS_RECORDING_SUBDIR;
		replay(clazz, recordingSuDdir);
	}

}
