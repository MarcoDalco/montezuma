package org.montezuma.test.traffic.replaying.cases;

import analysethis.returningpassedobjects.ChainedInit;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.ChainedInitTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class ChainedInitTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = ChainedInit.class;
		String recordingsSubDir = ChainedInitTrafficRecorder.CHAINEDINIT_RECORDING_SUBDIR;
		replay(clazz, CasesCommon.getRecordingsDir(recordingsSubDir));
	}

}
