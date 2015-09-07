package org.montezuma.test.traffic.replaying.cases;

import analysethis.utils.consumer.UtilsConsumer;

import org.montezuma.test.traffic.recording.cases.UtilsConsumerTrafficRecorder;
import org.montezuma.test.traffic.replaying.TrafficReplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class UtilsConsumerTrafficReplayer extends TrafficReplayer {

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {

		System.out.println("Replaying:");

		final Class<?> clazz = UtilsConsumer.class;
		String recordingSuDdir = UtilsConsumerTrafficRecorder.UTILS_CONSUMER_RECORDING_SUBDIR;
		replay(clazz, recordingSuDdir);
	}

}
