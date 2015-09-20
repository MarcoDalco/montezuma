package org.montezuma.test.traffic.replaying.cases;

import analysethis.untestable.until.canruncodebetweenexpectedinvocationandreturningvalue.B;

import org.montezuma.test.traffic.recording.cases.BoundaryChecksWithStateChangeinBothCallForthAndCallBackTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class BoundaryChecksWithStateChangeInBothCallForthAndCallBackTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = B.class;
		String recordingSuDdir = BoundaryChecksWithStateChangeinBothCallForthAndCallBackTrafficRecorder.BOUNDARY_CHECKS_RECORDING_SUBDIR;
		replay(clazz, recordingSuDdir);
	}

}
