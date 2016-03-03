package org.montezuma.test.traffic.replaying.cases;

import analysethis.untestable.until.westopmockingalltheclasseswithintheboundaryinsteadofjusttheentryone.B;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.BoundaryChecksCallbackTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class BoundaryChecksCallbackTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = B.class;
		String recordingsSubDir = BoundaryChecksCallbackTrafficRecorder.BOUNDARY_CHECKS_RECORDING_SUBDIR;
		replay(clazz, CasesCommon.getRecordingsDir(recordingsSubDir));
	}

}
