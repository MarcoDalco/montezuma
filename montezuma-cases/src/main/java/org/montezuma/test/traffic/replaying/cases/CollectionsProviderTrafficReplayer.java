package org.montezuma.test.traffic.replaying.cases;


import org.montezuma.test.traffic.CasesCommon;
import org.montezuma.test.traffic.recording.cases.CollectionsProviderTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import analysethis.collections.CollectionsProvider;

public class CollectionsProviderTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = CollectionsProvider.class;
		String recordingsSubDir = CollectionsProviderTrafficRecorder.COLLECTIONS_PROVIDER_RECORDING_SUBDIR;
		replay(clazz, CasesCommon.getRecordingsDir(recordingsSubDir));
	}

}
