package org.montezuma.test.traffic.replaying.cases;

import analysethis.utils.math.BigDecimalUtils;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.BigDecimalUtilsTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class BigDecimalUtilsTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = BigDecimalUtils.class;
		String recordingsSubDir = BigDecimalUtilsTrafficRecorder.BIGDECIMAL_UTILS_RECORDING_SUBDIR;
		replay(clazz, CasesCommon.getRecordingsDir(recordingsSubDir));
	}

}
