package org.montezuma.test.traffic.replaying.cases;

import analysethis.utils.math.CurrencyUtils;

import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.CurrencyUtilsTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class CurrencyUtilsTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = CurrencyUtils.class;
		String recordingsSubDir = CurrencyUtilsTrafficRecorder.CURRENCY_UTILS_RECORDING_SUBDIR;
		replay(clazz, CasesCommon.getRecordingsDir(recordingsSubDir));
	}

}
